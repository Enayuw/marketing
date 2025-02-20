package com.br.marketing.service.Impl;

import com.br.marketing.dto.ApiRecordLogDTO;
import com.br.marketing.enums.ApiNmEnum;
import com.br.marketing.service.IInterfaceLogService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IInterfaceLogServiceImpl implements IInterfaceLogService {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public ApiRecordLogDTO isRecord(ApiNmEnum apiNm) {
        HashMap<String, List<Boolean>> apiLogMark = marketingCommonConfig.getApiLogMark();
        List<Boolean> booleans = apiLogMark.get(apiNm.getApiNm());
        if (CollectionUtils.isEmpty(booleans)) {
            return new ApiRecordLogDTO(Boolean.FALSE, Boolean.TRUE);
        }
        return new ApiRecordLogDTO(booleans.get(0), booleans.get(1));
    }
}
