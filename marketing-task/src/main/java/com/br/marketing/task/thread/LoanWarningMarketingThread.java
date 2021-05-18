package com.br.marketing.task.thread;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.entity.RequestLog;
import com.br.marketing.task.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;


public class LoanWarningMarketingThread implements Callable<String> {
    private static final Logger log = LoggerFactory.getLogger(LoanWarningMarketingThread.class);
    private List<MarketingUser> list;
    private String apiCode;
    private int currentPage;
    private ProFieldsClient proFieldsClient;
    private boolean notSaveLog;
    private JSONObject strategyJson;
    private String appSecretKey;
    private JSONObject meal=new JSONObject();
    private String url;
    private Map<String,String> proFieldMap=new HashMap<>();
    private Map<String,String> sceStrProFieldMap=new HashMap<>();
    private List<MarketingUser> errorList=new ArrayList<>();
    private RedisChgService redisChgService;
    private String batchNumber;
    public LoanWarningMarketingThread(List<MarketingUser> list, Map<String,String> param, int currentPage, JSONObject strategyJson,
                                      ProFieldsClient proFieldsClient, boolean notSaveLog, RedisChgService redisChgService){
        this.list=list;
        this.apiCode=param.get("apiCode");
        this.currentPage=currentPage;
        this.proFieldsClient=proFieldsClient;
        this.notSaveLog=notSaveLog;
        this.strategyJson=strategyJson;
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
        String path = "" ;
        String stmtKey="";
        File errorFile=null;
        Writer errorFw=null;
        Map<String,Writer> writerMap=new HashMap<>();

        try {
            if(strategyJson!=null&&!strategyJson.isEmpty()){
                strategyId=strategyJson.getString("strategyId");
                strategyStr=strategyJson.getString("strategy");
                path=strategyJson.getString("path");
                stmtKey=strategyJson.getString("stmt_key");
                File writePath = new File(path );
                if (!writePath.exists()) {
                    boolean mkdirs = writePath.mkdirs();
                    if(!mkdirs){
                       log.error("mkdirs fail");
                    }
                }
                String dateAddYyMmDdHhMmSs = DateHelper.getDateAddYyMmDdHhMmSs(0);


                errorFile=new File(path + "/error"+ currentPage +"_"+dateAddYyMmDdHhMmSs+".txt");
                errorFw = new BufferedWriter(
                        new OutputStreamWriter(
                                Files.newOutputStream(Paths.get(path + "/error"+ currentPage +"_"+dateAddYyMmDdHhMmSs+".txt"))
                                ,StandardCharsets.UTF_8));

                proFieldsClient.setLoanPro(strategyId,apiCode,strategyStr,meal,sceStrProFieldMap,stmtKey);
                Set<String> products = meal.keySet();
                for(String product:products){
                    String lowerProduct= product.toLowerCase();
                    boolean flag=false;
                    File productPath = new File(path+ "/" +lowerProduct+"/" );
                    if(productPath.exists()){
                        flag=true;
                    }else {
                        flag= productPath.mkdirs();
                    }
                    if(flag){
                        Writer fw = new BufferedWriter(
                                new OutputStreamWriter(
                                        Files.newOutputStream(Paths.get(path + "/" +lowerProduct+"/"+currentPage + ".txt")), StandardCharsets.UTF_8));
                        writerMap.put(lowerProduct,fw);
                    }else {
                        log.error("productPath create fail");
                    }

                }

            }else {
                return null;
            }

            this.query(writerMap,list,strategyId);
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
            for (String key:writerMap.keySet()){
                Writer value = writerMap.get(key);
                if(value!=null){
                    value.close();
                }
            }
        }
      return null;
    }


    /**
     * 调用画像接口查询样本结果
     * @param writerMap 重点字段结果输出
     * @param list 样本列表
     * @param strategyId 策略编号
     * @throws IOException
     */
    private void query(Map<String,Writer> writerMap, List<MarketingUser> list, String strategyId) throws IOException {
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
                    MomUtil.send_mom(s,jsonData,requestLog,apiCode,strategyId,appSecretKey);
                }
            }
            dealResult(s,writerMap,blu.getCusNum(),blu.getBatchNumber(),strategyJson,blu);
        }
    }

    /**
     * 用流失预警api的返回生成结果文件
     * @param s
     */
    private void dealResult(String s, Map<String,Writer> writerMap, String cusNum,
                            String batchNumber, JSONObject strategyJson, MarketingUser blu) throws IOException {
        try {
            if(VaildHxResultUtil.isPass(s,meal,apiCode,redisChgService,blu,errorList)){
                JSONObject resultJson=JSONObject.parseObject(s);
                MarketingResultUtil.generateFile(resultJson,writerMap,strategyJson,cusNum,batchNumber,proFieldMap);
            }
        }catch (Exception e){
            log.error("dealResult出错了",e);
        }
    }



}
