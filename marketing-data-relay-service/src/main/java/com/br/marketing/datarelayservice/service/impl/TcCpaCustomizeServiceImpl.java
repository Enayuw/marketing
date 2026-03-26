package com.br.marketing.datarelayservice.service.impl;

import com.br.marketing.datarelayservice.context.TcMarketDataPushContext;
import com.br.marketing.datarelayservice.processor.AbstractTcCustomizeProcessor;
import com.br.marketing.datarelayservice.service.TcCpaCustomizeService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.dto.tc.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 同程易融实现
 * @author hedongshuo
 * @date 2025/4/15 15:04
 **/
@Service
@Slf4j
public class TcCpaCustomizeServiceImpl implements TcCpaCustomizeService {

    private static final String BIZ_CODE_CPA_DATA_PUSH = "-cpa-marketDataPush";

    private static final String BIZ_CODE_CPA_FAIL_DATA_PUSH = "-cpa-marketFailDataPush";

    private static final String BIZ_CODE_CPA_TRANSFER = "-cpa-transformNotify";

    private static final String BIZ_CODE_CPA_REVOKE = "-cpa-revoke";

    private static final String BIZ_CODE_CPA_SAMPLE_DATA_PUSH = "-cpa-sampleDataPush";

    @Resource
    private AbstractTcCustomizeProcessor tcCpaDataPushProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcDataPushProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcCpaTransformNotifyProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcCpaRevokeProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcCpaSampleDataPushProcessor;

    @Resource
    private AbstractTcCustomizeProcessor tcCpaFailDataPushProcessor;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * @param tcRequestDTO
     * @param apiCode
     * @return com.br.marketing.dto.tc.TcResponseCommonDTO
     * @description 数据推送
     * @author hedongshuo
     * @date 2025/8/11 15:24
     **/
    @Override
    public TcResponseDTO marketDataPush(TcRequestDTO tcRequestDTO, String apiCode) {
        String batchNo = parseBatchNo(tcRequestDTO);
        if (!matchAnyPrefix(batchNo, marketingCommonConfig.getTcCpaBatchNoPrefixConfig())) {
            TcMarketDataPushContext.set(TcMarketDataPushContext.Entry.CPA_SYNC_FALLBACK);
            try {
                return tcDataPushProcessor.process(tcRequestDTO, marketingCommonConfig.getTcyrApiCode(),
                        TcDataPushDto.class, BIZ_CODE_CPA_DATA_PUSH);
            } finally {
                TcMarketDataPushContext.clear();
            }
        }
        return tcCpaDataPushProcessor.process(tcRequestDTO, apiCode, TcDataPushDto.class, BIZ_CODE_CPA_DATA_PUSH);
    }

    /**
     * @param tcRequestDTO
     * @param apiCode
     * @return com.br.marketing.dto.tc.TcResponseDTO
     * @description 撤销营销
     * @author hedongshuo
     * @date 2025/8/11 10:20
     **/
    @Override
    public TcResponseDTO marketRevoke(TcRequestDTO tcRequestDTO, String apiCode) {
        return tcCpaRevokeProcessor.process(tcRequestDTO, apiCode, TcRevokeDto.class, BIZ_CODE_CPA_REVOKE);
    }

    /**
     * @param tcRequestDTO
     * @param apiCode
     * @return com.br.marketing.dto.tc.TcResponseDTO
     * @description 转化通知
     * @author hedongshuo
     * @date 2025/8/11 11:30
     **/
    @Override
    public TcResponseDTO transformNotify(TcRequestDTO tcRequestDTO, String apiCode) {
        return tcCpaTransformNotifyProcessor.process(tcRequestDTO, apiCode, TcTransformNotifyDto.class, BIZ_CODE_CPA_TRANSFER);
    }

    /**
     * @param tcRequestDTO
     * @param apiCode
     * @return com.br.marketing.dto.tc.TcResponseCommonDTO
     * @description 正负样本推送
     * @author hong.chen
     * @date 2025/8/11 16:27
     **/
    @Override
    public TcResponseDTO sampleDataPush(TcRequestDTO tcRequestDTO, String apiCode) {
        return tcCpaSampleDataPushProcessor.process(tcRequestDTO, apiCode, TcSampleDataPushDto.class, BIZ_CODE_CPA_SAMPLE_DATA_PUSH);
    }

    /**
     * @param tcRequestDTO
     * @param apiCode
     * @return com.br.marketing.dto.tc.TcResponseCommonDTO
     * @description 失败数据推送
     * @author hong.chen
     * @date 2025/8/11 16:27
     **/
    @Override
    public TcResponseDTO marketFailDataPush(TcRequestDTO tcRequestDTO, String apiCode) {
        return tcCpaFailDataPushProcessor.process(tcRequestDTO, apiCode, TcFailDataPushDto.class, BIZ_CODE_CPA_FAIL_DATA_PUSH);
    }

    private String parseBatchNo(TcRequestDTO tcRequestDTO) {
        if (tcRequestDTO == null || StringUtils.isBlank(tcRequestDTO.getData())) {
            return null;
        }
        try {
            JSONObject data = JSONObject.parseObject(tcRequestDTO.getData());
            return data == null ? null : data.getString("batchNo");
        } catch (Exception e) {
            return null;
        }
    }

    private boolean matchAnyPrefix(String batchNo, List<String> prefixList) {
        if (StringUtils.isBlank(batchNo) || prefixList == null || prefixList.isEmpty()) {
            return false;
        }
        for (String prefix : prefixList) {
            if (matchPrefix(batchNo, prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchPrefix(String batchNo, String prefix) {
        if (StringUtils.isBlank(batchNo) || StringUtils.isBlank(prefix)) {
            return false;
        }
        return StringUtils.startsWith(batchNo, prefix);
    }
}
