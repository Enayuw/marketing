package com.br.marketing.service.carclue.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.StringUtils;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.carclue.CarClueExecuteService;
import com.br.marketing.service.carclue.clueenums.ClueFileRecordingStatusEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName CarClueExecuteServiceImpl
 * @Author kongbx
 * @Date 2025/5/7 14:36
 */
@Service
@Slf4j
public class CarClueExecuteServiceImpl implements CarClueExecuteService {

    @Resource
    private ClueFileRecordingMapper clueFileRecordingMapper;
    @Resource
    private CarClueManageConfigMapper carClueManageConfigMapper;
    @Resource
    private CarChannelConfigMapper carChannelConfigMapper;
    @Resource
    private CarClueProvincesInformationMapper carClueProvincesInformationMapper;
    @Resource
    private CarClueSeriesInformationMapper carClueSeriesInformationMapper;
    @Resource
    private CarClueRelationalMappingMapper carClueRelationalMappingMapper;
    @Resource
    private CarClueInfoMapper carClueInfoMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public Optional<CarClueManageConfig> getCarClueConfig() {
        // 1. 检查是否有待清洗文件
        ClueFileRecordingExample example = new ClueFileRecordingExample();

        List<Integer> list = new ArrayList<>();
        list.add(ClueFileRecordingStatusEnum.AWAIT_CLEAN.getValue());
        list.add(ClueFileRecordingStatusEnum.CLEAN_ING.getValue());

        example.createCriteria()
                .andFileCleanStatusIn(list)
                .andIsDelEqualTo(Constants.DATA_VALID);

        if(clueFileRecordingMapper.countByExample(example) > 0){
            log.warn("存在待清洗的文件!");
            return Optional.empty();
        }

        // 2. 获取车线索管理配置
        CarClueManageConfigExample carClueManageConfigExample = new CarClueManageConfigExample();
        carClueManageConfigExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<CarClueManageConfig> configs = carClueManageConfigMapper.selectByExample(carClueManageConfigExample);
        return CollectionUtil.isEmpty(configs) ? Optional.empty() : Optional.of(configs.get(0));
    }

    @Override
    public List<String> getValueByKey(String key) {
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

    @Override
    public List<CarClueInfo> processClueByIds(String clueIds) {

        // 移除方括号并按逗号分割
        String[] idsArray = clueIds.replace("[", "").replace("]", "").split(",\\s*");

        List<Long> idList = Arrays.stream(idsArray)
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());

        CarClueInfoExample example = new CarClueInfoExample();
        example.createCriteria().andIdIn(idList);
        return carClueInfoMapper.selectByExample(example);
    }

    @Override
    public CarClueReportDTO processClueByRange(String clueRangeJson) {
        CarClueReportDTO dto = JSONObject.parseObject(clueRangeJson, CarClueReportDTO.class);
        if (ObjectUtil.isNotEmpty(dto.getCluePushChannel())) {
            List<String> channels = getValueByKey(dto.getCluePushChannel());
            dto.setCluePushChannel(channels.contains("fail") ? null : channels.get(0));
        }
        return dto;
    }

    @Override
    public List<CarClueProvincesInformation> getProvincesInfo() {
        String proviceCleanDate = carClueProvincesInformationMapper.getMaxCleanDate();
        if (StringUtils.isEmpty(proviceCleanDate)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车线索清洗配置最大清洗日期为空，请关注"));
            return null;
        }
        CarClueProvincesInformationExample provincesInformationExample = new CarClueProvincesInformationExample();
        provincesInformationExample.createCriteria().andAppletDateEqualTo(proviceCleanDate);
        return carClueProvincesInformationMapper.selectByExample(provincesInformationExample);
    }

    @Override
    public List<CarClueSeriesInformation> getSeriesInfo() {
        String seriesCleanDate = carClueSeriesInformationMapper.getMaxCleanDate();
        if (StringUtils.isEmpty(seriesCleanDate)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车线索清洗配置最大清洗日期为空，请关注"));
            return null;
        }
        CarClueSeriesInformationExample seriesInformationExample = new CarClueSeriesInformationExample();
        seriesInformationExample.createCriteria().andAppletDateEqualTo(seriesCleanDate);
        return carClueSeriesInformationMapper.selectByExample(seriesInformationExample);
    }

    @Override
    public List<CarClueRelationalMapping> getRelationalMapping() {
        String relationCleanDate = carClueRelationalMappingMapper.getMaxCleanDate();
        if (StringUtils.isEmpty(relationCleanDate)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(), "车线索清洗配置最大清洗日期为空，请关注"));
            return null;
        }
        CarClueRelationalMappingExample carClueRelationalMappingExample = new CarClueRelationalMappingExample();
        carClueRelationalMappingExample.createCriteria().andAppletDateEqualTo(relationCleanDate).andMatchingTypeEqualTo(0);
        return carClueRelationalMappingMapper.selectByExample(carClueRelationalMappingExample);
    }

    @Override
    public List<CarChannelConfig> getChannelConfig() {
        CarChannelConfigExample channelConfigExample = new CarChannelConfigExample();
        channelConfigExample.createCriteria().andIsDelEqualTo(1);
        List<CarChannelConfig> channelConfigList = carChannelConfigMapper.selectByExample(channelConfigExample);
        channelConfigList.sort(Comparator.comparingInt(t -> t.getOrder()));
        return channelConfigList;
    }

}
