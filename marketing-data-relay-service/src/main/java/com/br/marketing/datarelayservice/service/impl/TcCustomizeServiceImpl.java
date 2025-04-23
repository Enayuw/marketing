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
     * @description 数据推送
     * @param tcRequestDTO
     * @return com.br.marketing.dto.tc.TcResponseCommonDTO
     * @author hedongshuo
     * @date 2025/4/15 15:24
     **/
    @Override
    public TcResponseDTO marketDataPush(TcRequestDTO tcRequestDTO) {
        return tcDataPushProcessor.process(tcRequestDTO, TcDataPushDto.class);
    }

    /**
     * @description 撤销营销
     * @param tcRequestDTO
     * @return com.br.marketing.dto.tc.TcResponseDTO
     * @author hedongshuo
     * @date 2025/4/16 10:20
     **/
    @Override
    public TcResponseDTO marketRevoke(TcRequestDTO tcRequestDTO) {
        return tcRevokeProcessor.process(tcRequestDTO, TcRevokeDto.class);
    }

    /**
     * @description 转化通知
     * @param tcRequestDTO
     * @return com.br.marketing.dto.tc.TcResponseDTO
     * @author hedongshuo
     * @date 2025/4/16 11:30
     **/
    @Override
    public TcResponseDTO transformNotify(TcRequestDTO tcRequestDTO) {
        return tcTransformNotifyProcessor.process(tcRequestDTO, TcTransformNotifyDto.class);
    }
}
