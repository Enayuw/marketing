package com.br.marketing.strategy;

import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.InterfaceParams;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

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


    private static ApplicationContext ac;


    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    /**
     * 从应用上下文中处理封装获取三方接口 map <具体的接口枚举值,接口对象>
     *     {1:ArtificialBlackListHandler,4:CustomerTransferHandler}
     */
    private static Map<Integer, AbstractExternalInterfaceHandler> externalInterfaceHandlerMap = new HashMap<>();


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractExternalInterfaceHandler> handlerMap = applicationContext.getBeansOfType(AbstractExternalInterfaceHandler.class);
        handlerMap.values().forEach(interfaceHandler -> externalInterfaceHandlerMap.put(interfaceHandler.handlerEnum().getCode(), interfaceHandler));
        ac = applicationContext;
    }

    public void handler(int enumFlag, List<InterfaceParams> list, MarketingTransferInfo marketingTransferInfo) {
        /**
         *  1、不同的接口调用不同的三方接口类
         */
        try {
            externalInterfaceHandlerMap.get(enumFlag).call(list,marketingTransferInfo);
        } catch (Exception e) {
            log.error("调用三方接口处理异常 -- ",e);
        }

    }

    public Map<Integer, List<InterfaceParams>> assembleData(String apiCode, List<MarketingTransferSyncUser> transferList) {

        Map<Integer, List<InterfaceParams>> map = new HashMap();

        HashMap<String, String> customerRuleMapping = marketingCommonConfig.getCustomerRuleMapping();
        /**
         * 1、获取 apiCode获取所需的规则匹配方法
         */
        List<AssembleData> assembleDataList = new ArrayList<>();
        Map<String, AssembleData> assembleDataMap = ac.getBeansOfType(AssembleData.class);
        Collection<AssembleData> values = assembleDataMap.values();
        for (AssembleData assembleData : values) {
            if (assembleData.label().startsWith(customerRuleMapping.get(apiCode))){
                assembleDataList.add(assembleData);
            }
        }

        /**
         * 循环遍历所有详情数据，匹配该apiCode下所有匹配规则方法
         * 生成所对应的接口处理handler枚举及数据
         * map <具体的接口枚举,接口所需对应的参数类列表>
         */
        for (MarketingTransferSyncUser transferSyncUser : transferList) {
            for (AssembleData assembleData : assembleDataList) {
                if (assembleData.isNeedAssemble(transferSyncUser)){
                    InterfaceParams interfaceParam = assembleData.assemble(transferSyncUser);
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
}
