package com.br.marketing.monkey.job.carclue;

import cn.hutool.core.collection.CollectionUtil;
import com.br.common.log.AlertLog;
import com.br.common.util.StringUtils;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CarClueExecuteRecordingMapper;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.carclue.CarClueExecuteService;
import com.br.marketing.service.carclue.CarClueService;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.service.carclue.clueenums.CarClueManageConfigTypeEnum;
import com.br.marketing.service.carclue.clueenums.ExecuteClueStatusEnum;
import com.br.marketing.service.carclue.clueenums.ExecuteClueTypeEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 车线索数据清洗作业job
 *
 * @author zhen.Li1
 * @dateTime 2025/01/05 14:13
 */
@Component
@Slf4j
public class CarClueDataCleanJob extends AbstractSimpleElasticJob {

    @Resource
    private CarClueInfoMapper carClueInfoMapper;
    @Resource
    private CarClueService carClueService;

    @Resource
    private CarClueExecuteRecordingMapper carClueExecuteRecordingMapper;
    @Resource
    private CarClueExecuteService carClueExecuteService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;

    public static ThreadPoolExecutor pushCluePool = BrExecutors.getThreadPool(10, 10);

    private static final String TITLE = "【清洗车线索】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("{}开始执行", TITLE);
        long startTime = System.currentTimeMillis();

        // 1. 获取车线索管理配置
        Optional<CarClueManageConfig> configOpt = carClueExecuteService.getCarClueConfig();
        if (!configOpt.isPresent()) {
            log.warn("{}车线索管理配置为空！", TITLE);
            return;
        }

        // 2. 根据配置类型执行清洗
        CarClueManageConfig config = configOpt.get();
        if (Objects.equals(config.getCleanType(), CarClueManageConfigTypeEnum.PERFORMED_MANUALLY.getValue())) {
            manualCleanCarClue();
        } else {
            autoCleanCarClue();
        }

        log.warn("{}执行完成, 耗时{}ms", TITLE, System.currentTimeMillis() - startTime);
    }

    /**
     * 手动执行
     */
    private void manualCleanCarClue() {
        // 获取待手动执行的推送数据
        CarClueExecuteRecordingExample example = new CarClueExecuteRecordingExample();
        example.createCriteria()
                .andExecuteTypeEqualTo(ExecuteClueTypeEnum.CLEAN.getValue())
                .andExecuteStatusEqualTo(ExecuteClueStatusEnum.AWAIT_EXECUTE.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<CarClueExecuteRecording> recordings = carClueExecuteRecordingMapper.selectByExample(example);

        if (CollectionUtil.isEmpty(recordings)) {
            log.warn("{}手动待执行记录为空！", TITLE);
            return;
        }
        // 获取字典信息
        List<CarClueProvincesInformation> carClueProvincesInfoList = carClueExecuteService.getProvincesInfo();
        List<CarClueSeriesInformation> carClueSeriesInfoList = carClueExecuteService.getSeriesInfo();
        List<CarClueRelationalMapping> carClueRelationalMappingList = carClueExecuteService.getRelationalMapping();
        List<CarChannelConfig> channelConfigList = carClueExecuteService.getChannelConfig();

        for (CarClueExecuteRecording recording : recordings) {
            processSingleRecording(recording, carClueProvincesInfoList,
                    carClueSeriesInfoList, carClueRelationalMappingList, channelConfigList);
        }
    }

    private void processSingleRecording(CarClueExecuteRecording recording, List<CarClueProvincesInformation> carClueProvincesInfoList,
                                        List<CarClueSeriesInformation> carClueSeriesInfoList, List<CarClueRelationalMapping> carClueRelationalMappingList,
                                        List<CarChannelConfig> channelConfigList) {
        try {
            if (StringUtils.isNotBlank(recording.getClueIds())) {

                List<CarClueInfo> carClueInfos = carClueExecuteService.processClueByIds(recording.getClueIds());
                pushCluesByChannel(carClueInfos, carClueProvincesInfoList,
                        carClueSeriesInfoList, carClueRelationalMappingList, channelConfigList);

            } else if (StringUtils.isNotBlank(recording.getClueRange())) {

                CarClueReportDTO carClueReportDTO = carClueExecuteService.processClueByRange(recording.getClueRange());
                Long minId = null;
                while (true) {
                    List<CarClueInfo> clues = carClueInfoMapper.queryList(carClueReportDTO, minId);
                    if (CollectionUtil.isEmpty(clues)) {
                        break;
                    }
                    minId = clues.get(clues.size() - 1).getId();
                    pushCluesByChannel(clues, carClueProvincesInfoList,
                            carClueSeriesInfoList, carClueRelationalMappingList, channelConfigList);
                }

            } else {
                log.warn("{}线索查询条件为空！记录ID:{}", TITLE, recording.getId());
            }

            updateRecordingStatus(recording.getId(), ExecuteClueStatusEnum.EXECUTE_FINISH.getValue());
        } catch (Exception e) {
            log.error("{}处理推送记录失败, ID:{}", TITLE, recording.getId(), e);
            updateRecordingStatus(recording.getId(), ExecuteClueStatusEnum.EXECUTE_ERROR.getValue());
        }
    }

    private void pushCluesByChannel(List<CarClueInfo> carClueInfoList, List<CarClueProvincesInformation> carClueProvincesInfoList,
                                    List<CarClueSeriesInformation> carClueSeriesInfoList, List<CarClueRelationalMapping> carClueRelationalMappingList,
                                    List<CarChannelConfig> channelConfigList) {
        try {
            //筛选出待清洗的数据
            List<CarClueInfo> readyClueList = carClueInfoList.stream()
                    .filter(clue -> CarClueDataStatusEnum.READY.getValue().equals(clue.getClueDataStatus()))
                    .collect(Collectors.toList());

            cleanClueList(readyClueList, channelConfigList, carClueProvincesInfoList, carClueSeriesInfoList, carClueRelationalMappingList);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车线索清洗线程处理异常，请关注"), e);
        }
    }

    private void updateRecordingStatus(Long id, Integer status) {
        CarClueExecuteRecording update = new CarClueExecuteRecording();
        update.setId(id);
        update.setExecuteStatus(status);
        carClueExecuteRecordingMapper.updateByPrimaryKeySelective(update);
    }

    /**
     * 自动执行
     */
    private void autoCleanCarClue() {
        // 获取字典信息
        List<CarClueProvincesInformation> carClueProvincesInfoList = carClueExecuteService.getProvincesInfo();
        List<CarClueSeriesInformation> carClueSeriesInfoList = carClueExecuteService.getSeriesInfo();
        List<CarClueRelationalMapping> carClueRelationalMappingList = carClueExecuteService.getRelationalMapping();
        List<CarChannelConfig> channelConfigList = carClueExecuteService.getChannelConfig();
        // 通话明细apiCode
        Map<String, String> carClueStorageConfig = marketingCommonConfig.getCarClueStorageConfig();
        // 提取所有 apiCode
        List<String> carClueApiCodes = new ArrayList<>(carClueStorageConfig.values());

        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        boolean mark = Boolean.TRUE;
        Long minId = null;
        while (mark) {
            List<CarClueInfo> carClueInfoList = carClueInfoMapper.selectCarClueByMinId(carClueApiCodes, CarClueDataStatusEnum.READY.getValue(), minId);
            if (carClueInfoList.size() <= 0) {
                mark = Boolean.FALSE;
                continue;
            }
            minId = carClueInfoList.get(carClueInfoList.size() - 1).getId();
            List<List<CarClueInfo>> partitions = Lists.partition(carClueInfoList, 500);
            for (List<CarClueInfo> partition : partitions) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        cleanClueList(partition, channelConfigList, carClueProvincesInfoList, carClueSeriesInfoList, carClueRelationalMappingList);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车线索清洗线程处理异常，请关注"), e);
                    }
                }, pushCluePool));

            }

        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void cleanClueList(List<CarClueInfo> carClueInfoList, List<CarChannelConfig> channelConfigList, List<CarClueProvincesInformation>
            carClueProvincesInfoList, List<CarClueSeriesInformation> carClueSeriesInfoList, List<CarClueRelationalMapping> carClueRelationalMappingList) {
        long start = System.currentTimeMillis();
        carClueInfoList.forEach(carClueInfo -> {
            try {
                //清除错误信息
                carClueInfo.setClueErrorReason("");
                List<CarChannelConfig> configList = new ArrayList<>(channelConfigList);
                carClueService.carClueCleanHandler(carClueInfo, carClueProvincesInfoList, carClueSeriesInfoList, carClueRelationalMappingList,
                        configList);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车线索清洗异常，请关注"), e);
            }
        });
        log.warn("车线索清洗单批次，耗时：{}ms", System.currentTimeMillis() - start);
    }

}
