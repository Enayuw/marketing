package com.br.marketing.check.service.Impl.clean.upload;

import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.commondto.SimpleResult;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.service.IFileToMarketingRuleService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class XieChengFileToMarketingRuleServiceImpl implements IFileToMarketingRuleService {

    @Override
    public SimpleResult isVaild(List<FileToMarketingDataFieldVO> vos, Map<String,FileToMarketingDataFieldVO> voMaps) {
        FileToMarketingDataFieldVO keyType = voMaps.get("keyType");
        if(keyType!=null&&"1".equals(keyType.getDataValue())){
            return new SimpleResult().setCode(ResultCode.FAIL.getValue()).setMessage("keyType 为1");
        }
        return new SimpleResult().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public MarketingPreUserDetailDTO make(List<FileToMarketingDataFieldVO> vos) {
        return IFileToMarketingRuleService.super.make(vos);
    }
}
