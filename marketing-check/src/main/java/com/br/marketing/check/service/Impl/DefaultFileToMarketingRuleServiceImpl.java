package com.br.marketing.check.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.service.IFileToMarketingRuleService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DefaultFileToMarketingRuleServiceImpl implements IFileToMarketingRuleService {

    @Override
    public Result isVaild(List<FileToMarketingDataFieldVO> vos, Map<String,FileToMarketingDataFieldVO> voMaps) {
        return IFileToMarketingRuleService.super.isVaild(vos,voMaps);
    }

    @Override
    public MarketingPreUserDetailDTO make(List<FileToMarketingDataFieldVO> vos) {
        return IFileToMarketingRuleService.super.make(vos);
    }
}
