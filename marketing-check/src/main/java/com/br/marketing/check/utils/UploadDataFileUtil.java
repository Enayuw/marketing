package com.br.marketing.check.utils;

import com.br.marketing.client.IceClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.FtpUtil;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.LoadResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ftp 客户新上传文件处理。包含剔除文件和正常的数据文件。
 * 业务逻辑区分360、ppd定制逻辑和通用逻辑
 * 1、新上传文件列表获取
 * 2.客户api_code校验
 * 3.数据文件名称校验
 * 4.错误提示文件生成&回传到ftp
 *
 *
 * 目前只有数据文件校验会使用和这个工具类，后面会将剔除文件的处理也合并到这个工具类里
 * 方法中isDelete 参数是为剔除文件的处理预留的
 */
@Slf4j
public class UploadDataFileUtil {
    private static final Pattern MYREGEX = Pattern.compile("\\.");
    private static final Pattern REMARK_REGEX = Pattern.compile(Constants.DELETE_MONIZTOR_REMARK);
    private static final Pattern FILE_NUM_REGEX = Pattern.compile(Constants.DELETE_MONIZTOR_SERIALNUMBER);
    private static final Pattern FILE_NAME_REGEX =Pattern.compile("^[0-9]{7}");




    /**
     * 获取ftp上的新上传的数据文件。
     * @param path ftp路径
     * @param map 新上传的数据文件放在map里
     * @param isDelete 是否为剔除文件
     */
    public static void listFiles(String path, Map<String, Set<String>> map, final boolean isDelete, FtpUtil ftpUtil){
        try {
            if(path.startsWith("/")&&path.endsWith("/")){
                String directory = path;
                boolean b = ftpUtil.changeWorkingDirectory(directory);
                if(!b){
                    log.error("changeWorkingDirectory error:{}",directory);
                    return;
                }
                FTPFile[] files = ftpUtil.listFiles();
                for(FTPFile file:files){
                    log.debug("files file:{}", file.getName());
                    if(file.isFile()){
                        FTPFile[] ftpFiles = ftpUtil.listFiles(path, new FTPFileFilter() {
                            @Override
                            public boolean accept(FTPFile ftpFile) {
                                boolean flag = false;
                                if (ftpFile.isFile()) {
                                    String name = ftpFile.getName();
                                    log.debug("listFiles name:{}", name);
                                    boolean  containDelete= name.indexOf("DeleteMonitor")>=0;
                                    if ((isDelete==containDelete)&&StringUtils.isNotEmpty(name)&&(name.endsWith(".zip")
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
                            listFiles(path+fileName+"/",map,isDelete,ftpUtil);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("获取ftp上的剔除文件列表出错",e);
        }
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
     * 返回文件校验失败错误文件
     * @param apiCode 客户编号
     * @param localFile 本地路径
     * @param fileName 客户上传的文件名称
     * @param errorMessage 校验出错提示信息
     */
    public static void returnErrorFile(String apiCode, String  localFile, String fileName, StringBuilder errorMessage,FtpUtil ftpUtil) {
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
                ftpUtil.changeWorkingDirectory("/loanwarn/"+apiCode+"/error/");
                boolean upload = ftpUtil.upload("/loanwarn/"+apiCode+"/error/"+errorFileName,errorFile);
                if(upload){
                    File successFile=new File(absolutePath.toString()+".success");
                    successFile.createNewFile();
                    if(successFile.exists()){
                        ftpUtil.upload("/loanwarn/"+apiCode+"/error/"+errorFileName+".success",successFile);
                    }
                }
            } catch (Exception e) {
                log.error("上传错误文件到ftp出错",e);
            }
        }
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
                                          LoadResultMapper loadResultMapper,String batchNumber,FtpUtil ftpUtil,MerchantParam merchantParam){
        if(linenumber==0){
            errorMessage.append("文件内容为空");
            UploadDataFileUtil.returnErrorFile(apiCode, localFilePath, fileName, errorMessage,ftpUtil);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0",batchNumber,0,0,"");
            loadResultMapper.insertLoadResult(lr);
            log.error("txt 文件内容为空 ");
            return false;
        }else{
            boolean headFlag=true;
            if(com.br.marketing.common.utils.StringUtils.isEmpty(head)){
                headFlag=false;
            }else{
                int cusNum = head.indexOf("cus_num");
                if (cusNum == -1) {
                    headFlag=false;
                }
                int id = head.indexOf("id");
                int cell = head.indexOf("cell");
                int name = head.indexOf("name");
                if (merchantParam.getIsCheck() == 0||merchantParam.getIsCheck()==2||merchantParam.getIsCheck()==4) {
                    if (id==-1&&cell==-1&&name==-1) {
                        headFlag=false;
                    }
                } else if (merchantParam.getIsCheck() == 1||merchantParam.getIsCheck()==3||merchantParam.getIsCheck()==5) {
                    if (id == -1 || name == -1 || cell == -1 ) {
                        headFlag=false;
                    }
                }
            }
            if(!headFlag){
                errorMessage.append("文件表头异常");
                UploadDataFileUtil.returnErrorFile(apiCode, localFilePath, fileName, errorMessage,ftpUtil);
                LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0",batchNumber,0,0,"");
                loadResultMapper.insertLoadResult(lr);
                log.error("txt 文件表头异常:{} ",head);
                return false;
            }
        }
        return true;
    }

    public static String getBatchNumber(String apiCode) {
        String dateAddYyMmDdHhMmSs = DateHelper.getDateAddYyMmDdHhMmSs(0);
        int i = (int) ((Math.random()*9+1)*1000);
        return apiCode+"_"+dateAddYyMmDdHhMmSs+"_"+i;
    }
}
