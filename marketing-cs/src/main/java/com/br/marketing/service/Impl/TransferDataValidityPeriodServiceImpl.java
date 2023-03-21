package com.br.marketing.service.Impl;

import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/14 14:49
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransferDataValidityPeriodServiceImpl implements TransferDataValidityPeriodService {

    private final static String DATEFORMATPATTERN = "yyyy-MM-dd HH:mm:ss";


    private final MarketingSyncUserMapper marketingSyncUserMap;

    private final MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    private final IPeriodOfValidityService iPeriodOfValidityService;

    @Override
    public MarketingSyncUser getNewValidityPeriodData(MarketingTransferSyncUser marketingTransferSyncUser,String requestDate) {
        return getMarketingSyncUser(marketingTransferSyncUser,requestDate);
    }

    @Override
    public boolean isValidityPeriod(MarketingTransferSyncUser marketingTransferSyncUser,String requestDate) {
        return getMarketingSyncUser(marketingTransferSyncUser, requestDate) != null;
    }


    @Override
    public MarketingTransferSyncUserCell getNewValidityPeriodTransferData(MarketingTransferSyncUser marketingTransferSyncUser,String requestDate) {
        MarketingSyncUser marketingSyncUser = getMarketingSyncUser(marketingTransferSyncUser,requestDate);
        if (marketingSyncUser != null) {
            MarketingTransferSyncUserCell marketingTransferSyncUserCell = new MarketingTransferSyncUserCell();
            BeanUtils.copyProperties(marketingTransferSyncUser,marketingTransferSyncUserCell);
            marketingTransferSyncUserCell.setCell(marketingSyncUser.getCell());
            return marketingTransferSyncUserCell;
        }
        return null;
    }

    /**
     * shijian
     */
    private MarketingSyncUser getMarketingSyncUser(MarketingTransferSyncUser marketingTransferSyncUser,String requestDate) {
        MarketingSyncUser marketingSyncUser = null;
        // 1. 查询配置表

        List<MarketingDataValidConfig> marketingDataValidConfigs = marketingDataValidConfigMapper.selectInfo(marketingTransferSyncUser.getApiCode(), marketingTransferSyncUser.getUserType());
        // 获取需要判断的指定日期
        String requestTime = requestDate==null? marketingTransferSyncUser.getRequestTime():requestDate;
        LocalDateTime parse = LocalDateTime.parse(requestTime, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));

        // 2. 获取T,N 模式下 有效期范围的规则集合，T，N
        List<MarketingDataValidConfig> marketingDataValidConfigTN = marketingDataValidConfigs.stream().filter(m -> m.getValidType() == 1).collect(Collectors.toList());
        List<String> collectRequestDateTN = new ArrayList<>();
        marketingDataValidConfigTN.forEach(mctn -> {
            String validStartDate = mctn.getValidStartDate();
            String validEndDate = mctn.getValidEndDate();
            LocalDateTime startDateTime = LocalDateTime.parse(validStartDate+" 00:00:00", DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            LocalDateTime endDateTime = LocalDateTime.parse(validEndDate+" 23:59:59", DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            if ((startDateTime.isBefore(parse) || startDateTime.isEqual(parse) )&& (parse.isBefore(endDateTime) ||parse.isEqual(endDateTime))) {
                collectRequestDateTN.add(mctn.getAppletDate());
            }
        });

        // 如果T,N 模式不为空则查询最新一条数据
        if (!collectRequestDateTN.isEmpty()) {
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
        if (marketingDataValidConfigTaN.isEmpty()) {
            marketingSyncUser = getNewMarketingSyncUser(marketingSyncUser, marketingSyncUserTaN);
        } else {
            //6. 非空 判断上传数据的有效期。
            Date appletTime = marketingSyncUserTaN.getAppletTime() == null ? marketingSyncUserTaN.getCreateTime() : marketingSyncUserTaN.getAppletTime();
            for (MarketingDataValidConfig mctan : marketingDataValidConfigTaN) {
                // 判断是否有效
                if (iPeriodOfValidityService.isNotExpire(convertToDateViaInstant(parse), mctan.getValidDays(), appletTime)) {
                    marketingSyncUser = getNewMarketingSyncUser(marketingSyncUser, marketingSyncUserTaN);
                    break;
                }
            }
        }
        return marketingSyncUser;
    }
    public java.util.Date convertToDateViaInstant(LocalDateTime dateToConvert) {
        return java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault())
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


}
