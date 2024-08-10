package com.br.marketing.service.rulecenter;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.ScoreSearchCondition;
import com.br.marketing.service.rulecenter.enums.RuleCenterDataSourceEnum;
import com.br.marketing.vo.xiecheng.PushViewVO;

public interface IRuleTaskService {

    Result<PushViewVO> pushPreview(PushCustomerDTO dto);

    PushCustomerDTO buildPreviewDTO(CustomerInfoPushMain main, ScoreSearchCondition scoreSearchCondition);

    RuleCenterDataSourceEnum sourceLabel();
}
