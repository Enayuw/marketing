package com.br.marketing.service.Impl;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/14 14:49
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransferDataValidityPeriodServiceImpl implements TransferDataValidityPeriodService {

    private final static String DATEFORMATPATTERN = "yyyy-MM-dd";


    private final MarketingSyncUserMapper marketingSyncUserMap;

    private final MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    private final IPeriodOfValidityService iPeriodOfValidityService;

    private final MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public MarketingSyncUser getNewValidityPeriodData(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        return getMarketingSyncUser(marketingTransferSyncUser, requestDate);
    }

    @Override
    public boolean isValidityPeriod(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        return getMarketingSyncUser(marketingTransferSyncUser, requestDate) != null;
    }


    @Override
    public MarketingTransferSyncUserCell getNewValidityPeriodTransferData(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        MarketingSyncUser marketingSyncUser = getMarketingSyncUser(marketingTransferSyncUser, requestDate);
        if (marketingSyncUser != null) {
            MarketingTransferSyncUserCell marketingTransferSyncUserCell = new MarketingTransferSyncUserCell();
            BeanUtils.copyProperties(marketingTransferSyncUser, marketingTransferSyncUserCell);
            marketingTransferSyncUserCell.setCell(marketingSyncUser.getCell());
            return marketingTransferSyncUserCell;
        }
        return null;
    }

    /**
     * shijian
     */
    private MarketingSyncUser getMarketingSyncUser(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate) {
        MarketingSyncUser marketingSyncUser = null;
        // 1. 查询配置表

        List<MarketingDataValidConfig> marketingDataValidConfigs = marketingDataValidConfigMapper.selectInfo(marketingTransferSyncUser.getApiCode(), marketingTransferSyncUser.getUserType());
        // 获取需要判断的指定日期
        requestDate = requestDate == null ? marketingTransferSyncUser.getRequestData() : requestDate;
        LocalDate parse = LocalDate.parse(requestDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));

        // 2. 获取T,N 模式下 有效期范围的规则集合，T，N
        List<MarketingDataValidConfig> marketingDataValidConfigTN = marketingDataValidConfigs.stream().filter(m -> m.getValidType() == 1).collect(Collectors.toList());
        List<String> collectRequestDateTN = new ArrayList<>();
        marketingDataValidConfigTN.forEach(mctn -> {
            String validStartDate = mctn.getValidStartDate();
            String validEndDate = mctn.getValidEndDate();
            LocalDate startDate = LocalDate.parse(validStartDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            LocalDate endDate = LocalDate.parse(validEndDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            if ((startDate.isBefore(parse) || startDate.isEqual(parse)) && (parse.isBefore(endDate) || parse.isEqual(endDate))) {
                collectRequestDateTN.add(mctn.getAppletDate());
            }
        });

        // 如果T,N 模式不为空则查询最新一条数据
        if (collectRequestDateTN.size() > 0) {
            marketingSyncUser = marketingSyncUserMap.selectInAppletDate(collectRequestDateTN, marketingTransferSyncUser);
        }

        //3. 获取【非】以上集合最新的一条数据 collectRequestDateTN 需要进行非空判断
        MarketingSyncUser marketingSyncUserTaN = marketingSyncUserMap.selectNotInAppletDate(collectRequestDateTN, marketingTransferSyncUser);
        if (marketingSyncUserTaN == null) {
            return marketingSyncUser;
        }
        // 4. 获取T+N有效期范围的规则集合，T+N
        List<MarketingDataValidConfig> marketingDataValidConfigTaN = marketingDataValidConfigs.stream().filter(m -> m.getValidType() == 2).collect(Collectors.toList());

        //5. 空为没有配置默认永久有效
        if (marketingDataValidConfigTaN.size() == 0) {
            marketingSyncUser = getNewMarketingSyncUser(marketingSyncUser, marketingSyncUserTaN);
        } else {
            //6. 非空 判断上传数据的有效期。
            LocalDate applyDate = LocalDate.parse(marketingSyncUserTaN.getAppletDate(), DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            for (MarketingDataValidConfig mctan : marketingDataValidConfigTaN) {
                // 判断是否有效
                if (iPeriodOfValidityService.isNotExpire(convertToDateViaInstant(parse), mctan.getValidDays(), convertToDateViaInstant(applyDate))) {
                    marketingSyncUser = getNewMarketingSyncUser(marketingSyncUser, marketingSyncUserTaN);
                    break;
                }
            }
        }
        return marketingSyncUser;
    }

    public java.util.Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay().atZone(ZoneId.systemDefault())
                .toInstant());
    }


    private static MarketingSyncUser getNewMarketingSyncUser(MarketingSyncUser marketingSyncUser, MarketingSyncUser marketingSyncUserTaN) {
        if (marketingSyncUser != null) {
            Date appletTimeTN = marketingSyncUser.getAppletTime();
            Date appletTimeTaN = marketingSyncUserTaN.getAppletTime();
            if (appletTimeTaN.after(appletTimeTN)) {
                marketingSyncUser = marketingSyncUserTaN;
            }
        } else {
            marketingSyncUser = marketingSyncUserTaN;
        }
        return marketingSyncUser;
    }

    @Override
    public Map<String, SyncUserValidityPeriodBO> getSyncUserValidityPeriodMap(List<MarketingTransferSyncUser> transferSyncUserList
            , String apiCode) {
        Map<String, SyncUserValidityPeriodBO> boMap = new HashMap<>(2048);
        // 转化数据CustNum案件编号及对应的UserType场景
        Map<String, String> map = transferSyncUserList.parallelStream().collect(Collectors.toMap(
                MarketingTransferSyncUser::getCustNum, MarketingTransferSyncUser::getUserType));
        List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCustAndStatus(apiCode, map.keySet());
        // apicode全量有效期配置
        MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(1);
        List<MarketingDataValidConfig> configList = marketingDataValidConfigMapper.selectByExample(example);
        // 未配置任何有效期
        if (CollectionUtils.isEmpty(configList)) {
            return longValid(preUserByTask);
        }
        // 配置了T+N的情况
        List<MarketingDataValidConfig> tnList = configList.parallelStream().filter(
                marketingDataValidConfig -> marketingDataValidConfig.getValidType().equals(2)).collect(Collectors.toList());
        // 未配置T+N
        if (CollectionUtils.isEmpty(tnList)) {
            return longValid(preUserByTask);
        }

        // 配置了T,N （范围）的情况
        // TODO: 2023-03-22  T,N （范围）暂时不做

        final Date date = new Date();
        // 处理T+N的配置
        preUserByTask.parallelStream().filter(user -> {
            for (MarketingDataValidConfig config : tnList) {
                if (iPeriodOfValidityService.isNotExpire(date, config.getValidDays(), user.getAppletTime())) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toMap(
                MarketingSyncUser::getCustNum, marketingSyncUser -> {
//                    iPeriodOfValidityService.getPeriodOfValidityRange();
                    SyncUserValidityPeriodBO bo = new SyncUserValidityPeriodBO();
                    bo.setSyncUser(marketingSyncUser);
                    bo.setBuilder(PeriodOfValidityBO.custom(marketingSyncUser.getAppletTime(), null));
                    return bo;
                }, this::latestSyncUserValidityPeriodBO));

        return null;
    }

    /**
     * 2023-03-22 18:12
     * 永久有效
     *
     * @param preUserByTask 原始数据集合
     * @return Map key：custNum value：SyncUserValidityPeriodBO {@linkplain SyncUserValidityPeriodBO MarketingSyncUser PeriodOfValidityBO.Builder}
     */
    private Map<String, SyncUserValidityPeriodBO> longValid(List<MarketingSyncUser> preUserByTask) {
        return preUserByTask.parallelStream().collect(Collectors.toMap(
                MarketingSyncUser::getCustNum, marketingSyncUser -> {
                    SyncUserValidityPeriodBO bo = new SyncUserValidityPeriodBO();
                    bo.setSyncUser(marketingSyncUser);
                    bo.setBuilder(PeriodOfValidityBO.custom(marketingSyncUser.getAppletTime(), null));
                    return bo;
                }, this::latestSyncUserValidityPeriodBO));
    }

    /**
     * 2023-03-22 18:19
     * 获取最新：
     * 1、 v1的AppletTime不为null时，v2的AppletTime不为null，通过比较大小返回最大时间的对象
     * 2、 v1的AppletTime不为null时，v2的AppletTime为null，返回v1
     * 3、 v1的AppletTime为null时，v2的AppletTime不为null，返回v2
     * 4、 v1的AppletTime为null时，v2的AppletTime为null；v1的CreateTime不为null时，v2的CreateTime不为null，通过比较大小返回最大时间的对象
     * 4.1、 v1的AppletTime为null时，v2的AppletTime为null；v1的CreateTime不为null时，v2的CreateTime为null，返回v1
     * 4.2、 v1的AppletTime为null时，v2的AppletTime为null；v1的CreateTime为null时，v2的CreateTime不为null，返回v2
     * 4.3、 v1的AppletTime为null时，v2的AppletTime为null；v1的CreateTime为null时，v2的CreateTime为null，返回v1
     */
    private SyncUserValidityPeriodBO latestSyncUserValidityPeriodBO(SyncUserValidityPeriodBO v1, SyncUserValidityPeriodBO v2) {
        MarketingSyncUser syncUserV1 = v1.getSyncUser();
        MarketingSyncUser syncUserV2 = v2.getSyncUser();
        return ObjectUtils.isEmpty(syncUserV1.getAppletTime())
                ? (ObjectUtils.isEmpty(syncUserV2.getAppletTime())
                ? (ObjectUtils.isEmpty(syncUserV1.getCreateTime())
                ? (ObjectUtils.isEmpty(syncUserV2.getCreateTime())
                ? v1 : v2) : (ObjectUtils.isEmpty(syncUserV2.getCreateTime())
                ? v1 : (syncUserV1.getCreateTime().compareTo(syncUserV2.getCreateTime()) > 0
                ? v1 : v2))) : v2) : (ObjectUtils.isEmpty(syncUserV2.getAppletTime())
                ? v1 : (syncUserV1.getAppletTime().compareTo(syncUserV2.getAppletTime()) > 0
                ? v1 : v2));
    }

}
