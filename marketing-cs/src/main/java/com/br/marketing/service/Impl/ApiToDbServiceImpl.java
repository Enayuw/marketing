package com.br.marketing.service.Impl;

import com.br.common.util.BrExecutors;
import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.entity.MarketingSyncInfoExample;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.service.IApiToDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ApiToDbServiceImpl  implements IApiToDbService {

    @Autowired
    MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    MarketingSyncInfoMapper syncInfoMapper;

    @Autowired
    MarketingUserMapper marketingUserMapper;

    @Override
    public Result pushToDb() {
        Date date = new Date();
        String nowDate = DateUtils.format(date, "yyyy-MM-dd");
        Date preDate = DateUtils.getDate2(LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(new Byte("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);

        MarketingSyncInfoExample syncInfoExample = new MarketingSyncInfoExample();
        syncInfoExample.createCriteria().andCreateTimeGreaterThanOrEqualTo(preDate)
                .andCreateTimeLessThan(DateUtils.getDate2(nowDate));
        List<MarketingSyncInfo> marketingSyncInfos = syncInfoMapper.selectByExample(syncInfoExample);

        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            boolean b = marketingSyncInfos.stream().anyMatch(t -> t.getApiCode().equals(marketingCustomer.getApiCode())
                    && t.getStatus().equals(1));
            if(b){
                // 该apicode还有清洗未完成的数据
                continue;
            }
            List<MarketingSyncInfo> syncInfos = marketingSyncInfos.stream()
                    .filter(t -> t.getApiCode().equals(marketingCustomer.getApiCode())
                            && Arrays.asList(2, 4).contains(t.getStatus())).collect(Collectors.toList());
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
            List<Future> resList = new ArrayList<>();
            for (MarketingSyncInfo syncInfo : syncInfos) {
                String requestBatch = syncInfo.getRequestBatch();
                String apiCode = syncInfo.getApiCode();
                resList.add(threadPool.submit(()->{
//                    apiCode
                    String s = DateUtils.formatForDate2(new Date());
                    marketingUserMapper.insertSelectByRequestId(apiCode,"",s,requestBatch);
                }));

            }
        }
        return null;
    }
}
