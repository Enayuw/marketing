package com.br.marketing.service.strategy.callrecording;


import com.br.marketing.entity.CallRecordLLMResultV2;

/**
 * CallRecording插入策略接口
 *
 * @author kongbx
 * @date 2025/11/26
 */
public interface CallRecordingInsertStrategy {

    /**
     * 是否需要处理
     *
     * @param callRecordLLMResultV2 callRecordLLMResultV2
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2025/11/26
     */
    Boolean isProcessingRequired(CallRecordLLMResultV2 callRecordLLMResultV2);

    /**
     * 通话明细数据处理
     *
     * @param callRecordLLMResultV2 callRecordLLMResultV2
     * @author senyang.zheng
     * @date 2025/11/26
     */
    void process(CallRecordLLMResultV2 callRecordLLMResultV2);

}

