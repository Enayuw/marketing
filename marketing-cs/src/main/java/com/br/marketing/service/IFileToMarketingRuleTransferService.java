package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.commondto.SimpleResult;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingDataFileConfig;
import com.br.marketing.vo.FileToMarketingDataFieldVO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName IFileToMarketingRuleTransferService
 * @Description TODO
 * @Author kongbx
 * @Date 2025/5/26 18:18
 */
public interface IFileToMarketingRuleTransferService {

    /**
     * 是否剔除 true有效；false无效
     * @return
     */
    default SimpleResult isVaild(List<FileToMarketingDataFieldVO> vos, Map<String, FileToMarketingDataFieldVO> voMaps){
        return new SimpleResult().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 生成营销数据对象
     * @return
     */
    default TransferDataItemDTO make(List<FileToMarketingDataFieldVO> vos){
        return new TransferDataItemDTO();
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
            Matcher matcherWithoutSuccess = pattern.matcher(fileNm);
            if (!matcherWithoutSuccess.matches()) {
                return false;
            }
        }
        return true;
    }

}
