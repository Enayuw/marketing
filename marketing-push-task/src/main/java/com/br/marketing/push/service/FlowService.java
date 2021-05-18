package com.br.marketing.push.service;

import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.RabbitMqSenderUtils;
import com.br.marketing.entity.Customer;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.push.PushApplication;
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
    @Resource
    CustomerMapper customerMapper;
    @Resource(name = "rabbitTemplate")
    private RabbitTemplate rabbitTemplate;
    public void flow(String apiCode){
        Customer customer=customerMapper.getCustomerByApiCode(apiCode);
        List<LoanFile> pushList;
        try {
            /**
             * 文件合并
             */
            Class mergeServiceClass =Class.forName(customer.getMergeServiceName());
            MergeService mergeService=(MergeService) PushApplication.ac.getBean(mergeServiceClass);
             pushList =mergeService.process(customer.getApiCode());

//            /**
//             * 文件过滤
//             */
//            if(true){
//            Class mergeServiceClass1=Class.forName(customer.getPushServiceName());
//            MergeService mergeService1=(MergeService) PushApplication.ac.getBean(mergeServiceClass1);
//             pushList =mergeService.process(customer.getApiCode());
//            }
//
//            /**
//             * 文件压缩
//             */
            /**
             * 文件推送
             */
            if(pushList !=null&&pushList.size()>0){
                Class pushClass=Class.forName(customer.getPushServiceName());
                PushService pushService=(PushService) PushApplication.ac.getBean(pushClass);
                pushService.push(pushList);

                //推送消息到pushQueue，进行下一流程处理
                RabbitMqSenderUtils.convertAndSendPriority(rabbitTemplate, MQConstants.exchangerName, MQConstants.checkRoutingKey,apiCode);
            }

        } catch (ClassNotFoundException e) {
            log.error("实现类未找到，apiCode={},serviceName={}",customer.getApiCode(),customer.getPushServiceName());
        }

    }
}
