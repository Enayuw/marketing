package com.br.marketing.datarelayservice.service.impl;

import com.br.marketing.entity.MarketingCustomerOriginalData;
import com.br.marketing.entity.ZhongYuanAgent;
import com.br.marketing.mapper.ZhongYuanAgentMapper;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 中原坐席导入落库（独立事务），方法返回时事务已提交，便于与 MQ 发送顺序解耦。
 *
 * @see com.br.marketing.service.Impl.CallRecordInsertService#insertData
 */
@Service
@Slf4j
public class ZhongYuanAgentImportPersistService {

    @Resource
    private ZhongYuanAgentMapper zhongYuanAgentMapper;
    @Resource
    private MarketingCustomerOriginalDataMapper marketingCustomerOriginalDataMapper;

    @Transactional(rollbackFor = Exception.class)
    public Long insertAgentAndOriginal(ZhongYuanAgent agent, MarketingCustomerOriginalData original) {
        zhongYuanAgentMapper.insertSelective(agent);
        marketingCustomerOriginalDataMapper.insertSelective(original);
        Long dataId = original.getId();
        log.warn("中原坐席批量导入落库完成, requestNo={}, originalDataId={}", agent.getRequestNo(), dataId);
        return dataId;
    }
}
