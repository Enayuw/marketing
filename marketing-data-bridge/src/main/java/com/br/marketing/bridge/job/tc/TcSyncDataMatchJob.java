package com.br.marketing.bridge.job.tc;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.enums.TcSyncRecordStatusEnum;
import com.br.marketing.service.tc.TcSyncDataMatchService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 同程上传数据匹配任务 - GZ数据拉取&&匹配入库
 * @Author zhiyong.zhang
 * @CreateTime 2025/04/21
 */
@Component
@Slf4j
public class TcSyncDataMatchJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程上传数据匹配任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcSyncDataMatchService tcSyncDataMatchService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            log.warn(TITLE+"调度开始");

            Map<String, String> paramMap = parseParmeter();

            atciton(paramMap);

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
     * @param paramMap
     */
    private void atciton(Map<String, String> paramMap) {
        String apiCode = paramMap.get("apiCode");
        List<MarketingTcyrSyncRecord> syncRecordList = tcSyncDataMatchService.searchTcyrSyncList(apiCode, TcSyncRecordStatusEnum.ACCESS_SUCCESS.getValue());
        for (MarketingTcyrSyncRecord syncRecord : syncRecordList) {
            try {
                // 文件过期，继续下一个
                if (checkFileExpire(syncRecord)) {
                    continue;
                }
                tcSyncDataMatchService.updageTcyrRecordSyncStatus(syncRecord.getBatchNo(), TcSyncRecordStatusEnum.MATCH_IN.getValue());
                Result syncResult =tcSyncDataMatchService.dealTcyrFileSync(syncRecord);
                if (syncResult != null  && syncResult.isSuccess()) {
                    tcSyncDataMatchService.updageTcyrRecordSyncStatus(syncRecord.getBatchNo(), TcSyncRecordStatusEnum.MATTCH_COMPELTED.getValue());
                    Long total = JSONObject.parseObject(syncRecord.getData()).getLong("total");
                    log.warn(TITLE+"fileSync任务执行成功,apiCode:{}, batchNo:{},total:{},totalSuccess:{}",syncRecord.getApiCode(),syncRecord.getBatchNo(),total,syncResult.getData().toString());
                }
            }catch (Exception e) {
                log.error("{} apiCode:{}, batchNo:{} fileSync异常,error: ",TITLE,syncRecord.getApiCode(),syncRecord.getBatchNo(),e);
            }
        }
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

    /**
     * 解析job参数
     * 目前参数 tcyrApiCode
     * @return
     */
    private Map<String, String> parseParmeter() throws Exception {
        Map<String, String> paramMap = new HashMap<>();
        String apiCode = marketingCommonConfig.getTcyrApiCode();
        if(StringUtils.isEmpty(apiCode)){
            throw new Exception("Job参数apiCode格式不正确");
        }
        paramMap.put("apiCode", apiCode);
        return paramMap;
    }

}
