package com.br.marketing.speedconfig;

import com.alibaba.fastjson.annotation.JSONField;
import com.br.speed.client.common.annotations.SpeedItem;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

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
 *
 * @Description : 商户、产品信息变动接收类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2018/8/1 10:36
 */

@Configuration
@Data
public class AgentItem {

    private String message;
    @JSONField(name = "update_time")
    private Date updateTime;

    private Integer redisTest;

    private Integer speedTest;

    private String callRecordConfig;

    private String cybotstarAgentConfig;

    @SpeedItem(topic = "marketing",key = "marketing_broadcast_notice_item")
    public String getMessage(){
        return message;
    }

}
