package com.br.marketing.check.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.thread.CallingDataThread;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.CustomerCalling;
import com.br.marketing.entity.CustomerCallingDialog;
import com.br.marketing.entity.CustomerCallingDialogExample;
import com.br.marketing.entity.CustomerCallingExample;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.br.marketing.mapper.CustomerCallingMapper;
import com.br.marketing.mapper.CustomerCallingPushLogMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jdk.nashorn.internal.objects.annotations.Where;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.br.marketing.check.utils.CallingUtil.getJsonObject;

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
        log.warn("1用户信息：{}",customerCallings);
        for (CustomerCalling customerCalling : customerCallings) {
            String tableColumns = getTableColumns(customerCalling);

            if(tableColumns!=null){
                getPartitions(customerCalling,tableColumns);
                //doThreadSubmit(customerCalling, getPartitions(customerCalling,tableColumns));
            }
        }
    }

    private List<List<CustomerCallingDialog>> getPartitions(CustomerCalling customerCalling,String tableColumns) {
        Map<String, Object> cusMap = new HashMap<>(16);
        cusMap.put("columns", tableColumns);
        cusMap.put("apiCode", customerCalling.getApiCode());
        cusMap.put("sendStatus", 0);
        cusMap.put("conditions", customerCalling.getConditions());
        boolean index = true;
        List<CustomerCallingDialog> list = new ArrayList<>();
        while (index){
            List<CustomerCallingDialog>    customerCallingDialogsByEvery = customerCallingDialogMapper.getInfoByColumns(cusMap);
            index =  customerCallingDialogsByEvery.size()==0?false:true;
            if(index){
                List<Long> ids = customerCallingDialogsByEvery
                        .stream()
                        .map(CustomerCallingDialog::getId)
                        .collect(Collectors.toList());
                CustomerCallingDialogExample customerCallingDialogExample = new CustomerCallingDialogExample();
                customerCallingDialogExample.createCriteria().andIdIn(ids);
                CustomerCallingDialog customerCallingDialog = new CustomerCallingDialog();
                customerCallingDialog.setSendStatus(1);
                customerCallingDialogMapper.updateByExampleSelective(customerCallingDialog, customerCallingDialogExample);
                sendPostRequest(customerCalling,customerCallingDialogsByEvery);
            }
        }
        return Lists.partition(list, 1500);

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
            partitions.forEach((customerCallingDialogLists) -> pushExecutor.submit(new CallingDataThread(customerCallingDialogLists, customerCallingDialogMapper, customerCalling,httpProxyClient,customerCallingPushLogMapper)));
        }
    }


    private List<CustomerCalling> getCustomerCallings() {
        CustomerCallingExample customerCallingExample = new CustomerCallingExample();
        customerCallingExample.createCriteria().andStatusEqualTo((byte) 1);
        return customerCallingMapper.selectByExample(customerCallingExample);
    }

    private void sendPostRequest(CustomerCalling customerCalling, List<CustomerCallingDialog>  customerCallingDialogLists) {
        String requestId = customerCalling.getApiCode() + "_" + UUID.randomUUID();
        JSONObject param = new JSONObject();
        param.put("requestId", requestId);
        JSONArray dataItems = new JSONArray();
        customerCallingDialogLists.forEach(customerCallingDialog -> dataItems.add(JSONObject.parse(toJson(customerCallingDialog))));
        param.put("dataItems", dataItems);
        log.warn("3用户发送数据：{}", param.toJSONString());
        String extendConfigInfo = customerCalling.getExtendConfigInfo();
        String pushUrl = customerCalling.getPushUrl().trim();
        JSONObject extendConfigInfoJson = getJsonObject(extendConfigInfo);
        JSONObject pushUrlJson = getJsonObject(pushUrl);
        String sendUrl = pushUrlJson.getString("sendUrl");
        Boolean isProxy = extendConfigInfoJson.getBoolean("isProxy") == null ? Boolean.TRUE : extendConfigInfoJson.getBoolean("isProxy");
        Map<String, Object> result = httpProxyClient.request(sendUrl, param.toJSONString(), isProxy);
        updateRequestId(requestId,customerCallingDialogLists);
        savePushLog(requestId, param, result);

    }
    private void savePushLog(String requestId, JSONObject param, Map<String, Object> result) {
        JSONArray dataItems = param.getJSONArray("dataItems");
        Map<String, Object> map = new HashMap<>();
        map.put("requestId", requestId);
        map.put("params", param.toJSONString());
        map.put("result", result.toString());
        map.put("createTime", new Date());
        map.put("sum",dataItems.size());
        customerCallingPushLogMapper.insert(map);
        log.warn("拨打记录发送返回值：{}", result);
    }


    public static String toJson(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting(); //生成格式化后的json
        Gson gson = gsonBuilder.create();
        return gson.toJson(object);
    }

    private void updateRequestId(String requestId,List<CustomerCallingDialog>  customerCallingDialogLists) {
        List<Long> ids = customerCallingDialogLists
                .stream()
                .map(CustomerCallingDialog::getId)
                .collect(Collectors.toList());
        CustomerCallingDialogExample customerCallingDialogExample = new CustomerCallingDialogExample();
        customerCallingDialogExample.createCriteria().andIdIn(ids);
        CustomerCallingDialog customerCallingDialog = new CustomerCallingDialog();
        customerCallingDialog.setRequestId(requestId);
        customerCallingDialogMapper.updateByExampleSelective(customerCallingDialog, customerCallingDialogExample);
    }

}
