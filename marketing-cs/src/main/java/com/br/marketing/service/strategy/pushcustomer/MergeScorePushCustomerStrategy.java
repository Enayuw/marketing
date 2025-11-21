package com.br.marketing.service.strategy.pushcustomer;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.strategy.pushpreview.PushPreviewStrategyEnum;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 合并跑分推送客户策略
 * 完全按照原 pushScoreCustomer 方法中合并跑分相关逻辑实现
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class MergeScorePushCustomerStrategy extends AbstractPushCustomerStrategy {

    @Override
    public Result<String> execute(PushCustomerDTO dto) {
        // 参数校验
        Result<String> validateResult = validateScoreParams(dto);
        if (!ResultCode.SUCCESS.getValue().equals(validateResult.getCode())) {
            return validateResult;
        }

        // 查询文件列表
        List<StraHisFile> files = queryStraHisFiles(dto.getFileIdList());
        
        // 组装 CustomerInfoPushMain（包含批次号列表）
        Integer pushNum = dto.getmPrePlanNum();
        CustomerInfoPushMain customerInfoPushMain = buildScoreCustomerInfoPushMain(dto, pushNum);
        
        // 合并跑分特有逻辑
        customerInfoPushMain.setPushTarget(2);
        customerInfoPushMain.setExtend(dto.getScoreMergeField());
        customerInfoPushMainMapper.insertSelective(customerInfoPushMain);
        
        // 生成并更新批次名称
        String batchName = generateBatchName(dto, customerInfoPushMain);
        updateBatchName(customerInfoPushMain.getId(), batchName);
        
        // 批量插入 CustomerInfoPushBatch
        Date date = new Date();
        insertCustomerInfoPushBatch(dto, customerInfoPushMain, files, date);
        
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(customerInfoPushMain.getId().toString());
    }

    @Override
    public PushPreviewStrategyEnum getStrategyType() {
        return PushPreviewStrategyEnum.MERGE_SCORE;
    }
}

