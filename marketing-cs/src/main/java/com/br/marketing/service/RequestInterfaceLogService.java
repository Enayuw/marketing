package com.br.marketing.service;

import com.br.marketing.entity.RequestInterfaceLogWithBlobs;

/**
 * 请求接口参数服务类
 */
public interface RequestInterfaceLogService {

    void saveLog(String apiCode, String url, Object data,Object result,long expireTime );



}
