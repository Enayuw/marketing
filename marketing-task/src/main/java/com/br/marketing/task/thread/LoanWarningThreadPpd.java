package com.br.marketing.task.thread;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.client.*;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.*;
import com.br.marketing.task.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;


public class LoanWarningThreadPpd implements Callable<String> {
    private static final Logger log = LoggerFactory.getLogger(LoanWarningThreadPpd.class);
    private List<MarketingUser> list;
    private String apiCode;
    private int currentPage;
    private ProFieldsClient proFieldsClient;
    private boolean notSaveLog;
    private JSONObject firstJson;
    private JSONObject secJson;
    private String appSecretKey;
    private JSONObject meal=new JSONObject();
    private String url;
    private Map<String,String> proFieldMap=new HashMap<>();
    private Map<String,String> sceStrProFieldMap=new HashMap<>();
    private List<MarketingUser> errorList=new ArrayList<>();
    private RedisChgService redisChgService;
    private String batchNumber;
    public LoanWarningThreadPpd(List<MarketingUser> list, Map<String,String> param, int currentPage, JSONObject firstJson,
                                JSONObject secJson, ProFieldsClient proFieldsClient, boolean notSaveLog, RedisChgService redisChgService){
        this.list=list;
        this.apiCode=param.get("apiCode");
        this.currentPage=currentPage;
        this.proFieldsClient=proFieldsClient;
        this.notSaveLog=notSaveLog;
        this.firstJson=firstJson;
        this.secJson=secJson;
        this.appSecretKey=param.get("appSecretKey");
        this.url=param.get("url");
        this.redisChgService=redisChgService;
        this.batchNumber=param.get("batchNumber");;
    }

    @Override
    public String call() throws Exception {
        log.info("start-----------------");
        if(list.size()==0){
            log.warn("开始执行监控任务。。{}。。{}",currentPage,list.size());
            return null;
        }
        String strategyId="";
        String strategyStr="";
        String firstPath = "" ;
        String secPath = "" ;
        String stmtKey="";

        Writer secPathFw=null;
        Writer firstPathFw=null;
        Writer errorFw=null;
        File errorFile=null;
        try {
            if(firstJson!=null&&!firstJson.isEmpty()){
                strategyId=firstJson.getString("strategyId");
                strategyStr=firstJson.getString("strategy");
                firstPath=firstJson.getString("path");
                secPath=secJson.getString("path");
                File writeFirstPath = new File(firstPath );
                if (!writeFirstPath.exists()) {
                    writeFirstPath.mkdirs();
                }
                File writeSecPath = new File(secPath );
                if (!writeSecPath.exists()) {
                    writeSecPath.mkdirs();
                }
                String dateAddYyMmDdHhMmSs = DateHelper.getDateAddYyMmDdHhMmSs(0);
                 errorFile = new File(firstPath + "/error"+ currentPage +"_"+dateAddYyMmDdHhMmSs+  ".txt");
                 errorFw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(errorFile), "UTF-8"));


                File file1 = new File(firstPath + "/" + currentPage + ".txt");
                firstPathFw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file1), "UTF-8"));

                File sec_path_file1 = new File(secPath + "/" + currentPage + ".txt");
                secPathFw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(sec_path_file1), "UTF-8"));
                proFieldsClient.setLoanPro(strategyId,apiCode,strategyStr,meal,proFieldMap,"");
            }else{
                strategyId=secJson.getString("strategyId");

                secPath=secJson.getString("path");
                File writeSecPath = new File(secPath );
                if (!writeSecPath.exists()) {
                    writeSecPath.mkdirs();
                }

                File secPathFile1 = new File(secPath + "/" + currentPage + ".txt");
                secPathFw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(secPathFile1), "UTF-8"));

                errorFile = new File(secPath + "/error"+ currentPage + ".txt");
                errorFw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(errorFile), "UTF-8"));
            }
            stmtKey=secJson.getString("stmt_key");
            proFieldsClient.setLoanPro(secJson.getString("strategyId"),apiCode,secJson.getString("strategy"),meal,sceStrProFieldMap,stmtKey);
            this.query(firstPathFw,secPathFw,list,strategyId);
            if(errorList.size()>0){
                for(MarketingUser lu:errorList){
                    errorFw.append(lu.getBatchNumber()+","+lu.getCusNum()+","+lu.getIdCard()+","+lu.getCell()+","+lu.getName()+",end\n");
                }
                String key= Constants.HXRESULTERROR_RETRY_KEY+":"+apiCode;
                redisChgService.hset(key,errorFile.getPath(),batchNumber);
            }
        }catch (Exception e){
            log.error("生成文件出错。。。。",e);
        }finally {
            if(firstPathFw!=null){
                firstPathFw.close();
            }
            if(errorFw!=null){
                errorFw.close();
            }
            if(secPathFw!=null){
                secPathFw.close();
            }
        }
      return null;
    }


    /**
     * 调用画像接口查询样本结果
     * @param firstPathFw 全量字段结果输出
     * @param secPathFw 重点字段结果输出
     * @param list 样本列表
     * @param strategyId 策略编号
     * @throws IOException
     */
    private void query(Writer firstPathFw, Writer secPathFw, List<MarketingUser> list, String strategyId) throws IOException {
        log.warn("开始执行监控任务。。{}。。{}",currentPage,list.size());
        BrCipherMaker instance = BrCipherMaker.getInstance();

        for (MarketingUser blu : list) {
            if(blu.getStatus()!=1){
                continue;
            }
            RequestLog requestLog=new RequestLog();
            requestLog.setRequestTime(new Date());

            JSONObject jsonData = new JSONObject();
            jsonData.put("cusNum", blu.getCusNum());
            jsonData.put("idCard",instance .decode(blu.getIdCard()));
            jsonData.put("name", instance.decode(blu.getName()));
            jsonData.put("cell", instance.decode(blu.getCell()));
            jsonData.put("passDate", blu.getPassDate());
            jsonData.put("user_date", blu.getUserDate());
            jsonData.put("loanMaturityDate", blu.getLoanMaturityDate());
            jsonData.put("batch_number", blu.getBatchNumber());
            if(StringUtils.isNotEmpty(blu.getDecodeFailType())){
                jsonData.put("decodeFailType", blu.getDecodeFailType());
            }
            String s="";
            if (strategyId.startsWith("DTB")){
                s= HxUtil.getReport(apiCode,jsonData,meal,notSaveLog,url);
                requestLog.setResponseTime(new Date());
                if(!notSaveLog) {
                    MomUtil.sendMom(s,jsonData,requestLog,apiCode,strategyId,appSecretKey);
                }
            }
            dealResult(s, firstPathFw,secPathFw,blu.getCusNum(),blu.getBatchNumber(),firstJson,secJson,blu);
        }
    }

    /**
     * 用流失预警api的返回生成结果文件
     * @param s
     */
    private void dealResult(String s, Writer firstPathFw, Writer secPathFw, String cusNum,
                            String batchNumber, JSONObject firstStrategy, JSONObject secStrategy, MarketingUser blu) throws IOException {
        try {
            if(VaildHxResultUtil.isPass(s,meal,apiCode,redisChgService,blu,errorList)){
                JSONObject resultJson=JSONObject.parseObject(s);
                if(firstPathFw!=null){
                    log.info("firstStrategy:{}",firstStrategy);
                    PpdResultUtil.generateFile(resultJson,firstStrategy,firstPathFw,cusNum,batchNumber,proFieldMap);
                }
                if(secPathFw!=null){
                    log.info("secStrategy:{}",secStrategy);
                    PpdResultUtil.generateFile(resultJson,secStrategy,secPathFw,cusNum,batchNumber,sceStrProFieldMap);
                }
            }

        }catch (Exception e){
            log.error("dealResult出错了",e);
        }
    }




}
