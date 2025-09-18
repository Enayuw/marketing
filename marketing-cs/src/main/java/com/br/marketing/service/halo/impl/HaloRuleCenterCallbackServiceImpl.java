package com.br.marketing.service.halo.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.halo.HaluoAiApiServiceClient;
import com.br.marketing.client.halo.input.ReqHaluoApiDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.ErrorMarkTypeEnum;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.enums.RetryStatusEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.halo.HaloRuleCenterCallbackService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @ClassName HaloRuleCenterCallbackServiceImpl
 * @Author hang.zhou
 * @Date 2025/9/17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class HaloRuleCenterCallbackServiceImpl implements HaloRuleCenterCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(HaloRuleCenterCallbackServiceImpl.class);

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    ErrorMarkMapper errorMarkMapper;

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private HaluoAiApiServiceClient haluoAiApiServiceClient;

    @Resource
    private MarketingRuleCenterHaloCallbackDataMapper marketingRuleCenterHaloCallbackDataMapper;

    @Override
    public Result saveHaloCallbackTask(PushCustomerDTO dto) {
        if (dto.getBatchNumberList().size() > 50) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("批次最多选择50个");
        }
        if (dto.getmPlanNum() != null && dto.getmPlanNum() <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送数量不能小于等于0");
        }
        if (dto.getmPercentage() != null && dto.getmPercentage().compareTo(new BigDecimal(0)) <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("百分比不能小于等于0");
        }
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria().andIdIn(dto.getFileIdList());
        List<StraHisFile> files = straHisFileMapper.selectByExample(fileExample);

        Integer pushNum = dto.getmPrePlanNum();
        //region insert db
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(dto.getFileIdList());
        CustomerInfoPushMain customerInfoPushMain = new CustomerInfoPushMain();

        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        List<String> showTitles = straHisFiles.stream().map(t -> t.getBatchNumber()).collect(Collectors.toList());
        customerInfoPushMain.setmApiCode(dto.getApiCode());
        customerInfoPushMain.setmRuleCondition(dto.getmRuleCondition());
        customerInfoPushMain.setmRuleConditionShow(dto.getmRuleConditionShow());
        customerInfoPushMain.setmScoreCondition(dto.getmScoreCondition());
        customerInfoPushMain.setmPercentage(dto.getmPercentage());
        customerInfoPushMain.setmPlanNum(dto.getmPlanNum());
        customerInfoPushMain.setmRealyNum(pushNum);
        Date date = new Date();
        customerInfoPushMain.setCreateTime(date);
        customerInfoPushMain.setUpdateTime(date);
        customerInfoPushMain.setmCusBatchNumberList(Joiner.on(",").join(showTitles));
        customerInfoPushMain.setmStatus(PushRuleStatusEnum.TO_BE_RUNNING.getValue());
        customerInfoPushMain.setOptUserId(String.valueOf(dto.getUserDetail().getId()));
        customerInfoPushMain.setOptUserName(dto.getUserDetail().getRealName());
        customerInfoPushMain.setLabelName(dto.getLabelName());
        customerInfoPushMain.setPushTarget(dto.getPushTarget());
        customerInfoPushMain.setFilterType(3);
        customerInfoPushMainMapper.insertSelective(customerInfoPushMain);
        //数据集名称更新
        String batchName;
        if (StringUtils.isNotEmpty(dto.getBatchName())) {
            batchName = dto.getBatchName();
        } else {  //默认名称
            if (StringUtils.isNotEmpty(dto.getRuleModelName())) {
                batchName = LocalDate.now().toString().concat("-").concat(dto.getRuleModelName()).concat("-").concat(LocalTime.now().withNano(0)
                        .toString());
            } else {
                batchName = LocalDate.now().toString().concat("-").concat(customerInfoPushMain.getId().toString()).concat("-").
                        concat(LocalTime.now().withNano(0).toString());
            }
        }
        CustomerInfoPushMain updatePushMain = new CustomerInfoPushMain();
        updatePushMain.setId(customerInfoPushMain.getId());
        updatePushMain.setBatchName(batchName);
        customerInfoPushMainMapper.updateByPrimaryKeySelective(updatePushMain);
        files.forEach(t -> {
            CustomerInfoPushBatch customerInfoPushBatch = new CustomerInfoPushBatch();
            customerInfoPushBatch.setmId(customerInfoPushMain.getId());
            customerInfoPushBatch.setmApiCode(dto.getApiCode());
            customerInfoPushBatch.setmBatchNumber(t.getBatchNumber());
            customerInfoPushBatch.setCreateTime(date);
            customerInfoPushBatch.setUpdateTime(date);
            customerInfoPushBatch.setmFileId(t.getId());
            customerInfoPushBatchMapper.insertSelective(customerInfoPushBatch);
        });
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(customerInfoPushMain.getId().toString());
    }

    @Override
    public Result getHaloApiCodes() {
        List<String> apiCodeList = Arrays.asList(marketingCommonConfig.getHaloAIRuleCenterCallbackConfig().get("apiCodes").toString().split(","));
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(apiCodeList);
    }

    @Override
    public Integer queryExistError(Long id, Integer filterType) {
        List<Integer> retryTotalAttemptsList = errorMarkMapper.queryRetryTotalAttempts(id,
                RetryStatusEnum.AWAIT_COMPLETE.getValue(),
                filterType);

        if (!CollectionUtils.isEmpty(retryTotalAttemptsList)) {
            // 判断是否都已补推3次
            boolean allGreaterOrEqualThree = retryTotalAttemptsList.stream()
                    .allMatch(retryAttempts -> retryAttempts >= 3);
            if (allGreaterOrEqualThree) {
                logger.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode()
                        , "规则中心哈啰回调，重试3次失败 mid:" + id));
                return PushRuleStatusEnum.PUSH_FAIL.getValue();
            } else {
                return PushRuleStatusEnum.EXCEPTIONS_TO_REFILLED.getValue();
            }
        } else {
            return PushRuleStatusEnum.TO_BE_CONFIRMED.getValue();
        }
    }

    @Override
    public void makeUpCallbackData(CustomerInfoPushMain customerInfoPushMain) {

        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5, 50);

        Long minId = null;
        boolean isContinue = Boolean.TRUE;
        while (isContinue) {
            ErrorMarkExample errorMarkExample = new ErrorMarkExample();
            errorMarkExample.setOrderByClause(" id limit 2000");

            ErrorMarkExample.Criteria criteria = errorMarkExample.createCriteria().andMIdEqualTo(customerInfoPushMain.getId())
                    .andRetryStatusEqualTo(RetryStatusEnum.AWAIT_COMPLETE.getValue())
                    .andTypeEqualTo(ErrorMarkTypeEnum.HALO_CALLBACK_ERROR.getValue())
                    .andRetryTotalAttemptsLessThan(3);

            if (minId != null) {
                criteria.andIdGreaterThan(minId);
            }
            List<ErrorMark> callbackErrorList = errorMarkMapper.selectByExample(errorMarkExample);
            if (CollectionUtil.isEmpty(callbackErrorList)) {
                isContinue = Boolean.FALSE;
                continue;
            }
            minId = callbackErrorList.get(callbackErrorList.size() - 1).getId();

            for (ErrorMark errorMark : callbackErrorList) {
                threadPool.submit(() -> rePushCallbackData(errorMark));
            }
        }
    }

    private Result<String> rePushCallbackData(ErrorMark errorMark) {
        PushMarketingUserDTO<ReqHaluoApiDTO> pushMarketingUserDTO = JSON.parseObject(errorMark.getPolicyCondition(), new TypeReference<PushMarketingUserDTO>() {
        }.getType());

        Result<String> result = haluoAiApiServiceClient.postHaluoCallbackApi(pushMarketingUserDTO.getJsonData());

        ErrorMark errorMark1 = new ErrorMark();
        errorMark1.setId(errorMark.getId());
        List<Long> ids = Arrays.stream(errorMark.getAccessNumber().split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        if (ResultCode.TIME_OUT.getValue().equals(result.getCode())
                || ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
            int retryAttempts = errorMark.getRetryTotalAttempts();
            errorMark1.setRetryTotalAttempts(retryAttempts + 1);
            errorMark1.setUpdateTime(new Date());
            errorMarkMapper.updateByPrimaryKeySelective(errorMark1);
            marketingRuleCenterHaloCallbackDataMapper.updateStatus(ids, 2);
        } else if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            errorMark1.setRetryStatus(RetryStatusEnum.PUSH_COMPLETE.getValue());
            errorMark1.setUpdateTime(new Date());
            errorMarkMapper.updateByPrimaryKeySelective(errorMark1);
            marketingRuleCenterHaloCallbackDataMapper.updateStatus(ids, 1);
        }
        return result;
    }
}
