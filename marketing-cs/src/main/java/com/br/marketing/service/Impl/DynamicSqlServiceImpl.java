package com.br.marketing.service.Impl;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingRuleCenterLabelReportMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingSyncLabelMapper;
import com.br.marketing.service.IDynamicSqlService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class DynamicSqlServiceImpl implements IDynamicSqlService {

    private static final Logger logger = LoggerFactory.getLogger(DynamicSqlServiceImpl.class);

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    MarketingSyncLabelMapper marketingSyncLabelMapper;

    @Resource
    MarketingRuleCenterLabelReportMapper marketingRuleCenterLabelReportMapper;

    private static final String BUILD_TASK_NUM_KEY = "buildTaskNum";
    private static final String SCORE_MIN_ID_KEY = "scoreMinId";
    private static final String SCORE_DATA_KEY = "scoreData";

    private static final String TI_FLASH = "tiflash";
    private static final String TI_KV = "tikv";

    @Override
    public Integer countByRuleScoreWithDate(String apiCode, String whereStr, String labelName) {

        HashMap<String, Integer> sqlType = marketingCommonConfig.getSqlType();
        Integer type = sqlType == null ? 0 : sqlType.getOrDefault(BUILD_TASK_NUM_KEY, 0);
        Integer count;
        String storageType = type == 1 ? TI_FLASH : TI_KV;
        long start = System.currentTimeMillis();
        if (StringUtils.isNotBlank(labelName)) {
            Long labelId = getIdByLabelName(whereStr, labelName);
            if (type.equals(1)) {
                count = marketingSyncInfoMapper.countByRuleScoreLabelWithDatetiflash_(apiCode, whereStr, labelId);
            } else {
                count = marketingSyncInfoMapper.countByRuleScoreLabelWithDate(apiCode, whereStr, labelId);
            }
        } else {
            if (type.equals(1)) {
                count = marketingSyncInfoMapper.countByRuleScoreWithDatetiflash_(apiCode, whereStr);
            } else {
                count = marketingSyncInfoMapper.countByRuleScoreWithDate(apiCode, whereStr);
            }
        }
        logExecutionTime(BUILD_TASK_NUM_KEY, storageType, start);
        return count;
    }

    @Override
    public Long minIdRuleScoreWithDate(String apiCode, String whereStr, String labelName) {
        HashMap<String, Integer> sqlType = marketingCommonConfig.getSqlType();
        Integer type = sqlType == null ? 0 : sqlType.getOrDefault(SCORE_MIN_ID_KEY, 0);
        Long mid;
        String storageType = type == 1 ? TI_FLASH : TI_KV;
        long start = System.currentTimeMillis();
        if (StringUtils.isNotBlank(labelName)) {
            Long labelId = getIdByLabelName(whereStr, labelName);
            if (type.equals(1)) {
                mid = marketingSyncInfoMapper.minIdRuleScoreLabelWithDatetiflash_(apiCode, whereStr, labelId);
            } else {
                mid = marketingSyncInfoMapper.minIdRuleScoreLabelWithDate(apiCode, whereStr, labelId);
            }
        } else {
            if (type.equals(1)) {
                mid = marketingSyncInfoMapper.minIdRuleScoreWithDatetiflash_(apiCode, whereStr);
            } else {
                mid = marketingSyncInfoMapper.minIdRuleScoreWithDate(apiCode, whereStr);
            }
        }
        logExecutionTime(SCORE_MIN_ID_KEY, storageType, start);
        return mid;
    }

    @Override
    public List<MarketingSyncUser> selectDataRuleScoreWithDate(String apiCode, String whereStr,
                                                               Long id, Integer pageSize, String labelName,
                                                               Long minUnCompleteId,Long maxId) {
        HashMap<String, Integer> sqlType = marketingCommonConfig.getSqlType();
        Integer type = sqlType == null ? 0 : sqlType.getOrDefault(SCORE_DATA_KEY, 0);
        List<MarketingSyncUser> users = new ArrayList<>();
        List<MarketingSyncLabelUser> labelUsers;
        String storageType = type == 1 ? TI_FLASH : TI_KV;
        long start = System.currentTimeMillis();
        if (StringUtils.isNotBlank(labelName)) {
            Long labelId = getIdByLabelName(whereStr, labelName);
            List<Long> syncIdList = marketingSyncLabelMapper.getSyncIdByLabelId(apiCode, whereStr, id, pageSize, labelId, minUnCompleteId, maxId);
            if (syncIdList != null && !syncIdList.isEmpty()) {
                if (type.equals(1)) {
                    labelUsers = marketingSyncInfoMapper.selectDataRuleScoreLabelWithDatetiflash_(apiCode, syncIdList);
                    users.addAll(labelUsers);
                } else {
                    labelUsers = marketingSyncInfoMapper.selectDataRuleScoreLabelWithDate(apiCode, syncIdList);
                    users.addAll(labelUsers);
                }
            }
        } else {
            if (type.equals(1)) {
                users = marketingSyncInfoMapper.selectDataRuleScoreWithDatetiflash_(apiCode, whereStr, id, pageSize, minUnCompleteId, maxId);
            } else {
                users = marketingSyncInfoMapper.selectDataRuleScoreWithDate(apiCode, whereStr, id, pageSize, minUnCompleteId, maxId);
            }
        }
        logExecutionTime(SCORE_DATA_KEY, storageType, start);
        return users;
    }

    public Long getIdByLabelName(String whereStr, String labelName) {
        try {
            List<Long> idList = marketingRuleCenterLabelReportMapper.selectLabelIdWithLabelName(whereStr, labelName);
            if (idList.isEmpty()) {
                log.warn("未找到标签名为 {} 的记录", labelName);
                return null;
            }
            return idList.get(0);
        } catch (Exception e) {
            log.error("查询标签ID失败, labelName: {}", labelName, e);
            return null;
        }
    }

    /**
     * 记录执行时间
     */
    private void logExecutionTime(String operation, String storageType, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;
        if (log.isWarnEnabled()) {
            logger.warn("执行{}-{}耗时:{}", operation, storageType, executionTime);
        }
    }
}
