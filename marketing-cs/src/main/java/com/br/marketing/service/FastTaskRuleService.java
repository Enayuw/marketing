package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.vo.FastTaskRuleDetailVO;

import java.util.List;

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
    PageResultReturn list(int current, int size, String search, Integer status, String createTimeStart, String createTimeEnd, String updateTimeStart, String updateTimeEnd, Integer taskStatus);


    /**
     * 生成批量跑分
     * @param vo
     * @param user
     * @return
     */
    @Deprecated
    ApiResult<Boolean> save(FastTaskRuleDetailVO vo, MarketingUserDetail user);

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
    boolean updateStatusById(String id, Integer status, MarketingUserDetail user);

    /**
     * 修改跑分记录
     * @param ruleName
     * @param taskTime
     * @param user
     * @return
     */
    ApiResult<Boolean> update(String id,String ruleName, String taskTime, MarketingUserDetail user);

    /**
     * 跑分规则下拉列表
     * @param apiCode
     * @return
     */
    List<ScoreRuleConfig> getScoreRules(String apiCode);

    /**
     * 获取未跑分数据量
     * @param ids
     * @param apiCode
     * @return
     */
    Integer getNum(String ids, String apiCode);
}
