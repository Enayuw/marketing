package com.br.marketing.check.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.halo.HaluoApiServiceClient;
import com.br.marketing.client.halo.input.ReqHaluoApiDTO;
import com.br.marketing.client.halo.send.HaloApiParam;
import com.br.marketing.client.halo.send.HaloApiSend;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerCallingDataStatusMapper;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.br.marketing.mapper.CustomerCallingPushLogMapper;
import com.br.marketing.vo.HaloCallingDataVo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author guangchao.zhang
 * @Classname CallingDataThread
 * @Description 首次拨打记录数据处理
 * @Date 2022/2/16 1:35 PM
 */
@Slf4j
public class CallingDataThread implements Callable<String> {

    private final List<HaloCallingDataVo> haloCallingDataVoList;

    private final CustomerCallingDialogMapper customerCallingDialogMapper;

    private final CustomerCalling customerCalling;

    private final CustomerCallingPushLogMapper customerCallingPushLogMapper;

    private final CustomerCallingDataStatusMapper customerCallingDataStatusMapper;

    private final String haloMethod;

    private final HaluoApiServiceClient haluoApiServiceClient;

    private CountDownLatch countDownLatch;


    public CallingDataThread(List<HaloCallingDataVo> haloCallingDataVoList,
                             CountDownLatch countDownLatch,
                             CustomerCallingDialogMapper customerCallingDialogMapper,
                             CustomerCalling customerCalling,
                             HaluoApiServiceClient haluoApiServiceClient,
                             CustomerCallingPushLogMapper customerCallingPushLogMapper,
                             CustomerCallingDataStatusMapper customerCallingDataStatusMapper,
                             String method) {
        this.haloCallingDataVoList = haloCallingDataVoList;
        this.countDownLatch = countDownLatch;
        this.customerCallingDialogMapper = customerCallingDialogMapper;
        this.customerCalling = customerCalling;
        this.haluoApiServiceClient = haluoApiServiceClient;
        this.customerCallingPushLogMapper = customerCallingPushLogMapper;
        this.customerCallingDataStatusMapper = customerCallingDataStatusMapper;
        this.haloMethod = method;
    }

    @Override
    public String call() {
        try {
            String requestId = customerCalling.getApiCode() + "_" + UUID.randomUUID();
            sendPostRequest(requestId);
            return "success";
        } catch (Exception e) {
            log.error("程序处理异常", e);
            throw new RuntimeException(e);
        } finally {
            for (int i = 0; i < haloCallingDataVoList.size(); i++) {
                countDownLatch.countDown();
            }
        }
    }

    private void sendPostRequest(String requestId) {
        JSONObject param = new JSONObject();
        JSONArray dataItems = new JSONArray();
        param.put("openSerialNo", requestId);
        haloCallingDataVoList.forEach(haloCallingDataVo -> dataItems.add(JSONObject.parse(toJson(haloCallingDataVo))));
        param.put("dataItems", dataItems);
        String result = sendRequest(param);
        afterSendDoWork(requestId, result);
        savePushLog(requestId, param, result);
    }

    private void afterSendDoWork(String requestId, String result) {
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultJson = JSONObject.parseObject(result);
            String subCode = resultJson.getString("subCode");
            updateRequestId(requestId, "0".equals(subCode) ? 2 : 1);
            if (!"0".equals(subCode)) {
                String errorDescription = resultJson.getString("subMsg");
                CustomerCallingDataStatus customerCallingDataStatus = new CustomerCallingDataStatus();
                customerCallingDataStatus.setRequestId(requestId);
                customerCallingDataStatus.setSendStatus(2);
                customerCallingDataStatus.setDescription(errorDescription);
                customerCallingDataStatus.setCreateTime(new Date());
                customerCallingDataStatusMapper.insertSelective(customerCallingDataStatus);
            }
        }
    }

    private String sendRequest(JSONObject param) {
        ReqHaluoApiDTO reqHaluoApiDTO = new ReqHaluoApiDTO();
        reqHaluoApiDTO.setData(param.toJSONString());
        reqHaluoApiDTO.setMethod(haloMethod);
        Result<String> result = haluoApiServiceClient.postHaluoOpenApi(reqHaluoApiDTO);
        return result.getData();
    }

    private void savePushLog(String requestId, JSONObject param, String result) {
        JSONArray dataItems = param.getJSONArray("dataItems");
        Map<String, Object> map = new HashMap<>();
        map.put("requestId", requestId);
        map.put("params", param.toJSONString());
        map.put("result", result);
        map.put("createTime", new Date());
        map.put("sum", dataItems.size());
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
        List<Long> ids = haloCallingDataVoList
                .stream()
                .map(HaloCallingDataVo::getId)
                .collect(Collectors.toList());
        CustomerCallingDialogExample customerCallingDialogExample = new CustomerCallingDialogExample();
        customerCallingDialogExample.createCriteria().andIdIn(ids);
        CustomerCallingDialog customerCallingDialog = new CustomerCallingDialog();
        customerCallingDialog.setRequestId(requestId);
        customerCallingDialog.setSendStatus(sendStatus);
        customerCallingDialogMapper.updateByExampleSelective(customerCallingDialog, customerCallingDialogExample);

    }
}
