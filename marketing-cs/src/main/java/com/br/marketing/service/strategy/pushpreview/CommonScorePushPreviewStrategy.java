package com.br.marketing.service.strategy.pushpreview;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.service.Impl.PushRuleServiceImpl;
import com.br.marketing.vo.xiecheng.PushViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 通用跑分任务推送预览策略
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class CommonScorePushPreviewStrategy implements IPushPreviewStrategy {

    @Resource
    private PushRuleServiceImpl pushRuleService;

    @Override
    public Result<PushViewVO> execute(PushCustomerDTO dto) {
        PushViewVO pushViewVO = new PushViewVO();
        
        // 调用联邦查询获取数量
        Result<PushViewVO> pushViewVOResult = pushRuleService.queryFederation(dto, pushViewVO);
        if (!ResultCode.SUCCESS.getValue().equals(pushViewVOResult.getCode())) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage(pushViewVOResult.getMessage());
        }
        
        int total = pushViewVOResult.getData().getTotal();
        
        if (total <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合的数据");
        }
        
        // 处理百分比逻辑（原getScoreTotal方法中的逻辑）
        if (dto.getmPercentage() != null) {
            if (dto.getmPercentage().compareTo(BigDecimal.ZERO) <= 0) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("百分比不能小于等于0");
            }
            Integer res = dto.getmPercentage().multiply(new BigDecimal(total)).setScale(0, RoundingMode.UP).intValue();
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
        }

        pushViewVO.setTotal(total);
        return new Result<PushViewVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushViewVO);
    }

    @Override
    public PushPreviewStrategyEnum getStrategyType() {
        return PushPreviewStrategyEnum.COMMON_SCORE;
    }
}

