package com.br.marketing.service;

/**
 * 数禾促复借每日自动化匹配数据相关接口
 *
 * @author senyang.zheng
 * @date 2024/10/21
 */
public interface ShuHeCuFuJieMatchDataService {
    void matchData(String condition, String apiCode, String date, String batchNumber, Boolean forceFlag, Long fieldId);
}
