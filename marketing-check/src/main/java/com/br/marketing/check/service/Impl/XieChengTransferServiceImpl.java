package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.XieChengTransferService;
import com.br.marketing.entity.XieChengSmsCollidingDataLog;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.XieChengSmsCollidingDataLogMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhenLi
 * @Description 携程转化数据处理service
 * @Date 2023/03/08 20:09
 */
@Service
@Slf4j
public class XieChengTransferServiceImpl implements XieChengTransferService {

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private XieChengSmsCollidingDataLogMapper xieChengSmsCollidingDataLogMapper;

    private static final String XIECHENGAPICODE = "3710058";

    @Override
    public void pushDataToPolicy() {
        String requestDay = LocalDate.now().minusDays(1).toString();
        String tcId = tableCreateService.getTcId(XIECHENGAPICODE);
        Set<String> cellSets = new HashSet<>();
        PushStatusAHandler(requestDay, tcId, cellSets);
        PushStatusBHandler(requestDay, tcId, cellSets);
        PushStatusCHandler(requestDay, tcId, cellSets);
    }

    private void PushStatusCHandler(String requestDay, String tcId, Set<String> cellSets) {
    }

    private void PushStatusBHandler(String requestDay, String tcId, Set<String> cellSets) {
    }

    private void PushStatusAHandler(String requestDate, String tcId, Set<String> cellSets) {
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        int totalSize = 0;
        while (mark) {
            List<String> custNums = marketingTransferSyncUserMapper.getConvtypeData(tcId, page * 2000, requestDate, "214");
            if (CollectionUtils.isEmpty(custNums)) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<String> filterCustNums = marketingTransferSyncUserMapper.getCustNumAndConvtypeData(tcId, custNums, requestDate, "reserve_field1->>'$.convType' !='214'");
            custNums.removeAll(filterCustNums);
            cellSets.addAll(custNums);
            //获取orgChannel和result
            Map<String, XieChengSmsCollidingDataLog> smsCollidingDataLogMap = getSmsCollidingData(cellSets);
            //推送决策
            pushPolicy(cellSets, smsCollidingDataLogMap);
        }
    }

    private void pushPolicy(Set<String> cellSets, Map<String, XieChengSmsCollidingDataLog> smsCollidingDataLogMap) {


    }

    private Map<String, XieChengSmsCollidingDataLog> getSmsCollidingData(Set<String> cellSets) {

        List<XieChengSmsCollidingDataLog> xieChengSmsCollidingDataLogs = xieChengSmsCollidingDataLogMapper.getDataByCells(Lists.newArrayList(cellSets));

        Map<String, XieChengSmsCollidingDataLog> smsCollidingDataLogMap = xieChengSmsCollidingDataLogs.stream().collect(
                Collectors.groupingBy(XieChengSmsCollidingDataLog::getSha256CodeList
                        , Collectors.collectingAndThen(
                                Collectors.reducing((v1, v2) ->
                                        v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                , Optional::get)));

        return smsCollidingDataLogMap;

    }
}
