package com.br.marketing.origin;

import lombok.Data;

import java.util.Set;


@Data
public class MrpMqFact extends BaseMqFact{

    /**
     * apiCode信息
     */
    private String apiCode;

    /**
     *  mq中数据id
     */
    private Long sourceId;

    /**
     *  消息来源 数据来源于 TransferSource枚举类
     * @see com.br.marketing.origin.TransferSource
     */
    private Integer source;

    /**
     * 编排的规则名称
     */
    private String ruleScene;

    /**
     * mq中消息内容
     */
    private String message;

    /**
     * 是否为延迟队列的消息 1:是
     */
    private Integer isDelay;

    /**
     * 延迟时间；单位小时
     * eg:1或0.5
     */
    private float delayTime;

}
