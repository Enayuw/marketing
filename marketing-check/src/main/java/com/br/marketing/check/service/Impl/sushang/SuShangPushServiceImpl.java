package com.br.marketing.check.service.Impl.sushang;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.SushangCallRecordDataMapper;
import com.br.marketing.mapper.SushangPushResultDataMapper;
import com.br.marketing.mapper.SushangTransferDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
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
        //转化数据（已成交）推送
        long start = System.currentTimeMillis();
        Long indexId = null;
        Integer pageSize = marketingCommonConfig.getSuShangSearchPageSize();
        while (true) {
            //取extend03非空，最早（距离当前最远）的已成交数据
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
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode(), "苏商推送规则一线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }
        log.warn("苏商推送规则一(已成交)运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
        //插入180天通话明细数据
        long startTwo = System.currentTimeMillis();
        Integer page = 0;
        String beginDate = LocalDate.now().minusDays(179).toString();
        String endDate = LocalDate.now().minusDays(1).toString();
        //全局去重custNum集合
        HashSet custNumALL = new HashSet();
        while (true) {
            //获取180天的custNum,按call_time排序
            Integer limitStart = page * pageSize;
            List<SushangCallRecordData> callRecordDataList = sushangCallRecordDataMapper.getHalfYearCallRecordtikv_(callRecordLocalId,
                    beginDate, endDate, limitStart, pageSize);
            if (CollectionUtils.isEmpty(callRecordDataList)) {
                break;
            }
            page++;
            List<SushangCallRecordData> callRecordDataNewList = new ArrayList<>();
            callRecordDataList.forEach((SushangCallRecordData callData) -> {
                //添加set集合去重
                if (custNumALL.add(callData.getCustNum())) {
                    callRecordDataNewList.add(callData);
                }
            });
            if (CollectionUtils.isEmpty(callRecordDataNewList)) {
                continue;
            }
            modifyCorePoolSize(callRecordPool);
            List<List<SushangCallRecordData>> partition = Lists.partition(callRecordDataNewList, 500);
            partition.forEach((List<SushangCallRecordData> callRecordData) -> {
                List<SushangCallRecordData> list = new ArrayList<>();
                list.addAll(callRecordData);
                callRecordPool.submit(() -> pushNoDealData(list));
            });
        }
        // 关闭线程池
        callRecordPool.shutdown();
        try {
            while (!callRecordPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode(), "苏商推送规则二线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }
        log.warn("苏商推送规则二(非成交)运行耗时：{}s", (System.currentTimeMillis() - startTwo) / 1000);
        custNumALL.clear();
        //更新转化文件记录为推送成功状态
        localFile.setPushStatus("2");
        localFile.setId(localFile.getId());
        localFileMapper.updateByPrimaryKeySelective(localFile);
        //更新通话明细记录为推送成功状态
        callRecordFile.setPushStatus("2");
        callRecordFile.setId(callRecordFile.getId());
        localFileMapper.updateByPrimaryKeySelective(callRecordFile);
    }

    private void pushNoDealData(List<SushangCallRecordData> callRecordData) {
        try {
            List<SushangPushResultData> resultDataList = new ArrayList<>();
            List<String> custNums = callRecordData.stream().map(SushangCallRecordData::getCustNum).collect(Collectors.toList());
            String date = LocalDate.now().toString();
            List<SushangPushResultData> pushDealList = sushangPushResultDataMapper.getDealDataByCustNum(custNums, date);
            Set<String> dealCustNums = pushDealList.stream().map(SushangPushResultData::getCustNum).collect(Collectors.toSet());
            //剔除规则一
            callRecordData.removeIf(recordData -> dealCustNums.contains(recordData.getCustNum()));
            callRecordData.forEach((SushangCallRecordData callRecord) -> {
                SushangPushResultData pushResultData = new SushangPushResultData();
                BeanUtils.copyProperties(callRecord, pushResultData);
                pushResultData.setCreateTime(new Date());
                pushResultData.setUpdateTime(new Date());
                pushResultData.setUploadDate(LocalDate.now().toString());
                pushResultData.setRule(2);
                pushResultData.setStatus(1);
                resultDataList.add(pushResultData);
            });
            if (CollectionUtils.isEmpty(resultDataList)) {
                return;
            }
            //批量插入
            sushangPushResultDataMapper.insertBatch(resultDataList);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode(), "苏商银行规则二插入通话明细异常！"), e);
        }
    }

    private void pushDealData(List<SushangTransferData> transferList, Long callRecordLocalId) {
        try {
            List<SushangPushResultData> resultDataList = new ArrayList<>();
            for (SushangTransferData sushangTransferData : transferList) {
                String minDealTime = sushangTransferData.getExtend03();
                String custNum = sushangTransferData.getCustNum();
                //查询最接近该日期的外呼时间
                SushangCallRecordData callRecordData = sushangCallRecordDataMapper.getLastedCallData(callRecordLocalId, minDealTime, custNum);
                if (ObjectUtils.isEmpty(callRecordData)) {
                    log.warn("custNum={} 苏商银行规则一未查询到通话明细！", custNum);
                    continue;
                }
                SushangPushResultData pushResultData = new SushangPushResultData();
                BeanUtils.copyProperties(callRecordData, pushResultData);
                pushResultData.setCreateTime(new Date());
                pushResultData.setUpdateTime(new Date());
                pushResultData.setUploadDate(LocalDate.now().toString());
                pushResultData.setRule(1);
                pushResultData.setStatus(1);
                resultDataList.add(pushResultData);

            }
            if (CollectionUtils.isEmpty(resultDataList)) {
                return;
            }
            //批量插入
            sushangPushResultDataMapper.insertBatch(resultDataList);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode(), "苏商银行规则一插入通话明细异常！"), e);
        }
    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool) {
        Integer threadNum =
                marketingCommonConfig.getSuShangPushThreadNum();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
        log.warn("苏商推送通话明细线程数core={}，max={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());

    }
}
