package com.br.marketing.check.job;

import com.br.marketing.check.thread.CallingDataThread;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.CustomerCalling;
import com.br.marketing.entity.CustomerCallingDialog;
import com.br.marketing.entity.CustomerCallingDialogExample;
import com.br.marketing.entity.CustomerCallingExample;
import com.br.marketing.mapper.CustomerCallingDataStatusMapper;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.br.marketing.mapper.CustomerCallingMapper;
import com.br.marketing.mapper.CustomerCallingPushLogMapper;
import com.br.marketing.vo.HaloCallingDataVo;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author guangchao.zhang
 * @Classname CallingToSendJob
 * @Description 回调第三方接口发送不打信息
 * @Date 2022/2/16 10:02 AM
 */
@Component
@Slf4j
public class CallingToSendJob extends AbstractSimpleElasticJob {
    @Value("${api.halo.openUrl}")
    private String haloOpenUrl;

    @Value("${api.halo.appKey}")
    private String haloAppKey;

    @Value("${api.halo.secret}")
    private String haloSecret;

    @Value("${api.halo.method}")
    private String method;

    @Value("${api.halo.isProxy}")
    private boolean isProxy;

    @Resource
    CustomerCallingMapper customerCallingMapper;

    @Resource
    CustomerCallingDialogMapper customerCallingDialogMapper;

    @Resource
    CustomerCallingDataStatusMapper customerCallingDataStatusMapper;

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
            cusMap.put("pageNo", pageNo * 2000);
            cusMap.put("pageSize", 2000);
            List<HaloCallingDataVo> haloCallingDataVoList = customerCallingDialogMapper.getInfoByColumns(cusMap);
            index = haloCallingDataVoList.size() != 0;
            if (index) {
                updateSendStatus(haloCallingDataVoList);
                List<List<HaloCallingDataVo>> partitions = Lists.partition(haloCallingDataVoList, 20);
                partitions.forEach((customerCallingDialogLists) -> pushExecutor.submit(
                        new CallingDataThread(
                                customerCallingDialogLists,
                                customerCallingDialogMapper,
                                customerCalling,
                                httpProxyClient,
                                customerCallingPushLogMapper,
                                customerCallingDataStatusMapper,
                                haloOpenUrl,
                                haloAppKey,
                                haloSecret,
                                method,
                                isProxy)));
            }
            pageNo++;
        }
    }

    private void updateSendStatus(List<HaloCallingDataVo> haloCallingDataVoList) {
        List<Long> ids = haloCallingDataVoList
                .stream()
                .map(HaloCallingDataVo::getId)
                .collect(Collectors.toList());
        CustomerCallingDialogExample customerCallingDialogExample = new CustomerCallingDialogExample();
        customerCallingDialogExample.createCriteria().andIdIn(ids);
        CustomerCallingDialog customerCallingDialog = new CustomerCallingDialog();
        customerCallingDialog.setSendStatus(1);
        customerCallingDialogMapper.updateByExampleSelective(customerCallingDialog, customerCallingDialogExample);
    }

    private String getTableColumns(CustomerCalling customerCalling) {
        String column = customerCalling.getApiColumnsDetail();
        if (column != null && !column.isEmpty()) {
            String[] columns = column.split(",");
            List<String> columnsList = new ArrayList<>();
            Arrays.stream(columns).sequential().forEach(c -> {
                if("callStartTime".equals(c)){
                    c="UNIX_TIMESTAMP(call_start_time) as callStartTime";
                    columnsList.add(c);
                } else if("customerNo".equals(c)){
                    c="case_num as customerNo";
                    columnsList.add(c);
                }else {
                    c = "groupType".equals(c) ? "userType" : c;
                    columnsList.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, c));
                }
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
