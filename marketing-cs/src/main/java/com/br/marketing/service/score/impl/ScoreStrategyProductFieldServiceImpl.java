package com.br.marketing.service.score.impl;

import com.br.marketing.mapper.score.ScoreCustomerStrategyProductFieldMapper;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.service.score.IScoreStrategyProductFieldService;
import com.br.marketing.vo.MarketingCustomerVO;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 评分产品字段业务处理
 *
 * @author Hua Qiang
 * @date 2024-08-15 15:33
 */
@Service
public class ScoreStrategyProductFieldServiceImpl implements IScoreStrategyProductFieldService {

    @Resource
    private ScoreCustomerStrategyProductFieldMapper scoreCustomerStrategyProductFieldMapper;

    @Resource
    private MarketingCustomerService marketingCustomerService;

    @Override
    public List<String> getFieldNamePage(String apiCode, int current, int size) {
        List<MarketingCustomerVO> apiCodeList = marketingCustomerService.getApiCodeList(apiCode);
        // 检查客户信息是否正常
        if (apiCodeList.isEmpty()) {
            return Collections.emptyList();
        }
        MarketingCustomerVO marketingCustomerVO = null;
        // 遍历找到满足当前apiCode的客户信息
        for (MarketingCustomerVO customerVO : apiCodeList) {
            if (apiCode.equals(customerVO.getApiCode())) {
                marketingCustomerVO = customerVO;
                break;
            }
        }
        if (ObjectUtils.isEmpty(marketingCustomerVO)) {
            return Collections.emptyList();
        }
        String customerId = marketingCustomerVO.getId();
        // 查询析出字段
        return scoreCustomerStrategyProductFieldMapper.getFieldNamePage(customerId
                , (current - 1) * size, size);
    }
}
