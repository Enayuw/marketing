package com.br.marketing.monkey.job.carclue;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.entity.*;;
import com.br.marketing.mapper.CarClueExecuteRecordingMapper;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.mapper.CarClueManageConfigMapper;
import com.br.marketing.mapper.ClueFileRecordingMapper;
import com.br.marketing.service.carclue.CarClueService;
import com.br.marketing.service.carclue.clueenums.CarCluePushStatusEnum;
import com.br.marketing.service.carclue.clueenums.ClueFileRecordingStatusEnum;
import com.br.marketing.service.carclue.clueenums.ExecuteClueStatusEnum;
import com.br.marketing.service.carclue.clueenums.ExecuteClueTypeEnum;
import com.br.marketing.service.carclue.push.AbstractClueChannelPush;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private ClueFileRecordingMapper clueFileRecordingMapper;
    @Resource
    private CarClueManageConfigMapper carClueManageConfigMapper;
    @Resource
    private CarClueExecuteRecordingMapper carClueExecuteRecordingMapper;
    @Resource
    private CarClueService carClueService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Autowired
    private ClueChannelConfigService clueChannelConfigService;

    private static final String TITLE = "【推送车线索】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn(TITLE + "start");
        long start = System.currentTimeMillis();
        // 判断是否存在待清洗的文档记录
        ClueFileRecordingExample clueFileRecordingExample = new ClueFileRecordingExample();
        clueFileRecordingExample.createCriteria()
                .andFileCleanStatusEqualTo(ClueFileRecordingStatusEnum.AWAIT_CLEAN.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);
        int i = clueFileRecordingMapper.countByExample(clueFileRecordingExample);
        if(i > 0){
            log.warn(TITLE + "存在待清洗的文件!");
            return;
        }
        // 获取车线索配置 手动 or 自动
        CarClueManageConfigExample carClueManageConfigExample = new CarClueManageConfigExample();
        carClueManageConfigExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<CarClueManageConfig> carClueManageConfigs = carClueManageConfigMapper.selectByExample(carClueManageConfigExample);
        if(CollectionUtil.isEmpty(carClueManageConfigs)){
            log.warn(TITLE + "车线索配置为空！");
            return;
        }
        CarClueManageConfig carClueManageConfig = carClueManageConfigs.get(0);
        Integer pullType = carClueManageConfig.getPullType();
        if(pullType == 0){
            manualPushCarClue();
        }else {
            autoPushCarClue();
        }
        long end = System.currentTimeMillis();
        log.warn(TITLE + "end, 耗时{}ms", end-start);
    }

    private void manualPushCarClue() {
        // 获取 待手动执行的推送数据
        CarClueExecuteRecordingExample example = new CarClueExecuteRecordingExample();
        example.createCriteria().andExecuteTypeEqualTo(ExecuteClueTypeEnum.PUSH.getValue())
                .andExecuteStatusEqualTo(ExecuteClueStatusEnum.AWAIT_EXECUTE.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<CarClueExecuteRecording> carClueExecuteRecordings = carClueExecuteRecordingMapper.selectByExample(example);
        if(CollectionUtil.isEmpty(carClueExecuteRecordings)){
            log.warn(TITLE + "手动待执行记录为空！");
            return;
        }

        for (CarClueExecuteRecording carClueExecuteRecording : carClueExecuteRecordings){
            String clueIds = carClueExecuteRecording.getClueIds();
            List<CarClueInfo> carClueInfoList;
            if(!StringUtils.isEmpty(clueIds)){
                List<Long> list = new ArrayList<>();
                String[] split = clueIds.split(",");
                for (String s : split){
                    list.add(Long.valueOf(s));
                }
                CarClueInfoExample carClueInfoExample = new CarClueInfoExample();
                carClueInfoExample.createCriteria().andIdIn(list).andCluePushStatusEqualTo(CarCluePushStatusEnum.READY.getValue());
                carClueInfoList = carClueInfoMapper.selectByExample(carClueInfoExample);
            }else {
                String clueRange = carClueExecuteRecording.getClueRange();
                if(StringUtils.isBlank(clueRange)){
                    log.warn(TITLE + "线索查询条件为空！");
                    continue;
                }
                CarClueReportDTO carClueReportDTO = JSONObject.parseObject(clueRange, CarClueReportDTO.class);
                if (ObjectUtil.isNotEmpty(carClueReportDTO.getCluePushChannel())) {
                    List<String> cluePushChannel = getValueByKey(carClueReportDTO.getCluePushChannel());
                    if (cluePushChannel.contains("fail")) {
                        carClueReportDTO.setCluePushChannel(null);
                    } else {
                        carClueReportDTO.setCluePushChannel(cluePushChannel.get(0));
                    }
                }
                carClueInfoList = carClueInfoMapper.queryList(carClueReportDTO);
            }
            Map<String, List<CarClueInfo>> groupedByChannel = carClueInfoList.stream()
                    .collect(Collectors.groupingBy(CarClueInfo::getCluePushChannel));

            groupedByChannel.forEach((channel, clues) -> {
                AbstractClueChannelPush channelPushImpl = clueChannelConfigService.getChannelPushImpl(channel);
                carClueService.pushCarClueHandler(clues, channelPushImpl);
            });

            //修改状态为 执行完成
            CarClueExecuteRecording recording = new CarClueExecuteRecording();
            recording.setId(carClueExecuteRecording.getId());
            recording.setExecuteStatus(ExecuteClueStatusEnum.EXECUTE_FINISH.getValue());
            carClueExecuteRecordingMapper.updateByPrimaryKeySelective(recording);
        }

    }

    private void autoPushCarClue() {

        List<String> channels = carClueInfoMapper.queryApiCodes(CarCluePushStatusEnum.READY.getValue());
        if(CollectionUtil.isEmpty(channels)){
            return;
        }

        ThreadPoolExecutor pushCarClueThread =
                BrExecutors.getThreadPool(5, 5);

        for (String channel : channels) {

            AbstractClueChannelPush channelPushImpl = clueChannelConfigService.getChannelPushImpl(channel);

            if(channelPushImpl == null){
                log.warn(TITLE + "未找到推送实现，channel：{}", channel);
                continue;
            }

            Long minId = null;
            boolean isContiue = Boolean.TRUE;
            while (isContiue) {
                CarClueInfoExample carClueInfoExample = new CarClueInfoExample();
                carClueInfoExample.setOrderByClause("id limit 2000");

                CarClueInfoExample.Criteria criteria = carClueInfoExample.createCriteria()
                        .andCluePushChannelEqualTo(channel)
                        .andCluePushStatusEqualTo(CarCluePushStatusEnum.READY.getValue());

                if (minId != null) {
                    criteria.andIdGreaterThan(minId);
                }

                List<CarClueInfo> carClueInfoList = carClueInfoMapper.selectByExample(carClueInfoExample);
                if (CollectionUtil.isEmpty(carClueInfoList)) {
                    isContiue = Boolean.FALSE;
                    continue;
                }
                minId = carClueInfoList.get(carClueInfoList.size() - 1).getId();
                pushCarClueThread.submit(() -> carClueService.pushCarClueHandler(carClueInfoList, channelPushImpl));
            }
        }
        pushCarClueThread.shutdown();
        try {
            while (!pushCarClueThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("推送车线索线程池关闭");
            }
        } catch (InterruptedException ex) {
            pushCarClueThread.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), "推送车线索线程池关闭！异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

    public List<String>  getValueByKey(String key) {
        try {
            Map<String, Object> carClueApiCodeMapping = marketingCommonConfig.getCarClueApiCodeMapping();
            Map<String, List> channel = (Map<String, List>) carClueApiCodeMapping.get("channel");
            if (ObjectUtil.isEmpty(channel)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                        "渠道不存在！"));
            }
            List<String> carClueApiCodes = channel.get(key);
            return ObjectUtil.isNotEmpty(carClueApiCodes) ? carClueApiCodes : new ArrayList<>();
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "获取推送渠道映射失败！错误信息：" + e.getMessage()), e);
            return new ArrayList<>();
        }
    }

}
