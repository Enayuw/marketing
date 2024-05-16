package com.br.marketing.service;

import com.br.marketing.entity.RequestOperationLog;

/**
 * @author kongbx
 * @date 2024/4/18
 */
public interface LogRecordService {

    void insert(RequestOperationLog requestOperationLog);

}
