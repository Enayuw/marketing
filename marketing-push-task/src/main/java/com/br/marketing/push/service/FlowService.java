package com.br.marketing.push.service;

import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.Customer;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.push.PushApplication;
import com.br.marketing.push.service.impl.MergeServiceImpl;
import com.br.marketing.service.sftp.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    PushService pushService;

    @Resource
    private RocketMqSwitch rocketMqSwitch;

    public void flow(Customer customer){
        List<LoanFile> pushList;
        try {

//            Boolean action = iCompatibleService.isAction(customer.getExtendConfigInfo());
//            if(!action){
//                return;
//            }
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
                //PushService pushService= PushApplication.ac.getBean(PushServiceImpl.class);
                pushService.push(pushList);

                for (LoanFile loanFile : pushList) {
                    //推送消息到pushQueue，进行下一流程处理
//                    producter.send(MQConstants.CHECK_ROUTING_KEY,loanFile.getId().toString());
                    rocketMqSwitch.sendMessage(loanFile.getApiCode(),
                            MarketingAssistConstants.TOPIC,
                            MarketingAssistConstants.TAG_CHECK_QUEUE,
                            loanFile.getId().toString(),
                            MQConstants.CHECK_ROUTING_KEY);
                }
            }

        } catch (Exception e) {
            log.error("推送服务异常，apiCode={},",customer.getApiCode(),e);
        }

    }
}
