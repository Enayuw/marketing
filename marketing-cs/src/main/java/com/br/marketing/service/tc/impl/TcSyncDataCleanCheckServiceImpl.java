package com.br.marketing.service.tc.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    private MarketingTcyrSyncFileMapper marketingTcyrSyncFileMapper;

    @Resource
    private MarketingTcyrErrorInterfaceLogMapper errorInterfaceLogMapper;

    @Override
    public void pocess(String apiCode) {
        ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(
                marketingCommonConfig.getTcCleanCheckShardConfig().getInteger("threadPool"),
                marketingCommonConfig.getTcCleanCheckShardConfig().getInteger("threadPool"));
        try {
            while (true) {
                if (!marketingCommonConfig.getTcCleanCheckShardConfig().getBoolean("jobSwitch")) {
                    break;
                }
                Integer searchSize = marketingCommonConfig.getTcCleanCheckShardConfig().getInteger("pageSize");
                List<MarketingTcyrErrorInterfaceLog> errorInterfaceLogList = errorInterfaceLogMapper.selectNoDealList(apiCode,searchSize);
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
            shutdownThreadPool(actionPool);
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
                errorInterfaceLogMapper.updateDealStatus(errorInterfaceLog.getId(),0);
            }
        }catch (Exception e) {
            errorInterfaceLogMapper.updateDealStatus(errorInterfaceLog.getId(),0);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    public  void shutdownThreadPool(ThreadPoolExecutor executor) {
        log.warn(TITLE + "shutdownThreadPool开始");
        executor.shutdown();
        try {
            while (!executor.awaitTermination(60L, TimeUnit.SECONDS)) {
                log.info("{},线程池关闭",TITLE);
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            log.error("{},日志保存线程池结束异常！",TITLE,ex);
            Thread.currentThread().interrupt();
        }
        log.warn(TITLE + "shutdownThreadPool结束");
    }
}
