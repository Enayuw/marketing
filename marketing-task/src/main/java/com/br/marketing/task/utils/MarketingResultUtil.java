package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Bairong on 2020/4/21.
 */
@Slf4j
public class MarketingResultUtil {


    /**
     * 生成文件
     * @param resultJson
     * {"message":"成功","swift_number":"5200155_20190924162359_68649151","hxResult":{"InfoRelation":{"allmatch_days":"308","cell_x_name_cnt":"2","id_inlistwith_cell":"1","id_x_cell_notmat_days":"432","id_x_cell_lastchg_days":"308","id_x_name_cnt":"2","id_x_mail_cnt":"1","cell_x_mail_cnt":"1","cell_x_id_cnt":"1","cell_is_reabnormal":"0","cell_inlistwith_id":"1","cell_x_id_lastchg_days":"","id_x_cell_cnt":"2","id_is_reabnormal":"0","cell_x_id_notmat_days":"","m12":{"id_x_linkman_cell_cnt":"0","cell_x_linkman_cell_cnt":"0","cell_x_name_cnt":"1","cell_x_home_addr_cnt":"0","id_x_tel_home_cnt":"0","cell_x_tel_biz_cnt":"0","id_x_name_cnt":"1","cell_x_tel_home_cnt":"0","id_x_mail_cnt":"0","cell_x_mail_cnt":"0","id_x_device_cnt":"0","cell_x_biz_addr_cnt":"0","cell_x_id_cnt":"1","id_x_home_addr_cnt":"0","id_x_cell_cnt":"1","id_x_tel_biz_cnt":"0","id_x_biz_work_cnt":"0","cell_x_device_cnt":"1","id_x_biz_addr_cnt":"0","cell_x_biz_work_cnt":"0"}},"swift_number":"4000058_20190924162400_3397","Flag":{"applyloanusury":"1","applyloanstr":"0","executionlimited":"0","specialList_c":"1","inforelation":"1"},"SpecialList_c":{"id":{"nbank_bad":"0","nbank_other_bad_time":"1","nbank_bad_time":"1","nbank_bad_allnum":"1","nbank_other_bad_allnum":"1","nbank_other_bad":"0"},"cell":{"nbank_bad":"0","nbank_other_bad_time":"1","nbank_bad_time":"1","nbank_bad_allnum":"1","nbank_other_bad_allnum":"1","nbank_other_bad":"0"},"gid":{},"lm_cell":{}},"code":"00","ApplyLoanStr":{}},"flag":{"Rule_W_InfoRelation_mix_c":"0","Rule_W_ApplyLoanUsury_mix":"0","applyloanstr":"0","applyloanusury":"1","executionlimited":"0","specialList_c":"1","Rule_W_SpecialList_c_mix_c":"1","Rule_W_ExecutionLimited_mix":"0","Rule_W_ApplyLoanStr_mix_c":"0","loanStrategy":"1","inforelation":"1"},"loanStrategy":{"ruleArray":[{"ruleType":"Rule_W_SpecialList_c_mix_c","ruleWeight":80,"loanRule":[{"weight":80,"ruleName":"近两年本人命中非银中风险","ruleCode":"SLM019","ruleKeys":["0","1","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","0","1","0","1","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","0","1"]}],"rulerisk":"C","rule_name":"贷中预警全量规则-特殊名单验证-通用客群-高级","version":"1.0"}],"strategyName":"调用贷前安卓","customerType":"早期逾期客户","prodType":"通用","strategyId":"STRB0000006","strategyDecision":"C","ruleFinalRisk":"C","version":"1.0"},"code":"00","jsonData":{"loanMaturityDate":"2020-06-13","idCard":"452622198510280026","approveResult":"1","name":"雨露","cell":"13977652939","passDate":"2017-09-18","cusNum":"12"}}
     * */
    public static void generateFile(JSONObject resultJson, Map<String,Writer> writerMap,JSONObject json,
                                     String cusNum, String batchNumber,Map<String,String> proFieldMap) throws IOException {
        log.info("生成文件：{}",resultJson);
        StringBuilder sb=new StringBuilder();
        JSONObject flagJson =new JSONObject();
        /**
         * 调用画像打平接口，将数据产品详情打平
         */
        String sep=json.getString("sep");
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
        for(String product:writerMap.keySet()){
            StringBuilder sbPro=new StringBuilder();
            sbPro.append(sb);
            Writer fw = writerMap.get(product);
            dealProductInfo(fw, proFieldMap, sbPro, flagJson, sep, hxJson, product);
        }
    }

    private static void dealProductInfo(Writer fw, Map<String, String> proFieldMap, StringBuilder sb, JSONObject flagJson,
                                        String sep, JSONObject hxJson, String product) throws IOException {
        /**
         * 数据产品信息
         */
        log.info("flagJson:{} product:{}",flagJson,product);
        if (!hxJson.isEmpty()) {
            if("scoremcashon360xkone".equals(product)){
                String scoremcashon360xkone = hxJson.getString("scoremcashon360xkone");
                if(StringUtils.isNotEmpty(scoremcashon360xkone)){
                    sb.append("1").append(sep);
                }else {
                    sb.append("0").append(sep);
                }
                sb.append(scoremcashon360xkone).append(sep);
            }
            if("scoremcashon360xktwo".equals(product)){
                String scoremcashon360xktwo = hxJson.getString("scoremcashon360xktwo");
                if(StringUtils.isNotEmpty(scoremcashon360xktwo)){
                    sb.append("1").append(sep);
                }else {
                    sb.append("0").append(sep);
                }
                sb.append(scoremcashon360xktwo).append(sep);
            }
        }
        fw.append(sb + "\r\n");
    }

}
