package com.br.marketing.mq;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class BatchAddColumnsTest {

    // 数据库连接配置
    private static final String URL = "jdbc:mysql://tidb-pre.brapp.com:4000/marketing?useUnicode=true&characterEncoding=UTF-8";
    private static final String USERNAME = "u_pd_marketing";
    private static final String PASSWORD = "f9daIYUIAXQkeyVbDoOI";
    
    // 线程池配置
    private static final int THREAD_POOL_SIZE = 20;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    
    @Test
    public void batchAddColumnsToSyncTables() {
        log.info("开始批量添加字段到b_marketing_sync_表，线程池大小: {}", THREAD_POOL_SIZE);
        
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            // 1. 获取数据库连接
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            statement = connection.createStatement();
            
            // 2. 查询所有b_marketing_sync_开头且后面是数字的表
            String querySql = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME REGEXP '^b_marketing_sync_[0-9]+$'";
            
            resultSet = statement.executeQuery(querySql);
            List<String> tables = new ArrayList<>();
            
            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLE_NAME"));
            }
            
            log.info("找到{}个表需要添加字段", tables.size());
            
            // 3. 并发处理表
            List<CompletableFuture<TableResult>> futures = new ArrayList<>();
            AtomicInteger processedCount = new AtomicInteger(0);
            
            for (String tableName : tables) {
                CompletableFuture<TableResult> future = CompletableFuture.supplyAsync(() -> {
                    return processTable(tableName, processedCount);
                }, executorService);
                futures.add(future);
            }
            
            // 4. 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(60, TimeUnit.MINUTES);
            
            // 5. 收集结果
            int successCount = 0;
            int failCount = 0;
            List<String> failedTables = new ArrayList<>();
            
            for (CompletableFuture<TableResult> future : futures) {
                try {
                    TableResult result = future.get();
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                        failedTables.add(result.getTableName());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("获取任务结果失败", e);
                }
            }
            
            log.info("批量添加字段完成: 总数={}, 成功={}, 失败={}", tables.size(), successCount, failCount);
            if (!failedTables.isEmpty()) {
                log.error("失败的表: {}", failedTables);
            }
            
        } catch (Exception e) {
            log.error("批量添加字段过程中发生异常", e);
            throw new RuntimeException("批量添加字段失败", e);
        } finally {
            // 6. 关闭资源
            closeResources(resultSet, statement, connection);
            executorService.shutdown();
        }
    }
    
    /**
     * 处理单个表
     */
    private TableResult processTable(String tableName, AtomicInteger processedCount) {
        TableResult result = new TableResult(tableName);
        
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            // 检查字段是否已存在
            if (isColumnExists(connection, tableName, "cell_original")) {
                log.debug("表 {} 的字段已存在，跳过", tableName);
                result.setSuccess(true);
                return result;
            }
            
            try (Statement stmt = connection.createStatement()) {
                // 1. 先添加 cell_original 字段到 cell_sha256 后面
                String alterSql1 = String.format(
                    "ALTER TABLE `%s` " +
                    "ADD COLUMN `cell_original` VARCHAR(255) DEFAULT NULL COMMENT '手机号原始值' AFTER `cell_sha256`",
                    tableName
                );
                stmt.execute(alterSql1);
                
                // 2. 再添加 id_card_original 字段到 cell_original 后面
                String alterSql2 = String.format(
                    "ALTER TABLE `%s` " +
                    "ADD COLUMN `id_card_original` VARCHAR(255) DEFAULT NULL COMMENT '身份证号原始值' AFTER `cell_original`",
                    tableName
                );
                stmt.execute(alterSql2);
                
                // 3. 最后添加 name_original 字段到 id_card_original 后面
                String alterSql3 = String.format(
                    "ALTER TABLE `%s` " +
                    "ADD COLUMN `name_original` VARCHAR(255) DEFAULT NULL COMMENT '姓名原始值' AFTER `id_card_original`",
                    tableName
                );
                stmt.execute(alterSql3);
                
                int current = processedCount.incrementAndGet();
                log.info("成功为表 {} 添加字段 ({})", tableName, current);
                result.setSuccess(true);
            }
            
        } catch (Exception e) {
            log.error("为表 {} 添加字段失败: {}", tableName, e.getMessage());
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 检查字段是否已存在
     */
    private boolean isColumnExists(Connection connection, String tableName, String columnName) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet columns = metaData.getColumns(null, null, tableName, columnName);
        boolean exists = columns.next();
        columns.close();
        return exists;
    }
    
    /**
     * 关闭数据库资源
     */
    private void closeResources(ResultSet resultSet, Statement statement, Connection connection) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (Exception e) {
            log.warn("关闭ResultSet失败", e);
        }
        
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (Exception e) {
            log.warn("关闭Statement失败", e);
        }
        
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            log.warn("关闭Connection失败", e);
        }
    }
    
    /**
     * 测试数据库连接
     */
    @Test
    public void testConnection() {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            log.info("数据库连接测试成功");
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            throw new RuntimeException("数据库连接失败", e);
        }
    }
    
    /**
     * 表处理结果类
     */
    private static class TableResult {
        private String tableName;
        private boolean success;
        private String errorMessage;
        
        public TableResult(String tableName) {
            this.tableName = tableName;
        }
        
        // getters and setters
        public String getTableName() { return tableName; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}