package com.br.marketing.service.strategy.pushpreview;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.enums.TaskTypeEnum;
import com.br.marketing.service.Impl.PushRuleServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.xiecheng.PushViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 携程跑分任务推送预览策略
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class XieChengScorePushPreviewStrategy implements IPushPreviewStrategy {

    @Resource
    private PushRuleServiceImpl pushRuleService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public Result<PushViewVO> execute(PushCustomerDTO dto) {
        PushViewVO pushViewVO = new PushViewVO();
        int total = pushRuleService.getXieChengDataNum(
                dto.getmRuleCondition(),
                dto.getBatchNumberList(),
                pushViewVO);

        if (total <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合的数据");
        }
        
        // 处理百分比逻辑（原getScoreTotal方法中的逻辑）
        if (dto.getmPercentage() != null) {
            if (dto.getmPercentage().compareTo(new BigDecimal(0)) <= 0) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("百分比不能小于等于0");
            }
            Integer res = dto.getmPercentage().multiply(new BigDecimal(total)).setScale(0, RoundingMode.UP).intValue();
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
        }

        pushViewVO.setTotal(total);
        return new Result<PushViewVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushViewVO);
    }

    @Override
    public boolean support(PushCustomerDTO dto) {
        // 首先必须是跑分任务
        if (Objects.equals(dto.getTaskType(), TaskTypeEnum.UPLOAD_TASKS.getValue())) {
            return false;
        }

        // 判断是否为携程数据
        return isXieChengData(dto);
    }

    @Override
    public int priority() {
        return 2;
    }

    /**
     * 判断是否为携程数据
     *
     * @param dto 推送客户DTO
     * @return true-是携程数据，false-不是
     */
    private Boolean isXieChengData(PushCustomerDTO dto) {
        Boolean isXieCheng = Boolean.FALSE;
        JSONArray datas = JSON.parseObject(dto.getmRuleCondition()).getJSONArray("data");
        if (!CollectionUtils.isEmpty(datas)) {
            Object result = datas.stream().filter(obj -> ("result").equals(
                    ((JSONObject) obj).getString("key"))).findAny().orElse(null);
            Object blacklistDelete = datas.stream().filter(obj -> ("blacklist_delete").equals(
                    ((JSONObject) obj).getString("key"))).findAny().orElse(null);
            //api_code为携程且筛选条件传入result
            if (marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().contains(dto.getApiCode())
                    && (!ObjectUtils.isEmpty(result) || !ObjectUtils.isEmpty(blacklistDelete))) {
                isXieCheng = Boolean.TRUE;
            }
        }
        return isXieCheng;
    }

}

