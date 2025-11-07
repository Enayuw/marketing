package com.br.marketing.tools.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/sreadduser")
public class SreAddUserController {

    private static final String cookie = "_ga_QZJBSCYW27=GS1.2.1736951216.29.1.1736951387.0.0.0; _ga_W2FYNR9BLD=GS2.1.s1755156487$o429$g0$t1755156487$j60$l0$h0; _ga_XQ8L60ECY0=GS2.1.s1755156487$o429$g0$t1755156487$j60$l0$h0; _ga=GA1.2.580719948.1704336123; cas_cookie_key=9c68b8b50970cf0978d0ea05bb4a66832301f9f5; sre_cookie_key=414c4ca0314442b725d04e0d9704694146fab5bd";

    /**
     * 指定服务添加人员
     *
     * @param appId
     * @return
     */
    @GetMapping("addUser")
    public String addUser(@RequestParam("appId") Integer appId,
                          @RequestParam(value = "testMode", defaultValue = "false") Boolean testMode) {

        List<String> kfList = Stream.of("zhao.ma"
                        , "juman.wang"
                        , "guangchao.zhang"
                        , "zhen.li1"
                        , "yu.xia"
                        , "guangxiu.li"
                        , "senyang.zheng"
                        , "bingxu.kong"
                        , "hong.chen"
                        , "zeqiang.guo"
                        , "xiang.li4"
                        , "dongshuo.he"
                        , "hang.zhou"
                        , "yanchao.dong"
                        , "xiong.luo"
                        , "bin.li1")
                .collect(Collectors.toList());

        List<String> qaList = Stream.of("yanjun.wu"
                        , "bin.huang"
                        , "fan.li1"
                        , "yue.zhang4"
                        , "yanping.fu"
                        , "haosong.jiang")
                .collect(Collectors.toList());

        // 获取该服务现有的人员信息
        Map<String, List<String>> serviceInfo = getUsersOfService(appId);
        String serviceName = "未知服务"; // 单个服务时使用默认名称
        List<String> existingDevUsers = serviceInfo.getOrDefault("dev", new ArrayList<>());
        List<String> existingTestUsers = serviceInfo.getOrDefault("test", new ArrayList<>());
        List<String> existingAlarmUsers = serviceInfo.getOrDefault("alarm", new ArrayList<>());

        // 添加开发人员（类型0和10）
        for (String s : kfList) {
            if (!existingDevUsers.contains(s)) {
                if (testMode) {
                    System.out.println(String.format("[测试模式] 服务名称: %s, 服务ID: %d, 添加开发人员: %s (类型: 0)", serviceName, appId, s));
                } else {
                    addUserReq(s, 0, appId);
                }
            }
            if (!existingAlarmUsers.contains(s)) {
                if (testMode) {
                    System.out.println(String.format("[测试模式] 服务名称: %s, 服务ID: %d, 添加告警人员: %s (类型: 10)", serviceName, appId, s));
                } else {
                    addUserReq(s, 10, appId);
                }
            }
        }

        // 添加测试人员（类型1和10）
        for (String s : qaList) {
            if (!existingTestUsers.contains(s)) {
                if (testMode) {
                    System.out.println(String.format("[测试模式] 服务名称: %s, 服务ID: %d, 添加测试人员: %s (类型: 1)", serviceName, appId, s));
                } else {
                    addUserReq(s, 1, appId);
                }
            }
            if (!existingAlarmUsers.contains(s)) {
                if (testMode) {
                    System.out.println(String.format("[测试模式] 服务名称: %s, 服务ID: %d, 添加告警人员: %s (类型: 10)", serviceName, appId, s));
                } else {
                    addUserReq(s, 10, appId);
                }
            }
        }
        return "success";
    }

    /**
     * 指定下的所有服务添加人员
     *
     * @param proId
     * @return
     */
    @GetMapping("addUserByProId")
    public String addUserByProId(Integer proId,
                                 @RequestParam(value = "testMode", defaultValue = "false") Boolean testMode) {

        Map<Integer, String> appsOfProId = getAppsOfProId(proId);
        System.out.println(JSON.toJSONString(appsOfProId));
        List<String> kfList = Stream.of("zhao.ma"
                        , "juman.wang"
                        , "guangchao.zhang"
                        , "zhen.li1"
                        , "yu.xia"
                        , "guangxiu.li"
                        , "senyang.zheng"
                        , "bingxu.kong"
                        , "hong.chen"
                        , "zeqiang.guo"
                        , "xiang.li4"
                        , "dongshuo.he")
                .collect(Collectors.toList());

        List<String> qaList = Stream.of("yanjun.wu"
                        , "bin.huang"
                        , "fan.li1"
                        , "yue.zhang4")
                .collect(Collectors.toList());

        for (Map.Entry<Integer, String> entry : appsOfProId.entrySet()) {
            Integer appId = entry.getKey();
            String serviceName = entry.getValue();

            // 获取该服务现有的人员信息
            Map<String, List<String>> serviceInfo = getUsersOfService(appId);
            List<String> existingDevUsers = serviceInfo.getOrDefault("dev", new ArrayList<>());
            List<String> existingTestUsers = serviceInfo.getOrDefault("test", new ArrayList<>());
            List<String> existingAlarmUsers = serviceInfo.getOrDefault("alarm", new ArrayList<>());

            // 添加开发人员（类型0和10）
            for (String s : kfList) {
                if (!existingDevUsers.contains(s)) {
                    if (testMode) {
                        System.out.println(String.format("[测试模式] 服务名称: %s, 服务ID: %d, 添加开发人员: %s (类型: 0)", serviceName, appId, s));
                    } else {
                        addUserReq(s, 0, appId);
                    }
                }
                if (!existingAlarmUsers.contains(s)) {
                    if (testMode) {
                        System.out.println(String.format("[测试模式] 服务名称: %s, 服务ID: %d, 添加告警人员: %s (类型: 10)", serviceName, appId, s));
                    } else {
                        addUserReq(s, 10, appId);
                    }
                }
            }

            // 添加测试人员（类型1和10）
            for (String s : qaList) {
                if (!existingTestUsers.contains(s)) {
                    if (testMode) {
                        System.out.println(String.format("[测试模式] 服务名称: %s, 服务ID: %d, 添加测试人员: %s (类型: 1)", serviceName, appId, s));
                    } else {
                        addUserReq(s, 1, appId);
                    }
                }
                if (!existingAlarmUsers.contains(s)) {
                    if (testMode) {
                        System.out.println(String.format("[测试模式] 服务名称: %s, 服务ID: %d, 添加告警人员: %s (类型: 10)", serviceName, appId, s));
                    } else {
                        addUserReq(s, 10, appId);
                    }
                }
            }
        }
        return "success";
    }

    void addUserReq(String name, Integer type, Integer appId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Cookie", cookie);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        String url = String.format("https://sre.100credit.cn/api/service/addUser?type=%d&casId=%s&ownerId=%d"
                , type, name, appId);
        ResponseEntity<String> exchange = new RestTemplate().exchange(url, HttpMethod.GET, requestEntity, String.class);
        System.out.println("添加结果：" + exchange.getBody());
    }

    /**
     * 根据项目id获取所有的服务信息
     *
     * @param proId
     * @return 服务信息Map，key为serviceId，value为serviceName
     */
    Map<Integer, String> getAppsOfProId(Integer proId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Cookie", cookie);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);
        String url = String.format("https://sre.100credit.cn/api/project/listByProjectId?id=%d"
                , proId);
        ResponseEntity<String> responseEntity = new RestTemplate().exchange(url, HttpMethod.GET, requestEntity, String.class);
        if (Integer.valueOf(200).equals(responseEntity.getStatusCodeValue())) {
            JSONObject jo = JSON.parseObject(responseEntity.getBody());
            if (Integer.valueOf(0).equals(jo.getInteger("code"))) {
                JSONArray result = jo.getJSONArray("result");
                Map<Integer, String> serviceMap = new HashMap<>();
                for (Object o : result) {
                    JSONObject r = (JSONObject) o;
                    Integer serviceId = r.getInteger("id");
                    String serviceName = r.getString("serviceMark");
                    serviceMap.put(serviceId, serviceName);
                }
                return serviceMap;
            }
        }
        return new HashMap<>();
    }

    /**
     * 根据服务ID获取服务的人员列表信息
     *
     * @param serviceId 服务ID
     * @return 分组的人员信息Map，key为类型(dev/test/alarm)，value为人员casId列表
     */
    Map<String, List<String>> getUsersOfService(Integer serviceId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Cookie", cookie);
        HttpEntity<String> requestEntity = new HttpEntity<String>(httpHeaders);

        String url = String.format("https://sre.100credit.cn/api/service/getServiceUser?id=%d", serviceId);
        ResponseEntity<String> responseEntity = new RestTemplate().exchange(url, HttpMethod.GET, requestEntity, String.class);

        if (Integer.valueOf(200).equals(responseEntity.getStatusCodeValue())) {
            JSONObject jo = JSON.parseObject(responseEntity.getBody());
            if (Integer.valueOf(0).equals(jo.getInteger("code"))) {
                JSONObject result = jo.getJSONObject("result");
                Map<String, List<String>> userMap = new HashMap<>();

                // 获取开发人员列表
                List<String> devList = new ArrayList<>();
                JSONArray devArray = result.getJSONArray("devList");
                if (devArray != null) {
                    for (Object o : devArray) {
                        JSONObject user = (JSONObject) o;
                        devList.add(user.getString("casId"));
                    }
                }
                userMap.put("dev", devList);

                // 获取测试人员列表
                List<String> testList = new ArrayList<>();
                JSONArray testArray = result.getJSONArray("testList");
                if (testArray != null) {
                    for (Object o : testArray) {
                        JSONObject user = (JSONObject) o;
                        testList.add(user.getString("casId"));
                    }
                }
                userMap.put("test", testList);

                // 获取告警人员列表
                List<String> alarmList = new ArrayList<>();
                JSONArray alarmArray = result.getJSONArray("alarmList");
                if (alarmArray != null) {
                    for (Object o : alarmArray) {
                        JSONObject user = (JSONObject) o;
                        alarmList.add(user.getString("casId"));
                    }
                }
                userMap.put("alarm", alarmList);

                return userMap;
            }
        }
        return new HashMap<>();
    }

}
