package com.br.marketing.check.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.vo.FileToMarketingDataFieldVO;

import java.util.List;

/**
 * 文件写入营销数据 规则服务
 */
public interface IFileToMarketingRuleService {

    /**
     * 是否剔除 true有效；false无效
     * @return
     */
    default Result isVaild(List<FileToMarketingDataFieldVO> vos){
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 生成营销数据对象
     * @return
     */
    default MarketingPreUserDetailDTO make(List<FileToMarketingDataFieldVO> vos){
        MarketingPreUserDetailDTO dto = new MarketingPreUserDetailDTO();
        JSONObject reserveFieldJo = new JSONObject();
        for (FileToMarketingDataFieldVO vo : vos) {
            switch (vo.getInterfaceField()){
                case"cell":
                    dto.setCell(vo.getDataValue());
                    break;
                case"id":
                    if(StringUtils.isNotBlank(vo.getDataValue())){
                        dto.setId(vo.getDataValue());
                    }
                    break;
                case"name":
                    if(StringUtils.isNotBlank(vo.getDataValue())){
                        dto.setName(vo.getDataValue());
                    }
                    break;
                case"groupType":
                    if(StringUtils.isNotBlank(vo.getDataValue())){
                        dto.setGroupType(vo.getDataValue());
                    }
                    break;
                case"userType":
                    reserveFieldJo.put("userType",vo.getDataValue());
                    break;
            }
            if(vo.getIsExtend()){
                reserveFieldJo.put(StringUtils.isBlank(vo.getInterfaceField())?vo.getHeadField():vo.getInterfaceField(),vo.getDataValue());
            }
        }
        if (reserveFieldJo.keySet().size()>0) {
            dto.setReserveField1(JSON.toJSONString(reserveFieldJo));
        }
        return dto;
    }
}
