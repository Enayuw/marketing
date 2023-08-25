package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.service.ValidityPeriodDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 描述：： 根据有效期框定数据范围实现
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ValidityPeriodDataServiceImpl
 * @author: it-yml
 * @create: 2023-08-25 21:24
 * @Version 1.0
 * --------------------------------------
 **/
@Service
@Slf4j
public class ValidityPeriodDataServiceImpl implements ValidityPeriodDataService {
    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;
    @Override
    public Boolean getMarketingTransferDataWithValidityPeriod(String apiCode,String cell) {
        MarketingDataValidConfig marketingTransferDataWithValidityPeriod = marketingDataValidConfigMapper.getMarketingTransferDataWithValidityPeriod(apiCode);
        if(marketingTransferDataWithValidityPeriod != null){
            String validStartDate = marketingTransferDataWithValidityPeriod.getValidStartDate();
            String validEndDate = marketingTransferDataWithValidityPeriod.getValidEndDate();

        }

        return null;
    }
}
