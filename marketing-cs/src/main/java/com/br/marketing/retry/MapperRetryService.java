package com.br.marketing.retry;

/**
 * 数据库操作，重试方法
 */
public interface MapperRetryService {

    public void insertWithRetry();
}
