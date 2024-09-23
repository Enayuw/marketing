package com.br.marketing.entity.rocketmq;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * RocketMQ开关对象
 * global: 全局开关 true会让全部生产者生产消息到 RocketMQ
 * group: <key,value>
 *   key:表示配置启用RocketMQ的tag
 *   value:
 *     flag: tag对应队列全局开关 true会让全部apiCode生产消息到 RocketMQ
 *     apiCodes: 配置启用RocketMQ的apiCode,多个以逗号分隔
 * 完整样例：
 * {
 * 	 "global": "true",
 * 	 "group": {
 * 	   "Marketing.PreUser.Receive": {
 * 	   	 "flag": true,
 * 	   	 "apiCodes": "7410950,7410951"
 *     },
 * 	   "Marketing.PreUser.Receive.Small": {
 * 	   	 "flag": true,
 * 	   	 "apiCodes": "7410950,7410951"
 *     }
 *   }
 * }
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-22
 */
@Data
public class RocketMQSwitchEntity {
    /**
     * 全局开关
     */
    private String global;
    /**
     * 按照tag分类
     * {
     *   "Marketing.PreUser.Receive": {
     *   	"flag": true,
     *   	"apiCodes": "7410950,7410951"
     *   },
     *   "Marketing.PreUser.Receive.Small": {
     *   	"flag": true,
     *   	"apiCodes": "7410950,7410951"
     *   }
     * }
     */
    private JSONObject group;

}
