package com.br.marketing.monkey.job.wuba;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.service.Impl.wuba.WuBaSubmitConversionService;
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
 * @Description 58新客提交营销名单
 * @Author lixiang
 * @Date 2024-07-08
 */
@Component
@Slf4j
public class WuBaSubmitConversionJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【58新客提交营销名单】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private WuBaSubmitConversionService service;

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
            Integer pageSize = marketingCommonConfig.getWuBaSubmitConversionPageSize();

            // action
            for (Map<String, String> paramMap : paramList) {
                String apiCode = paramMap.get("apiCode");
                String bizDate = paramMap.get("bizDate");
                String userType = paramMap.get("userType");
                Integer createDate = Integer.parseInt(bizDate);

                Result<Map<String, Object>> actionResult = action(apiCode, createDate, userType, pageSize);
                boolean hasScanData = judgeHasScanData(actionResult);
                if(hasScanData){
                    break;
                }
            }

            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),e.getMessage(), TITLE), e);
        }
    }

    private Result<Map<String, Object>> action(String apiCode, Integer createDate, String userType, Integer pageSize) {
        WubaSubmitConversionData param = new WubaSubmitConversionData();
        param.setApiCode(apiCode);
        param.setStatus(1);
        param.setPushStatus(0);
        param.setUserType(userType);
        param.setCreateDate(createDate);
        Page2Condition<WubaSubmitConversionData> condition = new Page2Condition<>();
        condition.setParam(param);
        condition.setPageSize(pageSize);
        log.warn(TITLE + "condition: {}", JSONObject.toJSON(condition));
        return service.action(condition);
    }

    private boolean checkJobSwitch(){
        String wuBaSubmitConversionSwitch = marketingCommonConfig.getWuBaSubmitConversionSwitch();
        if ("1".equals(wuBaSubmitConversionSwitch)) {
            log.warn(TITLE + "开关打开");
            return true;
        }
        log.warn(TITLE + "开关关闭");
        return false;
    }

    /**
     * 解析Job参数，格式如下：
     * e.g [{"apiCode":"3710155","bizDate":"0","userType":"2"},{"apiCode":"3710155","bizDate":"-6","userType":"1"}]
     */
    private List<Map<String, String>> parseParameter() throws Exception {
        List<Map<String, String>> paramList = new ArrayList<>();
        List<Map<String, String>> configList = marketingCommonConfig.getWuBaSubmitConversionParams();
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
            Date bizDate = Date.from(bizLocalDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant());
            String bizDateFormat = DateUtils.format(bizDate, "yyyyMMdd");

            paramMap.put("bizDate", bizDateFormat);

            // userType
            String userType = configMap.get("userType");
            if(StringUtils.isEmpty(userType)){
                throw new Exception("Job参数bizDate格式不正确");
            }
            paramMap.put("userType", userType);

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
