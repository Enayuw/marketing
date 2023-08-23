package com.br.marketing.service.Impl.zhongbang;

import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
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

    private static final Integer PARTITION = 2000;

    @Override
    public void doProcessFirst() {
        List<String> apiCodes = marketingCommonConfig.getZhongBangToDassFilterApiCodes();
        apiCodes.forEach(apicode -> {
            String tcId = tableCreateService.getTcId(apicode);
            String requestDate = LocalDate.now().toString();
            Long indexId = 0l;

            // 创建线程池
            Integer threadNum = marketingCommonConfig.getZhongBangToDassFilterThreadNum();
            ThreadPoolExecutor pool = BrExecutors.getThreadPool(threadNum, threadNum);
            while (true) {
                // 动态修改线程池
                modifyCorePoolSize(pool);

                // 数据捞取：request_date=T日且(ifApply=1或ifLent=1)
                List<MarketingTransferSyncUser> transferSyncUserList = service.getMarketingTransferSyncUserListFirst(tcId, apicode,
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
                            filterAndPushData(list, apicode));
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

    @Override
    public void doProcessNoFirst() {

    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getZhongBangToDassFilterThreadNum();
        pool.setCorePoolSize(threadNum);
        pool.setMaximumPoolSize(threadNum);
    }

    private void filterAndPushData(List<MarketingTransferSyncUser> list, String apiCode) {
        for (String type : Arrays.asList("apply", "lent")) {
            // 1.捞取
            List<MarketingTransferSyncUser> cushenList = list.stream().filter(ifApplyOrLent(type)).collect(Collectors.toList());
            // 2.有效期
            Set<String> custNumSet = cushenList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodBO> map =
                    transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(custNumSet, apiCode, new Date());
            List<MarketingSyncUser> validedList = map.values().stream().map(SyncUserValidityPeriodBO::getSyncUser).collect(Collectors.toList());
            // 3.推送

        }
    }

    private Predicate<MarketingTransferSyncUser> ifApplyOrLent(String type) {
        return t -> {
            if (("apply").equals(type)) {
                return "1".equals(t.getIfApply());
            }else{
                return "1".equals(t.getIfLent());
            }
        };
    }
}
