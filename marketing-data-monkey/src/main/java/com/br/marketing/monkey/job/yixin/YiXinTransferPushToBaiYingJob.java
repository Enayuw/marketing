package com.br.marketing.monkey.job.yixin;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.monkeydata.entity.yixin.YiXinCondition;
import com.br.marketing.monkeydata.handle.yixin.YiXinBlackPushToBaiYingHandler;
import com.br.marketing.monkeydata.handle.yixin.YiXinTransferPushToBaiYingHandler;
import com.br.marketing.service.IYiXinTransferService;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 宜信转化过滤推送百应
 */
@Component
@Slf4j
public class YiXinTransferPushToBaiYingJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private YiXinTransferPushToBaiYingHandler yiXinTransferPushToBaiYingHandler;

    @Resource
    private YiXinBlackPushToBaiYingHandler yiXinBlackPushToBaiYingHandler;

    @Resource
    private JobManager jobManager;

    @Resource
    private ZnkfPushService znkfPushService;

    @Resource
    private IYiXinTransferService yiXinTransferService;


    private final static String EXECUTE_TIME = "06:00:00";

    private final static String TITLE = "【宜信转化过滤推送百应】";

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            log.warn(TITLE + "调度开始");
            if (!checkExecuteTime()) {
                return;
            }
            List<Map<String, String>> paramList = processJobParameter(shardingContext.getJobParameter());
            processTransfer(paramList);
            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.error(TITLE + "调度异常", e);
        }
    }

    public void processTransfer(List<Map<String, String>> paramList) {
        int actionTypeTransfer = JobManager.ActionTypeEnum.YIXIN_TRANSFER_PUSH_BAIYING.getActionType();

        for (Map<String, String> param: paramList) {
            String apiCode = param.get("apiCode");
            String bizDate = param.get("bizDate");
            String synApiCode = param.get("synApiCode");

            long start = System.currentTimeMillis();
            log.warn(TITLE+"转化数据任务, 调度开始, apiCode:{}, bizDate:{}", apiCode, bizDate);

            // check last
            Result<Date> checkResult = yiXinTransferService.checkPush(apiCode, bizDate);
            if (!ResultCode.SUCCESS.getValue().equals(checkResult.getCode())) {
                log.warn(TITLE+ "转化数据任务, 暂无last=1记录, {}, {}", apiCode, bizDate);
                continue;
            }

            // actionFront
            TransferActionFront actionFront = jobManager.getFrontData(apiCode, bizDate, actionTypeTransfer, null);
            if (actionFront != null) {
                if (2 == actionFront.getStatus()) {
                    log.warn(TITLE+"转化数据任务, 今日已经推送完成"+"api_code:{}, biz_date:{}", apiCode, bizDate);
                    processBlackPush(paramList);
                    continue;
                }
            } else {
                actionFront = jobManager.saveFront(apiCode, bizDate, actionTypeTransfer);
                if (actionFront.getId() == null) {
                    log.warn(TITLE+ "转化数据任务, 执行记录添加失败, {}, {}", apiCode, bizDate);
                    continue;
                }
            }

            // action transfer
            Result result3 = actionTransferPush(apiCode, bizDate, synApiCode,"3");
            Result result4 = actionTransferPush(apiCode, bizDate, synApiCode,"4");

            if (ResultCode.SUCCESS.getValue().equals(result3.getCode())
                    && ResultCode.SUCCESS.getValue().equals(result4.getCode())
            ) {
                jobManager.updateFrontDataStatus(actionFront.getId(), 2);
            }

            long end = System.currentTimeMillis();
            log.warn(TITLE+"转化数据任务, 调度结束, apiCode:{}, bizDate:{}, 耗时:{}", apiCode, bizDate, end - start);
        }
    }

    private Result<?> actionTransferPush(String apiCode, String bizDate, String synApiCode, String priority) {
        log.warn(TITLE+"转化数据任务, action开始, apiCode:{}, bizDate:{}, priority:{}", apiCode, bizDate, priority);
        YiXinCondition condition = new YiXinCondition();
        condition.setPageIndex(0);
        Map<String, Integer> pageConfig = marketingCommonConfig.getYiXinTransferPushBaiYingPageConfig();
        Integer transferPushPageSize = pageConfig.getOrDefault("transferPushPageSize", 2000);
        condition.setPageSize(transferPushPageSize);
        condition.setApiCode(apiCode);
        condition.setRequestData(bizDate);
        condition.setSynApiCode(synApiCode);
        condition.setPriority(priority);
        Result actionResult = yiXinTransferPushToBaiYingHandler.action(condition);
        log.warn(TITLE+"转化数据任务, action结束, apiCode:{}, bizDate:{}, priority:{}", apiCode, bizDate, priority);
        return actionResult;
    }

    private Result<?> actionBlackPush(String apiCode, String bizDate, String synApiCode) {
        YiXinCondition condition = new YiXinCondition();
        condition.setPageIndex(0);
        Map<String, Integer> pageConfig = marketingCommonConfig.getYiXinTransferPushBaiYingPageConfig();
        Integer blackPushPageSize = pageConfig.getOrDefault("blackPushPageSize", 500);
        condition.setPageSize(blackPushPageSize);
        condition.setApiCode(apiCode);
        condition.setRequestData(bizDate);
        condition.setSynApiCode(synApiCode);
        Result actionResult = yiXinBlackPushToBaiYingHandler.action(condition);
        return actionResult;
    }

    /**
     * checkExecuteTime
     */
    private boolean checkExecuteTime(){
        String executeTimeConfig = marketingCommonConfig.getYiXinTransferPushBaiYingExecuteTime();
        LocalTime executeLocalTime = LocalTime.parse(StringUtils.isNotBlank(executeTimeConfig)
                ? executeTimeConfig : EXECUTE_TIME);
        if (LocalTime.now().isBefore(executeLocalTime)) {
            log.warn(TITLE+"未到配置的运行时间:{}", executeLocalTime);
            return false;
        }
        return true;
    }

    /**
     * 解析Job参数，格式如下：
     * e.g [{"apiCode":"3710012","bizDate":"2024-03-11","synApiCode":"3710137"},{"apiCode":"3710012","bizDate":"2024-03-12","synApiCode":"3710137"}]
     */
    private List<Map<String, String>> processJobParameter(String parameter) throws Exception {
        List<Map<String, String>> paramList = new ArrayList<>();
        String curDate = DateUtils.format(new Date(), "yyyy-MM-dd");

        if (StringUtils.isNotEmpty(parameter)) {
            paramList = JSONObject.parseObject(parameter, List.class);
            for(Map<String, String> map : paramList){
                if(StringUtils.isEmpty(map.get("apiCode"))){
                    throw new Exception("Job参数格式不正确");
                }
                if(StringUtils.isEmpty(map.get("synApiCode"))){
                    throw new Exception("Job参数格式不正确");
                }
                if(StringUtils.isEmpty(map.get("bizDate"))){
                    map.put("bizDate", curDate);
                }
            }
            return paramList;
        }

        Map<String, String> map = new HashMap<>();
        map.put("apiCode", "3710012");
        map.put("bizDate", curDate);
        map.put("synApiCode", "3710137");
        paramList.add(map);
        return paramList;
    }

    public void processBlackPush(List<Map<String, String>> paramList) {
        int actionTypeBlack = JobManager.ActionTypeEnum.YIXIN_BLACK_PUSH_BAIYING.getActionType();

        for (Map<String, String> param: paramList) {
            String apiCode = param.get("apiCode");
            String bizDate = param.get("bizDate");
            String synApiCode = param.get("synApiCode");

            long start = System.currentTimeMillis();
            log.warn(TITLE+"黑名单推送任务, 调度开始, apiCode:{}, bizDate:{}", apiCode, bizDate);

            // isPushBlackPhoneEnd
            int hour = LocalDateTime.now().getHour();
            Boolean pushBlackPhoneEnd = znkfPushService.isPushBlackPhoneEnd(apiCode, bizDate);
            if (!pushBlackPhoneEnd && hour < 11) {
                log.warn(TITLE+"黑名单推送任务, 11点前未接收到黑名单标志不推送"+"apiCode:{}, bizDate:{}", apiCode, bizDate);
                continue;
            }

            // actionFront
            TransferActionFront actionFront = jobManager.getFrontData(apiCode, bizDate, actionTypeBlack, null);
            if (actionFront != null) {
                if (2 == actionFront.getStatus()) {
                    log.warn(TITLE+"黑名单推送任务, 今日已经推送完成"+"apiCode:{}, bizDate:{}", apiCode, bizDate);
                    continue;
                }
            } else {
                actionFront = jobManager.saveFront(apiCode, bizDate, actionTypeBlack);
                if (actionFront.getId() == null) {
                    log.warn(TITLE+ "黑名单推送任务, 执行记录添加失败, {}, {}", apiCode, bizDate);
                    continue;
                }
            }

            // actionBlackPush
            Result result = actionBlackPush(apiCode, bizDate, synApiCode);

            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                jobManager.updateFrontDataStatus(actionFront.getId(), 2);
            }

            long end = System.currentTimeMillis();
            log.warn(TITLE+"黑名单推送任务, 调度结束, apiCode:{}, bizDate:{}, 耗时:{}", apiCode, bizDate, end - start);
        }
    }

}
