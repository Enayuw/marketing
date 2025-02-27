package com.br.marketing.service.mark.Impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.DataMarkConfig;
import com.br.marketing.entity.DataMarkConfigExample;
import com.br.marketing.entity.FlagData;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.enums.DataMarkEnum;
import com.br.marketing.mapper.DataMarkConfigMapper;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.service.mark.PpRonShuMarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description pp榕树打标实现
 * @Author hong.chen
 * @Date 2025/2/19 18:16
 */
@Service
@Slf4j
public class PpRongShuMarkServiceImpl implements PpRonShuMarkService {
    @Autowired
    RedisChgService redisChgService;
    @Resource
    FlagDataMapper flagDataMapper;
    @Autowired
    DataCleaningAutoService cleaningAutoService;
    @Autowired
    MarketingCleanDataTaskMapper marketingCleanDataTaskMapper;
    @Resource
    LocalFileMapper localFileMapper;

    @Override
    public Result<Boolean> createCleanTask(Long localId) {
        LocalFile localFile = localFileMapper.getByPrimaryKey(localId);
        String apiCode = localFile.getApiCode();
        // 创建任务id
        Long taskId = cleaningAutoService.saveCleanTask(apiCode, 0, "pp榕树打标_上传清洗规则勿动");

        // 更新打标表任务id
        while (true) {
            int count = 0;
            try {
                count = flagDataMapper.updateTaskIdByLocalId(localId, taskId);
            } catch (Exception e) {
                String subject = "pp榕树更新打标表taskId异常,localFIleId:" + localFile.getId();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(), e.getMessage()
                        , subject), e);
            }
            if (count == 0) {
                break;
            }
        }

        // 更新数据清洗任务表状态为待清洗
        updateTaskCleanStatusById(taskId);
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    private void updateTaskCleanStatusById(Long taskId) {
        MarketingCleanDataTask cleanDataTask = new MarketingCleanDataTask();
        cleanDataTask.setId(taskId);
        cleanDataTask.setCleanStatus(0);
        marketingCleanDataTaskMapper.updateByPrimaryKeySelective(cleanDataTask);
    }

    @Override
    public void markAndUpdateFlagStatus(List<FlagData> flagData, String apiCode, List<DataMarkConfig> dataMarkConfigs) {
        // 打标表cell
        List<String> flagDataCells = flagData.stream().map(FlagData::getCellMd5).collect(Collectors.toList());
        // 基底表数据
        List<FlagData> orgDataByCellbI = flagDataMapper.queryOdsOrgDataByCellbI_(flagDataCells);
//        if (CollectionUtils.isEmpty(orgDataByCellbI)) {
//            return;
//        }
        List<DataMarkConfig> dataMarkConfigByRiskGroup =
                dataMarkConfigs.stream().filter(t -> t.getMarkType().equals(DataMarkEnum.MARK_RISKGROUP.getMarkType())).collect(Collectors.toList());
        // 更新客群标签
        handleRiskGroup(flagData, orgDataByCellbI, dataMarkConfigByRiskGroup);

        List<DataMarkConfig> dataMarkConfigByInterest =
                dataMarkConfigs.stream().filter(t -> t.getMarkType().equals(DataMarkEnum.MARK_INTEREST.getMarkType())).collect(Collectors.toList());
        // 更新利率标签
        handleInterest(flagData, orgDataByCellbI, dataMarkConfigByInterest);
    }

    private void handleInterest(List<FlagData> flagData, List<FlagData> orgDataByCellbI, List<DataMarkConfig> dataMarkConfigByInterest) {
        Map<Integer, List<DataMarkConfig>> configMap =
                dataMarkConfigByInterest.stream().collect(Collectors.groupingBy(DataMarkConfig::getMarkOutValueType));
        // 匹配到利率标签的配置
        Map<String, List<DataMarkConfig>> conditionMap = configMap.get(0).stream().collect(Collectors.groupingBy(DataMarkConfig::getMarkCondition));
        String otherInterestConfig = configMap.get(1).get(0).getMarkOutValue();

        // 匹配到利率标签的基底表数据
        List<String> matchedCellList = new ArrayList<>();
        for (String condition : conditionMap.keySet()) {
            List<DataMarkConfig> dataMarkConfigs = conditionMap.get(condition);
            String markOutValue = dataMarkConfigs.get(0).getMarkOutValue();
            List<String> matchedOrgData =
                    orgDataByCellbI.stream().filter(t -> isMatch(t, condition)).map(FlagData::getCellMd5).collect(Collectors.toList());
            List<FlagData> flagDataByApiCode = flagData.stream().filter(t -> matchedOrgData.contains(t.getCellMd5())).collect(Collectors.toList());
            matchedCellList.addAll(matchedOrgData);
            if (CollectionUtils.isEmpty(flagDataByApiCode)) {
                continue;
            }
            flagDataMapper.batchUpdateInterestFlagById(flagDataByApiCode, 1, markOutValue);
        }

        // 未匹配到利率标签的基底表数据
        List<String> unMatchedOrgCell =
                orgDataByCellbI.stream().map(FlagData::getCellMd5).filter(cellMd5 -> !matchedCellList.contains(cellMd5)).collect(Collectors.toList());
        // 未匹配到利率标签的打标表数据
        List<FlagData> flagDataOther = flagData.stream().filter(t -> unMatchedOrgCell.contains(t.getCellMd5())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(flagDataOther)) {
            flagDataMapper.batchUpdateInterestFlagById(flagDataOther, 1, otherInterestConfig);
        }
        Set<String> cellBiSet = orgDataByCellbI.stream()
                .map(FlagData::getCellMd5)
                .collect(Collectors.toSet());
        List<FlagData> differenceFlagData = flagData.stream()
                .filter(f -> !cellBiSet.contains(f.getCellMd5())) // 过滤掉 list2 中包含的元素
                .collect(Collectors.toList());
        flagDataMapper.batchUpdateInterestFlagById(differenceFlagData, 1, null);

    }

    private Boolean isMatch(FlagData t, String condition) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        Map map = new HashMap<String, String>();
        map.put("api_code", t.getApiCode());
        context.setVariables(map);
        ExpressionParser parser = new SpelExpressionParser();
        Boolean isMatch = parser.parseExpression(condition).getValue(context, Boolean.class);
        return isMatch;
    }

    private void handleRiskGroup(List<FlagData> flagData, List<FlagData> orgDataByCellbI, List<DataMarkConfig> dataMarkConfigByRiskGroup) {
        Map<Integer, List<DataMarkConfig>> configMap =
                dataMarkConfigByRiskGroup.stream().collect(Collectors.groupingBy(DataMarkConfig::getMarkOutValueType));
        List<String> userTypeConfig = Arrays.asList(configMap.get(0).get(0).getMarkOutValue().split(","));

        // 匹配到客群标签的基底表数据
        Map<String, List<FlagData>> mapGroupByUserType = orgDataByCellbI.stream().filter(t ->
                userTypeConfig.contains(t.getUserType())).collect(Collectors.groupingBy(FlagData::getUserType));

        for (String userType : mapGroupByUserType.keySet()) {
            List<String> cellByUserType = mapGroupByUserType.get(userType).stream().map(FlagData::getCellMd5).collect(Collectors.toList());
            List<FlagData> flagDataByUserType = flagData.stream().filter(t -> cellByUserType.contains(t.getCellMd5())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(flagDataByUserType)) {
                continue;
            }
            flagDataMapper.batchUpdateRiskGroupFlagById(flagDataByUserType,  userType);
        }

        // 未匹配到客群标签的基底表数据
        List<String> unMatchedOrgCell = orgDataByCellbI.stream().filter(t ->
                !userTypeConfig.contains(t.getUserType())
        ).map(FlagData::getCellMd5).collect(Collectors.toList());
        List<FlagData> flagDataUnMatch = flagData.stream().filter(t -> unMatchedOrgCell.contains(t.getCellMd5())).collect(Collectors.toList());
//        if (CollectionUtils.isEmpty(flagDataUnMatch)) {
//            return;
//        }
        Set<String> cellBiSet = orgDataByCellbI.stream()
                .map(FlagData::getCellMd5)
                .collect(Collectors.toSet());
        List<FlagData> differenceFlagData = flagData.stream()
                .filter(f -> !cellBiSet.contains(f.getCellMd5())) // 过滤掉 list2 中包含的元素
                .collect(Collectors.toList());
        flagDataUnMatch.addAll(differenceFlagData);
        flagDataMapper.batchUpdateRiskGroupFlagById(flagDataUnMatch, configMap.get(1).get(0).getMarkOutValue());
    }
}
