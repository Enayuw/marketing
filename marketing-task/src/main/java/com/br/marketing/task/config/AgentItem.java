package com.br.marketing.task.config;

import com.br.speed.client.common.annotations.SpeedItem;
import lombok.Data;
import org.springframework.stereotype.Service;

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
 * @Author :
 * @Date : Create in 2018/8/1 10:36
 */

@Service
@Data
public class AgentItem {
    private String status;

    @SpeedItem(key = "marketing-shutdown",topic = "compass_topic")
    public String getStatus(){
        return status;
    }
}
