package com.br.marketing.service.Impl.wuba;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.WubaCollidingDataFront;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.WubaCollidingDataFrontMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 58撞库数据同步作业实现类
 * @Author hong.chen
 * @CreateTime 2024/07/10
 */
@Service
@Slf4j
public class WuBaCollidingDataSynchronismServiceImpl implements WuBaCollidingDataSynchronismService {
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    private LocalFileMapper localFileMapper;
    @Resource
    WubaCollidingDataFrontMapper wubaCollidingDataFrontMapper;
    @Resource
    WuBaCollidingDataBusinessService wuBaCollidingDataBusinessService;

    private final static int PARTATION_SIZE = 50;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        marketingCommonConfig.getWubaCollidingApiCodes().forEach((String apiCode) -> {
            LocalFileExample example = new LocalFileExample();
            // 查询待推送文件 查询条件b_local_file：status=2 且 push_status=空
            example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.WUBA_COLLIDING.getValue())
                    .andStatusEqualTo("2").andPushStatusIsNull().andApiCodeEqualTo(apiCode);
            List<LocalFile> localFiles = localFileMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(localFiles)) {
                return;
            }

            for (LocalFile localFile : localFiles) {
                try {
                    process(localFile);
                    updatePushStatus(localFile, "2");
                } catch (Exception e) {
                    //推送异常更新状态,更新为失败status=3
                    updatePushStatus(localFile, "3");
                    String subject = "58撞库数据同步作业异常,localFIleId:" + localFile.getId();
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                            , subject), e);
                }
            }
        });
    }

    private void process(LocalFile localFile) {
        String apiCode = localFile.getApiCode();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(marketingCommonConfig.getWubaCollidingDataSyncThreadNum(),
                marketingCommonConfig.getWubaCollidingDataSyncThreadNum());

        // 查询高价值文件id
        List<Long> highValueIdList = getHighValueFileIds(apiCode);
        String highValueIds = Objects.isNull(highValueIdList) ? "(\"\")" : "(" + Joiner.on(",").join(highValueIdList) + ")";
        // 查询-2的文件id
        String reavedFileIds = getWubaCollidingReavedFileIds();
        log.warn("58撞库数据同步作业，开启撞库的status=-2文件ids：{}", reavedFileIds);
        Long minId = null;
        while (true) {
            Integer pageSize = marketingCommonConfig.getWuBaCollidingDataSyncPageSize();

            // local_id and status =1 and push_status =1，去重逻辑：
            // 1.与该文件本身数据去重
            // 2.与当天已上传数据去重
            // 3.与高价值数据去重
            // 4.与周期非金融数据去重
            // 5.与周期金融数据去重
            // 6.与周期非金融status=-2包去重
            // 7.与周期金融status=-2包去重
            // 8.与补包status=-2包去重
            Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date tomorrow = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            List<WubaCollidingDataFront> wubaCollidingDataFronts = wubaCollidingDataFrontMapper.selectNoDupDataByCurDatetikv_(localFile.getId(),
                    apiCode, minId, pageSize, today, tomorrow, highValueIds, reavedFileIds);
            if (CollectionUtils.isEmpty(wubaCollidingDataFronts)) {
                break;
            }
            minId = wubaCollidingDataFronts.get(wubaCollidingDataFronts.size() - 1).getId();

            modifyThreadPool(pool);

            List<List<WubaCollidingDataFront>> partitions = Lists.partition(wubaCollidingDataFronts, PARTATION_SIZE);
            for (List<WubaCollidingDataFront> partition : partitions) {
                pool.submit(() -> wuBaCollidingDataBusinessService.insertToRobAndUpdateFront(partition, localFile));
            }
        }

        threadPoolShutDown(pool);
    }

    private String getWubaCollidingReavedFileIds() {
        List<Long> reavedFileIds = new ArrayList<>();
        HashMap<String, JSONObject> map = marketingCommonConfig.getWubaCollidingReavedFileIds();
        for (Map.Entry<String, JSONObject> mapEntry : map.entrySet()) {
            for (Map.Entry<String, Object> booleanEntry : mapEntry.getValue().entrySet()) {
                if ((Boolean) booleanEntry.getValue()) {
                    reavedFileIds.add(Long.valueOf(booleanEntry.getKey()));
                }
            }
        }

        if (CollectionUtils.isEmpty(reavedFileIds)) {
            return "(\"\")";
        }
        return "(" + Joiner.on(",").join(reavedFileIds) + ")";
    }

    @Override
    public List<Long> getHighValueFileIds(String apiCode) {
        List<String> highValueFiles = marketingCommonConfig.getWubaCollidingHighValueFiles();
        if (CollectionUtils.isEmpty(highValueFiles)) {
            return null;
        }

        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria().andApiCodeEqualTo(apiCode).andFileNameIn(highValueFiles);
        List<LocalFile> localFiles = localFileMapper.selectByExample(localFileExample);
        List<Long> highValueIds = localFiles.stream().map(LocalFile::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(highValueIds)) {
            return null;
        }

        return highValueIds;
    }

    private void modifyThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getWubaCollidingDataSyncThreadNum();
        pool.setCorePoolSize(threadNum);
        pool.setMaximumPoolSize(threadNum);
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("58撞库数据同步作业，线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), ex.getMessage()
                    , "58撞库数据同步作业，日志保存线程池结束异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

    private void updatePushStatus(LocalFile localFile, String pushStatus) {
        localFile.setPushStatus(pushStatus);
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }
}
