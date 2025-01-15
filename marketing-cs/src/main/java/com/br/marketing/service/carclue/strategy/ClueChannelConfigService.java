package com.br.marketing.service.carclue.strategy;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;

import java.util.List;

/**
 * 车线索配置信息
 */
public interface ClueChannelConfigService {

    /**
     * 根据规则获取渠道商
     * @param label
     * @param type
     * @return
     */
    String getChannelApiCode(String label,Integer type);


    List<AbstractClueChannelMatch> getChannelMatch();


    /**
     * 更新车线索配置
     * @return
     */
    Result updateClueConfig();
}
