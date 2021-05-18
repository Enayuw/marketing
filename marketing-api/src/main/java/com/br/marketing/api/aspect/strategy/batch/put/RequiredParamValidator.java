package com.br.marketing.api.aspect.strategy.batch.put;

import cn.hutool.core.lang.Validator;
import com.br.marketing.api.aspect.AroundAspectProceeding;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.utils.ReturnUtil;
import com.br.marketing.common.constants.ParamConstant;
import com.br.marketing.common.constants.web.ResponseCode;
import com.br.marketing.common.utils.EnvironmentManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/** 策略推送接口必填参数检验切面
 * @author Wang Weiwei
 * @since 2018/3/12
 */
@Aspect
@Component
@Order(100)
@Slf4j
public class RequiredParamValidator implements AroundAspectProceeding {

    /**
     * 集群节点标识码
     */
    @Value("${cluster.flag}")
    private String flag;

    /**
     * 必填参数校验器
     * */
    @Around("com.br.marketing.api.aspect.strategy.StrategyBussinessJoinPoint.put()")
    @Override
    public void proceeding(ProceedingJoinPoint joinPoint) throws Throwable {
        Paramter paramter = new Paramter(joinPoint).invoke();
        StrategyApiContext context = paramter.getContext();
        Result result = paramter.getResult();
        if (validateApiCode(context)){
            result.putCode(ResponseCode.ERR_NULL);
            ReturnUtil.returnError(context,result);
            return;
        }

            String swiftNumber = result.getSwiftNumber();
            String hostNum;
            if(EnvironmentManager.initLocalhostIpAddr()==0){
                if(ParamConstant.SwiftNumberVersionEnum.FIRST.getCode().equals(context.getMerchantParam().getSnVer())){
                    hostNum = EnvironmentManager.localhostIpLastByte;
                    swiftNumber=swiftNumber.replace("_","_"+hostNum);
                }else{
                    hostNum = EnvironmentManager.localHostIpHex;
                    String flag1 = ParamConstant.ClusterEnum.getCode(flag);
                    StringBuilder append = new StringBuilder(swiftNumber).append(hostNum).append(flag1).append("09");
                    swiftNumber =append.toString();
                }
            }
            result.setSwiftNumber(context.getApiCode() + "_"+swiftNumber );
            joinPoint.proceed();

    }




    /**
     * 校验APICODE的正确性
     * 当验证不通过时，返回true
     * */
    private boolean validateApiCode(StrategyApiContext context) {
        return  (StringUtils.isEmpty(context.getApiCode()) ||
                context.getApiCode().length() < 5 ||
                !Validator.isNumber(context.getApiCode()));
    }



}
