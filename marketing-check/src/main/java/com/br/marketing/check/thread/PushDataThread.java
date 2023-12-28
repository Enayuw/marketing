//package com.br.marketing.check.thread;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.br.common.util.DateUtils;
//import com.br.marketing.check.CkeckApplication;
//import com.br.marketing.check.utils.MomUtil;
//import com.br.marketing.client.HttpProxyClient;
//import com.br.marketing.common.utils.DateHelper;
//import com.br.marketing.common.utils.StringUtils;
//import com.br.marketing.entity.Customer;
//import com.br.marketing.entity.PushErrorLog;
//import com.br.marketing.es.bean.MarketingHistory;
//import com.br.marketing.es.util.UuidUtils;
//import com.br.marketing.mapper.PushErrorLogMapper;
//import com.br.marketing.vo.TaskExtendInfoVO;
//import com.sun.org.apache.xpath.internal.operations.Bool;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.Callable;
//
///**
// * Created by Bairong on 2020/3/17.
// */
//@Slf4j
//public class PushDataThread implements Callable<String>{
//    private TaskExtendInfoVO taskExtendInfoVO;
//    private List<MarketingHistory> marketingHistoryList;
//    private Customer customer;
//    private Integer times;
//    private HttpProxyClient httpProxyClient;
//    private PushErrorLogMapper pushErrorLogMapper;
//    public PushDataThread(Customer customer,TaskExtendInfoVO taskExtendInfoVO, List<MarketingHistory> marketingHistoryList,Integer times){
//        this.customer=customer;
//        this.taskExtendInfoVO=taskExtendInfoVO;
//        this.marketingHistoryList=marketingHistoryList;
//        this.times=times;
//        httpProxyClient= CkeckApplication.ac.getBean(HttpProxyClient.class);
//        pushErrorLogMapper=CkeckApplication.ac.getBean(PushErrorLogMapper.class);
//    }
//    @Override
//    public String call() throws Exception {
//        JSONObject param=new JSONObject();
//        param.put("requestId", UuidUtils.getUuid());
//        JSONArray dataItems =new JSONArray();
//        marketingHistoryList.forEach(marketingHistory -> {
//            JSONObject item=new JSONObject();
//            item.put("taskId",marketingHistory.getTaskId());
//            item.put("groupType",marketingHistory.getUserType());
//            item.put("custNum",marketingHistory.getCusNum());
//            JSONObject resultJson=JSONObject.parseObject(marketingHistory.getReserveField()) ;
//            resultJson.put("times",times);
//            resultJson.put("request_time", DateUtils.format(marketingHistory.getRequestTime()));
//            item.put("resultJson",resultJson);
//            dataItems.add(item);
//        });
//        param.put("dataItems",dataItems);
//        Long begin=System.currentTimeMillis();
//        JSONObject extendConfigInfoJson=new JSONObject();
//        String extendConfigInfo=customer.getExtendConfigInfo();
//        if(StringUtils.isNotBlank(extendConfigInfo)){
//            extendConfigInfoJson=JSONObject.parseObject(extendConfigInfo);
//        }
//        Boolean isProxy=extendConfigInfoJson.getBoolean("isProxy")==null?Boolean.TRUE:extendConfigInfoJson.getBoolean("isProxy");
//        Map<String,Object> result=httpProxyClient.request(customer.getPushUrl().trim(),param.toJSONString(),isProxy);
//        Long end =System.currentTimeMillis();
//        String resultStr=result.get("data")!=null?result.get("data").toString():"";
//        String code="9999";
//        if(StringUtils.isNotBlank(resultStr)){
//            try{
//                JSONObject resultJson =JSONObject.parseObject(resultStr);
//                code=resultJson.getString("code");
//            }catch (Exception e){
//
//            }
//
//        }
//        if(!(Boolean) result.get("result")){
//            PushErrorLog pushErrorLog =new PushErrorLog();
//            pushErrorLog.setCreateTime(new Date());
//            pushErrorLog.setUpdateTime(new Date());
//            pushErrorLog.setApiCode(customer.getApiCode());
//            pushErrorLog.setBatchNumber(marketingHistoryList.get(0).getBatchNumber());
//            pushErrorLog.setActualPushTimes(0);
//            pushErrorLog.setRequestStr(param.toJSONString());
//            pushErrorLog.setFileId(Long.valueOf(marketingHistoryList.get(0).getFileId()));
//            pushErrorLog.setResponseStr(resultStr);
//            pushErrorLog.setPushTimes(3);
//            pushErrorLog.setStatus(2);
//            pushErrorLogMapper.insertSelective(pushErrorLog);
//        }
//        MomUtil.sendMom(customer.getApiCode(),param.toJSONString(),resultStr,end-begin,param.getString("requestId"),code);
//
//        return null;
//    }
//
//
//
//
//}
