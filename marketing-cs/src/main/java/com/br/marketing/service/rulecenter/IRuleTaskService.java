package com.br.marketing.service.rulecenter;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.ScoreSearchCondition;
import com.br.marketing.service.rulecenter.enums.RuleCenterDataSourceEnum;
import com.br.marketing.vo.xiecheng.PushViewVO;

/**
 * 规则中心推送决策的任务接口
 */
public interface IRuleTaskService {

    /**
     * 数量筛选
     * @param dto
     * @return
     */
    Result<PushViewVO> pushPreview(PushCustomerDTO dto);

    /**
     * 自动生成任务需要预览 构建的查询参数
     * @param main
     * @param scoreSearchCondition
     * @return
     */
    PushCustomerDTO buildPreviewDTO(CustomerInfoPushMain main, ScoreSearchCondition scoreSearchCondition);

    /**
     * 实现类的标签
     * @return
     */
    RuleCenterDataSourceEnum sourceLabel();
}
