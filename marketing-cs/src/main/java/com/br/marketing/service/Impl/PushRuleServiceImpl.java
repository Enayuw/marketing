package com.br.marketing.service.Impl;
import java.util.Date;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.util.DateUtils;
import com.br.common.util.StringUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.SendMailClint;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.commonentity.StatusConstants;
import com.br.marketing.dto.*;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.Product;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.vo.MarketingPreUserSyncDetailVO;
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

import javax.validation.Valid;
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
    @Qualifier("currentDbpool")
    ThreadPoolExecutor currentDbPoolExecutor;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Autowired
    MarketingHistoryEsServiceImpl marketingHistoryEsService;

    @Autowired
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    MarketingSyncErrorInfoMapper marketingSyncErrorInfoMapper;

    @Autowired
    AlarmApiClient alarmApiClient;

    final static Integer  errorIdMark = 1;

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
            long l = System.currentTimeMillis();
            marketingUserMapper.insertBatchMarketingPreUser(dto.getApiCode(), dto.getJsonData().getTaskId()
                    , DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"), dto.getJsonData().getDataItems());
            System.out.println("耗时"+(System.currentTimeMillis()-l));
//            StringBuilder sqlSb = new StringBuilder();
//            for (int i = 0; i < dto.getJsonData().getDataItems().size(); i++) {
//                MarketingPreUserDetailDTO t = dto.getJsonData().getDataItems().get(i);
//                String date = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
//                sqlSb.append(String.format("( '%s','%s','%s','%s','%s' ,'%s' ,'%s' ,'%s' ,'%s' ,'%s')",dto.getApiCode()
//                        ,dto.getJsonData().getTaskId(),t.getCaseNum(),t.getCell(),t.getGroupType(),t.getRegisterDate()
//                        ,t.getReserveField1(),t.getReserveField2(),date,date)
//                        .concat((i==dto.getJsonData().getDataItems().size()-1)?"":","));
////                String dataStr = String.format("( '%s','%s','%s','%s','%s' ,'%s' ,'%s' ,'%s' ,'%s' ,'%s')", dto.getApiCode()
////                        , dto.getJsonData().getTaskId(), t.getCaseNum(), t.getCell(), t.getGroupType(), t.getRegisterDate()
////                        , t.getReserveField1(), t.getReserveField2(), date, date);
////                marketingUserMapper.insertBatchMarketingPreUserByDatas(dto.getApiCode(),dataStr);
//            }
//            System.out.println("拼接耗时"+(System.currentTimeMillis()-l));
//            marketingUserMapper.insertBatchMarketingPreUserByDatas(dto.getApiCode(), sqlSb.toString());
//            System.out.println("耗时"+(System.currentTimeMillis()-l));
        }catch (Exception ex){
            if(ex.getMessage().contains("IDX_taskId_custNum")){
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("请核实下该批次内有重复的客户编号");
            }else{
                throw ex;
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }


    @Override
    public Result insertMarketingPreUserText(RequestCommonDTO<MarketingPreUserDTO> dto) {
        //region check
        long l1 = System.currentTimeMillis();
        if(!StringUtils.isNotBlank(dto.getApiCode())){
            throw new ParamValidErrorException("apiCode必传");
        }
        if(dto.getJsonData() == null){
            throw new ParamValidErrorException("jsonData必传");
        }
        if(!StringUtils.isNotBlank(dto.getJsonData().getTaskId())){
            throw new ParamValidErrorException("taskid必传");
        }
        if(!StringUtils.isNotBlank(dto.getJsonData().getRequestId())){
            throw new ParamValidErrorException("requestId必传");
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
        System.out.println("check耗时"+(System.currentTimeMillis()-l1));
        //endregion

        try {
            long l = System.currentTimeMillis();
            String s = JSON.toJSONString(dto.getJsonData());
            System.out.println("拼接耗时"+(System.currentTimeMillis()-l));
            String format = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            MarketingSyncInfo syncInfo = new MarketingSyncInfo();
            syncInfo.setApiCode(dto.getApiCode());
            syncInfo.setCusBatch(dto.getJsonData().getTaskId());
            syncInfo.setRequestBatch(dto.getJsonData().getRequestId());
            syncInfo.setCreateTime(new Date());
            syncInfo.setJsonData(s);
            marketingUserMapper.insertMarketingPreUserByText(syncInfo);
            producter.send("Marketing.PreUser.Receive",syncInfo.getId().toString());
            System.out.println("插入耗时"+(System.currentTimeMillis()-l));
        }catch (Exception ex){
            if(ex.getMessage().contains("IDX_taskId_custNum")){
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("请核实下该批次内有重复的客户编号");
            }else{
                throw ex;
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }

    @Override
    public Result<Boolean> insertMarketingPreUserSync(Long infoId) {
        Boolean isContinue = false;
        MarketingSyncInfo marketingSyncInfo = marketingSyncInfoMapper.selectByPrimaryKey(infoId);
        MarketingPreUserDTO dto = JSON.parseObject(marketingSyncInfo.getJsonData(), new TypeReference<MarketingPreUserDTO>() {
        }.getType());
        long l = System.currentTimeMillis();
        List<Future<Result>> results = new ArrayList<>();
        ArrayList<Callable<Result>> list = new ArrayList<>();
            for (int i = 0; i < dto.getDataItems().size(); i++) {
                String date = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                MarketingPreUserDetailDTO marketingPreUserDetailDTO = dto.getDataItems().get(i);
                                String dataStr = String.format("( '%s','%s','%s','%s','%s' ,'%s' ,'%s' ,'%s' ,'%s' ,'%s','%s')"
                                        , marketingSyncInfo.getApiCode(), marketingSyncInfo.getCusBatch()
                                        ,marketingSyncInfo.getRequestBatch(), marketingPreUserDetailDTO.getCaseNum()
                                        , marketingPreUserDetailDTO.getCell(), marketingPreUserDetailDTO.getGroupType()
                                        , marketingPreUserDetailDTO.getRegisterDate()
                                        , marketingPreUserDetailDTO.getReserveField1()
                                        , marketingPreUserDetailDTO.getReserveField2()
                                        , date, date);
//                    results.add(currentDbPoolExecutor.submit(() -> {
//                        try {
//                            marketingUserMapper.insertBatchMarketingPreUserByDatas(marketingSyncInfo.getApiCode(), dataStr);
//                        } catch (Exception ex) {
//                            if (ex.getMessage().contains("IDX_taskId_custNum")) {
//                                return new Result().setCode(ResultCode.FAIL.getValue()).setDate(marketingPreUserDetailDTO.getCaseNum());
//                            } else {
//                                throw ex;
//                            }
//                        }
//                        return new Result().setCode(ResultCode.SUCCESS.getValue());
//                    }));
                list.add(() -> {
                    try {
                        marketingUserMapper.insertBatchMarketingPreUserByDatas(marketingSyncInfo.getApiCode(), dataStr);
                    } catch (Exception ex) {
                        if (ex.getMessage().contains("IDX_taskId_custNum")) {
                            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(marketingPreUserDetailDTO.getCaseNum());
                        } else {
                            throw ex;
                        }
                    }
                    return new Result().setCode(ResultCode.SUCCESS.getValue());
                });
            }
            StringBuilder errorBuild = new StringBuilder();
            Integer errorSize = 0;
//            for (int i = 0; i < results.size(); i++) {
//                Result result = results.get(i).get();
//                if(ResultCode.FAIL.getValue().equals(result.getCode())){
//                    errorSize++;
//                    errorBuild.append(result.getMessage().concat(","));
//                }
//            }
        List<Future<Result>> futures = null;
        try {
            futures = currentDbPoolExecutor.invokeAll(list);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < futures.size(); i++) {
            Result result = null;
            try {
                result = futures.get(i).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(ResultCode.FAIL.getValue().equals(result.getCode())){
                    errorSize++;
                    errorBuild.append(result.getMessage().concat(","));
                }
            }
            if(StringUtils.isNotBlank(errorBuild.toString())){
                errorBuild.append("以上客户编号重复");
            }
            MarketingSyncInfo updateSyncInfo = new MarketingSyncInfo();
            updateSyncInfo.setId(marketingSyncInfo.getId());
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_running);
            if(errorSize==0){
                updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success);
            }else if(errorSize==futures.size()){
                updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_fail);
                updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success_part);
                MarketingSyncErrorInfo errorInfo = new MarketingSyncErrorInfo();
                errorInfo.setApiCode(marketingSyncInfo.getApiCode());
                errorInfo.setCusBatch(marketingSyncInfo.getCusBatch());
                errorInfo.setRequestBatch(marketingSyncInfo.getRequestBatch());
                errorInfo.setCreateTime(new Date());
                errorInfo.setErrorInfo(errorBuild.toString());
                marketingSyncErrorInfoMapper.insertMarketingSigle(errorInfo);
                updateSyncInfo.setErrorId(errorInfo.getId());
            }else if(errorSize<futures.size()){
                updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success_part);
                MarketingSyncErrorInfo errorInfo = new MarketingSyncErrorInfo();
                errorInfo.setApiCode(marketingSyncInfo.getApiCode());
                errorInfo.setCusBatch(marketingSyncInfo.getCusBatch());
                errorInfo.setRequestBatch(marketingSyncInfo.getRequestBatch());
                errorInfo.setCreateTime(new Date());
                errorInfo.setErrorInfo(errorBuild.toString());
                marketingSyncErrorInfoMapper.insertMarketingSigle(errorInfo);
                updateSyncInfo.setErrorId(errorInfo.getId());
            }
            marketingSyncInfoMapper.updateByPrimaryKeySelective(updateSyncInfo);
//            alarmApiClient.sendAlarm();
            System.out.println("耗时"+(System.currentTimeMillis()-l));
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue).setMessage("成功");
    }

    @Override
    public Result<MarketingPreUserSyncDetailVO> getMarketingPreUserSyncStatus(MarketingPreUserSyncStatusDTO dto) {
        MarketingPreUserSyncDetailVO vo = new MarketingPreUserSyncDetailVO();
        MarketingSyncInfoExample syncInfoExample = new MarketingSyncInfoExample();
        syncInfoExample.createCriteria().andApiCodeEqualTo(dto.getApiCode()).andCusBatchEqualTo(dto.getTaskId())
                .andRequestBatchEqualTo(dto.getRequestId());
        List<MarketingSyncInfo> marketingSyncInfos = marketingSyncInfoMapper.selectByExample(syncInfoExample);
        if(marketingSyncInfos.size()<=0){
            return new Result<MarketingPreUserSyncDetailVO>().setCode(ResultCode.FAIL.getValue()).setMessage("该批次信息不存在");
        }
        MarketingSyncInfo syncInfo = marketingSyncInfos.get(0);
        vo.setApiCode(syncInfo.getApiCode());
        vo.setTaskId(syncInfo.getCusBatch());
        vo.setRequestId(syncInfo.getRequestBatch());
        vo.setStatus(syncInfo.getStatus());

        if(StatusConstants.MarketingPreUserStatus_fail.equals(syncInfo.getStatus())
        ||StatusConstants.MarketingPreUserStatus_success_part.equals(syncInfo.getStatus())) {
            MarketingSyncErrorInfo errorInfo = marketingSyncErrorInfoMapper.selectByPrimaryKey(syncInfo.getErrorId());
            if(errorInfo != null){
                vo.setErrorInfo(errorInfo.getErrorInfo());
            }
        }
        return new Result<MarketingPreUserSyncDetailVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(vo);
    }
}
