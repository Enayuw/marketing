package com.br.marketing.monkey.job;

import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.monkeydata.service.PushRosterLockingDataToZhongAn;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private PushRosterLockingDataToZhongAn rosterLockingDataToZhongAn;

    @Resource
    private ZhonganRosterLockingDataMapper zhonganRosterLockingDataMapper;

    @Resource
    private LocalFileMapper localFileMapper;


    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        List<String> list = new ArrayList<>(Collections.singletonList("3710048"));
        String parameter = shardingContext.getJobParameter();
        if (StringUtils.isNotEmpty(parameter)) {
            StringTokenizer string = new StringTokenizer(parameter, ",");
            while (string.hasMoreTokens()) {
                list.add(string.nextToken());
            }
        }
        String bizDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        Page2Condition<ZhonganRosterLockingData> data = new Page2Condition<>();
        data.setPageIndex(0);
        data.setPageSize(2000);
        for (String apiCode : list) {
            List<Long> sftpFileIdList = zhonganRosterLockingDataMapper.getSftpFileIdList(apiCode, bizDate);
            if (!CollectionUtils.isEmpty(sftpFileIdList)) {
                localFileMapper.updateUploadStartTimeById(sftpFileIdList, new Date());
            }
            List<String> tags = zhonganRosterLockingDataMapper.getTagByApiCodeBizDateList(apiCode, bizDate);
            for (String tag : tags) {
                long startTag = System.currentTimeMillis();
                action(tag, apiCode, bizDate, data);
                long endTag = System.currentTimeMillis();
                log.warn("{}【{}名单锁定推送众安】结束，耗时:{}", apiCode, tag, endTag - startTag);
            }
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
