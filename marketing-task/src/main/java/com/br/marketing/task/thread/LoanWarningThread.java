package com.br.marketing.task.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.client.*;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.task.Scheduler;
import com.br.marketing.task.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by Bairong on 2019/8/20.
 * 流失预警任务异步处理
 */

public class LoanWarningThread implements Callable<String> {
    private static final Logger log = LoggerFactory.getLogger(LoanWarningThread.class);
    private List<MarketingUser> list;
    private String apiCode;
    private LoanWarningClient loanWarningClient;
    private String strategyId;
    private int currentPage;
    private String path;
    private String strategyStr;
    private RedisService redisService;
    private String   message;
    private boolean isIncr;

    /**
     * [
     {
     "platformCode":"",
     "interfaceType":"C4,C3",
     "additionInfo":"{}",
     "priceWay":"1",
     "sceneCode":"lend",
     "version":"S1.0",
     "serviceName":"1",
     "description":"",
     "businessTypeCode":"A101,A202",
     "compatibleVersion":"",
     "productionName":"scorebmix",
     "customerGroupCode":"100080",
     "dtsStatus":"1,2",
     "dependDataProduction":"{"InfoRelation":"V1.0","SpecialList_c":"V1.0","Consumption_c":"V2.0","Stability_c":"V2.0","ApplyLoanStr":"V2.0"}",
     "cost":"0",
     "productionTypeCode":"B303",
     "prerequisite":"0",
     "dtsThread":"5",
     "crmStatus":"1",
     "spreadStatus":"2",
     "productionChineseName":"贷中行为模型-通用客群",
     "dataDescription":"",
     "crmCustomer":"1",
     "abutmentWay":"3",
     "introduction":"适用于通用客群的贷中风险识别。"
     },
     {
     "platformCode":"",
     "interfaceType":"C3",
     "additionInfo":"{}",
     "priceWay":"1",
     "sceneCode":"",
     "version":"V1.0",
     "serviceName":"",
     "description":"",
     "businessTypeCode":"A202",
     "compatibleVersion":"",
     "productionName":"Rule_W_SpecialList_c_revoloan",
     "customerGroupCode":"100084",
     "dtsStatus":"0",
     "dependDataProduction":"{"SpecialList_c":"V1.0"}",
     "cost":"0",
     "productionTypeCode":"B201",
     "prerequisite":"0",
     "dtsThread":"0",
     "crmStatus":"1",
     "spreadStatus":"2",
     "productionChineseName":"贷中预警全量规则-特殊名单验证-信用卡（类信用卡）",
     "dataDescription":"",
     "crmCustomer":"1",
     "abutmentWay":"3",
     "introduction":"信用卡/类信用卡客群特殊名单验证贷中预警全量规则"
     }
     ]
     */
   // private JSONArray loanProAray;

    private JSONObject meal=new JSONObject();
    private String appSecretKey;
    private String url;
    private Map<String,String> proFieldMap=new HashMap<>();
    private String sep;
    private List<MarketingUser> errorList=new ArrayList<>();
    private RedisChgService redisChgService;
    private String batchNumber;
    private String cusBatchNumber;
    private String isRepair;
    private String fileId;
    private Customer customer;
    public LoanWarningThread(List<MarketingUser> list, Map<String,String> param, int currentPage,boolean isIncr,Customer customer){
        this.list=list;
        this.apiCode=param.get("apiCode");
        this.strategyId=param.get("strategyId");
        this.loanWarningClient= Scheduler.ac.getBean(LoanWarningClient.class);
        this.currentPage=currentPage;
        this.path=param.get("path");
        this.strategyStr=param.get("strategyStr");
        this.redisService=Scheduler.ac.getBean(RedisService.class);
        this.isIncr=isIncr;
        this.appSecretKey=param.get("appSecretKey");
        this.url=param.get("url");
        this.sep=param.get("sep");
        this.redisChgService=Scheduler.ac.getBean(RedisChgService.class);
        this.batchNumber=param.get("batchNumber");
        this.cusBatchNumber=param.get("cusBatchNumber");
        this.isRepair=param.get("isRepair");
        this.customer=customer;
        Scheduler.ac.getBean(ProFieldsClient.class).setLoanPro(strategyId,apiCode,strategyStr,meal,proFieldMap,"");
    }





    @Override
    public String call() throws Exception {
        log.warn("start-----------------");
        if(list.size()==0){
            log.warn("开始执行监控任务。。{}。。{}",currentPage,list.size());
            return null;
        }

        boolean check=this.checkRedisNumber();
        log.warn("开始执行监控任务。。{}。。{}",currentPage,list.size());
        String descPath = path ;

        File writeName = new File(descPath );
        if (!writeName.exists()) {
            writeName.mkdirs();
        }

        File errorFile = new File(descPath + "/error"+ currentPage + ".txt");
        File file1 = new File(descPath + "/" + currentPage + ".txt");

        try(Writer errorFw = new BufferedWriter(
                new OutputStreamWriter(
                new FileOutputStream(errorFile), "UTF-8"));
            Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file1), "UTF-8"));) {

            if(!check){
                log.error("条数不足--{}",message);
                dealResult(message,errorFw);
                errorFw.close();
                return null;
            }
            JSONObject param = new JSONObject();
            param.put("strategyId", strategyId);
            BrCipherMaker instance = BrCipherMaker.getInstance();
            for (MarketingUser blu : list) {
                if(blu.getStatus()!=1){
                    continue;
                }
                RequestLog  requestLog=new RequestLog();
                requestLog.setRequestTime(new Date());

                JSONObject jsonData = new JSONObject();
                jsonData.put("cusNum", blu.getCusNum());
                jsonData.put("idCard",instance.decode(blu.getIdCard()));
                jsonData.put("name", instance.decode(blu.getName()));
                jsonData.put("cell", instance.decode(blu.getCell()));
                jsonData.put("passDate", blu.getPassDate());
                jsonData.put("loanMaturityDate", blu.getLoanMaturityDate());
                jsonData.put("isRepair", isRepair);
                if(StringUtils.isNotEmpty(blu.getDecodeFailType())){
                    jsonData.put("decodeFailType", blu.getDecodeFailType());
                }
                String approvalResult = blu.getApprovalResult();
                String result="";
                if(StringUtils.isNotEmpty(approvalResult)){
                    switch (approvalResult){
                        case "通过" :
                            result= "1";
                            break;
                        case "拒绝" :
                            result= "2";
                            break;
                        case "复议" :
                            result= "3";
                            break;
                        case "无结果" :
                            result= "4";
                            break;
                        case "无贷前审批" :
                            result= "5";
                            break;
                        default:
                            break;
                    }
                }

                if(StringUtils.isNotEmpty(result)){
                    jsonData.put("approveResult", result);
                }
                jsonData.put("batch_number", blu.getBatchNumber());
                param.put("jsonData", jsonData.toString());
                String s="";
                if (strategyId.startsWith("DTM")){
                    //log.info("DTB策略调用画像");
                    s= HxUtil.getReport(customer,jsonData,meal,isIncr,url);
                    requestLog.setResponseTime(new Date());
                    if(!isIncr) {
                        MomUtil.sendMom(s,jsonData,requestLog,apiCode,strategyId,appSecretKey);
                    }
                }else{
                    s = loanWarningClient.queryApi(param, apiCode);
                }
                dealResult(s, fw,errorFw,blu.getCusNum(),blu.getBatchNumber(),apiCode, blu);
            }

            if(errorList.size()>0){
                for(MarketingUser lu:errorList){
                    errorFw.append(lu.getBatchNumber()+","+lu.getCusNum()+","+lu.getIdCard()+","+lu.getCell()
                            +","+lu.getName()+","+lu.getHitData()+",end\n");
                }
                String key= Constants.HXRESULTERROR_RETRY_KEY+":"+apiCode;
                redisChgService.hset(key,errorFile.getPath(),batchNumber);
            }

        }catch (Exception e){
            log.error("生成文件出错。。。。",e);
        }
      return null;
    }


    /**
     * 校验api_code的数据产品条数
     * @return
     */
    private boolean checkRedisNumber() {
        boolean flag=true;
        if(customer.getCheckRedisNumber()==0){
            return flag;
        }
        try{
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            Map<String,String> dayNumMap =new HashMap<>();
            List<String> typeNoList=new ArrayList<>();
            if(strategyId.startsWith("STRB")){
                addSTRBPro(typeNoList);
            }else if(strategyId.startsWith("DTM")){
                addDTBPro(typeNoList);
            }
            MerchantParam merchantParam = IceClient.getMerchantParam(apiCode);
            if(merchantParam==null){
                log.error("用户中心结果为空");
                return false;
            }
            getDayNumMap(dayNumMap,merchantParam);
            if(merchantParam.getAccountType()==1){
                //在redis中加上使用条数
                addRedisNum(Constants.REDIS_RADAR_PREFIX+":"+apiCode, typeNoList, list.size());
                for(String proCode:typeNoList){
                    String key = Constants.REDIS_RADAR_PREFIX +":"+ apiCode +":"+ proCode +":"+ date;
                    String currentNum = redisService.get(key);
                    if(currentNum==null){
                        redisService.set(key,null,604800);
                        addRedisNumForDayNum(key,null,list.size());
                    }else {
                        addRedisNumForDayNum(key, null, list.size());
                    }
                }
            }else {
                long min = 0;
                String result = getMinNum(apiCode, typeNoList,merchantParam);
                try{
                    min = Long.parseLong(result);
                }catch (Exception e){
                    message =result;
                    log.error("message--{}",message);
                    flag = false;
                    return flag;
                }

                if (min >= list.size()) {
                    addRedisNum(Constants.REDIS_RADAR_TEST_PREFIX+":"+apiCode, typeNoList, list.size());
                    for(String proCode:typeNoList){
                        String keyTest = Constants.REDIS_RADAR_TEST_PREFIX +":"+ apiCode +":"+ proCode +":"+ date;
                        String currentNum = redisService.get(keyTest);
                        String dayNum = dayNumMap.get(proCode);
                        if(currentNum==null){
                            redisService.set(keyTest,null,604800);
                            if(list.size() > Integer.parseInt(dayNum)){
                                message="可用条数不足，请确认，若需要请联系客服";
                                log.error("message--{}",message);
                                flag = false;
                            }else{
                                addRedisNumForDayNum(keyTest,null,list.size());
                            }
                        }else {
                            if (Integer.parseInt(currentNum) + list.size() > Integer.parseInt(dayNum)) {
                                message="可用条数不足，请确认，若需要请联系客服";
                                log.error("message--{}",message);
                                flag = false;
                            } else {
                                addRedisNumForDayNum(keyTest, null, list.size());
                            }
                        }
                    }
                } else {
                    message="可用条数不足，请确认，若需要请联系客服";
                    log.error("message--{}",message);
                    flag = false;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("error----{}",e);
        }

        return flag;
    }


    /**
     * 获取redis中最小的产品条数
     *
     * @param apiCode
     * @param typeNoList
     * @return
     */
    private String getMinNum(String apiCode, List<String> typeNoList,MerchantParam merchantParam) {
        List<Long> numList = new ArrayList<>();
        for (String typeNo : typeNoList) {
            String keyTest = Constants.REDIS_RADAR_TEST_PREFIX +":"+ apiCode +":"+ typeNo+":"+Constants.REDIS_RADAR_TOTALCOUNT;
            String str = redisService.get(keyTest);
            long num = 0;
            if (!org.springframework.util.StringUtils.isEmpty(str)) {
                num = Long.parseLong(str);
            }
            String limitNumFromUserCenter = getLimitNumFromUserCenter( typeNo,merchantParam);
            long canUseNum = 0;
            if (!"empty".equals(limitNumFromUserCenter)) {
                canUseNum = Long.parseLong(limitNumFromUserCenter) - num;
            } else {
                return "策略状态不可用";
            }
            numList.add(canUseNum);
        }
        //log.info("各个数据产品的可用条数：数据产品代号：{}，剩余条数：{}", JSON.toJSONString(typeNoList), JSON.toJSONString(numList));
        if (numList.size() > 0) {
            String string = Collections.min(numList).toString();
            return string;
        } else {
            return "0";
        }

    }

    private String getLimitNumFromUserCenter( String proCode, MerchantParam merchantParam ) {
        String limitNum = null;
            String meal = merchantParam.getMealJson();
            if(!org.springframework.util.StringUtils.isEmpty(meal)){
                JSONObject mealJson = JSON.parseObject(meal);
                String proCodeString = mealJson.getString(proCode);
                if (!org.springframework.util.StringUtils.isEmpty(proCodeString)) {
                    JSONObject proCodeJson = JSON.parseObject(proCodeString);
                    limitNum = proCodeJson.getString("limit_num");
                } else {
                    log.warn("产品---{}---没有权限",proCode);
                    return "empty";
                }
            }
        return limitNum;
    }
    private void getDayNumMap(Map<String,String> dayNumMap, MerchantParam merchantParam){
            String meal = merchantParam.getMealJson();
            if(!org.springframework.util.StringUtils.isEmpty(meal)){
                JSONObject mealJson =JSON.parseObject(meal) ;
                Set<String> strings = mealJson.keySet();
                for(String key :strings){
                    String proCodeString = mealJson.getString(key);
                    if (!org.springframework.util.StringUtils.isEmpty(proCodeString)) {
                        JSONObject proCodeJson = JSON.parseObject(proCodeString);
                        String dayNum="";
                        if(proCodeJson!=null&&proCodeJson.containsKey("dayNum")){
                            dayNum = proCodeJson.getString("dayNum");
                        }else if(proCodeJson!=null&&!proCodeJson.containsKey("dayNum")) {
                            dayNum = proCodeJson.getString("limit_num");
                        }
                        if(!org.springframework.util.StringUtils.isEmpty(dayNum)){
                            dayNumMap.put(key,dayNum);
                        }

                    }
                }
            }
    }
    private void addRedisNumForDayNum(String apiCode, String typeNo, int num) {
        long l = System.currentTimeMillis();
         redisService.incrBy(apiCode, typeNo, num);
    }
    private void addRedisNum(String apiCode, List<String> typeNoList, int num) {
        long l = System.currentTimeMillis();
        for (String typeNo : typeNoList) {
            redisService.incrBy(apiCode+":"+typeNo+":"+Constants.REDIS_RADAR_TOTALCOUNT,null, num);
        }
    }

    /**
     * 添加风险策略需要校验的数据产品列表
     * @param typeNoList
     */
    private void addSTRBPro( List<String> typeNoList) {
        String strategy = StrategyClient.getStrategy(apiCode, strategyId);
        JSONObject jsonObject = JSONObject.parseObject(strategy);
        if(jsonObject!=null&&!jsonObject.isEmpty()){
            if(StringUtils.isNotEmpty(jsonObject.getString("canUse"))&&"0".equals(jsonObject.getString("canUse"))
                    &&StringUtils.isNotEmpty(jsonObject.getString("status"))&&"1".equals(jsonObject.getString("status"))){
                JSONObject ruleTypeJson = jsonObject.getJSONObject("ruleType");
                if(StringUtils.isNotEmpty(ruleTypeJson.getString("status"))&&"1".equals(ruleTypeJson.getString("status"))){
                    JSONArray jsonArray = ruleTypeJson.getJSONArray("ruleTypeList");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject ruleType = jsonArray.getJSONObject(i);
                        String typeNo = ruleType.getString("ruleType");
                        typeNoList.add(typeNo);
                    }
                }
            }
        }
    }

    /**
     * {"dataProdList":[{"code":"TotalLoan","version":"V1.0"}]}
     * @param typeNoList
     */
    private void addDTBPro( List<String> typeNoList) {
         JSONArray dataProdList1 =JSONArray.parseArray(strategyStr);
         for(int j=0;j<dataProdList1.size();j++){
             JSONObject jsonObject = dataProdList1.getJSONObject(j);
             if(jsonObject!=null&&jsonObject.containsKey("code")){
                 String code = jsonObject.getString("code");
                 typeNoList.add(code);
             }
         }
    }
    /**
     * 用流失预警api的返回生成结果文件
     * @param s
     */
    private void dealResult(String s, Writer fw, Writer errorFw, String cusNum, String batchNumber, String apiCode, MarketingUser blu) throws IOException {
        try {
            if(strategyId.startsWith("DTM")&&VaildHxResultUtil.isPass(s,meal,apiCode, redisChgService,blu,errorList)){
                JSONObject resultJson=JSONObject.parseObject(s);
                if(fw!=null){
                    ResultUtil.generateFile(resultJson,strategyId,fw,sep,proFieldMap,blu,meal,cusBatchNumber,fileId,customer.getPushCustomer().toString());
                }
            }
            if(strategyId.startsWith("STRB")&&!StringUtils.isEmpty(s)){
                 JSONObject resultJson=JSONObject.parseObject(s);
                 if(StringUtils.isNotEmpty(resultJson.getString("code"))||"00".equals(resultJson.getString("code"))
                         ||"100002".equals(resultJson.getString("code"))){
                     ResultUtil.generateFile(resultJson,strategyId,fw,sep,proFieldMap,blu,meal,cusBatchNumber,fileId,customer.getPushCustomer().toString());
                 }else{
                     log.error("画像返回错误--{}",cusNum);
                     ResultUtil.generateErrorFile(resultJson,errorFw,batchNumber,sep,cusNum);
                 }
            }
        }catch (Exception e){
            log.error("dealResult出错了",e);
        }
    }

    private void dealResult(String s,Writer errorFw) throws IOException {
        if(!StringUtils.isEmpty(s)){
            errorFw.append(s + "\r\n");
        }
    }




}
