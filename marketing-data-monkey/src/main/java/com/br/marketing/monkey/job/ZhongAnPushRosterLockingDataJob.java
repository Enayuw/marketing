package com.br.marketing.monkey.job;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.monkeydata.handle.zhongan.PushRosterLockingDataToZhongAnHandle;
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

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
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
            rosterLockingDataToZhongAn.localFilePushStatis(apiCode, bizDate);
        }
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
}
