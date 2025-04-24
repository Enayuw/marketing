package com.br.marketing.bridge.job.tc;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingTcyrTransferRecord;
import com.br.marketing.enums.TcTransferRecordStatusEnum;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.service.tc.TcTransferRecordService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description 同城易融转化数据清洗任务
 * @Author zhiyong.zhang
 * @CreateTime 2025/04/21
 */
@Component
@Slf4j
public class TcTransferCleanJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同城易融转化数据清洗任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcTransferRecordService tcTransferRecordService;


    @Resource
    private GeneralDataCleanService generalDataCleanService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            log.warn(TITLE+"调度开始");
            // switch
            if (!checkJobSwitch()) {
                return;
            }

            //parseParameter
            Map<String, String> paramMap = parseParmeter();
            atciton(paramMap);

            log.warn(TITLE+"调度结束");
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_TC.getCode(),e.getMessage(), TITLE), e);
        }
    }

    /**
     * 具体执行动作
     *
     * @param paramMap
     */
    private void atciton(Map<String, String> paramMap) {
        String apiCode = paramMap.get("apiCode");

        Long lastSearchId = 0L;
        Integer searchSize =  marketingCommonConfig.getTcPageSearchSize();
        boolean stillFlag = true;
        while (stillFlag) {
            List<MarketingTcyrTransferRecord> tcyrTransferRecordList = tcTransferRecordService.selectTcyrTransforRecordList(apiCode, TcTransferRecordStatusEnum.ACCESS_SUCCESS.getValue(),lastSearchId,searchSize);
            if (CollectionUtils.isEmpty(tcyrTransferRecordList)) {
                stillFlag = false;
            }else{
                if (tcyrTransferRecordList.size() < searchSize) {
                    stillFlag = false;
                }else {
                    lastSearchId = tcyrTransferRecordList.get(tcyrTransferRecordList.size()-1).getId();
                }
                List<Long> idList = tcyrTransferRecordList.stream().map(MarketingTcyrTransferRecord::getId).collect(Collectors.toList());
                tcTransferRecordService.updateStatus(idList,TcTransferRecordStatusEnum.CLEAN_IN.getValue());
                List<JSONObject> jsonObjectList = tcyrTransferRecordList.stream().map(m->JSONObject.parseObject(m.getData())).collect(Collectors.toList());
                // 调用transferClean(List<JsonObject>, apiCode)；
                Result result = generalDataCleanService.transferClean(jsonObjectList,apiCode);
                if (result !=null && result.isSuccess()) {
                    tcTransferRecordService.updateStatus(idList,TcTransferRecordStatusEnum.CLEAN_COMPLETED.getValue());
                }
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

    /**
     * 检测开关
     * @return
     */
    private boolean checkJobSwitch(){
        String jobSwitch = marketingCommonConfig.getTcTransferCleanJobSwitch();
        if ("1".equals(jobSwitch)) {
            log.warn(TITLE + "开关打开");
            return true;
        }
        log.warn(TITLE + "开关关闭");
        return false;
    }
}
