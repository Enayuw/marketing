package com.br.marketing.entity;

import lombok.Data;

/**
 * 前端页面 跑分模型分布 保存任务记录参数对象
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-15
 */
@Data
public class ReportTaskVO {
    /**
     * 跑分文件id（多个以逗号分隔）
     */
    String ids;
    /**
     * 勾选跑分文件对应的cid
     */
    String cid;
    /**
     * 报告名称
     */
    String reportName;
    /**
     * 页面配置的 跑分模型 规则
     */
    String rules;

}
