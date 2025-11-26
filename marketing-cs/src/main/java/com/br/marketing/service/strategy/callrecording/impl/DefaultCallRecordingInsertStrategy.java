package com.br.marketing.service.strategy.callrecording.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.service.strategy.callrecording.CallRecordingInsertStrategy;
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

    @Override
    public void buildCallRecording(JSONObject jsonObject) {

    }

    @Override
    public String getApiCode() {
        // 默认策略支持所有apiCode
        return null;
    }

}

