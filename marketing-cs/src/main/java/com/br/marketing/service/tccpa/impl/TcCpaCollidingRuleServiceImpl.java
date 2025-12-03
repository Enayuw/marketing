package com.br.marketing.service.tccpa.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleDTO;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleInfoDTO;
import com.br.marketing.dto.tc.TcCpaMagnitudeDistDTO;
import com.br.marketing.dto.tc.TcyrCpaCollidingDataPackageInfo;
import com.br.marketing.dto.tc.TcyrCpaDeleteRuleInfo;
import com.br.marketing.dto.tccpa.TcyrFailMsgSupplyDTO;
import com.br.marketing.dto.tccpa.TcyrFailMsgSupplyGroupDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCleanStatusEnum;
import com.br.marketing.enums.TcCpaFailMsgEnum;
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
    public Result<List<TcyrFailMsgSupplyGroupDTO>> magnitudeDist(String releaseTimes) {
        List<String> releaseTimeList = Arrays.asList(releaseTimes.split(","));
        if(CollectionUtils.isEmpty(releaseTimeList)){
            return null;
        }
        //1.读取需要补充的failMsg配置
        String supplyFailMsgs = marketingCommonConfig.getTcyrCpaPushFileVTConfig().getString("supplyFailMsgs");
        List<Integer> supplyFailMsgList = Arrays.stream(supplyFailMsgs.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        //2.查询量级
        List<TcyrCpaMagnitude> magnitudeList = getTcyrCpaMagnitudes(releaseTimeList, supplyFailMsgList);
        //3.将lockBelong转成failMsg
        magnitudeList = magnitudeList.stream()
                .map(item -> {
                    if (item.getLockBelong() != null) {
                        Integer failMsg = convertLockBelongToFailMsg(item.getLockBelong());
                        item.setFailMsg(failMsg);
                    }
                    return item;
                })
                .collect(Collectors.toList());
        //4.补充量级为0的数据
        List<TcyrCpaMagnitude> allMagnitudeList = fillMissingData(magnitudeList, releaseTimeList, supplyFailMsgList);
        //5.按releaseTime分组
        List<TcyrFailMsgSupplyGroupDTO> result = groupByDate(allMagnitudeList);
        return new Result<List<TcCpaMagnitudeDistDTO>>()
                .setCode(ResultCode.SUCCESS.getValue())
                .setDate(result);
    }

    /**
     * @description 查询量级
     * @param releaseTimeList
     * @param supplyFailMsgList
     * @return java.util.List<com.br.marketing.entity.TcyrCpaMagnitude>
     * @author hedongshuo
     * @date 2025/12/3 20:34
     **/
    private List<TcyrCpaMagnitude> getTcyrCpaMagnitudes(List<String> releaseTimeList, List<Integer> supplyFailMsgList) {
        List<TcyrCpaMagnitude> magnitudeList = new ArrayList<>();
        Map<Integer, TcCpaFailMsgEnum> failMsgToEnumMap = new HashMap<>();
        for (TcCpaFailMsgEnum failMsgEnum : TcCpaFailMsgEnum.values()) {
            failMsgToEnumMap.put(failMsgEnum.getValue(), failMsgEnum);
        }
        for (Integer failMsg : supplyFailMsgList) {
            TcCpaFailMsgEnum failMsgEnum = failMsgToEnumMap.get(failMsg);
            if (failMsgEnum == null) {
                continue; // 没有对应的枚举，跳过
            }
            List<TcyrCpaMagnitude> magnitudeInnerList;
            if (failMsgEnum.getLockValue() != null) {
                //查询【b_tcyr_cpa_lock_data】
                magnitudeInnerList = tcyrCpaLockDataMapper
                        .queryMagnitudeWithBelong(releaseTimeList, failMsgEnum.getLockValue());
            } else {
                //查询【b_tcyr_cpa_invalue_data】
                magnitudeInnerList = tcyrCpaInvalueDataMapper
                        .queryMagnitudeWithFailMsg(releaseTimeList, failMsgEnum.getValue().toString());
            }
            magnitudeList.addAll(magnitudeInnerList);
        }
        return magnitudeList;
    }

    /**
     * 补全量级为0的数据
     * @param existingData
     * @param releaseTimeList
     * @param supplyFailMsgList
     */
    private List<TcyrCpaMagnitude> fillMissingData(List<TcyrCpaMagnitude> existingData,
                                 List<String> releaseTimeList,
                                 List<Integer> supplyFailMsgList) {
        if (CollectionUtils.isEmpty(existingData)) {
            existingData = new ArrayList<>();
        }
        Map<String, TcyrCpaMagnitude> existingDataMap = new HashMap<>();
        //1.创建存在数据的Map<releaseTime-failMsg, TcyrCpaMagnitude>
        for (TcyrCpaMagnitude item : existingData) {
            String key = item.getReleaseTime() + "-" + item.getFailMsg();
            existingDataMap.put(key, item);
        }
        //2.创建结果列表
        List<TcyrCpaMagnitude> result = new ArrayList<>();
        for (String releaseTime : releaseTimeList) {
            for (Integer failMsg : supplyFailMsgList) {
                String key = releaseTime + "-" + failMsg;
                if (existingDataMap.containsKey(key)) {
                    result.add(existingDataMap.get(key));
                } else {
                    TcyrCpaMagnitude missingItem = new TcyrCpaMagnitude();
                    missingItem.setReleaseTime(releaseTime);
                    missingItem.setFailMsg(failMsg);
                    missingItem.setMagnitude(0L);
                    result.add(missingItem);
                }
            }
        }
        //3.按日期和failMsg排序
        result.sort(Comparator
                .comparing(TcyrCpaMagnitude::getReleaseTime)
                .thenComparing(TcyrCpaMagnitude::getFailMsg));
        return result;
    }

    @Override
    public Result rule(TcCpaCollidingRuleDTO ruleDTO) {
        TcyrCpaCollidingTask basicTask = new TcyrCpaCollidingTask();
        basicTask.setApiCode(marketingCommonConfig.getTcyrApiCode());
        basicTask.setPackageIds(String.join(",", ruleDTO.getPackageIds()));
        basicTask.setLimitNum(ruleDTO.getLimitNum());
        basicTask.setDeleteRuleIds(String.join(",", ruleDTO.getDeleteRuleIds()));
//        if(ruleDTO.getSupplyReleaseTimes())

        return null;
    }

    /**
     * 将lockBelong转换为failMsg
     * @param lockBelong
     * @return
     */
    private Integer convertLockBelongToFailMsg(Integer lockBelong) {
        if (lockBelong == null) {
            return null;
        }
        for (TcCpaFailMsgEnum enumItem : TcCpaFailMsgEnum.values()) {
            if (lockBelong.equals(enumItem.getLockValue())) {
                return enumItem.getValue();
            }
        }
        return null;
    }

    /**
     * 将List<TcyrCpaMagnitude>转换为按日期分组的结果
     */
    public List<TcyrFailMsgSupplyGroupDTO> groupByDate(List<TcyrCpaMagnitude> magnitudeList) {
        if (CollectionUtils.isEmpty(magnitudeList)) {
            return Collections.emptyList();
        }
        //1.按日期分组
        Map<String, List<TcyrCpaMagnitude>> groupByDate = magnitudeList.stream()
                .filter(item -> item.getReleaseTime() != null)
                .collect(Collectors.groupingBy(
                        TcyrCpaMagnitude::getReleaseTime,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        //2.转换为目标结构
        return groupByDate.entrySet().stream()
                .map(entry -> {
                    TcyrFailMsgSupplyGroupDTO result = new TcyrFailMsgSupplyGroupDTO();
                    result.setReleaseTime(entry.getKey());
                    // 将每个日期下的数据转换为TcyrFailMsgSupplyDTO
                    List<TcyrFailMsgSupplyDTO> supplyInfoList = entry.getValue().stream()
                            .filter(item -> item.getFailMsg() != null)
                            .map(item -> new TcyrFailMsgSupplyDTO(
                                    item.getFailMsg(),
                                    item.getMagnitude() != null ? item.getMagnitude() : 0L, false
                            ))
                            .sorted(Comparator.comparing(TcyrFailMsgSupplyDTO::getFailMsg))
                            .collect(Collectors.toList());
                    result.setSupplyInfo(supplyInfoList);
                    return result;
                })
                .sorted(Comparator.comparing(TcyrFailMsgSupplyGroupDTO::getReleaseTime))
                .collect(Collectors.toList());
    }
}
