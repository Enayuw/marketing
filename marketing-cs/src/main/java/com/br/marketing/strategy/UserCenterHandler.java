package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
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
import java.util.UUID;

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

    private static final String TITLE = "【用户中心获取用户信息】";
    @Resource
    MarketingCustomerMapper marketingCustomerMapper;
    @Resource
    RedisChgService redisChgService;

    public Result<Boolean> handleDataUserCenter(String mes) {
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(false);
        MarketingCustomer marketingCustomer = new MarketingCustomer();
        String keyPrefix = RedisKeyConstant.DELIVERY_USER_INFORMATION;
        try {
            JSONObject jsonObject = JSON.parseObject(mes);
            String apiCode = jsonObject.getString("apiCode");
            String operateType = jsonObject.getString("operateType");
            String apiType = jsonObject.getString("apiType");
            String key = keyPrefix.concat(String.format(":%s", apiCode));
            log.info(TITLE + "key: {}", key);
            String lockValue = UUID.randomUUID().toString();
            try {
                boolean acquire = redisChgService.lock(key, lockValue, 10000L);
                if (!acquire) {
                    log.warn(TITLE + "handleDataUserCenter获取锁失败, {}", apiCode);
                    return result;
                }
                log.warn(TITLE + "handleDataUserCenter获取锁成功, {}", apiCode);
                // 智能运营 入库优先级高于 智能客服
                if ("智能运营".equals(apiType)) {
                    buildMerchant(apiCode,marketingCustomer);
                }else if("智能客服".equals(apiType)){
                    queryApiType(apiCode);
                }
                redisChgService.unlock(key, lockValue);
                log.warn(TITLE + "handleDataUserCenter释放锁成功, {}", apiCode);
            } catch (Exception e) {
                redisChgService.unlock(key, lockValue);
                log.warn(TITLE + "handleDataUserCenter error", e);
            }
        } catch (Exception e) {
            log.error(TITLE + "{} 失败 -- ", mes, e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }

    /**
     * 判断该apiCode是否已存在
     * @param apiCode
     */
    private void queryApiType(String apiCode) {
        MarketingCustomer marketingCustomer = new MarketingCustomer();
        MarketingCustomerExample marketingCustomerExample = new MarketingCustomerExample();
        marketingCustomerExample.createCriteria().andApiCodeEqualTo(apiCode);
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(marketingCustomerExample);
        if (marketingCustomers.isEmpty()) {
            marketingCustomer = buildCustomer(apiCode);
            marketingCustomer.setCreateTime(new Date());
            marketingCustomerMapper.insertSelective(marketingCustomer);
        } else {
            String apiType = marketingCustomers.get(0).getApiType();
            if("智能客服".equals(apiType)){
                marketingCustomer = buildCustomer(apiCode);
                marketingCustomer.setUpdateTime(new Date());
                marketingCustomerMapper.updateByExampleSelective(marketingCustomer, marketingCustomerExample);
            }
        }
    }

    /**
     * 构建智能运营数据
     * @param apiCode
     * @param marketingCustomer
     */
    private void buildMerchant(String apiCode, MarketingCustomer marketingCustomer) {
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
            marketingCustomer.setApiType("智能运营");
            buildMarketingCustomer(apiCode,marketingCustomer);
        } else {
            log.warn("商户信息查询失败:merchantParam：{}-----，companyMsg：{}------ ", merchantParam, companyMsg);
        }
    }

    /**
     * 构建智能客服参数
     * @param apiCode
     */
    private MarketingCustomer buildCustomer(String apiCode) {
        MarketingCustomer marketingCustomer = new MarketingCustomer();
        String customerMsg = RpcClientProxy.getCustomerMsg(apiCode);
        String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
        if (StringUtils.isNotEmpty(customerMsg) && StringUtils.isNotEmpty(companyMsg)) {
            JSONObject customerJSONObj = JSON.parseObject(customerMsg);
            JSONObject companyJSONObj = JSON.parseObject(companyMsg);
            marketingCustomer.setCid(String.valueOf(companyJSONObj.get("COMP_ID")));
            marketingCustomer.setName(companyJSONObj.getString("COMP_NAME"));
            marketingCustomer.setShortName(companyJSONObj.getString("COMP_SHORT_NAME"));
            marketingCustomer.setApplyLoanType(companyJSONObj.getString("APPLY_LOAN_TYPE"));
            marketingCustomer.setAccountStatus(customerJSONObj.getString("account_status"));
            marketingCustomer.setAccountType(customerJSONObj.getInteger("account_type"));
            marketingCustomer.setStatus(customerJSONObj.getByte("account_status"));
            marketingCustomer.setApiCode(apiCode);
            marketingCustomer.setApiType("智能客服");
        }
        return marketingCustomer;
    }

    private void buildMarketingCustomer(String apiCode, MarketingCustomer marketingCustomer) {
        MarketingCustomerExample marketingCustomerExample = new MarketingCustomerExample();
        marketingCustomerExample.createCriteria().andApiCodeEqualTo(apiCode).andCidEqualTo(marketingCustomer.getCid());
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(marketingCustomerExample);
        if (marketingCustomers.isEmpty()) {
            marketingCustomer.setCreateTime(new Date());
            marketingCustomerMapper.insertSelective(marketingCustomer);
        } else {
            marketingCustomer.setUpdateTime(new Date());
            marketingCustomerMapper.updateByExampleSelective(marketingCustomer, marketingCustomerExample);
        }
    }

}
