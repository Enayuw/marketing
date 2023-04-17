package com.br.marketing.service.Impl;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCustomerConfig;
import com.br.marketing.entity.MarketingCustomerConfigExample;
import com.br.marketing.enums.ThreeKeyEncryptEnum;
import com.br.marketing.enums.ThreeKeyTypeEnum;
import com.br.marketing.mapper.MarketingCustomerConfigMapper;
import com.br.marketing.service.ICustomerConfigService;
import com.br.marketing.util.EncAndDecUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class CustomerConfigServiceImpl implements ICustomerConfigService {

    @Resource
    MarketingCustomerConfigMapper customerConfigMapper;

    @Autowired
    RedisChgService redisChgService;

    @Override
    public Result<Integer> getEncryptyType(String apiCode) {
        String encrypty = "";
        String key = RedisKeyConstant.encryptyKey.concat(":").concat(apiCode);
        try {
            encrypty = redisChgService.get(key);
            if (StringUtils.isNotBlank(encrypty)) {
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Integer.valueOf(encrypty));
            }
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
        }

        MarketingCustomerConfigExample customerConfigExample = new MarketingCustomerConfigExample();
        customerConfigExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID).andApiCodeEqualTo(apiCode);
        List<MarketingCustomerConfig> marketingCustomerConfigs = customerConfigMapper.selectByExample(customerConfigExample);
        if(marketingCustomerConfigs.size()>0){
            MarketingCustomerConfig marketingCustomerConfig = marketingCustomerConfigs.get(0);
            if(marketingCustomerConfig.getThreeKEncryptType() != null && marketingCustomerConfig.getThreeKEncryptType()>0){
                try {
                    redisChgService.set(key, marketingCustomerConfig.getThreeKEncryptType().toString());
                }catch (Exception ex){
                    log.error(ex.getMessage(),ex);
                }
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(marketingCustomerConfig.getThreeKEncryptType());
            }
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该apiCode未配置加密类型");
    }

    @Override
    public Result updateEncryptyType(String apiCode, Integer type) {
        String key = RedisKeyConstant.encryptyKey.concat(":").concat(apiCode);
        MarketingCustomerConfigExample customerConfigExample = new MarketingCustomerConfigExample();
        customerConfigExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID).andApiCodeEqualTo(apiCode);
        List<MarketingCustomerConfig> marketingCustomerConfigs = customerConfigMapper.selectByExample(customerConfigExample);

        //修改
        if(marketingCustomerConfigs.size()<=0){
            return new Result().setCode(ResultCode.FAIL.getValue());
        }

        MarketingCustomerConfig customerConfig = marketingCustomerConfigs.get(0);
        MarketingCustomerConfig updateEntity = new MarketingCustomerConfig();
        updateEntity.setId(customerConfig.getId());
        updateEntity.setThreeKEncryptType(type);
        customerConfigMapper.updateByPrimaryKeySelective(updateEntity);
        if(redisChgService.exists(key)){
            redisChgService.del(key);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<String> getThreeKeyDigToLog(String apiCode, String content, ThreeKeyTypeEnum threeKeyTypeEnum) {
        Result<Integer> encryptyTypeRes = getEncryptyType(apiCode);
        if(!ResultCode.SUCCESS.getValue().equals(encryptyTypeRes.getCode())){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(encryptyTypeRes.getMessage());
        }
        Integer data = encryptyTypeRes.getData();
        ThreeKeyEncryptEnum threeKeyEncryptEnum = ThreeKeyEncryptEnum.getThreeKeyEncryptEnum(data);
        if(threeKeyEncryptEnum == null){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该客户配置的加密类型解析错误");
        }
        return EncAndDecUtil.digestToLog(content, threeKeyTypeEnum, threeKeyEncryptEnum);
    }

    @Override
    public Result<String> getThreeKeyLogToDig(String apiCode, String content) {
        Result<Integer> encryptyTypeRes = getEncryptyType(apiCode);
        if(!ResultCode.SUCCESS.getValue().equals(encryptyTypeRes.getCode())){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(encryptyTypeRes.getMessage());
        }
        Integer data = encryptyTypeRes.getData();
        ThreeKeyEncryptEnum threeKeyEncryptEnum = ThreeKeyEncryptEnum.getThreeKeyEncryptEnum(data);
        if(threeKeyEncryptEnum == null){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该客户配置的加密类型解析错误");
        }
        String s = EncAndDecUtil.logTodigest(content, threeKeyEncryptEnum);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(s);
    }
}
