package com.br.marketing.check.utils;

import com.br.common.validator.DateUtils;
import com.br.marketing.client.IceClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.file.FtpUtil2;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.LoadResultMapper;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author: Bairong
 * @Time: 2020/12/9 16:57
 * @Company：百融
 * @Description: 功能描述
 */
@Slf4j
public class SftpToDbUtils {
    private static final Pattern MYREGEX = Pattern.compile("\\.");
    private static final Pattern REMARK_REGEX = Pattern.compile(Constants.DELETE_MONIZTOR_REMARK);
    private static final Pattern FILE_NUM_REGEX = Pattern.compile(Constants.DELETE_MONIZTOR_SERIALNUMBER);
    private static final Pattern FILE_NAME_REGEX =Pattern.compile("^[0-9]{7}");


    /**
     * 遍历sftp目录下所有的文件，获取需要处理的文件名称
     * @param path sftp目录
     * @param map 结果
     * @param isDelete 是否是剔除文件
     * @param sftpClient sftp连接
     */
    public static void listFtpFile(String path, Map<String, Set<String>> map, final boolean isDelete, SftpClient sftpClient) {
        try {
            Map<String, SftpATTRS> attrsMap = sftpClient.listFiles(path);
            //log.info("map key:{}",map.keySet());
            for(Map.Entry<String, SftpATTRS> entry : attrsMap.entrySet()){
                String fileName = entry.getKey();
                SftpATTRS attrs = entry.getValue();
                if(attrs.isDir()){
                    log.debug("fileName:{}",fileName);
                    if(FILE_NAME_REGEX.matcher(fileName).matches()||"input".equals(fileName)){
                        log.debug("isDirectory file:{}",fileName);
                        listFtpFile(path+fileName+"/",map,isDelete,sftpClient);
                    }
                }else{
                    log.debug("filename：{} Atime:{},size:{},atTime:{},Extended:{},Flags:{},gid:{},mTime:{},MtimeString:{},Permissions:{},PermissionsString:{},uid:{}"
                            ,fileName,attrs.getAtimeString(),attrs.getSize(),attrs.getATime()
                            ,attrs.getExtended(),attrs.getFlags(),attrs.getGId(),attrs.getMTime()
                            ,attrs.getMtimeString(),attrs.getPermissions(),attrs.getPermissionsString(),attrs.getUId());


                    String createFileTime = DateHelper.timeStamp2Date(attrs.getMTime() + "", "yyyy-MM-dd HH:mm:ss");
                    String s1 = DateUtils.parseDateTimeByDate(new Date(), "yyyy-MM-dd HH:mm:ss");
                    long[] distanceTimes = DateHelper.getDistanceTimes(createFileTime, s1);
                    if(distanceTimes[0]==0&&distanceTimes[1]==0&&distanceTimes[2]<1){
                        log.warn("文件上传时间距离当前时间小于1分钟，暂时不处理");
                        continue;
                    }
                    boolean  containDelete= fileName.indexOf("DeleteMonitor")>=0;
                    if ((isDelete==containDelete)&& StringUtils.isNotEmpty(fileName)&&(fileName.endsWith(".zip")
                            ||fileName.endsWith(".finish")||fileName.endsWith(".success"))) {
                        Set<String> set = map.get(path);
                        if(set==null){
                            set=new HashSet<>();
                            map.put(path,set);
                        }
                        set.add(fileName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("遍历sftp文件出错",e);
        }
        log.warn("map :{}",map);
    }

    /**
     * 校验ftp目录中的apiCode是否正确
     * @param key ftp目录 loanwarn/4200333/input
     * @return
     */
    public static MerchantParam vaildApicode(String key) {
        if(StringUtils.isEmpty(key)){
            return null;
        }
        String[] split = key.split("/");
        if(split.length!=5){
            return null;
        }
        String apiCode = split[3];
        MerchantParam merchantParam = IceClient.getMerchantParam(apiCode);
        if (merchantParam==null){
            log.error("merchantParam is null,{}",apiCode);
            return null;
        }
        return merchantParam;
    }


    /**
     * 校验文件名称是否正确
     * @param fileName 文件名称
     *                 数据文件：
     *                          360:3005390_UploadCustomFileName20200923_00003001_20200923.zip
     *                          其他：3004761_bairongniankuanguserinfo20200820_20200821.zip
     *                 剔除文件：
     *                          360：4000100_2020080401_001_DeleteMonitor_20200710.zip
     *                          其他：4000100_2020080401001_DeleteMonitor_20200710.zip
     * @param apiCode apiCode
     */
    public static boolean vaildFileName(String fileName, String apiCode,StringBuilder errorMessage,boolean isDelete) {

        if(StringUtils.isNotEmpty(fileName)){
            String[] s = fileName.split("\\.");
            if(s.length<2){
                errorMessage.append("文件名称命名异常");
                return false;
            }
            String name = s[0];
            String[] s1 = name.split("_");
            if(apiCode.equals(Constants.APICODE_360_QA)||apiCode.equals(Constants.APICODE_360)){
                int length=isDelete ? 5:4;
                if(s1.length!=length){
                    errorMessage.append("文件名称命名异常");
                    return false;
                }else{
                    if(!apiCode.equals(s1[0])){
                        errorMessage.append("apicode异常");
                        return false;
                    }else if(!REMARK_REGEX.matcher(s1[1]).matches()){
                        errorMessage.append("文件批次命名异常");
                        return false;
                    }else if(!FILE_NUM_REGEX.matcher(s1[2]).matches()){
                        errorMessage.append("文件编号异常");
                        return false;
                    }else if(isDelete&&!"DeleteMonitor".equals(s1[3])){
                        errorMessage.append("文件名称命名异常");
                        return false;
                    }else {
                        String s2=isDelete ? s1[4] : s1[3];
                        try {
                            DateHelper.parseDate(s2);
                        }catch (IllegalArgumentException e){
                            log.error("日期异常",e);
                            errorMessage.append("日期异常");
                            return false;
                        }
                    }
                }
            }else{
                int length=isDelete ? 4:3;
                if(s1.length!=length){
                    errorMessage.append("文件名称命名异常");
                    return false;
                }else{
                    if(!apiCode.equals(s1[0])){
                        errorMessage.append("apicode异常");
                        return false;
                    }else if(!REMARK_REGEX.matcher(s1[1]).matches()){
                        errorMessage.append("文件批次命名异常");
                        return false;
                    }else if(isDelete&&!"DeleteMonitor".equals(s1[2])){
                        errorMessage.append("文件名称命名异常");
                        return false;
                    }else {
                        String s2=isDelete ? s1[3] : s1[2];
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
     * 是否含有finish文件
     * @param value 文件名称集合
     * @return
     */
    public static List<String> isFinish(Set<String> value){
        List<String> list=new ArrayList<>();
        for(String key:value){
            if(key.endsWith(".finish")){
                String[] split = MYREGEX.split(key);
                if(split.length>2){
                    continue;
                }
                String finishName = split[0];
                list.add(finishName);
            }
        }
        return list;
    }
    /**
     * 校验txt文件内容
     * @param linenumber 文件行数
     * @param localFilePath 当前路径
     * @param errorMessage 错误信息
     * @param apiCode 客户编号
     * @param fileName 压缩包文件名称
     * @param head 表头
     * @param sftpClient
     * @return 校验成功或者失败
     */
    public static  boolean checkDeleteTxtContent(int linenumber, String localFilePath, StringBuilder errorMessage,
                                   String apiCode, String fileName, String head, String cusBatch, SftpClient sftpClient,LoadResultMapper loadResultMapper){
        if(linenumber==0){
            errorMessage.append("文件内容为空");
            returnDeleteErrorFile(apiCode, localFilePath, fileName, errorMessage,sftpClient);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0","",0,0,"delete");
            loadResultMapper.insertLoadResult(lr);
            log.error("txt 文件内容为空 ");
            return false;
        }else{
            boolean headFlag=true;
            if(com.br.marketing.common.utils.StringUtils.isEmpty(head)){
                headFlag=false;
            }else{
                int cusNum = head.indexOf("cus_num");
                int id = head.indexOf("id");
                int cell = head.indexOf("cell");
                int name = head.indexOf("name");
                if(cusNum==-1||(id==-1&&cell==-1&&name==-1)){
                    headFlag=false;
                }
            }
            if(!headFlag){
                errorMessage.append("文件表头异常");
                returnDeleteErrorFile(apiCode, localFilePath, fileName, errorMessage,sftpClient);
                LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0","",0,0,"delete");
                loadResultMapper.insertLoadResult(lr);
                log.error("txt 文件表头异常:{} ",head);
                return false;
            }
        }
        return true;
    }

    /**
     * 校验txt文件格式
     * @param fileName 压缩包文件名
     * @param apiCode 客户编号
     * @param localFilePath 当前路径
     * @param txtFileName 应有的txt文件名称
     * @param errorMessage 错误信息
     * @param sftpClient
     * @return 校验成功或者失败
     */
    public static boolean checkDeleteTxtfile(String fileName, String apiCode, String localFilePath,
                                       String txtFileName, StringBuilder errorMessage, String cusBatch, SftpClient sftpClient,LoadResultMapper loadResultMapper){
        File dir=new File(localFilePath);
        File[] fileList = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathName) {
                String name = pathName.getName();
                if(name.endsWith(".txt")){
                    return true;
                }
                return false;
            }
        });

        boolean flag=true;
        if(fileList==null||fileList.length==0){
            errorMessage.append("压缩文件找不到上传数据文件");
            returnDeleteErrorFile(apiCode, localFilePath, fileName, errorMessage,sftpClient);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0","",0,0,"delete");
            loadResultMapper.insertLoadResult(lr);
            return false;
        }
        if(fileList.length!=1){
            errorMessage.append("压缩文件找不到上传数据文件");
            flag=false;
        }else if(!fileList[0].getName().equals(txtFileName)){
            File file1 = fileList[0];
            String name = file1.getName();
            flag = DeleteFileUtil.vaildFileName(name, apiCode, errorMessage);
        }

        if(!flag){
            returnDeleteErrorFile(apiCode, localFilePath, fileName, errorMessage,sftpClient);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0","",0,0,"delete");
            loadResultMapper.insertLoadResult(lr);
            log.error("txt 文件名称错误 txt :{},zip:{}",fileList[0].getName(),fileName);
            return false;
        }
        return true;
    }
    /**
     * 返回文件校验失败错误文件
     * @param apiCode 客户编号
     * @param localFile 本地路径
     * @param fileName 客户上传的文件名称
     * @param errorMessage 校验出错提示信息
     */
    public static void returnDeleteErrorFile(String apiCode, String  localFile, String fileName, StringBuilder errorMessage, SftpClient sftpClient) {
        StringBuilder errorFileName=new StringBuilder();
        String s = MYREGEX.split(fileName)[0];
        File dir= new File(localFile);
        if(!dir.isDirectory()){
            dir.mkdirs();
        }
        errorFileName
                .append(apiCode)
                .append("_")
                .append(s)
                .append(Constants.ERRORFILE)
                .append(DateHelper.getDateAddYyMmDdHhMmSs(0))
                .append(".txt");
        log.info("localFile:{},errorFileName：{}",localFile,errorFileName);
        File deleteErrorFile = new File(localFile+errorFileName.toString());
        try (Writer  fw = new BufferedWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(Paths.get(localFile+errorFileName.toString())), StandardCharsets.UTF_8));){

            fw.append("errorType,message\n");
            fw.append(errorMessage+"\n");
        }catch (Exception e){
            log.error("生成错误文件出错",e);
        }
        if(deleteErrorFile.isFile()){
            try {
                boolean upload = sftpClient.uploadFile("/UploadFiles/loanwarn/" + apiCode + "/error/", errorFileName.toString(), localFile+errorFileName.toString());
                File successFile=new File(localFile+errorFileName.toString()+".success");
                successFile.createNewFile();
                if(successFile.exists()){
                    sftpClient.uploadFile("/UploadFiles/loanwarn/"+apiCode+"/error/",errorFileName+".success",localFile+errorFileName.toString()+".success");
                }
            } catch (Exception e) {
                log.error("上传错误文件到ftp出错",e);
            }
        }
        try {
            String sftpPath = "/UploadFiles/loanwarn/" + apiCode + "/input/";
            sftpClient.rename(sftpPath+fileName,sftpPath+fileName+".bak");
            sftpClient.rename(sftpPath+fileName+".success",sftpPath+fileName+".success.bak");
        } catch (Exception e) {
            log.error("重命名ftp上文件出错",e);
        }

    }
    /**
     * 返回文件校验失败错误文件
     * @param apiCode 客户编号
     * @param localFile 本地路径
     * @param fileName 客户上传的文件名称
     * @param errorMessage 校验出错提示信息
     */
    public static void returnErrorFile(String apiCode, String  localFile, String fileName, StringBuilder errorMessage,SftpClient sftpClient) {
        StringBuilder errorFileName=new StringBuilder();
        String s = MYREGEX.split(fileName)[0];
        File dir= new File(localFile);
        if(!dir.isDirectory()){
            dir.mkdirs();
        }
        errorFileName.append(apiCode);
        if(!apiCode.equals(Constants.APICODE_PPD)&&!apiCode.equals(Constants.APICODE_PPD_QA)
                &&!apiCode.equals(Constants.APICODE_360)&&!apiCode.equals(Constants.APICODE_360_QA)
                &&!apiCode.equals(Constants.APICODE_SN_RISK_DEPARTMENT)&&!apiCode.equals(Constants.APICODE_SN_RISK_DEPARTMENT_QA)){
            errorFileName.append("_").append(s);
        }
        errorFileName.append(Constants.ERRORFILE)
                .append(DateHelper.getDateAddYyMmDdHhMmSs(0))
                .append(".txt");
        log.info("localFile:{},errorFileName：{}",localFile,errorFileName);
        StringBuilder absolutePath=new StringBuilder();
        absolutePath.append(localFile)
                .append("/")
                .append(errorFileName);
        File errorFile = new File(absolutePath.toString());
        if(errorFile.exists()){
            log.error("errorFileName {}文件已存在",absolutePath);
        }


        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(Paths.get(absolutePath.toString())), StandardCharsets.UTF_8));){

            fw.append("errorType,message\n");
            fw.append(errorMessage+"\n");
        }catch (Exception e){
            log.error("生成错误文件出错",e);
        }
        if(errorFile.isFile()){
            try {
                boolean upload = sftpClient.uploadFile("/UploadFiles/loanwarn/" + apiCode + "/error/", errorFileName.toString(), absolutePath.toString());
                if(upload){
                    File successFile=new File(absolutePath.toString()+".success");
                    successFile.createNewFile();
                    if(successFile.exists()){
                        sftpClient.uploadFile("/UploadFiles/loanwarn/"+apiCode+"/error/",errorFileName+".success",absolutePath.toString()+".success");
                    }
                }
            } catch (Exception e) {
                log.error("上传错误文件到ftp出错",e);
            }
        }
    }

    /**
     * 校验txt文件格式
     * @param fileName 压缩包文件名
     * @param apiCode 客户编号
     * @param localFilePath 当前路径
     * @param txtFileName 应有的txt文件名称
     * @param errorMessage 错误信息
     * @return 校验成功或者失败
     */
    public static boolean checkTxtfile(String fileName, String apiCode, String  localFilePath,
                                       String txtFileName, StringBuilder errorMessage, String cusBatch,
                                       LoadResultMapper loadResultMapper, String batchNumber, SftpClient sftpClient){
        File dir=new File(localFilePath);
        File[] fileList = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathName) {
                String name = pathName.getName();
                if(name.endsWith(".txt")){
                    return true;
                }
                return false;
            }
        });

        boolean flag=true;
        if(fileList==null||fileList.length==0){
            errorMessage.append("压缩文件找不到上传数据文件");
            returnErrorFile(apiCode, localFilePath, fileName, errorMessage,sftpClient);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0",batchNumber,0,0,"");
            loadResultMapper.insertLoadResult(lr);
            return false;
        }
        if(fileList.length!=1){
            errorMessage.append("压缩文件找不到上传数据文件");
            flag=false;
        }else{
            if(!fileList[0].getName().equals(txtFileName)){
                File file1 = fileList[0];
                String name = file1.getName();
                flag = vaildFileName(name, apiCode, errorMessage,false);
            }
        }


        if(!flag){
            returnErrorFile(apiCode, localFilePath, fileName, errorMessage,sftpClient);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0",batchNumber,0,0,"");
            loadResultMapper.insertLoadResult(lr);
            log.error("txt 文件名称错误 txt :{},zip:{}",fileList[0].getName(),fileName);
            return false;
        }
        return true;
    }

    /**
     * 校验txt文件内容
     * @param linenumber 文件行数
     * @param localFilePath 当前路径
     * @param errorMessage 错误信息
     * @param apiCode 客户编号
     * @param fileName 压缩包文件名称
     * @param head 表头
     * @return 校验成功或者失败
     */
    public static boolean checkTxtContent(int linenumber,String localFilePath,StringBuilder errorMessage,
                                          String apiCode,String fileName,String head,String cusBatch,
                                          LoadResultMapper loadResultMapper,String batchNumber,SftpClient sftpClient,MerchantParam merchantParam){
        if(linenumber==0){
            errorMessage.append("文件内容为空");
            returnErrorFile(apiCode, localFilePath, fileName, errorMessage,sftpClient);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0",batchNumber,0,0,"");
            loadResultMapper.insertLoadResult(lr);
            log.error("txt 文件内容为空 ");
            return false;
        }else{
            boolean headFlag=true;
            if(com.br.marketing.common.utils.StringUtils.isEmpty(head)){
                headFlag=false;
            }else{
                String[] headSplit = head.split(",");
                List<String> list = Arrays.asList(headSplit);
                int cusNum = head.indexOf("cus_num");
                if (!list.contains("cus_num")) {
                    headFlag=false;
                }
                /*int id = head.indexOf("id");
                int cell = head.indexOf("cell");
                int name = head.indexOf("name");*/
                if (merchantParam.getIsCheck() == 0||merchantParam.getIsCheck()==2||merchantParam.getIsCheck()==4) {
                    if (!list.contains("id")&&!list.contains("cell")&&!list.contains("name")) {
                        headFlag=false;
                    }
                } else if (merchantParam.getIsCheck() == 1||merchantParam.getIsCheck()==3||merchantParam.getIsCheck()==5) {
                    if (!list.contains("id")||!list.contains("cell")||!list.contains("name")) {
                        headFlag=false;
                    }
                }
            }
            if(!headFlag){
                errorMessage.append("文件表头异常");
                returnErrorFile(apiCode, localFilePath, fileName, errorMessage,sftpClient);
                LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0",batchNumber,0,0,"");
                loadResultMapper.insertLoadResult(lr);
                log.error("txt 文件表头异常:{} ",head);
                return false;
            }
        }
        return true;
    }

}
