package com.br.marketing.service.Impl;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.br.marketing.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.mapper.MarketingTaskMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 跨天恢复跑分：不再拦截「创建日≠当天」；跨天时将 b_marketing_task 日期窗口对齐到当天，便于调度取任务。
 */
@Service
@Slf4j
public class ScoreCrossDayRestoreService {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    private MarketingTaskMapper marketingTaskMapper;

    /**
     * 非跨天：直接成功；跨天：仅更新 b_marketing_task 起止日期（缺批次或任务则失败）。
     */
    @Transactional(rollbackFor = Exception.class)
    public Result prepareForResume(StraHisFile straHisFile) {
        if (straHisFile.getCreateTime() == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        String scoreDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(straHisFile.getCreateTime()).substring(0, 10);
        String today = LocalDate.now().format(ISO_DATE);

        if (scoreDate.equals(today)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        if (StringUtils.isBlank(straHisFile.getBatchNumber())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("跑分记录缺少批次号，无法跨天恢复");
        }
        MarketingTask marketingTask = marketingTaskMapper.getByBatchNumber(straHisFile.getBatchNumber());
        if (marketingTask == null) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("未找到对应营销任务，无法跨天恢复");
        }

        String closeDate = LocalDate.now().plusDays(1).format(ISO_DATE);
        int rows = marketingTaskMapper.updateDateWindowByBatchNumber(straHisFile.getBatchNumber(), today, closeDate);
        if (rows <= 0) {
            log.warn("跨天恢复自愈：按批次更新营销任务日期窗口影响行数为 0, batchNumber={}", straHisFile.getBatchNumber());
        }
        log.warn("跨天跑分恢复自愈完成 fileId={} batchNumber={} start_date={} close_date={}",
                straHisFile.getId(), straHisFile.getBatchNumber(), today, closeDate);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
