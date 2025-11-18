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


    @Override
    public Result<Boolean> pushData(Long id) {

        CustomerInfoPushMain customerInfoPushMain = customerInfoPushMainMapper.selectByPrimaryKey(id);
        Integer pushTarget = customerInfoPushMain.getPushTarget();
        RuleCenterPushTargetEnum pushTargetEnum = RuleCenterPushTargetEnum.findPushNameByCode(pushTarget);
        if (pushTargetEnum == null) {
            return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setMessage("规则中心数据处理-未匹配到到推送实现");
        }
        AbstractRuleCenterPushStrategy pushStrategy = SpringContextUtil.getBean(pushTargetEnum.getPushAchieve(),AbstractRuleCenterPushStrategy.class);
        //获取上下文
        RuleCenterPushContext context = pushStrategy.assemblePushContext(customerInfoPushMain);
        //设置线程信息
        pushStrategy.setThreadPoolNum(context);
        //执行推送策略
        return pushStrategy.executePush(context);
    }
}
