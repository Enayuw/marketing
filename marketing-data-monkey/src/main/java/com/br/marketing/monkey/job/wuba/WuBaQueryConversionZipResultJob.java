package com.br.marketing.monkey.job.wuba;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.wuba.WuBaQueryConversionZipResultDto;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.Impl.wuba.WuBaQueryConversionZipResultService;
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
 * @Description 58新客转化数据zip包清洗
 * @Author lixiang
 * @Date 2024-11-19
 */
@Component
@Slf4j
public class WuBaQueryConversionZipResultJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【58新客转化数据zip包清洗】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private WuBaQueryConversionZipResultService service;

    @Resource
    private JobManager jobManager;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            log.warn(TITLE + "调度开始");
            // switch
            if(!checkJobSwitch()) {
                return;
            }

            // parseParameter
            List<Map<String, String>> paramList = parseParameter();

            // action
            action(paramList);

            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),e.getMessage(), TITLE), e);
        }
    }

    private boolean checkJobSwitch(){
        String jobSwitch = marketingCommonConfig.getWuBaQueryConversionZipResultSwitch();
        if ("1".equals(jobSwitch)) {
            log.warn(TITLE + "开关打开");
            return true;
        }
        log.warn(TITLE + "开关关闭");
        return false;
    }

    private void action(List<Map<String, String>> paramList) {
        // action
        for (Map<String, String> param: paramList) {
            action(param);
        }
    }

    private void action(Map<String, String> param) {
        String apiCode = param.get("apiCode");
        String bizDate = param.get("bizDate");
        // actionFront
        int actionType = JobManager.ActionTypeEnum.WUBA_QUERY_CONVERSION_ZIP_RESULT.getActionType();

        String actionDate = LocalDate.now().toString();
        TransferActionFront action = jobManager.getFrontData(apiCode, actionDate, actionType, null);
        if (action != null) {
            Integer actionStatus = action.getStatus();
            log.warn(TITLE+"任务执行记录已存在, apiCode:{}, actionDate:{}, actionStatus:{}", apiCode, actionDate, actionStatus);
            return;
        }

        action = jobManager.saveFront(apiCode, actionDate, actionType);
        if (action.getId() == null) {
            log.warn(TITLE+ "任务执行记录新增失败, apiCode:{}, actionDate:{}", apiCode, actionDate);
            return;
        }

        WuBaQueryConversionZipResultDto conditionParam = new WuBaQueryConversionZipResultDto();
        conditionParam.setApiCode(apiCode);
        conditionParam.setBizDate(bizDate);

        Page2Condition<WuBaQueryConversionZipResultDto> condition = new Page2Condition<>();
        condition.setParam(conditionParam);
        Result actionResult = service.action(condition);

        if (actionResult!=null && actionResult.isSuccess()){
            jobManager.updateFrontDataStatus(action.getId(), 2);
            log.warn(TITLE+"任务执行记录更新成功, apiCode:{}, actionDate:{}", apiCode, actionDate);
        }
    }

    /**
     * 解析Job参数，格式如下：
     * e.g [{"apiCode":"3710155","bizDate":"-6"},{"apiCode":"3710155","bizDate":"-5"}]
     */
    private List<Map<String, String>> parseParameter() throws Exception {
        List<Map<String, String>> paramList = new ArrayList<>();
        List<Map<String, String>> configList = marketingCommonConfig.getWuBaQueryConversionZipResultParams();
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

            // bizDate
            String bizDateStr = configMap.get("bizDate");
            if(StringUtils.isEmpty(bizDateStr)){
                throw new Exception("Job参数bizDate格式不正确");
            }
            Long bizDateLong = Long.parseLong(bizDateStr);
            LocalDate bizLocalDate = curLocalDate.plusDays(bizDateLong);
            paramMap.put("bizDate", bizLocalDate.toString());

            paramList.add(paramMap);
        }
        log.warn(TITLE + "paramList: {}", JSONObject.toJSONString(paramList));
        return paramList;
    }
}
