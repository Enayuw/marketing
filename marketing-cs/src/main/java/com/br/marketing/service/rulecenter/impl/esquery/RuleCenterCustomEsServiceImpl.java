package com.br.marketing.service.rulecenter.impl.esquery;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.rulecenter.IEsActionService;
import com.br.marketing.service.rulecenter.IRuleCenterCustomEsService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsNewIndexRuleUtils;
import com.br.marketing.util.GeneScriptUtil;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName RuleCenterCustomEsServiceImpl
 * @Author hang.zhou
 * @Date 2025/10/16
 */
@Service
@Slf4j
public class RuleCenterCustomEsServiceImpl implements IRuleCenterCustomEsService {

    @Resource
    private CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    private CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Resource
    private StraHisFileMapper straHisFileMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    private EsQueryExecutor esQueryExecutor;

    @Autowired
    private IEsActionService iEsActionService;

    @Override
    public ApiResult<Map<String, Object>> queryEsData(Long id) {
        long startTime = System.currentTimeMillis();
        ApiResult<Map<String, Object>> apiResult = new ApiResult<>();
        Map<String, Object> resultData = new HashMap<>();

        try {
            // 1. 查询任务信息
            CustomerInfoPushMain customerInfoPushMain = customerInfoPushMainMapper.selectByPrimaryKey(id);
            if (customerInfoPushMain == null) {
                return apiResult.fail("任务不存在");
            }

            // 2. 查询批次信息
            CustomerInfoPushBatchExample searchPushBatch = new CustomerInfoPushBatchExample();
            searchPushBatch.createCriteria().andMIdEqualTo(customerInfoPushMain.getId());
            List<CustomerInfoPushBatch> customerInfoPushBatches = customerInfoPushBatchMapper.selectByExample(searchPushBatch);

            List<String> numList = new ArrayList<>();
            List<Long> fileIds = new ArrayList<>();
            for (CustomerInfoPushBatch customerInfoPushBatch : customerInfoPushBatches) {
                numList.add(customerInfoPushBatch.getmBatchNumber());
                fileIds.add(customerInfoPushBatch.getmFileId());
            }

            // 3. 查询文件信息
            StraHisFileExample fileExample = new StraHisFileExample();
            fileExample.createCriteria().andIdIn(fileIds);
            List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(fileExample);

            // 4. 判断是否单片
            String scoreFileYhTime = marketingCommonConfig.getScoreFileYhTime();
            Date yhTime = null;
            try {
                yhTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(scoreFileYhTime);
            } catch (ParseException e) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), e.getMessage()), e);
            }
            Date yh = yhTime;
            long beforeCount = straHisFiles.stream().filter(t -> t.getCreateTime().compareTo(yh) <= 0).count();
            Optional<StraHisFile> first = straHisFiles.stream().sorted(Comparator.comparing(StraHisFile::getIndexNum).reversed()).findFirst();
            Integer parNum = 0;
            if (first.isPresent()) {
                parNum = first.get().getIndexNum();
            }
            boolean isSingle = (customerInfoPushMain.getmPercentage() != null
                    && customerInfoPushMain.getmPercentage().compareTo(BigDecimal.ZERO) > 0)
                    || (customerInfoPushMain.getmPlanNum() != null && customerInfoPushMain.getmPlanNum() > 0)
                    || beforeCount > 0;
            if (isSingle) {
                parNum = 1;
            }

            // 5. 处理标签条件
            Boolean markWithEsFlag = marketingCommonConfig.getPushPolicyMarkWithEsFlag();
            String scoreCondition = customerInfoPushMain.getmScoreCondition();
            Object lableObject;
            if (StringUtils.isNotEmpty(scoreCondition)) {
                if (Boolean.TRUE.equals(markWithEsFlag)) {
                    lableObject = GeneScriptUtil.esLableScript(scoreCondition);
                } else {
                    lableObject = GeneScriptUtil.getScoreLables(scoreCondition, markWithEsFlag);
                }
            } else {
                lableObject = null;
            }

            // 6. 创建线程池
            int getEsNum = marketingCommonConfig.getScoreByEsThreadNum() != null
                    && marketingCommonConfig.getScoreByEsThreadNum() > 0
                    ? marketingCommonConfig.getScoreByEsThreadNum()
                    : 10;
            if (isSingle) {
                getEsNum = 1;
            }
            ThreadPoolExecutor esThreadPool = BrExecutors.getThreadPool(getEsNum, getEsNum, 50);

            // 7. 统计信息
            AtomicInteger totalQueryCount = new AtomicInteger(0);
            AtomicInteger totalDataCount = new AtomicInteger(0);
            List<Map<String, Object>> partitionDetails = new CopyOnWriteArrayList<>();
            AtomicLong esQueryTotalTime = new AtomicLong(0);

            // 8. 使用多线程并发查询每个分片
            List<Future<Map<String, Object>>> futures = new ArrayList<>();
            for (int i = 0; i < parNum; i++) {
                final Integer partitionIndex = i;
                Future<Map<String, Object>> future = esThreadPool.submit(() ->
                        queryPartitionData(customerInfoPushMain, partitionIndex, numList, fileIds,
                                isSingle, lableObject, markWithEsFlag, straHisFiles)
                );
                futures.add(future);
            }

            // 9. 等待所有任务完成并收集结果
            for (Future<Map<String, Object>> future : futures) {
                try {
                    Map<String, Object> partitionInfo = future.get();
                    partitionDetails.add(partitionInfo);

                    // 累计统计信息
                    if (partitionInfo.containsKey("queriedPages")) {
                        totalQueryCount.addAndGet((Integer) partitionInfo.get("queriedPages"));
                    }
                    if (partitionInfo.containsKey("dataCount")) {
                        totalDataCount.addAndGet((Integer) partitionInfo.get("dataCount"));
                    }
                    if (partitionInfo.containsKey("totalTime")) {
                        esQueryTotalTime.addAndGet((Long) partitionInfo.get("totalTime"));
                    }
                } catch (Exception e) {
                    log.error("获取分片查询结果异常，任务id：{}", customerInfoPushMain.getId(), e);
                    Thread.currentThread().interrupt();
                }
            }

            // 10. 关闭线程池
            shutdownThreadPool(esThreadPool);

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            // 11. 构建返回结果
            int totalQueries = totalQueryCount.get();
            int totalData = totalDataCount.get();
            long esQueryTime = esQueryTotalTime.get();

            resultData.put("taskId", id);
            resultData.put("apiCode", customerInfoPushMain.getmApiCode());
            resultData.put("partitionCount", parNum);
            resultData.put("isSinglePartition", isSingle);
            resultData.put("totalQueryCount", totalQueries);
            resultData.put("totalDataCount", totalData);
            resultData.put("totalTime", totalTime);
            resultData.put("esQueryTotalTime", esQueryTime);
            resultData.put("partitionDetails", partitionDetails);
            resultData.put("threadPoolSize", getEsNum);

            log.warn("任务id：{}，ES查询完成（多线程），总耗时：{}ms，ES查询累计耗时：{}ms，查询次数：{}，数据量：{}，线程数：{}",
                    id, totalTime, esQueryTime, totalQueries, totalData, getEsNum);
            log.warn(resultData.toString());
            return apiResult.success(resultData, "ES查询完成");

        } catch (Exception e) {
            log.error("查询ES数据异常，任务id：" + id, e);
            long endTime = System.currentTimeMillis();
            resultData.put("totalTime", endTime - startTime);
            resultData.put("error", e.getMessage());
            Thread.currentThread().interrupt();
            return apiResult.fail(resultData, "查询异常：" + e.getMessage());
        }
    }

    /**
     * 查询单个分片的数据
     */
    private Map<String, Object> queryPartitionData(CustomerInfoPushMain customerInfoPushMain,
                                                   Integer partitionIndex,
                                                   List<String> numList,
                                                   List<Long> fileIds,
                                                   Boolean isSingle,
                                                   Object lableObject,
                                                   Boolean markWithEsFlag,
                                                   List<StraHisFile> straHisFiles) {
        long partStartTime = System.currentTimeMillis();
        String part = partitionIndex.toString();
        Map<String, Object> partitionInfo = new HashMap<>();
        partitionInfo.put("partition", part);

        try {
            Integer pageSize = 2000;
            Integer partDataNum = isSingle ? customerInfoPushMain.getmRealyNum()
                    : getPartitionDataNum(customerInfoPushMain, part, numList, fileIds, isSingle, straHisFiles);

            Integer total = partDataNum;
            int totalYuShu = total % pageSize;
            int totalPage = total / pageSize + (totalYuShu > 0 ? 1 : 0);

            log.warn("任务id：{}，当前片：{}，总数：{}，页数：{}"
                    , customerInfoPushMain.getId()
                    , part
                    , total
                    , totalPage);

            // 初始化ES查询参数
            EsQueryParams esParams = esQueryExecutor.initializeParams(
                    customerInfoPushMain,
                    part,
                    numList,
                    fileIds,
                    pageSize,
                    totalPage,
                    isSingle,
                    lableObject,
                    markWithEsFlag,
                    straHisFiles
            );

            // 设置自定义索引
            JSONObject policyCustomIndexes = marketingCommonConfig.getPolicyCustomIndexes();
            if (policyCustomIndexes != null) {
                String customIndexes = policyCustomIndexes.getString(customerInfoPushMain.getmApiCode());
                esParams.setCustomIndexes(customIndexes);
            }

            int partDataCount = 0;
            int partQueryCount = 0;
            List<Long> pageQueryTimes = new ArrayList<>();

            // 分页查询ES
            for (int pageNum = esParams.getStartPageIndex(); pageNum <= totalPage; pageNum++) {
                long pageStartTime = System.currentTimeMillis();
                try {
                    // 执行ES查询
                    EsQueryResult esResult = esQueryExecutor.executeQuery(esParams, pageNum);
                    long pageEndTime = System.currentTimeMillis();
                    long pageQueryTime = pageEndTime - pageStartTime;
                    pageQueryTimes.add(pageQueryTime);

                    partQueryCount++;

                    if (esResult.isSuccess()) {
                        List<MarketingHistory> marketingHistories = esResult.getMarketingHistories();
                        int dataSize = marketingHistories != null ? marketingHistories.size() : 0;
                        partDataCount += dataSize;

                        log.warn("任务id：{}，当前片：{}，页码：{}，查询耗时：{}ms，数据量：{}",
                                customerInfoPushMain.getId(), part, pageNum, pageQueryTime, dataSize);
                    } else {
                        log.warn("任务id：{}，当前片：{}，页码：{}，ES查询失败",
                                customerInfoPushMain.getId(), part, pageNum);
                    }
                } catch (Exception e) {
                    log.error("任务id：{}，当前片：{}，页码：{}，查询异常",
                            customerInfoPushMain.getId(), part, pageNum, e);
                }
            }

            long partEndTime = System.currentTimeMillis();
            long partTotalTime = partEndTime - partStartTime;

            // 统计每个分片的信息
            partitionInfo.put("totalPages", totalPage);
            partitionInfo.put("queriedPages", partQueryCount);
            partitionInfo.put("dataCount", partDataCount);
            partitionInfo.put("totalTime", partTotalTime);
            partitionInfo.put("pageQueryTimes", pageQueryTimes);
            partitionInfo.put("success", true);

        } catch (Exception e) {
            log.error("查询分片数据异常，任务id：{}，分片：{}", customerInfoPushMain.getId(), part, e);
            partitionInfo.put("success", false);
            partitionInfo.put("error", e.getMessage());
            partitionInfo.put("totalTime", System.currentTimeMillis() - partStartTime);
        }

        return partitionInfo;
    }

    /**
     * 获取分片数据量
     */
    private Integer getPartitionDataNum(CustomerInfoPushMain customerInfoPushMain,
                                        String part,
                                        List<String> numList,
                                        List<Long> fileIds,
                                        Boolean isSingle,
                                        List<StraHisFile> straHisFiles) {
        try {
            if (isSingle) {
                return customerInfoPushMain.getmRealyNum();
            }

            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(customerInfoPushMain.getmApiCode());
            queryBaseBean.setBatchNumbers(Joiner.on(",").join(numList));
            queryBaseBean.setFileIds(Joiner.on(",").join(fileIds));
            queryBaseBean.setJsonData(customerInfoPushMain.getmRuleCondition());
            queryBaseBean.setPart(part);
            queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(straHisFiles, marketingCommonConfig));

            JSONObject policyCustomIndexes = marketingCommonConfig.getPolicyCustomIndexes();
            if (policyCustomIndexes != null) {
                String customIndexes = policyCustomIndexes.getString(customerInfoPushMain.getmApiCode());
                queryBaseBean.setCustomIndexes(customIndexes);
            }

            return iEsActionService.getTotal(queryBaseBean, customerInfoPushMain.getmApiCode(), customerInfoPushMain.getPushTarget());
        } catch (Exception e) {
            log.warn("获取分片数据量异常，任务id：{}，分片：{}", customerInfoPushMain.getId(), part, e);
            return 0;
        }
    }

    /**
     * 关闭线程池
     */
    private void shutdownThreadPool(ThreadPoolExecutor threadPool) {
        if (threadPool != null) {
            try {
                threadPool.shutdown();
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                    if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                        log.error("线程池关闭失败");
                    }
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
                log.error("线程池关闭时被中断", e);
            }
        }
    }
}
