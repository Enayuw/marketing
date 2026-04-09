package com.br.marketing.service.tccpa.impl;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.config.biz.TcyrCpaConfigManager;
import com.br.marketing.dto.tccpa.TcCpaDeleteRuleExecuteInfoDTO;
import com.br.marketing.dto.tccpa.TcCpaDeleteRuleVolumeItemDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaDeleteRuleSourceTypeEnum;
import com.br.marketing.mapper.TcyrCpaCommonMapper;
import com.br.marketing.mapper.TcyrCpaDeleteRuleMapper;
import com.br.marketing.service.tccpa.TcCpaCommonService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TcCpaCommonServiceImpl implements TcCpaCommonService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private TcyrCpaCommonMapper tcyrCpaCommonMapper;

    @Resource
    private TcyrCpaDeleteRuleMapper tcyrCpaDeleteRuleMapper;

    @Resource
    TcyrCpaConfigManager tcyrCpaConfigManager;

    @Override
    public void updateVolumeByTask(TcyrCpaCollidingTask collidingTask) throws IOException {
        String joinFrag = getDeleteSqlFrag(collidingTask.getDeleteRuleIds());
        List<Long> packageIds = com.br.marketing.common.utils.StringUtils
                .StrsConvertLongs(collidingTask.getPackageIds());
        String packageIdStr = com.br.marketing.common.utils.StringUtils.join(packageIds);
        //1.数据包预估
        String packageEstSql = "select count(distinct pck.user_key) from b_tcyr_cpa_colliding_data pck "
                .concat(" where pck.is_del = 1")
                .concat(" and pck.package_id in " + packageIdStr);
        //数据包全量
        int packageEstNum = tcyrCpaCommonMapper.magnitudeQuerytiflash_(packageEstSql);
        String packageEstWithDelSql = "select count(distinct pck.user_key) from b_tcyr_cpa_colliding_data pck "
                .concat(joinFrag)
                .concat(" and pck.package_id in " + packageIdStr);
        //数据包预估
        int packageEstWithDelNum = tcyrCpaCommonMapper.magnitudeQuerytiflash_(packageEstWithDelSql);
        // 1.1 按单规则统计数据包剔除明细
        // 仅启用该规则时：deleteNum = packageEstNum − 应用该规则后的量级
        collidingTask.setDeleteInfo(
                buildPackageDeleteInfoJson(packageEstNum, packageIdStr, collidingTask.getDeleteRuleIds()));
        //2.补充包预估
        //补充包全量
        int supplyEstNum = 0;
        //补充包预估
        int supplyEstNumWithDel = 0;

        //failMsg与lockBelong的映射Map
        Map<Integer, Integer> failMsgToLbMap = tcyrCpaConfigManager.getFailMsgToBlMap();
        if (StringUtils.isNotEmpty(collidingTask.getSupplyRuleInfo())) {
            List<TcyrSupplyRuleInfo> supplyRuleInfos =
                    objectMapper.readValue(collidingTask.getSupplyRuleInfo(),
                            new TypeReference<List<TcyrSupplyRuleInfo>>() {
                            });
            for (TcyrSupplyRuleInfo ruleInfo : supplyRuleInfos) {
                if (failMsgToLbMap.containsKey(ruleInfo.getFailMsg())) {
                    Integer lockBelong = failMsgToLbMap.get(ruleInfo.getFailMsg());
                    String lockEstSql = "select count(distinct pck.user_key) from b_tcyr_cpa_lock_data pck "
                            .concat(" where pck.is_del = 1")
                            .concat(" and pck.lock_belong = " + lockBelong)
                            .concat(" and date(pck.release_time) in " + ruleInfo.join());
                    supplyEstNum += tcyrCpaCommonMapper.magnitudeQuerytiflash_(lockEstSql);
                    String lockWithDelEstSql = "select count(distinct pck.user_key) from b_tcyr_cpa_lock_data pck "
                            .concat(joinFrag)
                            .concat(" and pck.lock_belong = " + lockBelong)
                            .concat(" and date(pck.release_time) in " + ruleInfo.join());
                    supplyEstNumWithDel += tcyrCpaCommonMapper.magnitudeQuerytiflash_(lockWithDelEstSql);
                } else {
                    String lockEstSql = "select count(distinct pck.user_key) from b_tcyr_cpa_invalue_data pck "
                            .concat(" where pck.is_del = 1")
                            .concat(" and pck.fail_msg = " + ruleInfo.getFailMsg())
                            .concat(" and date(pck.release_time) in " + ruleInfo.join());
                    supplyEstNum += tcyrCpaCommonMapper.magnitudeQuerytiflash_(lockEstSql);
                    String lockWithDelEstSql = "select count(distinct pck.user_key) from b_tcyr_cpa_invalue_data pck "
                            .concat(joinFrag)
                            .concat(" and pck.fail_msg = " + ruleInfo.getFailMsg())
                            .concat(" and date(pck.release_time) in " + ruleInfo.join());
                    supplyEstNumWithDel += tcyrCpaCommonMapper.magnitudeQuerytiflash_(lockWithDelEstSql);
                }
            }
        }
        int estNum = packageEstNum + supplyEstNum;
        int estWithDelNum = packageEstWithDelNum + supplyEstNumWithDel;
        collidingTask.setEstNum(estWithDelNum);
        collidingTask.setSupplyNum(supplyEstNumWithDel);
        collidingTask.setDeleteNum(estNum - estWithDelNum);
    }

    /**
     * 数据包维度：对每个剔除规则单独生成 {@link #getDeleteSqlFrag(String)}，
     * 计算「仅该规则」下的剔除量级，序列化为 JSON 写入 deleteInfo。
     */
    private String buildPackageDeleteInfoJson(int packageEstNum, String packageIdStr, String deleteRuleIdsStr)
            throws IOException {
        List<Long> ruleIds = com.br.marketing.common.utils.StringUtils.StrsConvertLongs(deleteRuleIdsStr);
        if (CollectionUtils.isEmpty(ruleIds)) {
            return null;
        }
        Map<Long, String> idToName = loadDeleteRuleNames(ruleIds);
        List<TcCpaDeleteRuleVolumeItemDTO> items = new ArrayList<>();
        for (Long ruleId : ruleIds) {
            String singleFrag = getDeleteSqlFrag(String.valueOf(ruleId));
            int withDelSingle = countPackageEstWithDeleteFrag(singleFrag, packageIdStr);
            int deleteNumForRule = Math.max(0, packageEstNum - withDelSingle);
            items.add(new TcCpaDeleteRuleVolumeItemDTO(
                    ruleId,
                    idToName.getOrDefault(ruleId, ""),
                    deleteNumForRule));
        }
        return objectMapper.writeValueAsString(items);
    }

    private Map<Long, String> loadDeleteRuleNames(List<Long> ruleIds) {
        TcyrCpaDeleteRuleExample example = new TcyrCpaDeleteRuleExample();
        example.createCriteria().andIdIn(ruleIds);
        return tcyrCpaDeleteRuleMapper.selectByExample(example).stream()
                .collect(Collectors.toMap(
                        TcyrCpaDeleteRule::getId,
                        r -> StringUtils.defaultString(r.getRuleName()),
                        (a, b) -> a));
    }

    private int countPackageEstWithDeleteFrag(String joinFrag, String packageIdStr) {
        String sql = "select count(distinct pck.user_key) from b_tcyr_cpa_colliding_data pck "
                .concat(joinFrag)
                .concat(" and pck.package_id in " + packageIdStr);
        return tcyrCpaCommonMapper.magnitudeQuerytiflash_(sql);
    }

    @Override
    public Integer calculateVolume(List<TcCpaDeleteRuleExecuteInfoDTO> executeInfoDTOS) {
        List<String> scripts = executeInfoDTOS.stream()
                .collect(Collectors.groupingBy(TcCpaDeleteRuleExecuteInfoDTO::getSourceType))
                .entrySet().stream().flatMap(this::generateSqlBySourceType)
                .collect(Collectors.toList());
        return executeScripts(scripts);
    }

    /**
     * 根据数据源类型生成对应的SQL查询脚本
     */
    private Stream<String> generateSqlBySourceType(Map.Entry<Integer, List<TcCpaDeleteRuleExecuteInfoDTO>> entry) {
        TcCpaDeleteRuleSourceTypeEnum sourceType = TcCpaDeleteRuleSourceTypeEnum.getByValue(entry.getKey());
        List<TcCpaDeleteRuleExecuteInfoDTO> executeInfoDTOs = entry.getValue();
        switch (Objects.requireNonNull(sourceType)) {
            case LOCK_DATA:
            case INVALUE_DATA:
                return generateLockOrInvalueQuery(sourceType, executeInfoDTOs);
            case BLANK_DATA:
                return generateBlankDataQuery(sourceType);
            case CUSTOMIZE:
                return generateCustomizeQuery(executeInfoDTOs);
            default:
                return Stream.empty();
        }
    }

    private Stream<String> generateLockOrInvalueQuery(TcCpaDeleteRuleSourceTypeEnum sourceType,
                                                      List<TcCpaDeleteRuleExecuteInfoDTO> executeInfoDTOs) {
        String inValues = executeInfoDTOs.stream()
                .flatMap(dto -> dto.getValue().stream())
                .distinct()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        String query = String.format("SELECT %s FROM %s WHERE %s IN (%s) AND %s",
                sourceType.getSelect(), sourceType.getTableName(), sourceType.getField(),
                inValues, sourceType.getDefaultCondition());
        return Stream.of(query);
    }

    private Stream<String> generateBlankDataQuery(TcCpaDeleteRuleSourceTypeEnum sourceType) {
        String query = String.format("SELECT %s FROM %s WHERE is_del = 1",
                sourceType.getSelect(),
                sourceType.getTableName());
        return Stream.of(query);
    }

    private Stream<String> generateCustomizeQuery(List<TcCpaDeleteRuleExecuteInfoDTO> executeInfoDTOs) {
        return executeInfoDTOs.stream().map(dto ->
                String.format("SELECT %s AS user_key FROM %s WHERE %s",
                        dto.getMappingField(),
                        dto.getTableName(),
                        dto.getCondition()));
    }

    private Integer executeScripts(List<String> scripts) {
        return scripts.size() == 1 ?
                tcyrCpaCommonMapper.calculateDeleteNumByScript(scripts.get(0)) :
                tcyrCpaCommonMapper.executeUnionQueriestikv_(scripts);
    }

    @Override
    public String getDeleteSqlFrag(String deleteRuleIdStr) throws IOException {
        List<Long> deleteRuleIds = com.br.marketing.common.utils.StringUtils.StrsConvertLongs(deleteRuleIdStr);
        String joinFrag = "";
        String whereFrag = " where pck.is_del = 1";
        if (CollectionUtils.isEmpty(deleteRuleIds)) {
            return whereFrag;
        }
        //1.获取剔除规则
        TcyrCpaDeleteRuleExample example = new TcyrCpaDeleteRuleExample();
        example.createCriteria()
                .andIdIn(deleteRuleIds)
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andEnabledEqualTo(Constants.ENABLED_ACT);
        List<TcyrCpaDeleteRule> deleteRules = tcyrCpaDeleteRuleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(deleteRules)) {
            return whereFrag;
        }
        //2.将剔除规则中的信息，结构化
        List<TcCpaDeleteRuleExecuteInfoDTO> infos = new ArrayList<>();
        for (TcyrCpaDeleteRule deleteRule : deleteRules) {
            String executeInfo = deleteRule.getExecuteInfo();
            if (com.br.marketing.common.utils.StringUtils.isBlank(executeInfo)) {
                continue;
            }
            List<TcCpaDeleteRuleExecuteInfoDTO> ruleInfos = objectMapper.readValue(
                    executeInfo,
                    new TypeReference<List<TcCpaDeleteRuleExecuteInfoDTO>>() {
                    }
            );
            infos.addAll(ruleInfos);
        }
        //3.生成sql片段
        Map<Integer, TcCpaDeleteRuleExecuteInfoDTO> commonInfos = new HashMap<>();
        for (TcCpaDeleteRuleExecuteInfoDTO info : infos) {
            //定制的剔除规则，在循环中就可以生成sql片段
            if (Objects.equals(info.getSourceType(), TcCpaDeleteRuleSourceTypeEnum.CUSTOMIZE.getValue())) {
                joinFrag = joinFrag.concat(" left join " + info.getTableName() +
                        " on " + info.getMappingField() + " = pck.user_key" + " and " + info.getCondition());
                whereFrag = whereFrag.concat(" and " + info.getMappingField() + " is null");
            } else {
                //通用的剔除规则，相同的sourceType的规则，value值需要做汇总去重
                TcCpaDeleteRuleExecuteInfoDTO updInfo =
                        commonInfos.computeIfAbsent(info.getSourceType(), k -> info);
                updInfo.addValue(info.getValue());
            }
        }
        //通用的剔除规则，生成sql片段
        for (TcCpaDeleteRuleExecuteInfoDTO info : commonInfos.values()) {
            TcCpaDeleteRuleSourceTypeEnum sourceTypeEnum = TcCpaDeleteRuleSourceTypeEnum.getByValue(info.getSourceType());
            joinFrag = joinFrag.concat(" left join " + sourceTypeEnum.getTableName() +
                    " on " + sourceTypeEnum.getSelect() + " = pck.user_key" +
                    " and " + sourceTypeEnum.getDefaultCondition());
            //lock
            if (!Objects.equals(info.getSourceType(), TcCpaDeleteRuleSourceTypeEnum.BLANK_DATA.getValue())) {
                joinFrag = joinFrag.concat(" and " + sourceTypeEnum.getField() + " in " + info.join());
            }
            whereFrag = whereFrag.concat(" and " + sourceTypeEnum.getSelect() + " is null");
        }
        return joinFrag + whereFrag;
    }
}
