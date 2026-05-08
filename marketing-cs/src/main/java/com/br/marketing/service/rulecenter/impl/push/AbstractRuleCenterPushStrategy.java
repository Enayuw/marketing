package com.br.marketing.service.rulecenter.impl.push;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.enums.*;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.ErrorMarkMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.Impl.PushRuleServiceImpl;
import com.br.marketing.service.ToPolicyByRuleService;
import com.br.marketing.service.rulecenter.IEsActionService;
import com.br.marketing.service.rulecenter.IRuleCenterPushStrategy;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.br.marketing.service.rulecenter.enums.RuleCenterPushTargetEnum;
import com.br.marketing.service.rulecenter.impl.esquery.EsQueryResult;
import com.br.marketing.service.rulecenter.impl.esquery.EsQueryExecutor;
import com.br.marketing.service.rulecenter.impl.esquery.EsQueryParams;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsNewIndexRuleUtils;
import com.br.marketing.util.GeneScriptUtil;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractRuleCenterPushStrategy implements IRuleCenterPushStrategy {


    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    MarketingHistoryEsServiceImpl marketingHistoryEsService;

    @Autowired
    PushRuleServiceImpl pushRuleService;

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    ErrorMarkMapper errorMarkMapper;

    @Resource
    private ToPolicyByRuleService toPolicyByRuleService;

    @Autowired
    private EsQueryExecutor esQueryExecutor;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    @Autowired
    IEsActionService iEsActionService;

    @Resource
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;


    @Override
    public final Result<Boolean> executePush(RuleCenterPushContext context) {
        try {
            // 1. 数据验证
            Result<Boolean> validateResult = validateData(context);
            if (!ResultCode.SUCCESS.getValue().equals(validateResult.getCode())) {
                return validateResult;
            }

            // 2. 预处理
            Result<Boolean> preProcessResult = preProcess(context);
            if (!ResultCode.SUCCESS.getValue().equals(preProcessResult.getCode())) {
                return preProcessResult;
            }

            // 3. 执行具体推送逻辑
            Result<Boolean> pushResult = doExecutePush(context);

            // 4. 后处理
            postProcess(context, pushResult);

            return pushResult;

        } catch (Exception e) {
            log.error("推送策略执行异常", e);
            return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(true);

        }
    }


    /**
     * 统一的数据捞取方法
     */
    protected QueryBaseBean createQueryBaseBean(RuleCenterPushContext context, Integer partitionIndex) {
        CustomerInfoPushMain main = context.getCustomerInfoPushMain();
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(main.getmApiCode());
        queryBaseBean.setBatchNumbers(Joiner.on(",").join(context.getBatchNumbers()));
        queryBaseBean.setFileIds(Joiner.on(",").join(context.getFileIds()));
        queryBaseBean.setJsonData(main.getmRuleCondition());
        if (partitionIndex != null) {
            queryBaseBean.setPart(partitionIndex.toString());
        }
        queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(context.getStraHisFiles(), marketingCommonConfig));
        return queryBaseBean;
    }

    /**
     * 预处理
     */
    protected Result<Boolean> preProcess(RuleCenterPushContext context) {
        log.warn(getPushName(context) + "开始执行推送策略预处理，任务ID: {}, 策略类型: {}",
                context.getCustomerInfoPushMain().getId(),
                getPushName(context));

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(true);
    }

    /**
     * 数据验证
     */
    protected Result<Boolean> validateData(RuleCenterPushContext context) {
        CustomerInfoPushMain pushMain = context.getCustomerInfoPushMain();
        Boolean isSigle = context.getSinglePartition();
        Integer parNum = context.getPartitionCount();
        Map partMap = context.getPartitionDataCount();
        if (!isSigle) {
            Integer nowSum = 0;
            List<Future<Result<Integer>>> resList = new ArrayList<>();
            Integer toPolicyThreadNum = marketingCommonConfig.getToPolicyThreadNum().get("toPolicyThreadNum");
            Integer toPolicyQueueNum = marketingCommonConfig.getToPolicyThreadNum().get("toPolicyQueueNum");
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(toPolicyThreadNum, toPolicyThreadNum, toPolicyQueueNum);

            for (Integer i = 0; i < parNum; i++) {
                QueryBaseBean queryBaseBean = createQueryBaseBean(context, i);
                Integer nowNum = iEsActionService.getTotal(queryBaseBean, pushMain.getmApiCode(), pushMain.getPushTarget());
                partMap.put(i, nowNum);
                if (pushMain.getTagContent() != null) {
                    resList.add(threadPool.submit(() -> pushRuleService.queryTotal(pushMain, context.getBatchNumbers(), queryBaseBean)));
                } else {
                    nowSum += nowNum;
                }
            }
            try {
                for (Future<Result<Integer>> pushFuture : resList) {
                    Result<Integer> pushRes = pushFuture.get();
                    if (ResultCode.SUCCESS.getValue().equals(pushRes.getCode())) {
                        nowSum += pushRes.getData();
                    } else {
                        return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setMessage("推送决策分片返回量级异常");
                    }
                }
            } catch (Exception ex) {
                log.error("推送决策 获取线程结果异常" + ex.getMessage(), ex);
            }
            // 关闭线程池
            threadPool.shutdown();
            try {
                while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                    log.info("推送决策查询量级：线程池关闭");
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
                log.error("推送决策查询量级：日志保存线程池结束异常！", ex);
                Thread.currentThread().interrupt();
            }
            if (!pushMain.getmRealyNum().equals(nowSum)) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                        "任务id：" + pushMain.getId() + "，分组查询和预览总数不一致，请手动处理！，分组查询的总数：" + nowSum.toString()
                                + "，预览总数：" + pushMain.getmRealyNum().toString()));
                return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setDate(Boolean.FALSE);
            }
        }
        log.warn("推送决策查询量级核对完成，任务id：{}", pushMain.getId());
        //加密校验
        Result<Integer> integerResult = pushRuleService.checkThreekEnc(context.getFileIds());
        if (!ResultCode.SUCCESS.getValue().equals(integerResult.getCode())) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                    String.format("该推送不符合推送决策的限制条件 流水号：%s,原因：%s", pushMain.getId().toString(), integerResult.getMessage())));
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
        //赋值加密方式
        context.setEncryptType(integerResult.getData());
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(true);
    }


    /**
     * 后处理
     */
    protected void postProcess(RuleCenterPushContext context, Result<Boolean> result) {
        log.warn(getPushName(context) + "推送策略后处理，任务ID: {}, 结果: {}",
                context.getCustomerInfoPushMain().getId(),
                result.getCode());

    }


    /**
     * 统一查询ES重试数据
     */
    protected int retryEsData(CustomerInfoPushMain customerInfoPushMain) {
        // 查询ES重试数据
        ErrorMarkExample errorMarkExample = new ErrorMarkExample();
        errorMarkExample.createCriteria().andMIdEqualTo(customerInfoPushMain.getId())
                .andRetryStatusEqualTo(RetryStatusEnum.AWAIT_COMPLETE.getValue())
                .andTypeEqualTo(ErrorMarkTypeEnum.ES_ERROR.getValue())
                .andRetryTotalAttemptsLessThan(3);

        return errorMarkMapper.countByExample(errorMarkExample);
    }

    /**
     * 统一的推送执行方法
     */
    protected Result<Boolean> doExecutePush(RuleCenterPushContext context) {
        long startTime = System.currentTimeMillis();
        log.info("执行{}推送策略，任务ID: {}", getPushName(context),
                context.getCustomerInfoPushMain().getId());

        List<Future<List<Future<Result<Integer>>>>> futures = new ArrayList<>();
        // 创建推送任务 - 由子类具体实现
        for (Integer i = 0; i < context.getPartitionCount(); i++) {

            Callable<List<Future<Result<Integer>>>> pushTask = createPushTask(context, i);
            futures.add(context.getEsThreadPool().submit(pushTask));
        }

        // 使用统一的结果处理方法
        return processPushResults(futures, context, startTime);
    }

    /**
     * 统一的推送结果处理方法
     */
    protected Result<Boolean> processPushResults(
            List<Future<List<Future<Result<Integer>>>>> futures,
            RuleCenterPushContext context,
            long startTime) {

        CustomerInfoPushMain customerInfoPushMain = context.getCustomerInfoPushMain();
        CustomerInfoPushMain main = new CustomerInfoPushMain();

        try {
            int retryCount = 0;
            int failCount = 0;

            try {
                // 处理推送结果
                for (Future<List<Future<Result<Integer>>>> actionFuture : futures) {
                    List<Future<Result<Integer>>> pushFutures = actionFuture.get();
                    for (Future<Result<Integer>> pushFuture : pushFutures) {
                        Result<Integer> pushRes = pushFuture.get();
                        if (ResultCode.TIME_OUT.getValue().equals(pushRes.getCode())
                                || ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(pushRes.getCode())) {
                            retryCount++;
                        } else if (ResultCode.FAIL.getValue().equals(pushRes.getCode())) {
                            failCount++;
                        }
                    }
                }

                // 设置状态 - 按照原始逻辑
                if (failCount > 0) {
                    main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
                } else {
                    Integer status = getSuccessStatus(customerInfoPushMain);
                    main.setmStatus(status);
                }

            } catch (Exception ex) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                        "推送决策 获取线程结果异常!"), ex);
                main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
            }

            // 记录结果日志 - 按照原始格式
            log.warn(getPushName(context) + "retryCount：" + retryCount + ",failCount:" + failCount);

            // 发送失败告警 - 按照原始逻辑
            if (failCount > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("推送决策失败：\n");
                sb.append("apiCode：").append(customerInfoPushMain.getmApiCode());
                sb.append("，任务id：").append(customerInfoPushMain.getId());
                sendAlert("推送决策失败", sb.toString());
            }

        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                    "推送决策 获取线程结果异常!"), ex);
            main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
        }

        // 关闭线程池 - 按照原始逻辑
        shutdownThreadPools(context);

        // 记录耗时日志 - 按照原始格式
        log.warn(getPushName(context) + " 任务id：{}；整体耗时：{}；计划数量：{}",
                customerInfoPushMain.getId(),
                System.currentTimeMillis() - startTime,
                customerInfoPushMain.getmRealyNum());

        // 更新数据库状态
        main.setId(customerInfoPushMain.getId());
        customerInfoPushMainMapper.updateByPrimaryKeySelective(main);

        // 返回结果
        Result<Boolean> result = new Result<>();
        if (PushRuleStatusEnum.PUSH_FAIL.getValue().equals(main.getmStatus())) {
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage("推送决策失败");
        } else {
            result.setCode(ResultCode.SUCCESS.getValue());
            result.setMessage("推送决策成功");
        }

        return result;
    }


    /**
     * 创建推送任务 - 子类必须实现
     */
    protected abstract Callable<List<Future<Result<Integer>>>> createPushTask(RuleCenterPushContext context, Integer partitionIndex);


    /**
     * 获取成功状态 - 子类可以重写
     */
    protected abstract Integer getSuccessStatus(CustomerInfoPushMain customerInfoPushMain);


    protected String getPushName(RuleCenterPushContext context) {

        CustomerInfoPushMain customerInfoPushMain = context.getCustomerInfoPushMain();
        RuleCenterPushTargetEnum pushTargetEnum = RuleCenterPushTargetEnum.findPushNameByCode(customerInfoPushMain.getPushTarget());
        return pushTargetEnum.getDesc();

    }

    ;


    /**
     * 关闭线程池 - 按照原始逻辑
     */
    protected void shutdownThreadPools(RuleCenterPushContext context) {
        try {
            if (context.getEsThreadPool() != null) {
                context.getEsThreadPool().shutdown();
                while (!context.getEsThreadPool().awaitTermination(5L, TimeUnit.SECONDS)) {
                    // 等待线程池关闭
                }
            }

            if (context.getPushThreadPool() != null) {
                context.getPushThreadPool().shutdown();
                while (!context.getPushThreadPool().awaitTermination(5L, TimeUnit.SECONDS)) {
                    // 等待线程池关闭
                }
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                    ex.getMessage()), ex);
        }
    }

    /**
     * 发送告警
     */
    protected void sendAlert(String title, String text) {
        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.ZHIJIA_CLUEFEEDBACK_MSG.toString());
        DingDingMarkdownMessage.Markdown markdown = new DingDingMarkdownMessage.Markdown();
        markdown.setTitle(title);
        markdown.setText(text);
        DingDingMarkdownMessage dingDingMarkdownMessage = new DingDingMarkdownMessage();
        dingDingMarkdownMessage.setMarkdown(markdown);
        dingDingRobotHookService.sendMessageGroup(map.get("token").toString(), map.get("secret").toString(), dingDingMarkdownMessage, true);
    }

    /**
     * 创建ES查询参数
     */
    protected EsQueryParams createEsQueryParams(CustomerInfoPushMain customerInfoPushMain,
                                                String part,
                                                List<String> numList,
                                                List<Long> fileIds,
                                                Integer pageSize,
                                                Integer totalPage,
                                                Boolean isPerOrTop,
                                                Object labelObject,
                                                Boolean markWithEsFlag,
                                                List<StraHisFile> straHisFiles) {
        return esQueryExecutor.initializeParams(customerInfoPushMain, part, numList, fileIds, pageSize, totalPage,
                isPerOrTop, labelObject, markWithEsFlag, straHisFiles);
    }

    /**
     * 执行ES查询
     */
    protected EsQueryResult executeEsQuery(EsQueryParams params, int currentPage) {
        return esQueryExecutor.executeQuery(params, currentPage);
    }


    /**
     * 组装推送上下文
     */
    protected RuleCenterPushContext assemblePushContext(CustomerInfoPushMain customerInfoPushMain) {
        CustomerInfoPushBatchExample searchPushBatch = new CustomerInfoPushBatchExample();
        searchPushBatch.createCriteria().andMIdEqualTo(customerInfoPushMain.getId());
        List<CustomerInfoPushBatch> customerInfoPushBatches = customerInfoPushBatchMapper.selectByExample(searchPushBatch);

        List<String> numList = new ArrayList<>();
        List<Long> fileIds = new ArrayList<>();
        for (CustomerInfoPushBatch customerInfoPushBatch : customerInfoPushBatches) {
            numList.add(customerInfoPushBatch.getmBatchNumber());
            fileIds.add(customerInfoPushBatch.getmFileId());
        }
        List<StraHisFile> straHisFiles;
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria().andIdIn(fileIds);
        straHisFiles = straHisFileMapper.selectByExample(fileExample);
        String scoreFileYhTime = marketingCommonConfig.getScoreFileYhTime();
        Date yhTime = null;
        try {
            yhTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(scoreFileYhTime);
        } catch (ParseException e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), e.getMessage()), e);
        }
        Date yh = yhTime;
        Integer parNum = 0;
        long beforeCount = straHisFiles.stream().filter(t -> t.getCreateTime().compareTo(yh) <= 0).count();
        Optional<StraHisFile> first = straHisFiles.stream().sorted(Comparator.comparing(StraHisFile::getIndexNum).reversed()).findFirst();

        if (first.isPresent()) {
            parNum = first.get().getIndexNum();
        }
        boolean isSigle = (customerInfoPushMain.getmPercentage() != null
                && customerInfoPushMain.getmPercentage().compareTo(BigDecimal.ZERO) > 0)
                || (customerInfoPushMain.getmPlanNum() != null && customerInfoPushMain.getmPlanNum() > 0)
                || beforeCount > 0;
        if (isSigle) {
            parNum = 1;
        }

        Boolean markWithEsFlag = marketingCommonConfig.getPushPolicyMarkWithEsFlag();
        String scoreCondition = customerInfoPushMain.getmScoreCondition();
        Object lableObject = null;
        if (StringUtils.isNotEmpty(scoreCondition)) {
            if (markWithEsFlag) {
                lableObject = GeneScriptUtil.esLableScript(scoreCondition);
            } else {
                lableObject = GeneScriptUtil.getScoreLables(scoreCondition, markWithEsFlag);
            }
        }
        //构建上下文
        RuleCenterPushContext context = new RuleCenterPushContext();
        context.setBatchNumbers(numList);
        context.setFileIds(fileIds);
        context.setStraHisFiles(straHisFiles);
        context.setCustomerInfoPushMain(customerInfoPushMain);
        context.setSinglePartition(isSigle);
        context.setMarkWithEsFlag(markWithEsFlag);
        context.setLabelObject(lableObject);
        context.setPartitionCount(parNum);
        return context;
    }

    /**
     * 设置线程池大小，子类各自实现
     */
    protected abstract RuleCenterPushContext setThreadPoolNum(RuleCenterPushContext pushContext);


}
