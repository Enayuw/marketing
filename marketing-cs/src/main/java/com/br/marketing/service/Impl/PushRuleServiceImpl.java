package com.br.marketing.service.Impl;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.common.util.StringUtils;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.StatusConstants;
import com.br.marketing.dto.*;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.Product;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.*;

import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.RabbitMqSenderUtils;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
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

    @Autowired
    MarketingUserMapper marketingUserMapper;

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
        productExample.createCriteria().andApiCodeEqualTo(dto.getApiCode()).andBatchNumberIn(dto.getBatchNumberList())
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
        queryBaseBean.setBatchNumbers(Joiner.on(",").join(dto.getBatchNumberList()));
        queryBaseBean.setModelCode(dto.getProductName());
        queryBaseBean.setModelVersion(dto.getProductVersion());
        queryBaseBean.setScoreRange(dto.getMinScore().toString().concat(",").concat(dto.getMaxScore().toString()));
        queryBaseBean.setAmountTop(dto.getMinTop().toString().concat(",").concat(dto.getMaxTop().toString()));
        int total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        if(total<=0){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合的数据");
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

        marketingStrategyProducts.forEach(t->{
            CustomerInfoPushBatch customerInfoPushBatch = new CustomerInfoPushBatch();
            customerInfoPushBatch.setmId(customerInfoPushMain.getId());
            customerInfoPushBatch.setmApiCode(dto.getApiCode());
            customerInfoPushBatch.setmBatchNumber(t.getCusBatchNumber());
            customerInfoPushBatch.setCreateTime(date);
            customerInfoPushBatch.setUpdateTime(date);
            customerInfoPushBatchMapper.insertSelective(customerInfoPushBatch);
        });
        //endregion

        //region push Intelligent Customer Service

        //调用es查询接口
        Integer minTop = dto.getMinTop();
        int startPageYushu = minTop % 10000;
        Integer startPage = minTop/10000+(startPageYushu >0?1:0);
        String searchAfterStr = "";
        for (int i = 1; i <=startPage; i++) {

            if(i==startPage&&startPageYushu>0){
                queryBaseBean.setPageSize(startPageYushu);
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
        int totalYuShu = total % 2000;
        int totalPage = total / 2000 + (totalYuShu > 0 ? 1 : 0);
        List<Callable<Result>> listCall = new ArrayList<>();
        for (int i = 1; i <= totalPage; i++) {
            String sn = String.valueOf(i);
            queryBaseBean.setSearchAfter(searchAfterStr);
            List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);

            List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
            for (int k = 0; k < marketingHistories.size(); k++) {
                MarketingHistory marketingHistory = marketingHistories.get(k);

                //人员信息
                PushMarketingUserDetailDTO dto1 = new PushMarketingUserDetailDTO();
//                dto1.setCaseNumber("test_202106020100".concat("_").concat(String.valueOf(System.currentTimeMillis())));
                dto1.setCaseNumber(marketingHistory.getCusNum().concat("_").concat(marketingHistory.getCusBatchNumber()).concat("_").concat(String.valueOf(System.currentTimeMillis())));
                dto1.setPhone(marketingHistory.getCell());
                Optional<Product> first = marketingHistory.getProduct().stream().filter(t -> customerInfoPushMain.getmModel().equals(t.getCode())
                        && customerInfoPushMain.getmModelVersion().equals(t.getVersion())).findFirst();

                //人员的变量信息
                PushMarketingUserDetailVariablesDTO pushMarketingUserDetailVariablesDTO = new PushMarketingUserDetailVariablesDTO();
                pushMarketingUserDetailVariablesDTO.setScoreDate(marketingHistory.getRequestTime()==null?"":(DateUtils.format(marketingHistory.getRequestTime(),"yyyy-MM-dd")));
                pushMarketingUserDetailVariablesDTO.setScoreName(customerInfoPushMain.getmModel());
                pushMarketingUserDetailVariablesDTO.setUpdate("");
                if(first.isPresent()){
                    pushMarketingUserDetailVariablesDTO.setScore(String.valueOf(first.get().getScore()));
                }
                dto1.setVariables(pushMarketingUserDetailVariablesDTO);
                userDetailDTOS.add(dto1);
            }

            //推送任务基础信息
            PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
            pushMarketingUserTaskInfoDTO.setMethod("caseAdd");
            pushMarketingUserTaskInfoDTO.setBatchNumber(customerInfoPushMain.getId().toString());
//                pushMarketingUserTaskInfoDTO.setStrategyCode("");
            pushMarketingUserTaskInfoDTO.setAccessNumber(customerInfoPushMain.getId()+"_"+ sn);
            PushMarketingExtendDataDTO extendDataDTO = new PushMarketingExtendDataDTO();
            extendDataDTO.setScoreName(dto.getProductName());
            extendDataDTO.setScoreRange(dto.getMinScore().toString().concat(",").concat(dto.getMaxScore().toString()));
            extendDataDTO.setAmountTop(Convert.toStr(dto.getMaxTop() - dto.getMinTop()));
            extendDataDTO.setSampleTotal(sn);
            pushMarketingUserTaskInfoDTO.setExtendData(extendDataDTO);
            pushMarketingUserTaskInfoDTO.setData(userDetailDTOS);

            //传输参数信息
            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(dto.getApiCode());
            pushMarketingUserDTO.setPlatApiCode(dto.getApiCode());
            pushMarketingUserDTO.setJsonData(pushMarketingUserTaskInfoDTO);

            listCall.add(()->{return intelligentCustomerServiceClient.pushUser(pushMarketingUserDTO,customerInfoPushMain.getId(),pushMarketingUserTaskInfoDTO.getAccessNumber());});
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
        producter.send("Marketing.Push.CustomerService",customerInfoPushMain.getId().toString());
        //endregion

        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<Boolean> getCustomerStatus(Long mId) {

        CustomerInfoPushMain main = customerInfoPushMainMapper.selectByPrimaryKey(mId);

        CustomerInfoPushLogExample logExample = new CustomerInfoPushLogExample();
        logExample.createCriteria().andMIdEqualTo(mId).andRealStautsIn(Arrays.asList("1","900013"));
        List<CustomerInfoPushLog> customerInfoPushLogs = customerInfoPushLogMapper.selectByExample(logExample);
        Boolean isContinue = false;
        for (CustomerInfoPushLog t : customerInfoPushLogs) {
            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(main.getmApiCode());
            pushMarketingUserDTO.setPlatApiCode("");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("method","uploadResult");
            jsonObject.put("accessNumber",t.getBatch());
            pushMarketingUserDTO.setJsonData(jsonObject);
            Result<String> userStatus = intelligentCustomerServiceClient.getUserStatus(pushMarketingUserDTO);
            if(ResultCode.SUCCESS.getValue().equals(userStatus.getCode())){
                CustomerInfoPushLog updateLog = new CustomerInfoPushLog();
                updateLog.setId(t.getId());
                if("900013".equals(userStatus.getData())){
                    isContinue=true;
                }
                updateLog.setRealStauts(userStatus.getData());
                customerInfoPushLogMapper.updateByPrimaryKeySelective(updateLog);
            }else{
                isContinue = true;
            }
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue);
    }

    @Override
    public Result insertMarketingPreUser(RequestCommonDTO<MarketingPreUserDTO> dto) {
        //region check
        if(!StringUtils.isNotBlank(dto.getApiCode())){
            throw new ParamValidErrorException("apiCode必传");
        }
        if(dto.getJsonData() == null){
            throw new ParamValidErrorException("jsonData必传");
        }
        if(!StringUtils.isNotBlank(dto.getJsonData().getTaskId())){
            throw new ParamValidErrorException("taskid必传");
        }
        boolean checkJson = dto.getJsonData().getDataItems().stream().anyMatch(t -> !StringUtils.isNotBlank(t.getCaseNum())
                || !StringUtils.isNotBlank(t.getCell()) || !StringUtils.isNotBlank(t.getGroupType()));
        if(checkJson){
            throw new ParamValidErrorException("有用户数据的cell或caseNum或groupType没有传输");
        }
        int size = dto.getJsonData().getDataItems().size();
        if(size>2000){
            throw new ParamValidErrorException("传输的数据不要超过2000条");
        }
        //endregion

        try {
            marketingUserMapper.insertBatchMarketingPreUser(dto.getApiCode(), dto.getJsonData().getTaskId()
                    , DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"), dto.getJsonData().getDataItems());
        }catch (Exception ex){
            if(ex.getMessage().contains("IDX_taskId_custNum")){
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("请核实下该批次内有重复的客户编号");
            }else{
                throw ex;
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }
}
