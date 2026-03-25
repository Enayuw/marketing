package com.br.marketing.datarelayservice.service.impl;

import com.br.marketing.datarelayservice.context.TcMarketDataPushContext;
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

    private static final String BIZ_CODE_DATA_PUSH = "-marketDataPush";

    private static final String BIZ_CODE_TRANSFER = "-transformNotify";

    private static final String BIZ_CODE_REVOKE = "-revoke";

    private static final String BIZ_CODE_SAMPLE_DATA_PUSH = "-sampleDataPush";

    @Resource
    private AbstractTcCustomizeProcessor tcDataPushProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcTransformNotifyProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcRevokeProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcSampleDataPushProcessor;

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
        TcMarketDataPushContext.set(TcMarketDataPushContext.Entry.STANDARD_SYNC);
        try {
            return tcDataPushProcessor.process(tcRequestDTO, apiCode, TcDataPushDto.class, BIZ_CODE_DATA_PUSH);
        } finally {
            TcMarketDataPushContext.clear();
        }
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
        return tcRevokeProcessor.process(tcRequestDTO, apiCode, TcRevokeDto.class, BIZ_CODE_REVOKE);
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
        return tcTransformNotifyProcessor.process(tcRequestDTO, apiCode, TcTransformNotifyDto.class, BIZ_CODE_TRANSFER);
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
        return tcSampleDataPushProcessor.process(tcRequestDTO, apiCode, TcSampleDataPushDto.class, BIZ_CODE_SAMPLE_DATA_PUSH);
    }
}
