package com.br.marketing.mapper;

import com.br.marketing.entity.RequestOperationLog;

public interface RequestOperationLogMapperBase {

    /**
     * insert
     * @param log log
     * @return int 插入的数据量
     */
    int insert(RequestOperationLog log);

}