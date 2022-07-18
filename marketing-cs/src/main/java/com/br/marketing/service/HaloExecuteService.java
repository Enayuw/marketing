package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingSyncUser;

/**
 * @author guangchao.zhang
 * @Classname HaloExecuteService
 * @Description 哈啰数据填充
 * @Date 2022/2/14 6:21 PM
 */
public interface HaloExecuteService {
    /**
     * 拨打数据执行器
     * @param marketingSyncUser
     * @return
     */
    Result execute(MarketingSyncUser marketingSyncUser);
}
