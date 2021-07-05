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

   // private  static String sep="\001";
    /**
     * 生成文件
     * @param resultJson
     * {
     *     "message":"成功",
     *     "swift_number":"5200155_20190924162359_68649151",
     *     "hxResult":{
     *         "InfoRelation":{
     *             "allmatch_days":"308",
     *             "cell_x_name_cnt":"2",
     *             "id_inlistwith_cell":"1",
     *             "id_x_cell_notmat_days":"432",
     *             "id_x_cell_lastchg_days":"308",
     *             "id_x_name_cnt":"2",
     *             "id_x_mail_cnt":"1",
     *             "cell_x_mail_cnt":"1",
     *             "cell_x_id_cnt":"1",
     *             "cell_is_reabnormal":"0",
     *             "cell_inlistwith_id":"1",
     *             "cell_x_id_lastchg_days":"",
     *             "id_x_cell_cnt":"2",
     *             "id_is_reabnormal":"0",
     *             "cell_x_id_notmat_days":"",
     *             "m12":{
     *                 "id_x_linkman_cell_cnt":"0",
     *                 "cell_x_linkman_cell_cnt":"0",
     *                 "cell_x_name_cnt":"1",
     *                 "cell_x_home_addr_cnt":"0",
     *                 "id_x_tel_home_cnt":"0",
     *                 "cell_x_tel_biz_cnt":"0",
     *                 "id_x_name_cnt":"1",
     *                 "cell_x_tel_home_cnt":"0",
     *                 "id_x_mail_cnt":"0",
     *                 "cell_x_mail_cnt":"0",
     *                 "id_x_device_cnt":"0",
     *                 "cell_x_biz_addr_cnt":"0",
     *                 "cell_x_id_cnt":"1",
     *                 "id_x_home_addr_cnt":"0",
     *                 "id_x_cell_cnt":"1",
     *                 "id_x_tel_biz_cnt":"0",
     *                 "id_x_biz_work_cnt":"0",
     *                 "cell_x_device_cnt":"1",
     *                 "id_x_biz_addr_cnt":"0",
     *                 "cell_x_biz_work_cnt":"0"
     *             }
     *         },
     *         "swift_number":"4000058_20190924162400_3397",
     *         "Flag":{
     *             "applyloanusury":"1",
     *             "applyloanstr":"0",
     *             "executionlimited":"0",
     *             "specialList_c":"1",
     *             "inforelation":"1"
     *         },
     *         "SpecialList_c":{
     *             "id":{
     *                 "nbank_bad":"0",
     *                 "nbank_other_bad_time":"1",
     *                 "nbank_bad_time":"1",
     *                 "nbank_bad_allnum":"1",
     *                 "nbank_other_bad_allnum":"1",
     *                 "nbank_other_bad":"0"
     *             },
     *             "cell":{
     *                 "nbank_bad":"0",
     *                 "nbank_other_bad_time":"1",
     *                 "nbank_bad_time":"1",
     *                 "nbank_bad_allnum":"1",
     *                 "nbank_other_bad_allnum":"1",
     *                 "nbank_other_bad":"0"
     *             },
     *             "gid":{
     *
     *             },
     *             "lm_cell":{
     *
     *             }
     *         },
     *         "code":"00",
     *         "ApplyLoanStr":{
     *
     *         }
     *     },
     *     "flag":{
     *         "Rule_W_InfoRelation_mix_c":"0",
     *         "Rule_W_ApplyLoanUsury_mix":"0",
     *         "applyloanstr":"0",
     *         "applyloanusury":"1",
     *         "executionlimited":"0",
     *         "specialList_c":"1",
     *         "Rule_W_SpecialList_c_mix_c":"1",
     *         "Rule_W_ExecutionLimited_mix":"0",
     *         "Rule_W_ApplyLoanStr_mix_c":"0",
     *         "loanStrategy":"1",
     *         "inforelation":"1"
     *     },
     *     "loanStrategy":{
     *         "ruleArray":[
     *             {
     *                 "ruleType":"Rule_W_SpecialList_c_mix_c",
     *                 "ruleWeight":80,
     *                 "loanRule":[
     *                     {
     *                         "weight":80,
     *                         "ruleName":"近两年本人命中非银中风险",
     *                         "ruleCode":"SLM019",
     *                         "ruleKeys":[
     *                             "0",
     *                             "1",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "0",
     *                             "1",
     *                             "0",
     *                             "1",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "tempVar",
     *                             "0",
     *                             "1"
     *                         ]
     *                     }
     *                 ],
     *                 "rulerisk":"C",
     *                 "rule_name":"贷中预警全量规则-特殊名单验证-通用客群-高级",
     *                 "version":"1.0"
     *             }
     *         ],
     *         "strategyName":"调用贷前安卓",
     *         "customerType":"早期逾期客户",
     *         "prodType":"通用",
     *         "strategyId":"STRB0000006",
     *         "strategyDecision":"C",
     *         "ruleFinalRisk":"C",
     *         "version":"1.0"
     *     },
     *     "code":"00",
     *     "jsonData":{
     *         "loanMaturityDate":"2020-06-13",
     *         "idCard":"452622198510280026",
     *         "approveResult":"1",
     *         "name":"雨露",
     *         "cell":"13977652939",
     *         "passDate":"2017-09-18",
     *         "cusNum":"12"
     *     }
     * }
     * * */
    public static void generateFile(JSONObject resultJson, String strategyId, Writer fw, String  sep , Map<String,String> proFieldMap, MarketingUser user, JSONObject meal, String cusBatchNumber, String fileId, AtomicLong desTime,Integer esOpen) throws IOException {
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
        if(sb.toString().split(",").length>5){
            fw.append(sb + "\r\n");
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
