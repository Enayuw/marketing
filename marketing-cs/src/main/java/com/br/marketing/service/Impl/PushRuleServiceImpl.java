package com.br.marketing.service.Impl;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.commonentity.StatusConstants;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.Product;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;

import java.util.*;

import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.RabbitMqSenderUtils;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.dto.RequestPushInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.CustomerBatchNumDTO;
import com.br.marketing.mapper.*;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.vo.PushInfoDetailVO;
import com.br.marketing.vo.ScoreDetailVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class PushRuleServiceImpl implements PushRuleService {

    @Autowired
    MarketingTaskMapper marketingTaskMapper;

    @Autowired
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Autowired
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Autowired
    CustomerInfoPushLogMapper customerInfoPushLogMapper;

    @Autowired
    MarketingStrategyProductMapper marketingStrategyProductMapper;

    @Override
    public Result<List<ScoreDetailVo>> getBatchInfos(CustomerBatchNumDTO dto) {
        List<ScoreDetailVo> scoreDetailVos = marketingTaskMapper.queryBatchs(dto);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(scoreDetailVos);
    }

    @Override
    public Result<List<PushInfoDetailVO>> getPushInfos(RequestPushInfoDTO dto) {
        List<PushInfoDetailVO> pushInfos = customerInfoPushMainMapper.getPushInfos(dto);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushInfos);
    }

    @Autowired
    RabbitMqProducter producter;

    @Autowired
    @Qualifier("apipool")
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Autowired
    MarketingHistoryEsServiceImpl marketingHistoryEsService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> pushCustomer(PushCustomerDTO dto) {

        /**
         * 先校验下 传过来的批次和 模型是否匹配
         * 推送mq
         */

        //region check
        MarketingStrategyProductExample productExample = new MarketingStrategyProductExample();
        productExample.createCriteria().andApiCodeEqualTo(dto.getApiCode()).andCusBatchNumberIn(dto.getCusBatchNumberList())
                .andProductNameEqualTo(dto.getProductName()).andProductVersionEqualTo(dto.getProductVersion()).andIsDelEqualTo(Constants.DATA_VALID);
        List<MarketingStrategyProduct> marketingStrategyProducts = marketingStrategyProductMapper.selectByExample(productExample);
        if(marketingStrategyProducts.size()<=0){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("请核实下该批次和所筛选的模型是否匹配");
        }
        int planNum = dto.getMaxTop() - dto.getMinTop();
        if(planNum<=0){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("所选的top区间不合理");
        }
        int scoreDvalue = dto.getMaxScore() - dto.getMinScore();

        if(scoreDvalue<0){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("所选的分值区间不合理");
        }
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(dto.getApiCode());
        queryBaseBean.setBatchNumbers(Joiner.on(",").join(dto.getCusBatchNumberList()));
        queryBaseBean.setModelCode(dto.getProductName());
        queryBaseBean.setModelVersion(dto.getProductVersion());
        queryBaseBean.setScoreRange(dto.getMinScore().toString().concat(",").concat(dto.getMaxScore().toString()));
        queryBaseBean.setAmountTop(dto.getMinTop().toString().concat(",").concat(dto.getMaxTop().toString()));
        int total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        if(total<=0){
            throw new RuntimeException("无符合的数据");
        }
        //endregion

        //region insert db
        CustomerInfoPushMain customerInfoPushMain = new CustomerInfoPushMain();
        customerInfoPushMain.setmApiCode(dto.getApiCode());
        customerInfoPushMain.setmModel(dto.getProductName());
        customerInfoPushMain.setmModelVersion(dto.getProductVersion());
        customerInfoPushMain.setmNumMin(dto.getMinTop());
        customerInfoPushMain.setmNumMax(dto.getMaxTop());
        customerInfoPushMain.setmScoreMin(dto.getMinScore());
        customerInfoPushMain.setmScoreMax(dto.getMaxScore());
        customerInfoPushMain.setmPlanNum(planNum);
        Date date = new Date();
        customerInfoPushMain.setCreateTime(date);
        customerInfoPushMain.setUpdateTime(date);
        customerInfoPushMainMapper.insertSelective(customerInfoPushMain);

        dto.getCusBatchNumberList().forEach(t->{
            CustomerInfoPushBatch customerInfoPushBatch = new CustomerInfoPushBatch();
            customerInfoPushBatch.setmId(customerInfoPushMain.getId());
            customerInfoPushBatch.setmApiCode(dto.getApiCode());
            customerInfoPushBatch.setmBatchNumber(t);
            customerInfoPushBatch.setCreateTime(date);
            customerInfoPushBatch.setUpdateTime(date);
            customerInfoPushBatchMapper.insertSelective(customerInfoPushBatch);
        });
        //endregion

        //region push Intelligent Customer Service
        //调用es查询接口

        Integer minTop = dto.getMinTop();
        int startPage_yushu = minTop % 10000;
        Integer startPage = minTop/10000+(startPage_yushu >0?1:0);
        String searchAfterStr = "";
        for (int i = 1; i <=startPage; i++) {

            if(i==startPage&&startPage_yushu>0){
                queryBaseBean.setPageSize(startPage_yushu);
            }else {
                queryBaseBean.setPageSize(10000);
            }
            if(i==startPage){
                queryBaseBean.setPageSize(queryBaseBean.getPageSize()-1);
            }
            queryBaseBean.setSearchAfter(searchAfterStr);
            String s = marketingHistoryEsService.builderMarketingWithSearchAfter(queryBaseBean);
            searchAfterStr = s;
        }
        int total_yushu = total % 2000;
        int total_page = total / 2000 + (total_yushu > 0 ? 1 : 0);
        List<Callable<Result>> listCall = new ArrayList<>();
        for (int i = 1; i <= total_page; i++) {
            String sn = String.valueOf(i);
            queryBaseBean.setSearchAfter(searchAfterStr);
            List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
            List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
            for (int k = 0; k < marketingHistories.size(); k++) {
                MarketingHistory marketingHistory = marketingHistories.get(k);
                PushMarketingUserDetailDTO dto1 = new PushMarketingUserDetailDTO();
                dto1.setCaseNumber(customerInfoPushMain.getId().toString()+"_"+ marketingHistory.getSwiftNumber());
                dto1.setPhone(marketingHistory.getCell());
//                dto1.setVariables("");
                Optional<Product> first = marketingHistory.getProduct().stream().filter(t -> customerInfoPushMain.getmModel().equals(t.getCode())
                        && customerInfoPushMain.getmModelVersion().equals(t.getVersion())).findFirst();
                if(first.isPresent()){
                    dto1.setScore(String.valueOf(first.get().getScore()));
                }
//                dto1.setScoreDate("2021-05-33");
                dto1.setScoreName(customerInfoPushMain.getmModel());
                dto1.setUpload("1");
                userDetailDTOS.add(dto1);
            }
            PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
            pushMarketingUserTaskInfoDTO.setMethod("caseAdd");
            pushMarketingUserTaskInfoDTO.setBatchNumber(customerInfoPushMain.getId().toString());
//                pushMarketingUserTaskInfoDTO.setStrategyCode("");
//                pushMarketingUserTaskInfoDTO.setIsAutoRunStrategy("");
            pushMarketingUserTaskInfoDTO.setAccessNumber(customerInfoPushMain.getId()+"_"+ sn);
//                pushMarketingUserTaskInfoDTO.setExtendData("");
            pushMarketingUserTaskInfoDTO.setScoreName(dto.getProductName());
            pushMarketingUserTaskInfoDTO.setScoreRange(dto.getMinScore().toString().concat(",").concat(dto.getMaxScore().toString()));
            pushMarketingUserTaskInfoDTO.setAmountTop(Convert.toStr(dto.getMaxTop() - dto.getMinTop()));
            pushMarketingUserTaskInfoDTO.setSampleTotal(sn);
            pushMarketingUserTaskInfoDTO.setData(userDetailDTOS);

            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(dto.getApiCode());
            pushMarketingUserDTO.setPlatApiCode(dto.getApiCode());
            pushMarketingUserDTO.setJsonData(JSON.toJSONString(pushMarketingUserTaskInfoDTO));

            return intelligentCustomerServiceClient.pushUser(pushMarketingUserDTO,customerInfoPushMain.getId(),pushMarketingUserTaskInfoDTO.getAccessNumber());
        }
        List<Future<Result>>  futures = null;
        try {
            futures = threadPoolExecutor.invokeAll(listCall);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Future<Result>> failFutures = futures.stream().filter(t -> {
            try {
                return !ResultCode.SUCCESS.getValue().equals(t.get().getCode());
            } catch (InterruptedException e) {
                return true;
            } catch (ExecutionException e) {
                return true;
            }
        }).collect(Collectors.toList());

        //endregion

        //region 校验结果
        if(failFutures.size()>0){
            Future<Result> resultFuture = failFutures.get(0);
            Result result = null;
            try {
                result = resultFuture.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            CustomerInfoPushMain main = new CustomerInfoPushMain();
            main.setId(customerInfoPushMain.getId());
            main.setmStatus(3);
            customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage(result.getMessage());
        }else{
            CustomerInfoPushMain main = new CustomerInfoPushMain();
            main.setId(customerInfoPushMain.getId());
            main.setmStatus(2);
            customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
        }
        //endregion

        //region push mq
        producter.send("Marketing.Push.CustomerService",customerInfoPushMain.getId());
        //endregion

        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<Boolean> getCustomerStatus(Long mId) {

        CustomerInfoPushMain main = customerInfoPushMainMapper.selectByPrimaryKey(mId);

        CustomerInfoPushLogExample logExample = new CustomerInfoPushLogExample();
        logExample.createCriteria().andMIdEqualTo(mId).andRealStautsIn(Arrays.asList(StatusConstants.CustomerService_query,StatusConstants.CustomerService_updateing));
        List<CustomerInfoPushLog> customerInfoPushLogs = customerInfoPushLogMapper.selectByExample(logExample);
        Boolean isContinue = false;
        for (CustomerInfoPushLog t : customerInfoPushLogs) {
            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(main.getmApiCode());
            pushMarketingUserDTO.setPlatApiCode("");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("method","uploadResult");
            jsonObject.put("accessNumber",t.getBatch());
            pushMarketingUserDTO.setJsonData(jsonObject.toJSONString());
            Result userStatus = intelligentCustomerServiceClient.getUserStatus(pushMarketingUserDTO);
            if(ResultCode.SUCCESS.getValue().equals(userStatus.getCode())){
                CustomerInfoPushLog updateLog = new CustomerInfoPushLog();
                updateLog.setId(t.getId());
                if("UPLOAD".equals(userStatus.getData())){
                    updateLog.setRealStauts(StatusConstants.CustomerService_success);
                    customerInfoPushLogMapper.updateByPrimaryKeySelective(updateLog);
                }else if("RUNING".equals(userStatus.getData())){
                    updateLog.setRealStauts(StatusConstants.CustomerService_updateing);
                    customerInfoPushLogMapper.updateByPrimaryKeySelective(updateLog);
                    isContinue=true;
                }else if("ERROR".equals(userStatus.getData())){
                    updateLog.setRealStauts(StatusConstants.CustomerService_error);
                    customerInfoPushLogMapper.updateByPrimaryKeySelective(updateLog);
                }
            }else{
                isContinue = true;
            }
        }
        return new Result<Boolean>().setDate(isContinue);
    }
}
