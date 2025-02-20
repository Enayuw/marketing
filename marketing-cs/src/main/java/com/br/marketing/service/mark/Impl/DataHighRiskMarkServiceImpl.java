package com.br.marketing.service.mark.Impl;

import com.br.marketing.service.mark.DataHighRiskMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description 高风险打标实现
 * @author hedongshuo
 * @date 2025/2/19 21:30
 **/
@Service
@Slf4j
public class DataHighRiskMarkServiceImpl implements DataHighRiskMarkService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process() {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            //1.查询【b_local_file】

        });




    }
}
