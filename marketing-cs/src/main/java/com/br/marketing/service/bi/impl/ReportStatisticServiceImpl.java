package com.br.marketing.service.bi.impl;

import cn.hutool.core.util.ObjectUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.report.zhongan.ZhongAnControlGroupDTO;
import com.br.marketing.entity.ReportTask;
import com.br.marketing.entity.ReportTaskExample;
import com.br.marketing.enums.report.ReportTaskTypeEnum;
import com.br.marketing.mapper.ReportTaskMapper;
import com.br.marketing.mapper.ZhongAnControlGroupMapper;
import com.br.marketing.service.bi.ReportStatisticService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 众安经营分析报表每日定时生成任务
 */
@Service
@Slf4j
public class ReportStatisticServiceImpl implements ReportStatisticService {

    @Resource
    ReportTaskMapper reportTaskMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    ZhongAnControlGroupMapper zhongAnControlGroupMapper;

    @Resource
    ZhongAnControlGroupServiceImpl zhongAnControlGroupService;

    @Override
    public void action(LocalDateTime actionDateTime, Integer reportType) {
        try {
            LocalDate today = actionDateTime.toLocalDate().minusDays(1);
            String resultDate = today.toString();
            List<Integer> userTypes = new ArrayList<>();
            List<String> zhongAnReportTypeList = new ArrayList<>();
            if (ObjectUtil.isEmpty(reportType)) {
                // 查询是否已存在统计记录
                ReportTaskExample example = new ReportTaskExample();
                example.createCriteria()
                        .andReportTypeIn(Arrays.asList(ReportTaskTypeEnum.BUSINESS_ANALYSIS_ONE_TYPE.getValue(),
                                ReportTaskTypeEnum.BUSINESS_ANALYSIS_SEVEN_TYPE.getValue(),
                                ReportTaskTypeEnum.BUSINESS_ANALYSIS_EIGHT_TYPE.getValue()))
                        .andReportNameLike("%" + resultDate);
                List<ReportTask> reportTasks = reportTaskMapper.selectByExample(example);
                if (reportTasks.size() == 3) {
                    log.warn("众安日新增报表任务已存在！");
                    return;
                }

                zhongAnReportTypeList = Arrays.asList(ReportTaskTypeEnum.BUSINESS_ANALYSIS_ONE_TYPE.getValue().toString(),
                        ReportTaskTypeEnum.BUSINESS_ANALYSIS_SEVEN_TYPE.getValue().toString(),
                        ReportTaskTypeEnum.BUSINESS_ANALYSIS_EIGHT_TYPE.getValue().toString());
                if (ObjectUtil.isEmpty(zhongAnReportTypeList)) {
                    log.warn("众安报表类型为空");
                    return;
                }

                userTypes = zhongAnReportTypeList.stream()
                        .map((String newReportType) -> {
                            switch (newReportType) {
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
            } else {
                switch (reportType) {
                    case 12:
                        userTypes.add(1);
                        zhongAnReportTypeList.add(ReportTaskTypeEnum.BUSINESS_ANALYSIS_ONE_TYPE.getValue().toString());
                        break;
                    case 13:
                        userTypes.add(7);
                        zhongAnReportTypeList.add(ReportTaskTypeEnum.BUSINESS_ANALYSIS_SEVEN_TYPE.getValue().toString());
                        break;
                    case 14:
                        userTypes.add(8);
                        zhongAnReportTypeList.add(ReportTaskTypeEnum.BUSINESS_ANALYSIS_EIGHT_TYPE.getValue().toString());
                        break;
                    default:
                        break;
                }
            }
            if (CollectionUtils.isEmpty(userTypes)) {
                log.warn("报表类型为空");
                return;
            }
            List<ZhongAnControlGroupDTO> zhongAnControlGroupDTOS = zhongAnControlGroupMapper.selectConfigTypeAndDatebI_(userTypes, resultDate);
            // 为填写报表配置，生成默认配置
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

            for (String zhongAnReportType : zhongAnReportTypeList) {
                if (!sqlProcessing(zhongAnReportType, today)) {
                    log.warn("众安经营分析报表任务生成失败");
                    return;
                }
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHONGAN_REPORTEERROR.getCode(),
                    "众安报表定时统计新增任务发生错误！"), e);
        }
    }

    private boolean sqlProcessing(String reportType, LocalDate actionDate) {

        if (ObjectUtil.isEmpty(reportType)) {
            log.warn("reportType不能为空");
            return false;
        }
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> score = new HashMap<>();
        score.put("field", Collections.emptyList());
        score.put("multiHeadField", Collections.emptyList());
        score.put("batchNumber", "");

        Map<String, String> transfer = new HashMap<>();
        transfer.put("isSplit", "1");
        // 获取统计日期
        String resultDate = actionDate.toString();

        transfer.put("statisticDate", resultDate);
        transfer.put("requestStartDate", actionDate.withDayOfMonth(1).toString());
        transfer.put("requestEndDate", resultDate);
        String reportName = "";
        Integer type = null;

        Map<String, Object> upload = new HashMap<>();

        List<Map<String, String>> dimensionsValue = new ArrayList<>();
        Map<String, String> dimensionsValueItem = new HashMap<>();
        dimensionsValueItem.put("code", "0");
        dimensionsValueItem.put("desc", "无分组");
        dimensionsValue.add(dimensionsValueItem);
        upload.put("dimensionsValue", dimensionsValue);

        Map<String, Object> jsonObject = new HashMap<>();
        String apiCode = marketingCommonConfig.getZhongAnReportStatisticApiCode();
        String statisticsScene = "报表统计_众安(".concat(apiCode);

        if ("12".equals(reportType)) {
            reportName = "场景一日统计" + resultDate;
            type = ReportTaskTypeEnum.BUSINESS_ANALYSIS_ONE_TYPE.getValue();
            upload.put("dimensionsField", "defaultNone");
            upload.put("userType", "1");

            jsonObject.put("score", score);
            jsonObject.put("transfer", transfer);
            jsonObject.put("upload", upload);
            jsonObject.put("apiCode", apiCode);
            jsonObject.put("statisticsScene", statisticsScene.concat(")_1场景经营分析报表"));
        } else if ("13".equals(reportType)) {
            reportName = "场景七日统计" + resultDate;
            type = ReportTaskTypeEnum.BUSINESS_ANALYSIS_SEVEN_TYPE.getValue();

            upload.put("dimensionsField", "defaultNone");
            upload.put("userType", "7");

            jsonObject.put("score", score);
            jsonObject.put("transfer", transfer);
            jsonObject.put("upload", upload);
            jsonObject.put("apiCode", apiCode);
            jsonObject.put("statisticsScene", statisticsScene.concat(")_7场景经营分析报表"));
        } else if ("14".equals(reportType)) {
            reportName = "场景八日统计" + resultDate;
            type = ReportTaskTypeEnum.BUSINESS_ANALYSIS_EIGHT_TYPE.getValue();

            upload.put("dimensionsField", "defaultNone");
            upload.put("userType", "8");

            jsonObject.put("score", score);
            jsonObject.put("transfer", transfer);
            jsonObject.put("upload", upload);
            jsonObject.put("apiCode", apiCode);
            jsonObject.put("statisticsScene", statisticsScene.concat(")_8场景经营分析报表"));
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
        // 判断是否已有统计任务
        ReportTaskExample example = new ReportTaskExample();
        example.createCriteria()
                .andReportTypeEqualTo(reportType)
                .andReportNameEqualTo(reportName)
                .andReportRulesEqualTo(jsonString);
        List<ReportTask> reportTasks = reportTaskMapper.selectByExample(example);
        if (!reportTasks.isEmpty()) {
            log.warn("众安日统计任务已存在");
            return;
        }
        // 新增统计任务
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
