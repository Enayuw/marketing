package com.br.marketing.service;

import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.MarketingDataFileConfig;

import java.io.IOException;

/**
 * 数据清洗处理接口
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-24
 */
public interface DataCleaningAutoService {


    /**
     * 自动清洗逻辑
     */
    void autoCleanDataByTask(MarketingCleanDataTask marketingCleanDataTask) ;

    /**
     * 保存清洗任务
     */
    Long saveCleanTask(String apiCode,Integer cleanType,String configName);

}
