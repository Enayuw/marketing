package com.br.marketing.service.clean.CarClue.impl;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.CarClue.CarCluesDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CarCluesDataCleanServiceImpl implements CarCluesDataCleanService {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    private CarClueInfoMapper carClueInfoMapper;

    @Resource
    private CallRecordMapper callRecordMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Override
    public void cleanCallDetailsData(List<String> apiCodes, String date) {
        if (ObjectUtil.isEmpty(apiCodes)) {
            return;
        }
        apiCodes.stream().forEach((String apiCode) -> clean(apiCode, date));
    }

    public void clean(String apiCode, String date) {
        String cid = tableCreateService.getCId(apiCode);
        List<String> carClueIntentionGrades = marketingCommonConfig.getCarClueIntentionGrades();
        JSONObject carClueDataCleanConfig = marketingCommonConfig.getCarClueDataCleanConfig();
        Integer limit = carClueDataCleanConfig.getInteger("limit");
        Integer threadNum = carClueDataCleanConfig.getInteger("threadNum");
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum,
                "CAR_CLUE_DATA_CLEAN_THREAD_POOL", 200);
        boolean mark = Boolean.TRUE;
        // 查询今日是否存在未清洗数据
        Long minId = callRecordMapper.cleanDataOfMinId(apiCode, date);
        if (minId == null) {
            return;
        }
        minId = minId - 1;
        while (mark) {
            // 获取待清洗数据
            List<CallRecord> callRecords = callRecordMapper.cleanDataByMinId(apiCode, date, minId, limit);
            if (callRecords.size() <= 0) {
                mark = Boolean.FALSE;
                continue;
            }
            List<Long> ids = callRecords.stream().map(CallRecord::getId).collect(Collectors.toList());
            // todo 插入日志表


            minId = callRecords.get(callRecords.size() - 1).getId();
            String requestId = apiCode + System.currentTimeMillis() + UUID.randomUUID();

            // 封装明细数据入上传 部分入线索
            threadPool.submit(() -> {
                try {
                    MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
                    userDTO.setTaskId(apiCode + "_" + LocalDate.now());
                    userDTO.setRequestId(requestId);
                    List<MarketingPreUserDetailDTO> dataItems = Lists.newArrayList();
                    List<CarClueInfo> carClueInfos = Lists.newArrayList();

                    for (CallRecord callRecord : callRecords) {
                            String intentionGrade = callRecord.getIntentionGrade();
                            MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
                            marketingPreUserDetailDTO.setCell(callRecord.getCaseNum());
                            marketingPreUserDetailDTO.setCustNum(callRecord.getCaseNum());
                            JSONObject reserveField1 = new JSONObject();
                            reserveField1.put("userType", callRecord.getIntentionGrade());
                            reserveField1.put("recordingPath", callRecord.getRecordingPath());
                            reserveField1.put("intentionGrade", intentionGrade);
                            marketingPreUserDetailDTO.setReserveField1(reserveField1.toJSONString());
                            dataItems.add(marketingPreUserDetailDTO);
                            if (ObjectUtil.isNotEmpty(carClueIntentionGrades) && carClueIntentionGrades.contains(intentionGrade)) {
                                CarClueInfo carClueInfo = new CarClueInfo();
                                carClueInfo.setCid(cid);
                                carClueInfo.setApiCode(apiCode);
                                carClueInfo.setCustNum(callRecord.getCaseNum());
                                carClueInfo.setCell(callRecord.getCaseNum());
                                carClueInfo.setIntention(intentionGrade);
                                carClueInfo.setRecordingpath(callRecord.getRecordingPath());
                                carClueInfo.setCreateTime(new Date());
                                carClueInfo.setCleanTime(new Date());
                                carClueInfos.add(carClueInfo);
                            }
                    }
                    userDTO.setDataItems(dataItems);
                    UploadDataDTO uploadDataDTO = new UploadDataDTO();
                    uploadDataDTO.setApiCode(apiCode);
                    uploadDataDTO.setJsonData(JSONObject.toJSONString(userDTO));
                    // 所有数据入上传
                    pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                    // todo 高意向入线索表
                    carClueInfoMapper.batchInsert(carClueInfos);
                    // todo 更新日志表状态

                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                            "车线索数据清洗，子线程处理异常"), e);
                }
            });
        }
        shutDownThreadPool(threadPool);
    }

    public void shutDownThreadPool(ThreadPoolExecutor threadPool) {
        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("车线索数据清洗 等待线程池结束");
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            log.error("车线索数据清洗 线程池关闭异常,直接关闭线程池", e);
        }
    }
}
