package com.br.marketing.service.didi.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.DiDiCollidingDataRob;
import com.br.marketing.entity.DiDiV5CollidingData;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.*;
import com.br.marketing.service.didi.DiDiDataFilterService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DidiDataFilterServiceImpl implements DiDiDataFilterService {

    private final static String TITLE = "【滴滴V5-筛选数据】";

    private final static int PARTATION_SIZE = 500;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private DiDiV5CollidingDataMapper diDiV5CollidingDataMapper;

    @Resource
    private DidiCallBackDataMapper didiCallBackDataMapper;

    @Resource
    private DiDiV5CollidingDataLogMapper diDiV5CollidingDataLogMapper;

    @Resource
    private DiDiV5CollidingDataRobMapper diDiV5CollidingDataRobMapper;

    /**
     * 滴滴V5筛选job执行方法
     */
    @Override
    public void filter() {
        JSONObject pushConfig = marketingCommonConfig.getDiDiV5Config();
        String apiCode = pushConfig.getString("apiCode");
        LocalFileExample example = new LocalFileExample();
        // 查询待推送文件 查询条件b_local_file：status=2 且 push_status=空
        example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.DD.getValue())
                .andStatusEqualTo("2").andPushStatusIsNull().andApiCodeEqualTo(apiCode);
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(localFiles)) {
            return;
        }
        for (LocalFile localFile : localFiles) {
            try {
                process(localFile);
                // 只有process执行成功，才更新push_status，否则下次调度时会重新执行
                updatePushStatus(localFile);
            } catch (Exception e) {
                String subject = TITLE + localFile.getId();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage()
                        , subject), e);
            }
        }
    }

    private void process(LocalFile localFile) {
        String apiCode = localFile.getApiCode();
        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.DIDI_V5_FILTER.getName(), 50, 50);
        Long minId = null;
        JSONObject pushConfig = marketingCommonConfig.getDiDiV5Config();
        while (true) {
            Integer pageSize = pushConfig.getInteger("limit");
            List<DiDiV5CollidingData> collidingDataList = diDiV5CollidingDataMapper.selectNoDupDataByLocalIdtikv_(localFile.getId(),
                    apiCode, minId, pageSize);
            if (CollectionUtils.isEmpty(collidingDataList)) {
                break;
            }
            List<Long> ids = collidingDataList.stream().map(DiDiV5CollidingData::getId).toList();
            diDiV5CollidingDataMapper.updatePushStatusByIds(1, ids);
            minId = collidingDataList.get(collidingDataList.size() - 1).getId();
            List<List<DiDiV5CollidingData>> partitions = Lists.partition(collidingDataList, PARTATION_SIZE);
            for (List<DiDiV5CollidingData> partition : partitions) {
                List<DiDiV5CollidingData> list = new ArrayList<>(partition);
                pushPool.submit(() -> removeDuplicateAndInsertToRob(list, localFile, apiCode));
            }
            diDiV5CollidingDataMapper.updatePushStatusByIds(3, ids);
        }
        pushPool.shutdownAndAwaitTermination();
    }

    private void updatePushStatus(LocalFile localFile) {
        localFile.setPushStatus("2");
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }

    private void removeDuplicateAndInsertToRob(List<DiDiV5CollidingData> list, LocalFile localFile, String apiCode) {
        try {
            List<DiDiCollidingDataRob> insertToRobData = filter(list, apiCode);
            if (CollectionUtils.isEmpty(insertToRobData)) {
                return;
            }
            diDiV5CollidingDataRobMapper.insertToRobAndUpdateFront(insertToRobData);
        } catch (Exception e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage()
                    , TITLE + localFile.getId()), e);
        }
    }

    private List<DiDiCollidingDataRob> filter(List<DiDiV5CollidingData> list, String apiCode) {
        try {
            JSONObject pushConfig = marketingCommonConfig.getDiDiV5Config();
            Set<String> cells = list.stream().map(DiDiV5CollidingData::getCell).collect(Collectors.toSet());

            // 1.筛选规则1
            boolean preScreen1 = pushConfig.getBoolean("preScreen1");
            if(preScreen1) {
                List<String> delayLoopCycleData = didiCallBackDataMapper.selectPushedCells(cells, apiCode);
                delayLoopCycleData.forEach(cells::remove);
                if (CollectionUtils.isEmpty(cells)) {
                    return Lists.newArrayList();
                }
            }

            // 2.筛选规则2
            boolean preScreen2 = pushConfig.getBoolean("preScreen2");
            if(preScreen2) {
                List<Integer> failMsgs = pushConfig.getObject("failMsgs", List.class);
                if (!CollectionUtils.isEmpty(failMsgs)) {
                    List<String> loopCycleData = diDiV5CollidingDataLogMapper.checkCellBatchFailMsgs(cells, failMsgs);
                    loopCycleData.forEach(cells::remove);
                }
                if (CollectionUtils.isEmpty(cells)) {
                    return Lists.newArrayList();
                }
            }
            return list.stream().filter(t -> cells.contains(t.getCell()))
                    .map(t -> {
                        DiDiCollidingDataRob diDiCollidingDataRob = new DiDiCollidingDataRob();
                        BeanUtils.copyProperties(t, diDiCollidingDataRob);
                        diDiCollidingDataRob.setPackageId(t.getLocalId());
                        diDiCollidingDataRob.setSourceType("F");
                        diDiCollidingDataRob.setIsDelete(0);
                        return diDiCollidingDataRob;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            String subject = TITLE + "数据剔除，子线程处理异常！";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage()
                    , subject), e);
            return new ArrayList<>();
        }
    }

}
