package com.br.marketing.datarelayservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.smy.request.SmyUploadRequestDTO;
import com.br.marketing.dto.smy.response.SmyResponseDTO;
import com.br.marketing.entity.CustomizeUploadDataSmy;
import com.br.marketing.mapper.CustomizeUploadDataSmyMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import java.time.LocalDate;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * smy upload data service
 *
 * @author Sion.Cheng
 * @date 2024/12/18
 */
@Service
@Slf4j
public class SmyUploadDataService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private CustomizeUploadDataSmyMapper customizeUploadDataSmyMapper;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private TrackingService trackingService;

    /**
     * receive smy upload data
     *
     * @param dto smy upload request dto
     * @return {@link SmyResponseDTO }
     * @author Sion Cheng
     * @date 2024/12/18
     */
    public SmyResponseDTO receiveSmyUploadData(String jsonData, HttpServletRequest request) {
        SmyResponseDTO smyResponseDTO = new SmyResponseDTO();
        smyResponseDTO.success();
        try {
            SmyUploadRequestDTO dto = JSONObject.parseObject(jsonData, SmyUploadRequestDTO.class);
            CustomizeUploadDataSmy customizeUploadDataSmy = new CustomizeUploadDataSmy();
            JSONObject smyCustomizeDataConfig = marketingCommonConfig.getSmyCustomizeDataConfig();
            String testApiCode = request.getHeader("Test-ApiCode");
            String apiCode = testApiCode != null ? testApiCode : smyCustomizeDataConfig.getString("uploadApiCode");
            String tCid = tableCreateService.getTcId(apiCode);
            if (StringUtils.isEmpty(tCid)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_UPLOAD_SERVICEERROR.getCode(),
                        "萨摩耶创建客户定制上传前置表，未查询到该apiCode:" + apiCode + "对应客户信息，请关注！！！"));
            } else {
                // 创建定制上传表
                customizeUploadDataSmyMapper.createCustomizeUploadDataTable(tCid);
            }
            customizeUploadDataSmy.setApiCode(apiCode);
            customizeUploadDataSmy.setTCid(tCid);
            customizeUploadDataSmy.setRequestId(dto.getRequestNo());
            customizeUploadDataSmy.setReceiveDate(LocalDate.now().toString());
            customizeUploadDataSmy.setRequestJsonData(jsonData);
            customizeUploadDataSmy.setStatus(1);
            //Check Field
            StringBuilder errorMessage = new StringBuilder();
            if (StringUtils.isBlank(dto.getRequestNo())) {
                errorMessage.append(", request_no 不可为空");
            } else if (StringUtils.isBlank(dto.getCaseType())) {
                errorMessage.append(", case_type 不可为空");
            } else if (dto.getTotal() == null) {
                errorMessage.append(", total 不可为空");
            } else if (dto.getNameList() == null || dto.getNameList().isEmpty()) {
                errorMessage.append(", name_list 不可为空");
            }
            if (errorMessage.length() > 0) {
                customizeUploadDataSmy.setStatus(0);
                smyResponseDTO = smyResponseDTO.failed(SmyResponseDTO.ResultEnum.FAILED_PARAM_ERROR, errorMessage.toString());
            }
            customizeUploadDataSmy.setBizDataNumber(dto.getNameList().size());
            customizeUploadDataSmy.setResponseCode(String.valueOf(smyResponseDTO.getCode()));
            customizeUploadDataSmy.setResponseData(smyResponseDTO.getMessage());
            int i = customizeUploadDataSmyMapper.insertSelective(customizeUploadDataSmy);
            if (i != 1) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_UPLOAD_SERVICEERROR.getCode(), "jsonData:" + jsonData,
                        "萨摩耶定制上传数据入库失败！！！"));
            }

            // 埋点
            try {
                JSONObject condition = new JSONObject();
                condition.put("request_no", dto.getRequestNo());
                trackingService.trackBusinessLog(DataFlowDirection.IN
                        , apiCode
                        , "萨摩耶定制上传接口"
                        ,"b_customize_upload_data_"+tCid
                        , JSON.toJSONString(condition)
                        , 1L
                        , TrackingContext.generateBatchId());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_UPLOAD_SERVICEERROR.getCode(), "jsonData:" + jsonData,
                    "萨摩耶定制上传数据接入异常！！！"), e);
            smyResponseDTO = smyResponseDTO.failed(SmyResponseDTO.ResultEnum.FAILED_SYSTEM_ERROR);
        }
        log.warn("萨摩耶代运营数据上传接口被调用");
        return smyResponseDTO;
    }
}
