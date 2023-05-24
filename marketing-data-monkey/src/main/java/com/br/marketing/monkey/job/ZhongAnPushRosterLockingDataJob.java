package com.br.marketing.monkey.job;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.monkeydata.handle.zhongan.PushRosterLockingDataToZhongAnHandle;
import com.br.marketing.service.Impl.YiXinTransferServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 名单锁定推送众安
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

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        String zhongAnRosterLockingTime = marketingCommonConfig.getZhongAnRosterLockingTime();
        LocalTime localTimeLockingTime = LocalTime.parse(StringUtils.isNotBlank(zhongAnRosterLockingTime)
                ? zhongAnRosterLockingTime : EXECUTE_TIME);
        if (LocalTime.now().isBefore(localTimeLockingTime)) {
            log.warn("【名单锁定推送众安】未到配置的运行时间:{}", zhongAnRosterLockingTime);
            return;
        }
        long start = System.currentTimeMillis();
        List<String> list = new ArrayList<>(Collections.singletonList("3710048"));
        String bizDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        List<String> dateList = new ArrayList<>(Collections.singletonList(bizDate));
        String parameter = shardingContext.getJobParameter();
        if (StringUtils.isNotEmpty(parameter)) {
            StringTokenizer string = new StringTokenizer(parameter, ",");
            while (string.hasMoreTokens()) {
                String[] split = string.nextToken().split("#");
                list.add(split[0]);
                if (split.length > 1) {
                    dateList.add(split[1]);
                    return;
                }
                dateList.add(bizDate);
            }
        }
        HashMap<String, JSONObject> zhongAnDetailPush = marketingCommonConfig.getZhongAnDetailPush();
        if (zhongAnDetailPush == null) {
            return;
        }

        Page2Condition<ZhonganRosterLockingData> data = new Page2Condition<>();
        data.setPageIndex(0);
        data.setPageSize(2000);
        int i = 0;
        for (String apiCode : list) {
            bizDate = dateList.get(i);
            ++i;
            //查询推送记录
            List<TransferActionFront> actionFrontList = getActionFront(apiCode, bizDate);
            if (actionFrontList.size() > 0) {
                log.warn("{}【名单锁定推送众安】该任务今日已经推送", apiCode);
            }
            Long frontId = yiXinTransferService.saveFrontData(apiCode, bizDate, 1);
            List<Long> sftpFileIdList = zhonganRosterLockingDataMapper.getSftpFileIdList(apiCode, bizDate);
            if (!CollectionUtils.isEmpty(sftpFileIdList)) {
                localFileMapper.updateUploadStartTimeById(sftpFileIdList, new Date());
            }
            long startcg = System.currentTimeMillis();
            action("CG", apiCode, bizDate, data);
            long endcg = System.currentTimeMillis();
            log.warn("{}【CG名单锁定推送众安】结束，耗时:{}", apiCode, endcg - startcg);

            long startmg = System.currentTimeMillis();
            action("MG", apiCode, bizDate, data);
            long endmg = System.currentTimeMillis();
            log.warn("{}【MG名单锁定推送众安】结束，耗时:{}", apiCode, endmg - startmg);
            yiXinTransferService.updateFrontDataStatus(frontId, 2);
            rosterLockingDataToZhongAn.localFilePushStatis(apiCode, bizDate);
        }
        // 清理缓存
//        List<String> custNumCache = redisChgService.srandmember(RedisKeyConstant.zhongAnblackCusNumToday,50000);
//        while (custNumCache != null && custNumCache.size() > 1) {
//            redisChgService.srem(RedisKeyConstant.zhongAnblackCusNumToday, custNumCache.toArray(new String[0]));
//        }
        long end = System.currentTimeMillis();
        log.warn("【名单锁定推送众安】调度结束，耗时:{}", end - start);
    }

    private void action(String tag, String apiCode, String bizDate, Page2Condition<ZhonganRosterLockingData> data) {
        ZhonganRosterLockingData zhonganRosterLockingData = new ZhonganRosterLockingData();
        zhonganRosterLockingData.setApiCode(apiCode);
        zhonganRosterLockingData.setTag(tag);
        zhonganRosterLockingData.setBizDate(bizDate);
        zhonganRosterLockingData.setPushStatus(1);
        data.setParam(zhonganRosterLockingData);
        rosterLockingDataToZhongAn.action(data);
    }


    private List<TransferActionFront> getActionFront(String apiCode, String bizDate) {
        TransferActionFrontExample example = new TransferActionFrontExample();
        TransferActionFrontExample.Criteria criteria = example.createCriteria();
        criteria.andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(bizDate)
                .andActionTypeEqualTo(1)
                .andIsDelEqualTo(1);
        return transferActionFrontMapper.selectByExample(example);
    }
}
