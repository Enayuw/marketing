package com.br.marketing.monkey.job.yixin;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.monkeydata.handle.yixin.YixinTransferPushToBaiYingHandler;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.Impl.YiXinTransferServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.util.*;

/**
 * 宜信转化过滤推送百应
 */
@Component
@Slf4j
public class YiXinTransferPushToBaiYingJob extends AbstractSimpleElasticJob {

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private YiXinTransferServiceImpl yiXinTransferService;

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private YixinTransferPushToBaiYingHandler yixinTransferPushToBaiYingHandler;

    @Resource
    private JobManager jobManager;

    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;


    private final static String EXECUTE_TIME = "21:00:00";
    private final static String CLEAR_REDIS_TIME = "23:40:00";

    private final static String TITLE = "【宜信转化过滤推送百应】";

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            log.warn(TITLE + "调度开始");
            if (!checkExecuteTime()) return;
            List<Map<String, String>> paramList = processJobParameter(shardingContext.getJobParameter());
            process(paramList);
            log.warn(TITLE + "调度开始");
        } catch (Exception e) {
            log.error(TITLE + "调度异常", e);
        }
    }

    public void process(List<Map<String, String>> paramList) {
        int actionType = JobManager.ActionTypeEnum.YIXIN_TRANSFER_PUSH_BAIYING.getActionType();

        for (Map<String, String> param: paramList) {
            String apiCode = param.get("apiCode");
            String bizDate = param.get("bizDate");

            long start = System.currentTimeMillis();
            log.warn(TITLE+"调度开始, apiCode:{}, bizDate:{}, 耗时:{}", apiCode, bizDate);

            // actionFront
            TransferActionFront actionFront = jobManager.getFrontData(apiCode, bizDate, actionType, null);
            if (actionFront != null) {
                if (2 == actionFront.getStatus()) {
                    log.warn(TITLE+"该任务今日已经推送"+"api_code:{}, biz_date:{}", apiCode, bizDate);
                    continue;
                }
            } else {
                jobManager.saveFrontData(apiCode, bizDate, actionType);
                if (actionFront.getId() == null) {
                    log.warn(TITLE+ "任务执行记录添加失败, {}, {}", apiCode, bizDate);
                    continue;
                }
            }

            // check last
            String requestId = marketingTransferInfoMapper.queryByApiCodAndLast(apiCode, bizDate, "1");
            if(StringUtils.isEmpty(requestId)){
                log.warn(TITLE+ "暂无last=1记录, {}, {}", apiCode, bizDate);
                continue;
            }

            // action transfer
            Result result = action(apiCode, bizDate);

            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                jobManager.updateFrontDataStatus(actionFront.getId(), 2);
            }

            long end = System.currentTimeMillis();
            log.warn(TITLE+"调度结束, apiCode:{}, bizDate:{}, 耗时:{}", apiCode, bizDate, end - start);
        }
    }

    private Result<?> action(String apiCode, String bizDate) {
        Page2Condition<MarketingTransferSyncUser> condition = new Page2Condition<>();
        condition.setPageIndex(0);
        condition.setPageSize(2000);
        MarketingTransferSyncUser param = new MarketingTransferSyncUser();
        param.setApiCode(apiCode);
        param.setRequestData(bizDate);
        condition.setParam(param);
        Result actionResult = yixinTransferPushToBaiYingHandler.action(condition);
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
     * e.g [{"apiCode":"3710012","bizDate":"2024-03-11"},{"apiCode":"3710012","bizDate":"2024-03-12"}]
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
                if(StringUtils.isEmpty(map.get("bizDate"))){
                    map.put("bizDate", curDate);
                }
            }
            return paramList;
        }

        Map<String, String> map = new HashMap<>();
        map.put("apiCode", "3710012");
        map.put("bizDate", curDate);
        paramList.add(map);
        return paramList;
    }

}
