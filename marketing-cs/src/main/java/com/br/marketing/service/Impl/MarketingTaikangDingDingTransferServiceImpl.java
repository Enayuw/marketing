package com.br.marketing.service.Impl;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.StringUtils;
import com.br.marketing.client.taikang.TaikangClient;
import com.br.marketing.client.taikang.TaikangMarketingEvent;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.TaikangDingDingDataRecord;
import com.br.marketing.entity.TaikangDingDingTransferDetail;
import com.br.marketing.entity.TaikangTransferDataLog;
import com.br.marketing.mapper.TaikangDingDingDataRecordMapper;
import com.br.marketing.mapper.TaikangDingDingTransferDetailMapper;
import com.br.marketing.mapper.TaikangTransferDataLogMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.MarketingTaikangDingDingTransferService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MarketingTaikangDingDingTransferServiceImpl implements MarketingTaikangDingDingTransferService {

    private final static String TITLE = "【泰康大健康线索-钉钉数据回传任务】";

    public static final DateTimeFormatter ymdhmsFormat = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Resource
    private TaikangDingDingDataRecordMapper taikangDingDingDataRecordMapper;

    @Resource
    private TaikangDingDingTransferDetailMapper taikangDingDingTransferDetailMapper;

    @Resource
    private TaikangTransferDataLogMapper taikangTransferDataLogMapper;

    @Resource
    private TaikangClient taikangClient;


    @Override
    public void process(String apiCode) {
        log.warn("TITLE:{},apiCode:{},开始处理",TITLE,apiCode);
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.
                getThreadPool(ThreadPoolNameEnum.TAIKANG_DINGDING_TRANSFER.getName(), 5, 5);
        try {
            // 1.泰康钉钉数据迁移
            taiKangDingDingDataMove(apiCode,actionPool);
            // 2.泰康数据回调
            taiKangDingDingDataTransfer(apiCode,actionPool);
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAIKANG_DINGDING_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }finally {
            log.warn("TITLE:{},apiCode:{},处理完成",TITLE,apiCode);
            actionPool.shutdownAndAwaitTermination();
        }
    }


    /**
     *  1.泰康钉钉数据迁移
     * @param apiCode
     * @param actionPool
     */
    private void taiKangDingDingDataMove(String apiCode,TpDynamicExecutor actionPool) {
        int totalCount = 0;
        Long searchId = 0L;
        LocalDateTime now = LocalDateTime.now();
        String nowTime = getAlignedTime(now);
        String previousTime = getAlignedTime(now.minusMinutes(30));
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        while (true) {
            Map<String, String> taikangConfig = marketingCommonConfig.getTaikangConfig();
            if ("true".equals(taikangConfig.get("ddJobStopSwitch"))) {
                break;
            }
            int searchSize = Integer.parseInt(taikangConfig.getOrDefault("ddJobPageSize", "100"));
            List<TaikangDingDingDataRecord> taikangDDRecordList =
                    taikangDingDingDataRecordMapper.selectRecordList(searchId,searchSize,previousTime,nowTime);
            if (CollectionUtils.isEmpty(taikangDDRecordList)) {
                break;
            }
            futures.add(CompletableFuture.runAsync(() ->
                    callBackDataDealMove(apiCode,taikangDDRecordList),actionPool));
            totalCount += taikangDDRecordList.size();
            searchId = taikangDDRecordList.get(taikangDDRecordList.size()-1).getId();
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.warn("TITLE:{},api_code:{},{}-{},move完成,新增数据{}条", TITLE,apiCode,previousTime,nowTime,totalCount);
    }


    private void callBackDataDealMove(String apiCode, List<TaikangDingDingDataRecord> taikangDDRecordList) {
        List<TaikangDingDingTransferDetail> transferDetailList = taikangDDRecordList.stream()
                .map(item -> {
                    TaikangDingDingTransferDetail transferDetail = new TaikangDingDingTransferDetail();
                    transferDetail.setApiCode(apiCode);
                    transferDetail.setCell(item.getCell());
                    transferDetail.setBrowseDate(item.getBrowseDate());
                    transferDetail.setApplicationName(item.getApplicationName());
                    transferDetail.setReturnResult1(item.getReturnResult1());
                    transferDetail.setStatus(1);
                    transferDetail.setPushStatus(0);
                    String applicantPhone = RpcClientProxy.decode(item.getCell(), "cell", "md5", "");
                    boolean phoneEmpty = StringUtils.isEmpty(applicantPhone);
                    boolean dateEmpty = StringUtils.isEmpty(item.getBrowseDate());
                    if (phoneEmpty || dateEmpty) {
                        transferDetail.setStatus(0);
                        transferDetail.setErrorMsg((phoneEmpty ? 1 : 0) + (dateEmpty ? 2 : 0));
                    }
                    return transferDetail;
                }).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(transferDetailList)) {
            taikangDingDingTransferDetailMapper.batchInsert(transferDetailList);
        }
    }



    /**
     * 2.泰康数据回传 3.泰康钉钉回调数据入库
     *
     * @param apiCode
     * @param actionPool
     */
    private void taiKangDingDingDataTransfer(String apiCode,TpDynamicExecutor actionPool) {
        int totalCount = 0;
        Long searchId = 0L;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        while (true) {
            Map<String, String> taikangConfig = marketingCommonConfig.getTaikangConfig();
            if ("true".equals(taikangConfig.get("ddJobStopSwitch"))) {
                break;
            }
            int searchSize = Integer.parseInt(taikangConfig.getOrDefault("ddJobPageSize", "100"));
            List<TaikangDingDingTransferDetail> taikangDDTransferList =
                    taikangDingDingTransferDetailMapper.selectDetailList(searchId,searchSize,0);
            if (CollectionUtils.isEmpty(taikangDDTransferList)) {
                break;
            }
            List<Long> idList = taikangDDTransferList.stream().map(TaikangDingDingTransferDetail::getId).toList();
            taikangDingDingTransferDetailMapper.updatePushStatusByIds(idList,1);

            futures.add( CompletableFuture.runAsync(() ->
                    callBackDataDealTransfer(taikangDDTransferList),actionPool));

            searchId = idList.get(idList.size()-1);
            totalCount += taikangDDTransferList.size();
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.warn("TITLE:{},api_code:{},transfer完成,新增数据{}条", TITLE,apiCode,totalCount);
    }

    private void callBackDataDealTransfer(List<TaikangDingDingTransferDetail> taikangDDTransferDetailList) {
        Map<String, String> taikangConfig = marketingCommonConfig.getTaikangConfig();

        taikangDDTransferDetailList.forEach(item -> {
            try {
                // 1.封装数据
                TaikangMarketingEvent taikangMarketingEvent = new TaikangMarketingEvent();
                String applicantPhone = RpcClientProxy.decode(item.getCell(), "cell", "md5", "");
                taikangMarketingEvent.setApplicantPhone(applicantPhone);
                taikangMarketingEvent.setBrowseDate(item.getBrowseDate());
                if (!StringUtils.isEmpty(item.getApplicationName())) {
                    taikangMarketingEvent.setApplicantName(item.getApplicationName());
                }
                if (!StringUtils.isEmpty(item.getReturnResult1())) {
                    taikangMarketingEvent.setRemark(item.getReturnResult1());
                }
                // 2.泰康回传数据
                String response;
                if ("true".equals(taikangConfig.get("ddMockSwitch"))) {
                    response = generateMockResponse();
                }else {
                    response = taikangClient.process(taikangMarketingEvent);
                }
                // 3.记录日志
                TaikangTransferDataLog taikangTransferDataLog = new TaikangTransferDataLog();
                taikangTransferDataLog.setDataType(2);
                taikangTransferDataLog.setDdRecordId(item.getId());
                taikangTransferDataLog.setApiCode(item.getApiCode());
                taikangTransferDataLog.setCell(item.getCell());
                taikangTransferDataLog.setName(item.getApplicationName());
                String httpCode = Optional.ofNullable(response)
                        .map(MarketingTaikangDingDingTransferServiceImpl::safeParseToJson)
                        .map((JSONObject res) -> res.getString("httpcode"))
                        .orElse(null);
                taikangTransferDataLog.setHttpCode(httpCode);
                String businessCode = Optional.ofNullable(response)
                        .map(MarketingTaikangDingDingTransferServiceImpl::safeParseToJson)
                        .map((JSONObject res) -> res.getString("content"))
                        .map(MarketingTaikangDingDingTransferServiceImpl::safeParseToJson)
                        .map((JSONObject cnt) -> cnt.getString("code"))
                        .orElse(null);
                taikangTransferDataLog.setBusinessCode(businessCode);
                taikangTransferDataLog.setReturnContent(response);
                taikangTransferDataLogMapper.insertSelective(taikangTransferDataLog);

                // 4.修改泰康-钉钉详情数据 推送状态
                boolean success = "200".equals(httpCode) && "0000".equals(businessCode);
                item.setPushStatus(success ? 2 : 3);
                item.setUpdateTime(new Date());
                taikangDingDingTransferDetailMapper.updateByPrimaryKey(item);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAIKANG_DINGDING_SERVICEERROR.getCode(),
                        "泰康大健康线索线索推送客户-钉钉记录日志异常，拨打明细id:" + item.getId()));
            }
        });
    }

    /**
     * mock数据
     * {"httpcode":"200","content":"{\"code\":\"0000\",\"message\":\"success\",\"timestamp\":\"20260122163610\",\"data\":\"\"}"}
     */
    private String generateMockResponse() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        JSONObject response = new JSONObject();
        response.put("httpcode", "200");

        JSONObject content = new JSONObject();
        content.put("code", "0000");
        content.put("message", "success");
        content.put("timestamp", timestamp);
        content.put("data", "");
        response.put("content", content.toJSONString());
        return response.toJSONString();
    }


    /**
     * 安全解析 JSON 字符串为 JSONObject，解析失败返回 null 并记录日志
     */
    private static JSONObject safeParseToJson(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return null;
        }
        try {
            return JSONObject.parseObject(jsonStr);
        } catch (Exception e) {
            // 记录解析失败但不抛异常，便于 Optional 链继续工作
            log.warn("解析 JSON 失败，input: {}", jsonStr, e);
            return null;
        }
    }

    /**
     * 获取当前时间对应的30分钟节点时间
     * 目前时间2026-01-27 21:07:00(查询上个30分钟时间周期)
     * 查询 2026-01-27 20:30:00 <= created_time  and created_time < 2026-01-27 21:00:00
     */
    private String getAlignedTime(LocalDateTime dateTime) {
        return dateTime.withMinute((dateTime.getMinute() / 30) * 30)
                .withSecond(0)
                .withNano(0)
                .format(ymdhmsFormat);
    }
}
