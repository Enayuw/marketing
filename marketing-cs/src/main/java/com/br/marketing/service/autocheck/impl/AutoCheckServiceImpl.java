package com.br.marketing.service.autocheck.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.dto.autocheck.*;
import com.br.marketing.entity.AutoCheckConfig;
import com.br.marketing.entity.AutoCheckResultLog;
import com.br.marketing.entity.AutoCheckTableDict;
import com.br.marketing.entity.AutoCheckTableDictExample;
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

    private static final String COMPARE_RESULT_SAME = "一致";
    private static final String COMPARE_RESULT_DIFFERENT = "不一致";

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

        // 查询场景信息
        List<AutoCheckSceneVO> autoCheckSceneVOList = autoCheckSceneDictMapper.selectBySceneCodes(sceneCodeList);
        Map<String, AutoCheckSceneVO> sceneMap = autoCheckSceneVOList.stream()
                .collect(Collectors.toMap(AutoCheckSceneVO::getSceneCode, scene -> scene));

        // 按照apiCode和sceneCode进行聚合分组
        Map<String, List<AutoCheckConfig>> configGroupMap = configList.stream()
                .filter(Objects::nonNull)
                .filter(cfg -> StringUtils.isNotBlank(cfg.getApiCode()) && StringUtils.isNotBlank(cfg.getSceneCode()))
                .collect(Collectors.groupingBy(cfg -> buildKey(cfg.getApiCode().trim(), cfg.getSceneCode().trim()),
                        LinkedHashMap::new,
                        Collectors.toList()));

        for (Map.Entry<String, List<AutoCheckConfig>> entry : configGroupMap.entrySet()) {
            List<AutoCheckConfig> group = entry.getValue();
            if (CollUtil.isEmpty(group)) {
                continue;
            }
            AutoCheckConfig first = group.get(0);
            if (first == null || StringUtils.isBlank(first.getApiCode()) || StringUtils.isBlank(first.getSceneCode())) {
                continue;
            }

            String apiCode = first.getApiCode().trim();
            String sceneCode = first.getSceneCode().trim();

            String tableNames = group.stream()
                    .filter(Objects::nonNull)
                    .map(AutoCheckConfig::getTableName)
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.joining(","));

            AutoCheckConfigVO vo = new AutoCheckConfigVO();
            vo.setApiCode(apiCode);
            vo.setName(Optional.ofNullable(apiCodeInfoMap.get(apiCode)).map(MarketingCustomerVO::getName).orElse(""));
            vo.setSceneCode(sceneCode);
            vo.setSceneName(Optional.ofNullable(sceneMap.get(sceneCode)).map(AutoCheckSceneVO::getSceneName).orElse(""));
            vo.setTableNames(tableNames);
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

        String apiCode = dto.getApiCode();
        String sceneCode = dto.getSceneCode();
        List<SaveAutoCheckConfigDto.TableNameAndField> tableNameAndFieldList = dto.getTableNameAndFieldList();
        // 拿apiCode和sceneCode去表里查询记录
        List<AutoCheckConfig> existingConfigs = autoCheckConfigMapper
                .selectByApiCodesAndSceneCodes(Collections.singletonList(apiCode)
                        , Collections.singletonList(sceneCode));

        if (!dto.getIsUpdate() && CollUtil.isNotEmpty(existingConfigs)) {
            log.warn("QA自动化巡检,要保存的配置已存在，apiCode: {}, sceneCode: {}", apiCode, sceneCode);
            result.setRes(false);
            result.setCode(ServiceResultEnum.UNKNOWN_ERROR.getCode());
            result.setMessage("保存失败，apiCode：" + apiCode + "，sceneCode：" + sceneCode + "已存在");
            return result;
        }

        if (dto.getIsUpdate() && CollUtil.isEmpty(existingConfigs)) {
            log.warn("QA自动化巡检,要编辑的配置不存在，apiCode: {}, sceneCode: {}", apiCode, sceneCode);
            result.setRes(false);
            result.setCode(ServiceResultEnum.UNKNOWN_ERROR.getCode());
            result.setMessage("要编辑的配置不存在");
            return result;
        }

        if (CollUtil.isNotEmpty(existingConfigs)) {
            autoCheckConfigMapper.batchDelete(existingConfigs);
        }

        List<AutoCheckConfig> insertConfigs = new ArrayList<>();
        for (SaveAutoCheckConfigDto.TableNameAndField tableNameAndField : tableNameAndFieldList) {
            if (tableNameAndField == null
                    || StringUtils.isBlank(tableNameAndField.getTableName())
                    || StringUtils.isBlank(tableNameAndField.getFieldNames())) {
                continue;
            }
            AutoCheckConfig config = new AutoCheckConfig();
            config.setApiCode(apiCode);
            config.setSceneCode(sceneCode);
            config.setTableName(tableNameAndField.getTableName().trim());
            config.setFieldName(tableNameAndField.getFieldNames().trim());
            insertConfigs.add(config);
        }
        // 防止同一个表重复提交导致重复插入（按 tableName 去重，保留第一条）
        if (CollUtil.isNotEmpty(insertConfigs)) {
            insertConfigs = insertConfigs.stream()
                    .collect(Collectors.toMap(AutoCheckConfig::getTableName, e -> e, (a, b) -> a, LinkedHashMap::new))
                    .values().stream().collect(Collectors.toList());
        }
        if (CollUtil.isNotEmpty(insertConfigs)) {
            autoCheckConfigMapper.batchInsert(insertConfigs);
        } else {
            result.setRes(false);
            result.setCode(ServiceResultEnum.UNKNOWN_ERROR.getCode());
            result.setMessage("有效配置为空，请检查 tableName/fieldNames");
            return result;
        }
        result.setRes(true);
        return result;
    }

    @Override
    public Boolean delAutoCheckConfig(String apiCode, String sceneCode) {
        if (StringUtils.isBlank(apiCode) || StringUtils.isBlank(sceneCode)) {
            return false;
        }
        List<AutoCheckConfig> existingConfigs = autoCheckConfigMapper
                .selectByApiCodesAndSceneCodes(Collections.singletonList(apiCode)
                        , Collections.singletonList(sceneCode));
        if (CollUtil.isEmpty(existingConfigs)) {
            log.warn("QA自动化巡检,要删除的配置不存在，apiCode: {}, sceneCode: {}", apiCode, sceneCode);
            return true;
        }
        autoCheckConfigMapper.batchDelete(existingConfigs);
        return true;
    }

    @Override
    public void autoCheck() {
        // 1、获取所有配置
        List<String> apiCodeList = handleApiCodeParam(null);
        List<String> sceneCodeList = handleSceneCodeParam(null);

        // 获取apiCode对应的场景配置
        List<AutoCheckConfigVO> configList = getAutoCheckConfigList(apiCodeList, sceneCodeList);
        // apiCode -> 配置的场景编码集合
        Map<String, Set<String>> apiSceneCodeMap = configList.stream()
                .filter(Objects::nonNull)
                .filter(vo -> StringUtils.isNotBlank(vo.getApiCode()) && StringUtils.isNotBlank(vo.getSceneCode()))
                .collect(Collectors.groupingBy(vo -> vo.getApiCode().trim(),
                        Collectors.mapping(vo -> vo.getSceneCode().trim(), Collectors.toSet())));

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
                            filterApiCodesByScene(apiCodeList, SCENE_UPLOAD, apiSceneCodeMap),
                            comparedIdMap));
                    break;
                case SCENE_TRANSFER:
                    // 过滤掉没有配置该场景的apiCode
                    saveList.addAll(checkTransferScene(
                            filterApiCodesByScene(apiCodeList, SCENE_TRANSFER, apiSceneCodeMap),
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

        // 组装resultVO
        if (CollUtil.isNotEmpty(resultList)) {
            // 表字典：tableName -> tableDesc（用于明细展示）
            Map<String, String> tableDescMap = new HashMap<>();
            List<String> tableNameList = resultList.stream()
                    .filter(Objects::nonNull)
                    .map(AutoCheckResultLog::getTableName)
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(tableNameList)) {
                AutoCheckTableDictExample example = new AutoCheckTableDictExample();
                example.createCriteria()
                        .andIsDeletedEqualTo((byte) 0)
                        .andTableNameIn(tableNameList);
                List<AutoCheckTableDict> tableDictList = autoCheckTableDictMapper.selectByExample(example);
                if (CollUtil.isNotEmpty(tableDictList)) {
                    tableDescMap = tableDictList.stream()
                            .collect(Collectors.toMap(
                                    d -> d.getTableName().trim(),
                                    d -> StringUtils.defaultString(d.getTableDesc()),
                                    (a, b) -> a
                            ));
                }
            }

            // 按 apiCode + sceneCode 聚合
            Map<String, List<AutoCheckResultLog>> groupMap = resultList.stream()
                    .collect(Collectors.groupingBy(
                            r -> buildKey(r.getApiCode().trim(), r.getSceneCode().trim()),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            for (Map.Entry<String, List<AutoCheckResultLog>> entry : groupMap.entrySet()) {
                List<AutoCheckResultLog> group = entry.getValue();
                if (CollUtil.isEmpty(group)) {
                    continue;
                }
                AutoCheckResultLog first = group.get(0);

                String apiCode = first.getApiCode().trim();
                String sceneCode = first.getSceneCode().trim();

                AutoCheckResultVO vo = new AutoCheckResultVO();
                vo.setApiCode(apiCode);
                vo.setSceneCode(sceneCode);

                MarketingCustomerVO apiInfo = apiInfoMap.get(apiCode);
                vo.setName(apiInfo == null ? "" : StringUtils.defaultString(apiInfo.getName()));

                AutoCheckSceneVO sceneInfo = sceneMap.get(sceneCode);
                vo.setSceneName(sceneInfo == null ? "" : StringUtils.defaultString(sceneInfo.getSceneName()));

                // 外层 time：取明细里最晚时间
                String earliestTime = null;
                // 外层 compareResult：只要任一条不一致，则不一致；全部一致才一致
                boolean allSame = true;

                List<AutoCheckResultVO.CompareResultDetail> detailList = new ArrayList<>();
                for (AutoCheckResultLog row : group) {
                    AutoCheckResultVO.CompareResultDetail detail = new AutoCheckResultVO.CompareResultDetail();
                    String tableName = StringUtils.defaultString(row.getTableName()).trim();
                    detail.setTableName(tableName);
                    detail.setTableDesc(StringUtils.defaultString(tableDescMap.get(tableName)));
                    detail.setLastDayData(StringUtils.defaultString(row.getLastData()));
                    detail.setThisData(StringUtils.defaultString(row.getTodayData()));
                    detail.setCompareResult(StringUtils.defaultString(row.getResult()));

                    String time = StringUtils.isBlank(row.getCompareTime()) ? "" : row.getCompareTime().trim();
                    detail.setTime(time);

                    // earliestTime（compare_time 通常为 yyyy-MM-dd HH:mm:ss，字典序=时间序）
                    if (StringUtils.isNotBlank(time)) {
                        if (earliestTime == null || time.compareTo(earliestTime) > 0) {
                            earliestTime = time;
                        }
                    }
                    // 聚合 compareResult：非“一致”都视为不一致（兼容后续新增结果值）
                    if (!COMPARE_RESULT_SAME.equals(detail.getCompareResult())) {
                        allSame = false;
                    }
                    detailList.add(detail);
                }

                vo.setTime(StringUtils.defaultString(earliestTime));
                vo.setCompareResult(allSame ? COMPARE_RESULT_SAME : COMPARE_RESULT_DIFFERENT);
                vo.setCompareResultDetailList(detailList);

                vo.setLastDayData(detailList.get(0).getLastDayData());
                vo.setThisData(detailList.get(0).getThisData());

                result.add(vo);
            }
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
                                               Map<String, Set<String>> apiSceneCodeMap) {
        if (CollUtil.isEmpty(apiCodeList) || StringUtils.isBlank(sceneCode) || apiSceneCodeMap == null || apiSceneCodeMap.isEmpty()) {
            return Collections.emptyList();
        }
        return apiCodeList.stream()
                .filter(StringUtils::isNotBlank)
                .filter(apiCode -> {
                    Set<String> scenes = apiSceneCodeMap.get(apiCode.trim());
                    return CollUtil.isNotEmpty(scenes) && scenes.contains(sceneCode);
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
