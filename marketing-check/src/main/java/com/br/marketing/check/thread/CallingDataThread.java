package com.br.marketing.check.thread;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.entity.CustomerCalling;
import com.br.marketing.entity.CustomerCallingDialog;
import com.br.marketing.entity.CustomerCallingDialogExample;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author guangchao.zhang
 * @Classname CallingDataThread
 * @Description 首次拨打记录数据处理
 * @Date 2022/2/16 1:35 PM
 */
@Slf4j
public class CallingDataThread implements Callable<String> {

    private final List<CustomerCallingDialog> customerCallingDialogLists;

    private final CustomerCallingDialogMapper customerCallingDialogMapper;

    private final CustomerCalling customerCalling;

    private final HttpProxyClient httpProxyClient;


    public CallingDataThread(List<CustomerCallingDialog> customerCallingDialogLists, CustomerCallingDialogMapper customerCallingDialogMapper, CustomerCalling customerCalling) {
        this.customerCallingDialogLists = customerCallingDialogLists;
        this.customerCallingDialogMapper = customerCallingDialogMapper;
        this.customerCalling = customerCalling;
        httpProxyClient = CkeckApplication.ac.getBean(HttpProxyClient.class);
    }

    @Override
    public String call() throws Exception {
        log.warn("开始多线程调用第三方接口");
        updateRequestId(customerCalling, customerCallingDialogLists);
        customerCallingDialogLists.forEach(sendPostRequest());
        return "success";
    }

    private Consumer<? super CustomerCallingDialog> sendPostRequest() {
        JSONObject param = new JSONObject();
        param.put("requestId", customerCalling.getApiCode() + "_" + UUID.randomUUID());
        JSONArray dataItems = new JSONArray();
        customerCallingDialogLists.forEach(customerCallingDialog -> dataItems.add(JSONObject.parse(toJson(customerCallingDialog))));
        param.put("dataItems", dataItems);
        log.warn("3用户发送数据：{}", param.toJSONString());
        Map<String, Object> result = httpProxyClient.request(customerCalling.getPushUrl().trim(), param.toJSONString(), true);
        //log.warn("拨打记录发送返回值：", result);
        return null;
    }


    public static String toJson(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting(); //生成格式化后的json
        Gson gson = gsonBuilder.create();
        return gson.toJson(object);
    }

    private void updateRequestId(CustomerCalling customerCalling, List<CustomerCallingDialog> customerCallingDialogLists) {
        List<Long> ids = customerCallingDialogLists
                .stream()
                .filter(customerCallingDialog -> {
                    customerCallingDialog.setRequestId(customerCalling.getApiCode() + "_" + UUID.randomUUID());
                    return true;
                })
                .collect(Collectors.toList())
                .stream()
                .map(CustomerCallingDialog::getId)
                .collect(Collectors.toList());
        CustomerCallingDialogExample customerCallingDialogExample = new CustomerCallingDialogExample();
        customerCallingDialogExample.createCriteria().andIdIn(ids);
        CustomerCallingDialog customerCallingDialog = new CustomerCallingDialog();
        customerCallingDialog.setRequestId(customerCalling.getApiCode() + "_" + UUID.randomUUID());
        customerCallingDialog.setSendStatus(1);
        customerCallingDialogMapper.updateByExampleSelective(customerCallingDialog, customerCallingDialogExample);
    }

}
