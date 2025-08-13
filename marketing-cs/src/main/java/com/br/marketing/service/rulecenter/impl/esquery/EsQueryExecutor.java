package com.br.marketing.service.rulecenter.impl.esquery;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.ErrorMark;
import com.br.marketing.entity.ErrorMarkExample;
import com.br.marketing.enums.MockSwitchEnum;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.enums.RetryStatusEnum;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.mapper.ErrorMarkMapper;
import com.br.marketing.service.ToPolicyByRuleService;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * ES查询执行器 - 封装ES查询和错误处理逻辑
 * 独立的ES查询执行器，负责处理ES查询、错误重试、分页等逻辑
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

    // 查询参数
    private CustomerInfoPushMain customerInfoPushMain;
    private String part;
    private List<String> numList;
    private List<Long> fileIds;
    private Integer pageSize;
    private Integer totalPage;
    private Boolean isPerOrTop;
    private Object labelObject;
    private Boolean markWithEsFlag;

    // 状态变量
    private ErrorMark errorMark = new ErrorMark();
    private int startPageIndex = 1;
    private String searchAfterStr = "";

    /**
     * 初始化查询执行器
     */
    public EsQueryExecutor initialize(CustomerInfoPushMain customerInfoPushMain,
                                     String part,
                                     List<String> numList,
                                     List<Long> fileIds,
                                     Integer pageSize,
                                     Integer totalPage,
                                     Boolean isPerOrTop,
                                     Object labelObject,
                                     Boolean markWithEsFlag) {
        this.customerInfoPushMain = customerInfoPushMain;
        this.part = part;
        this.numList = numList;
        this.fileIds = fileIds;
        this.pageSize = pageSize;
        this.totalPage = totalPage;
        this.isPerOrTop = isPerOrTop;
        this.labelObject = labelObject;
        this.markWithEsFlag = markWithEsFlag;
        // 重置状态
        this.errorMark = new ErrorMark();
        this.startPageIndex = 1;
        this.searchAfterStr = "";
        
        // 初始化重试逻辑
        initializeRetryLogic();
        
        return this;
    }

    /**
     * 初始化重试逻辑
     */
    private void initializeRetryLogic() {
        // 判断该任务是否为异常待补推任务
        if (PushRuleStatusEnum.EXCEPTIONS_RUNNING.getValue()
                .equals(customerInfoPushMain.getmStatus())) {

            // 查询待补推数据
            ErrorMarkExample errorMarkExample = new ErrorMarkExample();
            errorMarkExample.createCriteria().andMIdEqualTo(customerInfoPushMain.getId())
                    .andPartEqualTo(part)
                    .andRetryStatusEqualTo(RetryStatusEnum.AWAIT_COMPLETE.getValue());
            List<ErrorMark> errorMarks = errorMarkMapper.selectByExample(errorMarkExample);

            // 查询当前part下的异常数据
            if (!CollectionUtils.isEmpty(errorMarks)) {
                errorMark = errorMarks.get(0);
                startPageIndex = errorMark.getPageSize();
                searchAfterStr = errorMark.getSearchAfter();
            }
        }
    }

    /**
     * 执行ES查询
     */
    public EsQueryResult executeQuery(int currentPage) {
        // 构建查询参数
        QueryBaseBean queryBaseBean = createQueryBaseBean(currentPage);

        EsQueryResult result = new EsQueryResult();

        try {
            boolean mockEsError = toPolicyByRuleService.mockSwitch(customerInfoPushMain.getmApiCode(),
                    MockSwitchEnum.GENERAL.getValue(), MockSwitchEnum.ESRETRY.getValue());
            if (mockEsError) {
                throw new Exception("模拟ES异常场景");
            }

            // 查询ES数据
            List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);

            if (marketingHistories == null) {
                throw new Exception("ES查询返回空结果");
            }

            // 获取最后一条记录的searchAfter值
            if (!marketingHistories.isEmpty()) {
                searchAfterStr = marketingHistories.get(marketingHistories.size() - 1).getSearchAfter();
            }

            result.setSuccess(true);
            result.setMarketingHistories(marketingHistories);
            result.setSearchAfter(searchAfterStr);
            result.setQueryBaseBean(queryBaseBean);

            // 成功后清理错误标记
            if (errorMark.getId() != null) {
                clearErrorMark();
            }

        } catch (Exception e) {
            log.warn("ES查询异常，任务id：{}，当前片：{}，当前页码：{}",
                    customerInfoPushMain.getId(), part, currentPage, e);
            result.setSuccess(false);
            result.setException(e);
            // 处理错误标记
            handleEsQueryError(currentPage, queryBaseBean, e);
        }

        return result;
    }

    /**
     * 构建查询参数
     */
    private QueryBaseBean createQueryBaseBean(int currentPage) {
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(customerInfoPushMain.getmApiCode());
        queryBaseBean.setBatchNumbers(Joiner.on(",").join(numList));
        queryBaseBean.setFileIds(Joiner.on(",").join(fileIds));
        queryBaseBean.setJsonData(customerInfoPushMain.getmRuleCondition());

        // 处理标签对象的逻辑（兼容PushPolicyPushStrategy的需求）
        if (labelObject != null) {
            if (markWithEsFlag != null && markWithEsFlag) {
                // 赋值es脚本
                queryBaseBean.setScriptFields(labelObject.toString());
            }
            // 如果markWithEsFlag为false，则在子类中处理scoreLables逻辑
        }

        if (!isPerOrTop) {
            queryBaseBean.setPart(part);
        }

        // 设置分页参数
        int totalYuShu = (isPerOrTop ? customerInfoPushMain.getmRealyNum() :
                (totalPage * pageSize)) % pageSize;
        if (currentPage == totalPage && totalYuShu > 0) {
            queryBaseBean.setPageSize(totalYuShu);
        } else {
            queryBaseBean.setPageSize(pageSize);
        }
        queryBaseBean.setSearchAfter(searchAfterStr);

        return queryBaseBean;
    }

    /**
     * 处理ES查询错误
     */
    private void handleEsQueryError(int currentPage, QueryBaseBean queryBaseBean, Exception e) {
        try {
            if (errorMark.getId() != null) {
                // 已存在补推记录，更新重试次数
                if (errorMark.getRetryTotalAttempts() < 3) {
                    updateErrorMark(errorMark, errorMark.getRetryTotalAttempts() + 1);
                }
            } else {
                // 新增异常待补推数据
                insertNewErrorMark(customerInfoPushMain, part, currentPage, searchAfterStr,
                        JSONObject.toJSONString(queryBaseBean));
            }
        } catch (Exception ex) {
            log.error("处理ES查询错误时发生异常", ex);
        }
    }

    /**
     * 清理错误标记
     */
    private void clearErrorMark() {
        ErrorMark errorMark1 = new ErrorMark();
        errorMark1.setId(errorMark.getId());
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

    // Getters
    public int getStartPageIndex() {
        return startPageIndex;
    }

    public String getSearchAfterStr() {
        return searchAfterStr;
    }

    public Object getLabelObject() {
        return labelObject;
    }

    public Boolean getMarkWithEsFlag() {
        return markWithEsFlag;
    }
}
