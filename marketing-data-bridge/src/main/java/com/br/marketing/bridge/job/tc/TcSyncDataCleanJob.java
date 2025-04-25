package com.br.marketing.bridge.job.tc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.enums.TcSyncRecordStatusEnum;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.service.tc.TcSyncDataCleanService;
import com.br.marketing.service.tc.TcSyncDataMatchService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description 同城易融上传数据清洗任务
 * @Author zhiyong.zhang
 * @CreateTime 2025/04/21
 */
@Component
@Slf4j
public class TcSyncDataCleanJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同城易融上传数据清洗任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcSyncDataMatchService tcSyncDataMatchService;

    @Resource
    private TcSyncDataCleanService tcSyncDataCleanService;

    @Resource
    private GeneralDataCleanService generalDataCleanService;


    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            log.warn(TITLE+"调度开始");

            //parseParameter
            Map<String, String> paramMap = parseParmeter();
            atciton(paramMap);

            log.warn(TITLE+"调度结束");
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }

    /**
     * 具体执行动作
     * 1、查询未执行任务list
     * 2、单个批次号batchNo任务执行操作:
     *    (1)查询 b_marketing_tcyr_sync_record 匹配完成的数据,修改为清洗中
     *    (2)查找 b_marketing_tcyr_sync (batch_no = record.data.batchNo，is_clean = 0，limit 1000)
     *    (3)调用接口uploadClean(List<Object>, apiCode),
     *    (4)uploadClean成功，修改b_marketing_tcyr_syn is_clean=1
     *    (5)修改batchNo 对应 b_marketing_tcyr_sync_record 状态 ->清洗完成
     * @param paramMap
     */
    private void atciton(Map<String, String> paramMap) {
        String apiCode = paramMap.get("apiCode");
        List<MarketingTcyrSyncRecord> syncRecordList = tcSyncDataMatchService.searchTcyrSyncList(apiCode, TcSyncRecordStatusEnum.MATTCH_COMPELTED.getValue());

        for (MarketingTcyrSyncRecord syncRecord : syncRecordList) {
            try {
                tcSyncDataMatchService.updageTcyrRecordSyncStatus(syncRecord.getBatchNo(), TcSyncRecordStatusEnum.CLEAN_IN.getValue());

                boolean stillFlag =true;
                Long lastSearchId =0L;
                Integer searchSize = marketingCommonConfig.getTcPageSearchSize();
                while (stillFlag) {
                    List<MarketingTcyrSync> tcyrSyncList = tcSyncDataCleanService.selectTcSyncList(syncRecord.getBatchNo(),0,lastSearchId,searchSize);
                    if (CollectionUtils.isEmpty(tcyrSyncList)) {
                        stillFlag = false;
                    }else {
                        if (tcyrSyncList.size() < searchSize) {
                            stillFlag = false;
                        }else {
                            lastSearchId = tcyrSyncList.get(tcyrSyncList.size()-1).getId();
                        }
                        List<JSONObject> jsonObjectList = JSON.parseArray(JSON.toJSONString(tcyrSyncList), JSONObject.class);
                        // 调用uploadClean
                        Result result = generalDataCleanService.uploadClean(jsonObjectList,apiCode);
                        // 返回结果成功 修改isClean状态
                        if (result!=null && result.isSuccess()) {
                            List<Long> idList =tcyrSyncList.stream().map(MarketingTcyrSync::getId).collect(Collectors.toList());
                            tcSyncDataCleanService.updateCleanStatus(idList,1);
                        }
                    }
                }
                tcSyncDataMatchService.updageTcyrRecordSyncStatus(syncRecord.getBatchNo(), TcSyncRecordStatusEnum.CLEAN_COMPLETED.getValue());
                log.warn(TITLE+"fileSync任务执行成功,apiCode:{}, batchNo:{}",syncRecord.getApiCode(),syncRecord.getBatchNo());
            }catch (Exception e) {
                log.error("{} apiCode:{}, batchNo:{} syncDataClean异常,error: ",TITLE,syncRecord.getApiCode(),syncRecord.getBatchNo(),e);
            }
        }
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
