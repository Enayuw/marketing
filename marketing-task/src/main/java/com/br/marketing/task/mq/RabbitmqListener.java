package com.br.marketing.task.mq;

import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.RabbitMqSenderUtils;
import com.br.marketing.entity.Customer;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.task.Scheduler;
import com.br.marketing.task.service.Impl.LoanWarningServiceImpl;
import com.br.marketing.task.service.LoanWarningService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Component
@Slf4j
public class RabbitmqListener {
    @Resource
    private CustomerMapper customerMapper;
    @Resource(name = "rabbitTemplate")
    private RabbitTemplate rabbitTemplate;
    /**
     * 沙子项目任务处理
     * @param channel
     * @param message
     */
    @RabbitListener(queues = MQConstants.taskQueueName,containerFactory = "containerFactory")
    @RabbitHandler
    public void receiveMessage(Channel channel,Message message) {
        long startTime = System.currentTimeMillis();
        try {
            String msg = new String(message.getBody(), StandardCharsets.UTF_8);
            if (StringUtils.isBlank(msg)) {
                log.warn("============接收到的消息内容为空!===============");
                //消息的标识，false只确认当前一个消息收到，true确认所有consumer获得的消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
                return;
            }
            log.warn("==========接收到的消息内容为:{},返回给rabbitmq的Consumer tag为:{}",msg,message.getMessageProperties().getConsumerTag());

            Customer customer =customerMapper.getCustomerByApiCode(msg);
            LoanWarningService loanWarningService= Scheduler.ac.getBean(LoanWarningServiceImpl.class);
            //loanWarningService.process(customer);
            //推送消息到pushQueue，进行下一流程处理
            RabbitMqSenderUtils.convertAndSendPriority(rabbitTemplate,MQConstants.exchangerName, MQConstants.pushRoutingKey,customer.getApiCode());
            // 手动ack消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            log.warn("接收消息耗时：{}", (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            log.error("============处理消息异常!============",e);
            // 拒绝消息也相当于主动删除mq队列的消息
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            } catch (IOException e1) {
                log.error("taskQueue 拒绝消息",e1);
            }
        }
    }

}
