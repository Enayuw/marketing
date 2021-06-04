package com.br.marketing.task.thread;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.BrCipherMaker;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.entity.RequestLog;
import com.br.marketing.task.utils.ChgResultUtil;
import com.br.marketing.task.utils.HxUtil;
import com.br.marketing.task.utils.MomUtil;
import com.br.marketing.task.utils.VaildHxResultUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by Bairong on 2020/5/8.
 */
@Slf4j
public class LoanWarningChgThread implements Callable<String> {

    private List<MarketingUser> list;
    private String apiCode;
    private String strategyId;
    private int currentPage;
    private String path;
    private RedisChgService redisChgService;
    private boolean isIncr;
    private String appSecretKey;
    private String url;
    private JSONObject meal=new JSONObject();
    private Map<String,String> proFieldMap=new HashMap<>();
    private String sep;
    private String noChgPath;
    private boolean isCycle;
    private String batchNumber;
    private List<MarketingUser> errorList=new ArrayList<>();
    public LoanWarningChgThread(List<MarketingUser> list, Map<String,String> param, boolean isCycle,
                                int currentPage, String path, String noChgPath, RedisChgService redisChgService,
                                ProFieldsClient proFieldsClient, boolean isIncr, String appSecretKey) {
        this.list = list;
        this.apiCode = param.get("apiCode");
        this.strategyId = param.get("strategyId") ;
        this.currentPage = currentPage;
        this.path = path;
        this.redisChgService = redisChgService;
        this.isIncr = isIncr;
        this.appSecretKey = appSecretKey;
        this.url =param.get("url")  ;
        this.sep= param.get("sep") ;
        this.noChgPath=noChgPath;
        this.isCycle=isCycle;
        this.batchNumber=param.get("batchNumber");
        proFieldsClient.setLoanPro(strategyId,apiCode,param.get("strategyStr"),meal,proFieldMap,"");
    }

    @Override
    public String call() throws Exception {
        log.warn("开始执行监控任务。。{}。。{}",currentPage,list.size());
        File writeName = new File(path );
        if (!writeName.exists()) {
            writeName.mkdirs();
        }
        File writeNoChgName = new File(noChgPath );
        if (!writeNoChgName.exists()) {
            writeNoChgName.mkdirs();
        }
        String dateAddYyMmDdHhMmSs = DateHelper.getDateAddYyMmDdHhMmSs(0);
        File file1 = new File(path + "/" + currentPage + ".txt");
        File file = new File(noChgPath + "/" + currentPage + ".txt");
        File errorFile = new File(path + "/error"+ currentPage +"_"+dateAddYyMmDdHhMmSs+ ".txt");
        try (
                Writer  fw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file1), "UTF-8"));
                Writer noChgFw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file), "UTF-8"));
                Writer  errorFw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(errorFile), "UTF-8"));
                ){

            this.query(fw, noChgFw,list);
            if (errorList.size()>0){
                for(MarketingUser lu:errorList){
                    errorFw.append(lu.getBatchNumber()+","+lu.getCusNum()+","+lu.getIdCard()+","+lu.getCell()+","+lu.getName()+",end\n");
                }
                String key= Constants.HXRESULTERROR_RETRY_KEY+":"+apiCode;
                redisChgService.hset(key,errorFile.getPath(),batchNumber);
            }
        }catch (Exception e){
            log.error("流失预警变动任务执行失败",e);
        }
        return null;
    }

    /**
     * 调用画像接口查询样本结果
     * @param fw 结果输出
     * @param noChgFw 无变动结果输出
     * @param list 样本列表
     * @throws IOException
     */
    private void query(Writer fw, Writer noChgFw, List<MarketingUser> list) throws IOException {
        BrCipherMaker instance = BrCipherMaker.getInstance();
        for (MarketingUser blu : list) {
            if(blu.getStatus()!=1){
                continue;
            }
            RequestLog requestLog=new RequestLog();
            requestLog.setRequestTime(new Date());

            JSONObject jsonData = new JSONObject();
            jsonData.put("cusNum", blu.getCusNum());
            jsonData.put("idCard", instance.decode(blu.getIdCard()));
            jsonData.put("name", instance.decode(blu.getName()));
            jsonData.put("cell", instance.decode(blu.getCell()));
            jsonData.put("passDate", blu.getPassDate());
            jsonData.put("user_date", blu.getUserDate());
            jsonData.put("loanMaturityDate", blu.getLoanMaturityDate());
            jsonData.put("batch_number", blu.getBatchNumber());
            if(StringUtils.isNotEmpty(blu.getDecodeFailType())){
                jsonData.put("decodeFailType", blu.getDecodeFailType());
            }
            String  s= HxUtil.getReport(apiCode,jsonData,meal,isIncr,url);
            requestLog.setResponseTime(new Date());
            if(!isIncr) {
                MomUtil.sendMom(s,jsonData,requestLog,apiCode,strategyId,appSecretKey);
            }
            dealResult(s, fw,noChgFw,blu.getCusNum(),blu.getBatchNumber(),strategyId,isIncr,blu);
        }
    }


    /**
     * 用流失预警api的返回生成结果文件
     * @param s
     */
    private void dealResult(String s, Writer fw, Writer noChgFw, String cusNum,
                            String batchNumber, String strategyId, boolean flag, MarketingUser lu) throws IOException {

        try {
            if(VaildHxResultUtil.isPass(s,meal,apiCode,redisChgService,lu,errorList)){
                JSONObject resultJson=JSONObject.parseObject(s);
                if(fw!=null){
                    ChgResultUtil.generateFile(resultJson,fw,noChgFw,apiCode,cusNum,batchNumber,
                            strategyId,flag,redisChgService,sep,proFieldMap,isCycle);
                }
            }
        }catch (Exception e){
            log.error("dealResult出错了",e);
        }
    }

}
