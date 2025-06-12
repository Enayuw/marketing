package com.br.marketing.check.service.Impl.clean.upload;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.SimpleResult;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.service.IFileToMarketingRuleService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ZhaoLianFileToMarketingRuleServiceImpl
 * @Description 招联转化数据清洗实现
 * @Author kongbx
 * @Date 2025/5/26 19:29
 */
@Service
public class ZhaoLianFileToMarketingRuleServiceImpl implements IFileToMarketingRuleService {

    @Override
    public SimpleResult isVaild(List<FileToMarketingDataFieldVO> vos, Map<String, FileToMarketingDataFieldVO> voMaps) {
        return IFileToMarketingRuleService.super.isVaild(vos, voMaps);
    }

    @Override
    public MarketingPreUserDetailDTO make(List<FileToMarketingDataFieldVO> vos) {
        MarketingPreUserDetailDTO dto = new MarketingPreUserDetailDTO();
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

            // 使用 switch 处理字段映射
            switch (field) {
                case "custNum":
                    dto.setCustNum(value);
                    break;
                case "cell":
                    dto.setCell(value);
                    break;
                case "id":
                    if (StringUtils.isNotBlank(value)) {
                        dto.setId(value);
                    }
                    break;
                case "name":
                    if (StringUtils.isNotBlank(value)) {
                        dto.setName(value);
                    }
                    break;
                case "groupType":
                    if (StringUtils.isNotBlank(value)) {
                        dto.setGroupType(value);
                    }
                    break;
                case "userType":
                    reserveFieldJo.put("userType", value);
                    break;
                case "operateType":
                    reserveFieldJo.put("operateType", value);
                    break;
                case "fileName":
                    reserveFieldJo.put("fileName", value);
                    break;
                case "qy_typ":
                    handleQyTyp(reserveFieldJo, value);
                    break;
                default:
                    // 处理扩展字段
                    if (Boolean.TRUE.equals(vo.getIsExtend())) {
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
     * 处理 qy_typ 字段逻辑
     */
    private void handleQyTyp(JSONObject reserveFieldJo, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }

        if (value.contains("-")) {
            String[] parts = value.split("-", 2);
            if (parts.length == 2) {
                reserveFieldJo.put("qy_typ", mapLetterToNumber(parts[0]));
                reserveFieldJo.put("activityTime", parts[1]);
            }
        } else {
            reserveFieldJo.put("qy_typ", mapLetterToNumber(value));
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
        // 文件示例：original_yyyymmdd_E0012250428069BR_01.txt
        String[] parts = filename.split("_");
        String taskId = "";
        if (parts.length >= 3) {
            taskId = parts[2];
        }
        return taskId;
    }

}