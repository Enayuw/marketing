package com.br.marketing.service.rulecenter.impl.push;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.enums.FilterTypeEnum;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.mapper.MarketingRuleCenterLabelReportMapper;
import com.br.marketing.mapper.MarketingSyncLabelMapper;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.ToPolicyByRuleService;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.br.marketing.service.rulecenter.impl.esquery.EsQueryExecutor;
import com.br.marketing.service.rulecenter.impl.esquery.EsQueryResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.br.marketing.service.rulecenter.impl.esquery.EsQueryParams;


import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataLabelPushStrategy extends AbstractRuleCenterPushStrategy {

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    MarketingSyncLabelMapper marketingSyncLabelMapper;


    @Resource
    MarketingRuleCenterLabelReportMapper marketingRuleCenterLabelReportMapper;

    @Resource
    MarketingSyncReportMapper syncReportMapper;

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private ToPolicyByRuleService toPolicyByRuleService;

    @Autowired
    private EsQueryExecutor esQueryExecutor;


    protected Result<Boolean> preProcess(RuleCenterPushContext context) {

        // 补推逻辑
        CustomerInfoPushMain customerInfoPushMain = context.getCustomerInfoPushMain();
        if (PushRuleStatusEnum.EXCEPTIONS_RUNNING.getValue()
                .equals(customerInfoPushMain.getmStatus())) {
            // 是否存在ES重试数据
            int i = retryEsData(customerInfoPushMain);
            //流程结束
            if (i == 0) {
                Integer status = toPolicyByRuleService.queryExistError(customerInfoPushMain.getId(),
                        FilterTypeEnum.GENERAL_POLICY.getValue());
                CustomerInfoPushMain main = new CustomerInfoPushMain();
                main.setId(customerInfoPushMain.getId());
                main.setmStatus(status);
                customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
            }
        }
        MarketingRuleCenterLabelReportExample labelReportExample = new MarketingRuleCenterLabelReportExample();
        labelReportExample.createCriteria().andApiCodeEqualTo(customerInfoPushMain.getmApiCode())
                .andLabelIdEqualTo(customerInfoPushMain.getId())
                .andIsDelEqualTo(1);
        List<MarketingRuleCenterLabelReport> labelReportList = marketingRuleCenterLabelReportMapper.selectByExample(labelReportExample);
        List<Map<String, String>> conditionList = new ArrayList<>();
        labelReportList.forEach(report -> {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("appletDate", report.getAppletDate());
            dataMap.put("userType", report.getUserType());
            conditionList.add(dataMap);
        });

        context.setDataConditionList(conditionList);
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue());
    }


    protected void postProcess(RuleCenterPushContext context, Result<Boolean> result) {

        CustomerInfoPushMain pushMain = context.getCustomerInfoPushMain();

        List<Map<String, Object>> labelNumList = marketingSyncLabelMapper.getLabelNum(pushMain.getId(), pushMain.getmApiCode());

        MarketingRuleCenterLabelReportExample labelReportExample = new MarketingRuleCenterLabelReportExample();
        labelReportExample.createCriteria().andApiCodeEqualTo(pushMain.getmApiCode())
                .andLabelNameEqualTo(pushMain.getLabelName())
                .andIsDelEqualTo(1);
        List<MarketingRuleCenterLabelReport> labelReportList = marketingRuleCenterLabelReportMapper.selectByExample(labelReportExample);
        labelReportList.forEach(labelReport -> {
            String appletDate = labelReport.getAppletDate();
            String userType = labelReport.getUserType();
            Map<String, Object> numMap = labelNumList.stream().filter(labelMap -> appletDate.equals
                    (labelMap.get("applet_date").toString()) && userType.equals
                    (labelMap.get("user_type").toString())).findFirst().orElse(null);
            if (!CollectionUtils.isEmpty(numMap)) {
                labelReport.setNum(Long.parseLong(numMap.get("num").toString()));
                labelReport.setUpdateTime(new Date());
                marketingRuleCenterLabelReportMapper.updateByPrimaryKeySelective(labelReport);
                //更新上传记录表
                MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
                reportExample.createCriteria().andApiCodeEqualTo(pushMain.getmApiCode()).andAppletDateEqualTo(appletDate).andUserTypeEqualTo(userType);
                List<MarketingSyncReport> reportList = syncReportMapper.selectByExample(reportExample);
                if (!CollectionUtils.isEmpty(reportList)) {
                    MarketingSyncReport syncReport = reportList.get(0);
                    String labelMessage = syncReport.getLabelMessage();
                    JSONObject labelJson;
                    if (StringUtils.isEmpty(labelMessage)) {
                        labelJson = new JSONObject();
                    } else {
                        labelJson = JSON.parseObject(labelMessage);
                    }
                    labelJson.put(pushMain.getLabelName(), numMap.get("num"));
                    syncReport.setLabelMessage(labelJson.toJSONString());
                    syncReport.setUpdateTime(new Date());
                    syncReportMapper.updateByPrimaryKeySelective(syncReport);
                }
            }
        });
    }

    @Override
    protected Callable<List<Future<Result<Integer>>>> createPushTask(RuleCenterPushContext context, Integer partitionIndex) {

        return new DataLabelTask(
                context.getPushThreadPool(),
                context.getCustomerInfoPushMain(),
                context.getFileIds(),
                context.getBatchNumbers(),
                partitionIndex.toString(),
                context.getSinglePartition(),
                context.getPartitionDataCount().get(partitionIndex),
                context.getDataConditionList(),
                context.getStraHisFiles()
        );
    }

    @Override
    protected Integer getSuccessStatus(CustomerInfoPushMain customerInfoPushMain) {
        Integer status = toPolicyByRuleService.queryExistError(customerInfoPushMain.getId(),
                FilterTypeEnum.GENERAL_POLICY.getValue());
        if (status.equals(PushRuleStatusEnum.TO_BE_CONFIRMED.getValue())) {
            status = PushRuleStatusEnum.CONFIRMED_SUCCESS.getValue();
        }
        return status;
    }


    /**
     * 推送决策任务实现类 - 完全照搬actionEs类的逻辑
     */
    private class DataLabelTask implements Callable<List<Future<Result<Integer>>>> {

        private ThreadPoolExecutor pushJcPool;
        private CustomerInfoPushMain customerInfoPushMain;
        private List<Long> fileIds;
        private List<String> numList;
        private String part;
        private Boolean isPerOrTop;
        private Integer partDataNum;
        private List<Map<String, String>> conditionList;
        private List<StraHisFile> straHisFiles;


        public DataLabelTask(ThreadPoolExecutor pushJcPool
                , CustomerInfoPushMain customerInfoPushMain
                , List<Long> fileIds, List<String> numList
                , String part, Boolean isPerOrTop, Integer partDataNum, List<Map<String, String>> conditionList,
                List<StraHisFile> straHisFiles) {
            this.pushJcPool = pushJcPool;
            this.customerInfoPushMain = customerInfoPushMain;
            this.fileIds = fileIds;
            this.numList = numList;
            this.part = part;
            this.isPerOrTop = isPerOrTop;
            this.partDataNum = partDataNum;
            this.conditionList = conditionList;
            this.straHisFiles = straHisFiles;
        }

        @Override
        public List<Future<Result<Integer>>> call() {
            String apiCode = customerInfoPushMain.getmApiCode();
            Integer pageSize = 2000;
            Integer total = isPerOrTop ? customerInfoPushMain.getmRealyNum()
                    : partDataNum;
            int totalYuShu = total % pageSize;
            int totalPage = total / pageSize + (totalYuShu > 0 ? 1 : 0);
            log.warn("任务id：{}，当前片：{}，总数：{}，页数：{}"
                    , customerInfoPushMain.getId()
                    , StringUtils.isBlank(part) ? "" : part
                    , total
                    , totalPage);
            List<Future<Result<Integer>>> resList = new ArrayList<>();

            // 使用统一的ES查询参数
            EsQueryParams esParams = createEsQueryParams(customerInfoPushMain, part, numList, fileIds,
                    pageSize, totalPage, isPerOrTop,
                    null, null, straHisFiles);

            //前置处理，es补推时，非异常数据不重复处理
            if (!esQueryExecutor.excuteBefore(esParams)) {
                return resList;
            }

            for (int i = esParams.getStartPageIndex(); i <= totalPage; i++) {
                try {
                    // 执行ES查询
                    EsQueryResult esResult = executeEsQuery(esParams, i);

                    if (!esResult.isSuccess()) {
                        // ES查询失败，直接返回
                        return resList;
                    }

                    List<MarketingHistory> marketingHistories = esResult.getMarketingHistories();

                    Integer realNum = marketingHistories.size();
                    log.warn("任务id：{}，当前片：{}，获取的数量：{}，当前页码：{}"
                            , customerInfoPushMain.getId()
                            , StringUtils.isBlank(part) ? "" : part
                            , realNum
                            , i);
                    if (realNum == 0) {
                        continue;
                    }
                    //创建表
                    tableCreateService.createMarketingUserLabelTable(apiCode);
                    List<String> custNumList = marketingHistories.stream().map(MarketingHistory::getCusNum).collect(Collectors.toList());
                    List<List<String>> partitionList = ListUtils.partition(custNumList, 500);
                    partitionList.forEach(custNums -> {
                                resList.add(pushJcPool.submit(new LabelToDB(custNums, apiCode, conditionList, customerInfoPushMain.getId())));
                            }
                    );
                } catch (Exception ex) {
                    String error = String.format("任务id：%s，当前片：%s，当前页码：%d，异常："
                            , customerInfoPushMain.getId().toString()
                            , StringUtils.isBlank(part) ? "" : part
                            , i);
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), error), ex);
                    Result<Integer> result = new Result<>();
                    result.setCode(ResultCode.FAIL.getValue());
                    Callable<Result<Integer>> resultCallable = (Callable) () -> result;
                    resList.add(pushJcPool.submit(resultCallable));
                }
            }
            return resList;
        }
    }

    class LabelToDB implements Callable<Result<Integer>> {

        private List<String> custNumList;

        private String apiCode;

        private List<Map<String, String>> conditionList;

        private Long labelId;

        public LabelToDB(List<String> custNumList, String apiCode, List<Map<String, String>> conditionList, Long labelId) {
            this.custNumList = custNumList;
            this.apiCode = apiCode;
            this.conditionList = conditionList;
            this.labelId = labelId;
        }

        @Override
        public Result<Integer> call() {
            Result<Integer> result = new Result<>();
            long startTime = System.currentTimeMillis();
            //批量入库
            List<MarketingSyncLabel> marketingSyncLabelList = new ArrayList<>();
            List<MarketingSyncUser> marketingSyncUsers = marketingSyncUserMapper.getUserByCustNumAndAppletData(apiCode, conditionList, custNumList);
            marketingSyncUsers.forEach(syncUser -> {
                MarketingSyncLabel syncLabel = new MarketingSyncLabel();
                BeanUtils.copyProperties(syncUser, syncLabel);
                syncLabel.setLabelId(labelId);
                syncLabel.setSyncId(syncUser.getId());
                syncLabel.setId(null);
                marketingSyncLabelList.add(syncLabel);
            });
            try {
                // 先尝试批量插入（带重试）
                boolean batchSuccess = tryBatchInsert(apiCode, marketingSyncLabelList);

                if (batchSuccess) {
                    // 批量插入成功
                    result.setCode(ResultCode.SUCCESS.getValue());
                } else {
                    // 重复键异常，执行单条插入
                    singleInsertFallback(marketingSyncLabelList, result);
                }
                long totalTime = System.currentTimeMillis() - startTime;
                String insertType = batchSuccess ? "批量插入" : "单条插入";
                log.warn("数据插入处理完成 - 方式: {}, 耗时: {}ms", insertType, totalTime);
            } catch (Exception e) {
                // 批量插入其他异常失败
                result.setCode(ResultCode.FAIL.getValue());
            }

            marketingSyncUsers.clear();
            marketingSyncLabelList.clear();
            return result;
        }

        private boolean tryBatchInsert(String apiCode, List<MarketingSyncLabel> marketingSyncLabelList) {
            int maxRetries = 3;
            int retryCount = 0;

            while (retryCount <= maxRetries) {
                try {
                    marketingSyncLabelMapper.batchInsert(apiCode, marketingSyncLabelList);
                    return true; // 批量插入成功
                } catch (DuplicateKeyException e) {
                    log.warn("规则中心数据打标-批量插入存在重复数据，转为单条插入处理");
                    return false; // 需要单条插入
                } catch (Exception e) {
                    retryCount++;
                    if (retryCount <= maxRetries) {
                        log.warn("规则中心数据打标-批量入库失败，重试次数：{}/{}", retryCount, maxRetries, e);
                    } else {
                        log.error("规则中心数据打标-批量入库重试{}次后仍然失败", maxRetries, e);
                        throw new RuntimeException("批量插入失败", e); // 抛出异常，让主方法统一处理
                    }
                }
            }
            return Boolean.FALSE;
        }

        private void singleInsertFallback(List<MarketingSyncLabel> marketingSyncLabelList, Result<Integer> result) {
            // 默认成功，有错误时设置失败
            result.setCode(ResultCode.SUCCESS.getValue());

            marketingSyncLabelList.forEach(syncLabel -> {
                try {
                    marketingSyncLabelMapper.singleInsert(syncLabel.getApiCode(), syncLabel);
                } catch (DuplicateKeyException e) {
                    log.warn("规则中心数据打标-存在重复数据！sync_id={}", syncLabel.getLabelId());
                } catch (Exception e) {
                    log.error("规则中心数据打标-单条入库失败，sync_id={}", syncLabel.getSyncId(), e);
                    result.setCode(ResultCode.FAIL.getValue());
                }
            });
        }
    }

    @Override
    protected RuleCenterPushContext setThreadPoolNum(RuleCenterPushContext pushContext) {
        Integer getEsNum = marketingCommonConfig.getScoreByEsThreadNum() != null
                && marketingCommonConfig.getScoreByEsThreadNum() > 0
                ? marketingCommonConfig.getScoreByEsThreadNum()
                : 10;

        Integer getJcNum = marketingCommonConfig.getScoreToJcThreadNum() != null
                && marketingCommonConfig.getScoreToJcThreadNum() > 0
                ? marketingCommonConfig.getScoreToJcThreadNum()
                : 2;
        ThreadPoolExecutor actionEs = BrExecutors.getThreadPool(getEsNum, getEsNum, 50);
        ThreadPoolExecutor pushJc = BrExecutors.getThreadPool(getJcNum, getJcNum, 50);
        pushContext.setEsThreadPool(actionEs);
        pushContext.setPushThreadPool(pushJc);
        return pushContext;
    }

}
