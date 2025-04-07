package com.br.marketing.retry;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
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
        @Builder.Default private boolean printSqlOnError = true;
    }

    /**
     * 执行数据库操作（带重试和SQL打印）
     */
    public <T> T executeWithRetry(SqlOperation<T> operation, String operationName, RetryConfig config) {
        int retryCount = 0;
        long delay = config.getInitialDelay();
        Exception lastException = null;

        while (retryCount <= config.getMaxRetries()) {
            try {
                if (retryCount > 0) {
                    log.info("{}操作重试第{}次", operationName, retryCount);
                }

                // 获取SQL和参数
                String sql = getSqlStatement(operation);
                Map<String, Object> params = operation.getParams();

                // 执行操作前打印SQL
                logSql(sql, params);

                // 执行实际操作
                T result = operation.execute();

                if (retryCount > 0) {
                    log.info("{}操作重试成功", operationName);
                }

                return result;

            } catch (Exception e) {
                lastException = e;
                log.warn("{}操作失败，重试次数：{}", operationName, retryCount, e);

                if (retryCount == config.getMaxRetries()) {
                    if (config.isPrintSqlOnError()) {
                        // 最后一次失败时打印完整SQL信息
                        printDetailedSqlInfo(operation, e);
                    }
                    throw new RuntimeException("操作失败，重试次数耗尽", e);
                }

                sleep(delay);

                if (config.isExponentialBackoff()) {
                    delay = Math.min(delay * 2, config.getMaxDelay());
                }

                retryCount++;
            }
        }

        throw new RuntimeException("Unexpected error", lastException);
    }

    /**
     * SQL操作接口
     */
    public interface SqlOperation<T> {
        T execute();
        default Map<String, Object> getParams() {
            return new HashMap<>();
        }
        String getMapperClass();
        String getMapperMethod();
    }

    /**
     * 获取SQL语句
     */
    private String getSqlStatement(SqlOperation<?> operation) {
        try {
            Configuration configuration = sqlSessionFactory.getConfiguration();
            String statementId = operation.getMapperClass() + "." + operation.getMapperMethod();
            MappedStatement mappedStatement = configuration.getMappedStatement(statementId);
            BoundSql boundSql = mappedStatement.getBoundSql(operation.getParams());
            return boundSql.getSql();
        } catch (Exception e) {
            log.warn("获取SQL失败", e);
            return "无法获取SQL";
        }
    }

    /**
     * 打印SQL和参数
     */
    private void logSql(String sql, Map<String, Object> params) {
        if (log.isDebugEnabled()) {
            log.debug("执行SQL: {}", formatSql(sql));
            log.debug("参数: {}", params);
        }
    }

    /**
     * 打印详细的SQL信息
     */
    private void printDetailedSqlInfo(SqlOperation<?> operation, Exception e) {
        log.error("=== SQL执行失败详细信息 ===");
        log.error("Mapper类: {}", operation.getMapperClass());
        log.error("方法名: {}", operation.getMapperMethod());
        log.error("SQL: {}", formatSql(getSqlStatement(operation)));
        log.error("参数: {}", operation.getParams());
        log.error("异常: ", e);
    }

    /**
     * 格式化SQL
     */
    private String formatSql(String sql) {
        if (sql == null) {
            return "";
        }
        // 去除多余的空白字符
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