package com.br.marketing.service.tccpa.impl;

import com.br.marketing.service.tccpa.TcCpaCollidingDataFilterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TcCpaCollidingDataFilterServiceImpl implements TcCpaCollidingDataFilterService {

    private final static String TITLE = "【同程易融CPA-撞库数据过滤Job】";

    @Override
    public void process() {
        //
        log.info("{} 开始执行", TITLE);
    }
}
