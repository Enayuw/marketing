//package com.br.marketing.client;
//
//import com.br.bsf.ext.app.util.Ice1BSFConsumerBean;
//import com.br.ice.service.strategy.StrategyServicePrx;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
///**
// * Created by Bairong on 2019/7/16.
// */
//@Component
//@Slf4j
//public class StrategyClient {
//
//    /**
//     * 从策略中心ice服务查询策略信息
//     * 返回结果
//     * {"apiCode":"5200997","strCode":"STRB0000014","strName":"条数","useVersion":"1.0","prodName":"条数","prodType":"通用","ruleType":"{\"ruleTypeList\":[{\"ruleType\":\"Rule_W_SpecialList_c_mix\",\"version\":\"1.0\"},{\"ruleType\":\"Rule_W_SpecialList_c_cooff\",\"version\":\"1.0\"}],\"status\":1}","strategyRetry":"{\"preLoanStrategy\":[],\"status\":0}","behaviorScore":"{\"behaviorScore\":[],\"status\":0}","status":1,"canUse":0}
//     * @param apiCode
//     * @param strCode
//     * @return
//     */
//    public static String getStrategy(String apiCode, String strCode){
//        log.info("查询策略参数--apiCode：{}---策略编号：{}",apiCode,strCode);
//        String str="";
//        try{
//            StrategyServicePrx strategyService = (StrategyServicePrx) Ice1BSFConsumerBean.getServiceProxy(StrategyServicePrx.class);
//            strategyService= (StrategyServicePrx) strategyService.ice_connectionCached(false);
//            str = strategyService.getStrategyAllInfos(apiCode, strCode);
//        }catch (Exception e){
//            log.warn("调用风险策略出错，",e);
//            try{
//                StrategyServicePrx strategyService = (StrategyServicePrx) Ice1BSFConsumerBean.getServiceProxy(StrategyServicePrx.class);
//                strategyService = (StrategyServicePrx) strategyService.ice_connectionCached(false);
//                str= strategyService.getStrategyAllInfos(apiCode, strCode);
//            }catch (Exception e1) {
//                log.error("调用风险策略重试出错，",e1);
//            }
//        }
//        log.warn("策略返回--{}",str);
//        return str;
//    }
//
//    /**
//     * 从策略中心ice查询风险分级
//     * 返回结果
//     * {"strategyRetry":{"no_result":{"no_result":"C","Review":"C","Reject":"D","no_loan_approval":"D","Accept":"B"},"Review":{"no_result":"C","Review":"C","Reject":"B","no_loan_approval":"B","Accept":"C"},"Reject":{"no_result":"D","Review":"D","Reject":"D","no_loan_approval":"D","Accept":"D"},"Accept":{"no_result":"B","Review":"B","Reject":"B","no_loan_approval":"B","Accept":"A"}},"ruleType":[{"min":0,"max":49,"name":"A"},{"min":50,"max":69,"name":"B"},{"min":70,"max":89,"name":"C"},{"min":90,"max":100,"name":"D"}],"behavior":[{"min":300,"max":630,"name":"D"},{"min":631,"max":700,"name":"C"},{"min":701,"max":770,"name":"B"},{"min":771,"max":1000,"name":"A"}]}
//     * @param apiCode
//     * @return
//     */
//    public static String getRiskRank(String apiCode){
//        log.info("查询风险分级参数--apiCode：{}",apiCode);
//        StrategyServicePrx strategyService = (StrategyServicePrx) Ice1BSFConsumerBean.getServiceProxy(StrategyServicePrx.class);
//        strategyService= (StrategyServicePrx) strategyService.ice_connectionCached(false);
//        String str = strategyService.getRiskRank(apiCode);
//        log.info("查询风险分级返回--{}",str);
//        return str;
//    }
//
//
//
//
//}
