package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.InterfaceParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
@Slf4j
public class InterfaceHandlerService {

    @Resource
    private InterfaceHandlerFactory interfaceHandlerFactory;


    /**
     *  处理数据流向
     *  1、根据原始表id，查询该批次中传送数据
     *  2、遍历所有数据，按照不同调用接口逻辑将数据分类
     *  3、不同数据调用不同的接口处理
     */

    public Result<Boolean> handleDataDirection(String message){

        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue());

        try {
            /**
             *
             * 1、根据不同数据来源收集数据
             * 2  遍历数据 根据客户apiCode 及原始详情表数据封装到 map <具体的接口枚举,接口所需对应的参数类列表>
             *     如 { 1:List<BlackListDTO>,4:List<ConversionData>}
             */
            MqFact mqFact = JSON.parseObject(message, MqFact.class);
            ProcessHandlerContext processHandlerContext = new ProcessHandlerContext();
            processHandlerContext.setMqFact(mqFact);

            Map<Integer, List<InterfaceParams>> map =interfaceHandlerFactory.collectAndAssembleData(mqFact,processHandlerContext);

            /**
             *  3  根据2获取的map key -> 具体的三方接口，value -> 三方接口入参
             */
            Set<Integer> set = map.keySet();
            for (Integer enumFlag : set) {
                interfaceHandlerFactory.handler(enumFlag,map.get(enumFlag), processHandlerContext);
            }
            result.setDate(false);

        } catch (Exception e) {
            log.error("通用转化逻辑处理数据 mq:{} 失败 -- ",message,e);
            result.setDate(true);
        }
        return result;
    }

}
