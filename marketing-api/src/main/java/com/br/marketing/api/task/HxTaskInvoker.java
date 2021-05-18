package com.br.marketing.api.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.client.HxClient;
import com.br.marketing.api.client.MerchantRepository;
import com.br.marketing.api.entities.api.ApiUserParam;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.client.HxResult;
import com.br.marketing.api.entity.Strategy;
import com.br.marketing.common.constants.auth.AuthShowProductor;
import com.br.marketing.common.constants.strategy.ProType;
import com.br.marketing.common.exception.strategy.LoanTaskException;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/** 画像任务执行器，根据用户信息，远程调用画像接口
 * @author Wang Weiwei
 * @since 2018/3/16
 */
@Slf4j
public class HxTaskInvoker extends BaseRetryTaskInvoker<HxResult> {
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private HxClient hxClient;
    private MerchantRepository repository;
    private JSONObject param = new JSONObject();
    private JSONObject ruleParam = new JSONObject();
    private JSONObject behaviorParam = new JSONObject();
    private StrategyApiContext strategyApiContext;
    private Boolean haveToday = Boolean.FALSE;
    private Boolean haveDiffVersion=Boolean.FALSE;
   private ApiUserParam apiUserParam;

    /**
     * 画像任务执行器
     * @param apiUserParam 用户 入参
     * @param strategyApiContext 上下文
     */
    public HxTaskInvoker(ApiUserParam apiUserParam, StrategyApiContext strategyApiContext) {
        super();
        this.strategyApiContext = strategyApiContext;
        BeanFactory beanFactory = strategyApiContext.getSpringContext();
        threadPoolTaskExecutor = beanFactory.getBean("StrategyThreadPool", ThreadPoolTaskExecutor.class);
        hxClient = beanFactory.getBean("hxClient", HxClient.class);
        repository = beanFactory.getBean("merchantRepository",MerchantRepository.class);

        this.apiUserParam=apiUserParam;
        if(strategyApiContext.getStrategyId().startsWith("STRB")){
            parseHxProducts(strategyApiContext);
        }else{
            getStrategyProduct(strategyApiContext.getDtbStrategy());
        }
    }

    /**
     * 数据策略
     * @param dtbStrategy
     */
    protected final void getStrategyProduct(JSONObject dtbStrategy){
        JSONObject jsonMeal=new JSONObject();
        JSONArray pros = dtbStrategy.getJSONArray("dataProdList");
        for(int i=0;i<pros.size();i++){
            JSONObject product = pros.getJSONObject(i);
            if(product!=null&&!"BadInfo".equals(product.getString("code"))&&
                    !"CourtDetail".equals(product.getString("code"))){
                JSONObject version=new JSONObject();
                version.put("version",product.getString("version").trim());
                jsonMeal.put(product.getString("code"),version);
                if("ApplyLoan_d".equals(product.getString("code"))){
                    haveToday = Boolean.TRUE;
                }
            }
        }
        if (jsonMeal.size() == 0){
            throw new LoanTaskException("未发现可用的画像产品");
        }
        param.put("jsonMeal", jsonMeal);
        rePackageParam(param, apiUserParam);
    }

    /**
     * 重新构建请求参数
     * */
    protected void rePackageParam(JSONObject param,ApiUserParam apiUserParam) {
        param.put("id", apiUserParam.getIdCard());
        param.put("cell", apiUserParam.getPhone());
        param.put("name", apiUserParam.getName());
        param.put("originApiCode",strategyApiContext.getApiCode());
        String userDate = apiUserParam.getUserDate();
        String slUsetDate = apiUserParam.getSlUserDate();
        String decodeFailType = apiUserParam.getDecodeFailType();
        if(StringUtils.isNotEmpty(userDate)){
            param.put("start_date", apiUserParam.getPassDate());
            param.put("user_date", userDate);
        }
        if(StringUtils.isNotEmpty(slUsetDate)){
            param.put("sl_user_date", slUsetDate);
        }
        if(StringUtils.isNotEmpty(decodeFailType)){
            param.put("decodeFailType", decodeFailType);
        }
        if(haveToday&&StringUtils.isNotEmpty(apiUserParam.getUserTime())){
            param.put("user_time", apiUserParam.getUserTime());
        }
    }

    /**
     * 初始化画像请求产品
     * @param ruleType 策略关联画像数据产品
     * @param hxProductsJson 画像产品
     */
    public void parseRuleProductors(String ruleType, JSONObject hxProductsJson) {
        JSONArray ruleArray = JSONArray.parseArray(ruleType);
        for (int i = 0; i < ruleArray.size(); i++) {
            JSONObject object = ruleArray.getJSONObject(i);
            if(object.getString("ruleType").contains("ApplyLoan_d")){
                haveToday = Boolean.TRUE;
            }
            if ("hx".equals(object.getString("type"))){
                hxProductsJson.putAll(object.getJSONObject("productor"));
            }
        }

    }

    /**
     * 解析画像参数
     * @param
     */
    private   void parseHxProducts(StrategyApiContext context){
        Strategy strategy = context.getStrategy();
        JSONObject hxProductsJson=new JSONObject();
        JSONObject behaviorProductJson=new JSONObject();
        //是否含有规则任务
        if(context.getHaveRule()){
            String ruleType = strategy.getRuleType();
            parseRuleProductors(ruleType,hxProductsJson);
        }
        //是否包含评分任务
        if(context.getHaveBehavior()){
            String behaviorScore = strategy.getBehaviorScore();
            parseScoreProducts(behaviorScore,hxProductsJson,behaviorProductJson);
        }
        if (hxProductsJson.size() == 0){
            throw new LoanTaskException("未发现可用的画像产品");
        }
        if(!haveDiffVersion){
            param.put("jsonMeal",hxProductsJson);
            rePackageParam(param, apiUserParam);
        }else {
            ruleParam.put("jsonMeal",hxProductsJson);
            rePackageParam(ruleParam, apiUserParam);
            behaviorParam.put("jsonMeal",behaviorProductJson);
            rePackageParam(behaviorParam, apiUserParam);
        }
    }

    protected void parseScoreProducts(String behaviorScoreJson, JSONObject hxProductsJson, JSONObject behaviorProductJson) {
        JSONArray behaviorScore = JSON.parseObject(behaviorScoreJson).getJSONArray("behaviorScore");
        for(int i=0;i<behaviorScore.size();i++) {
            JSONObject version=new JSONObject();
            JSONObject behavior = behaviorScore.getJSONObject(i);
            String version1 = behavior.getString("version");
            version1=version1.trim();
            version.put("version",version1);
            hxProductsJson.put(behavior.getString("pro_code"),version);
            JSONObject productor = behavior.getJSONObject("productor");
            if(!productor.isEmpty()){
                behaviorProductJson.putAll(productor);
            }
        }
        behaviorShowData(hxProductsJson,behaviorProductJson);
        if(AuthShowProductor.SHOW==strategyApiContext.getIsShowData()){
            retainBehaviorProduct(behaviorProductJson);

            //如果评分的数据产品版本和规则集的数据产品版本没有冲突，将两个数据产品json合并
            if(!haveDiffVersion){
                hxProductsJson.putAll(behaviorProductJson);
            }

        }
        for(int i=0;i<behaviorScore.size();i++) {
            JSONObject version=new JSONObject();
            JSONObject behavior = behaviorScore.getJSONObject(i);
            String version1 = behavior.getString("version");
            version1=version1.trim();
            version.put("version",version1);
            behaviorProductJson.put(behavior.getString("pro_code"),version);
        }

    }

    /**
     * 如果返回数据，
     * 那么去评分依赖的数据产品与客户配置的数据产品的交集
     * @param behaviorProductJson
     */
    protected void retainBehaviorProduct(JSONObject behaviorProductJson){
        Set<String> relyPro = behaviorProductJson.keySet();
        //2. 获取客户配置的行为评分相关的数据产品
        String productCode = repository.getAllProductCode(strategyApiContext.getApiCode(), ProType.BASE_DATA.getCode());
        log.info("客户配置的评分相关产品：{}",productCode);
        if(StringUtils.isNotEmpty(productCode)) {
            Set<String> cusPro = new HashSet<>(Arrays.asList(productCode.split(",")));
            relyPro.retainAll(cusPro);
        }

        log.info("依赖产品与客户交集：{}",JSON.toJSONString(relyPro));
        List<String> removeKeys=new ArrayList<>();
        if(!relyPro.isEmpty()) {
            Set<String> keySet = behaviorProductJson.keySet();
            for (String key : keySet) {
                if (!relyPro.contains(key)) {
                    removeKeys.add(key);
                }
            }
            for(String removeKey:removeKeys){
                behaviorProductJson.remove(removeKey);
            }
        }
        log.info("返回数据产品，评分最终的数据产品：{}",JSON.toJSONString(behaviorProductJson));
    }

    /**
     * 评分产品返回是否返回数据
     * 依赖的数据产品处理
     * @param hxProductsJson
     * @param behaviorProductJson
     */
    protected void behaviorShowData(JSONObject hxProductsJson,JSONObject behaviorProductJson){
        Set<String> keySet = behaviorProductJson.keySet();
        for(String key:keySet){
            JSONObject version = hxProductsJson.getJSONObject(key);
            if(version!=null&&!version.getString("version").equals(behaviorProductJson.getJSONObject(key).getString("version"))){
                haveDiffVersion=Boolean.TRUE;
            }
        }
    }

    @Override
    Future<HxResult> retryInvoker() {
        return threadPoolTaskExecutor.submit(new Callable<HxResult>() {
            @Override
            public HxResult call() {
                HxResult report;
                if(!haveDiffVersion){
                     report = hxClient.report(param);
                }else{
                     report = hxClient.report(ruleParam);
                    HxResult behaviorReport = hxClient.report(behaviorParam);
                    report.setBehaviorResult(behaviorReport.toString());
                }
                return report;
            }
        });
    }
}
