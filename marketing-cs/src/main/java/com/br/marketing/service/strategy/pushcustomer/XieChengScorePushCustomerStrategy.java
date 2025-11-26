package com.br.marketing.service.strategy.pushcustomer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;
import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.Impl.PushRuleServiceImpl;
import com.br.marketing.service.strategy.pushpreview.PushPreviewStrategyEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.xiecheng.XieChengEsJsonHandler;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 携程跑分推送客户策略
 * 完全按照原 pushScoreCustomer 方法中携程相关逻辑实现
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class XieChengScorePushCustomerStrategy extends AbstractPushCustomerStrategy {

    @Resource
    private PushRuleServiceImpl pushRuleService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public Result<String> execute(PushCustomerDTO dto) {
        // 参数校验
        Result<String> validateResult = validateScoreParams(dto);
        if (!ResultCode.SUCCESS.getValue().equals(validateResult.getCode())) {
            return validateResult;
        }

        // 查询文件列表
        List<StraHisFile> files = queryStraHisFiles(dto.getFileIdList());
        
        // 携程撞库特有逻辑
        Integer pushNum = dto.getmPrePlanNum();
        JSONObject jsonObject = JSON.parseObject(dto.getmRuleCondition());
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        
        String extend;
        Boolean xcTruePushCustomerPushPreviewOptFlag = marketingCommonConfig.getXcTruePushCustomerPushPreviewOptFlag();
        if (!xcTruePushCustomerPushPreviewOptFlag) {
            extend = pushRuleService.cycleDataQuery(jsonObject, dto.getBatchNumberList(), collidingFilterDTO);
        } else {
            List<String> querySqls = new ArrayList<>();
            pushRuleService.cycleDataQueryOpt(jsonObject, dto.getBatchNumberList(), collidingFilterDTO, querySqls);
            extend = String.join(";", querySqls);
        }

        // 组装 CustomerInfoPushMain（包含批次号列表）
        CustomerInfoPushMain customerInfoPushMain = buildScoreCustomerInfoPushMain(dto, pushNum);
        customerInfoPushMain.setFilterType(1);
        customerInfoPushMain.setExtend(extend);
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
        return PushPreviewStrategyEnum.XIE_CHENG_SCORE;
    }
}

