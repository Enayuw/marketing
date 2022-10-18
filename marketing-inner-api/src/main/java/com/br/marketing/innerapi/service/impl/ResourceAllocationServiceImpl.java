package com.br.marketing.innerapi.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.constants.ZookeeperPath;
import com.br.marketing.innerapi.service.ResourceAllocationService;
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
//@ConditionalOnBean(name = "curatorFramework")
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

    @Override
    public Boolean createZkData(String path,String data) throws Exception{
        // 必须先保证节点不存在
        if (client.checkExists().forPath(path) == null){
            // 递归创建节点
            client.create().creatingParentsIfNeeded().forPath(path, data.getBytes());
            return true;
        }
        else {
            log.warn(String.format("node already existed:[%s]", path));
            return false;
        }
    }

    @Override
    public Boolean setNodeData(String path, String jsonStr) throws Exception {
        // 必须先保证节点存在
        if(client.checkExists().forPath(path) != null){
            client.setData().forPath(path, jsonStr.getBytes());
            return true;
        }
        else{
            log.warn(String.format("node does not exists:[%s]", path));
            return false;
        }
    }

    @Override
    public Boolean deleteZkData(String path) throws Exception{
        // 必须先保证节点存在
        if(client.checkExists().forPath(path) != null) {
            // 递归删除节点
            client.delete().deletingChildrenIfNeeded().forPath(path);
            return true;
        }
        else {
            log.warn(String.format("node does not exists:[%s]", path));
            return false;
        }
    }

    @Override
    public String seeZkData(String path) throws Exception {
        List<String> childPaths = client.getChildren().forPath(path);
        ArrayList<JSONObject> objects = new ArrayList<>();
        for (String childPath : childPaths) {
            JSONObject jsonObject = new JSONObject();
            String _path = path.concat("/").concat(childPath);
            String value = new String(client.getData().forPath(_path));
            jsonObject.put("path",_path);
            jsonObject.put("value",value);
            objects.add(jsonObject);
        }
        return JSON.toJSONString(objects);
    }
}
