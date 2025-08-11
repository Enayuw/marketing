package com.br.marketing.datarelayservice.service.impl;

import com.br.marketing.datarelayservice.processor.AbstractTcCustomizeProcessor;
import com.br.marketing.datarelayservice.service.TcCpaCustomizeService;
import com.br.marketing.datarelayservice.service.TcCustomizeService;
import com.br.marketing.dto.tc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: 同程易融实现
 * @author hedongshuo
 * @date 2025/4/15 15:04
 **/
@Service
@Slf4j
public class TcCpaCustomizeServiceImpl implements TcCpaCustomizeService {

    @Resource
    private AbstractTcCustomizeProcessor tcCpaDataPushProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcCpaTransformNotifyProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcCpaRevokeProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcCpaSampleDataPushProcessor;

    /**
     * @param tcRequestDTO
     * @param apiCode
     * @return com.br.marketing.dto.tc.TcResponseCommonDTO
     * @description 数据推送
     * @author hedongshuo
     * @date 2025/4/15 15:24
     **/
    @Override
    public TcResponseDTO marketDataPush(TcRequestDTO tcRequestDTO, String apiCode) {
        return tcCpaDataPushProcessor.process(tcRequestDTO, apiCode, TcDataPushDto.class);
    }

    /**
     * @param tcRequestDTO
     * @param apiCode
     * @return com.br.marketing.dto.tc.TcResponseDTO
     * @description 撤销营销
     * @author hedongshuo
     * @date 2025/4/16 10:20
     **/
    @Override
    public TcResponseDTO marketRevoke(TcRequestDTO tcRequestDTO, String apiCode) {
        return tcCpaRevokeProcessor.process(tcRequestDTO, apiCode, TcRevokeDto.class);
    }

    /**
     * @param tcRequestDTO
     * @param apiCode
     * @return com.br.marketing.dto.tc.TcResponseDTO
     * @description 转化通知
     * @author hedongshuo
     * @date 2025/4/16 11:30
     **/
    @Override
    public TcResponseDTO transformNotify(TcRequestDTO tcRequestDTO, String apiCode) {
        return tcCpaTransformNotifyProcessor.process(tcRequestDTO, apiCode, TcTransformNotifyDto.class);
    }

    /**
     * @param tcRequestDTO
     * @param apiCode
     * @return com.br.marketing.dto.tc.TcResponseCommonDTO
     * @description 正负样本推送
     * @author hong.chen
     * @date 2025/5/23 16:27
     **/
    @Override
    public TcResponseDTO sampleDataPush(TcRequestDTO tcRequestDTO, String apiCode) {
        return tcCpaSampleDataPushProcessor.process(tcRequestDTO, apiCode, TcSampleDataPushDto.class);
    }
}
