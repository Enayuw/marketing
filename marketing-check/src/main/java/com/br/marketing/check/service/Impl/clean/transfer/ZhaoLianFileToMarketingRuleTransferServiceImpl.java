package com.br.marketing.check.service.Impl.clean.transfer;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.SimpleResult;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.service.IFileToMarketingRuleTransferService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    public SimpleResult isVaild(List<FileToMarketingDataFieldVO> vos, Map<String, FileToMarketingDataFieldVO> voMaps) {
        return IFileToMarketingRuleTransferService.super.isVaild(vos, voMaps);
    }


    @Override
    public TransferDataItemDTO make(List<FileToMarketingDataFieldVO> vos) {
        TransferDataItemDTO dto = new TransferDataItemDTO();
        JSONObject reserveFieldJo = new JSONObject();

        if (CollectionUtils.isEmpty(vos)) {
            return dto;
        }

        for (FileToMarketingDataFieldVO vo : vos) {
            if (vo == null || StringUtils.isBlank(vo.getInterfaceField())) {
                continue;
            }

            String field = vo.getInterfaceField();
            String value = vo.getDataValue();

            switch (field) {
                case "custNum":
                    dto.setCustNum(value);
                    break;
                case "ifLogin":
                    if("1".equals(value)){
                        String formattedDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        dto.setLoginTime(formattedDate);
                    }
                    dto.setIfLogin(value);
                    break;
                case "ifLent":
                    if("1".equals(value)){
                        String formattedDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        dto.setLentTime(formattedDate);
                    }
                    dto.setIfLent(value);
                    break;
                case "caseEffective":
                    if(vo.getHeadField().equals("cmpn_ctrl_typ")){
                        reserveFieldJo.put("caseEffective", value);
                    }
                    break;
                case "lmt_sts":
                    reserveFieldJo.put("lmt_sts", value);
                    break;
                case "crd_typ":
                    reserveFieldJo.put("crd_typ", value);
                    break;
                case "qy_typ":
                    handleQyTyp(reserveFieldJo, value);
                    break;
                case "qy_rat":
                    reserveFieldJo.put("qy_rat", value);
                    break;
                case "applyLoan":
                    if("1".equals(value)){
                        String formattedDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        reserveFieldJo.put("applyLoanTime", formattedDate);
                    }
                    reserveFieldJo.put("applyLoan", value);
                    break;
                case "cmpn_value_typ":
                    reserveFieldJo.put("cmpn_value_typ", value);
                    break;
                default:
                    // 处理扩展字段
                    if (vo.getIsExtend() != null && vo.getIsExtend()) {
                        String extendField = StringUtils.isBlank(field) ? vo.getHeadField() : field;
                        reserveFieldJo.put(extendField, value);
                    }
                    break;
            }
        }

        // 设置 reserveField1
        if (!reserveFieldJo.isEmpty()) {
            dto.setReserveField1(JSON.toJSONString(reserveFieldJo));
        }

        return dto;
    }

    /**
     * 处理 qy_typ 字段逻辑：
     * 1. 如果格式为 "B-2025-05-21"，拆分存储到 qy_typ 和 activityTime。
     * 2. 如果值为 A/B/C，映射为 1/2/3 并存储到 qy_typ。
     */
    private void handleQyTyp(JSONObject reserveFieldJo, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }

        // 情况1：处理 "B-2025-05-21" 格式
        if (value.contains("-")) {
            String[] parts = value.split("-", 2); // 分割成两部分
            if (parts.length == 2) {
                // 存储映射后的字母（如 B→2）
                String letter = parts[0];
                String mappedValue = mapLetterToNumber(letter);
                reserveFieldJo.put("qy_typ", mappedValue);

                // 存储日期部分
                reserveFieldJo.put("activityTime", parts[1]);
            }
        }
        // 情况2：处理单独字母（A/B/C）
        else {
            String mappedValue = mapLetterToNumber(value);
            reserveFieldJo.put("qy_typ", mappedValue);
        }
    }

    /**
     * 映射规则：A→1, B→2, C→3
     */
    private String mapLetterToNumber(String letter) {
        switch (letter) {
            case "A": return "1";
            case "B": return "2";
            case "C": return "3";
            default: return letter;
        }
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
