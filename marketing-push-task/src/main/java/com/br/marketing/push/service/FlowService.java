package com.br.marketing.push.service;

import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.RabbitMqSenderUtils;
import com.br.marketing.entity.Customer;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.push.PushApplication;
import com.br.marketing.push.service.impl.MergeServiceImpl;
import com.br.marketing.push.service.impl.PushServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * //				    _ooOoo_
 * //				   o8888888o
 * //				   88" . "88
 * //				   (| -_- |)
 * //				   O\  =  /O
 * //			    ____/`---'\____
 * //			  .'  \\|     |//  `.
 * //		     /  \\|||  :  |||//  \
 * //		    /  _|||||--:--|||||_  \
 * //		    | / | \\\  -  /// | \ |
 * //		    | \_|  ''\-:-/''  |_/ |
 * //		    \  .-\__  `-`  ___/-. /
 * //		  ___`...'  /--.--\  '...`___
 * //	   ."" '< `.___\_<|>_/___.'  >' "".
 * //	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 * //	    \ \ `-.  \_ __\ /__ _/  .-` / /
 * // ======`-.____`-.____\____/.-`____.-`======
 * //				    `=---='
 * //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //			  Buddha Bless, No Bug !
 *
 * @Author xiaoxin.pang
 * @Date 2021/5/7 16:10
 * @Description:
 **/
@Service
@Slf4j
public class FlowService {

    @Resource(name = "rabbitTemplate")
    private RabbitTemplate rabbitTemplate;
    public void flow(Customer customer){
        List<LoanFile> pushList;
        try {
            /**
             * 文件合并
             */
            MergeService mergeService= PushApplication.ac.getBean(MergeServiceImpl.class);
             pushList =mergeService.process(customer);

//
            /**
             * 文件推送
             */
            if(pushList !=null&&pushList.size()>0){
                PushService pushService= PushApplication.ac.getBean(PushServiceImpl.class);
                pushService.push(pushList);

                //推送消息到pushQueue，进行下一流程处理
                RabbitMqSenderUtils.convertAndSendPriority(rabbitTemplate, MQConstants.exchangerName, MQConstants.checkRoutingKey,customer.getApiCode());
            }

        } catch (Exception e) {
            log.error("推送服务异常，apiCode={},",customer.getApiCode(),e);
        }

    }
}
