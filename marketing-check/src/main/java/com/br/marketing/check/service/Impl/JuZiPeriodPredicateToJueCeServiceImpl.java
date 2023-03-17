package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.JuZiPeriodPredicateService;
import com.br.marketing.entity.MarketingTransferSyncUserCell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/17 16:04
 */
@Service
@Slf4j
public class JuZiPeriodPredicateToJueCeServiceImpl implements JuZiPeriodPredicateService {
    private final static Set<String> statusSet = new HashSet<String>(){{
        add("d");
    }};
    @Override
    public void transferDataPeriod(String status, List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellList) {
        // 推决策
        if(statusSet.contains(status)){

        }
    }
}
