package com.br.marketing.entity.rocketmq;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

/**
 * RocketMQ开关对象
 * 完整样例：
 * {
 * 	"global": "true",
 * 	"group": [{
 * 		"flag": true,
 * 		"tag": {
 * 			"Marketing.PreUser.Receive": "7410095,7410951"
 *      }
 *  }, {
 * 		"flag": true,
 * 		"tag": {
 * 			"Marketing.PreUser.Receive.Small": "7410095,7410951"
 *      }
 *  }]
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
     * 按照tag分组开关
     * Array中单个对象样例：
     * {
     * 	"flag": true,
     * 	"tag": {
     * 		"Marketing.PreUser.Receive": "7410095,7410951"
     * }
     */
    private JSONArray group;

}
