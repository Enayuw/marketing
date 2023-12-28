package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.entity.RequestInterfaceLog;
import com.br.marketing.mapper.RequestInterfaceLogMapper;
import com.br.marketing.service.RequestInterfaceLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class RequestInterfaceLogServiceImpl implements RequestInterfaceLogService {

    @Resource
    private RequestInterfaceLogMapper requestInterfaceLogMapper;

    @Qualifier("requestInterfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor requestInterfaceLogDbpool;
    @Override
    public void saveLog(String apiCode, String url, Object data,Object result,long expireTime) {
        try {
            RequestInterfaceLog req = new RequestInterfaceLog();
            req.setApiCode(apiCode);
            req.setUrl(url);
            req.setRequestParam(data.toString());
            req.setResult(JSON.toJSONString(result));
            req.setExpire(expireTime);
            req.setCreateTime(new Date());
            requestInterfaceLogDbpool.submit(()->{
                try {
                    requestInterfaceLogMapper.insertSelective(req);
                }catch (Exception e){
                    log.error("第三方调用api 接口 日志存储异常，{}",e);
                }
            });
        }catch (Exception e){
            log.error("第三方调用api 接口 日志存储异常，{}",e);
        }
    }


}
