package com.br.marketing.api.aspect.strategy.batch.put;

import com.br.marketing.api.aspect.AroundAspectProceeding;
import com.br.marketing.api.entities.api.ApiParamErrorResult;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entity.MerchantParam;
import com.br.marketing.api.utils.ReturnUtil;
import com.br.marketing.common.constants.auth.AuthShowProductor;
import com.br.marketing.common.constants.web.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 用户参数信息检验器
 * <p>
 * 主要进行有无商户，有无api权限，有无api可用条数校验
 *
 * @author Wang Weiwei
 * @since 2018/3/15
 */
@Aspect
@Component
@Order(140)
@Slf4j
public class PermissionValidator implements AroundAspectProceeding {


    @Around("com.br.marketing.api.aspect.strategy.StrategyBussinessJoinPoint.put()")
    @Override
    public void proceeding(ProceedingJoinPoint joinPoint) throws Throwable {
        Paramter paramter = new Paramter(joinPoint).invoke();
        StrategyApiContext context = paramter.getContext();
        Result result1 = paramter.getResult();
        ApiParamErrorResult result = new ApiParamErrorResult(result1);
        //校验权限
        MerchantParam merchantParam = context.getMerchantParam();
        if (merchantParam == null) {
            result.putCode(ResponseCode.ERR_PERMISSION);
            ReturnUtil.returnError(context,result);
            return;
        } else {
                    context.setAccountType(merchantParam.getAccountType());
                    context.setIsShowData(AuthShowProductor.codeToEnum(merchantParam.getReturnData() == null ?
                            Integer.valueOf(0) : merchantParam.getReturnData()));
                   //检查账号状态  已停用的账号直接返回错误
                    if(merchantParam.getAccountType()==-1){
                        result.putCode(ResponseCode.ERR_PERMISSION);
                        ReturnUtil.returnError(context,result);
                        return;
                    }else {
                        //开通的不是流失预警模块
                        if(!"2".equals(merchantParam.getServiceMode())){
                            result.putCode(ResponseCode.ERR_PERMISSION);
                            ReturnUtil.returnError(context,result);
                        }
                        joinPoint.proceed();
                    }
        }
    }


}
