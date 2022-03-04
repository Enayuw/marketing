package com.br.marketing.check.job;

import com.br.marketing.check.thread.CallingDataThread;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.CustomerCalling;
import com.br.marketing.entity.CustomerCallingDialog;
import com.br.marketing.entity.CustomerCallingExample;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.br.marketing.mapper.CustomerCallingMapper;
import com.br.marketing.mapper.CustomerCallingPushLogMapper;
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

    @Resource
    CustomerCallingPushLogMapper customerCallingPushLogMapper;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        this.process(getCustomerCallings());
    }

    private void process(List<CustomerCalling> customerCallings) {
        log.warn("1用户信息：{}", customerCallings);
        for (CustomerCalling customerCalling : customerCallings) {
            String tableColumns = getTableColumns(customerCalling);

            if (tableColumns != null) {
                doThreadSubmit(customerCalling, tableColumns);
            }
        }
    }

    private void doThreadSubmit(CustomerCalling customerCalling, String tableColumns) {
        ThreadPoolExecutor pushExecutor;
        if (customerCalling.getPushThreadNum() != null) {
            pushExecutor = BrExecutors.getThreadPool(customerCalling.getPushThreadNum(), customerCalling.getPushThreadNum());
        } else {
            pushExecutor = BrExecutors.getThreadPool(5, 5);
        }
        Map<String, Object> cusMap = new HashMap<>(16);
        cusMap.put("columns", tableColumns);
        cusMap.put("apiCode", customerCalling.getApiCode());
        cusMap.put("sendStatus", 0);
        boolean index = true;
        int pageNo = 0;
        while (index) {
            cusMap.put("pageNo", pageNo * 15000);
            cusMap.put("pageSize", 15000);
            List<CustomerCallingDialog> customerCallingDialogsByEvery = customerCallingDialogMapper.getInfoByColumns(cusMap);
            index = customerCallingDialogsByEvery.size() != 0;
            if (index) {
                List<List<CustomerCallingDialog>> partitions = Lists.partition(customerCallingDialogsByEvery, 1500);
                partitions.forEach((customerCallingDialogLists) -> pushExecutor.submit(new CallingDataThread(customerCallingDialogLists, customerCallingDialogMapper, customerCalling, httpProxyClient, customerCallingPushLogMapper)));
            }
            pageNo++;
        }
    }

    private String getTableColumns(CustomerCalling customerCalling) {
        String column = customerCalling.getColumnsDetail();
        if (column != null && !column.isEmpty()) {
            String[] columns = column.split(",");
            List<String> columnsList = new ArrayList<>();
            Arrays.stream(columns).sequential().forEach(c -> {
                c = "custNum".equals(c) ? "caseNum" : c;
                c = "groupType".equals(c) ? "userType" : c;
                columnsList.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, c));
            });
            return Joiner.on(",").join(columnsList);
        }
        return null;
    }


    private List<CustomerCalling> getCustomerCallings() {
        CustomerCallingExample customerCallingExample = new CustomerCallingExample();
        customerCallingExample.createCriteria().andStatusEqualTo((byte) 1);
        return customerCallingMapper.selectByExample(customerCallingExample);
    }


}
