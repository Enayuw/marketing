package com.br.marketing.service.strategy.pushinfolist;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.output.PolicyResultByTaskIdsDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.vo.PushInfoListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 推送信息列表查询策略抽象基类
 * 提取公共的查询决策结果、组装返回消息等逻辑
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
public abstract class AbstractPushInfoListStrategy implements IPushInfoListStrategy {

    @Resource
    protected CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    protected IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    /**
     * 查询失败任务的决策结果
     *
     * @param dto          查询条件
     * @param failStatusIds 失败状态的任务ID列表
     * @return 任务ID -> 错误信息的映射
     */
    protected Map<String, Map<String, Object>> queryFailTaskResults(PushInfoFilterDTO dto, List<String> failStatusIds) {
        Map<String, Map<String, Object>> resultMap = new HashMap<>();
        
        if (!CollectionUtils.isEmpty(failStatusIds)) {
            Result<List<PolicyResultByTaskIdsDTO>> result = intelligentCustomerServiceClient.getTaskIdsResult(
                    dto.getmApiCode(), failStatusIds);
            
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                List<PolicyResultByTaskIdsDTO> resultByTaskIdsDTOS = result.getData();
                resultByTaskIdsDTOS.forEach(policyResultByTaskIdsDTO -> {
                    Map<String, Object> errorMap = JSON.parseObject(policyResultByTaskIdsDTO.getVerificationReason());
                    resultMap.put(policyResultByTaskIdsDTO.getVerification(), errorMap);
                });
            } else {
                log.warn("决策查询接口异常result={}", JSON.toJSONString(result));
            }
        }
        
        return resultMap;
    }

    /**
     * 提取失败状态的任务ID列表
     *
     * @param list 推送信息列表
     * @return 失败状态的任务ID列表（状态=5）
     */
    protected List<String> extractFailStatusIds(List<PushInfoListVO> list) {
        return list.stream()
                .filter(t -> t.getmStatus().equals(5))
                .map(t -> String.valueOf(t.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 提取任务ID列表
     *
     * @param list 推送信息列表
     * @return 任务ID列表
     */
    protected List<Long> extractIds(List<PushInfoListVO> list) {
        return list.stream()
                .map(PushInfoListVO::getId)
                .collect(Collectors.toList());
    }
}

