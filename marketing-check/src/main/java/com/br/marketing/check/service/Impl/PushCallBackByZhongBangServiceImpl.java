package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.check.service.PushCallBackService;
import com.br.marketing.check.service.PushCustomerService;
import com.br.marketing.client.zbank.ZbankClient;
import com.br.marketing.client.zbank.ZbankResponse;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.zbank.ZbankLabelRatingReResultDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.CallBackScoreResourceEnum;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.PushCustomerDetailMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.scorepushcustomer.ScoreSortJsonVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class PushCallBackByZhongBangServiceImpl implements PushCallBackService {

    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    PushCustomerDetailMapper pushCustomerDetailMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Value("${api.zbank.api.appId:2a0f9f71_29e5_466c_95a7_8cab99d93880}")
    private String appId;

    @Autowired
    ZbankClient zbankClient;

    @Autowired
    PushCustomerService pushCustomerService;

    @Override
    public void pushCustomer(StraHisFile straHisFile, List<ScoreSortJsonVO> vos, AtomicInteger error, ScorePushCustomerConfig pushCustomerConfig) {
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(straHisFile.getApiCode()).andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if (marketingCustomers.size() <= 0) {
            log.warn(String.format("【%s】客户被删除!", straHisFile.getApiCode()));
            return;
        }
        MarketingCustomer marketingCustomer = marketingCustomers.get(0);
        int pushThream = pushCustomerService.getPushCustomerResource(pushCustomerConfig, CallBackScoreResourceEnum.PushCustomerThreadNumber);
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(pushThream, pushThream, "job_pushCustomer");
        Integer pageIndex = 0;
        Integer pageSize = pushCustomerService.getPushCustomerResource(pushCustomerConfig, CallBackScoreResourceEnum.PushTaskPageByZhongBangNumber);
        Integer dataPageSize = pushCustomerService.getPushCustomerResource(pushCustomerConfig, CallBackScoreResourceEnum.PushCustomerDataPageNumber);
        Boolean taskAction = Boolean.TRUE;
        while (taskAction) {
            Integer start = pageIndex * pageSize;
            List<String> taskId = pushCustomerDetailMapper.getTaskId(straHisFile.getId(), start, pageSize);
            if (taskId.size() <= 0) {
                taskAction = Boolean.FALSE;
                continue;
            }
            log.warn(String.format("分页：%d", start) + JSON.toJSONString(taskId));
            pageIndex++;
            for (String s : taskId) {
                Boolean dataAction = Boolean.TRUE;
                Long minId = null;
                while (dataAction) {
                    PushCustomerDetailExample pushCustomerDetailExample = new PushCustomerDetailExample();
                    pushCustomerDetailExample.setOrderByClause(String.format(" id limit %d", dataPageSize));
                    PushCustomerDetailExample.Criteria criteria = pushCustomerDetailExample.createCriteria();
                    criteria.andFileIdEqualTo(straHisFile.getId()).andTaskIdEqualTo(s).andPushStatusIn(Arrays.asList(1, 3));
                    if (minId != null) {
                        criteria.andIdGreaterThan(minId);
                    }
                    List<PushCustomerDetail> pushCustomerDetails = pushCustomerDetailMapper.selectByExample(pushCustomerDetailExample);
                    if (pushCustomerDetails.size() <= 0) {
                        dataAction = Boolean.FALSE;
                        continue;
                    }
                    minId = pushCustomerDetails.get(pushCustomerDetails.size() - 1).getId();
                    pushPool.submit(() -> {
                        try {
                            JSONObject reqJb = new JSONObject();
                            JSONObject request = new JSONObject();
                            JSONArray cstInfoArray = new JSONArray();
                            reqJb.put("request", request);
                            request.put("CstInfoArray", cstInfoArray);
                            request.put("TxnSrlNo", appId + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                    + RandomStringUtils.randomNumeric(8));
                            request.put("TskId", s);
                            request.put("TxnDt", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                            request.put("TxnTs", LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmssSSS")));
                            request.put("RqsSeqNo", marketingCustomer.getApiCode()
                                    + "_" + request.getString("TskId")
                                    + "_" + UUID.randomUUID().toString());
                            List<Long> detailIds = new ArrayList<>();
                            for (PushCustomerDetail pushCustomerDetail : pushCustomerDetails) {
                                JSONObject cstInfo = new JSONObject();
                                cstInfo.put("GrpTp", pushCustomerDetail.getUserType());
                                detailIds.add(pushCustomerDetail.getId());
                                for (ScoreSortJsonVO vo : vos) {
                                    cstInfo.put(vo.getMappingKey(), getScoreSortByDb(vo.getDbNumber(), pushCustomerDetail));
                                }
                                cstInfo.put("CstNo", pushCustomerDetail.getCustNum());
                                cstInfoArray.add(cstInfo);
                            }
                            //region push
                            PushCustomerDetailExample example = new PushCustomerDetailExample();
                            example.createCriteria().andIdIn(detailIds);
                            PushCustomerDetail update = new PushCustomerDetail();
                            String rqsSeqNo = "";
                            try {
//                                pushCustomerService.mockError("3");
                                rqsSeqNo = zbankClient.cMBrScoDaFeBack(reqJb, request.getString("RqsSeqNo"));
                                ZbankResponse<ZbankLabelRatingReResultDTO> rqZbank = JSONObject.parseObject(rqsSeqNo
                                        , new TypeReference<ZbankResponse<ZbankLabelRatingReResultDTO>>() {
                                        });
                                if ("000000".equals(rqZbank.getCode())) {
                                    ZbankLabelRatingReResultDTO result1 = rqZbank.getResult();
                                    if ("00".equals(result1.getErrCd())) {
                                        update.setPushStatus(2);
                                    } else {
                                        update.setPushStatus(3);
                                        error.incrementAndGet();
                                    }
                                } else {
                                    update.setPushStatus(3);
                                    error.incrementAndGet();
                                }
                            } catch (Exception ex) {
                                log.error(ex.getMessage() + "响应：" + rqsSeqNo, ex);
                                update.setPushStatus(3);
                                error.incrementAndGet();
                            }
                            pushCustomerDetailMapper.updateByExampleSelective(update, example);
                            //endregion
                        } catch (Exception e) {
                            error.incrementAndGet();
                            log.error("推送客户线程报错" + e.getMessage(), e);
                        }
                    });

                }
            }

        }
        try {
            waitThreadPool(pushPool);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
