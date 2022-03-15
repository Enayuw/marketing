package com.br.marketing.strategy;

import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.origin.TransmitFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.InterfaceParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractExternalInterfaceHandler> handlerMap = applicationContext.getBeansOfType(AbstractExternalInterfaceHandler.class);
        handlerMap.values().forEach(interfaceHandler -> externalInterfaceHandlerMap.put(interfaceHandler.handlerEnum().getCode(), interfaceHandler));

        assembleDataMap = applicationContext.getBeansOfType(AssembleData.class);

        Map<String, OriginDataService> dataMap = applicationContext.getBeansOfType(OriginDataService.class);
        dataMap.values().forEach(originData -> originDataMap.put(originData.source().getCode(), originData));
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

    public Map<Integer, List<InterfaceParams>> assembleData(List<TransmitFact> facts, List<AssembleData> assembleDataList,
                                                            ProcessHandlerContext context) {

        Map<Integer, List<InterfaceParams>> map = new HashMap();


        /**
         * 循环遍历所有详情数据，匹配该apiCode下所有匹配规则方法
         * 生成所对应的接口处理handler枚举及数据
         * map <具体的接口枚举,接口所需对应的参数类列表>
         */
        for (TransmitFact transmitFact : facts) {
            for (AssembleData assembleData : assembleDataList) {
                if (assembleData.isNeedAssemble(transmitFact,context)){
                    InterfaceParams interfaceParam = assembleData.assemble(transmitFact,context);
                    List<InterfaceParams> array = map.get(assembleData.dataDirection());
                    if (CollectionUtils.isEmpty(array)){
                        array = new ArrayList<>();
                        array.add(interfaceParam);
                        map.put(assembleData.dataDirection(),array);
                    }else {
                        array.add(interfaceParam);
                    }
                }
            }
        }
        return map;

    }

    public Map<Integer, List<InterfaceParams>> collectAndAssembleData(MqFact mqFact, ProcessHandlerContext context) {

        OriginDataService originData = originDataMap.get(mqFact.getSource());
        /**
         * 1、根据不同数据来源收集数据信息
         */
        List<TransmitFact> transmitFacts = originData.collect(mqFact, context);

        /**
         * 2、根据不同数据来源 匹配出要执行的规则
         */
        List<AssembleData> assembleDataList = originData.patternMatch(mqFact, context);

        /**
         * 3、组装数据
         */
        return assembleData(transmitFacts,assembleDataList,context);
    }
}
