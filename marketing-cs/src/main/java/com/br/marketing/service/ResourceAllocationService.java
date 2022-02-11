package com.br.marketing.service;

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
}
