package com.br.marketing.service.rulecenter.impl.push;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.*;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.mapper.MarketingRuleCenterLabelReportMapper;
import com.br.marketing.mapper.MarketingSyncLabelMapper;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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


    protected Result<Boolean> preProcess(RuleCenterPushContext context) {
        CustomerInfoPushMain customerInfoPushMain = new CustomerInfoPushMain();
        MarketingRuleCenterLabelReportExample labelReportExample = new MarketingRuleCenterLabelReportExample();
        labelReportExample.createCriteria().andApiCodeEqualTo(customerInfoPushMain.getmApiCode())
                .andLabelIdEqualTo(customerInfoPushMain.getId())
                .andIsDelEqualTo(1);
        List<MarketingRuleCenterLabelReport> labelReportList = marketingRuleCenterLabelReportMapper.selectByExample(labelReportExample);
        List<String> appletDates = labelReportList.stream().map(MarketingRuleCenterLabelReport::getAppletDate).collect(Collectors.toList());
        context.setAppletDateList(appletDates);
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue());
    }


    protected void postProcess(RuleCenterPushContext context, Result<Boolean> result) {

        CustomerInfoPushMain pushMain = context.getCustomerInfoPushMain();

        List<Map<String, String>> labelNumList = marketingSyncLabelMapper.getLabelNum(pushMain.getId(), pushMain.getmApiCode());

        labelNumList.forEach(map -> {
            String appletDate = map.get("applet_date");
            String userType = map.get("user_type");
            String num = map.get("num");
            //更新统计表，上传记录表
            MarketingRuleCenterLabelReport report = new MarketingRuleCenterLabelReport();
            MarketingRuleCenterLabelReportExample labelReportExample = new MarketingRuleCenterLabelReportExample();
            labelReportExample.createCriteria().andApiCodeEqualTo(pushMain.getmApiCode())
                    .andLabelNameEqualTo(pushMain.getLabelName())
                    .andAppletDateEqualTo(appletDate)
                    .andUserTypeEqualTo(userType)
                    .andIsDelEqualTo(1);
            List<MarketingRuleCenterLabelReport> labelReportList = marketingRuleCenterLabelReportMapper.selectByExample(labelReportExample);
            if (!CollectionUtils.isEmpty(labelReportList)) {
                MarketingRuleCenterLabelReport update = labelReportList.get(0);
                update.setNum(Long.parseLong(num));
                marketingRuleCenterLabelReportMapper.updateByPrimaryKeySelective(update);
            }
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
                    labelJson.put(pushMain.getLabelName(), num);
                }
                syncReport.setLabelMessage(labelJson.toJSONString());
                syncReport.setUpdateTime(new Date());
                syncReportMapper.updateByPrimaryKeySelective(syncReport);
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
                context.getAppletDateList()
        );
    }

    @Override
    protected Integer getSuccessStatus(CustomerInfoPushMain customerInfoPushMain) {
        return PushRuleStatusEnum.CONFIRMED_SUCCESS.getValue();
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
        private List<String> appletDateList;


        public DataLabelTask(ThreadPoolExecutor pushJcPool
                , CustomerInfoPushMain customerInfoPushMain
                , List<Long> fileIds, List<String> numList
                , String part, Boolean isPerOrTop, Integer partDataNum, List<String> appletDateList) {
            this.pushJcPool = pushJcPool;
            this.customerInfoPushMain = customerInfoPushMain;
            this.fileIds = fileIds;
            this.numList = numList;
            this.part = part;
            this.isPerOrTop = isPerOrTop;
            this.partDataNum = partDataNum;
            this.appletDateList = appletDateList;
        }

        @Override
        public List<Future<Result<Integer>>> call() {
            String apiCode = customerInfoPushMain.getmApiCode();
            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(apiCode);
            queryBaseBean.setBatchNumbers(Joiner.on(",").join(numList));
            queryBaseBean.setFileIds(Joiner.on(",").join(fileIds));
            queryBaseBean.setJsonData(customerInfoPushMain.getmRuleCondition());
            if (!isPerOrTop) {
                queryBaseBean.setPart(part);
            }
            Integer pageSize = 2000;
            Integer total = isPerOrTop ? customerInfoPushMain.getmRealyNum()
                    : partDataNum;
            int totalYuShu = total % pageSize;
            String searchAfterStr = "";
            int totalPage = total / pageSize + (totalYuShu > 0 ? 1 : 0);
            log.warn("任务id：{}，当前片：{}，总数：{}，页数：{}"
                    , customerInfoPushMain.getId()
                    , StringUtils.isBlank(part) ? "" : part
                    , total
                    , totalPage);
            List<Future<Result<Integer>>> resList = new ArrayList<>();

            for (int i = 1; i <= totalPage; i++) {
                try {
                    String sn = String.valueOf(i);
                    if (i == totalPage && totalYuShu > 0) {
                        queryBaseBean.setPageSize(totalYuShu);
                    } else {
                        queryBaseBean.setPageSize(pageSize);
                    }
                    queryBaseBean.setSearchAfter(searchAfterStr);

                    List<MarketingHistory> marketingHistories;
                    marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);

                    if (marketingHistories == null) {
                        throw new Exception();
                    }
                    // 获取最后一条记录的searchAfter值
                    if (!marketingHistories.isEmpty()) {
                        searchAfterStr = marketingHistories.get(marketingHistories.size() - 1).getSearchAfter();
                    }

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
                    List<MarketingSyncLabel> marketingSyncLabelList = new ArrayList<>();
                    List<String> custNumList = marketingHistories.stream().map(MarketingHistory::getCusNum).collect(Collectors.toList());
                    List<List<String>> partitionList = ListUtils.partition(custNumList, 500);
                    partitionList.forEach(custNums -> {
                                resList.add(pushJcPool.submit(new LabelToDB(custNums, apiCode, appletDateList, customerInfoPushMain.getId())));
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

        private List<String> appletDateList;

        private Long labelId;

        public LabelToDB(List<String> custNumList, String apiCode, List<String> appletDateList, Long labelId) {
            this.custNumList = custNumList;
            this.apiCode = apiCode;
            this.appletDateList = appletDateList;
            this.labelId = labelId;
        }

        @Override
        public Result<Integer> call() {
            Result<Integer> result = new Result<>();
            //批量入库
            try {
                List<MarketingSyncUser> marketingSyncUsers = marketingSyncUserMapper.getUserByCustNumAndAppletData(apiCode, appletDateList, custNumList);
                List<MarketingSyncLabel> marketingSyncLabelList = new ArrayList<>();
                marketingSyncUsers.forEach(syncUser -> {
                    MarketingSyncLabel syncLabel = new MarketingSyncLabel();
                    BeanUtils.copyProperties(syncUser, syncLabel);
                    syncLabel.setLabelId(labelId);
                    syncLabel.setSyncId(syncUser.getId());
                    syncLabel.setId(null);
                    marketingSyncLabelList.add(syncLabel);

                });
                marketingSyncLabelMapper.batchInsert(apiCode, marketingSyncLabelList);
                log.warn("规则中心数据打标插入成功");
                marketingSyncUsers.clear();
                marketingSyncLabelList.clear();
                result.setCode(ResultCode.SUCCESS.getValue());
            } catch (Exception e) {
                log.error("规则中心数据打标批量入库失败", e);
                result.setCode(ResultCode.FAIL.getValue());
            }
            return result;
        }
    }

}
