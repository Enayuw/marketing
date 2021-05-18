package com.br.marketing.api.web;

import com.br.common.util.AESAlgorithmUtil;
import com.br.common.util.StringUtils;
import com.br.marketing.api.bussinesses.SingleStrategyBussiness;
import com.br.marketing.api.bussinesses.SingleStrategyDtbBussiness;
import com.br.marketing.api.entities.api.*;
import com.br.marketing.common.constants.web.ResponseCode;
import com.br.marketing.common.utils.ThreeDes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * 策略API控制器
 *
 * @author Wang Weiwei
 * @since 2018/3/12
 */
@RestController
@RequestMapping("/loanStrategy/v2/")
@Slf4j
public class StrategyController {



    @Resource
    private SingleStrategyDtbBussiness singleDtbStrategyBussiness;

    @Resource
    private SingleStrategyBussiness singleStrategyBussiness;

    /**
     * 单条推送加查询方法
     * 该方法为同步方法，用户向该策略API推送消息，并能直接拿出返回结果。
     * @param strategyApiContext 上下文
     * @param jsonData 客户入参
     * @param request HttpServletRequest
     * @return String 查询结果
     */
    @PostMapping("query")
    public String query(StrategyApiContext strategyApiContext, String jsonData, HttpServletRequest request) {
        strategyApiContext.setRequestUrl("query");
        String strategyId = strategyApiContext.getStrategyId();
        Result result = new Result();
        if (StringUtils.isEmpty(strategyId)) {
            result.putCode(ResponseCode.ERROR_STRATEGR_ID);
        } else {
            //风险策略
            if (strategyId.startsWith("STRB")) {
                singleStrategyBussiness.query(strategyApiContext, result);
            } else {
                singleDtbStrategyBussiness.query(strategyApiContext, result);
            }
        }
        boolean flag=true;

        long l = System.currentTimeMillis();
        while(!strategyApiContext.isDone()){
            if(System.currentTimeMillis()-l>5000){
                break;
            }
        }
        StrategyResult strategyResult = strategyApiContext.getStrategyResult();
        return  strategyResult.toString();
    }


    /**
     * 解密接口
     * @param type 解密类型
     * @param value 解密数据
     * @param key 解密key
     * @return String 密文
     */
    @PostMapping("decode")
    public String decode(String type, String value, String key){
        String result="";
        if(StringUtils.isNotEmpty(type)&&StringUtils.isNotEmpty(value)&&StringUtils.isNotEmpty(key)){
           if("aes".equals(type)){
               result= AESAlgorithmUtil.decrypt(value,key);
           }else if("3des".equals(type)){
               try {
                   result= ThreeDes.decryptByEcb(value, key);
               } catch (Exception e) {
                   log.error("解密失败",e);
               }
           }
        }else{
            result="参数不合法";
        }
        return result;
    }

    /**
     * 加密接口
     * @param type 加密类型
     * @param value 加密数据
     * @param key 加密key
     * @return String 密文
     */
    @PostMapping("encode")
    public String encode(String type, String value, String key){
        String result="";
        if(StringUtils.isNotEmpty(type)&&StringUtils.isNotEmpty(value)&&StringUtils.isNotEmpty(key)){
            if("aes".equals(type)){
                result=AESAlgorithmUtil.encrypt(value,key);
            }else if("3des".equals(type)){
                try {
                    result= ThreeDes.encryptByEcb(value, key);
                } catch (Exception e) {
                    log.error("加密异常",e);
                }
            }
        }else{
            result="参数不合法";
        }
        return result;
    }


}
