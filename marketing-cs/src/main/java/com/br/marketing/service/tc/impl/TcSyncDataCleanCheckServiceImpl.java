package com.br.marketing.service.tc.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.MarketingTcyrErrorInterfaceLog;
import com.br.marketing.mapper.MarketingTcyrErrorInterfaceLogMapper;
import com.br.marketing.mapper.MarketingTcyrSyncFileMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.tc.TcSyncDataCleanChekService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


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
    private PushInfoService pushInfoService;

    @Resource
    private MarketingTcyrSyncFileMapper marketingTcyrSyncFileMapper;

    @Resource
    private MarketingTcyrErrorInterfaceLogMapper errorInterfaceLogMapper;

    @Override
    public void pocess(String apiCode) {
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_CLEAN_CHECK.getName(), 10, 10);
        try {
            while (true) {
                List<MarketingTcyrErrorInterfaceLog> errorInterfaceLogList = errorInterfaceLogMapper.selectNoDealList(apiCode,500);
                if (CollectionUtils.isEmpty(errorInterfaceLogList)) {
                    break;
                }
                List<Long> idList = errorInterfaceLogList.stream().map(MarketingTcyrErrorInterfaceLog::getId).collect(Collectors.toList());
                errorInterfaceLogMapper.batchUpdateDealStatus(idList,1);
                errorInterfaceLogList.forEach(errorInterfaceLog ->
                        actionPool.execute(() -> dealErrorInterface(errorInterfaceLog))
                );
            }
        }catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }finally {
            actionPool.shutdownAndAwaitTermination();
        }
    }

    private void dealErrorInterface(MarketingTcyrErrorInterfaceLog errorInterfaceLog) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UploadDataDTO uploadDataDTO = objectMapper.readValue(errorInterfaceLog.getRequestParam(),UploadDataDTO.class);
            Result<Boolean> pushResult = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
            if (pushResult != null && pushResult.isSuccess()) {
                errorInterfaceLogMapper.updateDealStatus(errorInterfaceLog.getId(),2);
                marketingTcyrSyncFileMapper.updateSuccessCount(errorInterfaceLog.getSyncFileId(),errorInterfaceLog.getElementCount());
            }else {
                log.error("TITLE:{},上传请求失败，syncFileId: {}, 数据量: {}, resultMsg: {}", TITLE,errorInterfaceLog.getSyncFileId(),
                        errorInterfaceLog.getElementCount(), pushResult.getMessage());
                errorInterfaceLogMapper.updateDealStatus(errorInterfaceLog.getId(),3);
            }
        }catch (Exception e) {
            errorInterfaceLogMapper.updateDealStatus(errorInterfaceLog.getId(),4);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }
}
