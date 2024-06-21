package com.br.marketing.check.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.service.IFileToMarketingRuleService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class XieChengFileToMarketingRuleServiceImpl implements IFileToMarketingRuleService {

    @Override
    public Result isVaild(List<FileToMarketingDataFieldVO> vos, Map<String,FileToMarketingDataFieldVO> voMaps) {
        FileToMarketingDataFieldVO keyType = voMaps.get("keyType");
        if(keyType!=null&&"1".equals(keyType.getDataValue())){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("keyType 为1");
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public MarketingPreUserDetailDTO make(List<FileToMarketingDataFieldVO> vos) {
        return IFileToMarketingRuleService.super.make(vos);
    }
}
