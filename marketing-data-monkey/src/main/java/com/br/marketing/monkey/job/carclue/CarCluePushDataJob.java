package com.br.marketing.monkey.job.carclue;

import cn.hutool.core.collection.CollectionUtil;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CarClueExecuteRecordingMapper;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.mapper.CarClueManageConfigMapper;
import com.br.marketing.service.carclue.CarClueExecuteService;
import com.br.marketing.service.carclue.CarClueService;
import com.br.marketing.service.carclue.clueenums.CarClueManageConfigTypeEnum;
import com.br.marketing.service.carclue.clueenums.CarCluePushStatusEnum;
import com.br.marketing.service.carclue.clueenums.ExecuteClueStatusEnum;
import com.br.marketing.service.carclue.clueenums.ExecuteClueTypeEnum;
import com.br.marketing.service.carclue.push.AbstractClueChannelPush;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

;

/**
 * @ClassName CarCluePushDataJob
 * @Description 车线索推送
 * @Author kongbx
 * @Date 2025/1/15 15:18
 */
@Component
@Slf4j
public class CarCluePushDataJob extends AbstractSimpleElasticJob {

    @Resource
    private CarClueInfoMapper carClueInfoMapper;
    @Resource
    private CarClueExecuteRecordingMapper carClueExecuteRecordingMapper;
    @Resource
    private CarClueService carClueService;
    @Resource
    private CarClueExecuteService carClueExecuteService;
    @Resource
    private CarClueManageConfigMapper carClueManageConfigMapper;
    @Autowired
    private ClueChannelConfigService clueChannelConfigService;

    private static final String TITLE = "【推送车线索】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("{}开始执行", TITLE);
        long startTime = System.currentTimeMillis();

        // 1. 获取车线索管理配置
        CarClueManageConfigExample carClueManageConfigExample = new CarClueManageConfigExample();
        carClueManageConfigExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<CarClueManageConfig> configs = carClueManageConfigMapper.selectByExample(carClueManageConfigExample);

        if (CollectionUtil.isEmpty(configs)) {
            log.warn("{}车线索管理配置为空！", TITLE);
            return;
        }

        // 2.根据配置类型执行推送
        CarClueManageConfig config = configs.get(0);
        if (Objects.equals(config.getPullType(), CarClueManageConfigTypeEnum.PERFORMED_MANUALLY.getValue())) {
            manualPushCarClue();
        } else {
            autoPushCarClue();
        }

        log.warn("{}执行完成, 耗时{}ms", TITLE, System.currentTimeMillis() - startTime);
    }

    /**
     * 手动执行
     */
    private void manualPushCarClue() {
        // 获取待手动执行的推送数据
        CarClueExecuteRecordingExample example = new CarClueExecuteRecordingExample();
        example.createCriteria()
                .andExecuteTypeEqualTo(ExecuteClueTypeEnum.PUSH.getValue())
                .andExecuteStatusEqualTo(ExecuteClueStatusEnum.AWAIT_EXECUTE.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<CarClueExecuteRecording> recordings = carClueExecuteRecordingMapper.selectByExample(example);

        if (CollectionUtil.isEmpty(recordings)) {
            log.warn("{}手动待执行记录为空！", TITLE);
            return;
        }
        recordings.forEach(this::processSingleRecording);
    }

    private void processSingleRecording(CarClueExecuteRecording recording) {
        try {
            if (StringUtils.isNotBlank(recording.getClueIds())) {
                List<CarClueInfo> carClueInfos = carClueExecuteService.processClueByIds(recording.getClueIds());
                pushCluesByChannel(carClueInfos);
            } else if (StringUtils.isNotBlank(recording.getClueRange())) {
                CarClueReportDTO carClueReportDTO = carClueExecuteService.processClueByRange(recording.getClueRange());
                Long minId = null;
                while (true) {
                    List<CarClueInfo> clues = carClueInfoMapper.queryList(carClueReportDTO, minId);
                    if (CollectionUtil.isEmpty(clues)) {
                        break;
                    }
                    minId = clues.get(clues.size() - 1).getId();
                    pushCluesByChannel(clues);
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

    private void pushCluesByChannel(List<CarClueInfo> clues) {
        // 筛选出待推送的数据
        Map<String, List<CarClueInfo>> collect = clues.stream()
                .filter(clue -> Objects.equals(clue.getCluePushStatus(), CarCluePushStatusEnum.READY.getValue()))
                .collect(Collectors.groupingBy(CarClueInfo::getCluePushChannel));

        // 不同渠道推送
        collect.forEach((channel, carClueInfoList) -> {
            AbstractClueChannelPush channelPushImpl = clueChannelConfigService.getChannelPushImpl(channel);
            if (channelPushImpl != null) {
                carClueService.pushCarClueHandler(carClueInfoList, channelPushImpl);
            }
        });
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
    private void autoPushCarClue() {
        List<String> channels = carClueInfoMapper.queryApiCodes(CarCluePushStatusEnum.READY.getValue());
        if (CollectionUtil.isEmpty(channels)) {
            return;
        }
        ThreadPoolExecutor pushCarClueThread = BrExecutors.getThreadPool(5, 5);
        try {
            channels.forEach(channel -> processChannelClues(channel, pushCarClueThread));
        } finally {
            shutdownExecutor(pushCarClueThread);
        }
    }

    private void processChannelClues(String channel, ExecutorService executor) {
        AbstractClueChannelPush pusher = clueChannelConfigService.getChannelPushImpl(channel);
        if (pusher == null) {
            log.warn("{}未找到推送实现，channel：{}", TITLE, channel);
            return;
        }

        Long minId = null;
        while (true) {
            List<CarClueInfo> clues = fetchCluesByChannel(channel, minId, 2000);
            if (CollectionUtil.isEmpty(clues)) {
                break;
            }

            minId = clues.get(clues.size() - 1).getId();
            executor.submit(() -> carClueService.pushCarClueHandler(clues, pusher));
        }
    }

    private List<CarClueInfo> fetchCluesByChannel(String channel, Long minId, int limit) {
        CarClueInfoExample example = new CarClueInfoExample();
        example.setOrderByClause("id limit " + limit);

        CarClueInfoExample.Criteria criteria = example.createCriteria()
                .andCluePushChannelEqualTo(channel)
                .andCluePushStatusEqualTo(CarCluePushStatusEnum.READY.getValue());

        if (minId != null) {
            criteria.andIdGreaterThan(minId);
        }

        return carClueInfoMapper.selectByExample(example);
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10L, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            log.warn("{}推送车线索线程池关闭异常", TITLE, e);
        }
    }

}
