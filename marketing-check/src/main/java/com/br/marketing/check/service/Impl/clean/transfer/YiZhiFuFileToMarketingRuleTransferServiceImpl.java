package com.br.marketing.check.service.Impl.clean.transfer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.SimpleResult;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.service.IFileToMarketingRuleTransferService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 翼支付无表头文件转化清洗策略
 * 规则字段：custNum, ifLogin, ifApply, applyResult, insertTime；conversion 已在前置按列解析时处理（Y→1, N→0）
 *
 * @author kongbx
 */
@Slf4j
@Service
public class YiZhiFuFileToMarketingRuleTransferServiceImpl implements IFileToMarketingRuleTransferService {

    @Override
    public SimpleResult<?> isVaild(List<FileToMarketingDataFieldVO> vos, Map<String, FileToMarketingDataFieldVO> voMaps) {
        return IFileToMarketingRuleTransferService.super.isVaild(vos, voMaps);
    }

    @Override
    public TransferDataItemDTO make(List<FileToMarketingDataFieldVO> vos) {
        TransferDataItemDTO dto = new TransferDataItemDTO();
        JSONObject reserveFieldJo = new JSONObject();

        if (vos == null) {
            return dto;
        }

        for (FileToMarketingDataFieldVO vo : vos) {
            if (vo == null || StringUtils.isBlank(vo.getInterfaceField())) {
                continue;
            }
            String field = vo.getInterfaceField();
            String value = vo.getDataValue() == null ? "" : vo.getDataValue();

            switch (field) {
                case "custNum":
                    dto.setCustNum(value);
                    break;
                case "ifLogin":
                    dto.setIfLogin(value);
                    break;
                case "ifApply":
                    dto.setIfApply(value);
                    break;
                case "applyResult":
                    dto.setApplyResult(value);
                    break;
                case "insertTime":
                    dto.setInsertTime(value);
                    break;
                case "fileName":
                    reserveFieldJo.put("fileName", value);
                    break;
                default:
                    if (vo.getIsExtend() != null && vo.getIsExtend()) {
                        String extendField = StringUtils.isBlank(field) ? vo.getHeadField() : field;
                        reserveFieldJo.put(extendField, value);
                    }
                    break;
            }
            dto.setUserType("1");
        }

        if (!reserveFieldJo.isEmpty()) {
            dto.setReserveField1(JSON.toJSONString(reserveFieldJo));
        }

        return dto;
    }

    @Override
    public String getTaskId(String apiCode, String fileNm) {
        return IFileToMarketingRuleTransferService.super.getTaskId(apiCode, fileNm);
    }
}
