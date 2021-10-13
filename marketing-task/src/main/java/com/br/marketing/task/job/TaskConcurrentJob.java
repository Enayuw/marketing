package com.br.marketing.task.job;

import com.br.marketing.entity.Customer;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.task.Scheduler;
import com.br.marketing.task.service.Impl.LoanWarningServiceImpl;
import com.br.marketing.task.service.LoanWarningService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
 * @Date 2021/5/7 13:05
 * @Description:
 **/
@Component
@Slf4j
public class TaskConcurrentJob extends AbstractSimpleElasticJob {
    @Resource
    CustomerMapper customerMapper;
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        Long start=System.currentTimeMillis();
        log.warn("【跑批任务】调度开始");
        List<Customer> customers=customerMapper.getAllCustomer();
        customers.forEach(customer -> {
            try {
                if(customer.getStatus() ==1 && customer.getTaskTime()==1){
                    log.warn("开始执行跑批任务，apicode={}",customer.getApiCode());
                    LoanWarningService loanWarningService=Scheduler.ac.getBean(LoanWarningServiceImpl.class);
                    loanWarningService.concurrentProcess(customer,context);
                }
            } catch (Exception e) {
                log.error("程序跑批异常，apiCode={}",customer.getApiCode());
            }
        });
        Long end =System.currentTimeMillis();
        log.warn("【跑批任务】调度结束，耗时：{},分片：{}",end-start,context.getShardingItemParameters());
    }
}
