package com.br.marketing.push.job;

import com.br.marketing.entity.Customer;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.push.service.FlowService;
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
 * @Date 2021/6/21 15:01
 * @Description:
 **/
@Component
@Slf4j
public class MergeJob extends AbstractSimpleElasticJob {
    @Resource
    CustomerMapper customerMapper;
    @Resource
    private FlowService flowService;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Long start=System.currentTimeMillis();
        log.warn("【合并任务】调度开始");
        List<Customer> customers=customerMapper.getAllCustomer();
        customers.forEach(customer -> {
            try {
                log.warn("开始执行合并任务，apicode={}",customer.getApiCode());
                flowService.flow(customer);

            } catch (Exception e) {
                log.error("程序跑批异常，apiCode={}",customer.getApiCode(), e);
            }
        });
        Long end =System.currentTimeMillis();
        log.warn("【跑批任务】调度结束，耗时：{}",end-start);
    }
}
