package com.br.marketing.service.rulecenter.impl.esquery;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.ErrorMark;
import com.br.marketing.entity.ErrorMarkExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.enums.MockSwitchEnum;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.enums.RetryStatusEnum;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.mapper.ErrorMarkMapper;
import com.br.marketing.service.ToPolicyByRuleService;
import com.br.marketing.service.rulecenter.IEsActionService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsNewIndexRuleUtils;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * ES查询执行器 - 封装ES查询和错误处理逻辑
 * 无状态单例，所有参数通过EsQueryParams传递
 */
@Slf4j
@Component
public class EsQueryExecutor {

    @Autowired
    private MarketingHistoryEsServiceImpl marketingHistoryEsService;

    @Resource
    private ToPolicyByRuleService toPolicyByRuleService;

    @Resource
    private ErrorMarkMapper errorMarkMapper;

    @Autowired
    IEsActionService iEsActionService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    /**
     * 初始化查询参数（设置重试逻辑）
     */
    public EsQueryParams initializeParams(CustomerInfoPushMain customerInfoPushMain,
                                          String part,
                                          List<String> numList,
                                          List<Long> fileIds,
                                          Integer pageSize,
                                          Integer totalPage,
                                          Boolean isPerOrTop,
                                          Object labelObject,
                                          Boolean markWithEsFlag,
                                          List<StraHisFile> straHisFiles) {
        EsQueryParams params = new EsQueryParams(customerInfoPushMain, part, numList, fileIds,
                pageSize, totalPage, isPerOrTop, labelObject, markWithEsFlag);
        params.setStraHisFiles(straHisFiles);

        // 初始化重试逻辑
        initializeRetryLogic(params);

        return params;
    }

    /**
     * 初始化重试逻辑
     */
    private void initializeRetryLogic(EsQueryParams params) {
        // 判断该任务是否为异常待补推任务
        if (PushRuleStatusEnum.EXCEPTIONS_RUNNING.getValue()
                .equals(params.getCustomerInfoPushMain().getmStatus())) {

            // 查询待补推数据
            ErrorMarkExample errorMarkExample = new ErrorMarkExample();
            errorMarkExample.createCriteria().andMIdEqualTo(params.getCustomerInfoPushMain().getId())
                    .andPartEqualTo(params.getPart())
                    .andRetryStatusEqualTo(RetryStatusEnum.AWAIT_COMPLETE.getValue());
            List<ErrorMark> errorMarks = errorMarkMapper.selectByExample(errorMarkExample);

            // 查询当前part下的异常数据
            if (!CollectionUtils.isEmpty(errorMarks)) {
                ErrorMark errorMark = errorMarks.get(0);
                params.setErrorMark(errorMark);
                params.setStartPageIndex(errorMark.getPageSize());
                params.setSearchAfterStr(errorMark.getSearchAfter());
            }
        }
    }

    /**
     * 执行ES查询前置
     */

    public Boolean excuteBefore(EsQueryParams params) {
        Boolean isSuccess = Boolean.TRUE;
        if (PushRuleStatusEnum.EXCEPTIONS_RUNNING.getValue()
                .equals(params.getCustomerInfoPushMain().getmStatus())) {
            // 查询待补推数据
            ErrorMarkExample errorMarkExample = new ErrorMarkExample();
            errorMarkExample.createCriteria().andMIdEqualTo(params.getCustomerInfoPushMain().getId())
                    .andPartEqualTo(params.getPart())
                    .andRetryStatusEqualTo(RetryStatusEnum.AWAIT_COMPLETE.getValue());
            List<ErrorMark> errorMarks = errorMarkMapper.selectByExample(errorMarkExample);
            // 查询当前part下的异常数据
            if (CollectionUtils.isEmpty(errorMarks)) {
                isSuccess = Boolean.FALSE;
            }
        }
        return isSuccess;

    }

    /**
     * 执行ES查询
     */
    public EsQueryResult executeQuery(EsQueryParams params, int currentPage) {
        // 构建查询参数
        QueryBaseBean queryBaseBean = createQueryBaseBean(params, currentPage);

        EsQueryResult result = new EsQueryResult();

        try {
            boolean mockEsError = toPolicyByRuleService.mockSwitch(params.getCustomerInfoPushMain().getmApiCode(),
                    MockSwitchEnum.GENERAL.getValue(), MockSwitchEnum.ESRETRY.getValue());
            if (mockEsError) {
                throw new Exception("模拟ES异常场景");
            }

            // 查询ES数据
            List<MarketingHistory> marketingHistories = iEsActionService.
                    getMarketingHistorys(queryBaseBean
                            , params.getCustomerInfoPushMain().getmApiCode()
                            , params.getCustomerInfoPushMain().getPushTarget());

            if (marketingHistories == null) {
                throw new Exception("ES查询返回空结果");
            }

            // 获取最后一条记录的searchAfter值
            if (!marketingHistories.isEmpty()) {
                String searchAfterStr = marketingHistories.get(marketingHistories.size() - 1).getSearchAfter();
                //下一页searchAfter
                params.setSearchAfterStr(searchAfterStr);
            }

            result.setSuccess(true);
            result.setMarketingHistories(marketingHistories);
            result.setSearchAfter(params.getSearchAfterStr());
            result.setQueryBaseBean(queryBaseBean);

            // 成功后清理错误标记
            if (params.getErrorMark().getId() != null) {
                clearErrorMark(params);
            }

        } catch (Exception e) {
            log.warn("ES查询异常，任务id：{}，当前片：{}，当前页码：{}",
                    params.getCustomerInfoPushMain().getId(), params.getPart(), currentPage, e);
            result.setSuccess(false);
            result.setException(e);
            // 处理错误标记
            handleEsQueryError(params, currentPage, queryBaseBean, e);
        }

        return result;
    }

    /**
     * 构建查询参数
     */
    private QueryBaseBean createQueryBaseBean(EsQueryParams params, int currentPage) {
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(params.getCustomerInfoPushMain().getmApiCode());
        queryBaseBean.setBatchNumbers(Joiner.on(",").join(params.getNumList()));
        queryBaseBean.setFileIds(Joiner.on(",").join(params.getFileIds()));
        queryBaseBean.setJsonData(params.getCustomerInfoPushMain().getmRuleCondition());

        // 处理标签对象的逻辑
        if (params.getLabelObject() != null) {
            if (params.getMarkWithEsFlag() != null && params.getMarkWithEsFlag()) {
                queryBaseBean.setScriptFields(params.getLabelObject().toString());
            }
        }

        if (!params.getIsPerOrTop()) {
            queryBaseBean.setPart(params.getPart());
        }

        if (!StringUtils.isEmpty(params.getCustomIndexes())){
            queryBaseBean.setCustomIndexes(params.getCustomIndexes());
        }
        queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(params.getStraHisFiles(), marketingCommonConfig));

        // 设置分页参数
        int totalYuShu = (params.getIsPerOrTop() ? params.getCustomerInfoPushMain().getmRealyNum() :
                (params.getTotalPage() * params.getPageSize())) % params.getPageSize();
        if (currentPage == params.getTotalPage() && totalYuShu > 0) {
            queryBaseBean.setPageSize(totalYuShu);
        } else {
            queryBaseBean.setPageSize(params.getPageSize());
        }
        queryBaseBean.setSearchAfter(params.getSearchAfterStr());

        return queryBaseBean;
    }

    /**
     * 处理ES查询错误
     */
    private void handleEsQueryError(EsQueryParams params, int currentPage, QueryBaseBean queryBaseBean, Exception e) {
        try {
            if (params.getErrorMark().getId() != null) {
                // 已存在补推记录，更新重试次数
                if (params.getErrorMark().getRetryTotalAttempts() < 3) {
                    updateErrorMark(params.getErrorMark(), params.getErrorMark().getRetryTotalAttempts() + 1);
                }
            } else {
                // 新增异常待补推数据
                insertNewErrorMark(params.getCustomerInfoPushMain(), params.getPart(), currentPage, params.getSearchAfterStr(),
                        JSONObject.toJSONString(queryBaseBean));
            }
        } catch (Exception ex) {
            log.error("处理ES查询错误时发生异常", ex);
        }
    }

    /**
     * 清理错误标记
     */
    private void clearErrorMark(EsQueryParams params) {
        ErrorMark errorMark1 = new ErrorMark();
        errorMark1.setId(params.getErrorMark().getId());
        errorMark1.setRetryStatus(RetryStatusEnum.PUSH_COMPLETE.getValue());
        errorMarkMapper.updateByPrimaryKeySelective(errorMark1);
    }

    /**
     * 新增错误标记
     */
    private void insertNewErrorMark(CustomerInfoPushMain customerInfoPushMain,
                                    String part,
                                    int pageSize,
                                    String searchAfterStr,
                                    String esCondition) {
        ErrorMark errorMark = new ErrorMark();
        errorMark.setApiCode(customerInfoPushMain.getmApiCode());
        errorMark.setmId(customerInfoPushMain.getId());
        errorMark.setPart(part);
        errorMark.setPageSize(pageSize);
        errorMark.setSearchAfter(searchAfterStr);
        errorMark.setEsCondition(esCondition);
        errorMark.setRetryStatus(RetryStatusEnum.AWAIT_COMPLETE.getValue());
        errorMark.setAppletDate(LocalDate.now().toString());
        errorMark.setCreateTime(new Date());
        errorMark.setUpdateTime(new Date());
        errorMarkMapper.insertSelective(errorMark);
    }

    /**
     * 更新错误标记
     */
    private void updateErrorMark(ErrorMark errorMark, int retryAttempts) {
        ErrorMark errorMark1 = new ErrorMark();
        errorMark1.setId(errorMark.getId());
        errorMark1.setRetryTotalAttempts(retryAttempts);
        errorMark1.setUpdateTime(new Date());
        errorMarkMapper.updateByPrimaryKeySelective(errorMark1);
    }

}
