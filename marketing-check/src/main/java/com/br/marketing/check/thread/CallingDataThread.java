package com.br.marketing.check.thread;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.utils.StringUtils;
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

    private HttpProxyClient httpProxyClient;


    public CallingDataThread(List<CustomerCallingDialog> customerCallingDialogLists, CustomerCallingDialogMapper customerCallingDialogMapper, CustomerCalling customerCalling, HttpProxyClient httpProxyClient) {
        this.customerCallingDialogLists = customerCallingDialogLists;
        this.customerCallingDialogMapper = customerCallingDialogMapper;
        this.customerCalling = customerCalling;
        //httpProxyClient = CkeckApplication.ac.getBean(HttpProxyClient.class);
        this.httpProxyClient = httpProxyClient;
    }

    @Override
    public String call() throws Exception {
        log.warn("开始多线程调用第三方接口");
        String requestId = customerCalling.getApiCode() + "_" + UUID.randomUUID();
        updateRequestId(customerCallingDialogLists, requestId);
        customerCallingDialogLists.forEach(sendPostRequest(requestId));
        return "success";
    }

    private Consumer<? super CustomerCallingDialog> sendPostRequest(String requestId) {
        JSONObject param = new JSONObject();
        param.put("requestId", requestId);
        JSONArray dataItems = new JSONArray();
        customerCallingDialogLists.forEach(customerCallingDialog -> {
            customerCallingDialog.setRequestId(null);
            customerCallingDialog.setUserType(null);
            dataItems.add(JSONObject.parse(toJson(customerCallingDialog)));
        });
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

    private JSONObject getJsonObject(String extendConfigInfo) {
        JSONObject extendConfigInfoJson = new JSONObject();
        if (StringUtils.isNotBlank(extendConfigInfo)) {
            extendConfigInfoJson = JSONObject.parseObject(extendConfigInfo);
        }
        return extendConfigInfoJson;
    }


    public static String toJson(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting(); //生成格式化后的json
        Gson gson = gsonBuilder.create();
        return gson.toJson(object);
    }

    private void updateRequestId(List<CustomerCallingDialog> customerCallingDialogLists, String requestId) {
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
