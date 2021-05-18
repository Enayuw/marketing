package com.br.marketing.check.controller;

import com.br.marketing.check.service.Impl.DeleteMonitorServiceImpl;
import com.br.marketing.check.utils.DeleteFileUtil;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.file.FtpUtil2;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.service.Impl.ValidDataAlarmServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/delete/")
@Slf4j
public class DeleteMonitorController {

    @Value("${otherConfig.warning.ftpHost:00}")
    private String ftpHost;
    @Value("${otherConfig.warning.ftpPort:00}")
    private Integer ftpPort;
    @Value("${otherConfig.warning.ftpUsername:00}")
    private String ftpUsername;
    @Value("${otherConfig.warning.ftpPwd:00}")
    private String ftpPwd;
    @Value("${otherConfig.warning.path:00}")
    private String path;
    @Resource
    DeleteMonitorServiceImpl deleteMonitorService;
    @Resource
    LoadResultMapper loadResultMapper;
    private static final Pattern MYREGEX = Pattern.compile("\\.");
    private static final Pattern MYREGEX1 = Pattern.compile("_");

    @Resource
    ValidDataAlarmServiceImpl validDataAlarmService;
    @GetMapping("deleteMonitor")
    public String deleteMonitor(){
        Map<String, Set<String>> map=new HashMap<>();
        FtpUtil2 ftp=new FtpUtil2();
        try {
            boolean connect = ftp.connect( "/loanwarn/", ftpHost, ftpPort, ftpUsername, ftpPwd);
            if(connect){
                log.info("======登录成功===开始剔除文件处理======");
            }else{
                log.info("======登录失败=========");
                return "success";
            }
            DeleteFileUtil.listFiles("/loanwarn/",map,ftp);
            if(!map.isEmpty()){
                log.info("----------开始处理新上传的剔除文件-------------");
                dealDeleteMonitorFile(map,ftp);
            }
        } catch (Exception e) {
            log.error("获取ftp上的剔除文件列表出错",e);
        }finally {
            ftp.closeFtp();
        }
        return "success";
    }

    /**
     * 处理新上传的剔除监控的文件
     * @param map 存储新上传的剔除监控的文件路径和名称
     * @param ftp
     */
    private void dealDeleteMonitorFile(Map<String, Set<String>> map, FtpUtil2 ftp) {
        log.info("dealDeleteMonitorFile:{}",map);
        for(Map.Entry<String,Set<String>> entry:map.entrySet()){
            String key = entry.getKey();
            Set<String> value = entry.getValue();
            MerchantParam merchantParam = DeleteFileUtil.vaildApicode(key);
            if(merchantParam==null){
                log.error("vaildApicode error {}",key);
                continue;
            }
            String apiCode = merchantParam.getApiCode();
            StringBuilder localFile=new StringBuilder(path)
                    .append("delete")
                    .append("/")
                    .append(apiCode)
                    .append("/")
                    .append(DateHelper.getDateAddYyMmDd(0)).append("/");
            if(StringUtils.isNotEmpty(apiCode)&&(apiCode.equals(Constants.APICODE_360)||apiCode.equals(Constants.APICODE_360_QA))){
                List<String> finishList = isFinish(value);
                for(String finishName:finishList){
                    log.debug("finishName:{}",finishName);
                    for(String fileName:value){
                        if(fileName.endsWith(".zip")){
                            String s1 = MYREGEX.split(fileName)[0];
                            String[] s = MYREGEX1.split(s1);
                            String  name=s[0]+"_"+s[1]+"_"+s[3]+"_"+s[4];
                            log.debug("fileName:{},name:{}",fileName,name);
                            if(finishName.equals(name)){
                                StringBuilder errorMessage=new StringBuilder("压缩文件异常,");
                                if(DeleteFileUtil.vaildFileName(fileName, apiCode,errorMessage)){
                                    deleteMonitorService.parsingFile(key,fileName,localFile.toString(),apiCode,merchantParam,finishName,ftp);
                                }else{
                                    DeleteFileUtil.returnErrorFile(apiCode, localFile.toString(), fileName, errorMessage,ftp);
                                    LoadResult lr=new LoadResult(apiCode,finishName,fileName,errorMessage.toString(),"0","",0,0,"delete");
                                    loadResultMapper.insertLoadResult(lr);
                                }
                            }
                        }
                    }
                    try {
                        validDataAlarmService.deleteMonitorFileUpload(apiCode,finishName);
                        ftp.rename(finishName+".finish",finishName+".finish"+".bak");
                    } catch (Exception e) {
                        log.error("rename finish error ",e);
                    }
                }
            }else{
                for(String fileName:value){
                    if(fileName.endsWith(".zip")){
                        String successFile=fileName+".success";
                        if(value.contains(successFile)){
                            String[] split = MYREGEX.split(fileName);
                            String zipName = split[0];
                            StringBuilder errorMessage=new StringBuilder("压缩文件异常,");
                            if(DeleteFileUtil.vaildFileName(fileName, apiCode,errorMessage)){
                                deleteMonitorService.parsingFile(key,fileName,localFile.toString(),apiCode,merchantParam,zipName,ftp);
                            }else{
                                DeleteFileUtil.returnErrorFile(apiCode, localFile.toString(), fileName, errorMessage,ftp);
                                LoadResult lr=new LoadResult(apiCode,zipName,fileName,errorMessage.toString(),"0","",0,0,"delete");
                                loadResultMapper.insertLoadResult(lr);
                            }
                            validDataAlarmService.deleteMonitorFileUpload(apiCode,zipName);
                        }
                    }
                }
            }
        }
    }

    /**
     * 是否含有finish文件
     * @param value 文件名称集合
     * @return
     */
    private List<String> isFinish(Set<String> value ){
        List<String> list=new ArrayList<>();
        for(String key:value){
            if(key.endsWith(".finish")){
                String[] split = MYREGEX.split(key);
                String finishName = split[0];
                list.add(finishName);
            }
        }
        return list;
    }
}
