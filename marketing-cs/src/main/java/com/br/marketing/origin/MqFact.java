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
public class MqFact extends InterfaceParams {

    /**
     *  b_marketing_transfer_info 数据表中主键id
     */
    private Long transferInfoId;

    /**
     *  消息来源
     */
    private Integer source;

    /**
     * 数据需要执行的规则，非静置数据该字段为空
     */

    private Set<String> needExecuteRules;
}
