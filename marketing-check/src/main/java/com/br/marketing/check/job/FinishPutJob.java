package com.br.marketing.check.job;

import com.br.marketing.check.service.PushFinishService;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.Customer;
import com.br.marketing.mapper.CustomerMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
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
 * @Date 2021/4/27 15:46
 * @Description:
 **/
@Component
@Slf4j
public class FinishPutJob extends AbstractSimpleElasticJob {
    @Resource
    PushFinishService pushFinishServiceImpl;
    @Resource
    CustomerMapper customerMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        Long start=System.currentTimeMillis();
        log.warn("【推送finish文件】调度开始");
        String parameter = context.getJobParameter();
        List<Customer> customers=new ArrayList<>();
        if(StringUtils.isNotEmpty(parameter)){
            Customer customer =customerMapper.getCustomerByApiCode(parameter);
            customers.add(customer);
            if(customer !=null){
                customers.add(customer);
            }else {
                log.error("apicode错误");
                return;
            }
        }else {
            customers =customerMapper.getAllCustomer();
        }
        customers.forEach(customer -> pushFinishServiceImpl.pushFinish(customer.getApiCode()));
        Long end =System.currentTimeMillis();
        log.warn("【推送finish文件】调度结束，耗时：{}",end-start);
    }
}
