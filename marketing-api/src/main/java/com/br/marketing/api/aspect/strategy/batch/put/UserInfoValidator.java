package com.br.marketing.api.aspect.strategy.batch.put;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.api.aspect.AroundAspectProceeding;
import com.br.marketing.api.entities.api.ApiParamErrorResult;
import com.br.marketing.api.entities.api.ApiUserParam;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entity.MerchantParam;
import com.br.marketing.api.utils.ReturnUtil;
import com.br.marketing.common.constants.web.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/** 用户参数信息检验器
 * @author Wang Weiwei
 * @since 2018/3/15
 */
@Aspect
@Component
@Order(120)
@Slf4j
public class UserInfoValidator implements AroundAspectProceeding {

    /**
     * 限制最大上传条数为2000
     * */
    private static final int MAX_VALUE = 1000;

    @Around("com.br.marketing.api.aspect.strategy.StrategyBussinessJoinPoint.put()")
    @Override
    public void proceeding(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if(args.length<1){
            return;
        }
        Paramter paramter = new Paramter(joinPoint).invoke();
        StrategyApiContext context = paramter.getContext();
        Result result1 = paramter.getResult();
        ApiParamErrorResult result = new ApiParamErrorResult(result1 );
        JSONArray paramArray = (JSONArray) context.getJsonData();
        //校验权限
        MerchantParam merchantParam=context.getMerchantParam();
        if(merchantParam==null){
            result.putCode(ResponseCode.ACCOUNT_ERR);
            ReturnUtil.returnError(context,result);
            return;
        }
        //数据策略支持单条
        if(context.getStrategyId().startsWith("DTB") && paramArray.size()>1){
            result.putCode(ResponseCode.ERR_DTB_BATCH_COUNT);
            ReturnUtil.returnError(context,result);
            return;
        }
        if (paramArray.size() > MAX_VALUE){
            result.putCode(ResponseCode.ERR_BATCH_COUNT);
            ReturnUtil.returnError(context,result);
            return;
        }
        for (int i = 0; i < paramArray.size(); i++) {
            ApiUserParam putParam = null;
            try {
                // 当数组中存放的不是json对象时，直接返回错误
                putParam = new ApiUserParam(paramArray.getJSONObject(i));
            } catch (Exception e) {
                log.error("ApiUserParam",e);
                result.putCode(ResponseCode.MISS_JSON);
                ReturnUtil.returnError(context,result);
                return;
            }
                // 校验成功，将校验后正确的数据添加到上下文的UserParamList中，供后续计算使用
                context.addUserParam(putParam);
        }
            args[1] = result;
            joinPoint.proceed(args);

    }

}
