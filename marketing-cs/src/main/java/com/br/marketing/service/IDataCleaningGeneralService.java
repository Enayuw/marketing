package com.br.marketing.service;

import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.MarketingDataFileConfig;

/**
 * 数据清洗处理接口
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-24
 */
public interface IDataCleaningGeneralService {

    /**
     * 从任务表中获取一个清洗任务
     * @Author yu.xia@brgroup.com
     * @Date 2024/5/24 16:24
     * @return MarketingCleanDataTask
     */
    MarketingCleanDataTask getAction();
    /**
     * 读取文件并进行清洗
     * @Author yu.xia@brgroup.com
     * @Date 2024/5/24 11:31
     * @param task 从数据库中查询到的任务
     * @param iFileToMarketingRuleService 具体处理映射的对象
     * @param marketingDataFileConfig 任务对应的规则映射配置
     */
    void action(MarketingCleanDataTask task, IFileToMarketingRuleService iFileToMarketingRuleService,
                MarketingDataFileConfig marketingDataFileConfig);
    /**
     * 数据清洗试跑
     * @Author yu.xia@brgroup.com
     * @Date 2024/5/24 11:31
     * @param id 试跑任务的id
     */
    MarketingPreUserDTO pilotAction(Long id);

}
