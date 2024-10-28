package com.br.marketing.service;

/**
 * 数禾促复借每日自动化匹配数据相关接口
 *
 * @author senyang.zheng
 * @date 2024/10/21
 */
public interface ShuHeCuFuJieMatchDataService {
    /**
     * @param condition 跑分规则筛选条件
     * @param apiCode apiCode
     * @param date 促复借文件拉取日期
     * @param batchNumber 跑分编号
     * @param forceFlag 强制全量清洗标识
     * @param fieldId 跑分任务主键id
     */
    void matchData(String condition, String apiCode, String date, String batchNumber, Boolean forceFlag, Long fieldId);
}
