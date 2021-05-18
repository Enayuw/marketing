package com.br.marketing.task.job;

import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.RabbitMqSenderUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.Customer;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.task.Scheduler;
import com.br.marketing.task.service.Impl.LoanWarningServiceImpl;
import com.br.marketing.task.service.LoanWarningService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
 * @Date 2021/5/7 13:05
 * @Description:
 **/
@Component
@Slf4j
public class TaskJob extends AbstractSimpleElasticJob {
    @Resource
    CustomerMapper customerMapper;
    @Resource(name = "rabbitTemplate")
    private RabbitTemplate rabbitTemplate;
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        Long start=System.currentTimeMillis();
        log.warn("【跑批任务】调度开始");
        String parameter = context.getJobParameter();
        List<Customer> customers=new ArrayList<>();
        if(StringUtils.isNotEmpty(parameter)){
            Customer customer =customerMapper.getCustomerByApiCode(parameter);
            if(customer !=null){
                customers.add(customer);
            }else {
                log.error("apicode错误");
                return;
            }
        }else {
            customers =customerMapper.getAllCustomer();
        }
        customers.forEach(customer -> {
            try {
                Class loanWarningServiceClass =Class.forName(customer.getTaskServiceName());
                LoanWarningService loanWarningService=(LoanWarningService)Scheduler.ac.getBean(loanWarningServiceClass);
                loanWarningService.process(customer);
                //推送消息到pushQueue，进行下一流程处理
                RabbitMqSenderUtils.convertAndSendPriority(rabbitTemplate,MQConstants.exchangerName, MQConstants.pushRoutingKey,customer.getApiCode());

            } catch (ClassNotFoundException e) {
                log.error("实现类未找到，apiCode={}",customer.getApiCode());
            }

        });
        Long end =System.currentTimeMillis();
        log.warn("【跑批任务】调度结束，耗时：{}",end-start);
    }
}
