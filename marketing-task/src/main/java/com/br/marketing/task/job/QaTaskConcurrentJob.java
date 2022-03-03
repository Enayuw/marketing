package com.br.marketing.task.job;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.Customer;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.task.Scheduler;
import com.br.marketing.task.service.Impl.ConcurrentScoreServiceImpl;
import com.br.marketing.task.service.Impl.ObservedScoreThreadServiceImpl;
import com.br.marketing.task.service.LoanWarningService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
public class QaTaskConcurrentJob extends AbstractSimpleElasticJob {

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    @Resource
    CustomerMapper customerMapper;

    @Autowired
    ObservedScoreThreadServiceImpl observedScoreThreadService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        Long start = System.currentTimeMillis();
        log.warn("【跑批任务】调度开始");
        String parameter = context.getJobParameter();
        if (StringUtils.isEmpty(parameter)) {
            log.error("QaTaskConcurrentJob传入参数为空，apiCode={}", context.getJobParameter());
            return;
        }
        log.warn("手动触发跑批任务,apiCode={}", parameter);
        Customer customer = customerMapper.getCustomerByApiCode(parameter);
        if (customer == null) {
            log.error("apicode错误");
            return;
        }
        if (observedScoreThreadService.isInterrupt()) {
            StringBuilder content = new StringBuilder();
            content.append("当前跑分任务手动停止状态请手动开启");
            alarmClient.sendAlarm(content.toString(), "跑分暂停", appName, secretKey,
                    Constants.sendCodeMap.get("uploadSuccess"));
            return;
        } else {
            try {
                LoanWarningService loanWarningService = Scheduler.ac.getBean(ConcurrentScoreServiceImpl.class);
                loanWarningService.process(customer, context);
            } catch (Exception e) {
                log.error("程序跑批异常，apiCode={}", customer.getApiCode());
            }
            Long end = System.currentTimeMillis();
            log.warn("【跑批任务】调度结束，耗时：{},分片：{}", end - start, context.getShardingItemParameters());
        }
    }
}
