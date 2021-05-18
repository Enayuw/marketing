package com.br.marketing.task.config;

import com.br.speed.client.SpeedMgrBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;

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
 * @Description : 新版配置中心配置类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2018/7/31 14:10
 */
@Configuration
public class SpeedConfig {
    @Bean
    public SpeedMgrBean speedMgrBean(){
        SpeedMgrBean speedMgrBean = new SpeedMgrBean();
        speedMgrBean.setScanPackage("com.br");
        return speedMgrBean;
    }
    //阻塞队列，FIFO
    public static LinkedBlockingQueue<String> concurrentLinkedQueue = new LinkedBlockingQueue<>();

    //应用上下文对象
    public static ApplicationContext context= null;

}
