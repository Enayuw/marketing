package com.br.marketing.service.sftp.impl;

import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.service.EmailService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.sftp.ZipFileCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
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
    @Autowired
    SyncConfigService syncConfigService;
    @Resource
    LoanFileMapper loanFileMapper;
    @Resource
    EmailService businessAlarmServiceImpl;
    private static final Pattern MYREGEX = Pattern.compile("/");
    @Override
    public void zipFileCheck(LoanFile file) {
        try {
            String replace = file.getZipFileName().replace(".zip", ".txt");
            String zipFilePathAndName=file.getFilePath().concat("/").concat(file.getZipFileName());
            File zipFile=new File(zipFilePathAndName);
            if(!zipFile.exists()){
                log.error("压缩包中文件不存在。zipFilePathAndName：{}",zipFilePathAndName);
            }
            long zipTrueSize = getZipTrueSize(zipFilePathAndName);
            long txtFileLength = getTxtFileLength(zipFilePathAndName);
            if(zipTrueSize!=txtFileLength){
                businessAlarmServiceImpl.zipFileErrorAlarm(zipFilePathAndName,file.getApiCode());
                log.error("压缩包中文件大小与源文件大小不一致。zipFile：{}，压缩包中文件大小：{},源文件：{}，大小：{}",zipFilePathAndName,
                        zipTrueSize,syncConfigService.getPath() + file.getApiCode() + "/" + file.getBatchNumber() + "/" + replace,txtFileLength);
            }else {
                String md5="";
                if(zipFile.length()>1){
                    try {
                        md5 = MyFileUtil.getMd5(new FileInputStream(zipFilePathAndName));
                    } catch (IOException e) {
                        log.error("获取文件MD5出错",e);
                    }
                }
                Map<String,String> param=new HashMap<>();
                param.put("apiCode",file.getApiCode());
                param.put("batchNumber",file.getBatchNumber());
                param.put("fileName",file.getZipFileName());
                param.put("md5",md5);
                log.warn("param:{}",param);
                loanFileMapper.updateZipFileStatus(param);
                log.info("压缩包中文件大小{}:源文件大小{}:{}",syncConfigService.getPath() + file.getApiCode() + "/" + file.getBatchNumber() + "/" + replace,zipTrueSize,txtFileLength);
            }
        }catch (Exception e){
            log.error("校验压缩包文件出错",e);
        }

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
     * @return txt文件的大小和
     */
    private long getTxtFileLength(String filePath){
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
