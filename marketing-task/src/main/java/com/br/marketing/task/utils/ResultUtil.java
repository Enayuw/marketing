package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.vo.BaseHead;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.StrategyProductDetailVO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
                BaseHeadConfigVO o = JSON.parseObject(baseHeadInfo, new TypeReference<BaseHeadConfigVO>() {
                }.getType());
                Map<String, Integer> headMap = o.getBaseHead().stream().collect(Collectors.toMap(BaseHead::getName, BaseHead::getType));
                for (String s : o.getShowBaseHead()) {
                    if(jsonObject!=null){
                        String ss = jsonObject.getString(s);
                        if(StringUtils.isNotBlank(ss)){
                            sb.append(ss);
                        }
                        Integer tp = headMap.get(s);
                        if(tp!=null &&tp!= 1){
                            esResult.put(s,ss);
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
        fw.append(sb + "\r\n");
        if("1".equals(pushCustomer)){
            mh.setIdCard(user.getIdCard());
            mh.setName(user.getName());
            mh.setCell(user.getCell());
            mh.setCusBatchNumber(cusBatchNumber);
            mh.setBatchNumber(user.getBatchNumber());
            mh.setFileId(fileId);
            mh.setTaskId(user.getTaskId());
            mh.setUserType(user.getUserType());
            mh.setHxSwiftNumber(StringUtils.isNotBlank(resultJson.getString("swift_number"))?resultJson.getString("swift_number"):"");
            //region 写入condition
            HashMap<String,MarketingCondition> conditions = new HashMap<>();
            List<MarketingCondition> conditionList = new ArrayList<>();
            for (String product : meal.keySet()) {
                MarketingCondition marketingCondition = new MarketingCondition();
                marketingCondition.setCode(product);
                marketingCondition.setVersion(meal.getJSONObject(product).getString("version"));
                conditions.put(product.toLowerCase(),marketingCondition);
            }
            for (String s : esResult.keySet()) {
                MarketingCondition marketingCondition = conditions.get(s);
                if(marketingCondition!=null){
                    marketingCondition.setFlag(resultJson.get("flag_score")==null?"":resultJson.getString("flag_score"));
                    marketingCondition.setFieldKey(s);
                    marketingCondition.setDValue(esResult.get(s)==null?0:Double.valueOf(esResult.getString(s)));
                    marketingCondition.setStrValue("");
                    conditionList.add(marketingCondition);
                }else{
                    MarketingCondition marketingConditionStr = new MarketingCondition();
                    marketingConditionStr.setFieldKey(s);
                    marketingConditionStr.setStrValue(esResult.getString(s));
                    conditionList.add(marketingConditionStr);
                }
            }
            mh.setCondition(conditionList);
            mh.setReserveField(esResult.toJSONString());
            //endregion
            String id = UuidUtils.getUuid();
            MarketingHistoryEsServiceImpl service = new MarketingHistoryEsServiceImpl();
            service.insert(mh, id);
        }
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
