package com.br.marketing.monkey.job.wuba;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.wuba.WuBaChangeSubmitDataDto;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.Impl.wuba.WuBaSubmitConversionChangeDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @Description 58新客提交营销名单修改上报数据
 * @Author lixiang
 * @Date 2024-07-08
 */
@Component
@Slf4j
public class WuBaSubmitConversionChangeDataJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【58新客提交营销名单修改上报数据】";
    private static final String PUSH_TIME_START = "pushTimeStart";
    private static final String PUSH_TIME_END = "pushTimeEnd";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private WuBaSubmitConversionChangeDataService service;

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
            for (Map<String, String> param: paramList) {
                action(param);
            }

            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),e.getMessage(), TITLE), e);
        }
    }

    private boolean checkJobSwitch(){
        String wuBaSubmitConversionSwitch = marketingCommonConfig.getWuBaSubmitConversionChangeDataSwitch();
        if ("1".equals(wuBaSubmitConversionSwitch)) {
            log.warn(TITLE + "开关打开");
            return true;
        }
        log.warn(TITLE + "开关关闭");
        return false;
    }

    private void action(Map<String, String> param) {
        String apiCode = param.get("apiCode");
        String marketingTimeStart = param.get("marketingTimeStart");
        String marketingTimeEnd = param.get("marketingTimeEnd");

        // actionFront
        int actionType = JobManager.ActionTypeEnum.WUBA_CHANGE_SUBMIT_DATA_BATCH.getActionType();

        String bizDate = DateUtils.format(new Date(), "yyyy-MM-dd");
        TransferActionFront actionFront = jobManager.getFrontData(apiCode, bizDate, actionType, null);
        if (actionFront != null) {
            if (2 == actionFront.getStatus()) {
                log.warn(TITLE+"今日已经更新完成, apiCode:{}, bizDate:{}", apiCode, bizDate);
                return;
            }
        } else {
            actionFront = jobManager.saveFront(apiCode, bizDate, actionType);
            if (actionFront.getId() == null) {
                log.warn(TITLE+ "更新失败, apiCode:{}, bizDate:{}", apiCode, bizDate);
                return;
            }
        }

        WuBaChangeSubmitDataDto conditionParam = new WuBaChangeSubmitDataDto();
        conditionParam.setApiCode(apiCode);
        conditionParam.setMarketingTimeStart(marketingTimeStart);
        conditionParam.setMarketingTimeEnd(marketingTimeEnd);

        Page2Condition<WuBaChangeSubmitDataDto> condition = new Page2Condition<>();
        condition.setParam(conditionParam);
        Result actionResult = service.action(condition);

        if (actionResult!=null && actionResult.isSuccess()){
            jobManager.updateFrontDataStatus(actionFront.getId(), 2);
            log.warn(TITLE+"今日更新成功, apiCode:{}, bizDate:{}", apiCode, bizDate);
        }
    }

    /**
     * 解析Job参数，格式如下：
     * e.g [{"apiCode":"3710155","bizDate":"-6"},{"apiCode":"3710155","bizDate":"-5"}]
     */
    private List<Map<String, String>> parseParameter() throws Exception {
        List<Map<String, String>> paramList = new ArrayList<>();
        List<Map<String, String>> configList = marketingCommonConfig.getWuBaSubmitConversionChangeDataParams();
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
            LocalDate startLocalDate = curLocalDate.plusDays(bizDateLong);
            LocalDate endLocalDate = startLocalDate.plusDays(1);

            Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant());
            Date endDate = Date.from(endLocalDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant());

            String marketingTimeStart = DateUtils.format(startDate, "yyyy-MM-dd");
            String marketingTimeEnd = DateUtils.format(endDate, "yyyy-MM-dd");

            paramMap.put("marketingTimeStart", marketingTimeStart);
            paramMap.put("marketingTimeEnd", marketingTimeEnd);

            paramList.add(paramMap);
        }
        log.warn(TITLE + "paramList: {}", JSONObject.toJSONString(paramList));
        return paramList;
    }
}
