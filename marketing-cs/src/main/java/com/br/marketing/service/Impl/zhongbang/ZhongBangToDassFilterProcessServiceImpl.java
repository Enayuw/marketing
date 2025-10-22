package com.br.marketing.service.Impl.zhongbang;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.dassservice.input.transfer.DassAssembleTransferDataSoleDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExample;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ZhongBangToDassFilterProcessService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialTransferSoleHandler;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private ZhongBangToDassFilterGetDataService service;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    private PhoneSaleMapper phoneSaleMapper;

    @Resource
    private ArtificialTransferSoleHandler artificialTransferSoleHandler;

    private static HashMap<String, String> dxUserTypeMap = new HashMap<>();

    static {
        dxUserTypeMap.put("apply", "1");
        dxUserTypeMap.put("lent", "2");
    }

    @Override
    public void doProcessFirst(LocalDate date) {
        log.warn("众邦推Dass转化过滤，首次JOB开始");
        List<String> apiCodes = marketingCommonConfig.getZhongBangToDassFilterApiCodes();
        for (String apiCode : apiCodes) {
            try {
                String tcId = tableCreateService.getTcId(apiCode);
                String requestDate = date.toString();
                Long indexId = null;

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

                    pool.execute(() ->
                            filterAndPushData(transferSyncUserList, apiCode));
                }

                pool.shutdown();

                while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                    log.info("等待线程池结束");
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        log.warn("众邦推Dass转化过滤，首次JOB结束");
    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getZhongBangToDassFilterThreadNum();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
    }

    private void filterAndPushData(List<MarketingTransferSyncUser> list, String apiCode) {
        try {
            log.warn("众邦推Dass转化过滤，首次JOB过滤前量级：{}", list.size());
            for (String type : dxUserTypeMap.keySet()) {
                // 1.捞取
                List<MarketingTransferSyncUser> filterList = list.stream().filter(ifApplyOrLent(type)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(filterList)) {
                    continue;
                }
                log.warn("众邦推Dass转化过滤，首次JOB：{}，有效期过滤前量级：{}", type, filterList.size());
                // 2.有效期
                List<MarketingSyncUser> validedList = getValidedList(apiCode, filterList);
                if (CollectionUtils.isEmpty(validedList)) {
                    continue;
                }
                log.warn("众邦推Dass转化过滤，首次JOB：{}，有效期过滤后量级：{}", type, validedList.size());
                // 3.推送
                pushToDass(validedList, type, apiCode);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
        Map<String, SyncUserValidityPeriodsBO> map =
                transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSet, apiCode, new Date());
        List<MarketingSyncUser> validedList = new ArrayList<>();
        map.values().forEach((l) -> validedList.addAll(l.getSyncUsers()));
        return validedList;
    }

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
    public void doProcessNoFirst(LocalDate requestDate) {
        log.warn("众邦推Dass转化过滤，非首次JOB开始");
        List<String> apiCodes = marketingCommonConfig.getZhongBangToDassFilterApiCodes();
        // 创建线程池
        Integer threadNum = marketingCommonConfig.getZhongBangToDassFilterThreadNum();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(threadNum, threadNum);

        apiCodes.forEach(apiCode -> dxUserTypeMap.forEach((k, v) -> doProcess(apiCode, pool, k, requestDate)));

        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        log.warn("众邦推Dass转化过滤，非首次JOB结束");
    }

    public void doProcess(String apiCode, ThreadPoolExecutor pool, String type, LocalDate date) {
        try {
            String tcId = tableCreateService.getTcId(apiCode);
            String requestDate = date.toString();
            String lastDate = date.minusDays(1).toString();
            String lastDateStart = lastDate + " 00:00:00:000";
            String lastDateEnd = lastDate + " 23:59:59:999";

            Integer lastDays = marketingCommonConfig.getZhongBangToDassLastDays();
            Date dateStart = Date.from(date.minusDays(lastDays).atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dateEnd = Date.from(date.atTime(23, 59, 59, 999999999)
                    .atZone(ZoneId.systemDefault()).toInstant());

            String userType = dxUserTypeMap.get(type);
            Long indexId = null;

            while (true) {
                // 动态修改线程池
                modifyCorePoolSize(pool);

                List<MarketingTransferSyncUser> transferSyncUserList = new ArrayList<>();
                if (("1").equals(userType)) {
                    // request_date=T日且ifApply=1且applyDt=T-1
                    transferSyncUserList = service.getNoFirstCuShen(tcId, apiCode,
                            requestDate, lastDateStart, lastDateEnd, indexId);
                } else if (("2").equals(userType)) {
                    // request_date=T日且ifLent=1且lentTime=T-1
                    transferSyncUserList = service.getNoFirstCuTi(tcId, apiCode,
                            requestDate, lastDateStart, lastDateEnd, indexId);
                }

                if (CollectionUtils.isEmpty(transferSyncUserList)) {
                    break;
                }
                indexId = transferSyncUserList.get(transferSyncUserList.size() - 1).getId();

                List<String> custNums = findCustNumsBySftpAndApi(apiCode, dateStart, dateEnd, userType, transferSyncUserList);
                if (CollectionUtils.isEmpty(custNums)) {
                    return;
                }
                log.warn("众邦推Dass转化过滤，非首次JOB：{}，近3天命中推dass人工量级：{}", type, custNums.size());

                List<MarketingTransferSyncUser> transferSyncUsers =
                        transferSyncUserList.stream().filter(t -> custNums.contains(t.getCustNum())).collect(Collectors.toList());
                log.warn("众邦推Dass转化过滤，非首次JOB：{}，有效期过滤前量级：{}", type, transferSyncUsers.size());

                pool.execute(() ->
                        validAndPushData(transferSyncUsers, apiCode, type));
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * 根据转化数据custNum查询近3天命中推dass人工的数据，包括sftp和api
     * @return 返回sftp和api的并集custNum
     */
    private List<String> findCustNumsBySftpAndApi(String apiCode, Date dateStart, Date dateEnd, String userType,
                                                  List<MarketingTransferSyncUser> transferSyncUserList) {
        List<String> transferCustNums = transferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
        // 查询近3天命中推dass人工的数据，包括sftp和api
        PhoneSaleExtendInfoExample example = new PhoneSaleExtendInfoExample();
        example.createCriteria().andDxUserTypeEqualTo(userType)
                .andApiCodeEqualTo(apiCode).andCustNumIn(transferCustNums)
                .andCreateTimeBetween(dateStart, dateEnd);
        example.setDistinct(true);
        List<String> custNumSet = phoneSaleExtendInfoMapper.selectCustNumByExampletikv_(example);

        PhoneSaleExample example1 = new PhoneSaleExample();
        example1.createCriteria().andUserTypeEqualTo(userType).andApiCodeEqualTo(apiCode).andUidIn(transferCustNums)
                .andCreateTimeBetween(dateStart, dateEnd);
        example1.setDistinct(true);
        List<String> uidSet = phoneSaleMapper.selectUidByExampletikv_(example1);

        // 集合合并
        List<String> custNums = Stream.concat(custNumSet.stream(), uidSet.stream())
                .distinct() // 使用distinct去重
                .collect(Collectors.toList());
        return custNums;
    }

    private void validAndPushData(List<MarketingTransferSyncUser> list, String apiCode, String type) {
        try {
            // 2.有效期
            List<MarketingSyncUser> validedList = getValidedList(apiCode, list);
            if (CollectionUtils.isEmpty(validedList)) {
                return;
            }
            // 3.推送
            pushToDass(validedList, type, apiCode);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void pushToDass(List<MarketingSyncUser> marketingSyncUserList, String type, String apiCode) {
        log.warn("众邦推Dass转化过滤，{}，推送数据去重前的量级：{}", type, marketingSyncUserList.size());
        List<DassAssembleTransferDataSoleDTO> dtoList = new ArrayList<>();
        for (MarketingSyncUser marketingSyncUser : marketingSyncUserList) {
            DassTransferDataDTO dassDataDTO = new DassTransferDataDTO();
            dassDataDTO.setUid(marketingSyncUser.getCustNum());
            dassDataDTO.setSource("33");
            dassDataDTO.setUserType(dxUserTypeMap.get(type));
            String phone = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
            if (StringUtils.isEmpty(phone)) {
                log.error("众邦推Dass转化过滤，手机号log解密失败：{},上传明细表id：{}", marketingSyncUser.getCell(), marketingSyncUser.getId());
                continue;
            }
            dassDataDTO.setPhone(phone);
            dassDataDTO.setOrgName("zhongbang");
            dassDataDTO.setIfTransform("1");
            dassDataDTO.setTransformStatus("4");

            DassAssembleTransferDataSoleDTO dto = new DassAssembleTransferDataSoleDTO();
            dto.setDassTransferDataDTO(dassDataDTO);
            dto.setStatus(type);
            dto.setSoleField(SoleFieldEnum.CELL_STATUS_SOLE.getValue());
            dto.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.TRANSFER);
            dtoList.add(dto);
        }

        if (CollectionUtils.isEmpty(dtoList)) {
            return;
        }

        ProcessHandlerContext context = new ProcessHandlerContext();
        context.setApiCode(apiCode);
        artificialTransferSoleHandler.call(dtoList, context);
    }
}
