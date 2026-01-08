package com.br.marketing.service.tccpa.impl;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.mapper.MarketingTcyrCustCellMappingMapper;
import com.br.marketing.service.tccpa.TcCpaCustCellMappingService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TcCpaCustCellMappingServiceImpl implements TcCpaCustCellMappingService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrCustCellMappingMapper tcyrCustCellMappingMapper;



    @Override
    public String selectCell(String userKey) {
        if (StringUtils.isEmpty(userKey) || "0".equals(userKey)) {
            return null;
        }
        String apiCode =  marketingCommonConfig.getTcyrApiCode();
        Long syncId = Long.parseLong(userKey);
        return tcyrCustCellMappingMapper.selectCellBySyncId(apiCode,syncId);
    }

    @Override
    public List<Map<String, Object>> selectCellInfo(List<String> userKeyList) {
        String apiCode =  marketingCommonConfig.getTcyrApiCode();
        List<Long> idList = userKeyList.stream().filter(StringUtils::isNotBlank) .map(Long::valueOf).toList();
        return  tcyrCustCellMappingMapper.selectCellInfotikv_(apiCode,idList);
    }


}
