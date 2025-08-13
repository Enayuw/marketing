package com.br.marketing.service;

/**
 * 短链规则服务接口
 * @author system
 * @date 2025/01/17
 */
public interface LinkRuleService {

    /**
     * 创建导出任务
     * @param taskName 任务名称
     * @param dataSource 数据源编码
     * @param exportHeaders 导出表头
     * @param fieldMapping 字段映射JSON
     * @param queryCondition 查询条件JSON
     * @param estimatedRows 预估行数
     * @param fileNameTemplate 导出文件名模板
     * @param userName 用户名
     * @return 创建结果
     */
    Boolean createTask(String taskName,
                       Integer dataSource,
                       String exportHeaders,
                       String fieldMapping,
                       String queryCondition,
                       Long estimatedRows,
                       String fileNameTemplate,
                       String userName);
}
