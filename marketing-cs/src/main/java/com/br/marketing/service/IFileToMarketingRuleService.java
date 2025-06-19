package com.br.marketing.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.commondto.SimpleResult;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingDataFileConfig;
import com.br.marketing.vo.FileToMarketingDataFieldVO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件写入营销数据 规则服务
 */
public interface IFileToMarketingRuleService {

    /**
     * 是否剔除 true有效；false无效
     * @return
     */
    default SimpleResult isVaild(List<FileToMarketingDataFieldVO> vos, Map<String,FileToMarketingDataFieldVO> voMaps){
        return new SimpleResult().setCode(ResultCode.SUCCESS.getValue());
    }
    default SimpleResult isVaildByList(List<FileToMarketingDataFieldVO> vos, Map<String,List<FileToMarketingDataFieldVO>> voMaps){
        return new SimpleResult().setCode(ResultCode.SUCCESS.getValue());
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
                case "custNum":
                    dto.setCustNum(vo.getDataValue());
                    break;
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
                case"operateType":
                    reserveFieldJo.put("operateType",vo.getDataValue());
                    break;
                case"fileName":
                    reserveFieldJo.put("fileName",vo.getDataValue());
                    break;
                default:
                    break;
            }
            if(vo.getIsExtend()!=null && vo.getIsExtend()){
                reserveFieldJo.put(StringUtils.isBlank(vo.getInterfaceField())?vo.getHeadField():vo.getInterfaceField(),vo.getDataValue());
            }
        }
        if (reserveFieldJo.keySet().size()>0) {
            dto.setReserveField1(JSON.toJSONString(reserveFieldJo));
        }
        return dto;
    }

    /**
     * 获取taskId
     * @return
     */
    default String getTaskId(String apiCode,String fileNm){
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return apiCode.concat("_").concat(yyyyMMdd);
    }

    default Boolean isChecklistName(MarketingDataFileConfig fileConfig, String fileNm){
        // 校验表名称
        if(fileConfig.getIsChecklistName() != null && fileConfig.getIsChecklistName() == 0){
            String regex = fileConfig.getValidationRules();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(fileNm);
            if (!matcher.matches()) {
                return false;
            }
        }
        return true;
    }

}
