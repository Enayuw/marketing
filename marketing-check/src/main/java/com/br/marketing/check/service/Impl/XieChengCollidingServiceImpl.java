package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.service.XieChengCollidingService;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;
import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushBatchExample;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.enums.ThreeKeyEncryptEnum;
import com.br.marketing.enums.ThreeKeyTypeEnum;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EncAndDecUtil;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.br.marketing.util.xiecheng.XieChengEsJsonHandler;
import com.google.api.client.util.Lists;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: lizhen
 * @Time: 2024/04/27 10:06
 * @Description: 携程撞库Service
 */
@Service
@Slf4j
public class XieChengCollidingServiceImpl implements XieChengCollidingService {


    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    private XieChengCollidingDataLoopCycleMapper dataLoopCycleMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Autowired
    MarketingHistoryEsServiceImpl marketingHistoryEsService;

    @Autowired
    PushRuleService pushRuleService;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    /**
     * 携程撞库数据推决策
     * @param id  CustomerInfoPushMain的id
     */
    @Override
    public Result<Boolean> collidingDataPushPolicy(Long id) {

        CustomerInfoPushMain customerInfoPushMain = customerInfoPushMainMapper.selectByPrimaryKey(id);
        CustomerInfoPushBatchExample searchPushBatch = new CustomerInfoPushBatchExample();
        searchPushBatch.createCriteria().andMIdEqualTo(customerInfoPushMain.getId());
        List<CustomerInfoPushBatch> customerInfoPushBatches = customerInfoPushBatchMapper.selectByExample(searchPushBatch);

        List<String> numList = new ArrayList<>();
        List<Long> fileIds = new ArrayList<>();
        for (CustomerInfoPushBatch customerInfoPushBatch : customerInfoPushBatches) {
            numList.add(customerInfoPushBatch.getmBatchNumber());
            fileIds.add(customerInfoPushBatch.getmFileId());
        }
        Result<Integer> integerResult = pushRuleService.checkThreekEnc(fileIds);
        Integer threeEncrypt = integerResult.getData();
        JSONObject jsonRule = JSON.parseObject(customerInfoPushMain.getmRuleCondition());
        Object releaseTime = jsonRule.getJSONArray("data").stream().filter(obj ->
                ((JSONObject) obj).getString("key").equals("release_time")).findAny().orElse(null);
        if (ObjectUtils.isEmpty(releaseTime)) {
            log.error("携程撞库推送决策缺少release_time，请检查");
        }
        JSONObject releaseTimeJson = (JSONObject) releaseTime;
        String condition = EsConditionTransferSqlUtil.assemblefiled(releaseTimeJson.getString("key"), releaseTimeJson.getString("operation"),
                releaseTimeJson.get("value"));
        CustomerInfoPushMain main = new CustomerInfoPushMain();
        main.setmStatus(PushRuleStatusEnum.TO_BE_CONFIRMED.getValue());
        Integer pageSize = 2000;
        Long minId = null;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5, 200);
        List<Future<Result<Integer>>> resList = new ArrayList<>();
        Integer realTotalNum = 0;
        while (true) {
            List<XieChengCollidingDataLoopCycle> list = dataLoopCycleMapper.selectCycleDataByCondition(minId, condition, pageSize);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }
            minId = list.get(list.size() - 1).getId();
            if (marketingCommonConfig.getXieChengCollidingDataPushPolicyThread() != null) {
                threadPool.setCorePoolSize(marketingCommonConfig.getXieChengCollidingDataPushPolicyThread());
                threadPool.setMaximumPoolSize(marketingCommonConfig.getXieChengCollidingDataPushPolicyThread());
                log.warn("携程撞库推送决策线程调整,corePoolSize={},maxPoolSize={}", threadPool.getCorePoolSize(), threadPool.getMaximumPoolSize());
            }
            resList.add(threadPool.submit(() -> pushPolicy(list, numList, fileIds, customerInfoPushMain, threeEncrypt)));
        }
       try {
            for (Future<Result<Integer>> pushFuture : resList) {
                Result<Integer> pushRes = pushFuture.get();
                if (!ResultCode.SUCCESS.getValue().equals(pushRes.getCode())) {
                    main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
                } else {
                    realTotalNum += pushRes.getData();
                }
            }
        } catch (Exception ex) {
            log.error("推送决策 获取线程结果异常" + ex.getMessage(), ex);
            main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
        }
        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库推送决策：线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.error("携程撞库推送决策：日志保存线程池结束异常！", ex);
            Thread.currentThread().interrupt();
        }
        main.setId(customerInfoPushMain.getId());
        customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
        log.warn("携程撞库推送决策完成，推送数据量num={}",realTotalNum);
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }


    private Result<Integer> pushPolicy(List<XieChengCollidingDataLoopCycle> list, List<String> numList, List<Long> fileIds,
                                       CustomerInfoPushMain customerInfoPushMain, Integer threeEncrypt) {
        Result<Integer> result = new Result<>();
        try {
            List<String> cells = list.stream().map(XieChengCollidingDataLoopCycle::getCellSha256CodeList).collect(Collectors.toList());
            List<String> logCells = Lists.newArrayList();
            cells.forEach(cell->{
                logCells.add(EncAndDecUtil.digestToLog(cell, ThreeKeyTypeEnum.CELL, ThreeKeyEncryptEnum.sha256).getData());
            });

            JSONObject jsonRule = JSON.parseObject(customerInfoPushMain.getmRuleCondition());
            //去除result，release_time
            XieChengEsJsonHandler.handlerJson(jsonRule, new XieChengCollidingFilterDTO());
            //添加cell条件
            JSONArray jsonArray = jsonRule.getJSONArray("data");
            JSONObject cellCondition = new JSONObject();
            cellCondition.put("type", "operation");
            cellCondition.put("key", "cell");
            cellCondition.put("operation", "in");
            cellCondition.put("value", logCells);
            jsonArray.add(cellCondition);
            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(customerInfoPushMain.getmApiCode());
            queryBaseBean.setBatchNumbers(Joiner.on(",").join(numList));
            queryBaseBean.setFileIds(Joiner.on(",").join(fileIds));
            queryBaseBean.setJsonData(jsonRule.toString());
            //兼容数据重复的情况
            queryBaseBean.setPageSize(2500);
            List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
            List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
            assmbleUserDetail(marketingHistories, userDetailDTOS, threeEncrypt);
            //推送任务基础信息
            PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
            pushMarketingUserTaskInfoDTO.setMethod("caseAdd");
            pushMarketingUserTaskInfoDTO.setBatchNumber(customerInfoPushMain.getId().toString());
            pushMarketingUserTaskInfoDTO.setAccessNumber(customerInfoPushMain.getId() + "_" + UUID.randomUUID());
            pushMarketingUserTaskInfoDTO.setData(userDetailDTOS);
            pushMarketingUserTaskInfoDTO.setTaskId(customerInfoPushMain.getId().toString());
            //传输参数信息
            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(customerInfoPushMain.getmApiCode());
            pushMarketingUserDTO.setPlatApiCode(customerInfoPushMain.getmApiCode());
            pushMarketingUserDTO.setJsonData(pushMarketingUserTaskInfoDTO);
            result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, customerInfoPushMain.getId(),
                    pushMarketingUserTaskInfoDTO.getAccessNumber(), userDetailDTOS.size());
            if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, customerInfoPushMain.getId(),
                        pushMarketingUserTaskInfoDTO.getAccessNumber(), userDetailDTOS.size());
            }
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                log.error("推送决策重试失败 accessNumber:{}", pushMarketingUserTaskInfoDTO.getAccessNumber());
            }
            result.setDate(userDetailDTOS.size());
        } catch (Exception e) {
            log.error("携程撞库数据推送决策异常", e.getMessage());
        }
        return result;
    }

    private void assmbleUserDetail(List<MarketingHistory> marketingHistories, List<PushMarketingUserDetailDTO> userDetailDTOS, Integer threeEncrypt) {
        for (int k = 0; k < marketingHistories.size(); k++) {
            MarketingHistory marketingHistory = marketingHistories.get(k);
            //人员信息
            PushMarketingUserDetailDTO dto1 = new PushMarketingUserDetailDTO();
            dto1.setCaseNumber(marketingHistory.getCusNum());
            dto1.setPhone(pushRuleService.encrypt3k(threeEncrypt, marketingHistory.getCell()));
            JSONObject varObject = JSON.parseObject(marketingHistory.getReserveField());
            if (varObject == null) {
                varObject = new JSONObject();
            }
            for (MarketingCondition marketingCondition : marketingHistory.getCondition()) {
                if (org.apache.commons.lang3.StringUtils.isNotBlank(marketingCondition.getCode())) {
                    varObject.put(marketingCondition.getFieldKey(), marketingCondition.getDValue());
                } else {
                    varObject.put(marketingCondition.getFieldKey(), marketingCondition.getStrValue());
                }
            }
            varObject.put("custNum", marketingHistory.getCusNum());
            varObject.put("idCard", pushRuleService.encrypt3k(threeEncrypt, marketingHistory.getIdCard()));
            varObject.put("name", pushRuleService.encrypt3k(threeEncrypt, marketingHistory.getName()));
            varObject.put("batchNumber", marketingHistory.getBatchNumber());
            varObject.put("taskId", marketingHistory.getTaskId());
            varObject.put("userType", marketingHistory.getUserType());
            varObject.put("scoreDate", new SimpleDateFormat("yyyy-MM-dd").format(marketingHistory.getRequestTime()));
            dto1.setVariables(varObject);
            userDetailDTOS.add(dto1);
        }
    }
}
