package com.br.marketing.push.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.push.service.ZipFileCheckService;
import com.br.marketing.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @Author: Bairong
 * @Time: 2020/11/20 15:31
 * @Company：百融
 * @Description: 压缩包文件校验实现类
 */
@Slf4j
@Service
public class ZipFileCheckServiceImpl implements ZipFileCheckService {
    @Value("${otherConfig.warning.path:00}")
    private String path;
    @Resource
    LoanFileMapper loanFileMapper;
    @Resource
    EmailService businessAlarmServiceImpl;
    private static final Pattern MYREGEX = Pattern.compile("/");
    @Override
    public void zipFileCheck(JSONObject jsonObject) {
        try {
            log.warn("jsonObject :{}",jsonObject);
            String apiCode = jsonObject.getString("apiCode");
            JSONArray array = jsonObject.getJSONArray("files");
            List<String> list = JSONObject.parseArray(array.toJSONString(), String.class);
            String batchNumber = jsonObject.getString("batchNumber");
            boolean result=true;
            for(String fileName:list){
                result= checkZipFileSize(apiCode, batchNumber, fileName);
            }
            log.warn("checkZipFileSize list size:{} result:{}",list.size(),result);
            if(apiCode.equals(Constants.APICODE_360)||apiCode.equals(Constants.APICODE_360_QA)){
                if(result){
                    Map<String,String> param=new HashMap<>();
                    param.put("apiCode",apiCode);
                    param.put("batchNumber",batchNumber);
                    log.warn("param:{}",param);
                    loanFileMapper.updateZipFileStatus(param);
                }
            }
        }catch (Exception e){
            log.error("校验压缩包文件出错",e);
        }

    }

    /**
     * 对比压缩文件中的文件与源文件的大小
     * @param apiCode apiCode
     * @param batchNumber 批次号
     * @param fileName 文件名称
     * @return 是否校验通过
     */
    private boolean checkZipFileSize(String apiCode, String batchNumber, String fileName) {
        boolean flag=false;
        String[] split = fileName.split("/");
        String s = split[split.length - 1];
        String replace = s.replace(".zip", ".txt");
        File zipFile=new File(fileName);
        if(!zipFile.exists()){
            log.error("压缩包中文件不存在。zipFile：{}",fileName);
        }
        long zipTrueSize = getZipTrueSize(fileName);
        long txtFileLength = getTxtFileLength(fileName, apiCode, batchNumber);
        /*String txtFilePath = fileName.replace(".zip", ".txt");
        File file = new File(txtFilePath);*/
        if(zipTrueSize!=txtFileLength){
            businessAlarmServiceImpl.zipFileErrorAlarm(fileName,apiCode);
            log.error("压缩包中文件大小与源文件大小不一致。zipFile：{}，压缩包中文件大小：{},源文件：{}，大小：{}",fileName,
                    zipTrueSize,path + "/" + apiCode + "/" + batchNumber + "/" + replace,txtFileLength);
        }else {
            if(!apiCode.equals(Constants.APICODE_360)&&!apiCode.equals(Constants.APICODE_360_QA)){
                String md5="";
               // if(zipFile.length()>1073741824){
                if(zipFile.length()>1){
                    try {
                        md5 = MyFileUtil.getMd5(new FileInputStream(fileName));
                    } catch (IOException e) {
                        log.error("获取文件MD5出错",e);
                    }
                }
                Map<String,String> param=new HashMap<>();
                param.put("apiCode",apiCode);
                param.put("batchNumber",batchNumber);
                param.put("fileName",s);
                param.put("md5",md5);
                log.warn("param:{}",param);
                loanFileMapper.updateZipFileStatus(param);
            }
            log.info("压缩包中文件大小{}:源文件大小{}:{}",path + "/" + apiCode + "/" + batchNumber + "/" + replace,zipTrueSize,txtFileLength);
            flag=true;
        }
        return flag;
    }

    /**
     * 获取压缩包文件中的源文件的大小
     * @param fileName 压缩文件名称
     * @return 压缩包中文件的大小
     */
    private long getZipTrueSize(String fileName) {
        long size = 0;
        try {
            ZipFile zipFile = new ZipFile(fileName);
            Enumeration<? extends ZipEntry> en = zipFile.entries();
            while (en.hasMoreElements()) {
                size += en.nextElement().getSize();
            }
        } catch (IOException e) {
            log.error("IOException",e);
        }
        return size;
    }

    /**
     * 获取当前目录下的所有数据文件的大小的和
     * @param filePath 压缩包文件全路径
     * @param apiCode apiCode
     * @param batchNumber 批次号
     * @return txt文件的大小和
     */
    private long getTxtFileLength(String filePath,String apiCode,String batchNumber){
        String[] split = MYREGEX.split(filePath);
        String zipfileName = split[split.length - 1];
        String path = filePath.replace(zipfileName, "");
        String fileName=zipfileName.replace(".zip","");
        File dir=new File(path);
        if(!dir.exists()){
            log.warn("路径{} 不存在",path);
        }
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.startsWith(fileName)&&name.endsWith(".txt")){
                    return true;
                }
                return false;
            }
        });
        long length=0;
        for(int i=0;i<files.length;i++){
            length=files[i].length()+length;
        }
        return length;
    }
}
