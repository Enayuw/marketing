package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.ResourceAllocationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ResourceAllocationServiceImpl implements ResourceAllocationService {

    public static CuratorFramework client;

    @Value("${SERVER_LISTS}")
    private static String serverList;

    private static final String marketPath = "/pd_hx/marketing";
    private static final String loanPath = "/pd_hx/loan_warning";
    private static final String miniPath = "/pd_hx/mini_mark";

    static {
        System.out.println("开始建立连接。。。");
        System.out.println(serverList);
        // 重连策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 建立客户端
        client = CuratorFrameworkFactory.builder()
                .connectString(serverList)
                .sessionTimeoutMs(60 * 1000)  // 会话超时时间
                .connectionTimeoutMs(5000) // 连接超时时间
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }

    @Override
    public HashMap getThreadPoolData() throws Exception{
        HashMap map = new HashMap<>();
        //营销中台 线程池信息
        List<String> marketingChild = client.getChildren().forPath(marketPath);
        if(marketingChild.size()>0 && marketingChild!=null){
            //读取节点值
            HashMap child = new HashMap<>();
            for (String s:marketingChild){

                byte[] bytes = client.getData().forPath(marketPath + "/" + s);
                if(bytes!=null && bytes.length>0){
                    String num = new String(bytes);
                    child.put(s,num);
                }else {
                    child.put(s,0);
                }
            }
            map.put("marketing",child);
        }


        //存量监控 线程池信息
        List<String> loanChild = client.getChildren().forPath(loanPath);
        if(loanChild.size()>0 && loanChild!=null){
            HashMap child = new HashMap<>();
            //读取节点值
            for (String s:loanChild){

                byte[] bytes = client.getData().forPath(loanPath + "/" + s);
                if(bytes!=null && bytes.length>0){
                    String num = new String(bytes);
                    child.put(s,num);
                }else {
                    child.put(s,0);
                }
            }
            map.put("loan_warning",child);
        }


        //小程序 线程池信息
        List<String> miniChild = client.getChildren().forPath(miniPath);
        if(loanChild.size()>0 && loanChild!=null){
            HashMap child = new HashMap<>();
            //读取节点值
            for (String s:miniChild){

                byte[] bytes = client.getData().forPath(miniPath + "/" + s);
                if(bytes!=null && bytes.length>0){
                    String num = new String(bytes);
                    child.put(s,num);
                }else {
                    child.put(s,0);
                }
            }
            map.put("mini_mark",child);
        }

        return map;
    }

    @Override
    @Transactional
    public Boolean editThreadPoolNum(Map map){
        //修改节点值
        Map marketing = (Map) map.get("marketing");
        marketing.forEach((key,value)->{
            try {
                client.setData().forPath(marketPath+"/"+key, value.toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage(),e);
            }
        });

        Map loanWarning = (Map) map.get("loan_warning");
        loanWarning.forEach((key,value)->{
            try {
                client.setData().forPath(loanPath+"/"+key, value.toString().getBytes());
            } catch (Exception ex) {
                ex.printStackTrace();
                log.error(ex.getMessage(),ex);
            }
        });

        Map miniMark = (Map) map.get("mini_mark");
        miniMark.forEach((key,value)->{
            try {
                client.setData().forPath(miniPath+"/"+key, value.toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage(),e);
            }
        });

        return true;
    }


}
