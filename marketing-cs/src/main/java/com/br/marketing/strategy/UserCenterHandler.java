package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.IceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.MarketingCustomerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 根据交付下发消息，到用户中心获取用户信息
 * @Date 2022/4/8 11:47 AM
 * ------------------------------
 */
@Service
@Slf4j
public class UserCenterHandler {

    @Autowired
    MarketingCustomerMapper marketingCustomerMapper;
    public Result<Boolean> handleDataUserCenter(String mes){
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(true);
        MarketingCustomer marketingCustomer = new MarketingCustomer();
        try {
            JSONObject jsonObject = JSON.parseObject(mes);
            String apiCode = jsonObject.getString("apiCode");
            String operateType = jsonObject.getString("operateType");
            MerchantParam merchantParam = IceClient.getMerchantParam(apiCode);
            String companyMsg = IceClient.getCompanyMsg(apiCode);
            marketingCustomer.setApiCode(merchantParam.getApiCode());
            if(StringUtils.isNotEmpty(companyMsg)){
                JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                marketingCustomer.setCid(String.valueOf(companyJSONObj.get("COMP_ID")));
                marketingCustomer.setName(companyJSONObj.getString("COMP_NAME"));
            }
            marketingCustomer.setAccountStatus(merchantParam.getAccountSstatus());
            marketingCustomer.setAccountType(merchantParam.getAccountType());
            marketingCustomer.setApiCode(merchantParam.getApiCode());
            if("add".equals(operateType)){
                marketingCustomerMapper.insertSelective(marketingCustomer);
            }else {
                MarketingCustomerExample marketingCustomerExample = new MarketingCustomerExample();
                marketingCustomerExample.createCriteria().andApiCodeEqualTo(apiCode);
                marketingCustomerMapper.updateByExampleSelective(marketingCustomer,marketingCustomerExample);
            }
            //marketingCustomer.setCallMethod(merchantParam.getCallMethod());
            //marketingCustomer.setCreateTime(new Date());
            //marketingCustomer.setUpdateTime(new Date());
            //marketingCustomer.setIsCharging(merchantParam.getIsCharging());
            //marketingCustomer.setIsCheck(merchantParam.getIsCheck());
            //marketingCustomer.setRequestCode(merchantParam.getRequestCode());
            //marketingCustomer.setResponseCode(merchantParam.getResponseCode());
            //marketingCustomer.setAccountStatus(merchantParam.getAccountSstatus());
            //marketingCustomer.setStartTime(merchantParam.getStartTime());
            //marketingCustomer.setEndTime(merchantParam.getEndTime());
            //marketingCustomer.setTransport(merchantParam.getTransport());
            ////String mealJson = merchantParam.getMealJson();
            ////marketingCustomer.setMealJson(merchantParam.getMealJson().toString());
            //marketingCustomer.setEncryptionKey(merchantParam.getEncryptionKey());
            //marketingCustomer.setDecryptKey(merchantParam.getDecryptKey());
            //marketingCustomer.setSnVer(merchantParam.getSnVer());
            //marketingCustomer.setFileEncryptionMethods(merchantParam.getFileEncryptionMethods());
            //marketingCustomer.setFileEncryptionAlgorithm(merchantParam.getFileEncryptionAlgorithm());
            //marketingCustomer.setFileEncryptionKey(merchantParam.getFileEncryptionKey());
            //marketingCustomer.setIsOutputDataProduct(merchantParam.getIsOutputDataProduct());
            //marketingCustomer.setMessage(merchantParam.getRemarks());

        } catch (Exception e) {
            log.error("同步商中心信息:{} 失败 -- ",mes,e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;

    }




}
