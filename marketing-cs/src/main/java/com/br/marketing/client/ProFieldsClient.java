package com.br.marketing.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.PropertiesUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.ProInSys;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class ProFieldsClient {
    @Resource
    RedisService redisService;
    @Resource
    RedisTemplate redisTemplate;
    @Value("${otherConfig.warning.ruleList:00}")
    private String rules;


    /**
     * 设置贷中产品
     * @param strategyId 策略id
     * @param apiCode
     * @param strategyStr 策略字符串
     * @param meal
     * @param proFieldMap
     * @param stmtKey
     */
    public void setLoanPro(String strategyId,String apiCode,String strategyStr,JSONObject meal,Map<String,String> proFieldMap,String stmtKey){
        JSONArray loanProAray=new JSONArray();
        if(strategyId.startsWith("STRB")){
            try{
                //获取贷中产品
                String loanProInfo = redisService.get("LOAN_PRO_INFO");
                if(StringUtils.isEmpty(loanProInfo)){
                    //获取产管所有产品
                    String allProInfo=(String)redisTemplate.opsForValue().get("productionMng-allProductions");
                    List<ProInSys> proInSys = JSONArray.parseArray(allProInfo, ProInSys.class);
                    if(proInSys!=null){
                        Iterator<ProInSys> iterator = proInSys.iterator();
                        while (iterator.hasNext()){
                            ProInSys pro=iterator.next();
                            //判断是否是贷中产品
                            if(pro.getBusinessTypeCode().indexOf(Constants.LOAN_BUSINESSTYPECODE)==-1){
                                iterator.remove();
                            }
                        }
                        if(proInSys.size()>0){
                            String json= JSONObject.toJSONString(proInSys);
                            loanProAray=JSONArray.parseArray(json);
                        }
                    }
                }else{
                    loanProAray=JSONArray.parseArray(loanProInfo);
                }
            }catch (Exception e){
                log.error("获取贷中产品信息出错---",e);
            }

            String strategy = StrategyClient.getStrategy(apiCode, strategyId);
            JSONObject strategyJson = JSONObject.parseObject(strategy);
            JSONObject ruleType = strategyJson.getJSONObject("ruleType");
            JSONObject behaviorScore = strategyJson.getJSONObject("behaviorScore");
            JSONArray ruleTypeList = ruleType.getJSONArray("ruleTypeList");
            List<String> list = this.sortRuleList(ruleType);
            Set<String> products=new HashSet<>();
            JSONObject productJson=new JSONObject();
            for(int i=0;i<ruleTypeList.size();i++){
                JSONObject jsonObject = ruleTypeList.getJSONObject(i);
                String ruleType1 = jsonObject.getString("ruleType");
                JSONObject jsonObject1 = rule3Data(ruleType1,loanProAray);
                productJson.putAll(jsonObject1);
                //setFields(jsonObject1,apiCode,proFieldMap,strategyId,stmtKey);
            }
            JSONArray behaviorScoreArray= behaviorScore.getJSONArray("behaviorScore");
            for(int i=0;i<behaviorScoreArray.size();i++){
                JSONObject jsonObject = behaviorScoreArray.getJSONObject(i);
                String proCode = jsonObject.getString("pro_code");
                JSONObject jsonObject1 = rule3Data(proCode,loanProAray);
                productJson.putAll(jsonObject1);
                //setFields(jsonObject1,apiCode,proFieldMap,strategyId,stmtKey);
            }
            ReturnDataRepository.needReturnProduct(products,apiCode,list);
            Set<String> strings = productJson.keySet();
            List<String> removeKeys=new ArrayList<>();
            log.info("products {}",products);
            for(String key:strings){
                log.info("key {}",key);
                String s = key.toLowerCase();
                if(!products.contains(s)){
                    removeKeys.add(key);
                }
            }
            log.info("removeKeys {}",removeKeys);
            for(String key:removeKeys){
                productJson.remove(key);
            }
            log.info("productJson {}",productJson);
            setFields(productJson,apiCode,proFieldMap,strategyId,stmtKey);
        }else if(strategyId.startsWith("DTM")){
            log.info("strategyStr:{}",strategyStr);
            JSONArray dtbArray=JSONArray.parseArray(strategyStr);
            for(int i=0;i<dtbArray.size();i++){
                JSONObject jsonObject = dtbArray.getJSONObject(i);
                JSONObject jsonObject1=new JSONObject();
                jsonObject1.put(jsonObject.getString("code"),jsonObject.getString("version"));
                setFields(jsonObject1,apiCode,proFieldMap,strategyId,stmtKey);

                JSONObject versionJson=new JSONObject();
                versionJson.put("version",jsonObject.getString("version"));
                meal.put(jsonObject.getString("code"),versionJson);
            }
        }

    }
    private JSONObject  rule3Data(String proCode,JSONArray loanProAray) {
        for (int i = 0; i < loanProAray.size(); i++) {
            JSONObject ruleTypeObject = loanProAray.getJSONObject(i);
            if(proCode.equals(ruleTypeObject.getString("productionName"))){
                JSONObject dependDataProduction = ruleTypeObject.getJSONObject("dependDataProduction");
                return dependDataProduction;
            }
        }
        return null;
    }

    //设置字段属性
    public void setFields(JSONObject jsonObject1,String apiCode,Map<String,String> proFieldMap,String strategyId,String stmtKey){
        Set<String> keySet = jsonObject1.keySet();
        for(String pro:keySet){
            String  s = pro.toLowerCase();
//            if(s.indexOf("scoredata")!=-1){
//                Map<String,String> param=new HashedMap();
//                param.put("apiCode",apiCode);
//                param.put("strategyId",strategyId);
//                //查询客制化衍生变量
//                String  scoreDataFields= marketingTaskMapper.queryScoreData(param);
//                if(StringUtils.isNotEmpty(scoreDataFields)){
//                    scoreDataFields=scoreDataFields.trim();
//                    proFieldMap.put(pro,scoreDataFields);
//                }
//                continue;
//            }
            String stringValue = PropertiesUtil.getProperty(s);
            proFieldMap.put(pro,stringValue);
//            String proFields = getProFields(pro, jsonObject1.getString(pro), apiCode,stmtKey);
//            if(StringUtils.isEmpty(proFields)){
//                log.info("setFields s:{},value:{}",s,stringValue);
//                proFieldMap.put(pro,stringValue);
//            }else{
//                StringBuilder sb=new StringBuilder();
//                if(StringUtils.isNotEmpty(stringValue)){
//                    String[] split = stringValue.split(",");
//                    for(int k=0;k<split.length;k++){
//                        if(proFields.contains(split[k])){
//                            sb.append(split[k]).append(",");
//                        }
//                    }
//                }
//                proFieldMap.put(pro,sb.toString());
//            }
        }
    }

    /**
     * 获取策略后台配置的当前规则集的剔除逻辑
     * {"ruleCode":"Rule_W_ApplyLoanInter_autofin","apiCode":"5200156","level":"B","ruleVersion":"V1.0","ruleName":"贷中预警增量规则-借贷意向验证-汽车金融","detail":{"ruleList":[{"apiCode":"-1","_checked":true,"ruleTypeStatus":1,"jsonstr":"{\"fields\":[\"i_ali_id_nbank_allnum\",\"i_ali_cell_nbank_allnum\"],\"params\":[\"1\",\"1\"],\"logics\":[\"||\",\"\"],\"operators\":[\"==\",\"==\"],\"priority\":50,\"ruleType\":\"Rule_W_ApplyLoanInter_autofin\",\"ruleCode\":\"LIA001\",\"ruleName\":\"在非银机构申请次数极少\"}","createdate":1529477533000,"remark":"","updateuserid":4456,"priority":50,"ruleCode":"LIA001","ruleCount":"0","updatedate":1529477533000,"ruleType":"Rule_W_ApplyLoanInter_autofin","ruleGroup":"group1","ruleName":"在非银机构申请次数极少","createuserid":4456,"checked":true,"id":305110,"status":1},{"apiCode":"-1","_checked":true,"ruleTypeStatus":1,"jsonstr":"{\"fields\":[\"(i_ali_id_nbank_allnum\",\"i_ali_id_nbank_allnum)\",\"(i_ali_cell_nbank_allnum\",\"i_ali_cell_nbank_allnum)\"],\"params\":[\"2\",\"4\",\"2\",\"4\"],\"logics\":[\"&&\",\"||\",\"&&\",\"\"],\"operators\":[\">=\",\"<=\",\">=\",\"<=\"],\"priority\":60,\"ruleType\":\"Rule_W_ApplyLoanInter_autofin\",\"ruleCode\":\"LIA002\",\"ruleName\":\"在非银机构申请次数较少\"}","createdate":1529477533000,"remark":"","updateuserid":4456,"priority":60,"ruleCode":"LIA002","ruleCount":"0","updatedate":1529477533000,"ruleType":"Rule_W_ApplyLoanInter_autofin","ruleGroup":"group1","ruleName":"在非银机构申请次数较少","createuserid":4456,"checked":true,"id":305111,"status":1},{"apiCode":"-1","_checked":true,"ruleTypeStatus":1,"jsonstr":"{\"fields\":[\"i_ali_id_nbank_allnum\",\"i_ali_cell_nbank_allnum\"],\"params\":[\"5\",\"5\"],\"logics\":[\"||\",\"\"],\"operators\":[\">=\",\">=\"],\"priority\":70,\"ruleType\":\"Rule_W_ApplyLoanInter_autofin\",\"ruleCode\":\"LIA003\",\"ruleName\":\"在非银机构申请次数较多\"}","createdate":1529477533000,"remark":"","updateuserid":4456,"priority":70,"ruleCode":"LIA003","ruleCount":"0","updatedate":1529477533000,"ruleType":"Rule_W_ApplyLoanInter_autofin","ruleGroup":"group1","ruleName":"在非银机构申请次数较多","createuserid":4456,"checked":true,"id":305112,"status":1}],"relation":1}}
     * @param apiCode
     * @param ruleType
     * @param version
     * @return
     */
    public String getdataEliminateJson(String apiCode,String ruleType,String version){
        String key = Constants.REDIS_STMT_RULE_PREFIX + apiCode + "_" + ruleType + "_" + version;
        String s = redisService.get(key);
        if(StringUtils.isEmpty(s)){
            return "";
        }
        JSONObject result=new JSONObject();
        DocumentContext parse = JsonPath.parse(s);
        String relation = parse.read("$.detail.relation").toString();
        String level = parse.read("$.level").toString();
        String ruleCode = parse.read("$.detail.ruleList..ruleCode").toString();
        result.put("relation",relation);
        result.put("level",level);
        result.put("ruleCode",ruleCode);
        return result.toJSONString();

    }

    /**
     * 对策略中的规则集进行排序
     * @param ruleType
     * @return
     */
    private List<String> sortRuleList(JSONObject ruleType){
        List<String> result=new ArrayList<>();
        ReadContext context = JsonPath.parse(ruleType);
        String[] split = rules.split(",");
        for(int i=0;i<split.length;i++){
            Object read = context.read("$..ruleTypeList[?(@.ruleType=='"+split[i]+"')]");
            if(read!=null){
                JSONArray array = JSONArray.parseArray(read.toString());
                if(array!=null&&array.size()>0){
                    result.add(split[i]);
                }
            }
        }
        return result;
    }

}

