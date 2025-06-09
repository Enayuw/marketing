package com.br.marketing.retry;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DatabaseOperationService {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 重试配置
     */
    @Data
    @Builder
    public static class RetryConfig {
        @Builder.Default private int maxRetries = 3;
        @Builder.Default private long initialDelay = 1000L;
        @Builder.Default private long maxDelay = 5000L;
        @Builder.Default private boolean exponentialBackoff = true;
    }

    /**
     * 执行数据库操作（带重试和SQL打印）
     */
    public void executeWithRetry(SqlOperation operation, String operationName, RetryConfig config) {
        int retryCount = 0;
        long delay = config.getInitialDelay();
        while (retryCount <= config.getMaxRetries()) {
            try {
                // 执行实际操作
                operation.execute();
                return;
            } catch (Exception e) {
                if (retryCount == config.getMaxRetries()) {
                    //获取SQL和参数
                    String sql = getSqlStatement(operation);
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(),
                            "数据库操作异常，场景：" + operationName + "，尝试次数：" + retryCount + "，执行sql：" + sql), e);
                    throw e;
                }
                sleep(delay);
                if (config.isExponentialBackoff()) {
                    delay = Math.min(delay * 2, config.getMaxDelay());
                }
                retryCount++;
            }
        }
    }

    /**
     * SQL操作接口
     */
    public interface SqlOperation {
        void execute();
        Object getParams();
        String getMapperClass();
        String getMapperMethod();
    }

    /**
     * 获取SQL语句
     */
    private String getSqlStatement(SqlOperation operation) {
        try {
            Configuration configuration = sqlSessionFactory.getConfiguration();
            String statementId = operation.getMapperClass() + "." + operation.getMapperMethod();
            MappedStatement mappedStatement = configuration.getMappedStatement(statementId);
            BoundSql boundSql = mappedStatement.getBoundSql(operation.getParams());
            String completeSql = getCompleteSql(boundSql);
            return completeSql;
        } catch (Exception e) {
            log.warn("获取SQL失败", e);
            return "无法获取SQL";
        }
    }

    public String getCompleteSql(BoundSql boundSql) {
        String sql = boundSql.getSql();
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            return sql;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ParameterMapping mapping : parameterMappings) {
            String property = mapping.getProperty();
            Object value;
            // 处理动态参数
            if (boundSql.hasAdditionalParameter(property)) {
                value = boundSql.getAdditionalParameter(property);
            } else if (parameterObject == null) {
                value = null;
            } else if (parameterObject instanceof Map) {
                value = ((Map<?, ?>) parameterObject).get(property);
            } else {
                MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
                value = metaObject.getValue(property);
            }
            if (value instanceof Date) {
                Date date = (Date) value;
                LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                value = ldt.format(formatter);
            }
            if (value instanceof String) {
                value = "'" + value + "'";
            }
            sql = sql.replaceFirst("\\?", value.toString());
        }
        //格式化
        return sql.replaceAll("\\s+", " ").trim();
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}