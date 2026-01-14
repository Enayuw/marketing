package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.AbstractRuleCollectDataService;
import com.br.marketing.context.MqIdempotentContext;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.PeriodPushLog;
import com.br.marketing.mapper.PeriodPushLogMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.InterfaceParams;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 第三方接口代理工厂
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/2/28 20:54
 */

@Component
@Slf4j
public class InterfaceHandlerFactory implements ApplicationContextAware {

    /**
     * 从应用上下文中处理封装获取三方接口 map <具体的接口枚举值,接口对象>
     *     {1:ArtificialBlackListHandler,4:CustomerTransferHandler}
     */
    private static Map<Integer, AbstractExternalInterfaceHandler> externalInterfaceHandlerMap = new HashMap<>();


    /**
     * 应用上下文中获取所有实现AssembleData接口规则类
     */
    public static Map<String, AssembleData> assembleDataMap = new HashMap<>();

    /**
     * 应用上下文中获取所有实现OriginData数据来源处理类
     */
    private static Map<Integer, OriginDataService> originDataMap = new HashMap<>();


    /**
     * 应用上下文中获取所有实现AbstractRuleCollectDataService数据来源处理类
     */
    private static Map<Integer, AbstractRuleCollectDataService> ruleDataCollectionMap = new HashMap<>();

    @Resource
    private DataLoadingHandlerService dataLoadingHandlerService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private PeriodPushLogMapper periodPushLogMapper;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractExternalInterfaceHandler> handlerMap = applicationContext.getBeansOfType(AbstractExternalInterfaceHandler.class);
        handlerMap.values().forEach(interfaceHandler -> externalInterfaceHandlerMap.put(interfaceHandler.handlerEnum().getCode(), interfaceHandler));

        assembleDataMap = applicationContext.getBeansOfType(AssembleData.class);

        Map<String, OriginDataService> dataMap = applicationContext.getBeansOfType(OriginDataService.class);
        dataMap.values().forEach(originData -> originDataMap.put(originData.source().getCode(), originData));

        Map<String, AbstractRuleCollectDataService> collectDataServiceMap = applicationContext.getBeansOfType(AbstractRuleCollectDataService.class);
        collectDataServiceMap.values().forEach(ruleDataCollection -> ruleDataCollectionMap.put(ruleDataCollection.label().getCode(), ruleDataCollection));
    }

    public void handler(int enumFlag, List<InterfaceParams> list, ProcessHandlerContext context) {
        /**
         *  1、不同的接口调用不同的三方接口类
         */
        try {
            externalInterfaceHandlerMap.get(enumFlag).call(list,context);
        } catch (Exception e) {
            log.error("调用三方接口处理异常 -- ",e);
        }

    }

    public Map<Integer, List<InterfaceParams>> assembleData(List<Object> facts, List<AssembleData> assembleDataList,
                                                            ProcessHandlerContext context) {

        Map<Integer, List<InterfaceParams>> map = new HashMap();


        /**
         * 循环遍历所有详情数据，匹配该apiCode下所有匹配规则方法
         * 生成所对应的接口处理handler枚举及数据
         * map <具体的接口枚举,接口所需对应的参数类列表>
         */

        for (Object transmitFact : facts) {
            for (AssembleData assembleData : assembleDataList) {
                try {
                    if (assembleData.isNeedAssemble(transmitFact,context)){
                        InterfaceParams interfaceParam = assembleData.assemble(transmitFact,context);
                        List<InterfaceParams> array = map.get(assembleData.dataDirection());
                        if (!StringUtils.isEmpty(interfaceParam)){
                            if (CollectionUtils.isEmpty(array)){
                                array = new ArrayList<>();
                                array.add(interfaceParam);
                                map.put(assembleData.dataDirection(),array);
                            }else {
                                array.add(interfaceParam);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("数据组装逻辑出错 transmitFact -- {} -- ", JSON.toJSONString(transmitFact),e);
                }
            }
        }
        return map;

    }

    public Map<Integer, List<InterfaceParams>> collectAndAssembleData(MqFact mqFact, ProcessHandlerContext context) {
        Integer source = mqFact.getSource();
        OriginDataService originData = originDataMap.get(source);
        /**
         * 1、根据不同数据来源收集数据信息
         */
        List<Object> transmitFacts = originData.collect(mqFact, context);

        String apiCode = context.getApiCode();
        // 同步到ThreadLocal，供幂等性切面使用
        MqIdempotentContext.setApiCode(apiCode);

        // 检查配置是否要给间隔job添加数据以及是否继续往下走
        Map<String, JSONObject> periodPushConfig = marketingCommonConfig.getPeriodPushConfig();
        if(null != periodPushConfig){
            JSONObject config = periodPushConfig.get(apiCode);
            if(null != config){
                List<Long> idList = originData.getIdList(transmitFacts);
                if(null != idList && idList.size()>0){
                    PeriodPushLog periodPushLog = new PeriodPushLog();
                    periodPushLog.setApiCode(apiCode);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i <idList.size() ; i++) {
                        if(i == idList.size()-1){
                            sb.append(idList.get(i));
                        }else{
                            sb.append(idList.get(i)).append(",");
                        }
                    }
                    String idString = sb.toString();
                    periodPushLog.setIds(idString);
                    periodPushLog.setSource(source);
                    periodPushLog.setStatus(1);
                    periodPushLog.setIsDel(1);
                    periodPushLog.setCreateTime(new Date());
                    periodPushLogMapper.insert(periodPushLog);
                    Boolean breakFlag = config.getBoolean("breakFlag");
                    Integer sourceConfig = config.getInteger("source");
                    if(sourceConfig.equals(source) && breakFlag){
                        Map<Integer, List<InterfaceParams>> map = new HashMap();
                        return map;
                    }
                }
            }
        }

        /**
         * 2、根据不同数据来源 匹配出要执行的规则
         * 获取 apiCode获取所需的规则匹配方法
         */
        Set<String> customerRules = dataLoadingHandlerService.customerRules(apiCode);
        Set<String> execRules = new HashSet<>();
        if (!CollectionUtils.isEmpty(mqFact.getIncludeRules())){
            execRules.addAll(mqFact.getIncludeRules());
            execRules.retainAll(customerRules);
        }else {
            execRules.addAll(customerRules);
        }

        if (CollectionUtils.isEmpty(execRules)){
            log.error("customerRuleMapping 该apiCode: {}未配置对应规则", apiCode);
            return new HashMap<>();
        }
        log.warn("转化数据apiCode={}，使用规则rules={}", apiCode, execRules);
        /**
         * 规则排序
         */
        Collection<AssembleData> values = assembleDataMap.values();
        List<AssembleData> assembleDataList = values.stream().filter(data->execRules.contains(data.label()))
                .sorted(Comparator.comparing(AssembleData::label)).collect(Collectors.toList());

        dataLoadingHandlerService.commonRuleContextAction(source,assembleDataList,context);

        /**
         * 3、获取规则配置的上下文加载处理方法,set中值应不大于1
         */
        Set<Integer> set = assembleDataList.stream().map(AssembleData::ruleDataCollection).filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.toSet());
        if (set.size() > 1){
            log.error("规则配置的上下文加载处理方法有误");
        }
        for (Integer label : set) {
            ruleDataCollectionMap.get(label).ruleNecessaryData(transmitFacts,context);
        }

        /**
         * 4、组装数据
         */
        return assembleData(transmitFacts,assembleDataList,context);
    }
}
