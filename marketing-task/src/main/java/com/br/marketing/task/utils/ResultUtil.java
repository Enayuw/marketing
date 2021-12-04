package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.Product;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.vo.StrategyProductDetailVO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Bairong on 2019/8/21.
 *
 * 结果处理工具类，生成结果文件
 *
 */
@Slf4j
public class ResultUtil {


    public static void generateFile(JSONObject resultJson, String strategyId, Writer fw, String  sep , Map<String,String> proFieldMap,
                                    MarketingUser user, JSONObject meal, String cusBatchNumber, String fileId,String pushCustomer,
                                    String baseHeadInfo,StrategyProductDetailVO fieldInfo) throws IOException {
        log.info("cus_num：{} 画像流水:{}",user.getCusNum(),resultJson);
        JSONObject esResult=new JSONObject();
        StringBuilder sb=new StringBuilder();
        MarketingHistory mh = new MarketingHistory();
        /**
         * 添加基本信息
         */ Date requestTime=new Date();
              sb.append(new SimpleDateFormat("yyyy-MM-dd").format(requestTime)).append(sep)
                .append(user.getBatchNumber()).append(sep)
                .append(user.getCusNum()).append(sep)
                .append(strategyId).append(sep).append(sep);
              mh.setRequestTime(requestTime);
              mh.setBatchNumber(user.getBatchNumber());
              mh.setCusNum(user.getCusNum());
              mh.setApiCode(user.getApiCode());
              mh.setStrategyId(strategyId);
              mh.setVersion("");
            if(StringUtils.isNotBlank(baseHeadInfo)){
                JSONObject jsonObject = null;
                if(StringUtils.isNotBlank(user.getExtendJson())){
                    try {
                        jsonObject = JSONObject.parseObject(user.getExtendJson());
                    }catch (Exception ex){
                        log.error("跑分扩展信息解析有误 apiCode:{},id:{}",user.getApiCode(),user.getId());
                    }
                }
                for (String s : baseHeadInfo.split(",")) {
                    if(jsonObject!=null){
                        String ss = jsonObject.getString(s);
                        if(StringUtils.isNotBlank(ss)){
                            sb.append(ss);
                        }
                    }
                    sb.append(sep);
                }
            }
        Set<String> products=new HashSet<String>();
        for(String pro:proFieldMap.keySet()){
            products.add(pro.toLowerCase());
        }
        log.info("batch_number:{} products:{}",user.getBatchNumber(),products);

         buildResult(resultJson, sb, sep,esResult,fieldInfo);

        if(log.isInfoEnabled()){
            log.info("sb信息--"+sb.toString());
        }
        //if(countStr(sb.toString(),sep)>5){
        fw.append(sb + "\r\n");
        if("1".equals(pushCustomer)){
            mh.setIdCard(user.getIdCard());
            mh.setName(user.getName());
            mh.setCell(user.getCell());
            mh.setCusBatchNumber(cusBatchNumber);
            mh.setBatchNumber(user.getBatchNumber());
            mh.setFileId(fileId);
            mh.setReserveField(esResult.toJSONString());
            mh.setTaskId(user.getTaskId());
            mh.setUserType(user.getUserType());
            writeEs(mh,meal,resultJson);
        }
        //}
    }

      static Result buildResult(JSONObject hxJson, StringBuilder sb,String sep,JSONObject esResult,StrategyProductDetailVO fieldInfo) {
        StringBuilder result=new StringBuilder();
        if(fieldInfo == null){
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        for (int i = 0; i < fieldInfo.getFields().size(); i++) {
            String field = fieldInfo.getFields().get(i);
            String fieldRes = hxJson.getString(field);
            result.append(StringUtils.isNotBlank(fieldRes)?fieldRes:"").append(sep);
            esResult.put(field,StringUtils.isNotBlank(fieldRes)?fieldRes:"");
        }
        sb.append(result);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private static void writeEs(MarketingHistory mh,JSONObject meal,JSONObject hxJson){
        List<Product> list = new ArrayList<>();

        for (String product : meal.keySet()) {
            Product p =new Product();
            p.setCode(product);
            p.setVersion(meal.getJSONObject(product).getString("version"));
            p.setCodeVersion(p.getCode().concat("_").concat(p.getVersion()));
            p.setFlag(hxJson.get("flag_score")==null?"":hxJson.getString("flag_score"));
            p.setScore(new Double(hxJson.get(product.toLowerCase())==null?0:hxJson.getDoubleValue(product.toLowerCase())));
            list.add(p);
        }
        mh.setProduct(list);
        String id = UuidUtils.getUuid();
        MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
        service.insert(mh, id);
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
    private static int countStr(String str, String sToFind) {
        int num = 0;
        int len1 = str.length();
        String str1 = str.replaceAll(sToFind, "");
        int len2 = str1.length();
        num = len1 - len2;
        return num;
    }
}
