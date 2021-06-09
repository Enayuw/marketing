package com.br.marketing.push.aspect;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.push.service.ZipFileCheckService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * @Author: Bairong
 * @Time: 2020/11/20 10:36
 * @Company：百融
 * @Description: 文件上传到ftp时，发送文件全路径信息到mq
 */
@Aspect
@Component
@Slf4j
public class ZipFileUploadAspect {
    @Value("${otherConfig.warning.path:00}")
    private String path;
    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;
    @Resource
    ZipFileCheckService zipFileCheckServiceImpl;

    @Pointcut("execution(public * com.br.marketing.push.service.impl.Push*Impl.push(..))")
    public void push(){}


    @After("com.br.marketing.push.aspect.ZipFileUploadAspect.push()")
    public void push(JoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();
//        checkZipFile(args);
//        pushToSftp(args);
    }


}
