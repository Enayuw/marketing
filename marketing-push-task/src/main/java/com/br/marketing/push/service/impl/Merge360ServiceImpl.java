package com.br.marketing.push.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.client.DtbStrategyClient;
import com.br.marketing.client.ProFieldsClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.TaskStatus;
import com.br.marketing.entity.ProductField;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.push.PushApplication;
import com.br.marketing.push.service.MergeService;
import com.br.marketing.push.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * //				    _ooOoo_
 * //				   o8888888o
 * //				   88" . "88
 * //				   (| -_- |)
 * //				   O\  =  /O
 * //			    ____/`---'\____
 * //			  .'  \\|     |//  `.
 * //		     /  \\|||  :  |||//  \
 * //		    /  _|||||--:--|||||_  \
 * //		    | / | \\\  -  /// | \ |
 * //		    | \_|  ''\-:-/''  |_/ |
 * //		    \  .-\__  `-`  ___/-. /
 * //		  ___`...'  /--.--\  '...`___
 * //	   ."" '< `.___\_<|>_/___.'  >' "".
 * //	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 * //	    \ \ `-.  \_ __\ /__ _/  .-` / /
 * // ======`-.____`-.____\____/.-`____.-`======
 * //				    `=---='
 * //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //			  Buddha Bless, No Bug !
 *
 * @Author xiaoxin.pang
 * @Date 2021/5/7 13:35
 * @Description:
 **/
@Service
@Slf4j
public class Merge360ServiceImpl implements MergeService {

    @Resource
    LoanFileMapper loanFileMapper;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    TaskStatusMapper taskStatusMapper;
    @Resource
    DtbStrategyClient dtbStrategyClient;
    @Resource
    ProFieldsClient proFieldsClient;
    @Override
    public  List<LoanFile> process(String apiCode) {
        List<LoanFile> allList=new ArrayList<>();
        try{
            List<String> apiCodes=loanFileMapper.queryApiCodes();
            for(String str:apiCodes){
                if(apiCode.equals(str)){

                    initBatchNumList(allList,str);
                    for(LoanFile blf:allList){
                        List<String> strings = mergeResultFile(blf,2000);
                        blf.setZipFileNames(strings);
                    }
                }
            }
        }catch (Exception e){
            log.error("error-----",e);
        }
        return allList;
    }

    private List<String> mergeResultFile(LoanFile blf, int max) {
        List<String> zipFileList=new ArrayList<>();
        try{
            StringBuilder targetPath=new StringBuilder();
            targetPath.append(blf.getFilePath())
                    .append("/");
            MarketingTask blt = marketingTaskMapper.queryBlt(blf.getBatchNumber());

            TaskStatus bts= taskStatusMapper.queryNewestBts(blf.getBatchNumber());
            String fileName = blt.getFileName();
            fileName=fileName.replace(".txt","");
            String s = fileName.split("_")[1];
            String startTime = bts.getCreateTime();
            if(!StringUtils.isEmpty(startTime)){
                startTime=startTime.split(" ")[0];
                startTime=startTime.replace("-","");
            }else{
                startTime=  DateHelper.getDateAddYyMmDd(0);;
            }
            String strategyId=blt.getStrategyId();
            StringBuilder head= new StringBuilder();
            Integer sep= marketingTaskMapper.querySep(blt.getApiCode());
            String separator= Constants.sepMap.get(sep);
            initHead(head,blt.getApiCode(),strategyId,separator);
            zipFileList= FileUtil.merge360(head.toString(),s,
                    targetPath.toString(),startTime,blf.getBatchNumber(),strategyId,blf.getApiCode(),max,separator);

        }catch (Exception e){
            log.error("合并文件出错",e);
        }
        return zipFileList;
    }

    /**
     * 初始化表头
     * @param head
     * @param apiCode
     * @param strategyId
     *
     * .append("姓名").append(",").append("身份证号").append(",").append("证书号").append(",").append("手机号")
    .append(",")
     */
    private void  initHead(StringBuilder head,String apiCode,String strategyId,String sep){
        Map<String, String> proMap = getAllLoanProductsVersion(apiCode, strategyId.split(":")[0]);
        head.append("request_time").append(sep).append("batch_number").append(sep).append("cus_num")
                .append(sep).append("strategy_id").append(sep).append("version").append(sep);
        Set<String> products=new HashSet<>();
        if(strategyId.startsWith("DTB")){
            products=proMap.keySet();
        }
        String stmtKey="";
        log.warn("需要返回的数据产品--{}",products);
        ProductField pf=new ProductField();
        if(products.contains("speciallist_c")){
            String specialListc = proFieldsClient.getProFields("SpecialList_c", proMap.get("speciallist_c"), apiCode,stmtKey);
            if(StringUtils.isEmpty(specialListc)){
                List<String> specialListField = pf.getSpecialListField();
                for(String field:specialListField){
                    head.append(field).append(sep);
                }
            }else{
                JSONArray array=JSONArray.parseArray(specialListc);
                for (int i=0;i<array.size();i++){
                    String field=array.getString(i);
                    head.append(field).append(sep);
                }
            }
        }
        if(products.contains("inforelation")){
            String inforelation = proFieldsClient.getProFields("InfoRelation", proMap.get("inforelation"), apiCode,stmtKey);
            if(StringUtils.isEmpty(inforelation)){
                List<String> infoRelationField = pf.getInfoRelationField();
                for(String field:infoRelationField){
                    head.append(field).append(sep);
                }
            }else {
                JSONArray array=JSONArray.parseArray(inforelation);
                for (int i=0;i<array.size();i++){
                    String field=array.getString(i);
                    head.append(field).append(sep);
                }
            }
        }
        if(products.contains("applyloanstr")){
            String applyloanstr = proFieldsClient.getProFields("ApplyLoanStr", proMap.get("applyloanstr"), apiCode,stmtKey);
            if(StringUtils.isEmpty(applyloanstr)){
                List<String> applyloanstrField = pf.getApplyloanstrField();
                for(String field:applyloanstrField){
                    head.append(field).append(sep);
                }
            }else{
                JSONArray array=JSONArray.parseArray(applyloanstr);
                for (int i=0;i<array.size();i++){
                    String field=array.getString(i);
                    head.append(field).append(sep);
                }
            }
        }
        if(products.contains("applyloanusury")){
            String applyloanusury = proFieldsClient.getProFields("ApplyLoanUsury", proMap.get("applyloanusury"), apiCode,stmtKey);
            if(StringUtils.isEmpty(applyloanusury)){
                List<String> applyloanusuryField = pf.getApplyloanusuryField();
                for(String field:applyloanusuryField){
                    head.append(field).append(sep);
                }
            }else{
                JSONArray array=JSONArray.parseArray(applyloanusury);
                for (int i=0;i<array.size();i++){
                    String field=array.getString(i);
                    head.append(field).append(sep);
                }
            }
        }
        if(products.contains("executionlimited")){
            String executionlimited = proFieldsClient.getProFields("ExecutionLimited", proMap.get("executionlimited"), apiCode,stmtKey);
            if(StringUtils.isEmpty(executionlimited)){
                List<String> executionlimitedField = pf.getExecutionlimitedField();
                for(String field:executionlimitedField){
                    head.append(field).append(sep);
                }
            }else {
                JSONArray array=JSONArray.parseArray(executionlimited);
                for (int i=0;i<array.size();i++){
                    String field=array.getString(i);
                    head.append(field).append(sep);
                }
            }
        }
        if(products.contains("consumptionfeature")){
            String consumptionFeature = proFieldsClient.getProFields("ConsumptionFeature", proMap.get("consumptionfeature"), apiCode,stmtKey);
            if(StringUtils.isEmpty(consumptionFeature)){
                List<String> consumptionFeatureField = pf.getConsumptionFeatureField();
                for(String field:consumptionFeatureField){
                    head.append(field).append(sep);
                }
            }else{
                JSONArray array=JSONArray.parseArray(consumptionFeature);
                for (int i=0;i<array.size();i++){
                    String field=array.getString(i);
                    head.append(field).append(sep);
                }
            }
        }
        if(products.contains("netshopping")){
            String netshopping = proFieldsClient.getProFields("NetShopping", proMap.get("netshopping"), apiCode,stmtKey);
            if(StringUtils.isEmpty(netshopping)){
                List<String> netshoppingField = pf.getNetshoppingField();
                for(String field:netshoppingField){
                    head.append(field).append(sep);
                }
            }else {
                JSONArray array=JSONArray.parseArray(netshopping);
                for (int i=0;i<array.size();i++){
                    String field=array.getString(i);
                    head.append(field).append(sep);
                }
            }
        }
        initHead1(head, apiCode, sep, proMap, products, stmtKey, pf);
    }

    private void initHead1(StringBuilder head, String apiCode, String sep, Map<String, String> proMap,
                           Set<String> products, String stmtKey, ProductField pf) {
        if(products.contains("scorecust")){
            List<String> scoreField = pf.getScoreField();
            for(String field:scoreField){
                head.append(field).append(sep);
            }
        }
        if(products.contains("scoredata")){
            head.append("flag_scoredata").append(sep);
            List<String> scoredataField = pf.getScoredataField();
            for(String field:scoredataField){
                head.append(field).append(sep);
            }
        }
        if(products.contains("scorecust1")){
            List<String> scoreField = pf.getScore1Field();
            for(String field:scoreField){
                head.append(field).append(sep);
            }
        }
        if(products.contains("stability_c")){
            String stabilityc = proFieldsClient.getProFields("Stability_c", proMap.get("stability_c"), apiCode,stmtKey);
            if(StringUtils.isEmpty(stabilityc)){
                List<String> stability = pf.getStabilitField();
                for (String field:stability){
                    head.append(field);
                    head.append(sep);
                }
            }else{
                JSONArray array=JSONArray.parseArray(stabilityc);
                for (int i=0;i<array.size();i++){
                    String field=array.getString(i);
                    head.append(field).append(sep);
                }
            }
        }
        if(products.contains("applyfeature")){
            String applyFeature = proFieldsClient.getProFields("ApplyFeature", proMap.get("applyfeature"), apiCode,stmtKey);
            if(StringUtils.isEmpty(applyFeature)){
                List<String> applyFeatures = pf.getApplyFeatureField();
                for (String field:applyFeatures){
                    head.append(field);
                    head.append(sep);
                }
            }else{
                JSONArray array=JSONArray.parseArray(applyFeature);
                for (int i=0;i<array.size();i++){
                    String field=array.getString(i);
                    head.append(field).append(sep);
                }
            }
        }
        if(products.contains("totalloan")){
            String totalLoan = proFieldsClient.getProFields("TotalLoan", proMap.get("totalloan"), apiCode,stmtKey);
            if(StringUtils.isEmpty(totalLoan)){
                List<String> totalloan = pf.getTotalloanField();
                for (String field:totalloan){
                    head.append(field);
                    head.append(sep);
                }
            }else {
                JSONArray array=JSONArray.parseArray(totalLoan);
                for (int i=0;i<array.size();i++){
                    String field=array.getString(i);
                    head.append(field).append(sep);
                }
            }
        }
        if(products.contains("scoreafrevoloan")){
            List<String> scoreafrevoloan = pf.getScoreafrevoloanField();
            for (String field:scoreafrevoloan){
                head.append(field);
                head.append(sep);
            }
        }
    }
    /**
     * 从redis中获取策略贷中的全部产品信息
     * @return
     *
     */
    public   Map<String,String> getAllLoanProductsVersion(String apiCode,String strategyid) {
        Map<String,String> proMap=new HashMap<>();
        if(strategyid.startsWith("DTB")){
            dtbStrategyClient.needReturnProduct(apiCode,strategyid,proMap);
        }
        return proMap;
    }
    /**
     * 初始化当日需要监控的任务信息，并将增量监控和全量监控区分开来
     * @param allList
     */
    private void initBatchNumList(List<LoanFile> allList, String apiCode){

        List<LoanFile> list= loanFileMapper.queryFile(apiCode);
        for(LoanFile blf :list){
            if(blf.getType()==1){
                allList.add(blf);
            }
        }
        log.info("[PUSH] all size:{}",allList.size());
    }
}
