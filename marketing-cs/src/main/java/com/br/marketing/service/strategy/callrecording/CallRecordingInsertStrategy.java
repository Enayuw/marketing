package com.br.marketing.service.strategy.callrecording;


/**
 * CallRecording插入策略接口
 * 
 * @author kongbx
 * @date 2025/11/26
 */
public interface CallRecordingInsertStrategy {

    /**
     * 获取策略支持的apiCode
     * 
     * @return apiCode字符串，如果支持所有则返回null
     */
    String getApiCode();

    /**
     * 根据版本明细表数据构建CallRecording实体
     *
     */
    void buildCallRecording();

}

