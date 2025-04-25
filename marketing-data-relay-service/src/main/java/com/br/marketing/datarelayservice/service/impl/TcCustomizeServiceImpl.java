package com.br.marketing.datarelayservice.service.impl;

import com.br.marketing.datarelayservice.processor.AbstractTcCustomizeProcessor;
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
public class TcCustomizeServiceImpl implements TcCustomizeService {

    @Resource
    private AbstractTcCustomizeProcessor tcDataPushProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcTransformNotifyProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcRevokeProcessor;

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
        return tcDataPushProcessor.process(tcRequestDTO, apiCode, TcDataPushDto.class);
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
        return tcRevokeProcessor.process(tcRequestDTO, apiCode, TcRevokeDto.class);
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
        return tcTransformNotifyProcessor.process(tcRequestDTO, apiCode, TcTransformNotifyDto.class);
    }
}
