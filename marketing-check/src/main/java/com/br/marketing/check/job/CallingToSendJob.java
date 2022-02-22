package com.br.marketing.check.job;

import cn.hutool.log.Log;
import com.br.marketing.check.thread.CallingDataThread;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.CustomerCalling;
import com.br.marketing.entity.CustomerCallingDialog;
import com.br.marketing.entity.CustomerCallingExample;
import com.br.marketing.mapper.CustomerCallingDataStatusMapper;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.br.marketing.mapper.CustomerCallingMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CallingToSendJob extends AbstractSimpleElasticJob {

    @Resource
    CustomerCallingMapper customerCallingMapper;

    @Resource
    CustomerCallingDialogMapper customerCallingDialogMapper;

    @Resource
    HttpProxyClient httpProxyClient;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        this.process(getCustomerCallings());
    }

    private void process(List<CustomerCalling> customerCallings) {
        log.warn("1用户信息：{}",customerCallings);
        for (CustomerCalling customerCalling : customerCallings) {
            String tableColumns = getTableColumns(customerCalling);
            if(tableColumns!=null){
                doThreadSubmit(customerCalling, getPartitions(customerCalling,tableColumns));
            }
        }
    }

    private List<List<CustomerCallingDialog>> getPartitions(CustomerCalling customerCalling,String tableColumns) {
        Map<String, Object> cusMap = new HashMap<>(16);
        cusMap.put("columns", tableColumns);
        cusMap.put("apiCode", customerCalling.getApiCode());
        cusMap.put("sendStatus", 0);
        cusMap.put("conditions", customerCalling.getConditions());
        List<CustomerCallingDialog> customerCallingDialogs = customerCallingDialogMapper.getInfoByColumns(cusMap);
        return Lists.partition(customerCallingDialogs, 2000);
    }

    private String getTableColumns(CustomerCalling customerCalling) {
        String column = customerCalling.getColumnsDetail();
        if(column!=null&& !column.isEmpty()){
            String[] columns = column.split(",");
            List<String> columnsList = new ArrayList<>();
            Arrays.stream(columns).sequential().forEach(c -> {
                if ("custNum".equals(c)) {
                    c = "caseNum";
                }
                if("groupType".equals(c)){
                    c="userType";
                }
                columnsList.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, c));
            });
            return Joiner.on(",").join(columnsList);
        }
        return null;
    }

    private void doThreadSubmit(CustomerCalling customerCalling, List<List<CustomerCallingDialog>> partitions) {
        if(!partitions.isEmpty()){
            ThreadPoolExecutor pushExecutor;
            if (customerCalling.getPushThreadNum() != null) {
                pushExecutor = BrExecutors.getThreadPool(customerCalling.getPushThreadNum(), customerCalling.getPushThreadNum());
            } else {
                pushExecutor = BrExecutors.getThreadPool(2, 2);
            }
            log.warn("2用户处理信息：{}",partitions);
            partitions.forEach((customerCallingDialogLists) -> pushExecutor.submit(new CallingDataThread(customerCallingDialogLists, customerCallingDialogMapper, customerCalling,httpProxyClient)));
        }
    }


    private List<CustomerCalling> getCustomerCallings() {
        CustomerCallingExample customerCallingExample = new CustomerCallingExample();
        customerCallingExample.createCriteria().andStatusEqualTo((byte) 1);
        return customerCallingMapper.selectByExample(customerCallingExample);
    }
}
