package com.br.marketing.bridge.job.tc;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.enums.TcSyncRecordStatusEnum;
import com.br.marketing.service.tc.TcSyncDataDownService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @Description 同程上传数据-拉取文件&&DB入库
 * @Author zhiyong.zhang
 * @CreateTime 2025/04/21
 */
@Component
@Slf4j
public class TcSyncDataDownToDbJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程易融-DownToDb任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcSyncDataDownService tcSyncDataDownService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        //TODO 把处理过的 url配置下，重新下载
        try {
            log.warn(TITLE+"调度开始");
            atciton(marketingCommonConfig.getTcyrApiCode());
            log.warn(TITLE+"调度结束");
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    /**
     * 具体执行动作
     * 1、b_marketing_tcyr_sync_record 查询 ACCESS_SUCCESS接入成功的 list
     * 2、单个批次号batchNo任务执行操作:
     *      <1>判断文件是否有效
     *      <2>batchNo对应状态修改为 MATCH_IN匹配中
     *      <3>下载文件
     *      <4>判断匹配
     *      <5>基础数据入库
     *      <6> TODO 上传SFTP服务器
     *      <7>修改batchNo 对应记录为MATTCH_COMPELTED
     *
     *      downStatus: 0未下载 1下载中 2下载完成
     * @param apiCode
     */
    private void atciton(String apiCode) {
        //
        List<MarketingTcyrSyncRecord> syncRecordList = tcSyncDataDownService.searchTcyrSyncList(apiCode, TcSyncRecordStatusEnum.ACCESS_SUCCESS.getValue(),getStartOfDay(),getEndOfDay());

        for (MarketingTcyrSyncRecord syncRecord : syncRecordList) {
            try {
                tcSyncDataDownService.updageTcyrRecordDownStatus(syncRecord.getBatchNo(), 1);
                Result syncResult =tcSyncDataDownService.dealTcyrFileSync(syncRecord);
                if (syncResult != null  && syncResult.isSuccess()) {
                    tcSyncDataDownService.updageTcyrRecordDownStatus(syncRecord.getBatchNo(), 2);
                    Long total = JSONObject.parseObject(syncRecord.getData()).getLong("total");
                    Long totalSuccess = Long.parseLong(syncResult.getData().toString());
                    log.warn(TITLE+"任务执行成功,apiCode:{}, batchNo:{},total:{},totalSuccess:{}",syncRecord.getApiCode(),syncRecord.getBatchNo(),total,totalSuccess);
                    if (total != null && !total.equals(totalSuccess)) {
                        log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),"batchNo:"+syncRecord.getBatchNo()+",total:"+total+",success:"+totalSuccess+",success数量和total数不一致", TITLE));
                    }
                }
            }catch (Exception e) {
                log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
            }
        }
    }



    public static Date getStartOfDay() {
        LocalDateTime todayStart = LocalDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        return Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant());
    }

    // 获取当天的结束时间 (23:59:59.999999999)
    public static Date getEndOfDay() {
        LocalDateTime todayEnd = LocalDateTime.now()
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999);
        return Date.from(todayEnd.atZone(ZoneId.systemDefault()).toInstant());
    }


    /**
     * 判断文件是否过期 true:过期 false:未过期
     * @param syncRecord
     * @return
     */
    private boolean checkFileExpire(MarketingTcyrSyncRecord syncRecord) {
        boolean expireResult = false;
        String dataInfo = syncRecord.getData();
        if (StringUtils.isEmpty(dataInfo)) {
            log.warn("apiCode:{},batchNo:{} 下载数据为空",syncRecord.getApiCode(),syncRecord.getBatchNo());
            return false;
        }
        JSONObject dataJson = JSONObject.parseObject(dataInfo);
        String fileExpirationTime = dataJson.getString("fileExpirationTime");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime expirationTime = LocalDateTime.parse(fileExpirationTime, formatter);
        if (LocalDateTime.now().isAfter(expirationTime)) {
            expireResult = true;
            log.warn("batchNo:{},文件过期 endTime:{}",syncRecord.getBatchNo(),expirationTime);
        }
        return expireResult;
    }
}
