package com.br.marketing.entity.rocketmq;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * RocketMQ开关对象
 * global: 全局开关 true会让全部生产者生产消息到 RocketMQ
 * group: <key，value>
 *   key:表示配置启用RocketMQ的tag
 *   value:
 *     flag: tag对应队列全局开关 true会让全部apiCode生产消息到RocketMQ；false则不会使用RocketMQ（apiCodes配置会失效），默认不开启
 *     apiCodes: 配置启用RocketMQ的apiCode，多个以逗号分隔，默认不开启
 *     msgUUIdFlag: 消息是否添加id标签，为false时，则不添加 id;为ture时，则添加 id，默认开启
 *     msgIdemFlag: 消息是否需要使用id去重，为false时，则消息不去重，设置为true时，则消息去重，默认开启msgUUIdFlag=true，默认开启
 *     allowReprocessSeconds:  消息允许重复处理的时间间隔，大于-1时，配置的apiCode会启用RocketMQ;为空或等于-1时，则表示不启用Rocket，默认不开启
 *     printLog: 是否打印日志，为false时，则不打印日志;为ture时，则会打印日志，默认不开启
 * appNameFlag: <key，value>，appName对应队列全局开关，非必须配置
 *     key: 配置启用RocketMQ的appName
 *     value: value 为false或非布尔值且不为空时，则不会使用“group”配置的内容
 *       flag: appName对应队列全局开关，为false时，则不会使用RocketMQ;为ture时，则会使用RocketMQ(还会根据“group”配置判断启用RocketMQ)，默认不开启
 *       apiCodes: 配置启用RocketMQ的apiCode，多个以逗号分隔，不为空时，配置的apiCode会启用RocketMQ;为空时，则表示不启用Rocket(还会根据“group”配置判断启用RocketMQ)，默认不开启
 *       msgUUIdFlag: 消息是否添加id标签，为false时，则不添加 id;为ture时，则添加 id(还会根据“group”配置判断启用RocketMQ)，默认开启
 *       msgIdemFlag: 消息是否需要使用id去重，设置为true时，默认开启msgUUIdFlag=true，为false时，则不会使用RocketMQ，为ture时，则会使用RocketMQ(还会根据“group”配置判断启用RocketMQ)，默认开启
 *       allowReprocessSeconds:  消息允许重复处理的时间间隔，大于-1时，配置的apiCode会启用RocketMQ;为空或等于-1时，则表示不启用Rocket(为空时还会根据“group”配置判断启用RocketMQ)，默认不开启
 *       printLog: 是否打印日志，为false时，则不打印日志;为ture时，则会打印日志(还会根据“group”配置判断启用RocketMQ)，默认不开启
 * 完整样例：
 * {
 *     "global": "true"，
 *     "group": {
 *         "Marketing.PreUser.Receive": {
 *             "flag": true，
 *             "apiCodes": "7410950，7410951"，
 *             "msgUUIdFlag": true，
 *             "msgIdemFlag": true，
 *             "allowReprocessSeconds": 60，
 *             "printLog": false
 *         }
 *     }，
 *     "appNameFlag": {
 *        "marketing-mq-consumer": {
 *            "flag": true，
 *            "apiCodes": "7410950，7410951"，
 *            "msgUUIdFlag": true，
 *            "msgIdemFlag": true，
 *            "allowReprocessSeconds": 60，
 *            "printLog": false
 *        }
 *     }
 * }
 *
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-22
 */
@Data
public class RocketMqSwitchEntity {
    /**
     * 全局开关
     */
    private Boolean global;
    /**
     * 按照tag分类
     * {
     *     "Marketing.PreUser.Receive": {
     *         "flag": true，
     *         "apiCodes": "7410950，7410951"，
     *         "msgUUIdFlag": true，
     *         "msgIdemFlag": true，
     *         "allowReprocessSeconds": 60，
     *         "printLog": false
     *     }
     * }
     */
    private JSONObject group;

    /**
     * appNameFlag
     *     {
     *       "marketing-mq-consumer": {
     *           "flag": true，
     *           "apiCodes": "7410950，7410951"，
     *           "msgUUIdFlag": true，
     *           "msgIdemFlag": true，
     *           "allowReprocessSeconds": 60，
     *           "printLog": false
     *       }
     *     }
     */
    private JSONObject appNameFlag;

}
