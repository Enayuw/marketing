package com.br.marketing.service.Impl.xc;

/**
 * 携程CPS异常数据重试服务接口
 * @Author chenh
 * @Date 2025-06-26
 */
public interface XieChengCpsExceptionDataRetryService {
    
    /**
     * 执行CPS重试撞库处理
     */
    void process();
}