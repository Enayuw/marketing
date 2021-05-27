package com.br.marketing.service.Impl;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.google.common.collect.Lists;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import java.util.ArrayList;
import java.util.Date;

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
import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.MarketingStrategyProduct;
import com.br.marketing.entity.MarketingStrategyProductExample;
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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
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
        //todo 调用es查询接口
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        List<Callable<Result>> listCall = new ArrayList<>();
        list.forEach(k->{
            listCall.add(()->{

                List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
                for (int i = 0; i < 2000; i++) {
                    PushMarketingUserDetailDTO dto1 = new PushMarketingUserDetailDTO();
                    dto1.setCaseNumber(customerInfoPushMain.getId().toString()+"_"+k.toString()+i);
                    dto1.setPhone("123");
                    dto1.setVariables("123");
                    dto1.setScore("12");
                    dto1.setScoreDate("2021-05-33");
                    dto1.setScoreName("hehe");
                    dto1.setUpload("1");

                    userDetailDTOS.add(dto1);
                }
                PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
                pushMarketingUserTaskInfoDTO.setMethod("caseAdd");
                pushMarketingUserTaskInfoDTO.setBatchNumber(customerInfoPushMain.getId().toString());
//                pushMarketingUserTaskInfoDTO.setStrategyCode("");
//                pushMarketingUserTaskInfoDTO.setIsAutoRunStrategy("");
                pushMarketingUserTaskInfoDTO.setAccessNumber(customerInfoPushMain.getId()+"_"+k.toString());
//                pushMarketingUserTaskInfoDTO.setExtendData("");
                pushMarketingUserTaskInfoDTO.setScoreName(dto.getProductName());
                pushMarketingUserTaskInfoDTO.setScoreRange(dto.getMinScore().toString().concat(",").concat(dto.getMaxScore().toString()));
                pushMarketingUserTaskInfoDTO.setAmountTop(Convert.toStr(dto.getMaxTop() - dto.getMinTop()));
                pushMarketingUserTaskInfoDTO.setSampleTotal(k.toString());
                pushMarketingUserTaskInfoDTO.setData(userDetailDTOS);

                PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
                pushMarketingUserDTO.setApiCode(dto.getApiCode());
                pushMarketingUserDTO.setPlatApiCode(dto.getApiCode());
                pushMarketingUserDTO.setJsonData(JSON.toJSONString(pushMarketingUserTaskInfoDTO));

                return intelligentCustomerServiceClient.pushUser(pushMarketingUserDTO,customerInfoPushMain.getId(),pushMarketingUserTaskInfoDTO.getAccessNumber());
            });
        });

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
}
