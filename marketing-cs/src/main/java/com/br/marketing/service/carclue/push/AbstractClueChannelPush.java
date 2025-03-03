package com.br.marketing.service.carclue.push;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;
import org.springframework.stereotype.Service;

@Service
public abstract class AbstractClueChannelPush {

    /**
     * 组装和推送逻辑
     * @param carClueInfo
     * @return
     */
    public abstract Result push(CarClueInfo carClueInfo, Integer retry);

    /**
     * 过滤规则的名称
     * @return
     */
    public abstract String label();
}
