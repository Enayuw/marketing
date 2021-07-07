package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.entity.RuleField;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.Product;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.es.util.UuidUtils;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Bairong on 2019/8/21.
 *
 * 结果处理工具类，生成结果文件
 *
 */
@Slf4j
public class ResultUtil {


    public static void generateFile(JSONObject resultJson, String strategyId, Writer fw, String  sep , Map<String,String> proFieldMap, MarketingUser user, JSONObject meal, String cusBatchNumber, String fileId,Integer esOpen) throws IOException {

    public static void generateFile(JSONObject resultJson, String strategyId, Writer fw,  String  sep ,Map<String,String> proFieldMap,MarketingUser user,JSONObject meal,String cusBatchNumber,String fileId,String pushCustomer) throws IOException {
        log.info("cus_num：{} 画像流水:{}",user.getCusNum(),resultJson);

        StringBuilder sb=new StringBuilder();
        JSONObject jsonData;
        JSONObject strategyJson=new JSONObject();
        JSONObject flagJson =new JSONObject();

        JSONObject strategyInfo;
        MarketingHistory mh = new MarketingHistory();
        /**
         * 调用画像打平接口，将数据产品详情打平
         */
        JSONObject hxJson=new JSONObject();


        /**
         * 添加基本信息
         */ Date requestTime=new Date();
              sb.append(new SimpleDateFormat("yyyy-MM-dd").format(requestTime)).append(sep)
                .append(user.getBatchNumber()).append(sep)
                .append(user.getCusNum()).append(sep)
                .append(strategyId).append(sep);
              mh.setRequestTime(requestTime);
              mh.setBatchNumber(user.getBatchNumber());
              mh.setCusNum(user.getCusNum());
              mh.setApiCode(user.getApiCode());
              mh.setStrategyId(strategyId);
              if(strategyId.startsWith("STRB")){
                  String hxResult1 = resultJson.getString("hxResult");
                  if(StringUtils.isNotEmpty(hxResult1)){
                    String  hxResult = HxUtil.hauXiangFlat(hxResult1);
                      hxJson= JSONObject.parseObject(hxResult);
                  }
                  jsonData=resultJson.getJSONObject("jsonData");
                  strategyJson=resultJson.getJSONObject("loanStrategy");
                  flagJson = resultJson.getJSONObject("flag");
                  strategyInfo=jsonData.getJSONObject("strategyJson");
                  sb.append(strategyInfo.getString("useVersion")).append(sep);
                  mh.setVersion(strategyInfo.getString("useVersion"));
              }else if(strategyId.startsWith("DTM")){
                  mh.setVersion("");
                  sb.append(sep);
                  flagJson = resultJson.getJSONObject("Flag");
                  hxJson=resultJson;
              }


        /**
         * 添加风险策略信息
         */
        if(strategyId.startsWith("STRB")){
            if(!strategyJson.isEmpty()){
                sb.append(strategyJson.getString("strategyDecision")).append(sep);
                RuleField rf=new RuleField();
                String ruleType="Rule_W_SpecialList_c_mix_c";
                ruleInfo(flagJson,strategyJson,ruleType,rf.getRuleSpecialListField(),sb,sep);
                ruleType="Rule_W_InfoRelation_mix_c";
                ruleInfo(flagJson,strategyJson,ruleType,rf.getRuleInfoRelationField(),sb,sep);
                ruleType="Rule_W_ApplyLoanStr_mix_c";
                ruleInfo(flagJson,strategyJson,ruleType,rf.getRuleApplyloanstrField(),sb,sep);
                ruleType="Rule_W_ApplyLoanUsury_mix";
                ruleInfo(flagJson,strategyJson,ruleType,rf.getRuleApplyloanusuryField(),sb,sep);
                ruleType="Rule_W_ExecutionLimited_mix";
                ruleInfo(flagJson,strategyJson,ruleType,rf.getRuleExecutionlimitedField(),sb,sep);

            }

        }


        Set<String> products=new HashSet<String>();
        for(String pro:proFieldMap.keySet()){
            products.add(pro.toLowerCase());
        }
        log.info("batch_number:{} products:{}",user.getBatchNumber(),products);
        ProductResultUtil.dealProResult(hxJson,products,sb,proFieldMap,sep,user.getApiCode());
        if(log.isWarnEnabled()){
            log.warn("sb信息--"+sb.toString());
        }
        if(sb.toString().split(",").length>5){
            log.warn("sb写入fw--"+sb.toString());
            fw.append(sb + "\r\n");
            if("1".equals(pushCustomer)){
                mh.setIdCard(user.getIdCard());
                mh.setName(user.getName());
                mh.setCell(user.getCell());
                mh.setCusBatchNumber(cusBatchNumber);
                mh.setFileId(fileId);
                if(esOpen.equals(1)){
                    writeEs(mh,meal,hxJson);
                }
            }
        }
    }

    private static void writeEs(MarketingHistory mh,JSONObject meal,JSONObject hxJson){
        List<Product> list = new ArrayList<>();

        for (String product : meal.keySet()) {
            Product p =new Product();
            p.setCode(product);
            p.setVersion(meal.getJSONObject(product).getString("version"));
            p.setCodeVersion(p.getCode().concat("_").concat(p.getVersion()));
            p.setFlag(hxJson.get("flag_score")==null?"":hxJson.getString("flag_score"));
            p.setScore(new Double(hxJson.get(product)==null?0:hxJson.getDoubleValue(product)));
            list.add(p);
        }
        mh.setProduct(list);
        String id = UuidUtils.getUuid();
        MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
        service.insert(mh, id);
    }
    /**
     * 追加规则信息
     * @param flagJson
     * @param strategyJson
     * @param ruleType
     * @param ruleField
     * @param sb
     */
    private static void ruleInfo(JSONObject flagJson,JSONObject strategyJson,String ruleType,List<String> ruleField,StringBuilder sb,String sep){
        if(flagJson.containsKey(ruleType)){
            sb.append(flagJson.getString(ruleType)).append(sep);
            ReadContext context = JsonPath.parse(strategyJson);
            Object object = context.read("$..ruleArray[?(@.ruleType=='"+ruleType+"')]");
            if(object!=null){
                JSONArray array=JSONArray.parseArray(object.toString());
                if(!array.isEmpty()&&array.size()>0){
                    for(int i=0;i<array.size();i++){
                        JSONObject jsonObject = array.getJSONObject(i);
                        sb.append(jsonObject.getString("rulerisk")).append(sep);
                        sb.append(jsonObject.getString("ruleWeight")).append(sep);
                        ReadContext ruleContext = JsonPath.parse(jsonObject);
                        for(String ruleCode:ruleField){
                            Object rule = ruleContext.read("$..loanRule[?(@.ruleCode=='" + ruleCode + "')]");
                            if(rule!=null){
                                JSONArray ruleArray=JSONArray.parseArray(rule.toString());
                                if(ruleArray!=null&&ruleArray.size()>0){
                                    for(int j=0;j<ruleArray.size();j++){
                                        JSONObject jsonObject1 = ruleArray.getJSONObject(j);
                                        sb.append(jsonObject1.getString("weight")).append(sep);
                                    }
                                }else {
                                    sb.append(sep);
                                }
                            }else{
                                sb.append(sep);
                            }
                        }
                    }
                }else{
                    sb.append(sep);
                    sb.append(sep);
                    for(String ruleCode:ruleField){
                        sb.append(sep);
                    }
                }
            }else {
                sb.append(sep);
                sb.append(sep);
                for (String ruleCode : ruleField) {
                    sb.append(sep);
                }
            }
        }
    }

    public static void generateErrorFile(JSONObject resultJson,  Writer fw, String batchNumber,String sep,String cusNum) throws IOException {
        log.info("生成错误文件：{}",resultJson);
        StringBuilder sb=new StringBuilder();

        /**
         * 添加基本信息
         */
        sb.append(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append(",")
                .append(batchNumber).append(sep)
                .append(cusNum).append(sep)
                .append(resultJson.getString("code")).append(sep);
        fw.append(sb + "\r\n");
    }

}
