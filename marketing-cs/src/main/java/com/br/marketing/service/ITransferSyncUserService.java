package com.br.marketing.service;

import com.br.marketing.entity.MarketingTransferSyncUser;

/**
 * 客户转化数据记录业务接口
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/15 10:00
 */
public interface ITransferSyncUserService {

    int insertSelective(MarketingTransferSyncUser marketingTransferSyncUser);
}
