package com.br.marketing.check.service.Impl;

import com.br.marketing.check.thread.ValidatorSmallFileThread;
import com.br.marketing.check.thread.ValidatorThread;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.client.IceClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.*;
import com.br.marketing.check.service.FileCkeckServicce;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.MarketingDirtyUserMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.service.Impl.StrategyCs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by Bairong on 2020/1/15.
 */
@Service
@Slf4j
public class FileCkeckServicceImpl implements FileCkeckServicce {

    @Resource
    DecodeClient decodeClient;

    @Resource
    StrategyCs strategyCs;

    private final static Integer SPLITNUM=5000;

    @Resource
    MarketingUserMapper marketingUserMapper;
    @Resource
    MarketingDirtyUserMapper marketingDirtyUserMapper;
    @Resource
    RedisChgService redisChgService;
    @Override
    public boolean strategyIdCheck(String apiCode, String strategyId) {
        return StringUtils.isEmpty(strategyCs.strategyIdCheck(apiCode,strategyId))?false:true;
    }

    @Override
    public boolean checkSmallDataFile(String path, String filename,boolean flag,String batchNumber) {
        String[] split = filename.split("_");
        String apiCode=split[0];
        long l = System.currentTimeMillis();
        ExecutorService validatorExecutor = BrExecutors.getThreadPool(40,40);
        MerchantParam merchantParam = IceClient.getMerchantParam(apiCode);
        String resultPath=path+"/result/";
        String errorPath=path+"/error/";
        File resultPathFile=new File(resultPath);
        if(!resultPathFile.exists()){
            resultPathFile.mkdirs();
        }
        File errorPathFile=new File(errorPath);
        if(!errorPathFile.exists()){
            errorPathFile.mkdirs();
        }
        String errorFileName="";
        if(org.apache.commons.lang.StringUtils.isNotEmpty(apiCode)&&
                (apiCode.equals(Constants.APICODE_360)||apiCode.equals(Constants.APICODE_360_QA))){
            if(split.length>=4){
                errorFileName=split[0]+"_"+split[1]+"_"+split[2]+"_error_"+split[3];
            }
        }else{
            if(split.length>=3){
                errorFileName=split[0]+"_"+split[1]+"_"+"error_"+split[2];
            }
        }
        File decodeFile = new File(resultPath + Constants.FILE_DATA_RESULT);
        File file1 = new File(errorPath +errorFileName);
        try(BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(decodeFile), "UTF-8"));
        BufferedWriter errorfw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file1), "UTF-8"));
        FileReader read = new FileReader(path+filename);
        BufferedReader br = new BufferedReader(read)) {
            String row;
            String head="";
            while ((row = br.readLine()) != null) {
                //log.info("checkSmallDataFile row:{}",row);
                String trim = row.trim();
                if(StringUtils.isNotEmpty(row)&&StringUtils.isNotEmpty(trim)){
                    if(row.indexOf("cus_num")!=-1&&(row.indexOf("id")!=-1||row.indexOf("name")!=-1||row.indexOf("cell")!=-1)){
                        head=row;
                        if(!flag){
                            errorfw.append("error_message,"+head+"\n");
                        }
                    }else{
                        Map<String,String> param=new HashMap<>();
                        param.put("row",row);
                        param.put("head",head);
                        param.put("apiCode",apiCode);
                        param.put("fileName",filename);
                        param.put("batchNumber",batchNumber);
                        validatorExecutor.submit(new ValidatorSmallFileThread(param,fw,errorfw,decodeClient,flag, marketingUserMapper, marketingDirtyUserMapper
                                , redisChgService,merchantParam));
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
        log.warn("cost time :{}",System.currentTimeMillis()-l);
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

}
