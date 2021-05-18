package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.FileCkeckServicce;
import com.br.marketing.check.service.SftpToDbService;
import com.br.marketing.check.thread.ValidatorDeleteMonitorFileThread;
import com.br.marketing.check.utils.SftpToDbUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.*;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.common.utils.file.ZipUtil;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.mapper.MarketingDirtyUserMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.service.EmailService;
import com.br.marketing.service.Impl.StrategyCs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

/**
 * @Author: Bairong
 * @Time: 2020/12/9 15:06
 * @Company：百融
 * @Description: 功能描述
 */
@Service
@Slf4j
public class SftpToDbServiceImpl  implements SftpToDbService {
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
    MarketingDirtyUserMapper marketingDirtyUserMapper;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    MarketingUserMapper marketingUserMapper;
    @Resource
    EmailService validDataAlarmServiceImpl;
    private static final Pattern MYREGEX = Pattern.compile("\\.");
    private final static Integer SPLITSIZE=5000;
    private final static Pattern FREQUENCY= Pattern.compile("^[0-5]{1}$");

    private final static Integer BATCHSIZE=2000;
    private static String MYREGEX1="\\p{C}";
    private Calendar calendar =Calendar.getInstance();
    @Override
    public void parsingFile(String key, String fileName, String localFilePath, String apiCode, MerchantParam merchantParam, String cusBatch,
                            String batchNumber, MarketingTask lt, SftpClient sftpClient) {
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

        boolean download = sftpClient.downloadFile(key , fileName, sb.toString());
        if(!download){
            log.error("download error:{}",key + fileName);
            return;
        }
        File file=new File(sb.toString());
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
            SftpToDbUtils.returnErrorFile(apiCode,localFilePath, fileName, errorMessage,sftpClient);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0",batchNumber,0,0,"");
            loadResultMapper.insertLoadResult(lr);
            return;
        }

        String txtFileName=fileName.replace(".zip",".txt");
        StringBuilder errorMessage=new StringBuilder("数据文件异常,");
        if(!SftpToDbUtils.checkTxtfile(fileName,apiCode,localFilePath+"/"+name,txtFileName,
                errorMessage,cusBatch,loadResultMapper,batchNumber,sftpClient)){
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
        if(!SftpToDbUtils.checkTxtContent(totalLines,localFilePath+"/"+name,
                errorMessage,apiCode,fileName,head.toString(),cusBatch,loadResultMapper,batchNumber,sftpClient,merchantParam)){
            return;
        }
        File[] files = MyFileUtil.splitFileByCusNum(localFilePath+"/"+name+"/"+txtFileName,hash);
        MyFileUtil.distinctByCusNum(files,localFilePath+"/"+name+"/"+"result-"+txtFileName,hash,head);
        log.info("================去重结束============");


        /**
         * 360有可能是一个批次多个数据文件，这块代码主要是记录当前批次一共实际上传了多少数据量
         */
        if(totalLines>1&& StringUtils.isNotEmpty(head)){
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
        dealErrorResultFile(localFilePath, apiCode,  name,batchNumber,sftpClient);
        parseConfigFile(lt, localFilePath+"/"+name+"/", txtFileName,apiCode,batchNumber, sftpClient);
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

    @Override
    public void parsingDeleteFile(String key, String fileName, String localFilePath, String apiCode, MerchantParam merchantParam, String cusBatch, SftpClient sftpClient) {
        log.info("parsingDeleteFile key：{} ,fileName:{},localFilePath:{},apiCode:{}",key,fileName,localFilePath,apiCode);
        ExecutorService validatorExecutor = BrExecutors.getThreadPool(5,5);
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

        sftpClient.downloadFile(key , fileName, sb.toString());
        File file=new File(sb.toString());
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
            SftpToDbUtils.returnDeleteErrorFile(apiCode,localFilePath, fileName, errorMessage,sftpClient);
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,errorMessage.toString(),"0","",0,0,"delete");
            loadResultMapper.insertLoadResult(lr);
            return;
        }

        String txtFileName=fileName.replace(".zip",".txt");
        StringBuilder errorMessage=new StringBuilder("数据文件异常,");
        if(!SftpToDbUtils.checkDeleteTxtfile(fileName,apiCode,localFilePath+"/"+name,txtFileName,errorMessage,cusBatch,sftpClient,loadResultMapper)){
            return;
        }
        String deleteErrorFileName=apiCode+"_"+txtFileName.split("\\.")[0]+"_error_"+ DateHelper.getDateAddYyMmDd(0)+".txt";
        StringBuilder errorSb=new StringBuilder();
        errorSb.append(localFilePath).append("/").append(name).append("/")
                .append(deleteErrorFileName);
        File errorFile=new File(errorSb.toString());
        StringBuilder head=new StringBuilder();
        log.info("================开始去重============");
        int hash=0;
        int totalLines = MyFileUtil.getTotalLines(new File(localFilePath + "/" + name + "/" + txtFileName));
        hash= (totalLines + SPLITSIZE-1) /SPLITSIZE;
        File[] files = MyFileUtil.splitFile(localFilePath+"/"+name+"/"+txtFileName,hash,head);
        MyFileUtil.distinct(files,localFilePath+"/"+name+"/"+"result-"+txtFileName,hash, head);
        log.info("================去重结束============");
        try (Writer fw= new BufferedWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(Paths.get(errorSb.toString())), StandardCharsets.UTF_8));
             FileReader read = new FileReader(localFilePath+"/"+name+"/"+"result-"+txtFileName);
             BufferedReader br = new BufferedReader(read);) {
            Map<String,Integer> headIndexMap=new HashMap();
            if(StringUtils.isNotEmpty(head)){
                String[] headArray = head.toString().split(",");
                headIndexMap.put("batchNumberIndex",findIndex(headArray, "batch_number"));
                headIndexMap.put("cusNumIndex",findIndex(headArray, "cus_num"));
                headIndexMap.put("idIndex",findIndex(headArray, "id"));
                headIndexMap.put("cellIndex",findIndex(headArray, "cell"));
                headIndexMap.put("nameIndex",findIndex(headArray, "name"));
                fw.append("error_message,"+head+"\n");
            }
            String row;
            int linenumber = 0;
            int num = 1;
            Set<String> list=new HashSet<>();
            while ((row = br.readLine()) != null) {
                log.debug("row:{}",row);
                String trim = row.trim();
                trim=trim.replaceAll(MYREGEX1, "");
                if(StringUtils.isNotEmpty(trim)){
                    linenumber++;
                    if((linenumber / BATCHSIZE) > (num - 1)){
                        num ++ ;
                        validatorExecutor.submit(new ValidatorDeleteMonitorFileThread(list, apiCode, marketingDirtyUserMapper
                                , merchantParam, headIndexMap, fw,redisChgService,name));
                        list=new HashSet<>();
                    }
                    list.add(trim);
                }
            }


            if(!list.isEmpty()){
                validatorExecutor.submit(new ValidatorDeleteMonitorFileThread(list,apiCode, marketingDirtyUserMapper
                        ,merchantParam,headIndexMap,fw,redisChgService,name));

            }

            if(!SftpToDbUtils.checkDeleteTxtContent(linenumber+1,localFilePath+"/"+name,errorMessage,apiCode,fileName,head.toString(),cusBatch,sftpClient,loadResultMapper)){
                return;
            }
            validatorExecutor.shutdown();
            while (true){
                if(validatorExecutor.isTerminated()){
                    log.info("所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(3000);
                }catch (Exception e){
                    log.error("等待所有任务都执行完成",e);
                }
            }
            fw.close();
            String s2 = name.toUpperCase();
            String s = redisChgService.get(Constants.DELETE_MONITOR_ERROR + s2);
            String s1 = redisChgService.get(Constants.DELETE_MONITOR_SUCCESS + s2);
            if(StringUtils.isNotEmpty(s)&&Integer.parseInt(s)>0){
                log.warn("匹配出错条数：{}",s);
                sftpClient.uploadFile("/UploadFiles/loanwarn/"+apiCode+"/error/",deleteErrorFileName,errorSb.toString());
                File successFile=new File( errorSb.toString()+".success");
                successFile.createNewFile();
                if(successFile.exists()){
                    sftpClient.uploadFile("/UploadFiles/loanwarn/"+apiCode+"/error/",deleteErrorFileName+".success",errorSb.toString()+".success");
                }
                redisChgService.del(Constants.DELETE_MONITOR_ERROR + s2);
            }
            int actualNum=0;
            if(StringUtils.isNotEmpty(s1)){
                actualNum=Integer.parseInt(s1);
            }

            String sftpPath = "/UploadFiles/loanwarn/" + apiCode + "/input/";
            sftpClient.rename(sftpPath+fileName,sftpPath+fileName+".bak");
            LoadResult lr=new LoadResult(apiCode,cusBatch,fileName,"","1","",actualNum,linenumber,"delete");
            log.info("LoadResult :{}",lr);
            loadResultMapper.insertLoadResult(lr);

            List<MarketingTask> marketingTaskList = marketingTaskMapper.queryMonitorBatch(apiCode);
            log.info("loanTaskList:{},apiCode:{}", marketingTaskList.size(),apiCode);
            for(MarketingTask lt: marketingTaskList){
                lt.setTableName("b_marketing_user_"+apiCode);
                Integer integer = marketingUserMapper.queryCount(lt);
               // log.info("batch_number:{},actualnumber:{},count:{}",lt.getBatchNumber(),lt.getActualNumber(),integer);
                if(!integer.equals(lt.getActualNumber())){
                    lt.setActualNumber(integer);
                    marketingTaskMapper.updateTaskActualNumber(lt);
                }
            }
            redisChgService.del(Constants.DELETE_MONITOR_SUCCESS + s2);
        } catch (Exception e) {
            log.error("parsingFile error",e);
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
    private void parseConfigFile(MarketingTask lt, String path, String fileName, String apiCode, String batchNumber, SftpClient sftpClient){
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
                try(FileReader read = new FileReader(path+configFileName);
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
                                if(split.length>=2){
                                    strategyId = split[1];
                                }
                            }else if(trim.indexOf("monitorFrequency")>-1){
                                String[] split = trim.split("=");
                                if(split.length>=2){
                                    monitorFrequency = split[1];
                                }
                            }else if(trim.indexOf("monitorStartTime")>-1){
                                String[] split = trim.split("=");
                                if(split.length>=2){
                                    monitorStartTime = split[1];
                                }
                            }else if(trim.indexOf("monitorendTime")>-1){
                                String[] split = trim.split("=");
                                if(split.length>=2){
                                    monitorendTime = split[1];
                                }
                            }else if(trim.indexOf("dataVolume")>-1){
                                String[] split = trim.split("=");
                                if(split.length>=2){
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
                            if(Constants.APICODE_SHAZI.contains(apiCode)){
                                lt.setCloseDate(DateHelper.getDateAdd(-1));
                                lt.setStartDate(DateHelper.getDateAdd(0));
                            }else {
                                lt.setCloseDate(DateHelper.getDateAdd(-2));
                                lt.setStartDate(DateHelper.getDateAdd(-1));
                            }

                        }else {
                            lt.setMonitorStatus(3);
                            lt.setStatus(1);
                            lt.setErrorMessage("配置文件异常,策略编号异常");
                            SftpToDbUtils.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),sftpClient);
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
                            SftpToDbUtils.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),sftpClient);
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
                            SftpToDbUtils.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),sftpClient);
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
                            SftpToDbUtils.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),sftpClient);
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
                            SftpToDbUtils.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),sftpClient);
                            lr.setMessage(lt.getErrorMessage());
                            lr.setStatus("0");
                            loadResultMapper.insertLoadResult(lr);
                            return;
                        }
                    }else{
                        lt.setMonitorStatus(3);
                        lt.setStatus(1);
                        lt.setErrorMessage("监控模式异常");
                        SftpToDbUtils.returnErrorFile(apiCode, path, configFileName, new StringBuilder(lt.getErrorMessage()),sftpClient);
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
            }
        }
        lt.setStatus(1);
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
        if(!Constants.APICODE_DAAS.contains(apiCode)&&!Constants.APICODE_DAAS_QA.contains(apiCode)){
            if(!dateFirst.before(dateLast)){
                return false;
            }
        }
//        if(apiCode.equals(Constants.APICODE_360_MARKET)||Constants.APICODE_360_MARKET_QA.contains(apiCode)){
//            if(dateLast.before(dateFirst)){
//                return false;
//            }
//        }else{
//            if(!dateFirst.before(dateLast)){
//                return false;
//            }
//        }

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
    private void dealErrorResultFile(String localFilePath, String apiCode, String name, String batchNumber,SftpClient sftpClient) {
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
                    sftpClient.uploadFile("/UploadFiles/loanwarn/"+apiCode+"/error/",errorFileName,errorFilePath);
                    File successFile=new File(errorFilePath+".success");
                    successFile.createNewFile();
                    if(successFile.exists()){
                        sftpClient.uploadFile("/UploadFiles/loanwarn/"+apiCode+"/error/",errorFileName+".success",errorFilePath+".success");
                    }
                } catch (Exception e) {
                    log.error("上传错误文件到ftp出错",e);
                }
            }
        }
    }

    /**
     * 查找某个值在数组中的索引
     * @param array 数组
     * @param value 给定的值
     * @return 索引
     */
    public static int findIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }
}
