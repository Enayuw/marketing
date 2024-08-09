package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.OptConditionDTO;
import com.br.marketing.dto.PushDecisionsDTO;
import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.service.PushDecisionsService;
import com.br.marketing.vo.PushDecisionsDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @ClassName PushDecisionsServiceImpl
 * @Description TODO
 * @Author kongbx
 * @Date 2024/8/9 10:22
 */
@Service
@Slf4j
public class PushDecisionsServiceImpl implements PushDecisionsService {
    @Override
    public Result<Long> savePushDecisions(PushDecisionsDTO dto) {
        return null;
    }

    @Override
    public Result<Boolean> deletePushDecisions(Long id) {
        return null;
    }

    @Override
    public Result<PageResultReturn<PushDecisionsDetailVO>> getPushDecisionsList(SearchConditionDTO dto) {
        return null;
    }

    @Override
    public Result<PushDecisionsDetailVO> getPushDecisionsDetails(Long id) {
        return null;
    }

    @Override
    public Result updateStatus(OptConditionDTO dto) {
        return null;
    }
}
