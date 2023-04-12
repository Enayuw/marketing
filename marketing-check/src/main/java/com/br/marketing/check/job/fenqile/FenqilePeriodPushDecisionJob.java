package com.br.marketing.check.job.fenqile;

import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.service.IFenqileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * D20230307分期乐自动化周期转决策-3710027（营销→决策）
 * http://c.100credit.cn/pages/viewpage.action?pageId=103556472
 *
 * @author Guo Zeqiang
 * @dateTime 2023-03-10 15:31
 */
@Component
@Slf4j
public class FenqilePeriodPushDecisionJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private IFenqileService iFenqileService;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    /**
     * 2023-03-10 16:40
     * JobParameter 格式：yyyy-MM-ddThh:mm:ss或yyyy-MM-ddThh:mm:ss,yyyy-MM-ddThh:mm:ss
     * eg：2011-12-03T10:15:30,2011-12-05T10:15:30
     */
    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        String parameter = shardingContext.getJobParameter();
        LocalDateTime beginTime = null;
        LocalDateTime endTime = null;
        LocalDate customDate = null;
        if (StringUtils.isNotBlank(parameter)) {
            String[] params = parameter.split(",");
            if (params.length > 1) {
                beginTime = LocalDateTime.parse(params[0], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                endTime = LocalDateTime.parse(params[1], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else if (params.length > 0) {
                try {
                    beginTime = LocalDateTime.parse(params[0], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e) {
                    customDate = LocalDate.parse(params[0], DateTimeFormatter.ISO_LOCAL_DATE);
                }
            }
        }
        Map<String, Map<Integer, String>> apiCodePeriodMap = marketingCommonConfig.getFenqilePeriodPushDecisionPeriod();
        if (CollectionUtils.isEmpty(apiCodePeriodMap)) {
            apiCodePeriodMap = setDefaultValue();
        }
        Set<Map.Entry<String, Map<Integer, String>>> apiCodePeriods = apiCodePeriodMap.entrySet();
        LocalDate localDate = customDate == null ? LocalDate.now() : customDate;
        String beginTimeStr;
        String endTimeStr;
        if (endTime == null) {
            for (Map.Entry<String, Map<Integer, String>> apiCodePeriod : apiCodePeriods) {
                String key = apiCodePeriod.getKey();
                Map<Integer, String> periodMap = apiCodePeriod.getValue();
                Set<Map.Entry<Integer, String>> periods = periodMap.entrySet();
                for (Map.Entry<Integer, String> period : periods) {
                    int day = period.getKey() == null ? 0 : period.getKey();
                    if (beginTime == null || beginTime.toLocalDate().until(localDate, ChronoUnit.DAYS) >= day) {
                        LocalDate localDate1 = localDate.minusDays(day);
                        beginTimeStr = localDate1.atStartOfDay().format(dateTimeFormatter);
                        endTimeStr = localDate1.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 23:59:59:999";
                        Integer sum = iFenqileService.periodPushDecision(key, day, period.getValue(), localDate
                                , beginTimeStr, endTimeStr);
                        printMsg(key, sum, day, period.getValue());
                    }
                }
            }
        } else {
            // 配置自定义时间时只运行周期为0的数据，周期数据也可以通过配置自定义时间的启止来实现
            beginTimeStr = beginTime.format(dateTimeFormatter);
            int day = 0;
            for (Map.Entry<String, Map<Integer, String>> apiCodePeriod : apiCodePeriods) {
                Map<Integer, String> periodMap = apiCodePeriod.getValue();
                if (periodMap.containsKey(day)) {
                    String key = apiCodePeriod.getKey();
                    endTimeStr = endTime.format(dateTimeFormatter);
                    String s = periodMap.get(day);
                    Integer sum = iFenqileService.periodPushDecision(key, day, s, localDate, beginTimeStr, endTimeStr);
                    printMsg(key, sum, day, s);
                }
            }
        }
        long end = System.currentTimeMillis();
        log.warn("【分期乐自动化周期转决策】调度结束，耗时:{},周期信息：{},parameter:{}", end - start, apiCodePeriodMap, parameter);
    }

    private Map<String, Map<Integer, String>> setDefaultValue() {
        Map<String, Map<Integer, String>> apiCodePeriodMap = new HashMap<>(2);
        Map<Integer, String> period = new HashMap<>(4);
        period.put(0, "CASTR0000361");
        period.put(7, "CASTR0000362");
        period.put(14, "CASTR0000363");
        apiCodePeriodMap.put("3710027", period);
        return apiCodePeriodMap;
    }

    private void printMsg(String apiCode, Integer sum, int period, String strategyCode) {
        if (sum == null) {
            return;
        }
        log.warn("分期乐自动化周期转决策，apiCode:{},推送量:{},周期信息：{},{}", apiCode, sum, period, strategyCode);
    }
}
