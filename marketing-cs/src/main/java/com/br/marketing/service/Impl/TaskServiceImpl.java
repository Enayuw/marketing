package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.entity.MarketingSyncInfoExample;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.service.IApiToDbService;
import com.br.marketing.service.IRuleConfigService;
import com.br.marketing.service.ITaskService;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.CustomerScoreRuleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class TaskServiceImpl implements ITaskService {

    final static DateTimeFormatter ymdhms = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    final static DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    IRuleConfigService iRuleConfigService;

    @Autowired
    SoleStrategyService soleStrategyService;

    @Resource
    MarketingSyncInfoMapper syncInfoMapper;

    @Autowired
    IApiToDbService iApiToDbService;

    @Resource
    MarketingTaskMapper marketingTaskMapper;

    static String warnTemp = "apiCode：%s,数据id：%s,错误信息：%s";


    @Override
    public void buildScoreTask(List<Long> scoreRuleIds) {
        Result<List<CustomerScoreRuleVO>> scoreConfigNow = iRuleConfigService.getScoreConfigNow(scoreRuleIds);
        AssertResult.assertResult(scoreConfigNow);
        List<CustomerScoreRuleVO> data = scoreConfigNow.getData();
        for (CustomerScoreRuleVO datum : data) {
            if("1".equals(datum.getConditionType())){
                buildScoreTaskOfAuto(datum);
            }else{
                buildScoreTaskOfSelect(datum);
            }
        }
    }

    private void buildScoreTaskOfAuto(CustomerScoreRuleVO vo){
        String apiCode = vo.getApiCode();

        //region 时间处理
        String startTime = vo.getStartTime();
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDate nowData = LocalDate.now();
        String validTimeStr = nowData.format(ymd).concat(" " + startTime + ":00");
        LocalDateTime validTime = LocalDateTime.parse(validTimeStr, ymdhms);

        //筛选数据范围时间
        String sTimeStr = "", eTimeStr = "";
        Date sTime = null, eTime = null;
        //任务的开始时间和结束时间
        String taskStart = "", taskEnd = "";
        if (nowTime.compareTo(validTime) > 0) {
            if ("00:00".equals(startTime)) {
                sTimeStr = nowData.minusDays(1L).format(ymd).concat(" 00:00:00");
                eTimeStr = validTime.format(ymdhms);
                taskStart = LocalDate.now().format(ymd);
                taskEnd = LocalDate.now().plusDays(1L).format(ymd);
            } else {
                sTimeStr = nowData.format(ymd).concat(" 00:00:00");
                eTimeStr = validTime.format(ymdhms);
                taskStart = LocalDate.now().format(ymd);
                taskEnd = LocalDate.now().plusDays(1L).format(ymd);
            }
        } else {
            sTimeStr = nowData.minusDays(1L).format(ymd).concat(" 00:00:00");
            eTimeStr = validTime.minusDays(1L).format(ymdhms);
            taskStart = LocalDate.now().minusDays(1L).format(ymd);
            taskEnd = LocalDate.now().format(ymd);
        }
        try {
            sTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sTimeStr);
            eTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(eTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date ruleOpenTime = vo.getUpdateTime();
        if (ruleOpenTime == null) {
            ruleOpenTime = vo.getCreateTime();
        }
        String ruleOpenDay = new SimpleDateFormat("yyyy-MM-dd").format(ruleOpenTime);
        String nowDay = LocalDate.now().format(ymd);
        // 规则启用日期和生成任务日期相同 需要比较 生效时间是小于等于规则开启时间 认为历史的任务不予生成
        if (ruleOpenDay.equals(nowDay) && eTime.compareTo(ruleOpenTime) <= 0) {
            return;
        }
        //endregion

        //region 条件解析
        Result<String> conditionRes = soleStrategyService.analysisCondition(vo.getConditionInfo());
        if (!ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())) {
            log.warn(String.format("自动规则生成任务 数据范围解析有误;"+warnTemp,vo.getApiCode(),vo.getId(),conditionRes.getMessage()));
           return;
        }

        MarketingSyncInfoExample syncInfoIngExample = new MarketingSyncInfoExample();
        syncInfoIngExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andCreateTimeGreaterThanOrEqualTo(sTime)
                .andCreateTimeLessThan(eTime)
                .andStatusEqualTo(1)
                .andIsUploadEqualTo(1);
        int isUploadCount = syncInfoMapper.countByExample(syncInfoIngExample);
        if (isUploadCount > 0) {
            log.warn(String.format("自动规则生成任务 上传数据还未解析完成"+warnTemp,vo.getApiCode(),vo.getId()));
            return;
        }
        String number = "";
        String sDate = LocalDateTime.parse(sTimeStr).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Long minId = syncInfoMapper
                .getMinIdByRuleScoreWithDate(apiCode, sTimeStr, eTimeStr, conditionRes.getData());
        if (minId != null && minId > 0) {
            String time = LocalDateTime.parse(eTimeStr, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Result<String> batchNumberRes = iApiToDbService.buildBatchNumber(apiCode
                    , vo.getId().toString(), vo.getRuleNameShort()
                    , time, null);
            if (!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())) {
                log.warn(String.format("自动规则生成任务 批次号生成错误"+warnTemp,vo.getApiCode(),vo.getId()));
                return;
            }
            number = batchNumberRes.getData();
        } else {
            return;
        }

        MarketingTask hasTask = marketingTaskMapper.getByBatchNumber(number);
        if (hasTask != null) {
            return;
        }

        BaseHeadConfigVO baseHeadConfigVO = JSON.parseObject(vo.getBaseInfo()
                , new TypeReference<BaseHeadConfigVO>() {
                }.getType());
        //endregion

        MarketingTask task = new MarketingTask();

    }

    private void buildScoreTaskOfSelect(CustomerScoreRuleVO vo){

    }
}
