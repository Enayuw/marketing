package com.br.marketing.service.Impl.wuba;

import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.WubaCollidingDataFront;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.WubaCollidingDataFrontMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private final static int PARTATION_SIZE = 2000;

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
                    localFile.setPushStartTime(new Date());
                    process(localFile);
                    updatePushStatus(localFile, "2");
                } catch (Exception e) {
                    //推送异常更新状态,更新为失败status=3
                    updatePushStatus(localFile, "3");
                    log.error("58撞库数据同步作业异常，localFIleId：{}", localFile.getId(), e);
                }
            }
        });
    }

    private void process(LocalFile localFile) {
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(20, 20);
        Long minId = null;
        while (true) {
            Integer pageSize = marketingCommonConfig.getWuBaCollidingDataSyncPageSize();

            // local_id and status =1 and push_status =1
            List<WubaCollidingDataFront> wubaCollidingDataFronts = wubaCollidingDataFrontMapper.selectNoDupDataByCurDate(localFile.getId(),
                    localFile.getApiCode(), minId, pageSize);
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
            log.error("58撞库数据同步作业，日志保存线程池结束异常！", ex);
            Thread.currentThread().interrupt();
        }
    }

    private void updatePushStatus(LocalFile localFile, String pushStatus) {
        localFile.setPushEndTime(new Date());
        localFile.setPushStatus(pushStatus);
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }
}
