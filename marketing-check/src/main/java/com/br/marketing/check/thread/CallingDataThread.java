package com.br.marketing.check.thread;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.br.marketing.mapper.CustomerCallingPushLogMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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

    private final CustomerCallingPushLogMapper customerCallingPushLogMapper;


    public CallingDataThread(List<CustomerCallingDialog> customerCallingDialogLists,
                             CustomerCallingDialogMapper customerCallingDialogMapper,
                             CustomerCalling customerCalling,
                             HttpProxyClient httpProxyClient,
                             CustomerCallingPushLogMapper customerCallingPushLogMapper) {
        this.customerCallingDialogLists = customerCallingDialogLists;
        this.customerCallingDialogMapper = customerCallingDialogMapper;
        this.customerCalling = customerCalling;
        this.httpProxyClient = httpProxyClient;
        this.customerCallingPushLogMapper = customerCallingPushLogMapper;
    }

    @Override
    public String call() throws Exception {
        String requestId = customerCalling.getApiCode() + "_" + UUID.randomUUID();
        log.warn("开始多线程调用第三方接口");
        customerCallingDialogLists.forEach(sendPostRequest(requestId));
        return "success";
    }

    private Consumer<? super CustomerCallingDialog> sendPostRequest(String requestId) {
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
        boolean sendStatus = (boolean) result.get("result");
        updateRequestId(requestId, sendStatus == true ? 1 : 0);
        savePushLog(requestId, param, result);
        return null;
    }

    private void savePushLog(String requestId, JSONObject param, Map<String, Object> result) {
        CustomerCallingPushLog customerCallingPushLog = new CustomerCallingPushLog();
        customerCallingPushLog.setParams(param.toJSONString());
        customerCallingPushLog.setResult(String.valueOf(result));
        customerCallingPushLog.setRequestId(requestId);
        customerCallingPushLog.setCreateTime(new Date());
        Map<String, Object> map = new HashMap<>();
        map.put("requestId", requestId);
        map.put("params", param.toJSONString());
        map.put("result", result.toString());
        map.put("createTime", new Date());
        customerCallingPushLogMapper.insert(map);
        log.warn("拨打记录发送返回值：{}", result);
    }


    public static String toJson(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting(); //生成格式化后的json
        Gson gson = gsonBuilder.create();
        return gson.toJson(object);
    }

    private void updateRequestId(String requestId, Integer sendStatus) {
        List<Long> ids = customerCallingDialogLists
                .stream()
                .map(CustomerCallingDialog::getId)
                .collect(Collectors.toList());
        CustomerCallingDialogExample customerCallingDialogExample = new CustomerCallingDialogExample();
        customerCallingDialogExample.createCriteria().andIdIn(ids);
        CustomerCallingDialog customerCallingDialog = new CustomerCallingDialog();
        customerCallingDialog.setRequestId(requestId);
        customerCallingDialog.setSendStatus(sendStatus);
        customerCallingDialogMapper.updateByExampleSelective(customerCallingDialog, customerCallingDialogExample);
    }

}
