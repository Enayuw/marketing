package com.br.marketing.check.service.Impl.sushang;

import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.SushangCallRecordDataMapper;
import com.br.marketing.mapper.SushangPushResultDataMapper;
import com.br.marketing.mapper.SushangTransferDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 苏商推送通话明细实现
 *
 * @author zhen.Li1
 * @dateTime 2024/07/15 17:32
 */
@Service
@Slf4j
public class SuShangPushServiceImpl implements SuShangPushService {

    @Autowired
    private SushangTransferDataMapper sushangTransferDataMapper;

    @Autowired
    private SushangCallRecordDataMapper sushangCallRecordDataMapper;

    @Autowired
    private SushangPushResultDataMapper sushangPushResultDataMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private LocalFileMapper localFileMapper;

    @Override
    public void pushCallRecordHandler(LocalFile localFile, LocalFile callRecordFile) {
        Long transferLocalId = localFile.getId();
        Long callRecordLocalId = callRecordFile.getId();
        ThreadPoolExecutor transferPool = BrExecutors.getThreadPool(50, 50, 50);
        ThreadPoolExecutor callRecordPool = BrExecutors.getThreadPool(50, 50, 50);
        //
        Long indexId = null;
        Integer pageSize = marketingCommonConfig.getSuShangSearchPageSize();
        while (true) {
            List<SushangTransferData> sushangTransferList = sushangTransferDataMapper.getMinOrderDateDatatikv_(transferLocalId,
                    indexId, pageSize);
            if (CollectionUtils.isEmpty(sushangTransferList)) {
                break;
            }
            indexId = sushangTransferList.get(sushangTransferList.size() - 1).getId();
            modifyCorePoolSize(transferPool);
            List<List<SushangTransferData>> partition = Lists.partition(sushangTransferList, 100);
            partition.forEach((List<SushangTransferData> sushangTransferData) -> {
                transferPool.submit(() -> pushDealData(sushangTransferData, callRecordLocalId));
            });
        }
        // 关闭线程池
        transferPool.shutdown();
        try {
            while (!transferPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        //插入180天通话明细数据
        indexId = null;
        String beginDate = LocalDate.now().minusDays(179).toString();
        String endDate = LocalDate.now().minusDays(1).toString();

        while (true) {
            List<SushangCallRecordData> callRecordDataList = sushangCallRecordDataMapper.getHalfYearCallRecord(callRecordLocalId,
                    indexId, pageSize, beginDate, endDate);
            if (CollectionUtils.isEmpty(callRecordDataList)) {
                break;
            }
            indexId = callRecordDataList.get(callRecordDataList.size() - 1).getId();
            modifyCorePoolSize(callRecordPool);
            List<List<SushangCallRecordData>> partition = Lists.partition(callRecordDataList, 500);
            partition.forEach((List<SushangCallRecordData> callRecordData) -> {
                callRecordPool.submit(() -> pushNoDealData(callRecordData));
            });
        }
        // 关闭线程池
        callRecordPool.shutdown();
        try {
            while (!callRecordPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        //更新为推送成功状态
        localFile.setPushStatus("2");
        localFile.setId(localFile.getId());
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }

    private void pushNoDealData(List<SushangCallRecordData> callRecordData) {
        try {
            List<SushangPushResultData> resultDataList = new ArrayList<>();
            List<String> custNums = callRecordData.stream().map(SushangCallRecordData::getCustNum).collect(Collectors.toList());
            String date = LocalDate.now().toString();
            List<SushangPushResultData> pushDealList = sushangPushResultDataMapper.getDealDataByCustNum(custNums, date);
            Set<String> dealCustNums = pushDealList.stream().map(SushangPushResultData::getCustNum).collect(Collectors.toSet());
            //剔除
            callRecordData.removeIf(recordData -> dealCustNums.contains(recordData.getCustNum()));
            callRecordData.forEach(callRecord -> {
                SushangPushResultData pushResultData = new SushangPushResultData();
                BeanUtils.copyProperties(callRecord, pushResultData);
                pushResultData.setCreateTime(new Date());
                pushResultData.setUpdateTime(new Date());
                pushResultData.setUploadDate(LocalDate.now().toString());
                pushResultData.setRule(2);
                pushResultData.setStatus(1);
                resultDataList.add(pushResultData);
            });
            //批量插入
            sushangPushResultDataMapper.insertBatch(resultDataList);
        } catch (Exception e) {
            log.error("苏商银行规则二插入通话明细异常", e.getMessage());
        }
    }

    private void pushDealData(List<SushangTransferData> transferList, Long callRecordLocalId) {
        try {
            List<SushangPushResultData> resultDataList = new ArrayList<>();
            for (SushangTransferData sushangTransferData : transferList) {
                String minDealTime = sushangTransferData.getExtend03();
                String custNum = sushangTransferData.getCustNum();
                SushangCallRecordData callRecordData = sushangCallRecordDataMapper.getLastedCallData(callRecordLocalId, minDealTime, custNum);
                List<SushangCallRecordData> callRecordDataList = sushangCallRecordDataMapper.getCallRecordList(callRecordLocalId,
                        callRecordData.getCallTime(), callRecordData.getCustNum());
                for (SushangCallRecordData callRecord : callRecordDataList) {
                    SushangPushResultData pushResultData = new SushangPushResultData();
                    BeanUtils.copyProperties(callRecord, pushResultData);
                    pushResultData.setCreateTime(new Date());
                    pushResultData.setUpdateTime(new Date());
                    pushResultData.setUploadDate(LocalDate.now().toString());
                    pushResultData.setRule(1);
                    pushResultData.setStatus(1);
                    resultDataList.add(pushResultData);
                }
            }
            //批量插入
            sushangPushResultDataMapper.insertBatch(resultDataList);
        } catch (Exception e) {
            log.error("苏商银行规则一插入通话明细异常", e.getMessage());
        }
    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool) {
        Integer threadNum =
                marketingCommonConfig.getSuShangPushThreadNum();
        pool.setCorePoolSize(threadNum);
        pool.setMaximumPoolSize(threadNum);
        log.warn("苏商推送通话明细线程数core={}，max={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());

    }
}
