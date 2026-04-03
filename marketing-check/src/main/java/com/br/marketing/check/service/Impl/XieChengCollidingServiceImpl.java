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
import com.br.marketing.entity.*;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.enums.ThreeKeyEncryptEnum;
import com.br.marketing.enums.ThreeKeyTypeEnum;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EncAndDecUtil;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.br.marketing.util.EsNewIndexRuleUtils;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.util.xiecheng.XieChengEsJsonHandler;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Resource
    XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    /**
     * 携程撞库数据推决策
     *
     * @param id CustomerInfoPushMain的id
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
        List<StraHisFile> straHisFilesForIndex;
        if (fileIds.isEmpty()) {
            straHisFilesForIndex = new ArrayList<>();
        } else {
            StraHisFileExample fileExampleForIndex = new StraHisFileExample();
            fileExampleForIndex.createCriteria().andIdIn(fileIds);
            straHisFilesForIndex = straHisFileMapper.selectByExample(fileExampleForIndex);
        }
        Result<Integer> integerResult = pushRuleService.checkThreekEnc(fileIds);
        Integer threeEncrypt = integerResult.getData();
        JSONObject jsonRule = JSON.parseObject(customerInfoPushMain.getmRuleCondition());
        Object releaseTime = jsonRule.getJSONArray("data").stream().filter(obj ->("release_time").equals(
                ((JSONObject) obj).getString("key"))).findAny().orElse(null);
        if (ObjectUtils.isEmpty(releaseTime)) {
            log.error("携程撞库推送决策缺少release_time，请检查");
            return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
        }
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonRule, collidingFilterDTO);
        String condition =XieChengEsJsonHandler.zkTrueCondition(collidingFilterDTO);
        CustomerInfoPushMain main = new CustomerInfoPushMain();
        main.setmStatus(PushRuleStatusEnum.TO_BE_CONFIRMED.getValue());
        Integer pageSize = marketingCommonConfig.getXiechengZkToPlicyPageSize();
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
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, marketingCommonConfig.getXieChengCollidingDataPushPolicyThread());
                log.warn("携程撞库推送决策线程调整,corePoolSize={},maxPoolSize={}", threadPool.getCorePoolSize(), threadPool.getMaximumPoolSize());
            }
            //数据切分，为了兼容跑分文件重复数据，业务侧若保证撞库本次跑分文件不重复，该段逻辑去掉
            List<List<XieChengCollidingDataLoopCycle>> dataLoopCycleLists = Lists.partition(list, 1500);
            dataLoopCycleLists.forEach((List dataLoopCycleList) -> {
                resList.add(threadPool.submit(() -> pushPolicy(dataLoopCycleList, numList, fileIds, customerInfoPushMain, threeEncrypt, straHisFilesForIndex)));
            });
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
        main.setmRealyNum(realTotalNum);
        customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
        log.warn("携程撞库推送决策完成，推送数据量num={}", realTotalNum);
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }


    private Result<Integer> pushPolicy(List<XieChengCollidingDataLoopCycle> list, List<String> numList, List<Long> fileIds,
                                       CustomerInfoPushMain customerInfoPushMain, Integer threeEncrypt,
                                       List<StraHisFile> straHisFilesForIndex) {
        Result<Integer> result = new Result<>();
        try {
            List<String> cells = list.stream().map(XieChengCollidingDataLoopCycle::getCellSha256CodeList).collect(Collectors.toList());
            List<String> logCells = new ArrayList<>();
            //es查询cell为Log加密
            Long encstart=System.currentTimeMillis();
            cells.forEach((String cell) -> {
                logCells.add(EncAndDecUtil.digestToLog(cell, ThreeKeyTypeEnum.CELL, ThreeKeyEncryptEnum.sha256).getData());
            });
            log.warn("【携程撞库推决策解密】，耗时：{}",System.currentTimeMillis()-encstart);
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
            queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(straHisFilesForIndex, marketingCommonConfig));
            //兼容数据重复的情况
            queryBaseBean.setPageSize(2000);
            //根据跑分条件查询ES，符合条件的数据即为要推送数据
            Long queryStart=System.currentTimeMillis();
            List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
            log.warn("【携程撞库推决策查询es】，耗时：{}，量级={}",System.currentTimeMillis()-queryStart,marketingHistories.size());
            if(CollectionUtils.isEmpty(marketingHistories)){
                return result.setCode(ResultCode.SUCCESS.getValue()).setDate(0);
            }
            List<String> sha256Cell = marketingHistories.stream().map(marketingHistory ->
                    pushRuleService.encrypt3k(ScoreThreeKeyEncryptEnum.sha256.getValue(), marketingHistory.getCell())).collect(Collectors.toList());
            XieChengCollidingDataLogExample dataLogExample = new XieChengCollidingDataLogExample();
            XieChengCollidingDataLogExample.Criteria criteria = dataLogExample.createCriteria();
            criteria.andCellSha256CodeListIn(sha256Cell).andCreateTimeGreaterThanOrEqualTo
                    (Date.from(LocalDate.now().minusDays(7).atStartOfDay().atZone( ZoneId.systemDefault()).toInstant()));
            List<XieChengCollidingDataLog> xieChengCollidingDataLogs = xieChengCollidingDataLogMapper.selectByExample(dataLogExample);
            List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
            assmbleUserDetail(marketingHistories, userDetailDTOS, threeEncrypt,xieChengCollidingDataLogs);
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

    private void assmbleUserDetail(List<MarketingHistory> marketingHistories, List<PushMarketingUserDetailDTO> userDetailDTOS, Integer threeEncrypt,
                                   List<XieChengCollidingDataLog> xieChengCollidingDataLogs) {
        Map<String, XieChengCollidingDataLog> dataLogMap =getDataLogGroupByCell(xieChengCollidingDataLogs);
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
            //携程日志扩展字段
            XieChengCollidingDataLog xieChengCollidingDataLog = dataLogMap.get(dto1.getPhone());
            varObject.put("result", xieChengCollidingDataLog.getResult());
            varObject.put("orgChannel", xieChengCollidingDataLog.getOrgChannel());
            varObject.put("mktLevel", xieChengCollidingDataLog.getMktLevel());
            varObject.put("info", xieChengCollidingDataLog.getInfo());
            dto1.setVariables(varObject);
            userDetailDTOS.add(dto1);
        }
    }

    /**
     * 根据cell分组，按create_time最新一条数据去重
     *
     * @param xieChengCollidingDataLogList
     */
    private Map<String, XieChengCollidingDataLog> getDataLogGroupByCell(List<XieChengCollidingDataLog> xieChengCollidingDataLogList) {
        return xieChengCollidingDataLogList.stream().collect(
                Collectors.groupingBy(XieChengCollidingDataLog::getCellSha256CodeList
                        , Collectors.collectingAndThen(
                                Collectors.reducing((v1, v2) ->
                                        v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                , Optional::get)));
    }



}
