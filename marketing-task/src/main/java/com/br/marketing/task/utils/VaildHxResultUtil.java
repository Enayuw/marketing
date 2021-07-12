package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.exception.HxResultRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

/**
 * 画像结果校验
 */
@Slf4j
public class VaildHxResultUtil {

    /**
     * 校验画像结果正确性
     * @param hxResult 画像的结果
     * @param meal 请求的产品套餐
     * @return 校验是否通过
     */
    public static boolean isPass(String hxResult, JSONObject meal, String apiCode,
                                 RedisChgService redisChgService, MarketingUser lu, List<MarketingUser> errorList){
        boolean result=true;
        /**
         * 为空的情况一般是网络异常，重试之后也是异常，所以这种情况也需要加入到重新处理的文件中
         */
        if(StringUtils.isEmpty(hxResult)){
            errorList.add(lu);
            log.error("hxResult isEmpty");
            return false;
        }
        log.info("isPass画像result:"+hxResult);
        JSONObject resultJson=JSONObject.parseObject(hxResult);
        if(!"00".equals(resultJson.getString("code"))
                &&!"100002".equals(resultJson.getString("code"))){
            String code = resultJson.getString("code");
            String codeMessage="画像code异常";
            HxResultRuntimeException hxResultRuntimeException = new HxResultRuntimeException(
                    String.format("【紧急报警】【%s】智能营销平台-%s \001 您好:  【%s】%s，请及时跟进",
                            apiCode, codeMessage, apiCode, codeMessage + "-" + code));
            log.error("hxResult code error",hxResultRuntimeException);
            return false;
        }
        Set<String> strings = meal.keySet();
        for(String key:strings){
            if(key.equalsIgnoreCase("mappingcust")||key.equalsIgnoreCase("mappingcust1")){
                continue;
            }
            String flag;
            String s = Constants.flagMap.get(key.toLowerCase());
            String string = "";
            if(StringUtils.isNotBlank(s)){
                flag="flag_"+s;
                string = resultJson.getString(flag);
            }else{
                flag = "flag_" + key.toLowerCase();
                string = resultJson.getString(flag);
                if(!StringUtils.isNotBlank(string)){
                    flag="flag_score";
                    string = resultJson.getString(flag);
                }
            }
            if("100002".equals(resultJson.getString("code"))&&!StringUtils.isNotBlank(string)){
                continue;
            }
          if(!"0".equals(string)&&!"1".equals(string)){
                if("98".equals(string)){
                    String flagKey=Constants.HX_FLAG_98_NUM+ lu.getBatchNumber()+"_"+DateHelper.getDateAddYyMmDd(0);
                    redisChgService.incr(flagKey);
                    String hkey= Constants.HX_FLAG_98_NUM+":"+apiCode;
                    redisChgService.hset(hkey,lu.getBatchNumber(),"1");
                }else {
                    /**
                     * ScoreData未命中时不返回flag
                     * 需要特殊处理
                     */
                    if(StringUtils.isEmpty(string)){
                        if("ScoreData".equals(key)){
                            continue;
                        }else{
                            errorList.add(lu);
                            result=false;
                        }
                    }

                    if("99".equals(string)){
                        errorList.add(lu);
                        result=false;
                    }
                    HxResultRuntimeException hxResultRuntimeException = new HxResultRuntimeException(
                            String.format("【紧急报警】【%s】智能营销平台- 数据产品flag异常  \001 您好:  【%s】数据产品异常 %s- %s，请及时跟进"
                                    ,apiCode,apiCode,flag,string));
                    log.error("hxResult product flag error",hxResultRuntimeException);
                }
                break;
            }
        }
        return result;
    }
}
