package com.br.marketing.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.ErrorMark;
import com.br.marketing.entity.ErrorMarkExample;
import com.br.marketing.enums.*;
import com.br.marketing.mapper.ErrorMarkMapper;
import com.br.marketing.service.ToPolicyByRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ToPolicyByRuleServiceImpl
 * @Description 规则中心推决策相关
 * @Author kongbx
 * @Date 2025/1/12 14:00
 */
@Service
public class ToPolicyByRuleServiceImpl implements ToPolicyByRuleService {
    private static final Logger log = LoggerFactory.getLogger(PushRuleServiceImpl.class);

    @Resource
    ErrorMarkMapper errorMarkMapper;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    /**
     * 查询补推数据的最终状态
     * @param id
     * @param filterType:任务类型 0 通用推决策, 1 携程撞库结果推决策
     * @return
     */
    @Override
    public Integer queryExistError(Long id,Integer filterType) {
        List<Integer> retryTotalAttemptsList = errorMarkMapper.queryRetryTotalAttempts(id,
                RetryStatusEnum.AWAIT_COMPLETE.getValue(),
                filterType);

        if(!CollectionUtils.isEmpty(retryTotalAttemptsList)){
            // 判断是否都已补推3次
            boolean allGreaterOrEqualThree = retryTotalAttemptsList.stream()
                    .allMatch(retryAttempts -> retryAttempts >= 3);
            if(allGreaterOrEqualThree){
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode()
                        , "规则中心推决策，重试3次失败 mid:" + id));
                return PushRuleStatusEnum.PUSH_FAIL.getValue();
            }else {
                return PushRuleStatusEnum.EXCEPTIONS_TO_REFILLED.getValue();
            }
        }else{
            return PushRuleStatusEnum.TO_BE_CONFIRMED.getValue();
        }
    }


    @Override
    public void makeUpPolicyData(CustomerInfoPushMain customerInfoPushMain, String switchType) {
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5, 50);

        // 模拟推决策异常
        boolean b = mockSwitch(customerInfoPushMain.getmApiCode(), switchType, MockSwitchEnum.POLICYRETRY.getValue());

        Long minId = null;
        boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            ErrorMarkExample errorMarkExample = new ErrorMarkExample();
            errorMarkExample.setOrderByClause(" id limit 2000");

            ErrorMarkExample.Criteria criteria = errorMarkExample.createCriteria().andMIdEqualTo(customerInfoPushMain.getId())
                    .andRetryStatusEqualTo(RetryStatusEnum.AWAIT_COMPLETE.getValue())
                    .andTypeEqualTo(ErrorMarkTypeEnum.POLICY_ERROR.getValue())
                    .andRetryTotalAttemptsLessThan(3);

            if (minId != null) {
                criteria.andIdGreaterThan(minId);
            }
            List<ErrorMark> policyErrorList = errorMarkMapper.selectByExample(errorMarkExample);
            if (CollectionUtil.isEmpty(policyErrorList)) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = policyErrorList.get(policyErrorList.size() - 1).getId();

            for (ErrorMark errorMark : policyErrorList) {
                threadPool.submit(() -> rePushPolicyData(errorMark,b));
            }

        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("补推决策线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), "补推决策线程池关闭！异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public List<List<PushMarketingUserDetailDTO>> splitParam(String apiCode, List<PushMarketingUserDetailDTO> userDetailDTOS) {
        List<List<PushMarketingUserDetailDTO>> partition = new ArrayList<>();
        HashMap<String, Integer> toPolicyParamSize = marketingCommonConfig.getToPolicyParamSize();
        Integer paramSize = toPolicyParamSize.get(apiCode);
        if(paramSize != null){
            partition = Lists.partition(userDetailDTOS, paramSize);
        }else {
            partition.add(userDetailDTOS);
        }
        return partition;
    }

    private Result<Integer> rePushPolicyData(ErrorMark errorMark, boolean b) {
        Result<Integer> result = new Result<>();
        PushMarketingUserDTO pushMarketingUserDTO = JSON.parseObject(errorMark.getPolicyCondition(), new TypeReference<PushMarketingUserDTO>() {
        }.getType());

        if(b){
            result.setCode(ResultCode.TIME_OUT.getValue());
        }else {
            result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, errorMark.getmId(),
                    errorMark.getAccessNumber(), errorMark.getPushSize());
            if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, errorMark.getmId(),
                        errorMark.getAccessNumber(), errorMark.getPushSize());
            }
        }

        ErrorMark errorMark1 = new ErrorMark();
        errorMark1.setId(errorMark.getId());
        if (ResultCode.TIME_OUT.getValue().equals(result.getCode())
                || ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
            int retryAttempts = errorMark.getRetryTotalAttempts();
            errorMark1.setRetryTotalAttempts(retryAttempts + 1);
            errorMark1.setUpdateTime(new Date());
            errorMarkMapper.updateByPrimaryKeySelective(errorMark1);
        } else if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            errorMark1.setRetryStatus(RetryStatusEnum.PUSH_COMPLETE.getValue());
            errorMark1.setUpdateTime(new Date());
            errorMarkMapper.updateByPrimaryKeySelective(errorMark1);
        }
        return result;
    }

    /**
     * 推送决策挡板开关
     * @param apiCode
     * @param switchType
     * @param errorType
     * @return
     */
    @Override
    public boolean mockSwitch(String apiCode, String switchType, String errorType) {
        boolean o = Boolean.FALSE;
        HashMap<String, JSONObject> policyRetrySwitch = marketingCommonConfig.getPolicyRetrySwitch();
        JSONObject jsonObject = policyRetrySwitch.get(apiCode);
        if(jsonObject != null){
            o = jsonObject.getJSONObject(switchType).getBooleanValue(errorType);
        }
        return o;
    }

}
