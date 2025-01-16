package com.br.marketing.service.clean.CarClue.impl;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.CallRecordLog;
import com.br.marketing.entity.CallRecordLogExample;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.mapper.CallRecordLogMapper;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.CarClue.CarCluesDataToDBService;
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
public class CarCluesDataCleanServiceImpl implements CarCluesDataToDBService {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    private CarClueInfoMapper carClueInfoMapper;

    @Resource
    private CallRecordMapper callRecordMapper;

    @Resource
    private CallRecordLogMapper callRecordLogMapper;

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
            // 插入日志表
            callRecordLogMapper.batchInsert(callRecords);

            minId = callRecords.get(callRecords.size() - 1).getId();
            String requestId = apiCode + System.currentTimeMillis() + UUID.randomUUID();
            String taskId = apiCode + "_" + LocalDate.now();
            // 封装明细数据入上传 部分入线索
            threadPool.submit(() -> {
                try {
                    MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
                    userDTO.setTaskId(taskId);
                    userDTO.setRequestId(requestId);
                    List<MarketingPreUserDetailDTO> dataItems = Lists.newArrayList();
                    List<CarClueInfo> carClueInfos = Lists.newArrayList();
                    List<Long> recordIds = Lists.newArrayList();

                    for (CallRecord callRecord : callRecords) {
                        String userProperties = callRecord.getUserProperties();
                        JSONObject jsonObject = JSON.parseObject(userProperties);
                        if (ObjectUtil.isEmpty(jsonObject) || ObjectUtil.isEmpty(jsonObject.getString("phone"))){
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                                    "车线索数据入库异常：明细用户信息或手机号为空！"));
                            continue;
                        }
                        String phone = ObjectUtil.isNotEmpty(jsonObject.getString("phone")) ? jsonObject.getString("phone") : "";
                        String carBrand = ObjectUtil.isNotEmpty(jsonObject.getString("carBrand")) ? jsonObject.getString("carBrand") : "";
                        String carSeries = ObjectUtil.isNotEmpty(jsonObject.getString("carSeries")) ? jsonObject.getString("carSeries") : "";
                        String province = ObjectUtil.isNotEmpty(jsonObject.getString("province")) ? jsonObject.getString("province") : "";
                        String city = ObjectUtil.isNotEmpty(jsonObject.getString("city")) ? jsonObject.getString("city") : "";
                        String resourceType = ObjectUtil.isNotEmpty(jsonObject.getString("resourceType")) ? jsonObject.getString("resourceType") : "";

                        String intentionGrade = callRecord.getIntentionGrade();
                        MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
                        if (ObjectUtil.isEmpty(phone)){
                            continue;
                        }
                        marketingPreUserDetailDTO.setCell(phone);
                        marketingPreUserDetailDTO.setCustNum(callRecord.getCaseNum());
                        JSONObject reserveField1 = new JSONObject();
                        reserveField1.put("userType", "新车");
                        reserveField1.put("recordingPath", callRecord.getRecordingPath());
                        reserveField1.put("intentionGrade", intentionGrade);
                        reserveField1.put("brand", carBrand);
                        reserveField1.put("series", carSeries);
                        reserveField1.put("province", province);
                        reserveField1.put("city", city);
                        reserveField1.put("cluePushChannel", resourceType);
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
                            carClueInfo.setBrand(carBrand);
                            carClueInfo.setSeries(carSeries);
                            carClueInfo.setCell(phone);
                            carClueInfo.setProvince(province);
                            carClueInfo.setCity(city);
                            carClueInfo.setCluePushChannel(resourceType);
                            carClueInfo.setCreateTime(new Date());
                            carClueInfo.setUpdateTime(new Date());
                            carClueInfos.add(carClueInfo);
                        }
                        recordIds.add(callRecord.getId());
                    }
                    userDTO.setDataItems(dataItems);
                    UploadDataDTO uploadDataDTO = new UploadDataDTO();
                    uploadDataDTO.setApiCode(apiCode);
                    uploadDataDTO.setJsonData(JSONObject.toJSONString(userDTO));
                    // 所有数据入上传
                    pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                    // 高意向入线索表
                    carClueInfoMapper.batchInsert(carClueInfos);
                    // 更新日志表状态
                    CallRecordLog callRecordLog = new CallRecordLog();
                    callRecordLog.setInboundStatus(2);
                    CallRecordLogExample callRecordLogExample = new CallRecordLogExample();
                    callRecordLogExample.createCriteria().andRecordIdIn(recordIds);
                    callRecordLogMapper.updateByExample(callRecordLog,callRecordLogExample);
                } catch (Exception e) {
                    // 更新日志表状态
                    CallRecordLog callRecordLog = new CallRecordLog();
                    callRecordLog.setInboundStatus(3);
                    CallRecordLogExample callRecordLogExample = new CallRecordLogExample();
                    callRecordLogExample.createCriteria().andRecordIdIn(ids);
                    callRecordLogMapper.updateByExample(callRecordLog,callRecordLogExample);
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
