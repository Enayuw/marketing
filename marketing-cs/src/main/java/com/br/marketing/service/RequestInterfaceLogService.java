package com.br.marketing.service;

/**
 * 请求接口参数服务类
 */
public interface RequestInterfaceLogService {

    void saveLog(String apiCode, String url, Object data,Object result,long expireTime );



}
