package com.br.marketing.service.strategy.callrecording;


import com.br.marketing.entity.CallRecordLLMResultV2;
import java.util.List;

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
    List<String> getApiCodes();


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

