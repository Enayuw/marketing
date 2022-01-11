package com.br.marketing.service;

import java.util.HashMap;
import java.util.Map;

public interface ResourceAllocationService {

    /**
     * 获取线程池信息
     * @return
     * @throws Exception
     */
    HashMap getThreadPoolData() throws Exception;

    /**
     * 修改节点值
     * @param map
     * @return
     */
    Boolean editThreadPoolNum(Map map) throws Exception;
}
