package com.br.marketing.monkey.job.wuba;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.wuba.WubaQueryConversionDto;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.service.Impl.wuba.WuBaQueryConversionResultService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 58新客提交营销名单结果查询
 * @Author lixiang
 * @Date 2024-07-08
 */
@Component
@Slf4j
public class WuBaQueryConversionResultJob extends AbstractSimpleElasticJob {

    private static final String TITLE = "【58新客提交营销名单结果查询】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private WuBaQueryConversionResultService service;

    private static final String PUSH_TIME_START = "pushTimeStart";
    private static final String PUSH_TIME_END = "pushTimeEnd";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            log.warn(TITLE + "调度开始");
            // switch
            if(!checkJobSwitch()) {
                return;
            }

            // pushTimeInterval
            Map<String, Object> pushTimeInterval = acquirePushTimeInterval();

            // action
            List<String> apiCodes = marketingCommonConfig.getWuBaQueryConversionApiCodes();
            for (String apiCode: apiCodes) {
                actionByApiCode(apiCode, pushTimeInterval);
            }

            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),e.getMessage() , TITLE), e);
        }
    }

    private boolean checkJobSwitch(){
        String wuBaQueryConversionResultSwitch = marketingCommonConfig.getWuBaQueryConversionSwitch();
        if ("1".equals(wuBaQueryConversionResultSwitch)) {
            log.warn(TITLE + "开关打开");
            return true;
        }
        log.warn(TITLE + "开关关闭");
        return false;
    }

    private Map<String, Object> acquirePushTimeInterval() throws Exception {
        Map<String, Object> res = new HashMap<>();
        JSONObject interval = marketingCommonConfig.getWuBaQueryConversionPushTimeInterval();
        Integer pushStart = -2;
        Integer pushEnd = -1;
        if(interval != null){
            Integer pushStartSpeed = interval.getInteger("pushStart");
            Integer pushEndSpeed = interval.getInteger("pushEnd");
            if(pushStart != null){
                pushStart = pushStartSpeed;
            }
            if(pushEnd != null){
                pushEnd = pushEndSpeed;
            }
        }

        LocalDate curLocalDate = LocalDate.now();

        LocalDate startLocalDate = curLocalDate.plusDays(pushStart);
        Date pushTimeStart = Date.from(startLocalDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant());

        LocalDate endLocalDate = curLocalDate.plusDays(pushEnd);
        Date endDate = Date.from(endLocalDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant());
        String startPushTimeStr = DateUtils.format(endDate, "yyyy-MM-dd 23:00:00");
        Date pushTimeEnd = DateUtils.parse(startPushTimeStr, "yyyy-MM-dd HH:mm:ss");

        res.put(PUSH_TIME_START, pushTimeStart);
        res.put(PUSH_TIME_END, pushTimeEnd);

        return res;
    }

    private void actionByApiCode(String apiCode, Map<String, Object> pushTimeInterval) {
        WubaQueryConversionDto param = new WubaQueryConversionDto();
        param.setBatchType(2);
        param.setQueryStatus(0);
        param.setApiCode(apiCode);
        param.setPushTimeStart((Date) pushTimeInterval.get(PUSH_TIME_START));
        param.setPushTimeEnd((Date) pushTimeInterval.get(PUSH_TIME_END));

        Page2Condition<WubaQueryConversionDto> condition = new Page2Condition<>();
        condition.setParam(param);
        log.warn(TITLE + "condition: {}", JSONObject.toJSON(condition));
        service.action(condition);
    }
}
