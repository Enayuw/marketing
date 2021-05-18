package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.FileCkeckServicce;
import com.br.marketing.check.service.FileUploadService;
import com.br.marketing.check.utils.UploadDataFileUtil;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.FtpUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.common.utils.file.ZipUtil;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.service.EmailService;
import com.br.marketing.service.Impl.StrategyCs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 数据文件名称校验通过。从ftp下载文件到磁盘-->解压-->文件内容校验-->数据校验-->入库
 */
@Service
@Slf4j
public class DataFileUploadServiceImpl implements FileUploadService {
    /**
     * The Load result mapper.
     */
    @Resource
    LoadResultMapper loadResultMapper;
    /**
     * The File ckeck servicce.
     */
    @Resource
    FileCkeckServicce fileCkeckServicceImpl;
    /**
     * The Redis chg service.
     */
    @Resource
    RedisChgService redisChgService;
    /**
     * The Strategy cs.
     */
    @Resource
    StrategyCs strategyCs;

    @Resource
    EmailService validDataAlarmServiceImpl;
    private static final Pattern MYREGEX = Pattern.compile("\\.");
    private final static Integer SPLITSIZE=5000;
    private final static Pattern FREQUENCY= Pattern.compile("^[0-4]{1}$");
    private  Calendar calendar =Calendar.getInstance();
    @Override
    public void parsingFile(String key, String fileName, String localFilePath, String apiCode,
                            MerchantParam merchantParam, String cusBatch, String batchNumber, MarketingTask lt, FtpUtil ftpUtil) {
        log.info("DataFileUploadServiceImpl parsingFile key：{} ,fileName:{},localFilePath:{},apiCode:{}",key,fileName,localFilePath,apiCode);

        String[] split = MYREGEX.split(fileName);
        String name = split[0];
        File dir=new File(localFilePath);
        if(!dir.exists()||!dir.isDirectory()){
            boolean mkdirs = dir.mkdirs();
            if(!mkdirs){
                return;
            }
        }
        StringBuilder sb=new StringBuilder();
        sb.append(localFilePath)
                .append(fileName);
        File file=new File(sb.toString());
        boolean download = ftpUtil.download(key + fileName, file);
        if(!download){
            log.error("download error:{}",key + fileName);
            return;
        }
        try {
            if("0".equals(merchantParam.getFileEncryptionMethods())){
                ZipUtil.unZip(file,localFilePath+"/"+name);
            }else if("2".equals(merchantParam.getFileEncryptionMethods())){
                ZipUtils.unZip(file,localFilePath+"/"+name,merchantParam.getFileEncryptionKey());
            }
        }catch (Exception e){
            log.error("解压失败",e);
            StringBuilder errorMessage=new StringBuilder("压缩文件异常,");
            errorMessage.append("压缩文件解密异常");
            UploadDataFileUtil.returnErrorFile(apiCode,localFilePath, fileName, errorMessage,ftpUtil);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0",batchNumber,0,0,"");
            loadResultMapper.insertLoadResult(lr);
            return;
        }

        String txtFileName=fileName.replace(".zip",".txt");
        StringBuilder errorMessage=new StringBuilder("数据文件异常,");
        if(!UploadDataFileUtil.checkTxtfile(fileName,apiCode,localFilePath+"/"+name,txtFileName,
                errorMessage,cusBatch,loadResultMapper,batchNumber,ftpUtil)){
            return;
        }
        StringBuilder head=new StringBuilder();


        log.info("================开始去重============");
        int hash=0;
        int totalLines=0;
        try {
            totalLines = MyFileUtil.getTotalLines(new File(localFilePath + "/" + name + "/" + txtFileName));
            hash= (totalLines + SPLITSIZE-1) /SPLITSIZE;
            head=MyFileUtil.gethead(localFilePath + "/" + name + "/" + txtFileName);
        } catch (IOException e) {
            log.error("获取文件行数失败",e);
        }
        if(!UploadDataFileUtil.checkTxtContent(totalLines,localFilePath+"/"+name,
                errorMessage,apiCode,fileName,head.toString(),cusBatch,loadResultMapper,batchNumber,ftpUtil,merchantParam)){
            return;
        }
        File[] files = MyFileUtil.splitFileByCusNum(localFilePath+"/"+name+"/"+txtFileName,hash);
        MyFileUtil.distinctByCusNum(files,localFilePath+"/"+name+"/"+"result-"+txtFileName,hash,head);
        log.info("================去重结束============");


        if(totalLines>1&&StringUtils.isNotEmpty(head)){
            log.warn(" txtFileName:{} totalLines:{} head:{}",txtFileName,totalLines,head);
            String s = Constants.UPLOAD_DATA_NUM + batchNumber;
            String s1 = redisChgService.get(s);
            Integer value=StringUtils.isNotEmpty(s1)?((totalLines-1)+Integer.parseInt(s1)):(totalLines-1);
            redisChgService.setex(s,value.toString(),172800);
        }
        //文件重命名是为了复用之前shell的请求参数，要不shell那边也需要改
        File oldFile=new File(localFilePath+"/"+name+"/"+txtFileName);
        File newFile=new File(localFilePath+"/"+name+"/"+"old"+txtFileName);
        if (oldFile.exists() && oldFile.isFile()) {
            oldFile.renameTo(newFile);
        }

        File oldFile1=new File(localFilePath+"/"+name+"/"+"result-"+txtFileName);
        File newFile1=new File(localFilePath+"/"+name+"/"+txtFileName);
        if (oldFile1.exists() && oldFile1.isFile()) {
            oldFile1.renameTo(newFile1);
        }
        boolean b = fileCkeckServicceImpl.checkSmallDataFile(localFilePath+"/"+name+"/", txtFileName,false,batchNumber);
        if(!b){
            log.error("数据文件解密失败");
            return;
        }
        dealErrorResultFile(localFilePath, apiCode,  name,batchNumber,ftpUtil);
        parseConfigFile(lt, localFilePath+"/"+name+"/", txtFileName,apiCode,batchNumber, ftpUtil);
        log.info("parseConfigFile done");
        lt.setTableName("b_marketing_user_"+apiCode);
        String s = redisChgService.get(Constants.INSERT_DB_NUMBER + txtFileName);
        redisChgService.expire(Constants.INSERT_DB_NUMBER + txtFileName,172800);
        Integer actualNumber =StringUtils.isNotEmpty(s)?Integer.parseInt(s):0 ;
        LoadResult lr=new LoadResult();
        lr.setApiCode(apiCode);
        lr.setFileName(txtFileName);
        lr.setBatchNumber(batchNumber);
        lr.setStatus("1");
        lr.setActualNumber(actualNumber);
        lr.setTaskNumber(totalLines-1);
        loadResultMapper.insertLoadResult(lr);
        volidatorDataVolume(lt.getDataVolume(),totalLines-1,apiCode,txtFileName);
    }

    private void volidatorDataVolume(Integer dataVolume, int i,String apiCode,String fileName) {
        if(dataVolume==null){
            return;
        }
        if(dataVolume!=i){
            StringBuilder sb=new StringBuilder(fileName)
                    .append(",")
                    .append(dataVolume)
                    .append(",")
                    .append(i);
            validDataAlarmServiceImpl.dataFileVolumn(apiCode,sb.toString());
        }
    }

    /**
     * 解析客户上传的配置文件
     * @param lt 监控任务对象
     * @param path 配置文件路径
     * @param fileName 文件名称
     * @param apiCode 客户编号
     * @param batchNumber 批次号
     */
    private void parseConfigFile(MarketingTask lt, String path, String fileName, String apiCode, String batchNumber, FtpUtil ftpUtil){
        String dataVolume="";
        if((apiCode.equals(Constants.APICODE_PPD)||apiCode.equals(Constants.APICODE_PPD_QA))){
            lt.setStrategyId(Constants.STRATEGY_ID_PPD);
            lt.setFrequency(0+"");
            lt.setStartDate(DateHelper.getDateAdd(-1));
            lt.setCloseDate(Constants.CLOSE_DATE_PPD);
//            lt.setSecStrategyId(Constants.STRATEGY_ID_PPD_SEC);
            lt.setMonitorStatus(1);
        }else  if((apiCode.equals(Constants.APICODE_360)||apiCode.equals(Constants.APICODE_360_QA))){
            lt.setStrategyId(Constants.STRATEGY_ID_360);
            lt.setFrequency(0+"");
            lt.setStartDate(DateHelper.getDateAdd(-1));
            lt.setCloseDate(Constants.CLOSE_DATE_360);
            lt.setMonitorStatus(1);
        }else{
            String configFileName = fileName.replace(".txt", ".config");
            File configFile= new File(path+configFileName);
            log.info("path:{},configFileName:{}",path,configFileName);
            if(configFile.exists()&&configFile.isFile()){
                LoadResult lr=new LoadResult();
                lr.setApiCode(apiCode);
                lr.setFileName(configFileName);
                lr.setBatchNumber(batchNumber);
                try( FileReader read = new FileReader(path+configFileName);
                     BufferedReader br = new BufferedReader(read);){
                    String row;
                    String monitorStartTime="";
                    String strategyId="";
                    String monitorFrequency="";
                    String monitorendTime="";
                    while ((row = br.readLine()) != null) {
                        String trim = row.trim();
                        if(StringUtils.isNotEmpty(trim)){
                            if(trim.indexOf("strategyId")>-1){
                                String[] split = trim.split("=");
                                if(split.length>1){
                                    strategyId = split[1];
                                }
                            }else if(trim.indexOf("monitorFrequency")>-1){
                                String[] split = trim.split("=");
                                if(split.length>1){
                                    monitorFrequency = split[1];
                                }
                            }else if(trim.indexOf("monitorStartTime")>-1){
                                String[] split = trim.split("=");
                                if(split.length>1){
                                    monitorStartTime = split[1];
                                }
                            }else if(trim.indexOf("monitorendTime")>-1){
                                String[] split = trim.split("=");
                                if(split.length>1){
                                    monitorendTime = split[1];
                                }
                            }else if(trim.indexOf("dataVolume")>-1){
                                String[] split = trim.split("=");
                                if(split.length>1){
                                    dataVolume = split[1];
                                }
                                if(StringUtils.isNotEmpty(dataVolume)){
                                    try{
                                        int i = Integer.parseInt(dataVolume);
                                        lt.setDataVolume(i);
                                    }catch (Exception e){
                                        log.error("dataVolume error",e);
                                        log.warn("dataVolume error:{}",trim);
                                    }
                                }
                            }
                        }
                    }
                    log.warn("strategyId:{},monitorFrequency:{},monitorStartTime:{},monitorendTime:{}",
                            strategyId,monitorFrequency,monitorStartTime,monitorendTime);
                    if(lt.getMonitorType()==1){
                        if(checkConfig("strategyId", strategyId, apiCode, "")){
                            lt.setStrategyId(strategyId);
                            lt.setFrequency(0+"");
                            if(apiCode.equals(Constants.APICODE_360_MARKET)||Constants.APICODE_360_MARKET_QA.contains(apiCode)){
                                lt.setCloseDate(DateHelper.getDateAdd(0));
                                lt.setStartDate(DateHelper.getDateAdd(0));
                            }else {
                                lt.setCloseDate(DateHelper.getDateAdd(-2));
                                lt.setStartDate(DateHelper.getDateAdd(-1));
                            }
                        }else {
                            lt.setMonitorStatus(3);
                            lt.setStatus(1);
                            lt.setErrorMessage("配置文件异常,策略编号异常");
                            UploadDataFileUtil.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),ftpUtil);
                            lr.setMessage(lt.getErrorMessage());
                            lr.setStatus("0");
                            loadResultMapper.insertLoadResult(lr);
                            return;
                        }
                    }else if(lt.getMonitorType()==2||lt.getMonitorType()==3||lt.getMonitorType()==4){
                        if(checkConfig("strategyId", strategyId, apiCode, "")){
                            lt.setStrategyId(strategyId);
                        }else {
                            lt.setMonitorStatus(3);
                            lt.setStatus(1);
                            lt.setErrorMessage("配置文件异常,策略编号异常");
                            UploadDataFileUtil.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),ftpUtil);
                            lr.setMessage(lt.getErrorMessage());
                            lr.setStatus("0");
                            loadResultMapper.insertLoadResult(lr);
                            return;
                        }

                        if(checkConfig("monitorFrequency", monitorFrequency, apiCode, "")){
                            lt.setFrequency(monitorFrequency);
                        }else {
                            lt.setMonitorStatus(3);
                            lt.setStatus(1);
                            lt.setErrorMessage("配置文件异常,监控周期异常");
                            UploadDataFileUtil.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),ftpUtil);
                            lr.setMessage(lt.getErrorMessage());
                            lr.setStatus("0");
                            loadResultMapper.insertLoadResult(lr);
                            return;
                        }
                        if(checkConfig("monitorStartTime", monitorStartTime, apiCode, "")){
                            lt.setStartDate(monitorStartTime);
                        }else {
                            lt.setMonitorStatus(3);
                            lt.setStatus(1);
                            lt.setErrorMessage("配置文件异常,监控开始日期异常");
                            UploadDataFileUtil.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),ftpUtil);
                            lr.setMessage(lt.getErrorMessage());
                            lr.setStatus("0");
                            loadResultMapper.insertLoadResult(lr);
                            return;
                        }
                        if(checkConfig("monitorendTime", monitorendTime, apiCode, monitorStartTime)){
                            lt.setCloseDate(monitorendTime);
                        }else {
                            lt.setMonitorStatus(3);
                            lt.setStatus(1);
                            lt.setErrorMessage("配置文件异常,监控截止日期异常");
                            UploadDataFileUtil.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),ftpUtil);
                            lr.setMessage(lt.getErrorMessage());
                            lr.setStatus("0");
                            loadResultMapper.insertLoadResult(lr);
                            return;
                        }
                    }else{
                        lt.setMonitorStatus(3);
                        lt.setStatus(1);
                        lt.setErrorMessage("监控模式异常");
                        UploadDataFileUtil.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),ftpUtil);
                        lr.setMessage(lt.getErrorMessage());
                        lr.setStatus("0");
                        loadResultMapper.insertLoadResult(lr);
                        return;
                    }

                } catch (FileNotFoundException e) {
                    log.error("FileNotFoundException",e);
                } catch (IOException e) {
                    log.error("IOException",e);
                }
                if(lt.getMonitorStatus()!=3){
                    lr.setStatus("1");
                }
                loadResultMapper.insertLoadResult(lr);
                lt.setMonitorStatus(1);
            }else{
                if(lt.getMonitorType()==1){
                    if(apiCode.equals(Constants.APICODE_360_MARKET)||Constants.APICODE_360_MARKET_QA.contains(apiCode)){
                        lt.setCloseDate(DateHelper.getDateAdd(0));
                        lt.setStartDate(DateHelper.getDateAdd(0));
                    }
                }
            }
        }
        lt.setStatus(1);
        if(apiCode.equals(Constants.APICODE_360_MARKET)||Constants.APICODE_360_MARKET_QA.contains(apiCode)){
            lt.setIsRepair("1");
        }
    }

    /**
     * 配置内容检验
     * @param field 字段
     * @param value 字段值
     * @param apiCode apiCode
     * @param value1 字段值
     * @return
     */
    private boolean checkConfig(String field,String value,String apiCode,String value1){
        boolean flag=true;

        switch (field) {
            case "strategyId":
                String s = strategyCs.strategyIdCheck(apiCode, value);
                if(StringUtils.isEmpty(s)){
                    flag=false;
                }
                break;
            case "monitorFrequency":
                if(!FREQUENCY.matcher(value).matches()){
                    flag=false;
                }
                break;
            case "monitorStartTime":
                try{
                    flag=isVaildMonitorStartTime(value,apiCode);
                }catch (Exception e){
                    flag=false;
                }
                break;
            case "monitorendTime":
                try{
                    flag=isVaildMonitorendTime(value1,value);
                }catch (Exception e){
                    flag=false;
                }
                break;
            default:
        }
        return flag;
    }

    private boolean isVaildMonitorStartTime(String value,String apiCode) throws ParseException {
           SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateFirst = new Date();
            Date dateLast = dateFormat.parse(value);
            if(apiCode.equals(Constants.APICODE_360_MARKET)||apiCode.equals(Constants.APICODE_360_MARKET_QA)){
                if(dateLast.before(dateFirst)){
                    return false;
                }
            }else{
                if(!dateFirst.before(dateLast)){
                    return false;
                }
            }

        return true;
    }
    private boolean isVaildMonitorendTime(String value1,String value) throws ParseException {
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
            Date dateFirst = dateFormat1.parse(value1);
            Date dateLast = dateFormat1.parse(value);
            if(!dateFirst.before(dateLast)){
                return false;
            }

            calendar.setTime(dateFirst);
            //把日期往后增加一年.整数往后推,负数往前移动
            calendar.add(1, 1);
            Date date = calendar.getTime();
            if(!dateLast.before(date)){
                return false;
            }
            return true;
    }
    /**
     * 处理三要素校验失败的内容
     * @param localFilePath 错误文件路径
     * @param apiCode 客户编号
     * @param name 文件名称
     * @param batchNumber 批次号
     */
    private void dealErrorResultFile(String localFilePath, String apiCode, String name, String batchNumber,FtpUtil ftpUtil) {
        /**
         * 处理解密校验失败的三要素
         */
        String[] s = name.split("_");
        String errorFileName="";
        if(org.apache.commons.lang.StringUtils.isNotEmpty(apiCode)&&
                (apiCode.equals(Constants.APICODE_360)||apiCode.equals(Constants.APICODE_360_QA))){
            errorFileName=s[0]+"_"+s[1]+"_"+s[2]+"_error_"+s[3]+".txt";
        }else{
            errorFileName=s[0]+"_"+s[1]+"_"+"error_"+s[2]+".txt";
        }
        String errorFilePath=localFilePath+"/"+name+"/error/"+errorFileName;
        int erroNUm=0;
            erroNUm = MyFileUtil.getTotalLines(new File(errorFilePath));
        if(erroNUm>1){
            String s2 = Constants.UPLOAD_FAILDATA_NUM + batchNumber;
            String s1 = redisChgService.get(s2);
            Integer value=StringUtils.isNotEmpty(s1)?(erroNUm+Integer.parseInt(s1)):erroNUm;
            redisChgService.setex(s2,value.toString(),172800);

            File errorresultFile=new File(errorFilePath);

            if(errorresultFile.isFile()){
                try {
                    ftpUtil.changeWorkingDirectory("/loanwarn/"+apiCode+"/error/");
                    ftpUtil.upload("/loanwarn/"+apiCode+"/error/"+errorFileName,errorresultFile);
                    File successFile=new File(errorFilePath+".success");
                    successFile.createNewFile();
                    if(successFile.exists()){
                        ftpUtil.upload("/loanwarn/"+apiCode+"/error/"+errorFileName+".success",successFile);
                    }
                } catch (Exception e) {
                    log.error("上传错误文件到ftp出错",e);
                }
            }
        }
    }
}
