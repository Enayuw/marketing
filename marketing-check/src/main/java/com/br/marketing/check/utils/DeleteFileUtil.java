package com.br.marketing.check.utils;

import com.br.marketing.client.IceClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.file.FtpUtil2;
import com.br.marketing.entity.MerchantParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class DeleteFileUtil {
    private static final Pattern MYREGEX = Pattern.compile("\\.");
    private static final Pattern REMARK_REGEX = Pattern.compile(Constants.DELETE_MONIZTOR_REMARK);
    private static final Pattern FILE_NUM_REGEX = Pattern.compile(Constants.DELETE_MONIZTOR_SERIALNUMBER);
    private static final Pattern FILE_NAME_REGEX =Pattern.compile("^[0-9]{7}");
    /**
     * 返回文件校验失败错误文件
     * @param apiCode 客户编号
     * @param localFile 本地路径
     * @param fileName 客户上传的文件名称
     * @param errorMessage 校验出错提示信息
     */
    public static void returnErrorFile(String apiCode, String  localFile, String fileName, StringBuilder errorMessage,FtpUtil2 ftp) {
        StringBuilder errorFileName=new StringBuilder();
        String s = MYREGEX.split(fileName)[0];
        File dir= new File(localFile);
        if(!dir.isDirectory()){
            dir.mkdirs();
        }
        errorFileName.append(localFile).append("/")
                .append(apiCode)
                .append("_")
                .append(s)
                .append(Constants.ERRORFILE)
                .append(DateHelper.getDateAddYyMmDdHhMmSs(0))
                .append(".txt");
        log.info("localFile:{},errorFileName：{}",localFile,errorFileName);
        File deleteErrorFile = new File(errorFileName.toString());
        try (Writer  fw = new BufferedWriter(
                new OutputStreamWriter(
                         Files.newOutputStream(Paths.get(errorFileName.toString())), StandardCharsets.UTF_8));){

            fw.append("errorType,message\n");
            fw.append(errorMessage+"\n");
        }catch (Exception e){
            log.error("生成错误文件出错",e);
        }
        ftp.changeWorkingDirectory("/loanwarn/"+apiCode+"/error/");
        if(deleteErrorFile.isFile()){
            try {
                ftp.upload(deleteErrorFile);
                File successFile=new File(errorFileName.toString()+".success");
                successFile.createNewFile();
                if(successFile.exists()){
                    ftp.upload(successFile);
                }
            } catch (Exception e) {
                log.error("上传错误文件到ftp出错",e);
            }
        }

        ftp.changeWorkingDirectory("/loanwarn/"+apiCode+"/input/");
        try {
            ftp.rename(fileName,fileName+".bak");
            ftp.rename(fileName+".success",fileName+".success.bak");
        } catch (Exception e) {
            log.error("重命名ftp上文件出错",e);
        }

    }

    /**
     * 校验剔除文件名称是否正确
     * @param fileName 剔除文件名称
     * @param apiCode apiCode
     */
    public static boolean vaildFileName(String fileName, String apiCode,StringBuilder errorMessage) {
        if(errorMessage==null){
            return false;
        }
        if(StringUtils.isNotEmpty(fileName)){
            String[] s = fileName.split("\\.");
            if(s.length<2){
                errorMessage.append("文件名称命名异常");
                return false;
            }
            String name = s[0];
            String[] s1 = name.split("_");
            if(apiCode.equals(Constants.APICODE_360_QA)||apiCode.equals(Constants.APICODE_360)){
                if(s1.length!=5){
                    errorMessage.append("文件名称命名异常");
                    return false;
                }
                if(!apiCode.equals(s1[0])){
                    errorMessage.append("apicode异常");
                    return false;
                }else if(!REMARK_REGEX.matcher(s1[1]).matches()){
                    errorMessage.append("文件批次命名异常");
                    return false;
                }else if(!FILE_NUM_REGEX.matcher(s1[2]).matches()){
                    errorMessage.append("文件编号异常");
                    return false;
                }else if(!"DeleteMonitor".equals(s1[3])){
                    errorMessage.append("文件名称命名异常");
                    return false;
                }else {
                    String s2 = s1[4];
                    try {
                        DateHelper.parseDate(s2);
                    }catch (IllegalArgumentException e){
                        log.error("日期异常",e);
                        errorMessage.append("日期异常");
                        return false;
                    }
                }
            }else{
                if(s1.length!=4){
                    errorMessage.append("文件名称命名异常");
                    return false;
                }else{
                    if(!apiCode.equals(s1[0])){
                        errorMessage.append("apicode异常");
                        return false;
                    }else if(!REMARK_REGEX.matcher(s1[1]).matches()){
                        errorMessage.append("文件批次命名异常");
                        return false;
                    }else if(!"DeleteMonitor".equals(s1[2])){
                        errorMessage.append("文件名称命名异常");
                        return false;
                    }else {
                        String s2 = s1[3];
                        try {
                            DateHelper.parseDate(s2);
                        }catch (IllegalArgumentException e){
                            log.error("日期异常",e);
                            errorMessage.append("日期异常");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 校验ftp目录中的apiCode是否正确
     * @param key ftp目录
     * @return
     */
    public static MerchantParam vaildApicode(String key) {
        if(StringUtils.isEmpty(key)){
            return null;
        }
        String[] split = key.split("/");
        if(split.length!=4){
            return null;
        }
        String apiCode = split[2];
        MerchantParam merchantParam = IceClient.getMerchantParam(apiCode);
        if (merchantParam==null){
            log.error("merchantParam is null,{}",apiCode);
            return null;
        }
        return merchantParam;
    }


    /**
     * 获取ftp上的新上传的剔除数据文件。
     * @param path ftp路径
     * @param map 新上传的剔除文件放在map里
     */
    public static void listFiles(String path, Map<String, Set<String>> map,FtpUtil2 ftp){
        try {
            if(path.startsWith("/")&&path.endsWith("/")){
                String directory = path;
                ftp.change(directory);
                FTPFile[] files = ftp.listFiles();
                for(FTPFile file:files){
                    log.debug("files file:{}", file.getName());
                    if(file.isFile()){

                        FTPFile[] ftpFiles = ftp.listFiles(path, new FTPFileFilter() {
                            @Override
                            public boolean accept(FTPFile ftpFile) {
                                boolean flag = false;
                                if (ftpFile.isFile()) {
                                    String name = ftpFile.getName();
                                    log.debug("listFiles name:{}", name);
                                    if (StringUtils.isNotEmpty(name)&&name.indexOf("DeleteMonitor")>=0&&(name.endsWith(".zip")
                                            ||name.endsWith(".finish")||name.endsWith(".success"))) {
                                        flag = true;
                                    }
                                }
                                return flag;
                            }
                        });
                        log.debug("listFiles ftpFiles:{}", ftpFiles);
                        if(ftpFiles.length>0){
                            Set<String> list=new HashSet<>();
                            for(int i=0;i<ftpFiles.length;i++){
                                String name = ftpFiles[i].getName();
                                list.add(name);
                            }

                            Set<String> list1 = map.get(path);
                            log.debug("path:{},list:{},list1:{}",path,list,list1);
                            if(list1!=null&&list1.size()>0){
                                list1.addAll(list);
                                map.put(path,list1);
                            }else {
                                map.put(path,list);
                            }
                        }
                    }else if(file.isDirectory()){
                        String fileName = file.getName();
                        log.debug("fileName:{}",fileName);
                        if(FILE_NAME_REGEX.matcher(fileName).matches()||"input".equals(fileName)){
                            log.debug("isDirectory file:{}",fileName);
                            listFiles(path+fileName+"/",map,ftp);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("获取ftp上的剔除文件列表出错",e);
        }
    }

}
