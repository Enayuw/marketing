package com.br.marketing.check.job;

import IceInternal.Ex;
import com.br.marketing.check.service.PushCustomerService;
import com.br.marketing.check.service.ResultCheckService;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.Customer;
import com.br.marketing.mapper.CustomerMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
 * @Date 2021/4/27 15:46
 * @Description:
 **/

/**
 * api推送数据调度任务
 */
@Component
@Slf4j
public class PushCustomerJob extends AbstractSimpleElasticJob {
    @Resource
    PushCustomerService pushCustomerService;
    @Resource
    CustomerMapper customerMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Long start=System.currentTimeMillis();
        log.warn("【api推送客户数据】调度开始");
        try {
            List<Customer> customers = customerMapper.getAllCustomer();
            customers.forEach(customer -> {
                if (customer.getPushCustomer() == 1) {
                    pushCustomerService.push(customer, null);
                }
            });
        }catch (Exception ex){
            log.error("跑分推送客户报错"+ex.getMessage(),ex);
        }
        Long end =System.currentTimeMillis();
        log.warn("【结果文件校验api推送客户数据】调度结束，耗时：{}",end-start);

    }
}
