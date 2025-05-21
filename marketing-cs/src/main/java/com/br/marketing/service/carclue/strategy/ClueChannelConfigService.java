package com.br.marketing.service.carclue.strategy;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.service.carclue.callback.AbstractClueChannelCallBack;
import com.br.marketing.service.carclue.config.AbstractClueChannelConfig;
import com.br.marketing.service.carclue.filter.AbstractClueChannelFilter;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import com.br.marketing.service.carclue.push.AbstractClueChannelPush;

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

    /**
     * 获取有效的渠道商匹配配置
     * @return
     */
    List<AbstractClueChannelMatch> getChannelMatch();

    /**
     * 获取指定渠道商的过滤实现
     * @param apiCodeChannel
     * @return
     */
    List<AbstractClueChannelFilter> getChannelFilter(String apiCodeChannel);

    /**
     * 获取指定渠道商的推送实现
     * @param apiCodeChannel
     * @return
     */
    AbstractClueChannelPush getChannelPushImpl(String apiCodeChannel);

    /**
     * 获取渠道商的回调实现
     * @param apiCodeChannel
     * @return
     */
    AbstractClueChannelCallBack getChannelCallBackImpl(String apiCodeChannel);


    /**
     * 获取指定渠道商的匹配实现
     * @param apiCodeChannel
     * @return
     */
    AbstractClueChannelMatch getChannelMatchImpl(String apiCodeChannel);

    /**
     * 获取指定渠道商的配置实现
     * @param apiCodeChannel
     * @return
     */
    AbstractClueChannelConfig getChannelConfigImpl(String apiCodeChannel);


    /**
     * 更新车线索配置
     * @return
     */
    Result updateClueConfig();
}
