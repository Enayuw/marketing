package com.br.marketing.service.rulecenter.impl.push;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.mapper.MarketingSyncLabelMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class DataLabelPushStrategy extends AbstractRuleCenterPushStrategy {

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    MarketingSyncLabelMapper marketingSyncLabelMapper;


    protected void postProcess(RuleCenterPushContext context, Result<Boolean> result) {

        CustomerInfoPushMain pushMain = context.getCustomerInfoPushMain();

        List<Map<String,String>> labelNumList =  marketingSyncLabelMapper.getLabelNum(pushMain.getId(),pushMain.getmApiCode());

        labelNumList.forEach(map->{
            map.get("applet_date");
            //TODO更新统计表，上传记录表
            

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
                context.getPartitionDataCount().get(partitionIndex)
        );
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


        public DataLabelTask(ThreadPoolExecutor pushJcPool
                , CustomerInfoPushMain customerInfoPushMain
                , List<Long> fileIds, List<String> numList
                , String part, Boolean isPerOrTop, Integer partDataNum) {
            this.pushJcPool = pushJcPool;
            this.customerInfoPushMain = customerInfoPushMain;
            this.fileIds = fileIds;
            this.numList = numList;
            this.part = part;
            this.isPerOrTop = isPerOrTop;
            this.partDataNum = partDataNum;
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
                    tableCreateService.createMarketingSyncUserTable(apiCode);
                    List<MarketingSyncLabel> marketingSyncLabelList = new ArrayList<>();
                    for (int k = 0; k < marketingHistories.size(); k++) {
                        MarketingHistory marketingHistory = marketingHistories.get(k);
                        MarketingSyncLabel marketingSyncLabel = new MarketingSyncLabel();
                        marketingSyncLabel.setCustNum(marketingHistory.getCusNum());
                        marketingSyncLabel.setCell(marketingHistory.getCell());
                        marketingSyncLabel.setUserType(marketingHistory.getUserType());
                        marketingSyncLabel.setLabelId(customerInfoPushMain.getId());
                        marketingSyncLabel.setIdCard(marketingHistory.getIdCard());
                        marketingSyncLabel.setName(marketingHistory.getName());
                        marketingSyncLabel.setCusBatch(marketingHistory.getTaskId());
                        JSONObject varObject = JSON.parseObject(marketingHistory.getReserveField());
                        if (varObject == null) {
                            varObject = new JSONObject();
                        }
                        marketingSyncLabel.setReserveField1(varObject.toJSONString());
                        marketingSyncLabel.setAppletDate(varObject.getString("createTime"));

                        marketingSyncLabelList.add(marketingSyncLabel);
                    }
                    List<List<MarketingSyncLabel>> partitionList = ListUtils.partition(marketingSyncLabelList, 500);
                    partitionList.forEach(labelList -> {
                                resList.add(pushJcPool.submit(new LabelToDB(labelList)));


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

        private List<MarketingSyncLabel> marketingSyncLabelList;

        public LabelToDB(List<MarketingSyncLabel> marketingSyncLabelList) {
            this.marketingSyncLabelList = marketingSyncLabelList;
        }

        @Override
        public Result<Integer> call() {
            Result<Integer> result = new Result<>();
            //批量入库
            try {
                marketingSyncLabelMapper.batchInsert(marketingSyncLabelList);
                result.setCode(ResultCode.SUCCESS.getValue());
            } catch (Exception e) {
                log.error("规则中心数据打标批量入库失败");
                result.setCode(ResultCode.FAIL.getValue());
            }
            return result;
        }
    }

}
