package com.br.marketing.service.Impl;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.util.PeriodOfValidityHelper;
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
import java.util.Set;
import java.util.logging.SimpleFormatter;
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
            marketingTransferSyncUserCell.setTaskId(marketingSyncUser.getCusBatch());
            return marketingTransferSyncUserCell;
        }
        return null;
    }

    @Override
    public Result<Date> getValidityBeginOfTn(String apiCode, Date endDate) {
        MarketingDataValidConfigExample configExample = new MarketingDataValidConfigExample();
        configExample.createCriteria().andApiCodeEqualTo(apiCode).andValidTypeEqualTo(2).andIsDelEqualTo(1);
        List<MarketingDataValidConfig> marketingDataValidConfigs = marketingDataValidConfigMapper.selectByExample(configExample);
        if(marketingDataValidConfigs.size()>0){
            MarketingDataValidConfig marketingDataValidConfig = marketingDataValidConfigs.get(0);
            Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(marketingDataValidConfig.getValidDays());
            PeriodOfValidityBO builder = iPeriodOfValidityService.getPeriodOfValidityRange(-day, endDate).builder();
            Date beginDate = builder.getBeginDate();
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(beginDate);
        }else{
            String minAppletDate = marketingSyncUserMap.getMinAppletDate(apiCode);
            if(StringUtils.isBlank(minAppletDate)){
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该客户未上传过数据");
            }
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(minAppletDate);

        }
    }

    /**
     * shijian
     */
    private MarketingSyncUser getMarketingSyncUser(MarketingTransferSyncUser marketingTransferSyncUser,String requestDate) {
        MarketingSyncUser marketingSyncUser = null;
        // 1. 查询配置表

        List<MarketingDataValidConfig> marketingDataValidConfigs = marketingDataValidConfigMapper.selectInfo(marketingTransferSyncUser.getApiCode(), marketingTransferSyncUser.getUserType());
        // 获取需要判断的指定日期
        requestDate = requestDate==null? marketingTransferSyncUser.getRequestData():requestDate;
        LocalDate parse = LocalDate.parse(requestDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));

        String userType = marketingTransferSyncUser.getUserType();

        // 2. 获取T,N 模式下 有效期范围的规则集合，T，N
        List<MarketingDataValidConfig> marketingDataValidConfigTN = marketingDataValidConfigs.stream().filter(m -> m.getValidType() == 1).collect(Collectors.toList());
        List<String> collectRequestDateTN = new ArrayList<>();
        marketingDataValidConfigTN.forEach(mctn -> {
            String validStartDate = mctn.getValidStartDate();
            String validEndDate = mctn.getValidEndDate();
            LocalDate startDate = LocalDate.parse(validStartDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            LocalDate endDate = LocalDate.parse(validEndDate, DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
            if ((startDate.isBefore(parse) || startDate.isEqual(parse) )
                    && (parse.isBefore(endDate) ||parse.isEqual(endDate))
                    && mctn.getUserType().equals(userType)) {
                collectRequestDateTN.add(mctn.getAppletDate());
            }
        });

        // 如果T,N 模式不为空则查询最新一条数据
        if (collectRequestDateTN.size() > 0) {

            marketingSyncUser = marketingSyncUserMap.selectInAppletDate(collectRequestDateTN, marketingTransferSyncUser);
        }

        //3. 获取【非】以上集合最新的一条数据 configAppletDateTN 需要进行非空判断
        Set<String> configAppletDateTN = marketingDataValidConfigTN.stream().map(MarketingDataValidConfig::getAppletDate).collect(Collectors.toSet());
        MarketingSyncUser marketingSyncUserTaN = marketingSyncUserMap.selectNotInAppletDate(configAppletDateTN, marketingTransferSyncUser);
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
            LocalDate applyDate = LocalDate.parse(marketingSyncUserTaN.getAppletDate() , DateTimeFormatter.ofPattern(DATEFORMATPATTERN));
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


}
