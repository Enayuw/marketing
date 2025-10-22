package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dewu.DewuClient;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.dewu.DewuPushQueryQuantityDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.DewuCollidingDataLogMapper;
import com.br.marketing.mapper.DewuCollidingDataMapper;
import com.br.marketing.mapper.DewuCollidingDataUploadSyncMapper;
import com.br.marketing.service.DewuCollidingDataService;
import com.br.marketing.service.LocalFileService;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DewuCollidingDataServiceImpl implements DewuCollidingDataService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DewuCollidingDataLogMapper dewuCollidingDataLogMapper;

    @Resource
    private DewuCollidingDataMapper dewuCollidingDataMapper;

    @Resource
    private DewuCollidingDataUploadSyncMapper dewuCollidingDataUploadSyncMapper;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private DewuClient dewuClient;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    LocalFileService localFileService;

    @Override
    public void collidingDataProcess(Long localFileId) {
        // 创建撞库线程池
        ThreadPoolExecutor deWuCollidingThread =
                BrExecutors.getThreadPool(marketingCommonConfig.getDeWuCollidingThread(), marketingCommonConfig.getDeWuCollidingThread());

        boolean hasCollectedDate = false;
        Date pushStartTime = new Date();
        while (marketingCommonConfig.getDeWuCollidingSwitch()) {
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(deWuCollidingThread, marketingCommonConfig.getDeWuCollidingThread());

            // 查询撞库结果返回  status = 1  的量级
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            DewuCollidingDataUploadSyncExample dcuse = new DewuCollidingDataUploadSyncExample();
            dcuse.createCriteria().andCreateDateEqualTo(Integer.valueOf(currentDate)).andIsDeletedEqualTo(0);
            int todayUploadCount = dewuCollidingDataUploadSyncMapper.countByExample(dcuse);
            // 大于撞得量级 停止撞库
            if (todayUploadCount >= marketingCommonConfig.getDeWuCollidingStopCount()) {
                break;
            }
            List<DewuCollidingData> dewuCollidingDataList = getDewuCollidingDataList(localFileId, todayUploadCount);
            if (dewuCollidingDataList.size() == 0) {
                break;
            }
            hasCollectedDate = true;
            // 主要是为了判断异步执行是否完成（返回结果目前并没有实际意义）
            List<Future<String>> futureList = new ArrayList<>();
            List<List<DewuCollidingData>> dewuCollidingDataListPartition = Lists.partition(dewuCollidingDataList, 200);
            dewuCollidingDataListPartition.forEach((List<DewuCollidingData> p) -> {
                Future<String> submit = deWuCollidingThread.submit(() -> pushDewuCollidingData(p, localFileId));
                futureList.add(submit);
            });
            // 等待上面执行结束，确保下次循环开始时能正常查询已经撞得数据量
            for (int i = 0; i < futureList.size(); i++) {
                Future<String> stringFuture = futureList.get(i);
                try {
                    stringFuture.get(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.warn("InterruptedException:",e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    log.warn("InterruptedException:",e);
                } catch (TimeoutException e) {
                    log.warn("TimeoutException:",e);
                }
            }
        }
        Date pushEndTime = new Date();
        deWuCollidingThread.shutdown();
        try {
            while (!deWuCollidingThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("得物撞库线程池关闭");
            }
        } catch (InterruptedException ex) {
            deWuCollidingThread.shutdownNow();
            log.error("得物撞库线程池关闭异常！", ex);
            Thread.currentThread().interrupt();
        }

        // refreshLocalFile
        if(hasCollectedDate) {
            refreshLocalFile(localFileId, pushStartTime, pushEndTime);
        }
    }

    @Override
    public void collidingDataUploadSyncProcess() {
        ThreadPoolExecutor deWuCollidingDataUploadSyncThread =
                BrExecutors.getThreadPool(marketingCommonConfig.getDeWuCollidingDataUploadSyncThread()
                        , marketingCommonConfig.getDeWuCollidingDataUploadSyncThread());

        while (true) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            DewuCollidingDataUploadSyncExample dcuse = new DewuCollidingDataUploadSyncExample();
            dcuse.createCriteria().andCreateDateEqualTo(Integer.valueOf(yyyyMMdd)).andIsDeletedEqualTo(0)
                    .andPushStatusEqualTo(0);
            dcuse.setOrderByClause("id asc limit 2000");
            List<DewuCollidingDataUploadSync> dewuCollidingDataUploadSyncList = dewuCollidingDataUploadSyncMapper.selectByExample(dcuse);
            if (dewuCollidingDataUploadSyncList.size() == 0) {
                break;
            }
            List<Long> ids = dewuCollidingDataUploadSyncList.stream().map(DewuCollidingDataUploadSync::getId).collect(Collectors.toList());
            dewuCollidingDataUploadSyncMapper.updateBatchById(ids, 1);
            deWuCollidingDataUploadSyncThread.execute(() -> pushCollidingDataUploadSync(dewuCollidingDataUploadSyncList));
        }
        deWuCollidingDataUploadSyncThread.shutdown();
        try {
            while (!deWuCollidingDataUploadSyncThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("得物推送上传api接口线程池关闭");
            }
        } catch (InterruptedException ex) {
            deWuCollidingDataUploadSyncThread.shutdownNow();
            log.error("得物推送上传api接口线程池关闭！异常", ex);
            Thread.currentThread().interrupt();
        }
    }

    private void pushCollidingDataUploadSync(List<DewuCollidingDataUploadSync> dewuCollidingDataUploadSyncList) {
        Integer sendDate = Integer.valueOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(marketingCommonConfig.getDeWuCollidingAiCode() + "_" + sendDate);
        marketingPreUserDTO.setRequestId(marketingPreUserDTO.getTaskId().concat("_").concat(UUID.randomUUID().toString()));
        List<MarketingPreUserDetailDTO> dataItems = buildMarketingPreUserDetails(dewuCollidingDataUploadSyncList);
        marketingPreUserDTO.setDataItems(dataItems);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(marketingCommonConfig.getDeWuCollidingAiCode());
        uploadDataDTO.setJsonData(JSONObject.toJSONString(marketingPreUserDTO));
        Result result = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        List<Long> ids = dewuCollidingDataUploadSyncList.stream().map(DewuCollidingDataUploadSync::getId).collect(Collectors.toList());
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            dewuCollidingDataUploadSyncMapper.updateBatchById(ids, 2);
        } else {
            dewuCollidingDataUploadSyncMapper.updateBatchById(ids, 3);
        }
    }

    private static List<MarketingPreUserDetailDTO> buildMarketingPreUserDetails(List<DewuCollidingDataUploadSync> dewuCollidingDataUploadSyncList) {
        List<MarketingPreUserDetailDTO> dataItems = Lists.newArrayList();
        for (DewuCollidingDataUploadSync data : dewuCollidingDataUploadSyncList) {
            MarketingPreUserDetailDTO dto = new MarketingPreUserDetailDTO();
            dto.setCell(data.getMobile());
            dto.setCustNum(data.getUserId());
            JSONObject reserveField1 = new JSONObject();
            reserveField1.put("userType", 1);
            dto.setReserveField1(reserveField1.toJSONString());
            dataItems.add(dto);
        }
        return dataItems;
    }

    private List<DewuCollidingData> getDewuCollidingDataList(Long localFileId, int todayUploadCount) {
        // 创建limit 量级
        int limitCount = marketingCommonConfig.getDeWuCollidingLimit();
        // 判断是否接近停止撞库量级
        // 大于等于 则
        if (todayUploadCount >= marketingCommonConfig.getDeWuCollidingStopThresholdCount()) {
            limitCount = marketingCommonConfig.getDeWuCollidingStopCount() - todayUploadCount;
        }
        DewuCollidingDataExample dce = new DewuCollidingDataExample();
        dce.createCriteria()
                .andStatusEqualTo(1)
                .andPushStatusEqualTo(0)
                .andIsDeletedEqualTo(0)
                .andLocalIdEqualTo(localFileId);
        dce.setOrderByClause("id desc limit " + limitCount);
        // 查询待推送量级
        List<DewuCollidingData> dewuCollidingDataList = dewuCollidingDataMapper.selectByExample(dce);
        return dewuCollidingDataList;
    }

    public String pushDewuCollidingData(List<DewuCollidingData> dewuCollidingDataList, Long localFileId) {
        try {
            List<DewuCollidingData> collidingDataMobileList = pushCollidingDataProcessBefore(dewuCollidingDataList);
            if (collidingDataMobileList.size() > 0) {
                List<String> collidingMobileList = collidingDataMobileList.stream().map(DewuCollidingData::getMobile).collect(Collectors.toList());
                Result result = dewuClient.pushCollidingData(collidingMobileList);
                pushCollidingDataProcessAfter(localFileId, result, collidingDataMobileList);
            }
        }catch (Exception e){
            log.error("得物撞库异常",e);
        }
        return "";
    }

    private void pushCollidingDataProcessAfter(Long localFileId, Result result, List<DewuCollidingData> collidingDataMobileList) {
        try {
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            List<Long> ids = collidingDataMobileList.stream().map(DewuCollidingData::getId).collect(Collectors.toList());
            if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
                JSONObject resultJson = JSONObject.parseObject(result.getData().toString());
                JSONArray returnMobileDataList = resultJson.getJSONArray("data");

                List<DewuCollidingDataLog> insertDewuCollidingDataLogList = new ArrayList<>();
                List<DewuCollidingDataUploadSync> insertDewuCollidingDataUploadSyncList = new ArrayList<>();
                for (int i = 0; i < returnMobileDataList.size(); i++) {
                    JSONObject returnMobileDataJson = returnMobileDataList.getJSONObject(i);
                    String status = returnMobileDataJson.getString("status");
                    String mobile = returnMobileDataJson.getString("mobile");

                    // 若果返回的status =1 根据返回的mobile  从推送集合中匹配数据。
                    DewuCollidingData dewuCollidingData = collidingDataMobileList.stream().
                            filter((DewuCollidingData de) -> de.getMobile().equals(mobile)).findAny().orElse(null);
                    if (dewuCollidingData == null) {
                        log.error("得物撞库返回结果");
                        continue;
                    }
                    // 组装日志
                    DewuCollidingDataLog dewuCollidingDataLog = new DewuCollidingDataLog();
                    dewuCollidingDataLog.setApiCode(marketingCommonConfig.getDeWuCollidingAiCode());
                    dewuCollidingDataLog.setMobile(mobile);
                    dewuCollidingDataLog.setLocalId(localFileId);
                    dewuCollidingDataLog.setReturnStatus(status);



                    if (status.equals("1")) {
                        // 组装日志
                        String userId = returnMobileDataJson.getString("userId");
                        dewuCollidingDataLog.setUserId(userId);


                        // 组装待上传表数据
                        DewuCollidingDataUploadSync dewuCollidingDataUploadSync = new DewuCollidingDataUploadSync();
                        dewuCollidingDataUploadSync.setCreateDate(Integer.valueOf(currentDate));
                        dewuCollidingDataUploadSync.setMobile(mobile);
                        dewuCollidingDataUploadSync.setPushStatus(0);
                        dewuCollidingDataUploadSync.setUserId(userId);
                        dewuCollidingDataUploadSync.setApiCode(marketingCommonConfig.getDeWuCollidingAiCode());
                        dewuCollidingDataUploadSync.setLocalId(localFileId);
                        dewuCollidingDataUploadSync.setCreateDate(Integer.valueOf(currentDate));
                        insertDewuCollidingDataUploadSyncList.add(dewuCollidingDataUploadSync);
                    }
                    insertDewuCollidingDataLogList.add(dewuCollidingDataLog);
                }
                // 更新data 状态
                dewuCollidingDataMapper.updateBatchById(ids, 2,Integer.valueOf(currentDate));
                // 保存日志
                dewuCollidingDataLogMapper.saveBatch(insertDewuCollidingDataLogList);
                // 保存待上传记录
                if(insertDewuCollidingDataUploadSyncList.size()>0){
                    dewuCollidingDataUploadSyncMapper.saveBatch(insertDewuCollidingDataUploadSyncList);
                }
            } else {
                // 返回异常  更新 data 表push_status  =3
                log.error("得物撞库接口重试3次失败异常:{}", result.getData());
                dewuCollidingDataMapper.updateBatchById(ids, 3,Integer.valueOf(currentDate));
            }
        }catch (Exception e){
            log.error("得物撞库结果处理异常,{}",collidingDataMobileList,e);
        }

    }

    private List<DewuCollidingData> pushCollidingDataProcessBefore(List<DewuCollidingData> dewuCollidingDataList) {
        List<DewuCollidingData> collidingDataMobileList = new ArrayList<>();
        try {
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            dewuCollidingDataList.forEach((DewuCollidingData dewuCollidingData) -> {
                String mobile = dewuCollidingData.getMobile();
                String key = RedisKeyConstant.PUSH_DEWU_COLLIDING_DATA_LOCK.concat(":")
                        .concat(mobile);
                String value = UUID.randomUUID().toString();
                redisChgService.lock(key, value);
                DewuCollidingData dewuCollidingDataUpdatePushStatus = new DewuCollidingData();
                dewuCollidingDataUpdatePushStatus.setId(dewuCollidingData.getId());
                dewuCollidingDataUpdatePushStatus.setPushDate(Integer.valueOf(currentDate));
                dewuCollidingDataUpdatePushStatus.setPushStatus(1);
                int i = dewuCollidingDataMapper.updateByPrimaryKeySelective(dewuCollidingDataUpdatePushStatus);
                if(i>0){
                    DewuCollidingDataExample de = new DewuCollidingDataExample();
                    de.createCriteria().andIsDeletedEqualTo(0)
                            .andPushStatusGreaterThan(0)
                            .andPushDateEqualTo(Integer.valueOf(currentDate))
                            .andIdNotEqualTo(dewuCollidingData.getId())
                            .andMobileEqualTo(mobile);
                    int exitCount = dewuCollidingDataMapper.countByExample(de);
                    // 如果撞过则更新status  = 2 ,
                    if (exitCount > 0) {
                        dewuCollidingDataUpdatePushStatus.setStatus(2);
                        dewuCollidingDataUpdatePushStatus.setPushStatus(3);
                        dewuCollidingDataMapper.updateByPrimaryKeySelective(dewuCollidingDataUpdatePushStatus);
                    } else {
                        collidingDataMobileList.add(dewuCollidingData);
                    }
                }
            redisChgService.unlock(key, value);
            });
        }catch (Exception e){
            log.error("得物撞库程序前置处理异常,{}",dewuCollidingDataList,e);
        }
        return collidingDataMobileList;
    }

    /**
     * 已确认，每个localId每天只执行1次，刷新逻辑为直接更新PushNumber字段
     * 后续业务有变更，需要更新此方法
     */
    private void refreshLocalFile(Long localFileId, Date pushStartTime, Date pushEndTime){
        try {
            DewuPushQueryQuantityDTO params = new DewuPushQueryQuantityDTO();
            params.setLocalId(localFileId);
            params.setPushStatus(2);
            String curTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            int pushDate = Integer.parseInt(curTimeStr);
            params.setStartTime(pushDate);

            List<Map<String, Object>> queryQuantityList = dewuCollidingDataMapper.queryQuantityGroupByLocalId(params);
            List<Map<String, Object>> quantityList = new ArrayList<>();
            if(queryQuantityList == null || queryQuantityList.size() < 1
                    || "0".equals(String.valueOf(queryQuantityList.get(0).get("quantity")))) {
                Map<String, Object> map = new HashMap<>();
                map.put("localId", localFileId);
                map.put("quantity", 0L);
                quantityList.add(map);
            }else{
                quantityList.add(queryQuantityList.get(0));
            }
            localFileService.refreshPushNumber(quantityList, pushStartTime, pushEndTime);
        }catch (Exception e){
            log.warn("更新推送量级异常", e);
        }
    }
}
