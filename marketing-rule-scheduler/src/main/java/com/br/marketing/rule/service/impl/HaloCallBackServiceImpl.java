package com.br.marketing.rule.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.MarketingHaloCallbackRecord;
import com.br.marketing.entity.MarketingHaloCallbackRecordExample;
import com.br.marketing.enums.HaloCallBackStatusEum;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.MarketingHaloCallbackRecordMapper;
import com.br.marketing.mapper.ScoreDorisLogMapper;
import com.br.marketing.rule.service.HaloCallBackService;
import com.br.marketing.service.rulecenter.IRuleCenterPushStrategy;
import com.br.marketing.service.rulecenter.RuleCenterPushContext;
import com.br.marketing.service.rulecenter.enums.RuleCenterPushTargetEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.SpringContextUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @ClassName HaloCallBackServiceImpl
 * @Author hang.zhou
 * @Date 2025/9/16
 */
@Service
public class HaloCallBackServiceImpl implements HaloCallBackService {

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
