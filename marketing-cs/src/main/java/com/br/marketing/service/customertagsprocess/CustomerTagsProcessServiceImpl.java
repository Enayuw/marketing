package com.br.marketing.service.customertagsprocess;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingCustomerConfig;
import com.br.marketing.entity.MarketingCustomerConfigExample;
import com.br.marketing.mapper.MarketingCustomerConfigMapper;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CustomerTagsProcessServiceImpl {

    @Resource
    RedisChgService redisChgService;

    @Resource
    MarketingCustomerConfigMapper marketingCustomerConfigMapper;

    @Resource
    Map<String, IUploadCheckService> iUploadCheckServiceMap;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    final int customerExpireTime = 5 * 60;

    final String errorMsgPrefix = "【获取客户标签】";


    /**
     * 获取客户tags
     *
     * @param apiCode
     * @return
     */
    public CustomerTagsVO getTags(String apiCode) {

        CustomerTagsVO customerTagsVOByRedis = getTagsOfRedis(apiCode);
        if (customerTagsVOByRedis != null) {
            return customerTagsVOByRedis;
        }

        CustomerTagsVO customerTagsVO = new CustomerTagsVO();
        MarketingCustomerConfigExample configExample = new MarketingCustomerConfigExample();
        configExample.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(Constants.DATA_VALID);
        List<MarketingCustomerConfig> configs = marketingCustomerConfigMapper.selectByExample(configExample);
        if (configs.size() <= 0) {
            customerTagsVO.setCheckType(CustomerTagsValue.CheckTypeEnum.CHECKCELL.getValue());
            customerTagsVO.setPushJc3keyType(CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue());
            writeTagsOfRedis(apiCode, customerTagsVO);
            return customerTagsVO;
        }

        MarketingCustomerConfig marketingCustomerConfig = configs.get(0);
        Integer checkType = marketingCustomerConfig.getCheckType();
        customerTagsVO.setCheckType(checkType);
        customerTagsVO.setPushJc3keyType(
                marketingCustomerConfig.getThreeKEncryptType() == null
                        ? CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue()
                        : marketingCustomerConfig.getThreeKEncryptType());
        writeTagsOfRedis(apiCode, customerTagsVO);
        return customerTagsVO;
    }

    /**
     * 获取上传数据校验3k的方法
     *
     * @param vo
     * @return
     */
    public IUploadCheckService getIUploadCheckService(CustomerTagsVO vo) {

        CustomerTagsValue.CheckTypeEnum enumByValue = CustomerTagsValue.getEnumByValue(vo.getCheckType(), CustomerTagsValue.CheckTypeEnum.class);
        if (enumByValue == null) {
            return null;
        }
        IUploadCheckService iUploadCheckService = iUploadCheckServiceMap.get(enumByValue.getBean());
        return iUploadCheckService;
    }

    private CustomerTagsVO getTagsOfRedis(String apiCode) {
        String key = RedisKeyConstant.CUSTOMERTAGS.concat(":").concat(apiCode);
        try {
            if (redisChgService.exists(key)) {
                String s = redisChgService.get(key);
                if (StringUtils.isNotBlank(s)) {
                    CustomerTagsVO vo = JSON.parseObject(s, CustomerTagsVO.class);
                    return vo;
                }
            }
        } catch (Exception ex) {
            log.error(errorMsgPrefix.concat("【redis获取客户信息失败】").concat(ex.toString()), ex);
        }
        return null;
    }

    private void writeTagsOfRedis(String apiCode, CustomerTagsVO customerTagsVO) {
        String key = RedisKeyConstant.CUSTOMERTAGS.concat(":").concat(apiCode);
        try {
            String voStr = JSON.toJSONString(customerTagsVO);
            redisChgService.setex(key, voStr, customerExpireTime);
        } catch (Exception ex) {
            log.error(errorMsgPrefix.concat("【redis写入客户信息失败】").concat(ex.toString()), ex);
        }
    }

    public void delTagsOfRedis(String apiCode) {
        String key = RedisKeyConstant.CUSTOMERTAGS.concat(":").concat(apiCode);
        try {
            if (redisChgService.exists(key)) {
                redisChgService.del(key);
            }
        } catch (Exception ex) {
            log.error(errorMsgPrefix.concat("【redis删除客户标签信息失败】").concat(ex.toString()), ex);
        }
    }

}
