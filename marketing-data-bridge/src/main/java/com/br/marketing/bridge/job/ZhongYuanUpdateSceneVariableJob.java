package com.br.marketing.bridge.job;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bridge.common.enums.SceneVariableExecuteStatusEnum;
import com.br.marketing.bridge.model.dto.VariableItem;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSceneVariable;
import com.br.marketing.entity.MarketingSceneVariableExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSceneVariableMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ZhongYuanUpdateSceneVariableJob
 * @Description 中原消金修改场景变量job
 * @Author kongbx
 * @Date 2025/11/17 19:54
 */
@Component
@Slf4j
public class ZhongYuanUpdateSceneVariableJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private MarketingSceneVariableMapper marketingSceneVariableMapper;
    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    private static final String TITLE = "【中原消金场景变量修改】";

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        try {
            // 1. 从配置获取中原消金的apiCode
            String apiCode = jobExecutionMultipleShardingContext.getJobParameter();
            if (StringUtils.isEmpty(apiCode)) {
                apiCode = getZhongYuanApiCode();
            }

            if (apiCode == null) {
                log.warn("{}Job执行失败：未配置apiCode", TITLE);
                return;
            }

            log.warn("{}Job开始执行，apiCode: {}", TITLE, apiCode);

            // 2. 查询待执行的场景变量记录
            List<MarketingSceneVariable> pendingList = getPendingSceneVariables(apiCode);

            if (pendingList == null || pendingList.isEmpty()) {
                log.warn("{}Job执行完成，无待处理数据", TITLE);
                return;
            }

            log.warn("{}Job查询到待处理数据{}条", TITLE, pendingList.size());

            // 3. 处理每条记录
            int successCount = 0;
            int failCount = 0;

            for (MarketingSceneVariable sceneVariable : pendingList) {
                try {
                    boolean result = processSceneVariable(apiCode, sceneVariable);
                    if (result) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("{}处理场景变量记录失败，id: {}, taskUid: {}", TITLE, 
                            sceneVariable.getId(), sceneVariable.getTaskUid(), e);
                    failCount++;
                    // 更新状态为失败
                    String errorMsg = "处理异常: " + e.getMessage();
                    updateSceneVariableStatus(sceneVariable.getId(), SceneVariableExecuteStatusEnum.FAILED, errorMsg);
                }
            }

            log.warn("{}Job执行完成，成功: {}条，失败: {}条", TITLE, successCount, failCount);

        } catch (Exception e) {
            log.error("{}Job执行异常", TITLE, e);
        }
    }

    /**
     * 查询待执行的场景变量记录
     */
    private List<MarketingSceneVariable> getPendingSceneVariables(String apiCode) {
        try {
            MarketingSceneVariableExample example = new MarketingSceneVariableExample();
            example.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andExecuteStatusEqualTo(SceneVariableExecuteStatusEnum.PENDING.getCode());
            return marketingSceneVariableMapper.selectByExample(example);
        } catch (Exception e) {
            log.error("{}查询待执行场景变量记录异常", TITLE, e);
            return null;
        }
    }

    /**
     * 处理单条场景变量记录
     */
    private boolean processSceneVariable(String apiCode, MarketingSceneVariable sceneVariable) {
        String taskUid = sceneVariable.getTaskUid();
        String variableListJson = sceneVariable.getVariableList();

        log.warn("{}开始处理场景变量，id: {}, taskUid: {}, sceneCode: {}", 
                TITLE, sceneVariable.getId(), taskUid, sceneVariable.getSceneCode());

        // 1. 解析variableList
        if (StringUtils.isEmpty(variableListJson)) {
            String errorMsg = "variableList为空";
            log.warn("{}{}，id: {}, taskUid: {}", TITLE, errorMsg, sceneVariable.getId(), taskUid);
            updateSceneVariableStatus(sceneVariable.getId(), SceneVariableExecuteStatusEnum.FAILED, errorMsg);
            return false;
        }

        List<VariableItem> variableList;
        try {
            variableList = JSON.parseArray(variableListJson, VariableItem.class);
        } catch (Exception e) {
            String errorMsg = "解析variableList失败: " + e.getMessage();
            log.error("{}解析variableList失败，id: {}, variableList: {}", 
                    TITLE, sceneVariable.getId(), variableListJson, e);
            updateSceneVariableStatus(sceneVariable.getId(), SceneVariableExecuteStatusEnum.FAILED, errorMsg);
            return false;
        }

        // 2. 提取overAmt字段
        String overAmtValue = null;
        for (VariableItem variable : variableList) {
            if ("overAmt".equals(variable.getCode()) && StringUtils.isNotEmpty(variable.getValue())) {
                overAmtValue = variable.getValue();
                break;
            }
        }

        if (overAmtValue == null) {
            String errorMsg = "未找到overAmt字段";
            log.warn("{}{}，id: {}, taskUid: {}", TITLE, errorMsg, sceneVariable.getId(), taskUid);
            updateSceneVariableStatus(sceneVariable.getId(), SceneVariableExecuteStatusEnum.FAILED, errorMsg);
            return false;
        }

        // 3. 根据taskUid查询上传明细表
        MarketingSyncUser syncUser = marketingSyncUserMapper.selectSynsUserByCustNumLastWithStatus(apiCode, taskUid);

        if (syncUser == null) {
            String errorMsg = "未找到对应的上传记录";
            log.warn("{}{}，id: {}, taskUid: {}, apiCode: {}", 
                    TITLE, errorMsg, sceneVariable.getId(), taskUid, apiCode);
            return false;
        }

        // 4. 更新reserve_field1中的overAmt字段
        String reserveField1 = syncUser.getReserveField1();
        JSONObject reserveField1Json;

        if (StringUtils.isNotEmpty(reserveField1)) {
            try {
                reserveField1Json = JSON.parseObject(reserveField1);
            } catch (Exception e) {
                log.error("{}解析reserve_field1失败，id: {}, taskUid: {}, reserveField1: {}", 
                        TITLE, sceneVariable.getId(), taskUid, reserveField1, e);
                reserveField1Json = new JSONObject();
            }
        } else {
            reserveField1Json = new JSONObject();
        }

        // 只更新overAmt字段
        reserveField1Json.put("overAmt", overAmtValue);

        syncUser.setReserveField1(reserveField1Json.toJSONString());
        syncUser.setUpdateTime(new Date());
        syncUser.setApiCode(apiCode);

        // 5. 更新数据库
        int updateResult = marketingSyncUserMapper.updateReserveFieldByPrimaryKey(syncUser);

        if (updateResult <= 0) {
            String errorMsg = "更新上传记录失败";
            log.error("{}{}，id: {}, taskUid: {}, syncUserId: {}", 
                    TITLE, errorMsg, sceneVariable.getId(), taskUid, syncUser.getId());
            updateSceneVariableStatus(sceneVariable.getId(), SceneVariableExecuteStatusEnum.FAILED, errorMsg);
            return false;
        }

        // 6. 更新场景变量记录状态为已完成
        String successMsg = "执行成功，overAmt: " + overAmtValue;
        updateSceneVariableStatus(sceneVariable.getId(), SceneVariableExecuteStatusEnum.COMPLETED, successMsg);

        log.warn("{}处理场景变量成功，id: {}, taskUid: {}, overAmt: {}", 
                TITLE, sceneVariable.getId(), taskUid, overAmtValue);

        return true;
    }

    /**
     * 更新场景变量记录状态
     * 
     * @param id 记录ID
     * @param statusEnum 执行状态枚举
     * @param executeResult 执行结果描述
     */
    private void updateSceneVariableStatus(Long id, SceneVariableExecuteStatusEnum statusEnum, String executeResult) {
        try {
            MarketingSceneVariable record = new MarketingSceneVariable();
            record.setId(id);
            record.setExecuteStatus(statusEnum.getCode());
            record.setExecuteResult(executeResult);
            record.setUpdateTime(new Date());
            marketingSceneVariableMapper.updateByPrimaryKeySelective(record);
            
            log.info("{}更新场景变量状态成功，id: {}, status: {}({}), executeResult: {}", 
                    TITLE, id, statusEnum.getCode(), statusEnum.getDesc(), executeResult);
        } catch (Exception e) {
            log.error("{}更新场景变量状态失败，id: {}, status: {}({}), executeResult: {}", 
                    TITLE, id, statusEnum.getCode(), statusEnum.getDesc(), executeResult, e);
        }
    }

    /**
     * 从配置获取中原消金的apiCode
     */
    private String getZhongYuanApiCode() {
        try {
            Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
            return zhongYuanIdentity != null ? zhongYuanIdentity.get("apiCode") : null;
        } catch (Exception e) {
            log.error("{}获取apiCode异常", TITLE, e);
            return null;
        }
    }

}
