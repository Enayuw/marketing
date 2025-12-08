package com.br.marketing.origin;

import com.br.marketing.rule.InterfaceParams;
import lombok.Data;

import java.util.Set;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : Marketing_Universal_Transfer_Receive mq队列中消息字段
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/12 15:50
 */

@Data
public class MqFact extends BaseMqFact {

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
     * 数据需要执行的规则，非静置数据该字段为空
     */

    private Set<String> includeRules;

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
