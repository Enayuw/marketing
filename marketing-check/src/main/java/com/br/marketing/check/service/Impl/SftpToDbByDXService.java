package com.br.marketing.check.service.Impl;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.dto.FileContext;
import com.br.marketing.check.enums.ErrorFileTypeEnum;
import com.br.marketing.check.service.AbstractDataToDbService;
import com.br.marketing.check.thread.ValidatorSmallFileThread;
import com.br.marketing.check.utils.SftpToDbUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.*;
import com.br.marketing.common.utils.file.MyFileUtil;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.xml.ws.soap.Addressing;
import java.io.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Bairong
 * @Time: 2020/12/9 15:06
 * @Company：百融
 * @Description: 功能描述
 */
@Service
@Slf4j
public class SftpToDbByDXService {

    /**
     * The Load result mapper.
     */
    @Resource
    LoadResultMapper loadResultMapper;
    /**
     * The File ckeck servicce.
     */
    @Resource
    FileCheckServiceImpl fileCheckService;
    /**
     * The Redis chg service.
     */
    @Resource
    RedisChgService redisChgService;

    @Autowired
    LocalFileMapper localFileMapper;

    @Autowired
    PhoneSaleMapper phoneSaleMapper;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    private final static Integer SPLITSIZE=5000;

    public Boolean dowloadFile(FileContext context) {
        String localFilePath=context.getLocalTxtFilePath();
        String zipFileName=context.getTxtFileName();
        SftpClient client =(SftpClient)context.getBaseFtpClient();
        File dir=new File(localFilePath);
        if(!dir.exists()||!dir.isDirectory()){
            boolean mkdirs = dir.mkdirs();
            if(!mkdirs){
                log.error("创建文件夹失败-{}",context.getLocalZipFilePath());
                return false;
            }
        }
        StringBuilder sb=new StringBuilder().append(localFilePath).append(zipFileName);
        boolean download = client.downloadFile(context.getSftpZipFilePath() , zipFileName, sb.toString());
        if(!download){
            log.error("文件下载出错-SftpZipFilePath={},zipFileName={}",context.getSftpZipFilePath(),zipFileName);
            return false;
        }
        return true;
    }

    public Boolean actionTxtFile(FileContext context,LocalFile localFile) {
        StringBuilder errorMessage=new StringBuilder("数据文件异常,");
        if(!fileCheckService.checkTxtfile(context,errorMessage)){
            return false;
        }
        String txtFilePathAndName=context.getLocalTxtFilePath().concat(context.getTxtFileName());
        StringBuilder head;
        int totalLines = MyFileUtil.getTotalLines(new File(txtFilePathAndName));
        if(totalLines==0){
            log.error(String.format("%s 文件内容为空",context.getTxtFileName()));
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("4");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            return false;
        }
        head=MyFileUtil.gethead(txtFilePathAndName);

        HashMap<Integer, String> address = new HashMap<>();
        HashMap<Integer, String> extSetField = new HashMap<>();
        Result hashMapResult = SftpToDbUtils.statisticsHead(head.toString(),address,extSetField);
        if(!ResultCode.SUCCESS.getValue().equals(hashMapResult.getCode())){
            log.error(String.format("%s 文件：%s",context.getTxtFileName(),hashMapResult.getMessage()));
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("2");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
            return false;
        }

        long start = System.currentTimeMillis();

        String filepath = context.getLocalTxtFilePath().concat(context.getTxtFileName());
        AtomicInteger errorMark = new AtomicInteger(0);
        try(
                FileReader read = new FileReader(filepath);
                BufferedReader br = new BufferedReader(read);) {
            String row;
            Integer line = 1;
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
            while ((row = br.readLine()) != null) {
                String trim = row.trim();
                if(StringUtils.isNotEmpty(row)&&StringUtils.isNotEmpty(trim)){
                    if(line>1){
                        Integer lineNum = line;
                        threadPool.submit(()->{
                            PhoneSale phoneSale = new PhoneSale();
                            phoneSale.setApiCode(localFile.getApiCode());
                            phoneSale.setLocalId(localFile.getId().toString());
                            setDataByPhone(trim,phoneSale,address,extSetField,errorMark);
                        });
                    }
                }
                line++;
            }
            /**
             * 等待所有任务都执行完成
             **/
            threadPool.shutdown();
            while (true){
                if(threadPool.isTerminated()){
                    log.info("所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(3000);
                }catch (Exception e){
                }
            }
        if(errorMark.get()>0){
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setComplete("3");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
        }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        long end = System.currentTimeMillis();
        if(log.isWarnEnabled()){
            log.warn(String.format("数据入库时长:%d",end-start));
        }
        return true;
    }


    public void checkConfigFile(FileContext context) {
        MarketingTask task =context.getTask();
        String configFilePathAndName=context.getLocalTxtFilePath().concat(context.getConfigFileName());
            File configFile= new File(configFilePathAndName);
            if(configFile.exists()&&configFile.isFile()){

                Map<String,String> configMap = new HashedMap();
                try(FileReader read = new FileReader(configFilePathAndName);
                    BufferedReader br = new BufferedReader(read)){
                    String row;
                    while ((row = br.readLine()) != null) {
                        String trim = row.trim();
                        if(StringUtils.isNotEmpty(trim)){
                            String[] split = trim.split("=");
                            if(split.length>=2){
                                configMap.put(split[0],split[1]);
                            }
                        }
                    }
                    String dataVolume=configMap.get("dataVolume");
                    if(StringUtils.isNotEmpty(dataVolume)){
                        try{
                            int count = Integer.parseInt(dataVolume);
                            task.setDataVolume(count);
                        }catch (Exception e){
                            log.error("dataVolume error",e);
                        }
                    }
                    log.warn("{}，内容为{}",context.getConfigFileName(),configMap);
                    if(task.getMonitorType()==1){
                        if(StringUtils.isNotEmpty(configMap.get("strategyId"))&&fileCheckService.checkConfig("strategyId", configMap.get("strategyId"), task.getApiCode(), "")){
                            task.setStrategyId(configMap.get("strategyId"));
                            task.setFrequency(0+"");
                            task.setCloseDate(DateHelper.getDateAdd(2));
                            task.setStartDate(DateHelper.getDateAdd(0));
                        }else {
                            task.setMonitorStatus(3);
                            task.setStatus(1);
                            task.setErrorMessage("配置文件异常,策略编号异常");
                            fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
                            return;
                        }
                    }else if(task.getMonitorType()==2||task.getMonitorType()==3||task.getMonitorType()==4){
                        if(StringUtils.isNotEmpty(configMap.get("strategyId"))&&fileCheckService.checkConfig("strategyId", configMap.get("strategyId"), task.getApiCode(), "")){
                            task.setStrategyId(configMap.get("strategyId"));
                        }else {
                            task.setMonitorStatus(3);
                            task.setStatus(1);
                            task.setErrorMessage("配置文件异常,策略编号异常");
                            fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
                            return;
                        }
                        if(StringUtils.isNotEmpty(configMap.get("monitorFrequency"))&&fileCheckService.checkConfig("monitorFrequency", configMap.get("monitorFrequency"), task.getApiCode(), "")){
                            task.setFrequency(configMap.get("monitorFrequency"));
                        }else {
                            task.setMonitorStatus(3);
                            task.setStatus(1);
                            task.setErrorMessage("配置文件异常,监控周期异常");
                            fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
                            return;
                        }
                        if(StringUtils.isNotEmpty(configMap.get("monitorStartTime"))&&fileCheckService.checkConfig("monitorStartTime", configMap.get("monitorStartTime"), task.getApiCode(), "")){
                            task.setStartDate(configMap.get("monitorStartTime"));
                        }else {
                            task.setMonitorStatus(3);
                            task.setStatus(1);
                            task.setErrorMessage("配置文件异常,监控开始日期异常");
                            fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
                            return;
                        }
                        if(StringUtils.isNotEmpty(configMap.get("monitorStartTime"))&&fileCheckService.checkConfig("monitorendTime", configMap.get("monitorendTime"), task.getApiCode(), configMap.get("monitorStartTime"))){
                            task.setCloseDate(configMap.get("monitorStartTime"));
                        }else {
                            task.setMonitorStatus(3);
                            task.setStatus(1);
                            task.setErrorMessage("配置文件异常,监控截止日期异常");
                            fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
                            return;
                        }
                    }else{
                        task.setMonitorStatus(3);
                        task.setStatus(1);
                        task.setErrorMessage("监控模式异常");
                        fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
                        return;
                    }

                } catch (FileNotFoundException e) {
                    log.error("FileNotFoundException",e);
                } catch (IOException e) {
                    log.error("IOException",e);
                }
                LoadResult lr=new LoadResult();
                lr.setApiCode(task.getApiCode());
                lr.setFileName(context.getConfigFileName());
                lr.setBatchNumber(task.getBatchNumber());
                lr.setStatus("1");
                loadResultMapper.insertLoadResult(lr);
                task.setMonitorStatus(1);
            }
        task.setStatus(1);
    }


    private Result setDataByPhone(String row,PhoneSale phoneSale,HashMap<Integer,String> address,HashMap<Integer,String> extSetFields,AtomicInteger errorMark){
        List<String> datas = Splitter.on(",").splitToList(row);
        JSONObject jo = null;
        String error = "uid不能为空;phone不能为空;orgName不能为空;source不能为空;userType不能为空;type不能为空;customName不能为空;";
        for (int i = 0; i < datas.size(); i++) {
            String sureaddress = address.get(i);
            switch (sureaddress){
                case "uid":
                    if(StringUtils.isNotBlank(datas.get(i))){
                        error.replace("uid不能为空;","");
                    }
                    phoneSale.setUid(datas.get(i));
                    break;
                case "phone":
                    if(StringUtils.isNotBlank(datas.get(i))){
                        error.replace("phone不能为空;","");
                        phoneSale.setPhone(datas.get(i));
                        phoneSale.setPhoneAes(AESUtil.aesEncrypty(datas.get(i),aesKey));
                    }
                    break;
                case "name":
                    phoneSale.setName(datas.get(i));
                    if(StringUtils.isNotBlank(datas.get(i))){
                        phoneSale.setNameAes(DigestUtils.md5DigestAsHex(datas.get(i).getBytes()));
                    }
                    break;
                case "cid":
                    phoneSale.setCid(datas.get(i));
                    break;
                case "sex":
                    phoneSale.setSex(datas.get(i));
                    break;
                case "score":
                    phoneSale.setScore(datas.get(i));
                    break;
                case "riskScore":
                    phoneSale.setRiskScore(datas.get(i));
                    break;
                case "orgName":
                    if(StringUtils.isNotBlank(datas.get(i))){
                        error.replace("orgName不能为空;","");
                    }
                    phoneSale.setOrgName(datas.get(i));
                    break;
                case "source":
                    if(StringUtils.isNotBlank(datas.get(i))){
                        error.replace("source不能为空;","");
                    }
                    phoneSale.setSource(datas.get(i));
                    break;
                case "userType":
                    if(StringUtils.isNotBlank(datas.get(i))){
                        error.replace("userType不能为空;","");
                    }
                    phoneSale.setUserType(datas.get(i));
                    break;
                case "type":
                    if(StringUtils.isNotBlank(datas.get(i))){
                        error.replace("type不能为空;","");
                    }
                    phoneSale.setType(datas.get(i));
                    break;
                case "customName":
                    if(StringUtils.isNotBlank(datas.get(i))){
                        error.replace("customName不能为空;","");
                    }
                    phoneSale.setCustomName(datas.get(i));
                    break;
                case "ifRegister":
                    phoneSale.setIfRegister(datas.get(i));
                    break;
                case "registerTime":
                    phoneSale.setRegisterTime(datas.get(i));
                    break;
                case "ifLogin":
                    phoneSale.setIfLogin(datas.get(i));
                    break;
                case "loginTime":
                    phoneSale.setLoginTime(datas.get(i));
                    break;
                case "ifApply":
                    phoneSale.setIfApply(datas.get(i));
                    break;
                case "applyDt":
                    phoneSale.setApplyDt(datas.get(i));
                    break;
                case "applyTime":
                    phoneSale.setApplyTime(datas.get(i));
                    break;
                case "applyResult":
                    phoneSale.setApplyResult(datas.get(i));
                    break;
                case "refuseTime":
                    phoneSale.setRefuseTime(datas.get(i));
                    break;
                case "auditTime":
                    phoneSale.setAuditTime(datas.get(i));
                    break;
                case "auditAmount":
                    phoneSale.setAuditAmount(datas.get(i));
                    break;
                case "ifLent":
                    phoneSale.setIfLent(datas.get(i));
                    break;
                case "lentTime":
                    phoneSale.setLentTime(datas.get(i));
                    break;
                case "lentAmount":
                    phoneSale.setLentAmount(datas.get(i));
                    break;
                case "unlentAmount":
                    phoneSale.setUnlentAmount(datas.get(i));
                    break;
                case "ifSettle":
                    phoneSale.setIfSettle(datas.get(i));
                    break;
                case "settleTime":
                    phoneSale.setSettleTime(datas.get(i));
                    break;
                case "activity":
                    phoneSale.setActivity(datas.get(i));
                    break;
                case "day":
                    phoneSale.setDay(datas.get(i));
                    break;
                case "extraSet":
                    String s = extSetFields.get(i);
                    if(StringUtils.isNotBlank(s)){
                        if(jo==null){
                            jo = new JSONObject();
                        }
                        jo.put(s,datas.get(i));
                    }
                    break;
            }
            if(jo !=null){
                phoneSale.setExtraSet(jo.toJSONString());
            }
        }
        if(datas.size() != address.size()){
            phoneSale.setStatus(2);
            phoneSale.setDataMessage("表头和该行数据不一致");
            errorMark.getAndIncrement();
        }
        if(!StringUtils.isEmpty(error)){
            phoneSale.setStatus(2);
            phoneSale.setDataMessage(error);
            errorMark.getAndIncrement();
        }
        phoneSaleMapper.insertSelective(phoneSale);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 处理三要素校验失败的内容
     * @param context 参数对象
     */
    private void dealErrorResultFile(FileContext context) {
        /**
         * 处理解密校验失败的三要素
         */
        String errorFilePathAndName=context.getErrorFilePath().concat(context.getErrorDataFileName());
        SftpClient client=(SftpClient)context.getBaseFtpClient();
        int erroNUm = MyFileUtil.getTotalLines(new File(errorFilePathAndName));
        if(erroNUm>1){
            File errorresultFile=new File(errorFilePathAndName);
            if(errorresultFile.isFile()){
                try {
                    String sftpInErrorPath=Constants.SFTP_IN_ERROR_PATH.replace("apiCode",context.getTask().getApiCode());
                    client.uploadFile(sftpInErrorPath,context.getErrorDataFileName(),errorFilePathAndName);
                    File successFile=new File(errorFilePathAndName+".success");
                    successFile.createNewFile();
                    if(successFile.exists()){
                        client.uploadFile(sftpInErrorPath,context.getErrorDataFileName()+".success",errorFilePathAndName+".success");
                    }
                } catch (Exception e) {
                    log.error("上传错误文件到sftp出错",e);
                }
            }
        }
    }
}
