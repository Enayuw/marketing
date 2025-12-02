package com.br.marketing.service.strategy.pushpreview;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.mapper.TagDataRuleCalculateMapper;
import com.br.marketing.service.datagroup.rulecenter.RuleCenterLabelService;
import com.br.marketing.vo.xiecheng.PushViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 合并跑分任务推送预览策略
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class MergeScorePushPreviewStrategy implements IPushPreviewStrategy {

    @Resource
    private RuleCenterLabelService ruleCenterLabelService;

    @Resource
    private TagDataRuleCalculateMapper tagDataRuleCalculateMapper;

    @Override
    public Result<PushViewVO> execute(PushCustomerDTO dto) {
        long start = System.currentTimeMillis();
        
        // 组装查询sql
        String countSql = "SELECT COUNT(1) ".concat(ruleCenterLabelService.scoreMergeAssemble(dto));
        
        // 执行查询获取统计数量
        Integer count = tagDataRuleCalculateMapper.getCountbI_(countSql);
        log.warn("跑分合并预览量级查询sql={}，耗时={}ms", 
                countSql, System.currentTimeMillis() - start);
        
        int total = (count != null ? count : 0);
        
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

        PushViewVO pushViewVO = new PushViewVO();
        pushViewVO.setTotal(total);
        return new Result<PushViewVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushViewVO);
    }

    @Override
    public PushPreviewStrategyEnum getStrategyType() {
        return PushPreviewStrategyEnum.MERGE_SCORE;
    }
}

