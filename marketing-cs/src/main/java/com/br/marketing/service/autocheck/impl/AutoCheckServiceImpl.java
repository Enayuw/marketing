package com.br.marketing.service.autocheck.impl;

import cn.hutool.core.collection.CollUtil;
import com.br.marketing.dto.autocheck.CheckTransferSyncDataDto;
import com.br.marketing.dto.autocheck.CheckUploadSyncDataDto;
import com.br.marketing.dto.autocheck.SaveAutoCheckConfigDto;
import com.br.marketing.entity.AutoCheckConfig;
import com.br.marketing.mapper.AutoCheckConfigMapper;
import com.br.marketing.mapper.AutoCheckSenceDictMapper;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.mapper.TransferSyncReportMapper;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.service.autocheck.AutoCheckService;
import com.br.marketing.utils.CheckObjectSameUtil;
import com.br.marketing.vo.MarketingCustomerVO;
import com.br.marketing.vo.autocheck.AutoCheckResultVO;
import com.br.marketing.vo.autocheck.AutoCheckConfigVO;
import com.br.marketing.vo.autocheck.AutoCheckSenceVO;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: fuzhen.zhang
 * @description: 自动化巡检service
 * @date: 2025/12/18 15:35
 */
@Service
@Slf4j
public class AutoCheckServiceImpl implements AutoCheckService {

    private static final String SCENE_UPLOAD = "TY-SCJK";
    private static final String SCENE_TRANSFER = "TY-ZHJK";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String JSON_DYNAMIC_FILTER_ID = "autoCheckDynamicFieldFilter";

    @JsonFilter(JSON_DYNAMIC_FILTER_ID)
    private interface DynamicFieldFilterMixIn {
    }

    @Resource
    private AutoCheckSenceDictMapper autoCheckSenceDictMapper;

    @Resource
    private AutoCheckConfigMapper autoCheckConfigMapper;

    @Resource
    private MarketingCustomerService marketingCustomerService;

    @Resource
    private MarketingSyncReportMapper marketingSyncReportMapper;

    @Resource
    private TransferSyncReportMapper transferSyncReportMapper;

    @Override
    public List<AutoCheckConfigVO> getAutoCheckConfigList(String apiCodes, String senceCodes) {
        List<AutoCheckConfigVO> result = new ArrayList<>();

        // 处理apiCodes参数，用逗号分隔
        List<String> apiCodeList = handleApiCodeParam(apiCodes);

        // 处理senceCodes参数，用逗号分隔
        List<String> senceCodeList = handleSenceCodeParam(senceCodes);

        // 根据apiCodes和senceCodes查询配置信息
        List<AutoCheckConfig> configList = autoCheckConfigMapper.
                selectByApiCodesAndSenceCodes(apiCodeList, senceCodeList);

        if (CollUtil.isEmpty(configList)) {
            return result;
        }

        // 查询apiCode信息
        List<MarketingCustomerVO> apiCodeInfoList = marketingCustomerService.getApiCodeList(apiCodeList);
        Map<String, MarketingCustomerVO> apiCodeInfoMap = apiCodeInfoList.stream()
                .collect(Collectors.toMap(MarketingCustomerVO::getApiCode, e -> e));

        // 收集所有配置中涉及的场景编码，用于查询场景信息
        List<String> allSenceCodes = new ArrayList<>();
        for (AutoCheckConfig config : configList) {
            if (StringUtils.isNotBlank(config.getSenceCode())) {
                // 配置中的sence_code字段可能包含逗号分隔的多个场景
                List<String> configSenceCodes = Arrays.stream(config.getSenceCode().split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList());
                allSenceCodes.addAll(configSenceCodes);
            }
        }
        // 去重
        allSenceCodes = allSenceCodes.stream().distinct().collect(Collectors.toList());

        // 查询场景信息
        List<AutoCheckSenceVO> autoCheckSenceVOList = autoCheckSenceDictMapper.selectBySenceCodes(allSenceCodes);
        Map<String, AutoCheckSenceVO> senceMap = autoCheckSenceVOList.stream()
                .collect(Collectors.toMap(AutoCheckSenceVO::getSenceCode, sence -> sence));
        Map<String, AutoCheckConfig> configMapByApiCode = configList.stream()
                .collect(Collectors.toMap(AutoCheckConfig::getApiCode, e -> e));

        // 组装AutoConfigVO
        for (String apiCode : configMapByApiCode.keySet()) {
            AutoCheckConfigVO vo = new AutoCheckConfigVO();
            AutoCheckConfig apiConfig = configMapByApiCode.get(apiCode);
            vo.setId(apiConfig.getId());
            vo.setApiCode(apiCode);
            vo.setName(apiCodeInfoMap.get(apiCode).getName());

            // 收集该ApiCode下的所有场景信息
            List<AutoCheckSenceVO> senceList = new ArrayList<>();
            String simpleConfigSenceCodes = apiConfig.getSenceCode();
            if (StringUtils.isNotBlank(simpleConfigSenceCodes)) {
                String[] split = simpleConfigSenceCodes.split(",");
                for (String senceCode : split) {
                    AutoCheckSenceVO autoCheckSenceVO = senceMap.get(senceCode);
                    if (autoCheckSenceVO != null) {
                        senceList.add(autoCheckSenceVO);
                    }
                }
            }
            vo.setSence(senceList);
            result.add(vo);
        }
        return result;
    }

    private List<String> handleApiCodeParam(String apiCodes) {
        List<String> apiCodeList = new ArrayList<>();
        if (StringUtils.isNotBlank(apiCodes)) {
            apiCodeList = Arrays.stream(apiCodes.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            // 获取所有apiCode
            List<AutoCheckConfig> autoCheckConfigs = autoCheckConfigMapper.selectByApiCodesAndSenceCodes(null, null);
            for (AutoCheckConfig autoCheckConfig : autoCheckConfigs) {
                apiCodeList.add(autoCheckConfig.getApiCode());
            }
            apiCodeList = apiCodeList.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        }
        return apiCodeList;

    }

    private List<String> handleSenceCodeParam(String senceCodes) {
        List<String> senceCodeList = new ArrayList<>();
        if (StringUtils.isNotBlank(senceCodes)) {
            senceCodeList = Arrays.stream(senceCodes.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            // 获取所有场景编码
            List<AutoCheckSenceVO> allSenceList = autoCheckSenceDictMapper.selectBySenceCodes(null);
            for (AutoCheckSenceVO autoCheckSenceVO : allSenceList) {
                senceCodeList.add(autoCheckSenceVO.getSenceCode());
            }
            senceCodeList = senceCodeList.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        }
        return senceCodeList;
    }

    @Override
    public List<AutoCheckSenceVO> getAutoCheckSenceList(String searchContent) {
        return autoCheckSenceDictMapper.searchSenceList(searchContent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveAutoCheckConfig(SaveAutoCheckConfigDto dto) {
        int result;
        if (Objects.isNull(dto.getId())) {
            // 新增
            // 新增前做防止重复的处理
            AutoCheckConfig existingConfig = autoCheckConfigMapper.selectByApiCode(dto.getApiCode());
            if (Objects.nonNull(existingConfig)) {
                log.warn("要保存的配置已存在，apiCode: {}", dto.getApiCode());
                return false;
            }
            AutoCheckConfig config = new AutoCheckConfig();
            config.setApiCode(dto.getApiCode());
            config.setSenceCode(dto.getSenceCodes());
            Date now = new Date();
            config.setCreateTime(now);
            config.setUpdateTime(now);
            // 插入数据库
            result = autoCheckConfigMapper.insertSelective(config);
        } else {
            // 编辑
            AutoCheckConfig existingConfig = autoCheckConfigMapper.selectByPrimaryKey(dto.getId());
            if (Objects.isNull(existingConfig)) {
                log.warn("要编辑的配置不存在，id: {}", dto.getId());
                return false;
            }
            // 更新字段
            existingConfig.setSenceCode(dto.getSenceCodes());
            existingConfig.setUpdateTime(new Date());
            // 更新数据库
            result = autoCheckConfigMapper.updateByPrimaryKeySelective(existingConfig);
        }
        return result > 0;
    }

    @Override
    public Boolean delAutoCheckConfig(Long id) {
        if (Objects.isNull(id)) {
            return true;
        }
        AutoCheckConfig existingConfig = autoCheckConfigMapper.selectByPrimaryKey(id);
        if (Objects.isNull(existingConfig)) {
            log.warn("要删除的配置不存在，id: {}", id);
            return true;
        }
        // 更新字段
        existingConfig.setIsDeleted((byte) 1);
        existingConfig.setUpdateTime(new Date());
        // 更新数据库
        return autoCheckConfigMapper.updateByPrimaryKeySelective(existingConfig) > 0;
    }

    @Override
    public List<AutoCheckResultVO> getResultList(String apiCodes, String senceCodes) {
        // 处理apiCodes参数，用逗号分隔
        List<String> apiCodeList = handleApiCodeParam(apiCodes);

        // 处理senceCodes参数，用逗号分隔
        List<String> senceCodeList = handleSenceCodeParam(senceCodes);

        // 获取apiCode对应的场景配置
        List<AutoCheckConfigVO> configList = getAutoCheckConfigList(apiCodes, null);
        Map<String, AutoCheckConfigVO> configMap = configList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AutoCheckConfigVO::getApiCode, e -> e, (a, b) -> a));

        // apiCode 基础信息（名称）
        Map<String, MarketingCustomerVO> apiInfoMap = marketingCustomerService.getApiCodeList(apiCodeList)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MarketingCustomerVO::getApiCode, e -> e, (a, b) -> a));

        // 场景信息（名称）
        Map<String, AutoCheckSenceVO> senceMap = autoCheckSenceDictMapper.selectBySenceCodes(senceCodeList)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AutoCheckSenceVO::getSenceCode, e -> e, (a, b) -> a));

        List<AutoCheckResultVO> result = new ArrayList<>();

        // 针对每一个场景，查询apiCode对应的结果
        for (String senceCode : senceCodeList) {
            switch (senceCode) {
                case SCENE_UPLOAD:
                    // 过滤掉没有配置该场景的apiCode
                    result.addAll(checkUploadScene(
                            filterApiCodesByScene(apiCodeList, SCENE_UPLOAD, configMap),
                            apiInfoMap, senceMap));
                    break;
                case SCENE_TRANSFER:
                    // 过滤掉没有配置该场景的apiCode
                    result.addAll(checkTransferScene(
                            filterApiCodesByScene(apiCodeList, SCENE_TRANSFER, configMap),
                            apiInfoMap, senceMap));
                    break;
                default:
                    log.warn("未知场景编码: {}", senceCode);
            }
        }

        // 按时间倒序（time 为 yyyy-MM-dd HH:mm:ss 字符串，字典序=时间序）；空值/空串放最后
        result.sort(Comparator.comparing(
                vo -> StringUtils.isBlank(vo.getTime()) ? null : vo.getTime().trim(),
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        return result;
    }

    /**
     * 过滤出“配置了指定场景”的 apiCode。
     *
     * <p>规则：configMap 中存在该 apiCode，且其配置的场景列表包含指定 senceCode。</p>
     */
    private List<String> filterApiCodesByScene(List<String> apiCodeList,
                                               String senceCode,
                                               Map<String, AutoCheckConfigVO> configMap) {
        if (CollUtil.isEmpty(apiCodeList) || StringUtils.isBlank(senceCode) || configMap == null || configMap.isEmpty()) {
            return Collections.emptyList();
        }
        return apiCodeList.stream()
                .filter(StringUtils::isNotBlank)
                .filter(apiCode -> {
                    AutoCheckConfigVO cfg = configMap.get(apiCode);
                    if (cfg == null || CollUtil.isEmpty(cfg.getSence())) {
                        return false;
                    }
                    return cfg.getSence().stream()
                            .filter(Objects::nonNull)
                            .map(AutoCheckSenceVO::getSenceCode)
                            .anyMatch(code -> StringUtils.equals(code, senceCode));
                })
                .distinct()
                .collect(Collectors.toList());
    }

    private List<AutoCheckResultVO> checkUploadScene(List<String> apiCodeList,
                                                     Map<String, MarketingCustomerVO> apiInfoMap,
                                                     Map<String, AutoCheckSenceVO> senceMap) {
        if (CollUtil.isEmpty(apiCodeList)) {
            return Collections.emptyList();
        }

        List<CheckUploadSyncDataDto> lastDay8List = marketingSyncReportMapper.getLastDay8DataByApiCodes(apiCodeList);
        List<CheckUploadSyncDataDto> latestList = marketingSyncReportMapper.getLatestDataByApiCodes(apiCodeList);

        Map<String, CheckUploadSyncDataDto> lastDay8Map = toMapByApiCode(lastDay8List, CheckUploadSyncDataDto::getApiCode);
        Map<String, CheckUploadSyncDataDto> latestMap = toMapByApiCode(latestList, CheckUploadSyncDataDto::getApiCode);

        List<AutoCheckResultVO> voList = new ArrayList<>();
        for (String apiCode : apiCodeList) {
            CheckUploadSyncDataDto lastDay8 = lastDay8Map.get(apiCode);
            CheckUploadSyncDataDto latest = latestMap.get(apiCode);
            // 若前一天八点的数据不存在，或者当前数据不存在，则跳过
            if (Objects.isNull(lastDay8) || Objects.isNull(latest)) {
                continue;
            }
            AutoCheckResultVO vo = baseVO(apiCode, SCENE_UPLOAD, apiInfoMap, senceMap);
            vo.setLastDayData(toJsonSafe(lastDay8, "snapTime","apiCode"));
            vo.setThisData(toJsonSafe(latest, "snapTime","apiCode"));
            vo.setTime(latest.getSnapTime());
            vo.setCompareResult(compareUpload(lastDay8, latest));
            voList.add(vo);
        }
        return voList;
    }

    private List<AutoCheckResultVO> checkTransferScene(List<String> apiCodeList,
                                                       Map<String, MarketingCustomerVO> apiInfoMap,
                                                       Map<String, AutoCheckSenceVO> senceMap) {
        if (CollUtil.isEmpty(apiCodeList)) {
            return Collections.emptyList();
        }

        List<CheckTransferSyncDataDto> lastDay8List = transferSyncReportMapper.getLastDay8DataByApiCodes(apiCodeList);
        List<CheckTransferSyncDataDto> latestList = transferSyncReportMapper.getLatestDataByApiCodes(apiCodeList);

        Map<String, CheckTransferSyncDataDto> lastDay8Map = toMapByApiCode(lastDay8List, CheckTransferSyncDataDto::getApiCode);
        Map<String, CheckTransferSyncDataDto> latestMap = toMapByApiCode(latestList, CheckTransferSyncDataDto::getApiCode);

        List<AutoCheckResultVO> voList = new ArrayList<>();
        for (String apiCode : apiCodeList) {
            CheckTransferSyncDataDto lastDay8 = lastDay8Map.get(apiCode);
            CheckTransferSyncDataDto latest = latestMap.get(apiCode);
            // 若前一天八点的数据不存在，或者当前数据不存在，则跳过
            if (Objects.isNull(lastDay8) || Objects.isNull(latest)) {
                continue;
            }
            AutoCheckResultVO vo = baseVO(apiCode, SCENE_TRANSFER, apiInfoMap, senceMap);
            vo.setLastDayData(toJsonSafe(lastDay8, "snapTime","apiCode"));
            vo.setThisData(toJsonSafe(latest, "snapTime","apiCode"));
            vo.setTime(latest.getSnapTime());
            vo.setCompareResult(compareTransfer(lastDay8, latest));
            voList.add(vo);
        }
        return voList;
    }

    private AutoCheckResultVO baseVO(String apiCode,
                                     String senceCode,
                                     Map<String, MarketingCustomerVO> apiInfoMap,
                                     Map<String, AutoCheckSenceVO> senceMap) {
        AutoCheckResultVO vo = new AutoCheckResultVO();
        vo.setApiCode(apiCode);
        vo.setName(Optional.ofNullable(apiInfoMap.get(apiCode)).map(MarketingCustomerVO::getName).orElse(""));
        vo.setSenceCode(senceCode);
        vo.setSenceName(Optional.ofNullable(senceMap.get(senceCode)).map(AutoCheckSenceVO::getSenceName).orElse(""));
        return vo;
    }

    private String compareUpload(CheckUploadSyncDataDto lastDay8, CheckUploadSyncDataDto latest) {
        boolean same = CheckObjectSameUtil.isFieldsEqual(lastDay8, latest, "normalNum", "duplicateRemovalNum");
        return same ? "一致" : "不一致";
    }

    private String compareTransfer(CheckTransferSyncDataDto lastDay8, CheckTransferSyncDataDto latest) {
        boolean same = CheckObjectSameUtil.isFieldsEqual(lastDay8, latest, "dataCount");
        return same ? "一致" : "不一致";
    }

    /**
     * 对象转 JSON 字符串，并按需排除字段（仅对“对象属性”生效；如果传入的是 List/Map 需要按元素/值类型做过滤）。
     *
     * <p>示例：{@code toJsonSafe(latest, "id", "createTime")}</p>
     */
    @SuppressWarnings("unused")
    private String toJsonSafe(Object obj, String... excludeFields) {
        if (obj == null) return "";
        try {
            if (excludeFields == null || excludeFields.length == 0) {
                return OBJECT_MAPPER.writeValueAsString(obj);
            }

            // 注意：不要在共享的 OBJECT_MAPPER 上做 addMixIn / setFilterProvider（配置变更非线程安全），这里用 copy()
            ObjectMapper mapper = OBJECT_MAPPER.copy();
            mapper.addMixIn(obj.getClass(), DynamicFieldFilterMixIn.class);

            Set<String> excludeSet = Arrays.stream(excludeFields)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter(JSON_DYNAMIC_FILTER_ID,
                            SimpleBeanPropertyFilter.serializeAllExcept(excludeSet));

            return mapper.writer(filters).writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }

    private <T> Map<String, T> toMapByApiCode(List<T> list, java.util.function.Function<T, String> apiCodeFn) {
        if (CollUtil.isEmpty(list)) return Collections.emptyMap();
        return list.stream()
                .filter(Objects::nonNull)
                .filter(x -> StringUtils.isNotBlank(apiCodeFn.apply(x)))
                .collect(Collectors.toMap(apiCodeFn, e -> e, (a, b) -> a));
    }
}
