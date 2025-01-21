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
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.service.clean.CarClue.CarCluesDataToDBService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        Map<String, List<String>> carClueStorageConfig = marketingCommonConfig.getCarClueStorageConfig();
        List<String> carClueIntentionGrades = carClueStorageConfig.get("carClueIntentionGrades");
        JSONObject carClueDataCleanConfig = marketingCommonConfig.getCarClueDataCleanConfig();
        Integer limit = carClueDataCleanConfig.getInteger("limit");
        Integer threadNum = carClueDataCleanConfig.getInteger("threadNum");
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum,
                "CAR_CLUE_DATA_CLEAN_THREAD_POOL", 200);
        boolean mark = Boolean.TRUE;
        try {
            Long minId = callRecordMapper.cleanDataOfMinId(apiCode, date);
            if (minId == null) {
                return;
            }
            minId = minId - 1;
            while (mark) {
                List<CallRecord> callRecords = callRecordMapper.cleanDataByMinId(apiCode, date, minId, limit);
                if (callRecords.size() <= 0) {
                    mark = Boolean.FALSE;
                    continue;
                }
                callRecordLogMapper.batchInsert(callRecords);

                minId = callRecords.get(callRecords.size() - 1).getId();
                String requestId = apiCode + System.currentTimeMillis() + UUID.randomUUID();
                String taskId = apiCode + "_" + LocalDate.now();
                threadPool.submit(() -> {
                    MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
                    userDTO.setTaskId(taskId);
                    userDTO.setRequestId(requestId);
                    List<MarketingPreUserDetailDTO> dataItems = Lists.newArrayList();
                    List<CarClueInfo> carClueInfos = Lists.newArrayList();
                    List<Long> successRecordIds = Lists.newArrayList();
                    List<Long> failRecordIds = Lists.newArrayList();

                    for (CallRecord callRecord : callRecords) {
                        String userProperties = callRecord.getUserProperties();
                        JSONObject jsonObject = JSON.parseObject(userProperties);
                        String phone = getPhoneFromJsonObject(jsonObject, "phone");
                        String carBrand = getPhoneFromJsonObject(jsonObject, "carBrand");
                        String carSeries = getPhoneFromJsonObject(jsonObject, "carSeries");
                        String province = getPhoneFromJsonObject(jsonObject, "province");
                        String city = getPhoneFromJsonObject(jsonObject, "city");
                        String member = getPhoneFromJsonObject(jsonObject, "member");
                        String intentionGrade = callRecord.getIntentionGrade();
                        if (ObjectUtil.isNotEmpty(carClueIntentionGrades) && carClueIntentionGrades.contains(intentionGrade)) {
                            CarClueInfo carClueInfo = new CarClueInfo();
                            carClueInfo.setCid(cid);
                            carClueInfo.setApiCode(apiCode);
                            carClueInfo.setCustNum(callRecord.getCaseNum());
                            carClueInfo.setCell(callRecord.getCaseNum());
                            carClueInfo.setIntention(intentionGrade);
                            carClueInfo.setRecordingPath(callRecord.getRecordingPath());
                            carClueInfo.setBrand(carBrand);
                            carClueInfo.setSeries(carSeries);
                            carClueInfo.setCell(phone);
                            carClueInfo.setProvince(province);
                            carClueInfo.setCity(city);
                            carClueInfo.setMember(member);
                            carClueInfo.setClueDataStatus(CarClueDataStatusEnum.READY.getValue());
                            carClueInfo.setCreateTime(new Date());
                            carClueInfo.setUpdateTime(new Date());
                            carClueInfos.add(carClueInfo);
                        }
                        if (ObjectUtil.isEmpty(jsonObject) || ObjectUtil.isEmpty(phone)) {
                            failRecordIds.add(callRecord.getId());
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                                    "车线索数据入库异常：通话明细用户信息或手机号为空！"));
                            continue;
                        }
                        MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
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
                        reserveField1.put("member", member);
                        marketingPreUserDetailDTO.setReserveField1(reserveField1.toJSONString());
                        dataItems.add(marketingPreUserDetailDTO);
                        successRecordIds.add(callRecord.getId());
                    }
                    userDTO.setDataItems(dataItems);
                    UploadDataDTO uploadDataDTO = new UploadDataDTO();
                    uploadDataDTO.setApiCode(apiCode);
                    uploadDataDTO.setJsonData(JSONObject.toJSONString(userDTO));
                    pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                    carClueInfoMapper.batchInsert(carClueInfos);
                    updateCallRecordLogStatus(successRecordIds, 2);
                    updateCallRecordLogStatus(failRecordIds, 3);
                });
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "车线索数据入库异常！异常信息：" + e.getMessage()), e);
        } finally {
            shutDownThreadPool(threadPool);
        }
    }

    public void shutDownThreadPool(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("车线索数据清洗 等待线程池结束");
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "车线索数据清洗 线程池关闭异常,直接关闭线程池！"), e);
        }
    }

    public String getPhoneFromJsonObject(JSONObject jsonObject, String phoneKey) {
        if (jsonObject != null) {
            String phone = jsonObject.getString(phoneKey);
            if (ObjectUtil.isNotEmpty(phone)) {
                return phone;
            }
        }
        return "";
    }

    public void updateCallRecordLogStatus(List<Long> recordIds, int status) {
        if (recordIds == null || recordIds.isEmpty()) {
            return;
        }
        CallRecordLog callRecordLog = new CallRecordLog();
        callRecordLog.setInboundStatus(status);
        CallRecordLogExample callRecordLogExample = new CallRecordLogExample();
        callRecordLogExample.createCriteria().andRecordIdIn(recordIds);
        callRecordLogMapper.updateByExampleSelective(callRecordLog, callRecordLogExample);
    }
}
