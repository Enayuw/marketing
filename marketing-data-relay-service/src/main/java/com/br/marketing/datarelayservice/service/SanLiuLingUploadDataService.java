package com.br.marketing.datarelayservice.service;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.sanliuling.request.SanLiuLingUploadRequestDTO;
import com.br.marketing.dto.sanliuling.response.SanLiuLingResponseDTO;
import com.br.marketing.entity.MarketingCustomerOriginalData;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName SanLiuLingUploadDataService
 * @Author kongbx
 * @Date 2025/8/28 14:19
 */
@Service
@Slf4j
public class SanLiuLingUploadDataService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    MarketingCustomerOriginalDataMapper marketingCustomerOriginalDataMapper;

    public SanLiuLingResponseDTO receiveCollectionUploadData(String jsonData, HttpServletRequest request) {
        SanLiuLingResponseDTO sanLiuLingResponseDTO = new SanLiuLingResponseDTO();
        try {
            SanLiuLingUploadRequestDTO dto = JSONObject.parseObject(jsonData, SanLiuLingUploadRequestDTO.class);

            //todo 改成新的speed
            JSONObject smyCustomizeDataConfig = marketingCommonConfig.getSmyCustomizeDataConfig();
            String testApiCode = request.getHeader("Test-ApiCode");
            String apiCode = testApiCode != null ? testApiCode : smyCustomizeDataConfig.getString("uploadApiCode");

            MarketingCustomerOriginalData originalData = new MarketingCustomerOriginalData();
            originalData.setApiCode(apiCode);
            originalData.setRequestId(UUID.randomUUID().toString());
            originalData.setJsonData(jsonData);
            originalData.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
            originalData.setAcceptType(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode());
            originalData.setReceiveDate(LocalDate.now().toString());
            marketingCustomerOriginalDataMapper.insertSelective(originalData);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_UPLOAD_SERVICEERROR.getCode(), "jsonData:" + jsonData,
                    "360催收定制上传数据接入异常！！！"), e);
        }
        log.warn("360催收数据上传接口被调用");
        return sanLiuLingResponseDTO;
    }

}
