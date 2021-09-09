package com.br.marketing.service.Impl;

import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.vo.CustomerSelectVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户业务逻辑实现
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 15:34
 */
@Service
public class MarketingCustomerServiceImpl implements MarketingCustomerService {

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Override
    public List<CustomerSelectVO> getCidOrApiCodeList(String cid) {
        List<CustomerSelectVO> cidOrApiCodeList = marketingCustomerMapper.getCidOrApiCodeList(cid);
        if (StringUtils.isEmpty(cid)) {
            return cidOrApiCodeList.stream().distinct().collect(Collectors.toList());
        }
        return cidOrApiCodeList;
    }
}
