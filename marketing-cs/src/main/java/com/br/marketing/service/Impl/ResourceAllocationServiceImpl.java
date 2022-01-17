package com.br.marketing.service.Impl;

import com.br.marketing.common.constants.ZookeeperPath;
import com.br.marketing.service.ResourceAllocationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ResourceAllocationServiceImpl implements ResourceAllocationService {

    @Autowired
    private CuratorFramework client;


    @Override
    public List<Map> getThreadPoolData() throws Exception{
        //营销中台 线程池信息
        List<Map> list = new ArrayList<>();
        HashMap marketingMap = getData("marketing","智能营销", ZookeeperPath.marketPath);
        list.add(marketingMap);
        //存量监控 线程池信息
        HashMap loanMap = getData("loan_warning","存量监控",ZookeeperPath.loanPath);
        list.add(loanMap);
        //小程序 线程池信息
        HashMap miniMap = getData("mini_mark","小程序",ZookeeperPath.miniPath);
        list.add(miniMap);

        return list;
    }

    public HashMap getData(String value,String lable,String path) throws Exception {
        HashMap threadMap = new HashMap<>();
        threadMap.put("value",value);
        threadMap.put("label",lable);
        List<Map> info = new ArrayList<>();
        List<String> nodeChild = client.getChildren().forPath(path);
        if(nodeChild.size()>0 && nodeChild!=null){
            //读取节点值
            for (String s:nodeChild){
                HashMap child = new HashMap<>();
                byte[] bytes = client.getData().forPath(path + "/" + s);
                if(bytes!=null && bytes.length>0){
                    String num = new String(bytes);
                    child.put("threadNum",num);
                }else {
                    child.put("threadNum",0);
                }
                child.put("threadPoolInfo",s);
                child.put("threadNumEdit",null);
                info.add(child);
            }
        }
        threadMap.put("threadInfo",info);
        return threadMap;
    }

    @Override
    @Transactional
    public Boolean editThreadPoolNum(List<Map> list) throws Exception {
        for(Map single:list){
            String value = single.get("value").toString();
            List<Map> threadInfo = (List<Map>) single.get("threadInfo");
            switch (value){
                case "marketing" :
                    editNode(threadInfo,ZookeeperPath.marketPath);
                    break;
                case "loan_warning" :
                    editNode(threadInfo,ZookeeperPath.loanPath);
                    break;
                case "mini_mark" :
                    editNode(threadInfo,ZookeeperPath.miniPath);
                    break;
            }
        }
        return true;
    }

    //修改节点值
    public void editNode(List<Map> threadInfo,String path) throws Exception {
        for(Map s :threadInfo){
            String node = s.get("threadPoolInfo").toString();
            String numEdit = s.get("threadNumEdit").toString();
            client.setData().forPath(path+"/"+node, numEdit.getBytes());
        }
    }


}
