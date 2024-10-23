package com.br.marketing.monkey.job.qifu;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.qifu.QiFuCuWanJianBatQryUserRealDto;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.service.Impl.qifu.QiFuCuWanJianBatQryUserRealService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * QiFuCuWanJianBatQryUserRealJob
 *
 * @Author lixiang
 * @Date 2024-10-19
 */
@Component
@Slf4j
public class QiFuCuWanJianBatQryUserRealJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【360促完件用户信息批量查询】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private QiFuCuWanJianBatQryUserRealService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            log.warn(TITLE + "调度开始");
            // switch
            if(!checkJobSwitch()) {
                return;
            }
            // Params
            List<Map<String, String>> paramList = parseParameter();
            // pageSize
            Integer pageSize = marketingCommonConfig.getQiFuCuWanJianBatQryUserRealJobPageSize();
            // action
            for (Map<String, String> paramMap : paramList) {
                String apiCode = paramMap.get("apiCode");
                String taskId = paramMap.get("taskId");
                String bizDate = paramMap.get("bizDate");

                Result<Map<String, Object>> actionResult = action(apiCode, taskId, bizDate, pageSize);
                boolean hasScanData = judgeHasScanData(actionResult);
                if(hasScanData){
                    break;
                }
            }

            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(),e.getMessage(), TITLE), e);
        }
    }

    private Result<Map<String, Object>> action(String apiCode, String taskId, String bizDate, Integer pageSize) {
        QiFuCuWanJianBatQryUserRealDto param = new QiFuCuWanJianBatQryUserRealDto();
        param.setApiCode(apiCode);
        param.setTaskId(taskId);
        param.setBizDate(bizDate);
        List<Integer> statusList = new ArrayList<>();
        statusList.add(2);
        statusList.add(4);
        param.setStatusList(statusList);
        Page2Condition<QiFuCuWanJianBatQryUserRealDto> condition = new Page2Condition<>();
        condition.setParam(param);
        condition.setPageSize(pageSize);
        log.warn(TITLE + "condition: {}", JSONObject.toJSON(condition));
        return service.action(condition);
    }

    private boolean checkJobSwitch(){
        String jobSwitch = marketingCommonConfig.getQiFuCuWanJianBatQryUserRealJobSwitch();
        if ("1".equals(jobSwitch)) {
            log.warn(TITLE + "开关打开");
            return true;
        }
        log.warn(TITLE + "开关关闭");
        return false;
    }

    /**
     * 解析Job参数，格式如下：
     * e.g [{"apiCode":"3710155","bizDate":"0"},{"apiCode":"3710155","bizDate":"-1"}]
     */
    private List<Map<String, String>> parseParameter() throws Exception {
        List<Map<String, String>> paramList = new ArrayList<>();
        List<Map<String, String>> configList = marketingCommonConfig.getQiFuCuWanJianBatQryUserRealJobParams();
        LocalDate curLocalDate = LocalDate.now();
        log.warn(TITLE + "curDate: {}", curLocalDate);

        for(Map<String, String> configMap : configList){
            Map<String, String> paramMap = new HashMap<>();
            // apiCode
            String apiCode = configMap.get("apiCode");
            if(StringUtils.isEmpty(apiCode)){
                throw new Exception("Job参数apiCode格式不正确");
            }
            paramMap.put("apiCode", apiCode);

            // taskId
            String taskId = configMap.get("taskId");
            if(StringUtils.isEmpty(taskId)){
                taskId = "";
            }
            paramMap.put("taskId", taskId);

            // bizDate
            String bizDateDay = configMap.get("bizDate");
            if(StringUtils.isEmpty(bizDateDay)){
                throw new Exception("Job参数bizDate格式不正确");
            }
            Long bizDateDayLong = Long.parseLong(bizDateDay);
            LocalDate bizLocalDate = curLocalDate.plusDays(bizDateDayLong);
            String bizDateFormat = bizLocalDate.toString();
            paramMap.put("bizDate", bizDateFormat);

            // add paramList
            paramList.add(paramMap);
        }
        log.warn(TITLE + "paramList: {}", JSONObject.toJSONString(paramList));
        return paramList;
    }

    private boolean judgeHasScanData(Result<Map<String, Object>> actionResult){
        if (actionResult != null && actionResult.isSuccess() && actionResult.getData() != null) {
            Map<String, Object> data = actionResult.getData();
            if(data.get("hasScanData")!=null && "0".equals(String.valueOf(data.get("hasScanData")))) {
                return false;
            }
        }
        return true;
    }
}
