package com.br.marketing.service.rulecenter.impl.push;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.rulecenter.IRuleCenterPushService;
import com.br.marketing.service.rulecenter.IRuleCenterPushStrategy;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.br.marketing.service.rulecenter.enums.RuleCenterPushTargetEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.GeneScriptUtil;
import com.br.marketing.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class RuleCenterPushServiceImpl implements IRuleCenterPushService {

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;


    @Override
    public Result<Boolean> pushData(Long id) {

        CustomerInfoPushMain customerInfoPushMain = customerInfoPushMainMapper.selectByPrimaryKey(id);
        Integer pushTarget = customerInfoPushMain.getPushTarget();
        CustomerInfoPushBatchExample searchPushBatch = new CustomerInfoPushBatchExample();
        searchPushBatch.createCriteria().andMIdEqualTo(customerInfoPushMain.getId());
        List<CustomerInfoPushBatch> customerInfoPushBatches = customerInfoPushBatchMapper.selectByExample(searchPushBatch);

        List<String> numList = new ArrayList<>();
        List<Long> fileIds = new ArrayList<>();
        for (CustomerInfoPushBatch customerInfoPushBatch : customerInfoPushBatches) {
            numList.add(customerInfoPushBatch.getmBatchNumber());
            fileIds.add(customerInfoPushBatch.getmFileId());
        }
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria().andIdIn(fileIds);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(fileExample);

        Integer getEsNum = marketingCommonConfig.getScoreByEsThreadNum() != null
                && marketingCommonConfig.getScoreByEsThreadNum() > 0
                ? marketingCommonConfig.getScoreByEsThreadNum()
                : 10;

        Integer getJcNum = marketingCommonConfig.getScoreToJcThreadNum() != null
                && marketingCommonConfig.getScoreToJcThreadNum() > 0
                ? marketingCommonConfig.getScoreToJcThreadNum()
                : 2;

        Integer getCallbackNum = marketingCommonConfig.getScoreToCallbackThreadNum() != null
                && marketingCommonConfig.getScoreToCallbackThreadNum() > 0
                ? marketingCommonConfig.getScoreToCallbackThreadNum()
                : 10;

        String scoreFileYhTime = marketingCommonConfig.getScoreFileYhTime();
        Date yhTime = null;
        try {
            yhTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(scoreFileYhTime);
        } catch (ParseException e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), e.getMessage()), e);
        }
        Date yh = yhTime;
        Integer parNum = 0;
        boolean isSigle = Boolean.FALSE;
        if (CollectionUtils.isNotEmpty(straHisFiles)) {
            long beforeCount = straHisFiles.stream().filter(t -> t.getCreateTime().compareTo(yh) <= 0).count();
            Optional<StraHisFile> first = straHisFiles.stream().sorted(Comparator.comparing(StraHisFile::getIndexNum).reversed()).findFirst();

            if (first.isPresent()) {
                parNum = first.get().getIndexNum();
            }
            isSigle = (customerInfoPushMain.getmPercentage() != null
                    && customerInfoPushMain.getmPercentage().compareTo(BigDecimal.ZERO) > 0)
                    || (customerInfoPushMain.getmPlanNum() != null && customerInfoPushMain.getmPlanNum() > 0)
                    || beforeCount > 0;
            if (isSigle) {
                parNum = 1;
                getEsNum = 1;
            }
        }
        ThreadPoolExecutor actionEs = BrExecutors.getThreadPool(getEsNum, getEsNum, 50);
        ThreadPoolExecutor pushJc;
        if (RuleCenterPushTargetEnum.HALO_CALLBACK.getCode().equals(pushTarget)){
            pushJc = BrExecutors.getThreadPool(getCallbackNum, getCallbackNum, 50);
        }else {
            pushJc = BrExecutors.getThreadPool(getJcNum, getJcNum, 50);
        }
        Boolean markWithEsFlag = marketingCommonConfig.getPushPolicyMarkWithEsFlag();
        String scoreCondition = customerInfoPushMain.getmScoreCondition();
        Object lableObject = null;
        if (StringUtils.isNotEmpty(scoreCondition)) {
            if (markWithEsFlag) {
                lableObject = GeneScriptUtil.esLableScript(scoreCondition);
            } else {
                lableObject = GeneScriptUtil.getScoreLables(scoreCondition, markWithEsFlag);
            }
        }
        //构建上下文
        RuleCenterPushContext context = new RuleCenterPushContext();
        context.setBatchNumbers(numList);
        context.setFileIds(fileIds);
        context.setCustomerInfoPushMain(customerInfoPushMain);
        context.setSinglePartition(isSigle);
        context.setMarkWithEsFlag(markWithEsFlag);
        context.setLabelObject(lableObject);
        context.setPartitionCount((Objects.equals(customerInfoPushMain.getPushTarget(), RuleCenterPushTargetEnum.MERGE_PUSH_POLICY.getCode()) || (Objects.equals(customerInfoPushMain.getPushTarget(), RuleCenterPushTargetEnum.HALO_CALLBACK.getCode()))) ? 1 : parNum);
        context.setEsThreadPool(actionEs);
        context.setPushThreadPool(pushJc);
        RuleCenterPushTargetEnum pushTargetEnum = RuleCenterPushTargetEnum.findPushNameByCode(customerInfoPushMain.getPushTarget());
        if (pushTargetEnum == null) {
            return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setMessage("规则中心数据处理-未匹配到到推送实现");
        }
        //执行推送策略
        IRuleCenterPushStrategy pushStrategy = SpringContextUtil.getBean(pushTargetEnum.getPushAchieve(), IRuleCenterPushStrategy.class);
        return pushStrategy.executePush(context);
    }
}
