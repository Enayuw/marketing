package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.MarketingDict;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingDictMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.base.Splitter;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;

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
    private MarketingCustomerMapper marketingCustomerMapper;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private MarketingDictMapper marketingDictMapper;


    public Result<Boolean> handleDataUserCenter(String mes) {
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(false);
        MarketingCustomer marketingCustomer = new MarketingCustomer();
        String keyPrefix = RedisKeyConstant.DELIVERY_USER_INFORMATION;
        JSONObject jsonObject = JSON.parseObject(mes);
        String apiCode = jsonObject.getString("apiCode");
        String apiType = jsonObject.getString("apiType");
        List<String> opeApiTypes = marketingCommonConfig.getOpeApiTypes();
        List<String> opeHighApiTypes = marketingCommonConfig.getOpeHighApiTypes();
        if (!opeApiTypes.contains(apiType)) {
            result.setCode(ResultCode.FAIL.getValue());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "推送消息体：" + mes, "交付推送未知apiType：" + apiType));
            return result;
        }
        String iu = jsonObject.getString("iu");
        if (StringUtil.isBlank(iu)) {
            result.setCode(ResultCode.FAIL.getValue());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "推送消息体：" + mes, "交付推送iu字段缺失"));
            return result;
        }
        List<String> ius = Splitter.on("#").splitToList(iu);
        if (ius.size() < 2) {
            result.setCode(ResultCode.FAIL.getValue());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "推送消息体：" + mes, "交付推送部门格式错误"));
            return result;
        }
        String firstDept = ius.get(1);
        if (!checkDept(firstDept, "firstLevelDepart")) {
            result.setCode(ResultCode.FAIL.getValue());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "推送消息体：" + mes, "交付推送未知一级部门"));
            return result;
        } else {
            marketingCustomer.setFirstDepartment(firstDept);
        }
        if (!"泛IU".equals(firstDept)) {
            String secondDept = ius.get(2);
            if (!checkDept(secondDept, "secondLevelDepart")) {
                result.setCode(ResultCode.FAIL.getValue());
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "推送消息体：" + mes, "交付推送未知二级部门"));
                return result;
            } else {
                marketingCustomer.setSecondDepartment(secondDept);
            }
        }
        String key = keyPrefix.concat(String.format(":%s", apiCode));
        log.info(TITLE + "key: {}", key);
        String lockValue = UUID.randomUUID().toString();
        int num = 0;
        try {
            boolean acquire = redisChgService.lock(key, lockValue, 10000L);
            while (!acquire) {
                if (num == 3) {
                    log.error(TITLE + "handleDataUserCenter获取锁失败, apiCode:{}, apiType:{}", apiCode, apiType);
                    return result;
                }
                Thread.sleep(5000L);
                acquire = redisChgService.lock(key, lockValue, 10000L);
                num++;
            }
            log.warn(TITLE + "handleDataUserCenter获取锁成功, {}", apiCode);
            // 智能运营 入库优先级高于 智能客服
            if (opeHighApiTypes.contains(apiType)) {
                buildMerchant(apiCode, apiType, marketingCustomer);
            } else {
                queryApiType(apiCode, apiType, opeHighApiTypes, marketingCustomer);
            }
            redisChgService.unlock(key, lockValue);
            log.warn(TITLE + "handleDataUserCenter释放锁成功, {}", apiCode);
        } catch (Exception e) {
            redisChgService.unlock(key, lockValue);
            log.warn(TITLE + "handleDataUserCenter error", e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }

    private boolean checkDept(String firstDept, String dicType) {
        List<MarketingDict> firstLevelDepartDictList = marketingDictMapper.getDictInfo(dicType);
        List<String> departs = firstLevelDepartDictList.stream().map(MarketingDict::getDictValue).collect(Collectors.toList());
        return departs.contains(firstDept);
    }

    /**
     * 判断该apiCode是否已存在
     *
     * @param apiCode
     * @param apiType
     * @param opeHighApiTypes
     * @param marketingCustomer
     */
    private void queryApiType(String apiCode, String apiType, List<String> opeHighApiTypes, MarketingCustomer marketingCustomer) {
        MarketingCustomerExample marketingCustomerExample = new MarketingCustomerExample();
        marketingCustomerExample.createCriteria().andApiCodeEqualTo(apiCode);
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(marketingCustomerExample);
        if (marketingCustomers.isEmpty()) {
            buildCustomer(apiCode, apiType, marketingCustomer);
            marketingCustomer.setCreateTime(new Date());
            marketingCustomerMapper.insertSelective(marketingCustomer);
        } else {
            apiType = marketingCustomers.get(0).getApiType();
            if (!opeHighApiTypes.contains(apiType)) {
                buildCustomer(apiCode, apiType, marketingCustomer);
                marketingCustomer.setUpdateTime(new Date());
                marketingCustomerMapper.updateByExampleSelective(marketingCustomer, marketingCustomerExample);
            }
        }
    }

    /**
     * 构建智能运营数据
     *
     * @param apiCode
     * @param apiType
     * @param marketingCustomer
     */
    private void buildMerchant(String apiCode, String apiType, MarketingCustomer marketingCustomer) {
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
            marketingCustomer.setApiType(apiType);
            buildMarketingCustomer(apiCode, marketingCustomer);
        } else {
            log.warn("商户信息查询失败:merchantParam：{}-----，companyMsg：{}------ ", merchantParam, companyMsg);
        }
    }

    /**
     * 构建智能客服参数
     *
     * @param apiCode
     * @param apiType
     * @param marketingCustomer
     */
    private void buildCustomer(String apiCode, String apiType, MarketingCustomer marketingCustomer) {
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
            marketingCustomer.setApiType(apiType);
        }
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
