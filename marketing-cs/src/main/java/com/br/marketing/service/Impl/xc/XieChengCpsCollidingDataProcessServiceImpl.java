package com.br.marketing.service.Impl.xc;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.XieChengCpsCollidingDataFront;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.XieChengCpsCollidingDataFrontMapper;
import com.br.marketing.mapper.XieChengCpsCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCpsCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 携程CPS撞库数据同步服务实现类
 * @Author chenh
 * @Date 2025-06-26
 */
@Service
@Slf4j
public class XieChengCpsCollidingDataProcessServiceImpl implements XieChengCpsCollidingDataProcessService {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    XieChengCpsCollidingDataFrontMapper xieChengCpsCollidingDataFrontMapper;

    @Resource
    XieChengCpsCollidingDataLoopCycleMapper xieChengCpsCollidingDataLoopCycleMapper;

    @Resource
    XieChengCpsCollidingDataRobMapper xieChengCpsCollidingDataRobMapper;

    @Resource
    XieChengCpsCollidingDataBusinessService xieChengCpsCollidingDataBusinessService;

    private final static int PARTITION_SIZE = 500;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        LocalFileExample example = new LocalFileExample();
        // 查询待处理文件 查询条件b_local_file：fileType="xiecheng_cps_colliding"，status=2，pushstatus=null
        example.createCriteria()
                .andFileTypeEqualTo(SftpFileTypeEnum.XIECHENG_CPS_COLLIDING.getValue())
                .andStatusEqualTo("2")
                .andPushStatusIsNull();

        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(localFiles)) {
            log.info("携程CPS撞库数据同步作业，未找到待处理文件");
            return;
        }

        for (LocalFile localFile : localFiles) {
            try {
                processFile(localFile);
                // 只有process执行成功，才更新push_status，否则下次调度时会重新执行
                updatePushStatus(localFile, "2");
                log.warn("携程CPS撞库数据同步作业，文件处理完成，localFileId: {}", localFile.getId());
            } catch (Exception e) {
                String subject = "携程CPS撞库数据同步作业异常,localFileId:" + localFile.getId();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage(), subject), e);
            }
        }
    }

    private void processFile(LocalFile localFile) {
        TpDynamicExecutor pool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.XIECHENG_CPS_DATA_PROCESS_3710090.getName(), 10, 10);

        Long minId = null;
        while (true) {
            Integer pageSize = marketingCommonConfig.getXieChengCpsCollidingDataSyncPageSize();
            // 查询front表数据，文件内去重通过窗口函数实现
            List<XieChengCpsCollidingDataFront> frontDataList = xieChengCpsCollidingDataFrontMapper
                    .selectNoDupDataByLocalIdtikv_(localFile.getId(), minId, pageSize);

            if (CollectionUtils.isEmpty(frontDataList)) {
                break;
            }
            minId = frontDataList.get(frontDataList.size() - 1).getId();


            List<List<XieChengCpsCollidingDataFront>> partitions = Lists.partition(frontDataList, PARTITION_SIZE);
            for (List<XieChengCpsCollidingDataFront> partition : partitions) {
                List<XieChengCpsCollidingDataFront> list = new ArrayList<>(partition);
                pool.submit(() -> removeDuplicateAndSyncToRob(list, localFile));
            }
        }

        pool.shutdownAndAwaitTermination();
    }

    private void updatePushStatus(LocalFile localFile, String pushStatus) {
        localFile.setPushStatus(pushStatus);
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }

    private void removeDuplicateAndSyncToRob(List<XieChengCpsCollidingDataFront> list, LocalFile localFile) {
        List<XieChengCpsCollidingDataFront> syncToRobData = removeDuplicateData(list);
        if (CollectionUtils.isEmpty(syncToRobData)) {
            return;
        }
        xieChengCpsCollidingDataBusinessService.insertToRobAndUpdateFront(syncToRobData, localFile);
    }

    /**
     * 数据去重处理
     * 1.重复数据从周期表删除
     * 2.与当天已入库数据去重
     * @param list front表数据列表
     * @return 去重后的数据列表
     */
    private List<XieChengCpsCollidingDataFront> removeDuplicateData(List<XieChengCpsCollidingDataFront> list) {
        try {
            List<String> cells = list.stream()
                    .map(XieChengCpsCollidingDataFront::getSha256CodeList)
                    .collect(Collectors.toList());

            // 1.重复数据从周期表删除
            xieChengCpsCollidingDataLoopCycleMapper.batchDeleteExcludeCollidingData(cells, "删除原因：与非周期数据重复");

            // 2.与当天已入库数据去重
            Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date tomorrow = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            List<String> todayRobData = xieChengCpsCollidingDataRobMapper.selectDuplicateDataByCreateTime(cells, today, tomorrow);
            cells.removeAll(todayRobData);
            if (CollectionUtils.isEmpty(cells)) {
                return new ArrayList<>();
            }

            // 返回去重后的数据
            return list.stream()
                    .filter(item -> cells.contains(item.getSha256CodeList()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            String subject = "携程CPS撞库数据同步作业，数据去重处理异常";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage(), subject), e);
            return new ArrayList<>();
        }
    }
} 