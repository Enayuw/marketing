package com.br.marketing.datarelayservice.service;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.smy.request.SmyTransferRequestDTO;
import com.br.marketing.dto.smy.response.SmyResponseDTO;
import com.br.marketing.entity.CustomizeTransferDataSmy;
import com.br.marketing.mapper.CustomizeTransferDataSmyMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import java.time.LocalDate;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmyTransferDataService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private CustomizeTransferDataSmyMapper customizeTransferDataSmyMapper;
    @Resource
    private TableCreateServiceImpl tableCreateService;

    public SmyResponseDTO receiveSmyTransferData(SmyTransferRequestDTO dto, HttpServletRequest request) {
        SmyResponseDTO smyResponseDTO = new SmyResponseDTO();
        smyResponseDTO.success();
        CustomizeTransferDataSmy customizeTransferDataSmy = new CustomizeTransferDataSmy();
        JSONObject smyCustomizeDataConfig = marketingCommonConfig.getSmyCustomizeDataConfig();
        String testApiCode = request.getHeader("Test-ApiCode");
        String apiCode = testApiCode != null ? testApiCode : smyCustomizeDataConfig.getString("uploadApiCode");
        String tCid = tableCreateService.getTcId(apiCode);
        customizeTransferDataSmy.setTCid(tCid);
        if (StringUtils.isEmpty(tCid)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_TRANSFER_SERVICEERROR.getCode(),
                    "萨摩耶创建客户定制转化前置表，未查询到该apiCode:" + apiCode + "对应客户信息，请关注！！！"));
        } else {
            // 创建定制上传表
            customizeTransferDataSmyMapper.createCustomizeTransferDataTable(tCid);
        }

        customizeTransferDataSmy.setApiCode(smyCustomizeDataConfig == null ? null : smyCustomizeDataConfig.getString("transferApiCode"));
        customizeTransferDataSmy.setRequestId(UUID.fastUUID().toString(true));
        customizeTransferDataSmy.setReceiveDate(LocalDate.now().toString());
        customizeTransferDataSmy.setRequestJsonData(JSONObject.toJSONString(dto));
        customizeTransferDataSmy.setStatus(1);
        // Check Field
        StringBuilder errorMessage = new StringBuilder();
        if (StringUtils.isBlank(dto.getEventType())) {
            errorMessage.append(", event_type 不可为空");
        }
        if (dto.getEventTime() == null) {
            errorMessage.append(", event_time 不可为空");
        }
        if (StringUtils.isBlank(dto.getCid())) {
            errorMessage.append(", cid 不可为空");
        }
        if (StringUtils.isNotBlank(dto.getExtendFields()) && !JSONObject.isValid(dto.getExtendFields())) {
            errorMessage.append(", extend_fields 非Json格式");
        }
        if (errorMessage.length() > 0) {
            customizeTransferDataSmy.setStatus(0);
            smyResponseDTO = smyResponseDTO.failed(SmyResponseDTO.ResultEnum.FAILED_PARAM_ERROR, errorMessage.toString());
        }
        customizeTransferDataSmy.setBizDataNumber(1);
        customizeTransferDataSmy.setResponseCode(String.valueOf(smyResponseDTO.getCode()));
        customizeTransferDataSmy.setResponseData(smyResponseDTO.getMessage());
        int i = customizeTransferDataSmyMapper.insertSelective(customizeTransferDataSmy);
        if (i != 1) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_TRANSFER_SERVICEERROR.getCode(),
                    "dtoJson:" + JSONObject.toJSONString(dto), "萨摩耶定制转化数据入库失败！！！"));
        }
        return smyResponseDTO;
    }
}
