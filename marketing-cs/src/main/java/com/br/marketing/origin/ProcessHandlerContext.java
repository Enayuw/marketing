package com.br.marketing.origin;

import com.br.marketing.entity.MarketingSyncUser;
import lombok.Data;

import java.util.Map;

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
 * @Description : 从接收mq消息到处理过程，所需信息放在处理上下文中
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/12 16:26
 */
@Data
public class ProcessHandlerContext {

    /**
     * 客户apiCode
     */
    private String apiCode;

    /**
     * 转化表id
     */
    private Long transferInfoId;

    /**
     * 海尔客服转化所需信息
     */
    Map<String, MarketingSyncUser> customerMap;


    public ProcessHandlerContext(){}


    public ProcessHandlerContext(String apiCode, long transferInfoId, Map<String, MarketingSyncUser> map){
        this.apiCode = apiCode;
        this.transferInfoId = transferInfoId;
        this.customerMap = map;
    }


}
