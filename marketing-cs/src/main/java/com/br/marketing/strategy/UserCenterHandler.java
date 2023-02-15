package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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

    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

    public Result<Boolean> handleDataUserCenter(String mes) {
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(false);
        MarketingCustomer marketingCustomer = new MarketingCustomer();
        try {
            JSONObject jsonObject = JSON.parseObject(mes);
            String apiCode = jsonObject.getString("apiCode");
            String operateType = jsonObject.getString("operateType");
            String apiType = jsonObject.getString("apiType");
            if (apiType.equals("智能运营")) {
                MerchantParam merchantParam = RpcClientProxy.getMerchantParam(apiCode);
                String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
                if (StringUtils.isNotEmpty(companyMsg) && merchantParam != null) {
                    JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                    marketingCustomer.setCid(String.valueOf(companyJSONObj.get("COMP_ID")));
                    marketingCustomer.setName(companyJSONObj.getString("COMP_NAME"));
                    marketingCustomer.setShortName(companyJSONObj.getString("COMP_SHORT_NAME"));
                    marketingCustomer.setApplyLoanType(companyJSONObj.getString("APPLY_LOAN_TYPE"));
                    marketingCustomer.setAccountStatus(merchantParam.getAccountStatus());
                    marketingCustomer.setAccountType(merchantParam.getAccountType());
                    marketingCustomer.setApiCode(merchantParam.getApiCode());
                    marketingCustomer.setCallMethod(merchantParam.getCallMethod());
                    marketingCustomer.setUpdateTime(new Date());
                    marketingCustomer.setIsCharging(merchantParam.getIsCharging());
                    marketingCustomer.setIsCheck(merchantParam.getIsCheck());
                    marketingCustomer.setRequestCode(merchantParam.getRequestCode());
                    marketingCustomer.setResponseCode(merchantParam.getResponseCode());
                    marketingCustomer.setStatus(Byte.valueOf(merchantParam.getAccountStatus()));
                    marketingCustomer.setStartTime(merchantParam.getStartTime());
                    marketingCustomer.setEndTime(merchantParam.getEndTime());
                    marketingCustomer.setTransport(merchantParam.getTransport());
                    //String mealJson = merchantParam.getMealJson();
                    //marketingCustomer.setMealJson(merchantParam.getMealJson().toString());
                    marketingCustomer.setEncryptionKey(merchantParam.getEncryptionKey());
                    marketingCustomer.setDecryptKey(merchantParam.getDecryptKey());
                    marketingCustomer.setSnVer(merchantParam.getSnVer());
                    marketingCustomer.setFileEncryptionMethods(merchantParam.getFileEncryptionMethods());
                    marketingCustomer.setFileEncryptionAlgorithm(merchantParam.getFileEncryptionAlgorithm());
                    marketingCustomer.setFileEncryptionKey(merchantParam.getFileEncryptionKey());
                    marketingCustomer.setIsOutputDataProduct(merchantParam.getIsOutputDataProduct());
                    marketingCustomer.setMessage(merchantParam.getRemarks());
                    MarketingCustomerExample marketingCustomerExample = new MarketingCustomerExample();
                    marketingCustomerExample.createCriteria().andApiCodeEqualTo(apiCode).andCidEqualTo(marketingCustomer.getCid());
                    List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(marketingCustomerExample);
                    if (marketingCustomers.size() == 0) {
                        marketingCustomer.setCreateTime(new Date());
                        marketingCustomerMapper.insertSelective(marketingCustomer);
                    } else {
                        marketingCustomer.setUpdateTime(new Date());
                        marketingCustomerMapper.updateByExampleSelective(marketingCustomer, marketingCustomerExample);
                    }
                } else {
                    log.warn("商户信息查询失败:merchantParam：{}-----，companyMsg：{}------ ", merchantParam, companyMsg);
                }
            }

        } catch (Exception e) {
            log.error("同步商户中心信息:{} 失败 -- ", mes, e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;

    }


}
