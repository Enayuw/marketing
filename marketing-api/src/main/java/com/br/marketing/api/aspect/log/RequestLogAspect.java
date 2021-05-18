package com.br.marketing.api.aspect.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.bsf.ext.app.util.Ice1BSFConsumerBean;
import com.br.marketing.api.bean.RequestLog;
import com.br.marketing.api.client.DecodeClient;
import com.br.marketing.api.client.IceClient;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entity.MerchantParam;
import com.br.marketing.api.service.AlarmCs;
import com.br.marketing.common.constants.web.ResponseCode;
import com.br.marketing.common.utils.EncodeUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.net.IpUtil;
import com.br.mom.broker_layer_api.api.BrokerLayerServicePrx;
import com.br.mom.broker_layer_api.api.ResponseBean;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/** 接口请求日志管理切面
 * 为单条与批量推送或查询接口记录日志，并持久化到数据库中
 * @author Wang Weiwei
 * @since 2018/1/10
 */
@Aspect
@Slf4j
@Component
@Order(100)
public class RequestLogAspect {

    @Resource(name = "ThreadPool")
    private ThreadPoolTaskExecutor threadPool;
    @Resource
    private AlarmCs alarmCs;
    @Resource
    private IceClient iceClient;
    @Value("${otherConfig.mq.destinationName:00}")
    private String destinationName;
    @Value("${otherConfig.mq.appName:00}")
    private String appName;
    @Value("${otherConfig.mq.appSecretKey:00}")
    private String appSecretKey;

    /**
     * The Decode client.
     */
    @Resource
    DecodeClient decodeClient;


    /**
     * Batch query string.
     *
     * @param joinPoint the join point
     * @return the string
     */
    @Around(value = "com.br.marketing.api.aspect.log.RequestLogJoinPoint.query()")
    public String batchQuery(ProceedingJoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();
        return invoker(args, joinPoint, "query");
    }

    private String invoker(Object[] args, ProceedingJoinPoint joinPoint, String url) {
        GetParamter getParamter = new GetParamter(joinPoint).invoke();
        StrategyApiContext context = getParamter.getContext();
        String str = getParamter.getJsonData();
        JSONObject jsonData = null;
        try {
            jsonData = JSONObject.parseObject(str);
        } catch (Exception e) {
            log.error("总jsonData参数错误  ---   {} --参数str{}", e,str);
        }
        if (jsonData != null){
            if (jsonData.containsKey("requestType")){
                context.setRequestType(jsonData.getString("requestType"));
            }
            context.setJsonData(jsonData.getString("jsonData"));
            context.setReqData(new StringBuilder().append(jsonData.getString("jsonData")));
            context.setDeleteTime(jsonData.getString("deleteTime"));
            context.setStrategyId(jsonData.getString("strategyId"));
            if (jsonData.containsKey("swiftNumber")){
                context.setSwiftNumber(jsonData.getString("swiftNumber"));
            }
        }
        //拿到用户中心的配置，放到策略上下文里，以供后面使用
        MerchantParam merchantParam=null;
        try{
            merchantParam =  iceClient.getMerchantParam(context.getApiCode());
        }catch (Exception e){
            if(null == context.getApiCode()){
                log.error("从用户中心请求用户信息出错--",e);
            }else{
                log.error("从用户中心请求用户信息出错--ApiCode:{}",context.getApiCode(),e);
            }
        }
        context.setMerchantParam(merchantParam);

        Result result = new Result();
        result.putCode(ResponseCode.ERR_SYSTEM);
        RequestLog requestLog = new RequestLog();
        requestLog.setRequestTime(new Date());
        try {
            requestLog.setApiCode(context.getApiCode());
        }catch (Exception e){
            log.warn("Exception ",e);
            requestLog.setApiCode("");
        }
        try {
            context.setJsonData(context.getJsonData() == null ? "" :context.getJsonData().toString());
            requestLog.setRequestStr(JSONObject.toJSONString(context));
            requestLog.setIp(IpUtil.getIpAddr(getParamter.getRequest()));
            result = new Result(joinPoint.proceed(args).toString());
        }catch (Throwable e){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(30 * 1024);
            PrintStream printStream = new PrintStream(outputStream);
            e.printStackTrace(printStream);
            String errorString = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            requestLog.setError(errorString);
            alarmCs.handle(e,"api","loan-waring");
            log.error("策略请求错误   ---   {}", errorString);
        }
        requestLog.setResponseStr(result.toString());
        requestLog.setResponseTime(new Date());
        //log.warn("接口耗时---{}",requestLog.getResponseTime().getTime() - requestLog.getRequestTime().getTime());
        requestLog.setCostTime(requestLog.getResponseTime().getTime() - requestLog.getRequestTime().getTime());
        requestLog.setSwiftNumber(result.getSwiftNumber());
        requestLog.setUrl(url);
        requestLog.setCode(result.getCode());

        insertLog(requestLog,merchantParam);
        //log.warn("[{}] 接口请求---code：{}-------- SwiftNumber：{}", url,requestLog.getCode(), requestLog.getSwiftNumber());
        return result.toString();
    }
    private void sendMq( RequestLog requestLog){
        String responseStr = requestLog.getResponseStr();
        JSONObject responseJson=JSONObject.parseObject(responseStr);
        Set<String> strings = responseJson.keySet();
        if(strings.contains("ResultArray")){
            responseJson.remove("ResultArray");
            requestLog.setResponseStr(responseJson.toJSONString());
        }
        JSONObject paramJson=new JSONObject();
        JSONObject	requestData=new JSONObject();
        requestData.put("destinationName",destinationName);
        paramJson.put("appName",appName);
        paramJson.put("appSecretKey",appSecretKey);
        BrokerLayerServicePrx service = (BrokerLayerServicePrx) Ice1BSFConsumerBean.getServiceProxy(BrokerLayerServicePrx.class, "V3.0.0");
        service= (BrokerLayerServicePrx) service.ice_connectionCached(false);
        String param = JSON.toJSONString(requestLog);
        paramJson.put("swiftNum", UUID.randomUUID());
        requestData.put("content",param);
        paramJson.put("requestData",requestData);
        log.info("MQ入参--{}",paramJson);
       /* log.info("MQ入参--appName:{}--appSecretKey:{}--swiftNum:{}--destinationName:{}",paramJson.getString("appName"),
               paramJson.getString("appSecretKey"),paramJson.getString("swiftNum"),requestData.getString("destinationName"));*/
        try {
            long l = System.currentTimeMillis();
           ResponseBean sender = service.sender(paramJson.toString());
           // AsyncResult asyncResult = service.begin_sender(paramJson.toString());
            //log.warn("mom  request log return : {}  ", sender.toString());
            log.warn("mom  request log return : {}  ,{}", sender.getMessage(),sender.getResult());
            log.info("MQ耗时--{}",System.currentTimeMillis()-l);

        }catch (Exception e){
            log.error("日志信息写入消息队列异常",e);
        }
    }

    private String decodeIdCell(String jsonData,MerchantParam merchantParam){
        try {
            //若客户传入的是明文，则先解密再加密入库
            if (jsonData.startsWith("{") && jsonData.endsWith("}")) {
                JSONObject json = JSONObject.parseObject(jsonData);
                decryptIdCellName(json,merchantParam,merchantParam.getIsCheck());
                jsonData = json.toString();
            } else if (jsonData.startsWith("[") && jsonData.endsWith("]")) {
                JSONArray array = JSONArray.parseArray(jsonData);
                JSONArray decryptArray = new JSONArray();
                for (int i = 0; i < array.size(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    decryptIdCellName(jsonObject,merchantParam,merchantParam.getIsCheck());
                    decryptArray.add(jsonObject);
                }
                jsonData = decryptArray.toString();
            }
        }catch(Exception e){
            log.error("JSON处理出错----{}----{}",jsonData,e);
        }
        return jsonData;
    }
    private String encodeIdCell(String req, MerchantParam merchantParam){
        JSONObject parse = (JSONObject)JSONObject.parse(req);
        String jsonData = parse.getString("jsonData");
        String reqData = parse.getString("reqData");
        jsonData= decodeIdCell(jsonData,merchantParam);
        reqData= decodeIdCell(reqData,merchantParam);
        jsonData=EncodeUtil.encode(jsonData,"id,idCard,cell,name");
        reqData=EncodeUtil.encode(reqData,"id,idCard,cell,name");
        parse.put("reqData",reqData);
        parse.put("jsonData",jsonData);
        req=parse.toJSONString();
        return req;
    }

    private  void decryptIdCellName(JSONObject json, MerchantParam merchantParam,int isCheck){
        try {
            String idCard = json.getString("idCard");
            idCard=  decodeClient.decode("id",idCard,merchantParam.getRequestCode(),merchantParam.getDecryptKey(),isCheck);
            String cell = json.getString("cell");
            cell=  decodeClient.decode("cell",cell,merchantParam.getRequestCode(),merchantParam.getDecryptKey(),isCheck);
            String name = json.getString("name");
            name=  decodeClient.decode("name",name,merchantParam.getRequestCode(),merchantParam.getDecryptKey(),isCheck);
            if(StringUtils.isNotEmpty(idCard)){
                json.put("idCard", idCard);
            }
            if(StringUtils.isNotEmpty(cell)){
                json.put("cell", cell);
            }
            if(StringUtils.isNotEmpty(name)){
                json.put("name", name);
            }
        }catch (Exception e){
            log.error("日志入库解密出错---{}--json:{} --apiCode:{}",e,json.toJSONString(),merchantParam.getApiCode());
        }
    }
    /**
     * 将请求日志插入到请求日志表
     * 该存库操作为异步操作
     * */
    private void insertLog(final RequestLog requestLog,final MerchantParam merc) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // requestLogCS.insert(requestLog);
                    //入库日志 id,idCard,cell加密
        String requestStr = requestLog.getRequestStr();
        requestStr=encodeIdCell(requestStr,merc);
        String responseStr = requestLog.getResponseStr();
        responseStr= EncodeUtil.encodeDefault(responseStr);

        requestLog.setRequestStr(requestStr);
        requestLog.setResponseStr(responseStr);
                    sendMq(requestLog);
                }catch (Exception e){
                    log.error("记录请求日志错误", e);
                }
            }
        });
    }

}
