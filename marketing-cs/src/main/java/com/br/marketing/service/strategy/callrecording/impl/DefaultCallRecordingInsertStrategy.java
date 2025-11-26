package com.br.marketing.service.strategy.callrecording.impl;

import com.br.marketing.entity.CallRecordLLMResultV2;
import com.br.marketing.service.strategy.callrecording.CallRecordingInsertStrategy;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认CallRecording插入策略实现
 * 
 * @author kongbx
 * @date 2025/11/26
 */
@Slf4j
@Component
public class DefaultCallRecordingInsertStrategy implements CallRecordingInsertStrategy {
    /**
     * 获取策略支持的apiCode
     *
     * @return apiCode字符串，如果支持所有则返回null
     */
    @Override
    public List<String> getApiCodes() {
        return Collections.emptyList();
    }

    /**
     * 是否需要处理
     *
     * @param callRecordLLMResultV2 callRecordLLMResultV2
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2025/11/26
     */
    @Override
    public Boolean isProcessingRequired(CallRecordLLMResultV2 callRecordLLMResultV2) {
        return null;
    }

    /**
     * 通话明细数据处理
     *
     * @param callRecordLLMResultV2 callRecordLLMResultV2
     * @author senyang.zheng
     * @date 2025/11/26
     */
    @Override
    public void process(CallRecordLLMResultV2 callRecordLLMResultV2) {

    }

}

