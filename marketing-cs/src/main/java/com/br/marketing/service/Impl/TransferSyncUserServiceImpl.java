package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.ITransferSyncUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 客户转化数据记录业务接口 实现类
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/15 10:00
 */
@Service
@Slf4j
public class TransferSyncUserServiceImpl implements ITransferSyncUserService {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    public int insertSelective(MarketingTransferSyncUser marketingTransferSyncUser) {
        return marketingTransferSyncUserMapper.insertSelective(marketingTransferSyncUser);
    }
}
