package com.br.marketing.strategy;

import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.InterfaceParams;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
public class InterfaceHandlerFactory implements ApplicationContextAware {

    private static ApplicationContext ac;

    private static Map<Integer, AbstractExternalInterfaceHandler> map = new HashMap<>();



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractExternalInterfaceHandler> handlerMap = applicationContext.getBeansOfType(AbstractExternalInterfaceHandler.class);
        handlerMap.values().forEach(interfaceHandler -> map.put(interfaceHandler.handlerEnum().getCode(), interfaceHandler));

        ac = applicationContext;
    }

    public Object handler(int enumFlag, List<InterfaceParams> list, MarketingTransferInfo marketingTransferInfo) {
        /**
         *  1、不同的接口调用不同的三方接口类
         */
        return map.get(enumFlag).call(list,marketingTransferInfo);
        // todo 后续一些调用记录入库操作，方便以后数据对比
    }

    public Map<Integer, List<InterfaceParams>> assembleData(String apiCode, List<MarketingTransferSyncUser> transferList) {
        Map<Integer, List<InterfaceParams>> map = new HashMap();


        /**
         * 获取改 apiCode所需的规则匹配方法
         */
        List<AssembleData> assembleDataList = new ArrayList<>();
        Map<String, AssembleData> assembleDataMap = ac.getBeansOfType(AssembleData.class);
        Collection<AssembleData> values = assembleDataMap.values();
        for (AssembleData assembleData : values) {
            if (assembleData.belongTo().contains(apiCode)){
                assembleDataList.add(assembleData);
            }
        }

        /**
         * 循环遍历所有详情数据，匹配该apiCode下所有匹配规则方法‘
         * 生成所对应的接口处理handler枚举及数据
         */
        for (MarketingTransferSyncUser transferSyncUser : transferList) {
            for (AssembleData assembleData : assembleDataList) {
                InterfaceParams interfaceParam = assembleData.assemble(transferSyncUser);
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
        }
        return map;

    }
}
