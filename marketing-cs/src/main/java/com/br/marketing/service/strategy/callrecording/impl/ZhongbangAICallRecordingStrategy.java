package com.br.marketing.service.strategy.callrecording.impl;

import com.br.marketing.entity.CallRecordLLMResultV2;
import com.br.marketing.entity.CallRecording;
import com.br.marketing.mapper.CallRecordingMapper;
import com.br.marketing.service.strategy.callrecording.CallRecordingInsertStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ZhongbangAICallRecordingStrategy implements CallRecordingInsertStrategy {

    @Autowired
    private CallRecordingMapper callRecordingMapper;

    @Override
    public Boolean isProcessingRequired(CallRecordLLMResultV2 callRecordLLMResultV2) {
        return Boolean.TRUE;
    }

    @Override
    public void process(CallRecordLLMResultV2 callRecordLLMResultV2) {

        CallRecording callRecording = new CallRecording();
        BeanUtils.copyProperties(callRecordLLMResultV2, callRecording);
        callRecording.setStatus(0);
        callRecordingMapper.insertSelective(callRecording);

    }
}
