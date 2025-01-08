package com.br.marketing.monkey.job.zhongan;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.ZhonganRosterLockingDataActionBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.monkeydata.handle.zhongan.PushRosterLockingDataToZhongAnHandle;
import com.br.marketing.monkeydata.handle.zhongan.ZhongAnPushRosterDataHandler;
import com.br.marketing.service.Impl.YiXinTransferServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

import lombok.extern.slf4j.Slf4j;

/**
 * 名单锁定推送众安
 * <a href="https://c.100credit.cn/pages/viewpage.action?pageId=171453221">V8-D20240722众安信贷营销名单推送逻辑变更-3710048</a>
 *
 * @author Guo Zeqiang
 * @dateTime 2022/11/17 17:49
 */
@Component
@Slf4j
public class ZhongAnPushRosterLockingDataJob extends AbstractSimpleElasticJob {

    @Resource
    private PushRosterLockingDataToZhongAnHandle rosterLockingDataToZhongAn;

    @Resource
    private ZhongAnPushRosterDataHandler zhongAnPushRosterDataHandler;

    @Resource
    private ZhonganRosterLockingDataMapper zhonganRosterLockingDataMapper;

    @Resource
    private LocalFileMapper localFileMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private YiXinTransferServiceImpl yiXinTransferService;

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;

    private final static String EXECUTE_TIME = "21:00:00";
    private final static String CLEAR_REDIS_TIME = "23:40:00";

    private final static String TITLE = "【名单锁定推送众安】";

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        String zhongAnRosterLockingTime = marketingCommonConfig.getZhongAnRosterLockingTime();
        LocalTime localTimeLockingTime = LocalTime.parse(StringUtils.isNotBlank(zhongAnRosterLockingTime)
                ? zhongAnRosterLockingTime : EXECUTE_TIME);
        if (LocalTime.now().isBefore(localTimeLockingTime)) {
            log.warn(TITLE+"未到配置的运行时间:{}", zhongAnRosterLockingTime);
            return;
        }

        String parameter = shardingContext.getJobParameter();
        long start = System.currentTimeMillis();
        List<String> list = new ArrayList<>();
        String bizDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        List<String> dateList = new ArrayList<>();
        if (StringUtils.isNotEmpty(parameter)) {
            StringTokenizer string = new StringTokenizer(parameter, ",");
            while (string.hasMoreTokens()) {
                String[] split = string.nextToken().split("#");
                list.add(split[0]);
                if (split.length > 1) {
                    dateList.add(split[1]);
                    continue;
                }
                dateList.add(bizDate);
            }
        } else {
            list.add("3710048");
            dateList.add(bizDate);
        }
        HashMap<String, JSONObject> zhongAnDetailPush = marketingCommonConfig.getZhongAnDetailPush();
        if (zhongAnDetailPush == null) {
            return;
        }

        log.warn(TITLE+"调度开始apiCodes:{}, bizDate:{}, 耗时:{}", Arrays.toString(list.toArray())
                , Arrays.toString(dateList.toArray()));
        Page2Condition<ZhonganRosterLockingData> data = new Page2Condition<>();
        data.setPageIndex(0);
        data.setPageSize(2000);
        int i = 0;
        for (String apiCode : list) {
            bizDate = dateList.get(i);
            ++i;
            // 查询推送记录
            List<TransferActionFront> actionFrontList = getActionFront(apiCode, bizDate);
            Long frontId;
            if (actionFrontList.size() > 0) {
                TransferActionFront actionFront = actionFrontList.get(0);
                if (2 == actionFront.getStatus()) {
                    log.warn(TITLE+"该任务今日已经推送"+"api_code:{}, biz_date:{}", apiCode, bizDate);
                    continue;
                } else {
                    frontId = actionFront.getId();
                }
            } else {
                frontId = yiXinTransferService.saveFrontData(apiCode, bizDate, 3);
            }

            List<Long> sftpFileIdList = zhonganRosterLockingDataMapper.getSftpFileIdList(apiCode, bizDate);
            if (!CollectionUtils.isEmpty(sftpFileIdList)) {
                localFileMapper.updateUploadStartTimeById(sftpFileIdList, new Date());
            }
            List<ZhonganRosterLockingDataActionBO> actions = new ArrayList<>();
            // 先处理 isConnect = 1 的，再处理 isConnect = 0 的
            for (int isConnect = 1; isConnect >= 0; --isConnect) {
                actions.add(new ZhonganRosterLockingDataActionBO(apiCode, bizDate, "CG", 1, "1", isConnect));
                actions.add(new ZhonganRosterLockingDataActionBO(apiCode, bizDate, "MG", 2, "1", isConnect));
                actions.add(new ZhonganRosterLockingDataActionBO(apiCode, bizDate, "MG", 1, "1", isConnect));
                actions.add(new ZhonganRosterLockingDataActionBO(apiCode, bizDate, "MG", 2, "7", isConnect));
                actions.add(new ZhonganRosterLockingDataActionBO(apiCode, bizDate, "MG", 2, "8", isConnect));
                actions.add(new ZhonganRosterLockingDataActionBO(apiCode, bizDate, "MG", 2, "4", isConnect));
                actions.add(new ZhonganRosterLockingDataActionBO(apiCode, bizDate, "MG", 2, "2", isConnect));

            }
            List<Boolean> results = actions.stream()
                .map(actionBO -> ResultCode.SUCCESS.getValue().equals(action(actionBO,data).getCode()))
                .collect(Collectors.toList());
            boolean allSuccess = results.stream().allMatch(result -> result);
            if (allSuccess) {
                yiXinTransferService.updateFrontDataStatus(frontId, 2);
            }
            rosterLockingDataToZhongAn.localFilePushStatis(apiCode, bizDate);
        }
        // 清理缓存
        if (LocalTime.now().isAfter(LocalTime.parse(CLEAR_REDIS_TIME))) {
            popCache(RedisKeyConstant.zhongAnblackCusNumToday, 3000);
        }
        long end = System.currentTimeMillis();
        log.warn(TITLE+"调度结束apiCodes:{}, bizDate:{}, 耗时:{}", Arrays.toString(list.toArray())
                , Arrays.toString(dateList.toArray()), end - start);
    }

    /**
     * 2023-05-25 18:03
     * 清理缓存
     */
    private void popCache(String key, int count) {
        try {
            Set<String> custNumCache = redisChgService.spop(key, count);
            if (custNumCache != null && custNumCache.size() > 1) {
                popCache(key, count);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private Result<?> action(ZhonganRosterLockingDataActionBO action, Page2Condition<ZhonganRosterLockingData> condition) {
        long start = System.currentTimeMillis();
        log.warn(TITLE + "action开始" + "apiCode:{}, bizDate:{}, tag:{}, dataSource:{}, userType:{},isConnect:{}", action.getApiCode(),
            action.getBizDate(), action.getTag(), action.getDataSource(), action.getUserType(), action.getIsConnect());
        ZhonganRosterLockingData param = new ZhonganRosterLockingData();
        param.setApiCode(action.getApiCode());
        param.setBizDate(action.getBizDate());
        param.setTag(action.getTag());
        param.setDataSource(action.getDataSource());
        param.setPushStatus(1);
        param.setUserType(action.getUserType());
        param.setIsConnect(action.getIsConnect());
        condition.setParam(param);
        Result actionResult = zhongAnPushRosterDataHandler.action(condition);
        long end = System.currentTimeMillis();
        log.warn(TITLE + "action结束" + "apiCode:{}, bizDate:{}, tag:{}, dataSource:{}, userType:{},isConnect:{} 耗时:{}", action.getApiCode(),
            action.getBizDate(), action.getTag(), action.getDataSource(), action.getUserType(), action.getIsConnect(), end - start);
        return actionResult;
    }

    private List<TransferActionFront> getActionFront(String apiCode, String bizDate) {
        TransferActionFrontExample example = new TransferActionFrontExample();
        TransferActionFrontExample.Criteria criteria = example.createCriteria();
        criteria.andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(bizDate)
                .andActionTypeEqualTo(3)
                .andIsDelEqualTo(1);
        return transferActionFrontMapper.selectByExample(example);
    }
}
