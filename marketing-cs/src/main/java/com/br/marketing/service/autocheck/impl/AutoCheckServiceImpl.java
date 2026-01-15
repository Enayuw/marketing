package com.br.marketing.service.autocheck.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.dto.autocheck.*;
import com.br.marketing.entity.AutoCheckConfig;
import com.br.marketing.entity.AutoCheckResultLog;
import com.br.marketing.mapper.*;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.service.autocheck.AutoCheckService;
import com.br.marketing.utils.CheckObjectSameUtil;
import com.br.marketing.utils.JsonFilterUtil;
import com.br.marketing.vo.MarketingCustomerVO;
import com.br.marketing.vo.autocheck.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
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

    @Resource
    private AutoCheckResultLogMapper autoCheckResultLogMapper;

    @Resource
    private AutoCheckTableDictMapper autoCheckTableDictMapper;


    @Override
    public List<AutoCheckConfigVO> getAutoCheckConfigList(String apiCodes, String sceneCodes) {
        // 处理apiCodes参数，用逗号分隔
        List<String> apiCodeList = handleApiCodeParam(apiCodes);

        // 处理sceneCodes参数，用逗号分隔
        List<String> sceneCodeList = handleSceneCodeParam(sceneCodes);

        return getAutoCheckConfigList(apiCodeList, sceneCodeList);
    }

    private List<AutoCheckConfigVO> getAutoCheckConfigList(List<String> apiCodeList, List<String> sceneCodeList) {
        List<AutoCheckConfigVO> result = new ArrayList<>();

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
    public SaveAutoCheckConfigResDto saveAutoCheckConfig(SaveAutoCheckConfigDto dto) {
        SaveAutoCheckConfigResDto result = new SaveAutoCheckConfigResDto();
        if (Objects.isNull(dto.getId())) {
            // 新增
            // 新增前做防止重复的处理
            AutoCheckConfig existingConfig = autoCheckConfigMapper.selectByApiCode(dto.getApiCode());
            if (Objects.nonNull(existingConfig)) {
                log.warn("QA自动化巡检,要保存的配置已存在，apiCode: {}", dto.getApiCode());
                result.setRes(false);
                result.setCode(ServiceResultEnum.UNKNOWN_ERROR.getCode());
                result.setMessage("保存失败，apiCode：" + dto.getApiCode() + "已存在");
                return result;
            }
            AutoCheckConfig config = new AutoCheckConfig();
            config.setApiCode(dto.getApiCode());
            config.setSceneCode(dto.getSceneCodes());
            Date now = new Date();
            config.setCreateTime(now);
            config.setUpdateTime(now);
            // 插入数据库
            result.setRes(autoCheckConfigMapper.insertSelective(config) > 0);
        } else {
            // 编辑
            AutoCheckConfig existingConfig = autoCheckConfigMapper.selectByPrimaryKey(dto.getId());
            if (Objects.isNull(existingConfig)) {
                log.warn("QA自动化巡检,要编辑的配置不存在，id: {}", dto.getId());
                result.setRes(false);
                result.setCode(ServiceResultEnum.UNKNOWN_ERROR.getCode());
                result.setMessage("要编辑的配置不存在");
                return result;
            }
            // 更新字段
            existingConfig.setSceneCode(dto.getSceneCodes());
            existingConfig.setUpdateTime(new Date());
            // 更新数据库
            result.setRes(autoCheckConfigMapper.updateByPrimaryKeySelective(existingConfig) > 0);
        }
        return result;
    }

    @Override
    public Boolean delAutoCheckConfig(Long id) {
        if (Objects.isNull(id)) {
            return true;
        }
        AutoCheckConfig existingConfig = autoCheckConfigMapper.selectByPrimaryKey(id);
        if (Objects.isNull(existingConfig)) {
            log.warn("QA自动化巡检,要删除的配置不存在，id: {}", id);
            return true;
        }
        // 更新字段
        existingConfig.setIsDeleted((byte) 1);
        existingConfig.setUpdateTime(new Date());
        // 更新数据库
        return autoCheckConfigMapper.updateByPrimaryKeySelective(existingConfig) > 0;
    }

    @Override
    public void autoCheck() {
        // 1、获取所有配置
        List<String> apiCodeList = handleApiCodeParam(null);
        List<String> sceneCodeList = handleSceneCodeParam(null);

        // 获取apiCode对应的场景配置
        List<AutoCheckConfigVO> configList = getAutoCheckConfigList(apiCodeList, sceneCodeList);
        Map<String, AutoCheckConfigVO> configMap = configList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AutoCheckConfigVO::getApiCode, e -> e, (a, b) -> a));

        // apiCode 基础信息（名称）
        Map<String, MarketingCustomerVO> apiInfoMap = marketingCustomerService.getApiCodeList(apiCodeList)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MarketingCustomerVO::getApiCode, e -> e, (a, b) -> a));

        // 获取今天已经比对过的id
        String today = DateUtil.today();
        List<AutoCheckResultLog> resultList = autoCheckResultLogMapper.selectByCodeListAndTime(today, null, null);
        Map<String, List<Long>> comparedIdMap = new HashMap<>();
        for (AutoCheckResultLog result : resultList) {
            String key = buildKey(result.getApiCode(), result.getSceneCode());
            List<Long> values = comparedIdMap.get(key);
            if (values == null) {
                values = new ArrayList<>();
            }
            values.add(result.getTodayDataId());
            comparedIdMap.put(key, values);
        }

        List<AutoCheckResultLog> saveList = new ArrayList<>();
        // 针对每一个场景，查询apiCode对应的结果
        for (String sceneCode : sceneCodeList) {
            switch (sceneCode) {
                case SCENE_UPLOAD:
                    // 过滤掉没有配置该场景的apiCode
                    saveList.addAll(checkUploadScene(
                            filterApiCodesByScene(apiCodeList, SCENE_UPLOAD, configMap),
                            comparedIdMap));
                    break;
                case SCENE_TRANSFER:
                    // 过滤掉没有配置该场景的apiCode
                    saveList.addAll(checkTransferScene(
                            filterApiCodesByScene(apiCodeList, SCENE_TRANSFER, configMap),
                            apiInfoMap, comparedIdMap));
                    break;
                default:
                    log.warn("未知场景编码: {}", sceneCode);
            }
        }
        if (CollUtil.isNotEmpty(saveList)) {
            autoCheckResultLogMapper.batchInsert(saveList);
        }
    }

    @Override
    public List<AutoCheckResultVO> getResultList(String apiCodes, String sceneCodes) {
        // 处理apiCodes参数，用逗号分隔
        List<String> apiCodeList = handleApiCodeParam(apiCodes);

        // 处理sceneCodes参数，用逗号分隔
        List<String> sceneCodeList = handleSceneCodeParam(sceneCodes);

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
        // 找出当天的比对结果，用于前端展示
        String today = DateUtil.today();
        List<AutoCheckResultLog> resultList = autoCheckResultLogMapper.selectByCodeListAndTime(today, apiCodeList, sceneCodeList);
        for (AutoCheckResultLog log : resultList) {
            AutoCheckResultVO autoCheckResultVO = new AutoCheckResultVO();
            autoCheckResultVO.setTime(log.getCompareTime());
            autoCheckResultVO.setApiCode(log.getApiCode());
            autoCheckResultVO.setName(Optional.ofNullable(apiInfoMap.get(log.getApiCode()))
                    .map(MarketingCustomerVO::getName).orElse(""));
            autoCheckResultVO.setSceneCode(log.getSceneCode());
            autoCheckResultVO.setSceneName(Optional.ofNullable(sceneMap.get(log.getSceneCode()))
                    .map(AutoCheckSceneVO::getSceneName).orElse(""));
            autoCheckResultVO.setLastDayData(log.getLastData());
            autoCheckResultVO.setThisData(log.getTodayData());
            autoCheckResultVO.setCompareResult(log.getResult());
            result.add(autoCheckResultVO);
        }
        // 按时间倒序（time 为 yyyy-MM-dd HH:mm:ss 字符串，字典序=时间序）；空值/空串放最后
        result.sort(Comparator.comparing(
                vo -> StringUtils.isBlank(vo.getTime()) ? null : vo.getTime().trim(),
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        return result;
    }

    @Override
    public List<AutoCheckAssociationTableVO> getAssociationTable(String tableName) {
        return autoCheckTableDictMapper.getAssociationTable(tableName);
    }

    @Override
    public List<AutoCheckAssociationTableFieldVO> getAssociationTableFields(QueryAssociationTableFieldDto dto) {
        if (dto == null || CollUtil.isEmpty(dto.getTableNameList())) {
            return Collections.emptyList();
        }

        List<String> tableNameList = dto.getTableNameList().stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(tableNameList)) {
            return Collections.emptyList();
        }

        // 先按入参顺序初始化，确保“表不存在”也能返回空 fieldList
        Map<String, List<AutoCheckAssociationTableFieldVO.FieldVO>> tableFieldMap = new LinkedHashMap<>();
        for (String tableName : tableNameList) {
            tableFieldMap.put(tableName, new ArrayList<>());
        }

        List<AutoCheckTableColumnVO> rows = autoCheckTableDictMapper.getAssociationTableColumns(tableNameList);
        if (CollUtil.isNotEmpty(rows)) {
            for (AutoCheckTableColumnVO row : rows) {
                if (row == null || StringUtils.isBlank(row.getTableName()) || StringUtils.isBlank(row.getFieldName())) {
                    continue;
                }

                AutoCheckAssociationTableFieldVO.FieldVO fieldVO = new AutoCheckAssociationTableFieldVO.FieldVO();

                fieldVO.setFieldName(row.getFieldName().trim());
                fieldVO.setFieldDesc(StringUtils.defaultString(row.getFieldDesc()));

                tableFieldMap.computeIfAbsent(row.getTableName().trim(), k -> new ArrayList<>()).add(fieldVO);
            }
        }

        List<AutoCheckAssociationTableFieldVO> result = new ArrayList<>();
        for (Map.Entry<String, List<AutoCheckAssociationTableFieldVO.FieldVO>> entry : tableFieldMap.entrySet()) {
            AutoCheckAssociationTableFieldVO vo = new AutoCheckAssociationTableFieldVO();
            vo.setTableName(entry.getKey());
            vo.setFieldList(entry.getValue());
            result.add(vo);

            // 表不存在/无字段：不抛错，给前端空列表即可
            if (CollUtil.isEmpty(entry.getValue())) {
                log.warn("QA自动化巡检-查询关联表字段：表不存在或无字段，tableName={}", entry.getKey());
            }
        }
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

    private List<AutoCheckResultLog> checkUploadScene(List<String> apiCodeList,
                                                      Map<String, List<Long>> comparedIdMap) {
        List<AutoCheckResultLog> resultLogList = new ArrayList<>();
        for (String apiCode : apiCodeList) {
            CheckUploadSyncDataDto lastDay8;
            CheckUploadSyncDataDto latest;
            try {
                lastDay8 = marketingSyncInfoMapper.getLastDay8DataByApiCode(apiCode);
                latest = marketingSyncInfoMapper.getLatestDataByApiCode(apiCode);
            } catch (Exception ex) {
                if (isUploadSyncTableNotExist(ex, apiCode)) {
                    // 分表不存在：跳过该 apiCode（不影响其他 apiCode 的查询）
                    log.warn("QA自动化巡检-上传场景：跳过 apiCode={}，分表不存在：b_marketing_sync_{}", apiCode, apiCode);
                    continue;
                }
                throw ex;
            }
            // 若前一天八点的数据不存在，或者当前数据不存在，则跳过
            if (lastDay8 == null || latest == null) {
                log.warn("QA自动化巡检-上传场景：跳过 apiCode={}，数据不存在", apiCode);
                continue;
            }
            // 若当天本条数据已经比对过，则跳过
            String key = buildKey(apiCode, SCENE_UPLOAD);
            List<Long> existIds = comparedIdMap.get(key);
            if (CollUtil.isNotEmpty(existIds) && existIds.contains(latest.getId())) {
                continue;
            }
            AutoCheckResultLog log = new AutoCheckResultLog();
            log.setApiCode(apiCode);
            log.setSceneCode(SCENE_UPLOAD);
            log.setCompareTime(latest.getSnapTime());
            log.setTodayDataId(latest.getId());
            log.setLastData(toJsonExcludeSafe(lastDay8, "id", "snapTime", "cusBatch",
                    "requestBatch", "custNum", "fingerprint"));
            log.setTodayData(toJsonExcludeSafe(latest, "id", "snapTime", "cusBatch",
                    "requestBatch", "custNum", "fingerprint"));
            log.setResult(compareUpload(lastDay8, latest));
            Date now = new Date();
            log.setCreateTime(now);
            log.setUpdateTime(now);
            resultLogList.add(log);
        }

        return resultLogList;
    }

    /**
     * 判断是否为“上传同步分表不存在”的异常（MySQL error code: 1146）。
     */
    private boolean isUploadSyncTableNotExist(Throwable ex, String apiCode) {
        String tableName = "b_marketing_sync_" + apiCode;
        Throwable t = ex;
        while (t != null) {
            if (t instanceof SQLException) {
                SQLException sqlEx = (SQLException) t;
                if (sqlEx.getErrorCode() == 1146) {
                    return true;
                }
            }
            String msg = t.getMessage();
            if (StringUtils.isNotBlank(msg)
                    && msg.contains("doesn't exist")
                    && msg.contains(tableName)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private List<AutoCheckResultLog> checkTransferScene(List<String> apiCodeList,
                                                        Map<String, MarketingCustomerVO> apiInfoMap,
                                                        Map<String, List<Long>> comparedIdMap) {
        List<AutoCheckResultLog> resultLogList = new ArrayList<>();
        for (String apiCode : apiCodeList) {
            MarketingCustomerVO apiInfo = apiInfoMap.get(apiCode);
            if (apiInfo == null || StringUtils.isBlank(apiInfo.getCid())) {
                log.warn("QA自动化巡检-转化场景：跳过 apiCode={}，cid为空", apiCode);
                continue;
            }
            String tCid = apiInfo.getCid().replaceFirst("-", "");

            CheckTransferSyncDataDto lastDay8;
            CheckTransferSyncDataDto latest;
            try {
                lastDay8 = marketingTransferSyncUserMapper.getLastDay8DataByCidAndApiCode(tCid, apiCode);
                latest = marketingTransferSyncUserMapper.getLatestDataByCidAndApiCode(tCid, apiCode);
            } catch (Exception ex) {
                if (isTransferSyncTableNotExist(ex, tCid)) {
                    log.warn("QA自动化巡检-转化场景：跳过 apiCode={}，分表不存在：b_marketing_transfer_sync_{}", apiCode, tCid);
                    continue;
                }
                throw ex;
            }
            // 若前一天八点的数据不存在，或者当前数据不存在，则跳过
            if (Objects.isNull(lastDay8) || Objects.isNull(latest)) {
                log.warn("QA自动化巡检-转化场景：跳过 apiCode={}，数据不存在", apiCode);
                continue;
            }
            // 若当天本条数据已经比对过，则跳过
            String key = buildKey(apiCode, SCENE_TRANSFER);
            List<Long> existIds = comparedIdMap.get(key);
            if (CollUtil.isNotEmpty(existIds) && existIds.contains(latest.getId())) {
                continue;
            }
            AutoCheckResultLog log = new AutoCheckResultLog();
            log.setApiCode(apiCode);
            log.setSceneCode(SCENE_TRANSFER);
            log.setCompareTime(latest.getSnapTime());
            log.setTodayDataId(latest.getId());
            log.setLastData(toJsonExcludeSafe(lastDay8, "id", "snapTime", "cid", "tCid", "fingerprint"));
            log.setTodayData(toJsonExcludeSafe(latest, "id", "snapTime", "cid", "tCid", "fingerprint"));
            log.setResult(compareTransfer(lastDay8, latest));
            Date now = new Date();
            log.setCreateTime(now);
            log.setUpdateTime(now);
            resultLogList.add(log);
        }
        return resultLogList;
    }

    /**
     * 判断是否为“转化同步分表不存在”的异常（MySQL error code: 1146）。
     */
    private boolean isTransferSyncTableNotExist(Throwable ex, String tCid) {
        String tableName = "b_marketing_transfer_sync_" + tCid;
        Throwable t = ex;
        while (t != null) {
            if (t instanceof SQLException) {
                SQLException sqlEx = (SQLException) t;
                if (sqlEx.getErrorCode() == 1146) {
                    return true;
                }
            }
            String msg = t.getMessage();
            if (StringUtils.isNotBlank(msg)
                    && msg.contains("doesn't exist")
                    && msg.contains(tableName)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private String compareUpload(CheckUploadSyncDataDto lastDay8, CheckUploadSyncDataDto latest) {
        boolean same = CheckObjectSameUtil.isAllFieldsEqualExclude(lastDay8, latest, "id", "snapTime", "cusBatch",
                "requestBatch", "custNum", "fingerprint");
        return same ? "一致" : "不一致";
    }

    private String compareTransfer(CheckTransferSyncDataDto lastDay8, CheckTransferSyncDataDto latest) {
        boolean same = CheckObjectSameUtil.isAllFieldsEqualExclude(lastDay8, latest, "id", "snapTime", "cid", "tCid", "fingerprint");
        return same ? "一致" : "不一致";
    }

    private String toJsonExcludeSafe(Object obj, String... excludeFields) {
        return JsonFilterUtil.toJsonExcludeSafe(obj, excludeFields);
    }

    private String buildKey(String apiCode, String sceneCode) {
        return apiCode + "_" + sceneCode;
    }
}
