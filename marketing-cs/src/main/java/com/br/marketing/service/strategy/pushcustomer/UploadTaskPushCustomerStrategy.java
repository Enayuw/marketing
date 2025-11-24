package com.br.marketing.service.strategy.pushcustomer;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.service.rulecenter.enums.RuleCenterPushTargetEnum;
import com.br.marketing.service.strategy.pushpreview.PushPreviewStrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/**
 * 上传任务推送客户策略
 * 完全按照原 pushUplodCustomer 方法逻辑实现
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class UploadTaskPushCustomerStrategy extends AbstractPushCustomerStrategy {

    @Override
    public Result<String> execute(PushCustomerDTO dto) {
        // 参数校验
        Result<String> validateResult = validateCommonParams(dto);
        if (!ResultCode.SUCCESS.getValue().equals(validateResult.getCode())) {
            return validateResult;
        }

        // 组装 CustomerInfoPushMain 基础字段
        CustomerInfoPushMain customerInfoPushMain = buildCustomerInfoPushMain(dto, dto.getmPrePlanNum());
        
        // 上传任务特有逻辑：设置特殊的创建时间
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            customerInfoPushMain.setCreateTime(formatter.parse(dto.getRepushTime()));
        } catch (Exception e) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("重推框定数据时间转换异常:" + e.getMessage());
        }
        
        // 设置上传任务特有字段
        customerInfoPushMain.setPushTarget(RuleCenterPushTargetEnum.UPLOAD_REPUSH_POLICY.getCode());
        customerInfoPushMain.setUploadReportIds(dto.getUploadReportId());
        customerInfoPushMainMapper.insertSelective(customerInfoPushMain);
        
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(customerInfoPushMain.getId().toString());
    }

    @Override
    public PushPreviewStrategyEnum getStrategyType() {
        return PushPreviewStrategyEnum.UPLOAD_TASK;
    }
}

