package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Bairong on 2020/4/21.
 */
@Slf4j
public class PpdResultUtil {


    /**
     * 生成文件
     * @param resultJson
     * {"message":"成功","swift_number":"5200155_20190924162359_68649151","hxResult":{"InfoRelation":{"allmatch_days":"308","cell_x_name_cnt":"2","id_inlistwith_cell":"1","id_x_cell_notmat_days":"432","id_x_cell_lastchg_days":"308","id_x_name_cnt":"2","id_x_mail_cnt":"1","cell_x_mail_cnt":"1","cell_x_id_cnt":"1","cell_is_reabnormal":"0","cell_inlistwith_id":"1","cell_x_id_lastchg_days":"","id_x_cell_cnt":"2","id_is_reabnormal":"0","cell_x_id_notmat_days":"","m12":{"id_x_linkman_cell_cnt":"0","cell_x_linkman_cell_cnt":"0","cell_x_name_cnt":"1","cell_x_home_addr_cnt":"0","id_x_tel_home_cnt":"0","cell_x_tel_biz_cnt":"0","id_x_name_cnt":"1","cell_x_tel_home_cnt":"0","id_x_mail_cnt":"0","cell_x_mail_cnt":"0","id_x_device_cnt":"0","cell_x_biz_addr_cnt":"0","cell_x_id_cnt":"1","id_x_home_addr_cnt":"0","id_x_cell_cnt":"1","id_x_tel_biz_cnt":"0","id_x_biz_work_cnt":"0","cell_x_device_cnt":"1","id_x_biz_addr_cnt":"0","cell_x_biz_work_cnt":"0"}},"swift_number":"4000058_20190924162400_3397","Flag":{"applyloanusury":"1","applyloanstr":"0","executionlimited":"0","specialList_c":"1","inforelation":"1"},"SpecialList_c":{"id":{"nbank_bad":"0","nbank_other_bad_time":"1","nbank_bad_time":"1","nbank_bad_allnum":"1","nbank_other_bad_allnum":"1","nbank_other_bad":"0"},"cell":{"nbank_bad":"0","nbank_other_bad_time":"1","nbank_bad_time":"1","nbank_bad_allnum":"1","nbank_other_bad_allnum":"1","nbank_other_bad":"0"},"gid":{},"lm_cell":{}},"code":"00","ApplyLoanStr":{}},"flag":{"Rule_W_InfoRelation_mix_c":"0","Rule_W_ApplyLoanUsury_mix":"0","applyloanstr":"0","applyloanusury":"1","executionlimited":"0","specialList_c":"1","Rule_W_SpecialList_c_mix_c":"1","Rule_W_ExecutionLimited_mix":"0","Rule_W_ApplyLoanStr_mix_c":"0","loanStrategy":"1","inforelation":"1"},"loanStrategy":{"ruleArray":[{"ruleType":"Rule_W_SpecialList_c_mix_c","ruleWeight":80,"loanRule":[{"weight":80,"ruleName":"近两年本人命中非银中风险","ruleCode":"SLM019","ruleKeys":["0","1","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","0","1","0","1","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","0","1"]}],"rulerisk":"C","rule_name":"贷中预警全量规则-特殊名单验证-通用客群-高级","version":"1.0"}],"strategyName":"调用贷前安卓","customerType":"早期逾期客户","prodType":"通用","strategyId":"STRB0000006","strategyDecision":"C","ruleFinalRisk":"C","version":"1.0"},"code":"00","jsonData":{"loanMaturityDate":"2020-06-13","idCard":"452622198510280026","approveResult":"1","name":"雨露","cell":"13977652939","passDate":"2017-09-18","cusNum":"12"}}
     * */
    public static void generateFile(JSONObject resultJson, JSONObject json, Writer fw,
                                     String cusNum, String batchNumber,Map<String,String> proFieldMap) throws IOException {
        log.info("生成文件：{}",resultJson);
        StringBuilder sb=new StringBuilder();
        JSONObject flagJson =new JSONObject();

        /**
         * 调用画像打平接口，将数据产品详情打平
         */
        String sep=",";

        String strategyId=json.getString("strategyId");
        /**
         * 添加基本信息
         */
        sb.append(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append(",")
                .append(batchNumber).append(",")
                .append(cusNum).append(",")
                .append(strategyId).append(",");
        if(strategyId.startsWith("DTB")){
            sb.append(",");
        }

        JSONObject hxJson=resultJson;

        log.info("cus_num：{} 画像流水:{}",cusNum,hxJson.getString("swift_number"));
        Set<String> products=new HashSet<String>();
        if(strategyId.startsWith("DTB")){
            for(String pro:proFieldMap.keySet()){
                products.add(pro.toLowerCase());
            }
        }
        dealProductInfo(fw, proFieldMap, sb, flagJson, sep, hxJson, products);
    }

    private static void dealProductInfo(Writer fw, Map<String, String> proFieldMap, StringBuilder sb, JSONObject flagJson,
                                        String sep, JSONObject hxJson, Set<String> products) throws IOException {
        /**
         * 数据产品信息
         */
        log.info("flagJson:{} products:{}",flagJson,products);
        if (!hxJson.isEmpty()) {
            if(products.contains("speciallist_c")){
                String speciallistCFields = proFieldMap.get("SpecialList_c");
                String[] split = speciallistCFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i])).append(sep);
                }
            }
            /**
             * 添加实名产品详情
             */
            if(products.contains("inforelation")){
                String inforelationFields = proFieldMap.get("InfoRelation");
                String[] split = inforelationFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i])).append(sep);
                }
            }
            /**
             * 添加借贷意向产品详情
             */
            if(products.contains("applyloanstr")){
                String applyloanstrFields = proFieldMap.get("ApplyLoanStr");
                String[] split = applyloanstrFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i])).append(sep);
                }
            }
            /**
             * 添加高风险借贷意向产品详情
             */
            if(products.contains("applyloanusury")){
                String applyloanusuryFields = proFieldMap.get("ApplyLoanUsury");
                String[] split = applyloanusuryFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i])).append(sep);
                }
            }
            /**
             * 添加法院限高产品详情
             */
            if(products.contains("executionlimited")){
                String executionlimitedFields = proFieldMap.get("ExecutionLimited");
                String[] split = executionlimitedFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i])).append(sep);
                }
            }

            /**
             * 商品消费衍生特征
             */
            if(products.contains("consumptionfeature")){
                String consumptionfeatureFields = proFieldMap.get("ConsumptionFeature");
                String[] split = consumptionfeatureFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i])).append(sep);
                }
            }
            /**
             * 消费指数
             */
            if(products.contains("netshopping")){
                String netshoppingFields = proFieldMap.get("NetShopping");
                String[] split = netshoppingFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i])).append(sep);
                }
            }

            /**
             * 客制化评分
             *   add("flag_score");
             add("scorecust");
             */
            if(products.contains("scorecust")){
                String scorecust = hxJson.getString("scorecust");
                if(StringUtils.isEmpty(scorecust)){
                    sb.append("0").append(sep).append(sep);
                }else{
                    sb.append("1").append(sep).append(scorecust).append(sep);
                }
            }
            /**
             * 客制化评分衍生变量
             */
            if(products.contains("scoredata")){
                String scoredataFields = proFieldMap.get("ScoreData");
                String[] split = scoredataFields.split(",");
                for (int i=0;i<split.length;i++){
                    if("flag_scoredata".equals(split[i])){
                        sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i])).append(sep);
                    }else{
                        sb.append(hxJson.get("sd_scorecust_"+split[i])==null?"":hxJson.get("sd_scorecust_"+split[i])).append(sep);
                    }
                }
            }

            /**
             * 客制化评分1
             */
            if(products.contains("scorecust1")){
                String scorecust = hxJson.getString("scorecust1");
                if(StringUtils.isEmpty(scorecust)){
                    sb.append("0").append(sep).append(sep);
                }else{
                    sb.append("1").append(sep).append(scorecust).append(sep);
                }
            }
            /**
             * 稳定性指数
             */
            if(products.contains("stability_c")){
                String stability_cFields = proFieldMap.get("Stability_c");
                String[] split = stability_cFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
            }
            /**
             * 借贷意向衍生特征
             */
            if(products.contains("applyfeature")){
                String applyfeatureFields = proFieldMap.get("ApplyFeature");
                String[] split = applyfeatureFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
            }
            /**
             * 借贷行为验证
             */
            if(products.contains("totalloan")){
                String totalloanFields = proFieldMap.get("TotalLoan");
                String[] split = totalloanFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
            }
            /**
             * 反欺诈风险识别-信用卡（类信用卡）
             */
            if(products.contains("scoreafrevoloan")){
                String scoreafrevoloan = hxJson.getString("scoreafrevoloan");
                if(StringUtils.isEmpty(scoreafrevoloan)){
                    sb.append("0").append(sep).append(sep);
                }else{
                    sb.append("1").append(sep).append(scoreafrevoloan).append(sep);
                }
            }
            /**
             * 客制化-信用风险识别-线上现金分期-拍拍贷老客标签一
             */
            if (products.contains("scorecashonppdlklabel1")) {
                String scoreafrevoloan = hxJson.getString("scpl1_score");
                if (StringUtils.isEmpty(scoreafrevoloan)) {
                    sb.append("0").append(",").append(",");
                } else {
                    sb.append("1").append(",").append(scoreafrevoloan).append(",");
                }
            }
            /**
             * 客制化-信用风险识别-线上现金分期-拍拍贷老客标签一点二
             */
            if (products.contains("scorecashonppdlklabel12")) {
                String scoreafrevoloan = hxJson.getString("scpl12_score");
                if (StringUtils.isEmpty(scoreafrevoloan)) {
                    sb.append("0").append(",").append(",");
                } else {
                    sb.append("1").append(",").append(scoreafrevoloan).append(",");
                }
            }
            /**
             * 客制化-信用风险识别-线上现金分期-拍拍贷老客标签一拒绝
             */
            if (products.contains("scorecashonppdlklabel1rej")) {
                String scprScore = hxJson.getString("scpr_score");
                if (StringUtils.isEmpty(scprScore)) {
                    sb.append("0").append(",").append(",");
                } else {
                    sb.append("1").append(",").append(scprScore).append(",");
                }
            }
            /**
             * 客制化-信用风险识别-线上现金分期-拍拍贷老客标签二
             */
            if (products.contains("scorecashonppdlklabel2")) {
                String scoreafrevoloan = hxJson.getString("scpl2_score");
                if (StringUtils.isEmpty(scoreafrevoloan)) {
                    sb.append("0").append(",").append(",");
                } else {
                    sb.append("1").append(",").append(scoreafrevoloan).append(",");
                }
            }
            /**
             * 客制化-信用风险识别-线上现金分期-拍拍贷老客标签一点一
             */
            if (products.contains("scorecashonppdlklabel11")) {
                String scoreafrevoloan = hxJson.getString("scpl11_score");
                if (StringUtils.isEmpty(scoreafrevoloan)) {
                    sb.append("0").append(",").append(",");
                } else {
                    sb.append("1").append(",").append(scoreafrevoloan).append(",");
                }
            }
        }
        fw.append(sb + "\r\n");
    }

}
