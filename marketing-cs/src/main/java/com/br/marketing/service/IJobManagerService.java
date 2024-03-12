package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.TransferActionFront;

import java.util.function.Function;

public interface IJobManagerService {

    /**
     * 判断任务是否允许执行
     *
     * @param apiCode    客户编号
     * @param actionType 任务类型
     * @param actionDate 任务日期
     * @param taskArgs   定制判断逻辑所需的参数
     * @return
     */
    Result<TransferActionFront> isAllowExecute(String apiCode, Integer actionType, String actionDate,Object... taskArgs);

    /**
     * 更新状态
     * @param task
     * @param taskArgs
     * @return
     */
    Result<TransferActionFront> updateJobStatus(TransferActionFront task,Object... taskArgs);
}
