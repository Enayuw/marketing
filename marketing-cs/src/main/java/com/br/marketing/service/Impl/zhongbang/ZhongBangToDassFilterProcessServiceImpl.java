package com.br.marketing.service.Impl.zhongbang;

import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExample;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ZhongBangToDassFilterProcessService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Description ZhongBangToDassFilterProcessServiceImpl
 * @Author hong.chen
 * @CreateTime 2023/08/23
 */
@Service
@Slf4j
public class ZhongBangToDassFilterProcessServiceImpl implements ZhongBangToDassFilterProcessService {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private ZhongBangToDassFilterGetDataService service;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    private PhoneSaleMapper phoneSaleMapper;

    private static final Integer PARTITION = 2000;

    @Override
    public void doProcessFirst() {
        List<String> apiCodes = marketingCommonConfig.getZhongBangToDassFilterApiCodes();
        apiCodes.forEach(apiCode -> {
            String tcId = tableCreateService.getTcId(apiCode);
            String requestDate = LocalDate.now().toString();
            Long indexId = 0l;

            // 创建线程池
            Integer threadNum = marketingCommonConfig.getZhongBangToDassFilterThreadNum();
            ThreadPoolExecutor pool = BrExecutors.getThreadPool(threadNum, threadNum);
            while (true) {
                // 动态修改线程池
                modifyCorePoolSize(pool);

                // 数据捞取：request_date=T日且(ifApply=1或ifLent=1)
                List<MarketingTransferSyncUser> transferSyncUserList = service.getMarketingTransferSyncUserListFirst(tcId, apiCode,
                        requestDate, indexId);

                if (CollectionUtils.isEmpty(transferSyncUserList)) {
                    break;
                }
                indexId = transferSyncUserList.get(transferSyncUserList.size() - 1).getId();

                List<List<MarketingTransferSyncUser>> partition = ListUtils.partition(transferSyncUserList, PARTITION);
                partition.forEach(part -> {
                    List<MarketingTransferSyncUser> list = new ArrayList<>();
                    list.addAll(part);
                    pool.execute(() ->
                            filterAndPushData(list, apiCode));
                });
            }

            pool.shutdown();
            try {
                while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                    log.info("等待线程池结束");
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        });
    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getZhongBangToDassFilterThreadNum();
        pool.setCorePoolSize(threadNum);
        pool.setMaximumPoolSize(threadNum);
    }

    private void filterAndPushData(List<MarketingTransferSyncUser> list, String apiCode) {
        for (String type : Arrays.asList("apply", "lent")) {
            // 1.捞取
            List<MarketingTransferSyncUser> filterList = list.stream().filter(ifApplyOrLent(type)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(filterList)) {
                break;
            }
            // 2.有效期
            List<MarketingSyncUser> validedList = getValidedList(apiCode, filterList);
            if (CollectionUtils.isEmpty(validedList)) {
                break;
            }
            // 3.推送

        }
    }

    /**
     * 根据转化数据list获取上传数据list
     * @param apiCode
     * @param list
     * @return 上传数据list
     */
    private List<MarketingSyncUser> getValidedList(String apiCode, List<MarketingTransferSyncUser> list) {
        Set<String> custNumSet = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        Map<String, SyncUserValidityPeriodBO> map =
                transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(custNumSet, apiCode, new Date());
        List<MarketingSyncUser> validedList = map.values().stream().map(SyncUserValidityPeriodBO::getSyncUser).collect(Collectors.toList());
        return validedList;
    }

//    public static void main(String[] args) {
//        ifApplyOrLent("type").test();
//    }

    private Predicate<MarketingTransferSyncUser> ifApplyOrLent(String type) {
        return t -> {
            if (("apply").equals(type)) {
                return "1".equals(t.getIfApply());
            } else {
                return "1".equals(t.getIfLent());
            }
        };
    }

    @Override
    public void doProcessNoFirst() {
        List<String> apiCodes = marketingCommonConfig.getZhongBangToDassFilterApiCodes();
        // 创建线程池
        Integer threadNum = marketingCommonConfig.getZhongBangToDassFilterThreadNum();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(threadNum, threadNum);

        for (String apiCode : apiCodes) {
            // 1:促申，2:促提
            for (String type : Arrays.asList("1", "2")) {
                doProcess(apiCode, pool, type);
            }
        }

        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void doProcess(String apiCode, ThreadPoolExecutor pool, String type) {
        String tcId = tableCreateService.getTcId(apiCode);
        String requestDate = LocalDate.now().toString();
        String lastDate = LocalDate.now().minusDays(1).toString();
        String lastDateStart = lastDate + " 00:00:00:000";
        String lastDateEnd = lastDate + " 23:59:59:999";

        Integer lastDays = marketingCommonConfig.getZhongBangToDassLastDays();
        Date dateStart = Date.from(LocalDate.now().minusDays(lastDays).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date dateEnd = Date.from(LocalDate.now().atTime(23, 59, 59, 999999999)
                .atZone(ZoneId.systemDefault()).toInstant());

        // 查询近3天命中推dass人工的数据，包括sftp和api
        PhoneSaleExtendInfoExample example = new PhoneSaleExtendInfoExample();
        example.createCriteria().andDxUserTypeEqualTo(type)
                .andApiCodeEqualTo(apiCode)
                .andCreateTimeBetween(dateStart, dateEnd);
        example.setDistinct(true);
        List<String> custNumSet = phoneSaleExtendInfoMapper.selectCustNumByExampletikv_(example);

        PhoneSaleExample example1 = new PhoneSaleExample();
        example1.createCriteria().andUserTypeEqualTo(type).andApiCodeEqualTo(apiCode)
                .andCreateTimeBetween(dateStart, dateEnd);
        example1.setDistinct(true);
        List<String> uidSet = phoneSaleMapper.selectUidByExampletikv_(example1);

        // custNum集合合并
        custNumSet.addAll(uidSet);
        if (CollectionUtils.isEmpty(custNumSet)) {
            return;
        }

        List<MarketingTransferSyncUser> transferSyncUserList = new ArrayList<>();
        if (("1").equals(type)) {
            // request_date=T日且ifApply=1且applyDt=T-1
            transferSyncUserList = service.getNoFirstCuShen(tcId, apiCode,
                    requestDate, lastDateStart, lastDateEnd, custNumSet);
        } else if (("2").equals(type)) {
            // request_date=T日且ifLent=1且lentTime=T-1
            transferSyncUserList = service.getNoFirstCuTi(tcId, apiCode,
                    requestDate, lastDateStart, lastDateEnd, custNumSet);
        }

        List<List<MarketingTransferSyncUser>> partition = ListUtils.partition(transferSyncUserList, PARTITION);
        partition.forEach(part -> {
            List<MarketingTransferSyncUser> list = new ArrayList<>();
            list.addAll(part);
            pool.execute(() ->
                    validAndPushData(list, apiCode, type));
        });
    }

    private void validAndPushData(List<MarketingTransferSyncUser> list, String apiCode, String type) {
        // 2.有效期
        List<MarketingSyncUser> validedList = getValidedList(apiCode, list);
        if (CollectionUtils.isEmpty(validedList)) {
            return;
        }
        // 3.推送

    }
}
