package com.br.marketing.service.carclue.todb.impl;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.mapper.CallRecordLogMapper;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.mapper.CarClueManageConfigMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.carclue.clueenums.CarClueCompleteStatusEnum;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.service.carclue.todb.CarCluesDataToDBService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.msgtype.At;
import com.br.marketing.webhook.dingding.msgtype.DingDingTextMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
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
    private CarClueManageConfigMapper carClueManageConfigMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    @Override
    public void cleanCallDetailsData(List<String> apiCodes, String date) {
        if (ObjectUtil.isEmpty(apiCodes)) {
            return;
        }
        apiCodes.stream().forEach((String apiCode) -> clean(apiCode, date));
    }

    public void clean(String apiCode, String date) {
        String cid = tableCreateService.getCId(apiCode);
        if (StringUtils.isEmpty(cid)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "车线索数据入库异常：获取cid失败，apiCode：" + apiCode));
            return;
        }

        // 查询意向等级配置
        CarClueManageConfigExample example = new CarClueManageConfigExample();
        example.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<CarClueManageConfig> configs = carClueManageConfigMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(configs)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "车线索数据入库异常：渠道商配置为空！"));
            return;
        }

        // 解析意向配置
        String intentionConfig = configs.get(0).getIntentionConfig();
        if (StringUtils.isEmpty(intentionConfig)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "车线索数据入库异常：意向配置为空！"));
            return;
        }

        JSONObject intentionConfigObj = JSONObject.parseObject(intentionConfig);
        Map<String, String> storageConfig = marketingCommonConfig.getCarClueStorageConfig();

        // 查找匹配apiCode下的意向等级
        Optional<String> intentionOpt = storageConfig.entrySet().stream()
                .filter(entry -> apiCode.equals(entry.getValue()))
                .map(entry -> intentionConfigObj.getString(entry.getKey()))
                .filter(StringUtils::isNotEmpty)
                .findFirst();

        if (!intentionOpt.isPresent()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "车线索数据入库异常：未配置意向等级，apiCode：" + apiCode));
            return;
        }

        // 分割意向等级
        List<String> carClueIntentionGrades = Arrays.stream(intentionOpt.get().split(","))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());

        JSONObject carClueDataCleanConfig = marketingCommonConfig.getCarClueDataCleanConfig();
        Integer limit = carClueDataCleanConfig.getInteger("limit");
        Integer threadNum = carClueDataCleanConfig.getInteger("threadNum");
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum,
                "CAR_CLUE_DATA_CLEAN_THREAD_POOL", 200);
        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.CARCLUES_ERROR_MESSAGE.toString());
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

                minId = callRecords.get(callRecords.size() - 1).getId();
                String requestId = apiCode + System.currentTimeMillis() + UUID.randomUUID();
                String taskId = apiCode + "_" + LocalDate.now();
                threadPool.submit(() -> {
                    try {
                        callRecordLogMapper.batchInsert(callRecords);
                        MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
                        userDTO.setTaskId(taskId);
                        userDTO.setRequestId(requestId);
                        List<MarketingPreUserDetailDTO> dataItems = Lists.newArrayList();
                        List<CarClueInfo> carClueInfos = Lists.newArrayList();
                        List<CallRecordLog> successRecords = Lists.newArrayList();
                        List<CallRecordLog> failRecords = Lists.newArrayList();

                        for (CallRecord callRecord : callRecords) {
                            CallRecordLog callRecordLog = new CallRecordLog();
                            callRecordLog.setId(callRecord.getId());
                            try {
                                String userProperties = callRecord.getUserProperties();
                                JSONObject jsonObject = JSON.parseObject(userProperties);
                                String phone = getPhoneFromJsonObject(jsonObject, "phone");
                                if (ObjectUtil.isEmpty(jsonObject) || ObjectUtil.isEmpty(phone)) {
                                    callRecordLog.setErrorMessage("通话明细用户信息或手机号为空！");
                                    failRecords.add(callRecordLog);
                                    // 钉钉报警
                                    String content =
                                            ("车线索入库异常 " + LocalDate.now() + "\n通话明细id      异常原因\n"
                                                    .concat(callRecordLog.getId().toString())
                                                    .concat("      " + ("通话明细用户信息或手机号为空!"))
                                                    .concat("\n"));
                                    sendDingDingTextMessage(content, map);
                                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                                            "车线索数据入库异常：通话明细用户信息或手机号为空！"));
                                    continue;
                                }
                                String carBrand = getPhoneFromJsonObject(jsonObject, "brandName");
                                String carSeries = getPhoneFromJsonObject(jsonObject, "seriesName");
                                String province = getPhoneFromJsonObject(jsonObject, "province");
                                String city = getPhoneFromJsonObject(jsonObject, "city");
                                String resourceType = getPhoneFromJsonObject(jsonObject, "resourceType");
                                String firstName = getFirstName(jsonObject, resourceType);
                                String gender = getGenderTitle(jsonObject, resourceType);
                                String intentionGrade = ObjectUtil.isNotEmpty(callRecord.getIntentionGrade()) ?
                                        callRecord.getIntentionGrade() : "";
                                if (ObjectUtil.isNotEmpty(carClueIntentionGrades) && carClueIntentionGrades.contains(intentionGrade)) {
                                    CarClueInfo carClueInfo = new CarClueInfo();
                                    carClueInfo.setCid(cid);
                                    carClueInfo.setApiCode(apiCode);
                                    carClueInfo.setCustNum(callRecord.getCaseNum());
                                    carClueInfo.setCell(phone);
                                    carClueInfo.setIntention(intentionGrade.toUpperCase());
                                    carClueInfo.setBrand(carBrand);
                                    carClueInfo.setMember(firstName + gender);
                                    carClueInfo.setSeries(carSeries);
                                    carClueInfo.setProvince(province);
                                    carClueInfo.setCity(city);
                                    carClueInfo.setRecordingPath(callRecord.getRecordingPath());
                                    carClueInfo.setCallDialog(callRecord.getCallDialog());
                                    carClueInfo.setClueDataStatus(CarClueDataStatusEnum.READY.getValue());
                                    carClueInfo.setClueCompleteStatus(CarClueCompleteStatusEnum.NORMAL_COMPLETE.getValue());
                                    carClueInfo.setResourceType(resourceType);
                                    carClueInfo.setCallId(callRecord.getSessionId());
                                    carClueInfo.setCreateTime(new Date());
                                    carClueInfo.setUpdateTime(new Date());
                                    carClueInfos.add(carClueInfo);
                                }

                                MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
                                marketingPreUserDetailDTO.setCell(phone);
                                marketingPreUserDetailDTO.setCustNum(callRecord.getCaseNum());
                                JSONObject reserveField1 = new JSONObject();
                                reserveField1.put("userType", "新车");
                                reserveField1.put("recordingPath", callRecord.getRecordingPath());
                                reserveField1.put("intentionGrade", intentionGrade.toUpperCase());
                                reserveField1.put("brand", carBrand);
                                reserveField1.put("series", carSeries);
                                reserveField1.put("province", province);
                                reserveField1.put("city", city);
                                reserveField1.put("firstName", firstName);
                                reserveField1.put("gender", gender);
                                reserveField1.put("member", (firstName + gender));
                                reserveField1.put("resourceType", resourceType);
                                reserveField1.put("callId", callRecord.getSessionId());
                                marketingPreUserDetailDTO.setReserveField1(reserveField1.toJSONString());
                                dataItems.add(marketingPreUserDetailDTO);
                                successRecords.add(callRecordLog);
                            } catch (Exception e) {
                                callRecordLog.setErrorMessage("通话明细组装过程异常！");
                                failRecords.add(callRecordLog);
                                // 钉钉报警
                                String content =
                                        ("车线索入库异常 " + LocalDate.now() + "\n通话明细id      异常原因\n"
                                                .concat(callRecordLog.getId().toString())
                                                .concat("      " + ("通话明细组装过程异常!"))
                                                .concat("\n"));
                                sendDingDingTextMessage(content, map);
                                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                                        "车线索数据入库异常！异常信息：" + e.getMessage()), e);
                            }
                        }
                        userDTO.setDataItems(dataItems);
                        UploadDataDTO uploadDataDTO = new UploadDataDTO();
                        uploadDataDTO.setApiCode(apiCode);
                        uploadDataDTO.setJsonData(JSONObject.toJSONString(userDTO));
                        pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                        if (ObjectUtil.isNotEmpty(carClueInfos)) {
                            carClueInfoMapper.batchInsert(carClueInfos);
                        }
                        updateCallRecordLogStatus(successRecords, 2);
                        updateCallRecordLogStatus(failRecords, 3);
                    } catch (Exception e) {
                        List<Long> recordIds = callRecords.stream().map(CallRecord::getId).collect(Collectors.toList());
                        List<String> recordIdsStrList = recordIds.stream().map(String::valueOf).collect(Collectors.toList());
                        // 钉钉报警
                        String content =
                                ("本批车线索数据入库异常！当前错误数据ids: " + String.join(", ", recordIdsStrList));
                        sendDingDingTextMessage(content, map);
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                                "车线索数据入库异常！当前错误数据ids: " + String.join(", ", recordIdsStrList)), e);
                    }
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

    public void updateCallRecordLogStatus(List<CallRecordLog> recordLogs, int status) {
        if (ObjectUtil.isEmpty(recordLogs)) {
            return;
        }
        CallRecordLog callRecordLog = new CallRecordLog();
        callRecordLog.setInboundStatus(status);
        if (status == 2) {
            List<Long> recordIds = recordLogs.stream().map(CallRecordLog::getId).collect(Collectors.toList());
            CallRecordLogExample callRecordLogExample = new CallRecordLogExample();
            callRecordLogExample.createCriteria().andRecordIdIn(recordIds);

            callRecordLogMapper.updateByExampleSelective(callRecordLog, callRecordLogExample);
        } else if (status == 3) {
            for (CallRecordLog log : recordLogs) {
                callRecordLog.setErrorMessage(log.getErrorMessage());
                CallRecordLogExample example = new CallRecordLogExample();
                example.createCriteria().andRecordIdEqualTo(log.getId());
                callRecordLogMapper.updateByExampleSelective(callRecordLog, example);
            }
        }

    }

    /**
     * 2024-03-05 17:47
     * 发送钉钉文本消息
     */
    private void sendDingDingTextMessage(String content, Map<String, Object> sendMgsInfoMap) {
        try {
            DingDingTextMessage dingDingTextMessage = new DingDingTextMessage();
            DingDingTextMessage.Text text = new DingDingTextMessage.Text();
            dingDingTextMessage.setText(text);
            JSONArray ats = (JSONArray) sendMgsInfoMap.get("at");
            if (ats != null) {
                At at = new At();
                at.setAtMobiles(ats.toJavaList(String.class));
                dingDingTextMessage.setAt(at);
            }
            text.setContent(content);
            log.warn(dingDingTextMessage.toString());
            // 发送实时消息
            dingDingRobotHookService.sendMessageGroup(sendMgsInfoMap.get("token").toString()
                    , sendMgsInfoMap.get("secret").toString()
                    , dingDingTextMessage);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "车线索数据入库机器人告警异常！"), e);
        }
    }

    public String getFirstName(JSONObject jsonObject, String resourceType) {
        try {
            JSONObject config = marketingCommonConfig.getCarClueDataMemberConfig();
            JSONObject genderKeys = config.getJSONObject("firstNameKeys");

            JSONArray genderKeyArray = genderKeys.getJSONArray(resourceType);
            if (ObjectUtil.isEmpty(genderKeyArray)) {
                return "";
            }


            for (int i = 0; i < genderKeyArray.size(); i++) {
                String genderKey = genderKeyArray.getString(i);
                String genderValue = jsonObject.getString(genderKey);

                if (ObjectUtil.isNotEmpty(genderValue)) {
                    return genderValue;
                }
            }

            return "";
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "车线索数据姓名转化入库异常！异常信息：" + e.getMessage()), e);
            return "";
        }
    }

    public String getGenderTitle(JSONObject jsonObject, String resourceType) {
        try {
            JSONObject config = marketingCommonConfig.getCarClueDataMemberConfig();
            JSONObject genderKeys = config.getJSONObject("genderKeys");

            JSONArray genderKeyArray = genderKeys.getJSONArray(resourceType);
            if (ObjectUtil.isEmpty(genderKeyArray)) {
                return "";
            }

            JSONArray maleTitles = config.getJSONArray("先生");
            JSONArray femaleTitles = config.getJSONArray("女士");

            for (int i = 0; i < genderKeyArray.size(); i++) {
                String genderKey = genderKeyArray.getString(i);
                String genderValue = jsonObject.getString(genderKey);

                if (ObjectUtil.isNotEmpty(genderValue)) {
                    if (maleTitles.contains(genderValue)) {
                        return "先生";
                    } else if (femaleTitles.contains(genderValue)) {
                        return "女士";
                    }
                }
            }

            return "";
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "车线索数据性别转化入库异常！异常信息：" + e.getMessage()), e);
            return "";
        }
    }
}
