package com.br.marketing.service.Impl;

import com.br.marketing.entity.RequestOperationLog;
import com.br.marketing.mapper.RequestOperationLogMapper;
import com.br.marketing.service.LogRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author kongbx
 * @date 2024/4/18
 */
@Service
@Slf4j
public class LogRecordServiceImpl implements LogRecordService {

    @Resource
    private RequestOperationLogMapper requestOperationLogMapper;

    @Override
    public void insert(RequestOperationLog requestOperationLog) {
        requestOperationLogMapper.insert(requestOperationLog);
    }
}
