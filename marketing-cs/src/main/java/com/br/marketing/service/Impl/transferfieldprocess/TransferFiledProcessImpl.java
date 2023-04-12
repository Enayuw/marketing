package com.br.marketing.service.Impl.transferfieldprocess;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.TransferFieldProcessFactory;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransferFiledProcessImpl {

    @Autowired
    ApplicationContext applicationContext;

    Map<String, List<TransferFieldProcessFactory>> map;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @PostConstruct
    void init() {
        Map<String, TransferFieldProcessFactory> beans = applicationContext.getBeansOfType(TransferFieldProcessFactory.class);
        this.map = beans.values().stream().collect(Collectors.groupingBy(t -> t.customerName()));
    }

    public TransferFieldProcessFactory getTransferFieldProcessFactory(String apiCode) {
        Map<String, String> transferProcessFieldApiCode = marketingCommonConfig.getTransferProcessFieldApiCode();
        String s = transferProcessFieldApiCode.get(apiCode);
        if (StringUtils.isBlank(s)) {
            return null;
        }

        List<TransferFieldProcessFactory> transferFieldProcessFactories = this.map.get(s);
        if (transferFieldProcessFactories.size() <= 0) {
            return null;
        }
        return transferFieldProcessFactories.get(0);
    }
}
