package com.br.marketing.innerapi.service;

import java.util.List;
import java.util.Map;

/**
 * 该接口只有在marketing-inner服务使用
 */
public interface ResourceAllocationService {

    /**
     * 获取线程池信息
     * @return
     * @throws Exception
     */
    List<Map> getThreadPoolData() throws Exception;

    /**
     * 修改节点值
     * @param list
     * @return
     */
    Boolean editThreadPoolNum(List<Map> list) throws Exception;

    /**
     * 创建zk节点
     * @param path
     * @param data
     * @return
     * @throws Exception
     */
    Boolean createZkData(String path,String data) throws Exception;

    /**
     * 修改zk节点信息
     * @param path
     * @param data
     * @return
     * @throws Exception
     */
    Boolean setNodeData(String path,String data) throws Exception;

    /**
     * 删除zk节点
     * @param path
     * @return
     * @throws Exception
     */
    Boolean deleteZkData(String path) throws Exception;

    String seeZkData(String path) throws Exception;
}
