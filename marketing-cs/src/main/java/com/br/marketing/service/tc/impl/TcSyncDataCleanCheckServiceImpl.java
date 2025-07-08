package com.br.marketing.service.tc.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingTcyrErrorInterfaceLog;
import com.br.marketing.mapper.MarketingTcyrErrorInterfaceLogMapper;
import com.br.marketing.mapper.MarketingTcyrSyncFileMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.tc.TcSyncDataCleanChekService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;


/**
 * 同程易融cleanCheck流程(上传请求失败二次处理)
 * @author zhiyong.zhang
 * @date 2025/07/08
 */
@Service
@Slf4j
public class TcSyncDataCleanCheckServiceImpl implements TcSyncDataCleanChekService {

    private final static String TITLE = "【同程易融-cleanCheck任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PushInfoService pushInfoService;


    @Resource
    private MarketingTcyrSyncFileMapper marketingTcyrSyncFileMapper;


    @Resource
    private MarketingTcyrErrorInterfaceLogMapper errorInterfaceLogMapper;

    @Override
    public void pocess(String apiCode) {
        while (true) {
            Integer searchSize = 1000;
            List<MarketingTcyrErrorInterfaceLog> errorInterfaceLogList = errorInterfaceLogMapper.selectNoDealList(apiCode,searchSize);
            if (CollectionUtils.isEmpty(errorInterfaceLogList)) {
                break;
            }
            errorInterfaceLogList.forEach(this::dealErrorInterface);
        }
    }

    private void dealErrorInterface(MarketingTcyrErrorInterfaceLog errorInterfaceLog) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UploadDataDTO uploadDataDTO = objectMapper.readValue(errorInterfaceLog.getRequestParam(),UploadDataDTO.class);
            Result<Boolean> pushResult = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
            if (pushResult != null && pushResult.isSuccess()) {
                errorInterfaceLogMapper.updateDealStatus(errorInterfaceLog.getId(),1);
                marketingTcyrSyncFileMapper.updateSuccessCount(errorInterfaceLog.getSyncFileId(),errorInterfaceLog.getElementCount());
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
    }
}
