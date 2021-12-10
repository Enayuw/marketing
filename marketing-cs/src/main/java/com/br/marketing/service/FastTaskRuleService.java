package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.vo.FastTaskRuleDetailVO;

public interface FastTaskRuleService {
    /**
     * 跑分记录列表
     * @param current
     * @param size
     * @param search
     * @param status
     * @param createTimeStart
     * @param createTimeEnd
     * @param updateTimeStart
     * @param updateTimeEnd
     * @param taskStatus
     * @return
     */
    PageResultReturn list(int current, int size, String search, Integer status, String createTimeStart, String createTimeEnd, String updateTimeStart, String updateTimeEnd, String taskStatus);


    /**
     * 生成批量跑分
     * @param vo
     * @param user
     * @return
     */
    ApiResult<Boolean> save(FastTaskRuleDetailVO vo, UserDetail user);

    /**
     * 查看跑分记录
     * @param id
     * @return
     */
    FastTaskRuleDetailVO getFastTask(String id);

    /**
     * 操作状态
     * @param id
     * @param status
     * @param user
     * @return
     */
    boolean updateStatusById(String id, Integer status, UserDetail user);

    /**
     * 修改跑分记录
     * @param ruleName
     * @param taskTime
     * @param user
     * @return
     */
    ApiResult<Boolean> update(String ruleName, String taskTime, UserDetail user);
}
