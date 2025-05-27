package com.br.marketing.check.service.Impl.clean.transfer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.service.IFileToMarketingRuleTransferService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ZhaoLianFileToMarketingRuleTransferServiceImpl
 * @Description 招联转化数据清洗
 * @Author kongbx
 * @Date 2025/5/26 20:04
 */
@Service
public class ZhaoLianFileToMarketingRuleTransferServiceImpl implements IFileToMarketingRuleTransferService {

    @Override
    public Result isVaild(List<FileToMarketingDataFieldVO> vos, Map<String, FileToMarketingDataFieldVO> voMaps) {
        return IFileToMarketingRuleTransferService.super.isVaild(vos, voMaps);
    }

    @Override
    public TransferDataItemDTO make(List<FileToMarketingDataFieldVO> vos) {
        TransferDataItemDTO dto = new TransferDataItemDTO();
        JSONObject reserveFieldJo = new JSONObject();
        for (FileToMarketingDataFieldVO vo : vos) {
            switch (vo.getInterfaceField()){
                case "custNum":
                    dto.setCustNum(vo.getDataValue());
                    break;
                case"ifLogin":
                    dto.setIfLogin(vo.getDataValue());
                    break;
                case"ifLent":
                    if(StringUtils.isNotBlank(vo.getDataValue())){
                        dto.setIfLent(vo.getDataValue());
                    }
                    break;
                case"caseEffective":
                    reserveFieldJo.put("caseEffective",vo.getDataValue());
                    break;
                case"lmt_sts":
                    reserveFieldJo.put("lmt_sts",vo.getDataValue());
                    break;
                case"crd_typ":
                    reserveFieldJo.put("crd_typ",vo.getDataValue());
                    break;
                case"qy_typ":
                    if(vo.getDataValue().contains("-")){
                        reserveFieldJo.put("activityTime",vo.getDataValue());
                    }else {
                        reserveFieldJo.put("qy_typ",vo.getDataValue());
                    }
                    break;
                case"qy_rat":
                    reserveFieldJo.put("qy_rat",vo.getDataValue());
                    break;
                case"applyLoan":
                    reserveFieldJo.put("applyLoan",vo.getDataValue());
                    break;
                case"cmpn_value_typ":
                    reserveFieldJo.put("cmpn_value_typ",vo.getDataValue());
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

    @Override
    public String getTaskId(String apiCode, String filename) {
        // 文件示例：transform_20250526_E1Z13250526004BR_01.txt
        String[] parts = filename.split("_");
        String taskId = "";
        if (parts.length >= 3) {
            taskId = parts[2];
        }
        return taskId;
    }

}
