package com.br.marketing.service.tccpa.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.tc.TcCpaCollidingRuleInfoDTO;
import com.br.marketing.dto.tc.TcCpaMagnitudeDistDTO;
import com.br.marketing.dto.tc.TcyrCpaCollidingDataPackageInfo;
import com.br.marketing.dto.tc.TcyrCpaDeleteRuleInfo;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCleanStatusEnum;
import com.br.marketing.enums.TcCpaLockBelongEnum;
import com.br.marketing.mapper.TcyrCpaCollidingDataPackageMapper;
import com.br.marketing.mapper.TcyrCpaDeleteRuleMapper;
import com.br.marketing.mapper.TcyrCpaInvalueDataMapper;
import com.br.marketing.mapper.TcyrCpaLockDataMapper;
import com.br.marketing.service.tccpa.TcCpaCollidingRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TcCpaCollidingRuleServiceImpl implements TcCpaCollidingRuleService {

    @Resource
    TcyrCpaCollidingDataPackageMapper tcyrCpaCollidingDataPackageMapper;

    @Resource
    TcyrCpaDeleteRuleMapper tcyrCpaDeleteRuleMapper;

    @Resource
    TcyrCpaLockDataMapper tcyrCpaLockDataMapper;

    @Resource
    TcyrCpaInvalueDataMapper tcyrCpaInvalueDataMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;



    @Override
    public Result<TcCpaCollidingRuleInfoDTO> info() {
        TcCpaCollidingRuleInfoDTO infoDTO = new TcCpaCollidingRuleInfoDTO();
        //1.查询有效的数据包
        TcyrCpaCollidingDataPackageExample dataPackageExample = new TcyrCpaCollidingDataPackageExample();
        dataPackageExample.createCriteria()
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT)
                .andCleanStatusEqualTo(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
        List<TcyrCpaCollidingDataPackage> dataPackages = tcyrCpaCollidingDataPackageMapper.selectByExample(dataPackageExample);
        if(CollectionUtils.isNotEmpty(dataPackages)){
            List<TcyrCpaCollidingDataPackageInfo> dataPackageInfo = dataPackages.stream()
                    .map(dataPackage -> new TcyrCpaCollidingDataPackageInfo(dataPackage.getId(), dataPackage.getPackageName()))
                    .collect(Collectors.toList());
            infoDTO.setDataPackages(dataPackageInfo);
        }
        //2.查询有效的剔除规则
        TcyrCpaDeleteRuleExample deleteRuleExample = new TcyrCpaDeleteRuleExample();
        deleteRuleExample.createCriteria()
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT);
        List<TcyrCpaDeleteRule> deleteRules = tcyrCpaDeleteRuleMapper.selectByExample(deleteRuleExample);
        if(CollectionUtils.isNotEmpty(deleteRules)){
            List<TcyrCpaDeleteRuleInfo> deleteRuleInfo = deleteRules.stream()
                    .map(deleteRule -> new TcyrCpaDeleteRuleInfo(deleteRule.getId(), deleteRule.getRuleName()))
                    .collect(Collectors.toList());
            infoDTO.setDeleteRules(deleteRuleInfo);
        }
        //3.查询提取量级阈值和提取时间
        infoDTO.setExtraNumTotal(marketingCommonConfig.getTcyrCpaPushFileVTConfig().getInteger("extraNumTotal"));
        infoDTO.setExtraTime(marketingCommonConfig.getTcyrCpaPushFileVTConfig().getString("extraTime"));
        return new Result<TcCpaCollidingRuleInfoDTO>()
                .setCode(ResultCode.SUCCESS.getValue())
                .setDate(infoDTO);
    }

    @Override
    public Result<List<TcCpaMagnitudeDistDTO>> magnitudeDist(List<String> releaseTimes) {
        if(CollectionUtils.isEmpty(releaseTimes)){
            return null;
        }
        //fail_msg = 2-被友商锁定
        List<TcyrCpaMagnitude> magnitudeOtrList = tcyrCpaLockDataMapper
                .queryMagnitudeWithBelong(releaseTimes, TcCpaLockBelongEnum.BELONG_OTR.getValue());
        //fail_msg = 6-空白组
        List<TcyrCpaMagnitude> magnitudeBlankList = tcyrCpaLockDataMapper
                .queryMagnitudeWithBelong(releaseTimes, TcCpaLockBelongEnum.BELON_BLANK.getValue());
        Map<String, Long> magnitudeOtrMap = magnitudeOtrList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        TcyrCpaMagnitude::getReleaseTime,
                        TcyrCpaMagnitude::getCount,
                        (v1, v2) -> v1
                ));

        Map<String, Long> magnitudeBlankMap = magnitudeBlankList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        TcyrCpaMagnitude::getReleaseTime,
                        TcyrCpaMagnitude::getCount,
                        (v1, v2) -> v1
                ));
        List<TcCpaMagnitudeDistDTO> magnitudes = releaseTimes.stream()
                .map(releaseTime -> {
                    Long lockByOtrNum = magnitudeOtrMap.getOrDefault(releaseTime, 0l);
                    Long blankNum = magnitudeBlankMap.getOrDefault(releaseTime, 0l);
                    return new TcCpaMagnitudeDistDTO(releaseTime, lockByOtrNum, blankNum);
                })
                .sorted(Comparator.comparing(TcCpaMagnitudeDistDTO::getReleaseTime))
                .collect(Collectors.toList());
        return new Result<List<TcCpaMagnitudeDistDTO>>()
                .setCode(ResultCode.SUCCESS.getValue())
                .setDate(magnitudes);
    }
}
