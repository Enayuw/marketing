package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.check.service.PushCallBackService;
import com.br.marketing.check.service.PushCustomerService;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.enums.CallBackScoreResourceEnum;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.PushCustomerDetailMapper;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.scorepushcustomer.ScoreSortJsonVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class PushCallBackByPolicyServiceImpl implements PushCallBackService {

    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    PushCustomerDetailMapper pushCustomerDetailMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    PushRuleService pushRuleService;

    @Autowired
    PushCustomerService pushCustomerService;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Override
    public void pushCustomer(StraHisFile straHisFile, List<ScoreSortJsonVO> vos, AtomicInteger error, ScorePushCustomerConfig pushCustomerConfig) {

        Result<Integer> integerResult = pushRuleService.checkThreekEnc(Arrays.asList(straHisFile.getId()));
        if (!ResultCode.SUCCESS.getValue().equals(integerResult.getCode())) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode()
                    , String.format("该推送不符合推送决策的限制条件 跑批id：【%s】,原因：【%s】", straHisFile.getId(), integerResult.getMessage())));
            return;
        }
        Integer threeEncrypt = integerResult.getData();
        int pushThream = pushCustomerService.getPushCustomerResource(pushCustomerConfig, CallBackScoreResourceEnum.PushCustomerThreadNumber);
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(pushThream, pushThream, "job_pushCustomer");
        Integer dataPageSize = pushCustomerService.getPushCustomerResource(pushCustomerConfig, CallBackScoreResourceEnum.PushCustomerDataPageNumber);
        Boolean dataAction = Boolean.TRUE;
        Long minId = null;
        String batchNumber = new SimpleDateFormat("yyyyMMdd").format(straHisFile.getCreateTime()).concat("_a_").concat(straHisFile.getApiCode());
        Integer pageIndex = 1;
        while (dataAction) {
            PushCustomerDetailExample pushCustomerDetailExample = new PushCustomerDetailExample();
            pushCustomerDetailExample.setOrderByClause(String.format(" id limit %d", dataPageSize));
            PushCustomerDetailExample.Criteria criteria = pushCustomerDetailExample.createCriteria();
            criteria.andFileIdEqualTo(straHisFile.getId()).andPushStatusIn(Arrays.asList(1, 3));
            if (minId != null) {
                criteria.andIdGreaterThan(minId);
            }
            List<PushCustomerDetail> pushCustomerDetails = pushCustomerDetailMapper.selectByExample(pushCustomerDetailExample);
            if (pushCustomerDetails.size() <= 0) {
                dataAction = Boolean.FALSE;
                continue;
            }
            minId = pushCustomerDetails.get(pushCustomerDetails.size() - 1).getId();
            Integer pageThread = pageIndex;
            pushPool.submit(() -> {
                try {
                    List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
                    List<Long> detailIds = new ArrayList<>();
                    for (int k = 0; k < pushCustomerDetails.size(); k++) {
                        PushCustomerDetail detail = pushCustomerDetails.get(k);
                        detailIds.add(detail.getId());
                        //人员信息
                        PushMarketingUserDetailDTO dto1 = new PushMarketingUserDetailDTO();
                        dto1.setCaseNumber(detail.getCustNum());
                        dto1.setPhone(pushRuleService.encrypt3k(threeEncrypt, BrCipherMaker.getInstance().decode(detail.getCell())));
                        JSONObject varObject = null;
                        try {
                            varObject = JSON.parseObject(detail.getPushJson());
                        } catch (Exception ex) {
                            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), ex.getMessage()), ex);
                            varObject = new JSONObject();
                        }
                        varObject.put("orderId", detail.getCustNum());
                        varObject.put("taskId", detail.getTaskId());
                        varObject.put("userType", detail.getUserType());
                        for (ScoreSortJsonVO vo : vos) {
                            varObject.put(vo.getMappingKey(), getScoreSortByDb(vo.getDbNumber(), detail));
                        }
                        dto1.setVariables(varObject);
                        userDetailDTOS.add(dto1);
                    }

                    //推送任务基础信息
                    PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
                    pushMarketingUserTaskInfoDTO.setMethod("caseAdd");
                    pushMarketingUserTaskInfoDTO.setBatchNumber(batchNumber);
                    pushMarketingUserTaskInfoDTO.setAccessNumber(straHisFile.getId().toString().concat("_")
                            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + pageThread);
                    pushMarketingUserTaskInfoDTO.setData(userDetailDTOS);
                    //传输参数信息
                    PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
                    pushMarketingUserDTO.setApiCode(straHisFile.getApiCode());
                    pushMarketingUserDTO.setJsonData(pushMarketingUserTaskInfoDTO);
                    PushCustomerDetailExample example = new PushCustomerDetailExample();
                    example.createCriteria().andIdIn(detailIds);
                    PushCustomerDetail update = new PushCustomerDetail();

                    try {
                        //pushCustomerService.mockError("3");
                        Result result = intelligentCustomerServiceClient.pushUser(pushMarketingUserDTO);

                        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            update.setPushStatus(2);
                        } else {
                            update.setPushStatus(3);
                            error.incrementAndGet();
                        }
                    }catch (Exception ex){
                        update.setPushStatus(3);
                        error.incrementAndGet();
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), "推送决策接口异常!"), ex);
                    }
                    pushCustomerDetailMapper.updateByExampleSelective(update, example);
                    //endregion
                } catch (Exception e) {
                    error.incrementAndGet();
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), "推送客户线程报错!"), e);
                }
            });
            pageIndex++;
        }
        try {
            waitThreadPool(pushPool);
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), ex.getMessage()), ex);
        }
    }
}
