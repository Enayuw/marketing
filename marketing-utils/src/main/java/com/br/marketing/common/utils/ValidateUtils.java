/*
package com.br.marketing.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.validators.user.UserValidator;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.util.IdcardUtil;

import java.util.Date;

*/
/**
 * 验证工具类
 *//*

public class ValidateUtils {

    */
/**
     * 验证姓名
     * @param name 姓名
     * @return json
     *//*

    public static JSONObject validateName(String name, String requestCode, String decrypt_key){
        JSONObject obj = new JSONObject();
        obj.put("flag",true);
        if(StringUtils.isEmpty(name)){
            obj.put("flag",false);
            obj.put("message","姓名为空");
            return obj;
        }
        String result =EsMd5Utils.decode("name",name,requestCode,decrypt_key);
        if(!StringUtils.isEmpty(result)){
            name=result;
        }
        if(name.length()>30){
            obj.put("flag",false);
            obj.put("message","姓名格式错误");
            return obj;
        }
        if(!name.matches(Constants.NAME_REGEX)){
            obj.put("flag",false);
            obj.put("message","姓名格式错误");
            return obj;
        }
        return obj;
    }

    */
/**
     * 验证手机号
     * @param cell 手机号
     * @return json
     *//*

    public static JSONObject validateCell(String cell, String requestCode, String decrypt_key){
        JSONObject obj = new JSONObject();
        obj.put("flag",true);
        if(StringUtils.isEmpty(cell)){
            obj.put("flag",false);
            obj.put("message","手机号为空");
            return obj;
        }
        String result =EsMd5Utils.decode("cell",cell,requestCode,decrypt_key);
        //String result = EsMd5Utils.autoQuery("cell", cell);
        if(!StringUtils.isEmpty(result)){
            cell=result;
        }
        if(!(new UserValidator().validatePhone(cell).isPass())){
            obj.put("flag",false);
            obj.put("message","手机号格式错误");
        }
        return obj;
    }

    */
/**
     * 验证客户编号
     * @param cusNum 手机号
     * @return json
     *//*

    public static JSONObject validateCusNum(String cusNum){
        JSONObject obj = new JSONObject();
        obj.put("flag",true);
        if(StringUtils.isEmpty(cusNum)){
            obj.put("flag",false);
            obj.put("message","客户编号为空");
            return obj;
        }
        if(!cusNum.matches(Constants.CUS_NUM_REGEX)){
            obj.put("flag",false);
            obj.put("message","客户编号格式错误");
        }
        return obj;
    }

    */
/**
     * 验证客户编号
     * @param approveResult 贷前审批结果
     * @return json
     *//*

    public static JSONObject validateApproveResult(String approveResult){
        JSONObject obj = new JSONObject();
        obj.put("flag",true);
        if(StringUtils.isNotEmpty(approveResult) && !approveResult.matches("^([1-5])$")){
            obj.put("flag",false);
            obj.put("message","贷前审批结果错误");
        }
        return obj;
    }

    */
/**
     * 验证身份证号
     * @param id 手机号
     * @return json
     *//*

    public static JSONObject validateId(String id, String requestCode, String decrypt_key){
        JSONObject obj = new JSONObject();
        obj.put("flag",true);
        if(StringUtils.isEmpty(id)){
            obj.put("flag",false);
            obj.put("message","身份证号码为空");
            return obj;
        }
        //String result = EsMd5Utils.autoQuery("id", id);
        String result =EsMd5Utils.decode("id",id,requestCode,decrypt_key);
        if(!StringUtils.isEmpty(result)){
            id=result;
        }
        if(!IdcardUtil.isValidCard(id)){
            obj.put("flag",false);
            obj.put("message","身份证号码格式错误");
        }
        return obj;
    }

    */
/**
     * 验证审批通过日
     * @param date date
     * @return json
     *//*

    public static JSONObject validatePassDate(String date){
        JSONObject obj = new JSONObject();
        obj.put("flag",true);
        if(StringUtils.isEmpty(date)){
            obj.put("flag",false);
            obj.put("message","审批通过日为空");
            return obj;
        }
        try {
            Date passDate = DateUtil.parseDate(date);
            Date now = new Date();
            if (passDate.compareTo(now) > 0){
                // 审批通过日大于当前时间
                obj.put("flag",false);
                obj.put("message","审批通过日大于当前时间");
                return obj;
            }
        }catch (Exception e){
            obj.put("flag",false);
            obj.put("message","审批通过日格式错误");
            return obj;
        }
        return obj;
    }

    */
/**
     * 验证贷款到期日
     * @param date date
     * @return json
     *//*

    public static JSONObject validateMaturityDate(String date){
        JSONObject obj = new JSONObject();
        obj.put("flag",true);
        if(StringUtils.isNotEmpty(date)){
            try {
                Date maturityDate = DateUtil.parseDate(date);
                Date now = new Date();
                if (maturityDate.compareTo(now) < 0){
                    // 审批通过日大于当前时间
                    obj.put("flag",false);
                    obj.put("message","贷款到期日小于当前时间");
                    return obj;
                }
            }catch (Exception e){
                obj.put("flag",false);
                obj.put("message","贷款到期日格式错误");
                return obj;
            }
        }
        return obj;
    }

    */
/**
     * 审批通过日和贷款到期日
     * @return json
     *//*

    public static JSONObject validateMaturityDateAndPassDate(String maturityDate, String passDate){
        JSONObject obj = new JSONObject();
        obj.put("flag",true);
        try {
            Date maturity = DateUtil.parseDate(maturityDate);
            Date pass = DateUtil.parseDate(passDate);
            if (maturity.compareTo(pass) <= 0){
                // 审批通过日大于当前时间
                obj.put("flag",false);
                obj.put("message","审批通过日大于或等于贷款到期日");
                return obj;
            }
        }catch (Exception e){
            obj.put("flag",false);
            obj.put("message","贷款到期日格式错误");
            return obj;
        }
        return obj;
    }

}
*/
