package com.br.marketing.service.customertagsprocess;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
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
        // 客户未配置信息 会缓存null值信息
        if (configs.size() <= 0) {
            customerTagsVO.setCheckType(null);
            customerTagsVO.setPushJc3keyType(null);
            writeTagsOfRedis(apiCode, customerTagsVO);
            return customerTagsVO;
        }

        // 客户配置 会缓存表中的信息
        MarketingCustomerConfig marketingCustomerConfig = configs.get(0);
        customerTagsVO.setCheckType(marketingCustomerConfig.getCheckType());
        customerTagsVO.setPushJc3keyType(marketingCustomerConfig.getThreeKEncryptType());
        customerTagsVO.setCipherMode(marketingCustomerConfig.getCipherMode());
        customerTagsVO.setPaddingScheme(marketingCustomerConfig.getPaddingScheme());
        customerTagsVO.setCharset(marketingCustomerConfig.getCharset());
        customerTagsVO.setIv(marketingCustomerConfig.getIv());
        customerTagsVO.setDynamicKeys(marketingCustomerConfig.getDynamicKeys());
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

        CustomerTagsValue.PushJc3keyTypeEnum enumByValue = CustomerTagsValue
                .getEnumByValue(
                        vo.getPushJc3keyType()
                        , CustomerTagsValue.PushJc3keyTypeEnum.class
                );
        if (enumByValue != null) {
            IUploadCheckService iUploadCheckService = iUploadCheckServiceMap.get(enumByValue.getStrategyBean());
            return iUploadCheckService;
        }

        IUploadCheckService iUploadCheckService = iUploadCheckServiceMap.get(CustomerTagsValue.PushJc3keyTypeEnum.PLAINTEXT.getStrategyBean());
        return iUploadCheckService;
    }

    /**
     * 客户的配置信息从redis读取
     *
     * @param apiCode
     * @return
     */
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

    /**
     * 客户的配置信息写入redis
     *
     * @param apiCode
     * @param customerTagsVO
     */
    private void writeTagsOfRedis(String apiCode, CustomerTagsVO customerTagsVO) {
        String key = RedisKeyConstant.CUSTOMERTAGS.concat(":").concat(apiCode);
        try {
            String voStr = JSON.toJSONString(customerTagsVO);
            redisChgService.setex(key, voStr, customerExpireTime);
        } catch (Exception ex) {
            log.error(errorMsgPrefix.concat("【redis写入客户信息失败】").concat(ex.toString()), ex);
        }
    }

    /**
     * 客户的配置信息从redis删除
     *
     * @param apiCode
     */
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
