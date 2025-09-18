package com.br.marketing.service.rulecenter.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.service.rulecenter.IRuleCenterHaloCallbackService;
import com.br.marketing.service.rulecenter.IRuleCenterPushStrategy;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.br.marketing.service.rulecenter.enums.RuleCenterPushTargetEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.SpringContextUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName RuleCenterHaloCallbackServiceImpl
 * @Author hang.zhou
 * @Date 2025/9/18
 */
@Service
public class RuleCenterHaloCallbackServiceImpl implements IRuleCenterHaloCallbackService {


    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Override
    public Result<Boolean> callBack(Long id) {
        CustomerInfoPushMain customerInfoPushMain = customerInfoPushMainMapper.selectByPrimaryKey(id);

        Integer getEsNum = marketingCommonConfig.getScoreByEsThreadNum() != null
                && marketingCommonConfig.getScoreByEsThreadNum() > 0
                ? marketingCommonConfig.getScoreByEsThreadNum()
                : 10;
        Integer getJcNum = marketingCommonConfig.getScoreToJcThreadNum() != null
                && marketingCommonConfig.getScoreToJcThreadNum() > 0
                ? marketingCommonConfig.getScoreToJcThreadNum()
                : 2;

        ThreadPoolExecutor actionEs = BrExecutors.getThreadPool(getEsNum, getEsNum, 50);
        ThreadPoolExecutor pushJc = BrExecutors.getThreadPool(getJcNum, getJcNum, 50);

        RuleCenterPushContext context = new RuleCenterPushContext();
        context.setCustomerInfoPushMain(customerInfoPushMain);
        context.setPartitionCount(1);
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
