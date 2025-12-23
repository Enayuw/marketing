package com.br.marketing.service.autocheck.impl;

import cn.hutool.core.collection.CollUtil;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.dto.autocheck.CheckTransferSyncDataDto;
import com.br.marketing.dto.autocheck.CheckUploadSyncDataDto;
import com.br.marketing.dto.autocheck.SaveAutoCheckConfigDto;
import com.br.marketing.entity.AutoCheckConfig;
import com.br.marketing.mapper.*;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.service.autocheck.AutoCheckService;
import com.br.marketing.utils.CheckObjectSameUtil;
import com.br.marketing.utils.JsonFilterUtil;
import com.br.marketing.vo.MarketingCustomerVO;
import com.br.marketing.vo.autocheck.AutoCheckResultVO;
import com.br.marketing.vo.autocheck.AutoCheckConfigVO;
import com.br.marketing.vo.autocheck.AutoCheckSceneVO;
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

    @Resource
    private AutoCheckSceneDictMapper autoCheckSceneDictMapper;

    @Resource
    private AutoCheckConfigMapper autoCheckConfigMapper;

    @Resource
    private MarketingCustomerService marketingCustomerService;

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    public List<AutoCheckConfigVO> getAutoCheckConfigList(String apiCodes, String sceneCodes) {
        List<AutoCheckConfigVO> result = new ArrayList<>();

        // 处理apiCodes参数，用逗号分隔
        List<String> apiCodeList = handleApiCodeParam(apiCodes);

        // 处理sceneCodes参数，用逗号分隔
        List<String> sceneCodeList = handleSceneCodeParam(sceneCodes);

        // 根据apiCodes和sceneCodes查询配置信息
        List<AutoCheckConfig> configList = autoCheckConfigMapper.
                selectByApiCodesAndSceneCodes(apiCodeList, sceneCodeList);

        if (CollUtil.isEmpty(configList)) {
            return result;
        }

        // 查询apiCode信息
        List<MarketingCustomerVO> apiCodeInfoList = marketingCustomerService.getApiCodeList(apiCodeList);
        Map<String, MarketingCustomerVO> apiCodeInfoMap = apiCodeInfoList.stream()
                .collect(Collectors.toMap(MarketingCustomerVO::getApiCode, e -> e));

        // 收集所有配置中涉及的场景编码，用于查询场景信息
        List<String> allSceneCodes = new ArrayList<>();
        for (AutoCheckConfig config : configList) {
            if (StringUtils.isNotBlank(config.getSceneCode())) {
                // 配置中的scene_code字段可能包含逗号分隔的多个场景
                List<String> configSceneCodes = Arrays.stream(config.getSceneCode().split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList());
                allSceneCodes.addAll(configSceneCodes);
            }
        }
        // 去重
        allSceneCodes = allSceneCodes.stream().distinct().collect(Collectors.toList());

        // 查询场景信息
        List<AutoCheckSceneVO> autoCheckSceneVOList = autoCheckSceneDictMapper.selectBySceneCodes(allSceneCodes);
        Map<String, AutoCheckSceneVO> sceneMap = autoCheckSceneVOList.stream()
                .collect(Collectors.toMap(AutoCheckSceneVO::getSceneCode, scene -> scene));
        Map<String, AutoCheckConfig> configMapByApiCode = configList.stream()
                .collect(Collectors.toMap(AutoCheckConfig::getApiCode, e -> e));

        // 组装AutoConfigVO
        for (String apiCode : configMapByApiCode.keySet()) {
            AutoCheckConfigVO vo = new AutoCheckConfigVO();
            AutoCheckConfig apiConfig = configMapByApiCode.get(apiCode);
            vo.setId(apiConfig.getId());
            vo.setApiCode(apiCode);
            vo.setName(Optional.ofNullable(apiCodeInfoMap.get(apiCode)).map(MarketingCustomerVO::getName).orElse(""));

            // 收集该ApiCode下的所有场景信息
            List<AutoCheckSceneVO> sceneList = new ArrayList<>();
            String configSceneCodes = apiConfig.getSceneCode();
            if (StringUtils.isNotBlank(configSceneCodes)) {
                String[] split = configSceneCodes.split(",");
                for (String sceneCode : split) {
                    AutoCheckSceneVO autoCheckSceneVO = sceneMap.get(StringUtils.trimToEmpty(sceneCode));
                    if (autoCheckSceneVO != null) {
                        sceneList.add(autoCheckSceneVO);
                    }
                }
            }
            vo.setSceneList(sceneList);
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
            List<AutoCheckConfig> autoCheckConfigs = autoCheckConfigMapper.selectByApiCodesAndSceneCodes(null, null);
            for (AutoCheckConfig autoCheckConfig : autoCheckConfigs) {
                apiCodeList.add(autoCheckConfig.getApiCode());
            }
            apiCodeList = apiCodeList.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        }
        return apiCodeList;

    }

    private List<String> handleSceneCodeParam(String sceneCodes) {
        List<String> sceneCodeList = new ArrayList<>();
        if (StringUtils.isNotBlank(sceneCodes)) {
            sceneCodeList = Arrays.stream(sceneCodes.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            // 获取所有场景编码
            List<AutoCheckSceneVO> allSceneList = autoCheckSceneDictMapper.selectBySceneCodes(null);
            for (AutoCheckSceneVO autoCheckSceneVO : allSceneList) {
                sceneCodeList.add(autoCheckSceneVO.getSceneCode());
            }
            sceneCodeList = sceneCodeList.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        }
        return sceneCodeList;
    }

    @Override
    public List<AutoCheckSceneVO> getAutoCheckSceneList(String searchContent) {
        return autoCheckSceneDictMapper.searchSceneList(searchContent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> saveAutoCheckConfig(SaveAutoCheckConfigDto dto) {
        int result;
        if (Objects.isNull(dto.getId())) {
            // 新增
            // 新增前做防止重复的处理
            AutoCheckConfig existingConfig = autoCheckConfigMapper.selectByApiCode(dto.getApiCode());
            if (Objects.nonNull(existingConfig)) {
                log.warn("要保存的配置已存在，apiCode: {}", dto.getApiCode());
                return new ApiResult<Boolean>().fail(ServiceResultEnum.UNKNOWN_ERROR.getCode())
                        .setData(false)
                        .setMessage("保存失败，apiCode：" + dto.getApiCode() + "已存在");
            }
            AutoCheckConfig config = new AutoCheckConfig();
            config.setApiCode(dto.getApiCode());
            config.setSceneCode(dto.getSceneCodes());
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
                return new ApiResult<Boolean>().fail(ServiceResultEnum.UNKNOWN_ERROR.getCode())
                        .setData(false)
                        .setMessage("要编辑的配置不存在");
            }
            // 更新字段
            existingConfig.setSceneCode(dto.getSceneCodes());
            existingConfig.setUpdateTime(new Date());
            // 更新数据库
            result = autoCheckConfigMapper.updateByPrimaryKeySelective(existingConfig);
        }
        return new ApiResult<Boolean>().success(result > 0);
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
    public List<AutoCheckResultVO> getResultList(String apiCodes, String sceneCodes) {
        // 处理apiCodes参数，用逗号分隔
        List<String> apiCodeList = handleApiCodeParam(apiCodes);

        // 处理sceneCodes参数，用逗号分隔
        List<String> sceneCodeList = handleSceneCodeParam(sceneCodes);

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
        Map<String, AutoCheckSceneVO> sceneMap = autoCheckSceneDictMapper.selectBySceneCodes(sceneCodeList)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AutoCheckSceneVO::getSceneCode, e -> e, (a, b) -> a));

        List<AutoCheckResultVO> result = new ArrayList<>();

        // 针对每一个场景，查询apiCode对应的结果
        for (String sceneCode : sceneCodeList) {
            switch (sceneCode) {
                case SCENE_UPLOAD:
                    // 过滤掉没有配置该场景的apiCode
                    result.addAll(checkUploadScene(
                            filterApiCodesByScene(apiCodeList, SCENE_UPLOAD, configMap),
                            apiInfoMap, sceneMap));
                    break;
                case SCENE_TRANSFER:
                    // 过滤掉没有配置该场景的apiCode
                    result.addAll(checkTransferScene(
                            filterApiCodesByScene(apiCodeList, SCENE_TRANSFER, configMap),
                            apiInfoMap, sceneMap));
                    break;
                default:
                    log.warn("未知场景编码: {}", sceneCode);
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
     * <p>规则：configMap 中存在该 apiCode，且其配置的场景列表包含指定 sceneCode。</p>
     */
    private List<String> filterApiCodesByScene(List<String> apiCodeList,
                                               String sceneCode,
                                               Map<String, AutoCheckConfigVO> configMap) {
        if (CollUtil.isEmpty(apiCodeList) || StringUtils.isBlank(sceneCode) || configMap == null || configMap.isEmpty()) {
            return Collections.emptyList();
        }
        return apiCodeList.stream()
                .filter(StringUtils::isNotBlank)
                .filter(apiCode -> {
                    AutoCheckConfigVO cfg = configMap.get(apiCode);
                    if (cfg == null || CollUtil.isEmpty(cfg.getSceneList())) {
                        return false;
                    }
                    return cfg.getSceneList().stream()
                            .filter(Objects::nonNull)
                            .map(AutoCheckSceneVO::getSceneCode)
                            .anyMatch(code -> StringUtils.equals(code, sceneCode));
                })
                .distinct()
                .collect(Collectors.toList());
    }

    private List<AutoCheckResultVO> checkUploadScene(List<String> apiCodeList,
                                                     Map<String, MarketingCustomerVO> apiInfoMap,
                                                     Map<String, AutoCheckSceneVO> sceneMap) {
        if (CollUtil.isEmpty(apiCodeList)) {
            return Collections.emptyList();
        }

        List<CheckUploadSyncDataDto> lastDay8List = marketingSyncInfoMapper.getLastDay8DataByApiCodes(apiCodeList);
        List<CheckUploadSyncDataDto> latestList = marketingSyncInfoMapper.getLatestDataByApiCodes(apiCodeList);

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
            AutoCheckResultVO vo = baseVO(apiCode, SCENE_UPLOAD, apiInfoMap, sceneMap);
            vo.setLastDayData(toJsonExcludeSafe(lastDay8, "snapTime", "cusBatch",
                    "requestBatch", "custNum", "registerDate", "createTime", "updateTime",
                    "appletDate", "appletTime", "taskTime", "fingerprint"));
            vo.setThisData(toJsonExcludeSafe(latest, "snapTime", "cusBatch",
                    "requestBatch", "custNum", "registerDate", "createTime", "updateTime",
                    "appletDate", "appletTime", "taskTime", "fingerprint"));
            vo.setTime(latest.getSnapTime());
            vo.setCompareResult(compareUpload(lastDay8, latest));
            voList.add(vo);
        }
        return voList;
    }

    private List<AutoCheckResultVO> checkTransferScene(List<String> apiCodeList,
                                                       Map<String, MarketingCustomerVO> apiInfoMap,
                                                       Map<String, AutoCheckSceneVO> sceneMap) {
        if (CollUtil.isEmpty(apiCodeList)) {
            return Collections.emptyList();
        }

        // 查出cid
        List<String> cidList = new ArrayList<>();
        for (String apiCode : apiCodeList) {
            MarketingCustomerVO apiInfo = apiInfoMap.get(apiCode);
            if (apiInfo == null || StringUtils.isBlank(apiInfo.getCid())) {
                continue;
            }
            cidList.add(apiInfo.getCid().replaceFirst("-", ""));
        }

        List<CheckTransferSyncDataDto> lastDay8List = marketingTransferSyncUserMapper.getLastDay8DataByCids(cidList, apiCodeList);
        List<CheckTransferSyncDataDto> latestList = marketingTransferSyncUserMapper.getLatestDataByCids(cidList, apiCodeList);

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
            AutoCheckResultVO vo = baseVO(apiCode, SCENE_TRANSFER, apiInfoMap, sceneMap);
            vo.setLastDayData(toJsonExcludeSafe(lastDay8, "snapTime", "cid", "tCid",
                    "requestId", "registerTime", "loginTime", "applyDt", "applyTime", "refuseTime",
                    "auditTime", "lentTime", "settleTime", "transformTime", "insertTime", "createTime",
                    "updateTime", "requestTime", "fingerprint"));
            vo.setThisData(toJsonExcludeSafe(latest, "snapTime", "cid", "tCid",
                    "requestId", "registerTime", "loginTime", "applyDt", "applyTime", "refuseTime",
                    "auditTime", "lentTime", "settleTime", "transformTime", "insertTime", "createTime",
                    "updateTime", "requestTime", "fingerprint"));
            vo.setTime(latest.getSnapTime());
            vo.setCompareResult(compareTransfer(lastDay8, latest));
            voList.add(vo);
        }
        return voList;
    }

    private AutoCheckResultVO baseVO(String apiCode,
                                     String sceneCode,
                                     Map<String, MarketingCustomerVO> apiInfoMap,
                                     Map<String, AutoCheckSceneVO> sceneMap) {
        AutoCheckResultVO vo = new AutoCheckResultVO();
        vo.setApiCode(apiCode);
        vo.setName(Optional.ofNullable(apiInfoMap.get(apiCode)).map(MarketingCustomerVO::getName).orElse(""));
        vo.setSceneCode(sceneCode);
        vo.setSceneName(Optional.ofNullable(sceneMap.get(sceneCode)).map(AutoCheckSceneVO::getSceneName).orElse(""));
        return vo;
    }

    private String compareUpload(CheckUploadSyncDataDto lastDay8, CheckUploadSyncDataDto latest) {
        boolean same = CheckObjectSameUtil.isAllFieldsEqualExclude(lastDay8, latest, "snapTime", "cusBatch",
                "requestBatch", "custNum", "registerDate", "createTime", "updateTime",
                "appletDate", "appletTime", "taskTime", "fingerprint");
        return same ? "一致" : "不一致";
    }

    private String compareTransfer(CheckTransferSyncDataDto lastDay8, CheckTransferSyncDataDto latest) {
        boolean same = CheckObjectSameUtil.isAllFieldsEqualExclude(lastDay8, latest, "snapTime", "cid", "tCid",
                "requestId", "registerTime", "loginTime", "applyDt", "applyTime", "refuseTime",
                "auditTime", "lentTime", "settleTime", "transformTime", "insertTime", "createTime",
                "updateTime", "requestTime", "fingerprint");
        return same ? "一致" : "不一致";
    }

    private String toJsonExcludeSafe(Object obj, String... excludeFields) {
        return JsonFilterUtil.toJsonExcludeSafe(obj, excludeFields);
    }

    private <T> Map<String, T> toMapByApiCode(List<T> list, java.util.function.Function<T, String> apiCodeFn) {
        if (CollUtil.isEmpty(list)) return Collections.emptyMap();
        return list.stream()
                .filter(Objects::nonNull)
                .filter(x -> StringUtils.isNotBlank(apiCodeFn.apply(x)))
                .collect(Collectors.toMap(apiCodeFn, e -> e, (a, b) -> a));
    }
}
