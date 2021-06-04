package com.br.marketing.task.utils;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Bairong on 2020/5/8.
 */
@Slf4j
public class ChgResultUtil {

    /**
     *
     * @param resultJson 画像结果
     * {"message":"成功","swift_number":"5200155_20190924162359_68649151","hxResult":{"InfoRelation":{"allmatch_days":"308","cell_x_name_cnt":"2","id_inlistwith_cell":"1","id_x_cell_notmat_days":"432","id_x_cell_lastchg_days":"308","id_x_name_cnt":"2","id_x_mail_cnt":"1","cell_x_mail_cnt":"1","cell_x_id_cnt":"1","cell_is_reabnormal":"0","cell_inlistwith_id":"1","cell_x_id_lastchg_days":"","id_x_cell_cnt":"2","id_is_reabnormal":"0","cell_x_id_notmat_days":"","m12":{"id_x_linkman_cell_cnt":"0","cell_x_linkman_cell_cnt":"0","cell_x_name_cnt":"1","cell_x_home_addr_cnt":"0","id_x_tel_home_cnt":"0","cell_x_tel_biz_cnt":"0","id_x_name_cnt":"1","cell_x_tel_home_cnt":"0","id_x_mail_cnt":"0","cell_x_mail_cnt":"0","id_x_device_cnt":"0","cell_x_biz_addr_cnt":"0","cell_x_id_cnt":"1","id_x_home_addr_cnt":"0","id_x_cell_cnt":"1","id_x_tel_biz_cnt":"0","id_x_biz_work_cnt":"0","cell_x_device_cnt":"1","id_x_biz_addr_cnt":"0","cell_x_biz_work_cnt":"0"}},"swift_number":"4000058_20190924162400_3397","Flag":{"applyloanusury":"1","applyloanstr":"0","executionlimited":"0","specialList_c":"1","inforelation":"1"},"SpecialList_c":{"id":{"nbank_bad":"0","nbank_other_bad_time":"1","nbank_bad_time":"1","nbank_bad_allnum":"1","nbank_other_bad_allnum":"1","nbank_other_bad":"0"},"cell":{"nbank_bad":"0","nbank_other_bad_time":"1","nbank_bad_time":"1","nbank_bad_allnum":"1","nbank_other_bad_allnum":"1","nbank_other_bad":"0"},"gid":{},"lm_cell":{}},"code":"00","ApplyLoanStr":{}},"flag":{"Rule_W_InfoRelation_mix_c":"0","Rule_W_ApplyLoanUsury_mix":"0","applyloanstr":"0","applyloanusury":"1","executionlimited":"0","specialList_c":"1","Rule_W_SpecialList_c_mix_c":"1","Rule_W_ExecutionLimited_mix":"0","Rule_W_ApplyLoanStr_mix_c":"0","loanStrategy":"1","inforelation":"1"},"loanStrategy":{"ruleArray":[{"ruleType":"Rule_W_SpecialList_c_mix_c","ruleWeight":80,"loanRule":[{"weight":80,"ruleName":"近两年本人命中非银中风险","ruleCode":"SLM019","ruleKeys":["0","1","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","0","1","0","1","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","tempVar","0","1"]}],"rulerisk":"C","rule_name":"贷中预警全量规则-特殊名单验证-通用客群-高级","version":"1.0"}],"strategyName":"调用贷前安卓","customerType":"早期逾期客户","prodType":"通用","strategyId":"STRB0000006","strategyDecision":"C","ruleFinalRisk":"C","version":"1.0"},"code":"00","jsonData":{"loanMaturityDate":"2020-06-13","idCard":"452622198510280026","approveResult":"1","name":"雨露","cell":"13977652939","passDate":"2017-09-18","cusNum":"12"}}
     * @param fw 结果数据输出流
     * @param noChgFw 无变动数据输出流
     * @param apiCode
     * @param cusNum 客户编号
     * @param batchNumber 批次号
     * @param strategyId 策略编号
     * @param flag 是否是客户新上传的增量数据
     * @param redisChgService 字段比对redis集群
     * @param sep 数据分隔符
     * @param proFieldMap 数据产品以及输出字段映射
     * @param isCycle 是否是返回全量周期日
     * @throws IOException
     */
    public static void generateFile(JSONObject resultJson, Writer fw, Writer noChgFw, String apiCode, String cusNum, String batchNumber, String strategyId,
                                    boolean flag, RedisChgService redisChgService, String sep, Map<String,String> proFieldMap,boolean isCycle) throws IOException {
        log.info("cus_num：{} 画像流水:{}",cusNum,resultJson.getString("swift_number"));
        StringBuilder chgSb = new StringBuilder();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        /**
         * 添加特殊名单产品详情
         */
            String speciallistCFields = proFieldMap.get("SpecialList_c");
            String[] split = speciallistCFields.split(",");
            for (int i=0;i<split.length;i++){
                chgSb.append(resultJson.get(split[i])==null?"":resultJson.get(split[i]));
                chgSb.append(Constants.sepMap.get(2));
            }
        /**
         * 添加借贷意向产品详情
         */
            String applyloanstrFields = PropertiesUtil.getProperty("ApplyLoanStrChg");
            String[] split1 = applyloanstrFields.split(",");
            for (int i=0;i<split1.length;i++){
                chgSb.append(resultJson.get(split1[i])==null?"":resultJson.get(split1[i]));
                chgSb.append(Constants.sepMap.get(2));
            }
        /**
         * 添加法院限高产品详情
         */
            String executionlimitedFields = proFieldMap.get("ExecutionLimited");
            String[] split2 = executionlimitedFields.split(",");
            for (int i=0;i<split2.length;i++){
                chgSb.append(resultJson.get(split2[i])==null?"":resultJson.get(split2[i]));
                chgSb.append(Constants.sepMap.get(2));
            }
           log.info("对比字段结果：{}",chgSb);


        /**
         * 字段变动对比逻辑：
         * 增量数据（新上传的未查询过的数据）：
         *  1.客户新上传的增量的数据不做对比，查询结束后将新的结果set到redis。
         * 存量数据（上传后至少已经查询过一次的数据）：
         *  2.从redis查询为空的用户，直接输出并更新查询结果到redis。
         *  3.redis中记录的用户的hash值后面的日期如果是当天，证明今天已确认有变动。可能当前是重新跑数，按有变动输出到客户的结果文件，不用再次更新redis。
         *  4.redis中记录的用户的hash值后面的日期不是当天且小于当天，再对比当天结果的hash值和redis中的hash。不同则证明有变动，输出到客户的结果文件，并且刷新redis中的结果
         * 相同证明当前用户没有变动，不做处理。
         * 5.根据批次的监控开始时间，T+7返回一次全量的结果。比对的逻辑保持不变，同样也会更新redis中的数据，只是将无变动的数据也输出到结果文件中。
         *
         */
        try{
            String md5String = SecureUtil.md5(chgSb.toString());
            String key= Constants.LOAN_WARNING_CHF_KEY+apiCode+"_"+batchNumber+"_"+cusNum;
            String cntKey= Constants.LOAN_WARNING_CHF_CNT_KEY+apiCode+"_"+batchNumber+"_"+today;
            String s2 = dealResult(resultJson, strategyId, cusNum, batchNumber, sep, proFieldMap);
            if(flag){
                String s = redisChgService.get(key);
                if(StringUtils.isNotEmpty(s)){
                    String[] split3 = s.split(":");
                    String s1 = split3[1];
                    if(today.equals(s1)&&!isCycle){
                        fw.append(s2 + "\r\n");
                        redisChgService.incrBy(cntKey,1);
                        log.info("重跑有变动--{}---{}",key,s);
                    }else{
                        if(!md5String.equals(split3[0])){
                            redisChgService.set(key,md5String+":"+today);
                            fw.append(s2 + "\r\n");
                            redisChgService.incrBy(cntKey,1);
                            log.info("正常处理有变动--{}---{}",key,s);

                        }else{
                            log.info("无变动--{}---{}",key,md5String+":"+today);
                            /**
                             * 如果是到了7天的周期日，无变动的部分也一起输出到结果文件中
                             * T+7返回一次全量
                             */
                            if(isCycle){
                                fw.append(s2 + "\r\n");
                                redisChgService.incrBy(cntKey,1);
                            }else {
                                noChgFw.append(s2 + "\r\n");
                            }
                        }
                    }
                }else{
                    redisChgService.set(key,md5String+":"+today);
                    fw.append(s2 + "\r\n");
                    redisChgService.incrBy(cntKey,1);
                    log.info("redis中无对应key--{}---{}",key,md5String+":"+today);
                }
            }else{
                redisChgService.set(key,md5String+":"+today);
                fw.append(s2 + "\r\n");
                redisChgService.incrBy(cntKey,1);
                log.info("新增数据--{}---{}",key,md5String+":"+today);
            }
        }catch (Exception e){
            log.error("360变动数据处理出错",e);
        }








    }

    private static  String dealResult(JSONObject resultJson,  String strategyId, String cusNum, String batchNumber, String sep, Map<String,String> proFieldMap){
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        StringBuilder sb = new StringBuilder();
        /**
         * 添加基本信息
         * 日期、批次号不纳入对比的字段中
         * 不纳入批次号，按客户账号下的所有样本去对比
         */

        sb.append(today).append(sep)
                .append(batchNumber).append(sep);


        sb.append(cusNum).append(sep)
                .append(strategyId).append(sep);
        if (strategyId.startsWith("DTB")) {
            sb.append(sep);
        }

        Set<String> products=new HashSet<String>();
        for(String pro:proFieldMap.keySet()){
            products.add(pro.toLowerCase());
        }
        log.info(" products:{}",products);
        /**
         * 添加数据产品信息
         */
        ProductResultUtil.dealProResult(resultJson,products,sb,proFieldMap,sep,Constants.APICODE_360);
        return sb.toString();
    }
}

