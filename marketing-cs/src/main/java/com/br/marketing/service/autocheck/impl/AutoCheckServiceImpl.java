package com.br.marketing.service.autocheck.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.dto.autocheck.*;
import com.br.marketing.entity.AutoCheckConfig;
import com.br.marketing.entity.AutoCheckResultLog;
import com.br.marketing.entity.AutoCheckSceneDict;
import com.br.marketing.entity.AutoCheckSceneDictExample;
import com.br.marketing.entity.AutoCheckTableDict;
import com.br.marketing.entity.AutoCheckTableDictExample;
import com.br.marketing.mapper.*;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.service.autocheck.AutoCheckService;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: fuzhen.zhang
 * @description: 自动化巡检service
 * @date: 2025/12/18 15:35
 */
@Service
@Slf4j
public class AutoCheckServiceImpl implements AutoCheckService {

    private static final String COMPARE_RESULT_SAME = "一致";
    private static final String COMPARE_RESULT_DIFFERENT = "不一致";

    @Resource
    private AutoCheckSceneDictMapper autoCheckSceneDictMapper;

    @Resource
    private AutoCheckConfigMapper autoCheckConfigMapper;

    @Resource
    private MarketingCustomerService marketingCustomerService;

    @Resource
    private AutoCheckResultLogMapper autoCheckResultLogMapper;

    @Resource
    private AutoCheckTableDictMapper autoCheckTableDictMapper;

    @Resource
    private AutoCheckDynamicDataMapper autoCheckDynamicDataMapper;

    /**
     * SQL 标识符白名单：仅允许字母数字下划线，防止 ${} 拼接注入。
     */
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[0-9a-zA-Z_]+$");


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

            String apiCode = first.getApiCode();
            String sceneCode = first.getSceneCode();

            AutoCheckConfigVO vo = new AutoCheckConfigVO();
            vo.setApiCode(apiCode);
            vo.setName(Optional.ofNullable(apiCodeInfoMap.get(apiCode)).map(MarketingCustomerVO::getName).orElse(""));
            vo.setSceneCode(sceneCode);
            vo.setSceneName(Optional.ofNullable(sceneMap.get(sceneCode)).map(AutoCheckSceneVO::getSceneName).orElse(""));
            List<TableNameAndFieldVO> tableNameAndFieldList = new ArrayList<>();
            for (AutoCheckConfig config : group) {
                TableNameAndFieldVO tableNameAndFieldVO = new TableNameAndFieldVO();
                tableNameAndFieldVO.setTableName(config.getTableName());
                tableNameAndFieldVO.setFieldNames(config.getFieldName());
                tableNameAndFieldList.add(tableNameAndFieldVO);
            }
            vo.setTableNameAndFieldList(tableNameAndFieldList);
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
        List<TableNameAndFieldVO> tableNameAndFieldList = dto.getTableNameAndFieldList();
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
        for (TableNameAndFieldVO tableNameAndField : tableNameAndFieldList) {
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
//        // 1、获取所有配置
//        List<String> apiCodeList = handleApiCodeParam(null);
//        List<String> sceneCodeList = handleSceneCodeParam(null);

        List<AutoCheckConfig> configList = autoCheckConfigMapper.
                selectByApiCodesAndSceneCodes(null, null);
        String today = DateUtil.today();
        List<AutoCheckResultLog> saveList = new ArrayList<>();
        // 生成这一次对比的批次号，方便查看巡检结果时数据聚合
        String batchId = DateUtil.format(new Date(), "yyyyMMddHHmmss");
        String compareTime = DateUtil.formatDateTime(new Date());
        // 获取今天已经比对过的id
        Map<String, List<Long>> comparedIdMap = getTodayExistComparedId(today);

        for (AutoCheckConfig config : configList) {
            /**
             * 针对每一条配置 apiCode、sceneCode、tableName，进行巡检
             * 1、是否有当天最新的数据
             * 2、是否有昨天的八点的数据
             * 3、是否对比过
             */
            String apiCode = StringUtils.defaultString(config.getApiCode()).trim();
            String sceneCode = StringUtils.defaultString(config.getSceneCode()).trim();
            String tableName = StringUtils.defaultString(config.getTableName()).trim();
            String fieldName = StringUtils.defaultString(config.getFieldName()).trim();

            List<String> compareFields = parseFieldNames(fieldName);

            // 数据校验
            if (!checkConfig(apiCode, sceneCode, tableName, fieldName, compareFields)) {
                continue;
            }

            String safeTableSql = quoteIdentifier(tableName);
            String selectColumnsSql = buildSelectColumnsSql(compareFields);

            Map<String, Object> lastDay8;
            Map<String, Object> latest;
            try {
                String ymd = DateUtil.format(DateUtil.yesterday(), "yyyy-MM-dd");
                String startTime = ymd + " 08:00:00";
                String endTime = ymd + " 08:10:00";
                lastDay8 = autoCheckDynamicDataMapper.selectLastDay8(safeTableSql, selectColumnsSql, startTime, endTime, apiCode);
                latest = autoCheckDynamicDataMapper.selectLatestToday(safeTableSql, selectColumnsSql, today, apiCode);
            } catch (Exception ex) {
                if (isTableNotExist(ex, tableName)) {
                    log.warn("QA自动化巡检-动态查表：跳过表不存在，apiCode={}, sceneCode={}, tableName={}", apiCode, sceneCode, tableName);
                    continue;
                }
                // 字段不存在（例如表里无 create_time / api_code / id）：仅跳过该配置，不影响其他配置
                if (isColumnNotExist(ex, "create_time") || isColumnNotExist(ex, "api_code") || isColumnNotExist(ex, "id")) {
                    log.warn("QA自动化巡检-动态查表：跳过字段不存在，apiCode={}, sceneCode={}, tableName={}, msg={}",
                            apiCode, sceneCode, tableName, ex.getMessage());
                    continue;
                }
                // 其他异常继续抛出，便于尽快暴露配置/SQL问题
                throw ex;
            }

            if (!checkData(lastDay8, latest, apiCode, sceneCode, tableName, comparedIdMap)) {
                continue;
            }

            Long todayDataId = getLong(latest.get("id"));

            // 仅比较配置的字段（不包含 id/create_time）
            boolean same = isAllFieldsSame(compareFields, lastDay8, latest);

            AutoCheckResultLog row = new AutoCheckResultLog();
            row.setApiCode(apiCode);
            row.setSceneCode(sceneCode);
            row.setTableName(tableName);
            row.setCompareTime(compareTime);
            row.setTodayDataId(todayDataId);
            row.setLastData(toJsonExcludeSafe(filterToCompareFields(lastDay8, compareFields), "id", "create_time"));
            row.setTodayData(toJsonExcludeSafe(filterToCompareFields(latest, compareFields), "id", "create_time"));
            row.setResult(same ? COMPARE_RESULT_SAME : COMPARE_RESULT_DIFFERENT);
            row.setBatchId(batchId);
            Date now = new Date();
            row.setCreateTime(now);
            row.setUpdateTime(now);
            saveList.add(row);
        }

        if (CollUtil.isNotEmpty(saveList)) {
            autoCheckResultLogMapper.batchInsert(saveList);
        }
    }

    private boolean checkConfig(String apiCode, String sceneCode, String tableName,
                                String fieldName, List<String> compareFields) {
        if (StringUtils.isBlank(apiCode) || StringUtils.isBlank(sceneCode)
                || StringUtils.isBlank(tableName) || StringUtils.isBlank(fieldName)) {
            log.warn("QA自动化巡检-动态查表：跳过无效配置，apiCode={}, sceneCode={}, tableName={}, fieldName={}",
                    apiCode, sceneCode, tableName, fieldName);
            return false;
        }
        if (!isSafeIdentifier(tableName)) {
            log.warn("QA自动化巡检-动态查表：跳过不安全表名，apiCode={}, sceneCode={}, tableName={}",
                    apiCode, sceneCode, tableName);
            return false;
        }

        if (CollUtil.isEmpty(compareFields)) {
            log.warn("QA自动化巡检-动态查表：跳过空字段配置，apiCode={}, sceneCode={}, tableName={}, fieldName={}",
                    apiCode, sceneCode, tableName, fieldName);
            return false;
        }
        // 字段名白名单校验
        boolean unsafeField = compareFields.stream().anyMatch(f -> !isSafeIdentifier(f));
        if (unsafeField) {
            log.warn("QA自动化巡检-动态查表：跳过不安全字段名，apiCode={}, sceneCode={}, tableName={}, fieldName={}",
                    apiCode, sceneCode, tableName, fieldName);
            return false;
        }
        return true;
    }

    private boolean checkData(Map<String, Object> lastDay8,
                              Map<String, Object> latest,
                              String apiCode,
                              String sceneCode,
                              String tableName,
                              Map<String, List<Long>> comparedIdMap) {
        if (lastDay8 == null || latest == null || lastDay8.isEmpty() || latest.isEmpty()) {
            log.warn("QA自动化巡检-动态查表：跳过，数据不存在，apiCode={}, sceneCode={}, tableName={}", apiCode, sceneCode, tableName);
            return false;
        }

        Long todayDataId = getLong(latest.get("id"));
        String key = buildKey(apiCode, sceneCode);
        List<Long> existIds = comparedIdMap.get(key);
        if (CollUtil.isNotEmpty(existIds) && existIds.contains(todayDataId)) {
            log.warn("QA自动化巡检-动态查表：跳过，数据已存在，apiCode={}, sceneCode={}, tableName={}, id={}",
                    apiCode, sceneCode, tableName, todayDataId);
            return false;
        }

        return true;
    }

    private Map<String, List<Long>> getTodayExistComparedId(String today) {
        String todayStartTime = today + " 00:00:00";
        String todayEndTime = today + " 23:59:59";
        List<AutoCheckResultLog> resultList = autoCheckResultLogMapper
                .selectByCodeListAndTime(todayStartTime, todayEndTime, null, null);
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
        return comparedIdMap;
    }

    @Override
    public List<AutoCheckResultVO> getResultList(String apiCodes, String sceneCodes, String startTime, String endTime) {
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

        List<AutoCheckResultLog> resultList = autoCheckResultLogMapper
                .selectByCodeListAndTime(startTime, endTime, apiCodeList, sceneCodeList);

        if (CollUtil.isEmpty(resultList)) {
            return result;
        }
        // 表字典：tableName -> tableDesc（用于明细展示）
        Map<String, String> tableDescMap = getTableDescMap(resultList);

        // 按 apiCode + sceneCode 聚合
        Map<String, List<AutoCheckResultLog>> groupMap = resultList.stream()
                .collect(Collectors.groupingBy(
                        r -> buildKey(r.getApiCode().trim(), r.getSceneCode().trim()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        // 组装resultVO
        for (Map.Entry<String, List<AutoCheckResultLog>> entry : groupMap.entrySet()) {
            List<AutoCheckResultLog> apiSceneGroup = entry.getValue();
            if (CollUtil.isEmpty(apiSceneGroup)) {
                continue;
            }
            // 同一 apiCode + sceneCode 下，再按 batchId 聚合
            Map<String, List<AutoCheckResultLog>> batchGroupMap = apiSceneGroup.stream()
                    .collect(Collectors.groupingBy(
                            r -> StringUtils.defaultString(r.getBatchId()).trim(),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            for (Map.Entry<String, List<AutoCheckResultLog>> batchEntry : batchGroupMap.entrySet()) {
                List<AutoCheckResultLog> group = batchEntry.getValue();
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
                String oldestTime = null;
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

                    String time = row.getCompareTime().trim();
                    detail.setTime(time);

                    // oldestTime（compare_time 通常为 yyyy-MM-dd HH:mm:ss，字典序=时间序）
                    if (StringUtils.isNotBlank(time) && (oldestTime == null || time.compareTo(oldestTime) > 0)) {
                        oldestTime = time;
                    }
                    // 聚合 compareResult：非“一致”都视为不一致（兼容后续新增结果值）
                    if (!COMPARE_RESULT_SAME.equals(detail.getCompareResult())) {
                        allSame = false;
                    }
                    detailList.add(detail);
                }

                vo.setTime(StringUtils.defaultString(oldestTime));
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

    private Map<String, String> getTableDescMap(List<AutoCheckResultLog> resultList) {
        Map<String, String> tableDescMap = new HashMap<>();
        if (CollUtil.isEmpty(resultList)) {
            return tableDescMap;
        }
        // 表字典：tableName -> tableDesc（用于明细展示）
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
        return tableDescMap;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutoCheckDictInitResultVO initSceneDictBatch(BatchInitAutoCheckSceneDictDto dto) {
        AutoCheckDictInitResultVO res = new AutoCheckDictInitResultVO();
        res.setTotal(0);
        res.setInserted(0);
        res.setUpdated(0);
        res.setSkipped(0);

        if (dto == null || CollUtil.isEmpty(dto.getSceneList())) {
            return res;
        }

        // 按 sceneCode 去重（保留最后一次）
        Map<String, AutoCheckSceneDictInitDto> inputMap = new LinkedHashMap<>();
        for (AutoCheckSceneDictInitDto item : dto.getSceneList()) {
            if (item == null || StringUtils.isBlank(item.getSceneCode()) || StringUtils.isBlank(item.getSceneName())) {
                continue;
            }
            String code = item.getSceneCode().trim();
            String name = item.getSceneName().trim();
            if (StringUtils.isBlank(code) || StringUtils.isBlank(name)) {
                continue;
            }
            AutoCheckSceneDictInitDto normalized = new AutoCheckSceneDictInitDto();
            normalized.setSceneCode(code);
            normalized.setSceneName(name);
            inputMap.put(code, normalized);
        }

        if (inputMap.isEmpty()) {
            return res;
        }

        List<String> sceneCodes = new ArrayList<>(inputMap.keySet());
        res.setTotal(sceneCodes.size());

        // 查库（包含已删除记录），便于“恢复” is_deleted=0
        AutoCheckSceneDictExample example = new AutoCheckSceneDictExample();
        example.createCriteria().andSceneCodeIn(sceneCodes);
        List<AutoCheckSceneDict> existingList = autoCheckSceneDictMapper.selectByExample(example);
        Map<String, AutoCheckSceneDict> existingMap = existingList.stream()
                .filter(Objects::nonNull)
                .filter(e -> StringUtils.isNotBlank(e.getSceneCode()))
                .collect(Collectors.toMap(
                        e -> e.getSceneCode().trim(),
                        e -> e,
                        (a, b) -> a
                ));

        Date now = new Date();
        int updated = 0;
        int skipped = 0;
        List<AutoCheckSceneDict> insertList = new ArrayList<>();

        for (String code : sceneCodes) {
            AutoCheckSceneDictInitDto item = inputMap.get(code);
            if (item == null) {
                continue;
            }

            AutoCheckSceneDict exist = existingMap.get(code);
            if (exist == null || exist.getId() == null) {
                AutoCheckSceneDict row = new AutoCheckSceneDict();
                row.setSceneCode(code);
                row.setSceneName(item.getSceneName());
                row.setIsDeleted((byte) 0);
                row.setCreateTime(now);
                row.setUpdateTime(now);
                insertList.add(row);
                continue;
            }

            String existName = StringUtils.defaultString(exist.getSceneName()).trim();
            boolean needUpdate = false;
            AutoCheckSceneDict updateRow = new AutoCheckSceneDict();
            updateRow.setId(exist.getId());

            if (!Objects.equals(existName, item.getSceneName())) {
                updateRow.setSceneName(item.getSceneName());
                needUpdate = true;
            }
            if (exist.getIsDeleted() != null && exist.getIsDeleted() == 1) {
                updateRow.setIsDeleted((byte) 0);
                needUpdate = true;
            }

            if (needUpdate) {
                updateRow.setUpdateTime(now);
                autoCheckSceneDictMapper.updateByPrimaryKeySelective(updateRow);
                updated++;
            } else {
                skipped++;
            }
        }

        if (CollUtil.isNotEmpty(insertList)) {
            autoCheckSceneDictMapper.batchInsert(insertList);
        }

        res.setInserted(insertList.size());
        res.setUpdated(updated);
        res.setSkipped(skipped);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutoCheckDictInitResultVO initTableDictBatch(BatchInitAutoCheckTableDictDto dto) {
        AutoCheckDictInitResultVO res = new AutoCheckDictInitResultVO();
        res.setTotal(0);
        res.setInserted(0);
        res.setUpdated(0);
        res.setSkipped(0);

        if (dto == null || CollUtil.isEmpty(dto.getTableList())) {
            return res;
        }

        // 按 tableName 去重（保留最后一次）
        Map<String, AutoCheckTableDictInitDto> inputMap = new LinkedHashMap<>();
        for (AutoCheckTableDictInitDto item : dto.getTableList()) {
            if (item == null || StringUtils.isBlank(item.getTableName()) || StringUtils.isBlank(item.getTableDesc())) {
                continue;
            }
            String name = item.getTableName().trim();
            String desc = item.getTableDesc().trim();
            if (StringUtils.isBlank(name) || StringUtils.isBlank(desc)) {
                continue;
            }
            AutoCheckTableDictInitDto normalized = new AutoCheckTableDictInitDto();
            normalized.setTableName(name);
            normalized.setTableDesc(desc);
            inputMap.put(name, normalized);
        }

        if (inputMap.isEmpty()) {
            return res;
        }

        List<String> tableNames = new ArrayList<>(inputMap.keySet());
        res.setTotal(tableNames.size());

        // 查库（包含已删除记录），便于“恢复” is_deleted=0
        AutoCheckTableDictExample example = new AutoCheckTableDictExample();
        example.createCriteria().andTableNameIn(tableNames);
        List<AutoCheckTableDict> existingList = autoCheckTableDictMapper.selectByExample(example);
        Map<String, AutoCheckTableDict> existingMap = existingList.stream()
                .filter(Objects::nonNull)
                .filter(e -> StringUtils.isNotBlank(e.getTableName()))
                .collect(Collectors.toMap(
                        e -> e.getTableName().trim(),
                        e -> e,
                        (a, b) -> a
                ));

        Date now = new Date();
        int updated = 0;
        int skipped = 0;
        List<AutoCheckTableDict> insertList = new ArrayList<>();

        for (String tableName : tableNames) {
            AutoCheckTableDictInitDto item = inputMap.get(tableName);
            if (item == null) {
                continue;
            }

            AutoCheckTableDict exist = existingMap.get(tableName);
            if (exist == null || exist.getId() == null) {
                AutoCheckTableDict row = new AutoCheckTableDict();
                row.setTableName(tableName);
                row.setTableDesc(item.getTableDesc());
                row.setIsDeleted((byte) 0);
                row.setCreateTime(now);
                row.setUpdateTime(now);
                insertList.add(row);
                continue;
            }

            String existDesc = StringUtils.defaultString(exist.getTableDesc()).trim();
            boolean needUpdate = false;
            AutoCheckTableDict updateRow = new AutoCheckTableDict();
            updateRow.setId(exist.getId());

            if (!Objects.equals(existDesc, item.getTableDesc())) {
                updateRow.setTableDesc(item.getTableDesc());
                needUpdate = true;
            }
            if (exist.getIsDeleted() != null && exist.getIsDeleted() == 1) {
                updateRow.setIsDeleted((byte) 0);
                needUpdate = true;
            }

            if (needUpdate) {
                updateRow.setUpdateTime(now);
                autoCheckTableDictMapper.updateByPrimaryKeySelective(updateRow);
                updated++;
            } else {
                skipped++;
            }
        }

        if (CollUtil.isNotEmpty(insertList)) {
            autoCheckTableDictMapper.batchInsert(insertList);
        }

        res.setInserted(insertList.size());
        res.setUpdated(updated);
        res.setSkipped(skipped);
        return res;
    }

    private String toJsonExcludeSafe(Object obj, String... excludeFields) {
        return JsonFilterUtil.toJsonExcludeSafe(obj, excludeFields);
    }

    private String buildKey(String apiCode, String sceneCode) {
        return apiCode + "_" + sceneCode;
    }

    private boolean isSafeIdentifier(String identifier) {
        if (StringUtils.isBlank(identifier)) {
            return false;
        }
        return SAFE_IDENTIFIER.matcher(identifier.trim()).matches();
    }

    private String quoteIdentifier(String identifier) {
        // identifier 已校验，仅做反引号包裹
        return "`" + identifier.trim() + "`";
    }

    private List<String> parseFieldNames(String fieldNames) {
        if (StringUtils.isBlank(fieldNames)) {
            return Collections.emptyList();
        }
        return Arrays.stream(fieldNames.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 构造 selectColumns SQL 片段（用于 ${selectColumns}）。
     * <p>强制包含 id 与 snap_time（用于去重与定位时间）。</p>
     */
    private String buildSelectColumnsSql(List<String> compareFields) {
        LinkedHashSet<String> cols = new LinkedHashSet<>();
        cols.add("id");
        // 输出为字符串，避免 JDBC 返回 Date/Time 导致序列化不一致
        cols.add("DATE_FORMAT(create_time,'%Y-%m-%d %H:%i:%s') AS create_time");
        for (String f : compareFields) {
            if (StringUtils.isBlank(f)) {
                continue;
            }
            String c = f.trim();
            if ("id".equalsIgnoreCase(c) || "create_time".equalsIgnoreCase(c)) {
                continue;
            }
            cols.add(quoteIdentifier(c));
        }
        return String.join(", ", cols);
    }

    private boolean isTableNotExist(Throwable ex, String tableName) {
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

    /**
     * 判断是否为“字段不存在”的异常（MySQL error code: 1054）。
     */
    private boolean isColumnNotExist(Throwable ex, String columnName) {
        if (StringUtils.isBlank(columnName)) {
            return false;
        }
        Throwable t = ex;
        while (t != null) {
            if (t instanceof SQLException) {
                SQLException sqlEx = (SQLException) t;
                if (sqlEx.getErrorCode() == 1054) {
                    return true;
                }
            }
            String msg = t.getMessage();
            if (StringUtils.isNotBlank(msg)
                    && msg.contains("Unknown column")
                    && msg.contains("'" + columnName + "'")) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private Long getLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v).trim());
        } catch (Exception ignore) {
            return null;
        }
    }

    private Map<String, Object> filterToCompareFields(Map<String, Object> src, List<String> compareFields) {
        if (src == null || src.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (String f : compareFields) {
            if (StringUtils.isBlank(f)) {
                continue;
            }
            String key = f.trim();
            out.put(key, src.get(key));
        }
        return out;
    }

    private boolean isAllFieldsSame(List<String> compareFields, Map<String, Object> last, Map<String, Object> today) {
        for (String f : compareFields) {
            if (StringUtils.isBlank(f)) {
                continue;
            }
            String key = f.trim();
            if ("id".equalsIgnoreCase(key) || "create_time".equalsIgnoreCase(key)) {
                continue;
            }
            Object a = last.get(key);
            Object b = today.get(key);
            if (!Objects.equals(a, b)) {
                // 兼容 JDBC 返回类型不一致（如 BigDecimal vs Long）：统一按字符串比对一次
                String as = a == null ? null : String.valueOf(a);
                String bs = b == null ? null : String.valueOf(b);
                if (!Objects.equals(as, bs)) {
                    return false;
                }
            }
        }
        return true;
    }
}
