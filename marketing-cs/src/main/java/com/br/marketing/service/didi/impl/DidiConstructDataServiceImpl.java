package com.br.marketing.service.didi.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.DidiCallBackData;
import com.br.marketing.mapper.DidiCallBackDataMapper;
import com.br.marketing.service.didi.DidiConstructDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DidiConstructDataServiceImpl implements DidiConstructDataService {

    private final static String TITLE = "【滴滴V5-构造数据】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DidiCallBackDataMapper didiCallBackDataMapper;

    /**
     * 数据构造job执行方法
     */
    @Override
    public void process() {
        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.DIDI_V5_CALLBACK.getName(), 50, 50);
        try {
            JSONObject pushConfig = marketingCommonConfig.getDiDiV5Config();
            Double samplingCallRate = pushConfig.getDouble("samplingCallRate") != null ?
                    pushConfig.getDouble("samplingCallRate") : 0;
            Double samplingSmsRate = pushConfig.getDouble("samplingSmsRate") != null ?
                    pushConfig.getDouble("samplingSmsRate") : 0;
            String apiCode = pushConfig.getString("apiCode");
            // 构造拨打成功的数据
            processStageData(samplingCallRate, 3, apiCode);
            // 构造短信成功的数据
            processStageData(samplingSmsRate, 4, apiCode);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "触达回推job执行异常", TITLE), e);
        } finally {
            pushPool.shutdownAndAwaitTermination();
        }
    }

    /**
     * 分阶段处理数据
     */
    private void processStageData(Double samplingRate, int pushType, String apiCode) {
        Long lastId = 0L;
        int pageSize = marketingCommonConfig.getDiDiV5Config().getInteger("limit");
        while (true) {
            if (marketingCommonConfig.getDiDiV5Config().getBooleanValue("constructSwitch")) {
                log.warn("检测到中断信号，停止构造阶段{}的数据", pushType);
                break;
            }

            List<DidiCallBackData> pageData = queryData(lastId, pushType, pageSize, apiCode);
            if (CollectionUtils.isEmpty(pageData)) {
                break;
            }
            lastId = pageData.get(pageData.size() - 1).getId();

            Set<String> cellSet = pageData.stream().map(DidiCallBackData::getCustNum).collect(Collectors.toSet());
            List<String> constructedCells = didiCallBackDataMapper.queryConstructedData(cellSet, pushType);

            // 过滤已构造的cell
            List<DidiCallBackData> filteredData = pageData.stream()
                    .filter(data -> !constructedCells.contains(data.getCustNum()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(filteredData)) {
                continue;
            }

            // 按cell分组，每个cell只取一条
            Map<String, List<DidiCallBackData>> cellGroupMap = filteredData.stream()
                    .collect(Collectors.groupingBy(DidiCallBackData::getCustNum));

            List<DidiCallBackData> uniqueData = new ArrayList<>();
            for (List<DidiCallBackData> cellDataList : cellGroupMap.values()) {
                DidiCallBackData selectedData = cellDataList.get(0);
                uniqueData.add(selectedData);
            }
            List<DidiCallBackData> dataToPush = samplingData(uniqueData, samplingRate, pushType);
            // 更新数据
            dataToPush.forEach(data -> didiCallBackDataMapper.updateByPrimaryKey(data));
        }
    }

    /**
     * 游标分页查询数据
     */
    private List<DidiCallBackData> queryData(Long lastId, int stage, int pageSize, String apiCode) {
        return switch (stage) {
            case 3 -> didiCallBackDataMapper.queryDidiCellFailData(pageSize, lastId, apiCode);
            case 4 -> didiCallBackDataMapper.queryDidiSmsFailData(pageSize, lastId, apiCode);
            default -> Lists.newArrayList();
        };
    }

    /**
     * 蓄水池抽样
     */
    private List<DidiCallBackData> samplingData(List<DidiCallBackData> dataList, Double samplingRate, int stage) {
        if (CollectionUtils.isEmpty(dataList) || samplingRate >= 1.0) {
            return dataList;
        }
        if (samplingRate == 0) {
            return Lists.newArrayList();
        }

        int sampleSize = new BigDecimal(dataList.size())
                .multiply(BigDecimal.valueOf(samplingRate))
                .setScale(0, RoundingMode.UP)
                .intValue();
        List<DidiCallBackData> reservoirs = new ArrayList<>(sampleSize);
        // 前k个元素直接放入蓄水池
        for (int i = 0; i < sampleSize; i++) {
            DidiCallBackData sample = prepareSample(dataList.get(i), stage);
            reservoirs.add(sample);
        }
        // 处理剩余元素
        for (int i = sampleSize; i < dataList.size(); i++) {
            int j = RandomUtils.nextInt(0, i + 1);
            if (j < sampleSize) {
                DidiCallBackData sample = prepareSample(dataList.get(i), stage);
                reservoirs.set(j, sample);
            }
        }
        return reservoirs;
    }

    private DidiCallBackData prepareSample(DidiCallBackData data, int stage) {
        DidiCallBackData sample = new DidiCallBackData();
        BeanUtils.copyProperties(data, sample);
        sample.setId(data.getId());
        sample.setCustNum(data.getCustNum());
        sample.setCell(data.getCell());
        sample.setScas(data.getScas());
        sample.setApiCode(data.getApiCode());
        sample.setCreateTime(data.getCreateTime());
        sample.setExtend(data.getExtend());
        sample.setPushType(stage);
        sample.setUpdateTime(new Date());
        sample.setConstructTime(new Date());

        // 根据阶段设置特定字段
        if (stage == 3) {
            sample.setIsConnect(1);
            sample.setCallbackType(1);
        } else {
            sample.setSmsSendStatus(1);
            sample.setCallbackType(2);
        }
        return sample;
    }
}
