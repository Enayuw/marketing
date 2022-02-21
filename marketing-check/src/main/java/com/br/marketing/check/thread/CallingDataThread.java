package com.br.marketing.check.thread;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerCallingDataStatusMapper;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.org.apache.regexp.internal.RE;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.br.marketing.check.utils.CallingUtil.getJsonObject;

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

    private final String requestId;


    public CallingDataThread(String RequestId,List<CustomerCallingDialog> customerCallingDialogLists, CustomerCallingDialogMapper customerCallingDialogMapper,
                             CustomerCalling customerCalling, HttpProxyClient httpProxyClient) {
        this.requestId = RequestId;
        this.customerCallingDialogLists = customerCallingDialogLists;
        this.customerCallingDialogMapper = customerCallingDialogMapper;
        this.customerCalling = customerCalling;
        this.httpProxyClient = httpProxyClient;
    }

    @Override
    public String call() throws Exception {
        log.warn("开始多线程调用第三方接口");
        updateRequestId();
        customerCallingDialogLists.forEach(sendPostRequest());
        return "success";
    }

    private Consumer<? super CustomerCallingDialog> sendPostRequest() {
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
        log.warn("拨打记录发送返回值：", result);
        return null;
    }



    public static String toJson(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting(); //生成格式化后的json
        Gson gson = gsonBuilder.create();
        return gson.toJson(object);
    }

    private void updateRequestId() {
        List<Long> ids = customerCallingDialogLists
                .stream()
                .map(CustomerCallingDialog::getId)
                .collect(Collectors.toList());
        CustomerCallingDialogExample customerCallingDialogExample = new CustomerCallingDialogExample();
        customerCallingDialogExample.createCriteria().andIdIn(ids);
        CustomerCallingDialog customerCallingDialog = new CustomerCallingDialog();
        customerCallingDialog.setRequestId(requestId);
        customerCallingDialog.setSendStatus(1);
        customerCallingDialogMapper.updateByExampleSelective(customerCallingDialog, customerCallingDialogExample);
    }

}
