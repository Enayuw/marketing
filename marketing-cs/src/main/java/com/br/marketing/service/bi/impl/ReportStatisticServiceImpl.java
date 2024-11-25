package com.br.marketing.service.bi.impl;

import cn.hutool.core.util.ObjectUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.report.zhongan.ZhongAnControlGroupDTO;
import com.br.marketing.entity.ReportTask;
import com.br.marketing.entity.ReportTaskAction;
import com.br.marketing.entity.ReportTaskActionExample;
import com.br.marketing.entity.ReportTaskExample;
import com.br.marketing.enums.report.ReportTaskTypeEnum;
import com.br.marketing.mapper.ReportTaskActionMapper;
import com.br.marketing.mapper.ReportTaskMapper;
import com.br.marketing.mapper.ZhongAnControlGroupMapper;
import com.br.marketing.service.bi.ReportStatisticService;
import com.br.marketing.vo.zhongan.param.ZhongAnControlGroupParam;
import com.br.marketing.vo.zhongan.param.ZhongAnCustomInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  众安经营分析报表每日定时生成任务
 */
@Service
@Slf4j
public class ReportStatisticServiceImpl implements ReportStatisticService {

    @Resource
    ReportTaskMapper reportTaskMapper;

    @Resource
    ReportTaskActionMapper reportTaskActionMapper;

    @Resource
    ZhongAnControlGroupMapper zhongAnControlGroupMapper;

    @Resource
    ZhongAnControlGroupServiceImpl zhongAnControlGroupService;

    @Override
    public void action(String actionDate) {

        String curLocalTime = LocalTime.now().toString();

        ReportTaskExample reportTaskExample = new ReportTaskExample();
        reportTaskExample.createCriteria()
                .andReportTypeEqualTo(18)
                .andStatisticsTypeEqualTo(2)
                .andIsDelEqualTo(1)
                .andStatisticsTimeLessThan(curLocalTime);
        reportTaskExample.setOrderByClause("id desc");
        List<ReportTask> reportTasks = reportTaskMapper.selectByExample(reportTaskExample);
        if(CollectionUtils.isEmpty(reportTasks)){
            log.warn("未获取到报表任务");
            return;
        }

        for (ReportTask reportTask : reportTasks) {
            processReportTask(reportTask, actionDate);
        }
    }


    private void processReportTask(ReportTask reportTaskPO, String actionDate){

        Long reportTaskId = reportTaskPO.getId();
        try {
            ReportTaskActionExample taskActionExample = new ReportTaskActionExample();
            taskActionExample.createCriteria()
                    .andReportTaskIdEqualTo(reportTaskId)
                    .andActionDateEqualTo(actionDate)
                    .andDeleteFlagEqualTo(0);
            taskActionExample.setOrderByClause("create_time desc");
            List<ReportTaskAction> taskActions = reportTaskActionMapper.selectByExample(taskActionExample);
            if (ObjectUtil.isNotEmpty(taskActions)) {
                log.warn("任务执行记录已存在, reportTaskId:{}, actionDate:{}", reportTaskId, actionDate);
                return;
            }
            ReportTaskAction action = new ReportTaskAction();
            action.setReportTaskId(reportTaskId);
            action.setActionStatus(1);
            action.setActionDate(actionDate);
            action.setCreateTime(new Date());
            action.setUpdateTime(new Date());
            int i = reportTaskActionMapper.insertSelective(action);
            if (i < 1) {
                log.warn("新增失败, reportTaskId:{}, actionDate:{}", reportTaskId, actionDate);
                return;
            }

            List<String> zhongAnReportType =
                    Arrays.asList(ReportTaskTypeEnum.BUSINESS_ANALYSIS_ONE_TYPE.getValue().toString(),
                            ReportTaskTypeEnum.BUSINESS_ANALYSIS_SEVEN_TYPE.getValue().toString(),
                            ReportTaskTypeEnum.BUSINESS_ANALYSIS_EIGHT_TYPE.getValue().toString());
            if (ObjectUtil.isEmpty(zhongAnReportType)) {
                log.warn("众安报表类型为空");
                return;
            }

            LocalDate today = LocalDate.now();
            String resultDate = today.minusDays(1).toString();

            List<Integer> userTypes = zhongAnReportType.stream()
                    .map(reportType -> {
                        switch (reportType) {
                            case "12":
                                return 1;
                            case "13":
                                return 7;
                            case "14":
                                return 8;
                            default:
                                log.warn("未匹配到报表类型");
                                return null;
                        }
                    })
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(userTypes)) {
                log.warn("报表类型为空");
                return;
            }
            List<ZhongAnControlGroupDTO> zhongAnControlGroupDTOS = zhongAnControlGroupMapper.selectConfigTypeAndDatebI_(userTypes, resultDate);
            if ((ObjectUtil.isEmpty(zhongAnControlGroupDTOS) || zhongAnControlGroupDTOS.size() < 1)) {
                log.warn("众安报表配置为空");
                ZhongAnControlGroupParam param = new ZhongAnControlGroupParam();
                param.setReportDate(resultDate);
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    String jsonData1 = "[{\"constituencies\":1,\"totalNum\":0,\"incomingNum\":0,\"approversNum\":0}," +
                            "{\"constituencies\":2,\"totalNum\":0,\"incomingNum\":0,\"approversNum\":0}]";
                    String jsonData7 = "[{\"constituencies\":3,\"payPassRate\":0,\"lendersSucAmount\":0}," +
                            "{\"constituencies\":4,\"totalNum\":0,\"loginRate\":0,\"incomingNum\":0,\"approversNum\":0," +
                            "\"approvalAvailable\":0,\"applyPayNum\":0,\"payPassRate\":0,\"lendersSucNum\":0,\"lendersSucAmount\":0}]";
                    String jsonData8 = "[{\"constituencies\":5,\"totalNum\":0,\"incomingNum\":0,\"approversNum\":0}]";

                    param.setUserType1(objectMapper.readValue(jsonData1, new TypeReference<List<ZhongAnCustomInfo>>() {}));
                    param.setUserType7(objectMapper.readValue(jsonData7, new TypeReference<List<ZhongAnCustomInfo>>() {}));
                    param.setUserType8(objectMapper.readValue(jsonData8, new TypeReference<List<ZhongAnCustomInfo>>() {}));
                    zhongAnControlGroupService.saveCustomInfo(param);
                } catch (Exception e) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                            "默认统计生成报表配置错误"), e);
                }

            }

            for (String reportType : zhongAnReportType) {
                if (!sqlProcessing(reportType)) {
                    log.warn("众安经营分析报表任务生成失败");
                    return;
                }
            }
            ReportTaskActionExample actionExample = new ReportTaskActionExample();
            actionExample.createCriteria()
                    .andReportTaskIdEqualTo(reportTaskId)
                    .andActionDateEqualTo(actionDate)
                    .andDeleteFlagEqualTo(0)
                    .andActionStatusEqualTo(1);
            List<ReportTaskAction> reportTaskActions = reportTaskActionMapper.selectByExample(actionExample);
            for (ReportTaskAction taskAction : reportTaskActions) {
                taskAction.setId(taskAction.getId());
                taskAction.setActionStatus(2);
                reportTaskActionMapper.updateByPrimaryKeySelective(taskAction);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHONGAN_REPORTEERROR.getCode(),
                            "众安报表定时统计新增任务发生错误！"), e);
        }
    }

    private boolean sqlProcessing(String reportType) {

        if (ObjectUtil.isEmpty(reportType)) {
            log.warn("reportType不能为空");
            return false;
        }
        ObjectMapper objectMapper = new ObjectMapper();

        // 创建 score 对象
        Map<String, Object> score = new HashMap<>();
        score.put("field", Collections.emptyList());
        score.put("multiHeadField", Collections.emptyList());
        score.put("batchNumber", "");

        Map<String, String> transfer = new HashMap<>();
        // 创建 transfer 对象
        transfer.put("isSplit", "1");
        // 获取当前日期
        LocalDate today = LocalDate.now();
        String resultDate = today.minusDays(1).toString();

        transfer.put("statisticDate", resultDate);
        transfer.put("requestStartDate", today.withDayOfMonth(1).toString());
        transfer.put("requestEndDate", resultDate);
        String reportName = "";
        Integer type = null;

        // 创建 upload 对象
        Map<String, Object> upload = new HashMap<>();

        // 创建 dimensionsValue 列表
        List<Map<String, String>> dimensionsValue = new ArrayList<>();
        Map<String, String> dimensionsValueItem = new HashMap<>();
        dimensionsValueItem.put("code", "0");
        dimensionsValueItem.put("desc", "无分组");
        dimensionsValue.add(dimensionsValueItem);
        upload.put("dimensionsValue", dimensionsValue);

        // 创建最终 JSON 对象
        Map<String, Object> jsonObject = new HashMap<>();

        if ("12".equals(reportType)) {
            reportName = "场景一日统计" + today.toString();
            type = ReportTaskTypeEnum.BUSINESS_ANALYSIS_ONE_TYPE.getValue();

            upload.put("dimensionsField", "defaultNone");
            upload.put("userType", "1");

            jsonObject.put("score", score);
            jsonObject.put("transfer", transfer);
            jsonObject.put("upload", upload);
            jsonObject.put("apiCode", "3710048");
            jsonObject.put("statisticsScene", "报表统计_众安(3710048)_1场景经营分析报表");
        } else if ("13".equals(reportType)) {
            reportName = "场景七日统计" + today.toString();
            type = ReportTaskTypeEnum.BUSINESS_ANALYSIS_SEVEN_TYPE.getValue();

            upload.put("dimensionsField", "defaultNone");
            upload.put("userType", "7");

            jsonObject.put("score", score);
            jsonObject.put("transfer", transfer);
            jsonObject.put("upload", upload);
            jsonObject.put("apiCode", "3710048");
            jsonObject.put("statisticsScene", "报表统计_众安(3710048)_7场景经营分析报表");
        } else if ("14".equals(reportType)) {
            reportName = "场景八日统计" + today.toString();
            type = ReportTaskTypeEnum.BUSINESS_ANALYSIS_EIGHT_TYPE.getValue();

            upload.put("dimensionsField", "defaultNone");
            upload.put("userType", "8");

            jsonObject.put("score", score);
            jsonObject.put("transfer", transfer);
            jsonObject.put("upload", upload);
            jsonObject.put("apiCode", "3710048");
            jsonObject.put("statisticsScene", "报表统计_众安(3710048)_8场景经营分析报表");
        }

        try {
            String jsonString = objectMapper.writeValueAsString(jsonObject);
            addReportTask(jsonString, type, reportName);
        } catch (JsonProcessingException e) {
            log.warn("众安日统计写入规则错误", e);
            return false;
        }

        return true;
    }

    public void addReportTask(String jsonString, Integer reportType, String reportName) {
        ReportTask reportTask = new ReportTask();
        reportTask.setReportName(reportName);
        reportTask.setReportRules(jsonString);
        reportTask.setReportType(reportType);
        reportTask.setStatus(0);
        reportTask.setCreateTime(new Date());
        reportTask.setUpdateTime(new Date());

        reportTaskMapper.insertSelective(reportTask);
    }



}
