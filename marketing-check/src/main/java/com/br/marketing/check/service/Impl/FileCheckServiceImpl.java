package com.br.marketing.check.service.Impl;

import com.br.marketing.check.dto.FileContext;
import com.br.marketing.check.enums.ErrorFileTypeEnum;
import com.br.marketing.check.service.FileCheckService;
import com.br.marketing.check.thread.ValidatorSmallFileThread;
import com.br.marketing.check.thread.ValidatorThread;
import com.br.marketing.check.utils.SftpToDbUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.Customer;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.service.EmailService;
import com.br.marketing.service.Impl.StrategyCs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Bairong on 2020/1/15.
 */
@Service
@Slf4j
public class FileCheckServiceImpl implements FileCheckService {

    @Resource
    DecodeGrpcClient decodeClient;
    @Resource
    StrategyCs strategyCs;
    @Resource
    private LoadResultMapper loadResultMapper;
    @Resource
    EmailService validDataAlarmServiceImpl;

    @Autowired
    RedisChgService redisChgService;

    final static String DB_POOL_KEY = "DB:Pool:Num";

    final static String DB_POOL_QUEUE_KEY = "DB:Pool:Num:Queue:Num";

    final static String DB_POOL_CHECK_OPEN = "DB:Pool:checkopen";

    @Resource
    CustomerMapper customerMapper;
    private Calendar calendar =Calendar.getInstance();
    private final static Integer SPLITNUM=5000;
    @Override
    public boolean strategyIdCheck(String apiCode, String strategyId) {
        return StringUtils.isEmpty(strategyCs.strategyIdCheck(apiCode,strategyId))?false:true;
    }

    @Override
    public boolean checkSmallDataFile(FileContext context) {
        long l = System.currentTimeMillis();
        Customer customer = customerMapper.getCustomerByApiCode(context.getApiCode());
        String s = redisChgService.get(DB_POOL_KEY);
        Integer dbPoolNum = StringUtils.isNotBlank(s)?Integer.valueOf(s):40;
        String s1 = redisChgService.get(DB_POOL_QUEUE_KEY);
        Integer dbPoolQueueNum = StringUtils.isNotBlank(s1)?Integer.valueOf(s1):200;
        String s2 = redisChgService.get(DB_POOL_CHECK_OPEN);
        Integer dbPoolCheckOpenMark = StringUtils.isNotBlank(s2)?Integer.valueOf(s2):1;
        ExecutorService validatorExecutor = BrExecutors.getThreadPool(dbPoolNum,dbPoolNum,dbPoolQueueNum);
        File errorPathFile=new File(context.getErrorFilePath());
        if(!errorPathFile.exists()){
            errorPathFile.mkdirs();
        }
        File file1 = new File(context.getErrorFilePath().concat(context.getErrorDataFileName()));
        AtomicLong desTime = new AtomicLong();
        try(Writer errorfw = new BufferedWriter(
                new OutputStreamWriter(
                new FileOutputStream(file1), "UTF-8"));
            FileReader read = new FileReader(context.getDistinctTxtFilePath().concat(context.getDistinctTxtFileName()));
            BufferedReader br = new BufferedReader(read);) {
            String row;
            String head="";
            while ((row = br.readLine()) != null) {
                String trim = row.trim();
                if(StringUtils.isNotEmpty(row)&&StringUtils.isNotEmpty(trim)){
                    if(row.indexOf("cus_num")!=-1&&(row.indexOf("id")!=-1||row.indexOf("name")!=-1||row.indexOf("cell")!=-1)){
                        head=row;
                        errorfw.append("error_message,"+head+"\n");
                    }else{
                        Map<String,String> param=new HashMap<>();
                        param.put("row",row);
                        param.put("head",head);
                        param.put("checkBlackList",customer.getCheckBlackList().toString());
                        validatorExecutor.submit(new ValidatorSmallFileThread(context,param,errorfw,desTime,dbPoolCheckOpenMark ));
                    }
                }
            }


            /**
             * 等待所有任务都执行完成
             **/
            validatorExecutor.shutdown();
            while (true){
                if(validatorExecutor.isTerminated()){
                    log.info("所有线程都执行结束");
                    break;
                }
                try {
                    Thread.sleep(3000);
                }catch (Exception e){
                }
            }

        }catch (Exception e){
            log.error("checkSmallFile error",e);
        }
        log.warn(String.format("check耗时--batchNumber:%s~~time:%d~~desTime:%d~~开启check:%s~~poolSize:%d"
                ,context.getTask().getBatchNumber(),System.currentTimeMillis()-l,desTime.get(),dbPoolCheckOpenMark.equals(1)?"开":"关",dbPoolNum));
        return true;
    }

    @Override
    public boolean checkDataFile(String path, String filename) {
        long l = System.currentTimeMillis();
        ExecutorService validatorExecutor = BrExecutors.getThreadPool(20,20);
        String[] split = filename.split("_");
        String apiCode=split[0];
        File file=new File(path+"/"+filename);
        if(!file.exists()){
            return false;
        }

        try(FileReader read = new FileReader(path+filename);
            BufferedReader br = new BufferedReader(read);) {
            int rownum = 0;
            int fileNo = 1;
            String head="";
            String row;
            List<String> dataList=new ArrayList<>();
            while ((row = br.readLine()) != null) {
                String trim = row.trim();
                if(StringUtils.isNotEmpty(row)&&StringUtils.isNotEmpty(trim)){
                    if(row.indexOf("name")==-1&&row.indexOf("id")==-1&&row.indexOf("cell")==-1){
                        rownum++;
                        dataList.add(row);
                        if((rownum / SPLITNUM) > (fileNo - 1)){
                            validatorExecutor.submit(new ValidatorThread(dataList,apiCode,path,fileNo,decodeClient,head,filename));
                            fileNo ++ ;
                            dataList=new ArrayList<>();
                        }
                    }else{
                        head=row;
                    }
                }
            }
            if(dataList.size()>0){
                validatorExecutor.submit(new ValidatorThread(dataList,apiCode,path,fileNo,decodeClient,head,filename));
            }
            log.info("rownum---{}",rownum);
        }catch (Exception e){
            log.error("check file fail --{}",e);
            return false;
        }

        /**
         * 等待所有任务都执行完成
         **/
        validatorExecutor.shutdown();
        while (true){
            if(validatorExecutor.isTerminated()){
                log.info("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(3000);
            }catch (Exception e){
            }
        }
        log.info("cost time :{}",System.currentTimeMillis()-l);
        return true;
    }


    public void errorDetail(FileContext context, String errorMessage, ErrorFileTypeEnum errorFileTypeEnum){
        SftpToDbUtils.returnErrorFile(context,errorMessage,errorFileTypeEnum);
        String fileName="";
        switch (errorFileTypeEnum){
            case ERROR_CONFIG:
                fileName=context.getConfigFileName();
                break;
            case ERROR_DATA:
                fileName=context.getTxtFileName();
                break;
            case ERROR_FILE:
                fileName=context.getTxtFileName();
                break;
            default:
        }
        LoadResult loadResult=LoadResult.builder().apiCode(context.getApiCode())
                .cusBatch(context.getCusBatch())
                .fileName(fileName)
                .message(errorMessage)
                .status("0")
                .batchNumber(context.getBatchNumber())
                .taskNumber(0).actualNumber(0).type(context.getType()).build();
        loadResultMapper.insertLoadResult(loadResult);
    }


    /**
     * 校验txt文件格式
     * @param context 参数对象
     * @param errorMessage 错误信息
     * @return 校验成功或者失败
     */
    public  boolean checkTxtfile(FileContext context,StringBuilder errorMessage){
        File dir=new File(context.getLocalTxtFilePath());
        File[] fileList = dir.listFiles(pathName -> {
            String name = pathName.getName();
            if(name.endsWith(".txt")){
                return true;
            }
            return false;
        });

        boolean flag=true;
        if(fileList.length!=1){
            errorMessage.append("压缩文件找不到上传数据文件");
            errorDetail(context,errorMessage.toString(),ErrorFileTypeEnum.ERROR_FILE);
            return false;
        }
        if(!fileList[0].getName().equals(context.getTxtFileName())){
            File file1 = fileList[0];
            String name = file1.getName();
            flag = SftpToDbUtils.vaildFileName(name, context.getApiCode(), errorMessage);
        }
        if(!flag){
            errorDetail(context,errorMessage.toString(),ErrorFileTypeEnum.ERROR_FILE);
            return false;
        }
        return true;
    }



    /**
     * 配置内容检验
     * @param field 字段
     * @param value 字段值
     * @param apiCode apiCode
     * @param value1 字段值
     * @return
     */
    public boolean checkConfig(String field,String value,String apiCode,String value1){
        boolean flag=true;

        switch (field) {
            case "strategyId":
                String s = strategyCs.strategyIdCheck(apiCode, value);
                if(StringUtils.isEmpty(s)){
                    flag=false;
                }
                break;
            case "monitorFrequency":
                if(!Constants.FREQUENCY.matcher(value).matches()){
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

    public void volidatorDataVolume(Integer dataVolume, int i,String apiCode,String fileName) {
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

    private boolean isVaildMonitorStartTime(String value,String apiCode) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFirst = new Date();
        Date dateLast = dateFormat.parse(value);
        if(!dateFirst.before(dateLast)){
            return false;
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

}
