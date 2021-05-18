package com.br.marketing.api.broker;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.client.RedisService;
import com.br.marketing.api.client.RuleTypesClient;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entity.ProInSys;
import com.br.marketing.common.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** 贷中策略解释器接口
 * 对策略中的评分、规则集进行解析。设置好依赖的数据产品等信息，后面执行具体任务时会用到。
 * @author Wang Weiwei
 * @since 2018/3/15
 */
@Slf4j
public class LoanStrategyExpression implements StrategyExpression,StrategyApiContextHolder {
    private StrategyApiContext strategyApiContext;

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
     "productionName":"Rule_W_SpecialList_c_mix",
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

    private static   JSONArray allProductArray;

    /**
     * Instantiates a new Loan strategy expression.
     *
     * @param strategyApiContext the strategy api context
     */
    public LoanStrategyExpression(StrategyApiContext strategyApiContext) {
        this.strategyApiContext = strategyApiContext;
        RuleTypesClient ruleTypesClient = strategyApiContext.getSpringContext().getBean("ruleTypesClient", RuleTypesClient.class);
        RedisService redisService= strategyApiContext.getSpringContext().getBean("redisService", RedisService.class);
        if(allProductArray==null||allProductArray.size()==0){
            log.warn("缓存为空，直接取redis中的数据");
            updateProducts(ruleTypesClient,redisService);
        }
    }


    /**
     * Update products.
     *
     * @param ruleTypesClient the rule types client
     * @param redisService    the redis service
     */
    public static void updateProducts(RuleTypesClient ruleTypesClient,RedisService redisService){
        allProductArray=getAllLoanProducts(ruleTypesClient,redisService);
        log.info("策略贷中产品信息:{}",allProductArray);
    }

    /**
     * Gets all loan products.
     *
     * @param ruleTypesClient the rule types client
     * @param redisService    the redis service
     * @return the all loan products
     */
    public static  JSONArray getAllLoanProducts(RuleTypesClient ruleTypesClient,RedisService redisService) {
        JSONArray array=new JSONArray();
        String s =  ruleTypesClient.getAllRuleType();
        List<ProInSys> proInSys = JSONArray.parseArray(s, ProInSys.class);
        Iterator<ProInSys> iterator = proInSys.iterator();
        while (iterator.hasNext()){
            ProInSys pro=iterator.next();
            if(pro.getBusinessTypeCode().indexOf(Constants.LOAN_BUSINESSTYPECODE)==-1){
                iterator.remove();
            }
        }
        if(proInSys.size()>0){
            String json= JSONObject.toJSONString(proInSys);
            array=JSONArray.parseArray(json);
        }
        if(array!=null&&!array.isEmpty()){
            redisService.set("LOAN_PRO_INFO",array.toString());
        }

        return array;
    }


    @Override
    public void interpret() {
        interpretRuleDaraRelation();
    }

    private void interpretRuleDaraRelation() {
        if (!StringUtils.isEmpty(strategyApiContext.getStrategy().getRuleType())){
            JSONArray jsonArray = JSONArray.parseArray(strategyApiContext.getStrategy().getRuleType());
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject ruleObject = jsonArray.getJSONObject(i);
                JSONObject ruleData = rule3Data(ruleObject.getString("ruleType"));
                if (ruleData != null){
                    if (!StringUtils.isEmpty(ruleObject.getString("type"))){
                        break;
                    }
                    String dependDataProduction = ruleData.getString("dependDataProduction");
                    JSONObject product=productMagic(dependDataProduction);
                    ruleObject.put("productor", product);
                    //如果是三相、海纳的产品，将type设为 sx
                    if("Rule_W_BadInfo_mix".equals(ruleObject.getString("ruleType"))){
                        ruleObject.put("type", "sx");
                    }else if("Rule_W_CourtDetail_mix".equals(ruleObject.getString("ruleType"))){
                        ruleObject.put("type", "hn");
                    }else{
                        ruleObject.put("type", "hx");
                    }
                    ruleObject.put("rule_name",ruleData.getString("productionChineseName"));
                }
            }
            log.info("规则与数据产品相关的关系：{}", jsonArray);
            strategyApiContext.getStrategy().setRuleType(jsonArray.toJSONString());
        }
        if(strategyApiContext.getHaveBehavior()){
            behaviorScoreDataRelation();
        }

    }

    /**
     * 解释上下文定义中评分与数据产品相关的关系
     * */
    private void behaviorScoreDataRelation(){
        if(!StringUtils.isEmpty(strategyApiContext.getStrategy().getBehaviorScore())){
            String behaviorScore1 = strategyApiContext.getStrategy().getBehaviorScore();
            log.info("评分：{}",behaviorScore1);
            JSONObject jsonObject1 = JSONObject.parseObject(behaviorScore1);
            JSONArray behaviorScore = jsonObject1.getJSONArray("behaviorScore");
            for(int i=0;i<behaviorScore.size();i++) {
                JSONObject behavior = behaviorScore.getJSONObject(i);
                String proCode = behavior.getString("pro_code");
                JSONObject jsonObject = rule3Data(proCode);
                if(jsonObject!=null){
                    String dependDataProduction = jsonObject.getString("dependDataProduction");
                    JSONObject product=productMagic(dependDataProduction);
                    behavior.put("productor", product);
                }
            }
            log.info("评分与数据产品的依赖关系--{}",jsonObject1);
            strategyApiContext.getStrategy().setBehaviorScore(jsonObject1.toJSONString());
        }
    }

    /**
     * 格式化规则集依赖的数据产品
     * @param product
     * @return
     */
    private JSONObject productMagic(String product){
        JSONObject json=null;
        if(!StringUtils.isEmpty(product)){
            json=new JSONObject();
            JSONObject productJson=JSONObject.parseObject(product);
            Set<String> keySet = productJson.keySet();
            for(String key:keySet){
                JSONObject version=new JSONObject();
                String string = productJson.getString(key);
                string=string.trim();
                version.put("version",string);
                json.put(key,version);
            }
        }
        return json;
    }
    /**
     *  将规则集转化为数据产品的关联信息的方法
     * 如果该规则集未有关联的产品，则直接返回null
     * @param proCode
     * @return
     */
    private JSONObject rule3Data(String proCode) {
        for (int i = 0; i < allProductArray.size(); i++) {
            JSONObject ruleTypeObject = allProductArray.getJSONObject(i);
            if(proCode.equals(ruleTypeObject.getString("productionName"))){
                return ruleTypeObject;
            }
        }
        return null;
    }


    @Override
    public StrategyApiContext getStrategyApiContext() {
        return strategyApiContext;
    }

    @Override
    public void setStrategyApiContext(StrategyApiContext strategyApiContext) {
        this.strategyApiContext = strategyApiContext;
    }
}
