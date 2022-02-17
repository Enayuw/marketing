package com.br.marketing.check.job;

import com.br.marketing.check.thread.CallingDataThread;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.CustomerCalling;
import com.br.marketing.entity.CustomerCallingDialog;
import com.br.marketing.entity.CustomerCallingExample;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.br.marketing.mapper.CustomerCallingMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author guangchao.zhang
 * @Classname CallingToSendJob
 * @Description 回调第三方接口发送不打信息
 * @Date 2022/2/16 10:02 AM
 */
@Component
public class CallingToSendJob extends AbstractSimpleElasticJob {

    @Resource
    CustomerCallingMapper customerCallingMapper;

    @Resource
    CustomerCallingDialogMapper customerCallingDialogMapper;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        this.process(getCustomerCallings());
    }

    private void process(List<CustomerCalling> customerCallings) {
        for (CustomerCalling customerCalling : customerCallings) {
            doThreadSubmit(customerCalling, getPartitions(customerCalling));
        }
    }

    private List<List<CustomerCallingDialog>> getPartitions(CustomerCalling customerCalling) {
        Map<String, Object> cusMap = new HashMap<>(16);
        cusMap.put("columns", customerCalling.getColumnsDetail());
        cusMap.put("apiCode", customerCalling.getApiCode());
        cusMap.put("sendStatus", 0);
        cusMap.put("conditions", customerCalling.getConditions());
        List<CustomerCallingDialog> customerCallingDialogs = customerCallingDialogMapper.getInfoByColumns(cusMap);
        return Lists.partition(customerCallingDialogs, 20);
    }

    private void doThreadSubmit(CustomerCalling customerCalling, List<List<CustomerCallingDialog>> partitions) {
        ThreadPoolExecutor pushExecutor;
        if (customerCalling.getPushThreadNum() != null) {
            pushExecutor = BrExecutors.getThreadPool(customerCalling.getPushThreadNum(), customerCalling.getPushThreadNum());
        } else {
            pushExecutor = BrExecutors.getThreadPool(2, 2);
        }
        partitions.forEach((customerCallingDialogLists) -> pushExecutor.submit(new CallingDataThread(customerCallingDialogLists, customerCallingDialogMapper, customerCalling)));
    }


    private List<CustomerCalling> getCustomerCallings() {
        CustomerCallingExample customerCallingExample = new CustomerCallingExample();
        customerCallingExample.createCriteria().andStatusEqualTo((byte) 1);
        return customerCallingMapper.selectByExample(customerCallingExample);
    }
}
