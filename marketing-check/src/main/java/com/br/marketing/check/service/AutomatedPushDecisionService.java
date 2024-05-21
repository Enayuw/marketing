package com.br.marketing.check.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.bo.JobPushDecisionParameterBO;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.strategy.MethodRetryHandlerService;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 自动化推送决策系统
 *
 * @author Guo Zeqiang
 * @dateTime 2023-04-11 10:36
 */
public interface AutomatedPushDecisionService {

    /**
     * 2023-04-11 16:31
     * 客户活动
     *
     * @return CustomerPushDecisionActionEnum
     */
    CustomerPushDecisionActionEnum customerAction();

    /**
     * 2023-04-13 13:53
     * 创建任务记录
     *
     * @param parameter    配置参数
     * @param mapper       dao
     * @param jobParameter 调度任务参数
     * @return list 为null或size小于0 说明任务已存在
     */
    List<TransferActionFront> createActionFrontRows(JobPushDecisionParameterBO parameter
            , TransferActionFrontMapper mapper, String jobParameter);

    /**
     * 2023-04-13 13:54
     * 处理数据
     *
     * @param actionFront  任务记录
     * @param parameter    配置参数
     * @param jobParameter 调度任务参数
     * @return 决策接口数据
     */
    TransferActionFront actionData(TransferActionFront actionFront
            , JobPushDecisionParameterBO parameter
            , String jobParameter
            , MethodRetryHandlerService methodRetryHandlerService);

    /**
     * 2023-04-13 21:19
     * 调用决策接口
     *
     * @param retryByRuleDTO            决策接口数据结构
     * @param methodRetryHandlerService 接口帮助类
     * @return 状态
     */
    default Result<?> pushDecision(PolicyRetryByRuleDTO retryByRuleDTO
            , MethodRetryHandlerService methodRetryHandlerService) {
        return methodRetryHandlerService.callPolicyData(retryByRuleDTO, null);
    }

    /**
     * 2023-04-13 21:20
     * 获取任务记录
     *
     * @param apiCode      客户编号
     * @param actionType   执行类型
     * @param localDateStr 执行日期
     * @param mapper       dao
     * @return list
     */
    default List<TransferActionFront> getActionFrontList(String apiCode, int actionType
            , String localDateStr, TransferActionFrontMapper mapper) {
        TransferActionFrontExample example = new TransferActionFrontExample();
        example.setOrderByClause("create_time desc");
        example.createCriteria()
                .andActionTypeEqualTo(actionType)
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(localDateStr)
                .andIsDelEqualTo(1);
        List<TransferActionFront> list = mapper.selectByExample(example);
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : list;
    }

    /**
     * 2023-04-13 21:20
     * 保存任务记录
     *
     * @param list   任务记录集合
     * @param mapper dao
     * @return 落库成功数据量
     */
    default int saveActionFront(TransferActionFrontMapper mapper, TransferActionFront... list) {
        int count = 0;
        for (TransferActionFront actionFront : list) {
            int i = mapper.insertSelective(actionFront);
            if (i == 1) {
                count++;
            }
        }
        return count;
    }

    /**
     * 2023-04-13 21:20
     * 更新任务记录
     *
     * @param list   任务记录集合
     * @param mapper dao
     * @return 更新成功数据量
     */
    default int updateActionFrontStatus(TransferActionFrontMapper mapper, TransferActionFront... list) {
        int count = 0;
        for (TransferActionFront actionFront : list) {
            int i = mapper.updateByPrimaryKeySelective(actionFront);
            if (i == 1) {
                count++;
            }
        }
        return count;
    }

    /**
     * 2023-04-13 21:20
     * 获取调度任务中传递的参数
     *
     * @param apiCode      客户编号
     * @param jobParameter 调度任务参数
     * @return JobPushDecisionParameterBO
     * @throws IllegalArgumentException 参数格式非法
     */
    default JobPushDecisionParameterBO getJobParameter(String apiCode, String jobParameter) {
        Object parse = JSON.parse(jobParameter);
        if (parse instanceof JSONArray) {
            List<JobPushDecisionParameterBO> list = JSON.parseObject(jobParameter
                    , new TypeReference<List<JobPushDecisionParameterBO>>() {
                    });
            for (JobPushDecisionParameterBO parameterBO : list) {
                if (apiCode.equals(parameterBO.getApiCode())) {
                    return parameterBO;
                }
            }
        } else if (parse instanceof JSONObject) {
            JobPushDecisionParameterBO parameterBO = JSON.parseObject(jobParameter
                    , new TypeReference<JobPushDecisionParameterBO>() {
                    });
            if (apiCode.equals(parameterBO.getApiCode())) {
                return parameterBO;
            }
        } else {
            throw new IllegalArgumentException("非法的参数:" + jobParameter);
        }
        return null;
    }
}
