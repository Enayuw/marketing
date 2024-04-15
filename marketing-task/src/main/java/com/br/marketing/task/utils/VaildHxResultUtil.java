package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.HxResultErrorCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTask;
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
                                 RedisChgService redisChgService, MarketingUser lu
            , List<MarketingUser> errorList, List<String> noflagproductlist, List<String> flagProductList){
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
            if(noflagproductlist.contains(key.toLowerCase())){
                continue;
            }
            String flag = "";
            String s = Constants.flagMap.get(key.toLowerCase());
            String string = "";
            if(StringUtils.isNotBlank(s)){
                flag="flag_"+s;
                string = resultJson.getString(flag);
            }else{
                if(flagProductList.contains(key)){
                        flag="flag_score";
                        string = resultJson.getString(flag);
                }else{
                    flag = "flag_" + key.toLowerCase();
                    string = resultJson.getString(flag);
                }
            }
            if("100002".equals(resultJson.getString("code"))&&!StringUtils.isNotBlank(string)){
                continue;
            }
          if(!"0".equals(string)&&!"1".equals(string)){
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
                    String.format("【紧急报警】【%s】智能营销平台--数据产品flag异常-\001您好:【%s】数据产品异常-%s--%s，请及时跟进"
                            ,apiCode,apiCode,flag,string));
            log.warn("hxResult product flag error",hxResultRuntimeException);
            String title = String.format("【紧急报警】【%s】智能营销平台--数据产品flag异常", apiCode);
            log.warn(AlertLog.buildWarnMessage(("99".equals(string)||"98".equals(string))
                            ?AlarmSendCodeEnum.EXCEPTION_HUAX.getCode()
                            :AlarmSendCodeEnum.ERROR_UNKNOWN.getCode()
                    , hxResultRuntimeException.getMessage(), title));
        break;
            }
        }
        return result;
    }

    /**
     * 校验画像结果正确性
     * @param hxResult 画像的结果
     * @param meal 请求的产品套餐
     * @return 校验是否通过
     */
    public static boolean isPass(String hxResult, JSONObject meal, String apiCode,
                                 RedisChgService redisChgService, MarketingSyncUser lu, List<MarketingSyncUser> errorList,
                                 List<String> noflagproductlist,List<String> flagProductList,MarketingTask marketingTask,Boolean isRetry){
        boolean result=true;
        String errorResultKey = RedisKeyConstant.TASKSCORE_HXRESULTERROR.concat(":").concat(apiCode).concat(":").concat(marketingTask.getId().
                toString());
        String errorMessage ="";
        /**
         * 为空的情况一般是网络异常，重试之后也是异常，所以这种情况也需要加入到重新处理的文件中
         */
        if(StringUtils.isEmpty(hxResult)){
            errorMessage = "画像返回结果为空";
            errorResultHandler(redisChgService, isRetry, errorResultKey, errorMessage);
            errorList.add(lu);
            log.error("hxResult isEmpty");
            return false;
        }
        log.info("isPass画像result:"+hxResult);
        JSONObject resultJson=JSONObject.parseObject(hxResult);
        if(!"00".equals(resultJson.getString("code"))
                &&!"100002".equals(resultJson.getString("code"))){
            String code = resultJson.getString("code");
            String codeMessage="画像code异常"+":"+ HxResultErrorCodeEnum.getByCode(code);
            HxResultRuntimeException hxResultRuntimeException = new HxResultRuntimeException(
                    String.format("【紧急报警】【%s】智能营销平台-%s \001 您好:  【%s】%s，请及时跟进",
                            apiCode, codeMessage, apiCode, codeMessage + "-" + code));
            errorMessage = "画像返回错误信息code="+code;
            errorResultHandler(redisChgService,isRetry,errorResultKey,errorMessage);
            errorList.add(lu);
            log.error("hxResult code error",hxResultRuntimeException);
            return false;
        }
        Set<String> strings = meal.keySet();
        for(String key:strings){
            if(noflagproductlist.contains(key.toLowerCase())){
                continue;
            }
            String flag = "";
            String s = Constants.flagMap.get(key.toLowerCase());
            String string = "";
            if(StringUtils.isNotBlank(s)){
                flag="flag_"+s;
                string = resultJson.getString(flag);
            }else{
                if(flagProductList.contains(key)){
                    flag="flag_score";
                    string = resultJson.getString(flag);
                }else{
                    flag = "flag_" + key.toLowerCase();
                    string = resultJson.getString(flag);
                }
            }
            if("100002".equals(resultJson.getString("code"))&&!StringUtils.isNotBlank(string)){
                errorMessage = "画像返回code码为100002,且所有flag产品标识为空";
                errorResultHandler(redisChgService, isRetry, errorResultKey, errorMessage);
                errorList.add(lu);
                result=false;
                break;
            }
            if(!"0".equals(string)&&!"1".equals(string)){
                /**
                 * ScoreData未命中时不返回flag
                 * 需要特殊处理
                 */
                if(StringUtils.isEmpty(string)){
                    if("ScoreData".equals(key)){
                        continue;
                    }else{
                        errorMessage = "画像返回flag为空,且产品名称不是ScoreData";
                        errorResultHandler(redisChgService, isRetry, errorResultKey, errorMessage);
                        errorList.add(lu);
                        result=false;
                    }
                }

                if("99".equals(string)){
                    errorMessage = "画像返回flag为99";
                    errorResultHandler(redisChgService, isRetry, errorResultKey, errorMessage);
                    errorList.add(lu);
                    result=false;
                }
                HxResultRuntimeException hxResultRuntimeException = new HxResultRuntimeException(
                        String.format("【紧急报警】【%s】智能营销平台-数据产品flag异常\001 您好:【%s】数据产品异常-%s--%s，请及时跟进"
                                ,apiCode,apiCode,flag,string+":"+HxResultErrorCodeEnum.getByCode(string)));
                log.warn("hxResult product flag error",hxResultRuntimeException);
                String title = String.format("【紧急报警】【%s】智能营销平台-数据产品flag异常", apiCode);
                log.warn(AlertLog.buildWarnMessage(("99".equals(string)||"98".equals(string))
                                ?AlarmSendCodeEnum.EXCEPTION_HUAX.getCode()
                                :AlarmSendCodeEnum.ERROR_UNKNOWN.getCode()
                        , hxResultRuntimeException.getMessage(), title));
                break;
            }
        }
        return result;
    }

    /**
     * 画像结果返回异常统计
     * @param redisChgService
     * @param isRetry 是否为重试
     * @param key 异常统计key
     * @param errorMessage 异常信息
     */
    private static void errorResultHandler(RedisChgService redisChgService, Boolean isRetry, String key, String errorMessage) {
        //非重试，跳过
        if (!isRetry) {
            return;
        }
        try {
            redisChgService.hincrby(key, errorMessage, 1);
        } catch (Exception e) {
            log.error("跑分画像异常结果统计redis异常", e.getMessage());
        }

    }
}
