package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.OffLineCallBackDTO;
import com.br.marketing.dto.TaskExtendExtendFieldDTO;
import com.br.marketing.dto.TaskSelectSaveDTO;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingSyncInfoExample;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.entity.MarketingSyncReportExample;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MarketingTaskAutoBuildConfig;
import com.br.marketing.entity.MarketingTaskAutoBuildConfigExample;
import com.br.marketing.entity.MarketingTaskExtend;
import com.br.marketing.entity.MarketingTaskResultPreview;
import com.br.marketing.entity.MarketingTaskResultPreviewExample;
import com.br.marketing.entity.MarketingTaskUserType;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.entity.TaskBatchnumberPre;
import com.br.marketing.entity.TaskBatchnumberPreExample;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.mapper.MarketingTaskAutoBuildConfigMapper;
import com.br.marketing.mapper.MarketingTaskExtendMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.MarketingTaskResultPreviewMapper;
import com.br.marketing.mapper.MarketingTaskUserTypeMapper;
import com.br.marketing.mapper.ScoreRuleConfigMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.mapper.TaskBatchnumberPreMapper;
import com.br.marketing.mapper.TaskStatusMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IApiToDbService;
import com.br.marketing.service.IDynamicSqlService;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.IRuleConfigService;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.CustomerScoreRuleVO;
import com.br.marketing.vo.MarketingTaskVO;
import com.br.marketing.vo.ResultPreviewVO;
import com.br.marketing.vo.StatisticsDataDayVO;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 跑人任务接口实现类
 * @Date 2022/5/10 11:57 AM
 * ------------------------------
 */
@Service
@Slf4j
public class MarketingTaskServiceImpl implements MarketingTaskService {

    static String warnTemp = "apiCode：%s,数据id：%s,错误信息：%s";

    final static DateTimeFormatter ymdhms = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    final static DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    IDynamicSqlService iDynamicSqlService;

    @Resource
    MarketingSyncInfoMapper syncInfoMapper;

    @Autowired
    IApiToDbService iApiToDbService;

    @Autowired
    SoleStrategyService soleStrategyService;

    @Resource
    MarketingTaskMapper marketingTaskMapper;

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;
    @Resource
    MarketingTaskUserTypeMapper marketingTaskUserTypeMapper;

    @Resource
    TaskBatchnumberPreMapper taskBatchnumberPreMapper;

    @Autowired
    private RedisChgService redisChgService;

    @Resource
    private ScoreRuleConfigMapper scoreRuleConfigMapper;

    @Autowired
    private IRuleConfigService iRuleConfigService;

    @Resource
    private MarketingSyncReportMapper marketingSyncReportMapper;

    @Resource
    MarketingTaskResultPreviewMapper marketingTaskResultPreviewMapper;

    static final String judgmentRegex = "<=|>=|=|>|<";

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    @Autowired
    RabbitMqProducter producter;

    @Autowired
    IProductResultSimpleService iProductResultSimpleService;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Autowired
    EntityOptServiceImpl entityOptService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    TaskStatusMapper taskStatusMapper;

    @Autowired
    MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    @Autowired
    MarketingTaskAutoBuildConfigMapper buildConfigMapper;

    @Override
    public PageResultReturn list(int current, int size, String search, Integer status, String createTimeStart, String createTimeEnd,
                                 String updateTimeStart, String updateTimeEnd, Integer taskStatus, Integer execType) {

        if (StringUtils.isNotEmpty(createTimeEnd)) {
            createTimeEnd = DateUtils.format(addDay(createTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (StringUtils.isNotEmpty(updateTimeEnd)) {
            updateTimeEnd = DateUtils.format(addDay(updateTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (StringUtils.isNotEmpty(search) && search.contains("_")) {
            search = search.replace("_", "\\_");
        }

        PageHelper.startPage(current, size);
        List<MarketingTaskVO> fastTaskRuleListVOS = marketingTaskMapper.selectList(search, status,
                createTimeStart, createTimeEnd, updateTimeStart, updateTimeEnd, taskStatus, null, execType);

        return PageResultReturn.setPageResult(fastTaskRuleListVOS, current, size);
    }

    @Override
    public ApiResult<Boolean> editPriority(String id, Integer priority) {
        MarketingTask marketingTask = new MarketingTask();
        marketingTask.setPriority(priority);
        marketingTask.setId(Long.valueOf(id));
        marketingTaskMapper.updateByPrimaryKeySelective(marketingTask);
        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public boolean updateStatusById(String id, Integer status) {
        try {
            MarketingTask marketingTask = new MarketingTask();
            marketingTask.setStatus(status);
            marketingTask.setId(Long.valueOf(id));
            marketingTaskMapper.updateByPrimaryKeySelective(marketingTask);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public MarketingTaskVO getTask(String id) {
        List<MarketingTaskVO> marketingTaskVO = marketingTaskMapper.selectList(null, null, null, null, null, null, null, id, null);
        if (marketingTaskVO.size() > 0) {
            return marketingTaskVO.get(0);
        }
        return null;
    }

    @Override
    public Long getTaskPercent(String hisFileId, String id) {
        //3，1 正在跑分
        //0，待推送
        //2，已完成
        String status = marketingTaskMapper.selectHisFileById(hisFileId);
        if ("2".equals(status)) {
            return Long.valueOf(100);
        } else if ("3,1".contains(status)) {
            MarketingTask marketingTask = marketingTaskMapper.selectByPrimaryKey(Long.valueOf(id));
            long taskNumber = Long.valueOf(marketingTask.getTaskNumber());
            String s = redisChgService.get(RedisKeyConstant.taskScoreNum + ":" + hisFileId);
            if (s == null) {
                s = "0";
            }
            return Long.valueOf(s) * 100 / taskNumber;
        }
        return Long.valueOf(0);
    }


    @Override
    public List<ScoreRuleConfig> getScoreRules(String apiCode) {
        List<ScoreRuleConfig> list = scoreRuleConfigMapper.getScoreRules(apiCode);
        return list;
    }


    private Date addDay(String date, Integer addDays, String format) {
        Calendar c = Calendar.getInstance();
        Date time = null;
        try {
            Date endTime = DateUtils.parse(date, format);
            c.setTime(endTime);
            c.add(Calendar.DAY_OF_MONTH, addDays);
            time = c.getTime();
        } catch (ParseException e) {
            log.error("date:{} is error", date, e);
        }
        return time;
    }

    @Override
    public void addTaskPercent(Long fileId, Long number) {
        String key = RedisKeyConstant.taskScoreNum.concat(":").concat(fileId.toString());
        redisChgService.incrBy(key, number);
    }

    @Override
    public Result<Long> buildScoreTaskOfAutoBuild(CustomerScoreRuleVO vo) {
        // 获取当前时间和日期
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDate nowDate = LocalDate.now();
        LocalDate plusDays = nowDate.plusDays(1);
        Date nowDateStart = Date.from(nowDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date nowDateEnd = Date.from(plusDays.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // 解析和构建有效时间
        String startTime = vo.getStartTime();
        String validTimeStr = nowDate.format(ymd) + " " + startTime + ":00";
        LocalDateTime validTime = LocalDateTime.parse(validTimeStr, ymdhms);
        // 判断是否达到开始时间
        if (nowTime.isBefore(validTime)) {
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setMessage("该规则没达到开始时间，暂不生成任务");
        }

        String apiCode = vo.getApiCode();

        Integer execType = vo.getExecType();
        // 每个任务的周期
        if (execType == 3 && vo.getAutoBuild() == 1) {
            MarketingTaskAutoBuildConfigExample buildConfigExample = new MarketingTaskAutoBuildConfigExample();
            buildConfigExample.createCriteria().andIsDeletedEqualTo(0)
                    .andScoreRuleIdEqualTo(vo.getId().intValue())
                    .andCloseDateLessThanOrEqualTo(vo.getCycleEndDay());
            List<MarketingTaskAutoBuildConfig> autoBuildConfigList = buildConfigMapper.selectByExample(buildConfigExample);
            if (CollectionUtils.isEmpty(autoBuildConfigList)) {
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setMessage("该周期生成任务规则，已过期或已删除");
            }

            MarketingTaskAutoBuildConfig autoBuildConfig = autoBuildConfigList.get(0);
            Integer cycleDay = autoBuildConfig.getCycleDay();
            if (cycleDay == null || cycleDay == 0) {
                log.error(String.format("该周期生成任务规则，没有配置周期天数，任务id：%d", vo.getId()));
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }

            long days;
            try {
                days = DateHelper.getDistanceDays(nowDate.toString(), autoBuildConfig.getStartDate());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }

            if (days % cycleDay == 0){
                vo.setConditionInfo(autoBuildConfig.getDataCondition());
                vo.setStartDate(autoBuildConfig.getStartDate());
                vo.setStartTime(autoBuildConfig.getStartTime());

                List<String> userTypeList = new ArrayList<>();

                if (StringUtils.isNotEmpty(autoBuildConfig.getSyncReportId())) {
                    List<Long> syncReportIds = Arrays.stream(autoBuildConfig.getSyncReportId()
                            .split(",")).map(Long::new).collect(Collectors.toList());

                    MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
                    reportExample.createCriteria().andIdIn(syncReportIds);
                    List<MarketingSyncReport> marketingSyncReports = marketingSyncReportMapper.selectByExample(reportExample);
                    // 查询符合跑分数据的场景

                    for (MarketingSyncReport marketingSyncReport : marketingSyncReports) {
                        userTypeList.add(marketingSyncReport.getUserType());
                    }
                }

                vo.setBuildType(2);
                Result<Long> result = buildScoreTaskOfSelect(vo, userTypeList);
                if (! ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    return new Result<>().setCode(ResultCode.FAIL.getValue());
                }
            }
        }

        // 每日定时
        if (execType == 4) {
            //region 条件解析userType配置
            Result<String> conditionRes = soleStrategyService.analysisCondition(vo.getConditionInfo());
            if (!ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())) {
                String errorMsg = String.format("自动规则生成任务 数据范围解析有误;" + warnTemp, vo.getApiCode(), vo.getId(), conditionRes.getMessage());
                log.warn(errorMsg);
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg);
            }

            // 是否存在T日上传完成、T-1未上传完成的情况
            MarketingSyncInfoExample syncInfoIngExample = new MarketingSyncInfoExample();
            syncInfoIngExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andCreateTimeGreaterThanOrEqualTo(nowDateStart)
                    .andCreateTimeLessThan(nowDateEnd)
                    .andStatusEqualTo(1);
//                .andIsUploadEqualTo(1);
            int isUploadCount = syncInfoMapper.countByExample(syncInfoIngExample);
            if (isUploadCount > 0) {
                String errorMsg = String.format("自动规则生成任务 上传数据还未解析完成" + warnTemp, vo.getApiCode(), vo.getId(), "");
                log.warn(errorMsg);
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg);
            }

            // 是否叠加有效期数据
            Integer isStackValidity = vo.getIsStackValidity();
            vo.setBuildType(2);
            if (isStackValidity == 0) {
                Long minId = syncInfoMapper
                        .getMinIdByRuleScoreWithDate(apiCode, nowDate.toString(), validTimeStr, conditionRes.getData());

                if (minId == null || minId <= 0) {
                    return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("没有符合条件的数据");
                }

                String time = LocalDateTime.parse(validTimeStr, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                // 生成跑分批次号
                Result<String> batchNumberRes = iApiToDbService.buildBatchNumber(apiCode
                        , vo.getId().toString(), vo.getRuleNameShort()
                        , time, null);
                if (!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())) {
                    String errorMsg = String.format("自动规则生成任务 批次号生成错误" + warnTemp, vo.getApiCode(), vo.getId(), "");
                    log.warn(errorMsg);
                    return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg);
                }
                String batchNumber = batchNumberRes.getData();

                // 查询符合跑分数据的场景
                List<String> userTypeList = syncInfoMapper
                        .queryUserTypeListWithDatetikv_(apiCode, nowDate.toString(), validTimeStr, conditionRes.getData());

                //跑分条件转化
                Result<String> conditionTransferRes = soleStrategyService.analysisTransferConditions(vo.getConditionInfo(), nowDate.toString(),
                        validTimeStr);

                return getResult(vo, conditionTransferRes, apiCode, batchNumber, nowDate, userTypeList);
            }

            if (isStackValidity == 1) {
                List<MarketingDataValidConfig> configList = marketingDataValidConfigMapper
                        .findListByApiCodeAndUserTypeSetPagetikv_(
                                apiCode, nowDate.toString(), null, null, null);

                Long minId = syncInfoMapper
                        .getMinIdByRuleScoreWithValidConfig(apiCode, configList,conditionRes.getData(), validTimeStr);

                if (minId == null || minId <= 0) {
                    return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("没有符合条件的数据");
                }

                String time = LocalDateTime.parse(validTimeStr, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                // 生成跑分批次号
                Result<String> batchNumberRes = iApiToDbService.buildBatchNumber(apiCode
                        , vo.getId().toString(), vo.getRuleNameShort()
                        , time, null);
                if (!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())) {
                    String errorMsg = String.format("自动规则生成任务 批次号生成错误" + warnTemp, vo.getApiCode(), vo.getId(), "");
                    log.warn(errorMsg);
                    return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg);
                }
                String batchNumber = batchNumberRes.getData();
                // 查询符合跑分数据的场景
                List<String> userTypeList = syncInfoMapper
                        .queryUserTypeListWithValidConfigtikv_(apiCode, configList, conditionRes.getData(), validTimeStr);

                //跑分条件转化
                Result<String> conditionTransferRes = soleStrategyService.analysisTransferConditionsByValidConfig(vo.getConditionInfo(), configList,
                        validTimeStr);

                return getResult(vo, conditionTransferRes, apiCode, batchNumber, nowDate, userTypeList);
            }
        }

        return null;
    }

    private Result getResult(CustomerScoreRuleVO vo, Result<String> conditionTransferRes, String apiCode,
                             String batchNumber, LocalDate nowDate, List<String> userTypeList) {
        if (!ResultCode.SUCCESS.getValue().equals(conditionTransferRes.getCode())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("数据条件转化错误");
        }

        String transferData = conditionTransferRes.getData();
        //获取查询sql条件
        Result<List<String>> transferWhereRes = soleStrategyService.analysisConditions(transferData);
        if (!ResultCode.SUCCESS.getValue().equals(transferWhereRes.getCode())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(transferWhereRes.getMessage());
        }
        StringBuilder showStr = new StringBuilder();
        Integer count = 0;
        for (int i = 0; i < transferWhereRes.getData().size(); i++) {
            String datum = transferWhereRes.getData().get(i);
            String s = whereSqlToShow(datum);
            Integer integer = iDynamicSqlService.countByRuleScoreWithDate(apiCode, datum);
            count += integer;
            showStr.append(s).append("总数据").append(integer.toString());
            if (i < transferWhereRes.getData().size() - 1) {
                showStr.append(",");
            }
        }
        vo.setConditionInfo(transferData);
        return saveTask(apiCode, batchNumber, vo, nowDate.toString(), count, 1, showStr.toString(), userTypeList);
    }

    @Override
    public Result<Long> buildScoreTaskOfAuto(CustomerScoreRuleVO vo) {
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
            } else {
                sTimeStr = nowData.format(ymd).concat(" 00:00:00");
                eTimeStr = validTime.format(ymdhms);
            }
        } else {
            sTimeStr = nowData.minusDays(1L).format(ymd).concat(" 00:00:00");
            eTimeStr = validTime.minusDays(1L).format(ymdhms);

        }
        taskStart = LocalDate.now().format(ymd);
        taskEnd = LocalDate.now().plusDays(1L).format(ymd);
        String sDate = LocalDateTime.parse(sTimeStr, ymdhms).format(ymd);
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
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(String.format("概规则的历史数据不予生成 规则id：%d", vo.getId()));
        }
        //endregion

        //region 条件解析
        Result<String> conditionRes = soleStrategyService.analysisCondition(vo.getConditionInfo());
        if (!ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())) {
            String errorMsg = String.format("自动规则生成任务 数据范围解析有误;" + warnTemp, vo.getApiCode(), vo.getId(), conditionRes.getMessage());
            log.warn(errorMsg);
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg);
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
            String errorMsg = String.format("自动规则生成任务 上传数据还未解析完成" + warnTemp, vo.getApiCode(), vo.getId(), "");
            log.warn(errorMsg);
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg);
        }
        // 跑分批次号
        String number = "";

        Long minId = syncInfoMapper
                .getMinIdByRuleScoreWithDate(apiCode, sDate, eTimeStr, conditionRes.getData());
        if (minId != null && minId > 0) {
            String time = LocalDateTime.parse(eTimeStr, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            // 生成跑分批次号
            Result<String> batchNumberRes = iApiToDbService.buildBatchNumber(apiCode
                    , vo.getId().toString(), vo.getRuleNameShort()
                    , time, null);
            if (!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())) {
                String errorMsg = String.format("自动规则生成任务 批次号生成错误" + warnTemp, vo.getApiCode(), vo.getId(), "");
                log.warn(errorMsg);
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg);
            }
            number = batchNumberRes.getData();
        } else {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("没有符合条件的数据");
        }
        //endregion

        // 查询符合跑分数据的场景
        List<String> userTypeList = syncInfoMapper
                .queryUserTypeListWithDatetikv_(apiCode, sDate, eTimeStr, conditionRes.getData());

        //跑分条件转化
        Result<String> conditionTransferRes = soleStrategyService.analysisTransferConditions(vo.getConditionInfo(), sDate, eTimeStr);

        if (!ResultCode.SUCCESS.getValue().equals(conditionTransferRes.getCode())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("数据条件转化错误");
        }

        String transferData = conditionTransferRes.getData();
        //获取查询sql条件
        Result<List<String>> transferWhereRes = soleStrategyService.analysisConditions(transferData);
        if (!ResultCode.SUCCESS.getValue().equals(transferWhereRes.getCode())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(transferWhereRes.getMessage());
        }
        StringBuilder showStr = new StringBuilder();
        Integer count = 0;
        for (int i = 0; i < transferWhereRes.getData().size(); i++) {
            String datum = transferWhereRes.getData().get(i);
            String s = whereSqlToShow(datum);
            Integer integer = iDynamicSqlService.countByRuleScoreWithDate(apiCode, datum);
            count += integer;
            showStr.append(s).append("总数据" + integer);
            if (i < transferWhereRes.getData().size() - 1) {
                showStr.append(",");
            }
        }
        vo.setConditionInfo(transferData);
        return saveTask(apiCode, number, vo, taskStart, count, 1, showStr.toString(), userTypeList);
    }

    @Override
    public Result<Long> buildScoreTaskOfSelect(CustomerScoreRuleVO vo, List<String> userTypeList) {
        Boolean isVer = new Integer(1).equals(vo.getIsOrNoScoreVer());
        String apiCode = vo.getApiCode();
        Result<List<String>> listResult = soleStrategyService.analysisConditions(vo.getConditionInfo());
        if (!ResultCode.SUCCESS.getValue().equals(listResult.getCode())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(listResult.getMessage());
        }

        List<String> data = listResult.getData();

        Integer count = 0;
        Integer preMaxNum = vo.getDataLimit() != null && vo.getDataLimit() > 0 ? vo.getDataLimit() : 500;
        StringBuilder showStr = new StringBuilder();
        // 是否需要根据ConditionInfos获取多条件下的场景
        boolean userTypeFromConditionInfosFlag = false;
        if(null == userTypeList || userTypeList.size()<1){
            userTypeFromConditionInfosFlag = true;
            userTypeList = new ArrayList<>();
        }
        for (int i = 0; i < data.size(); i++) {
            if (isVer && preMaxNum <= 0) {
                continue;
            }
            String whereStr = data.get(i);
            String s = whereSqlToShow(whereStr);
            Integer integer = iDynamicSqlService.countByRuleScoreWithDate(apiCode, whereStr);
            if (isVer) {
                integer = integer >= preMaxNum ? preMaxNum : integer;
                preMaxNum = preMaxNum - integer;
            }
            count += integer;
            showStr.append(s).append("总数据").append(integer.toString());
            if (i < data.size() - 1) {
                showStr.append(",");
            }
            if(userTypeFromConditionInfosFlag){
                List<String> userTypeByList;
                // 查询符合跑分数据的场景
                userTypeByList = syncInfoMapper
                        .queryUserTypeListWithDatetikv_(apiCode, null, null, whereStr);
                if(null != userTypeByList && userTypeByList.size() > 0){
                    userTypeList.addAll(userTypeByList);
                }
            }
        }
        // 去重
        userTypeList = userTypeList.stream().distinct().collect(Collectors.toList());
        if (count <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该apicode的统计记录失真，请更新该apicode所选的数据统计记录");
        }

        String number = "";
        if (count > 0) {
            if (Objects.equals(vo.getBuildType(), 2)) {
                // 生成跑分批次号
                String validTimeStr = LocalDate.now() + " " + vo.getStartTime() + ":00";
                String time = LocalDateTime.parse(validTimeStr, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                Result<String> batchNumberRes = iApiToDbService.buildBatchNumber(apiCode
                        , vo.getId().toString(), vo.getRuleNameShort()
                        , time, null);
                if (!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())) {
                    String errorMsg = String.format("自动规则生成任务 批次号生成错误" + warnTemp, vo.getApiCode(), vo.getId(), "");
                    log.warn(errorMsg);
                    return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg);
                }
                number = batchNumberRes.getData();
            } else {
                String concatTime = vo.getStartDate().concat(" ").concat(vo.getStartTime() + ":00");
                String time = LocalDateTime.parse(concatTime, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                number = createMarketingTaskBatchNumber(apiCode, time);
            }
        }
        return saveTask(apiCode, number, vo, vo.getStartDate(), count, 2, showStr.toString(), userTypeList);

    }

    @Override
    public Result<List<Long>> saveTaskSelect(TaskSelectSaveDTO dto) {
        List<Long> resIds = new ArrayList<>();
        // 查询符合跑分数据的场景
        List<String> userTypeList = new ArrayList<>();

        Result<List<CustomerScoreRuleVO>> scoreConfigNow = iRuleConfigService.getScoreConfigNow(dto.getRuleIds());
        AssertResult.assertResult(scoreConfigNow);
        String conditionInfo = getConditionInfo(dto.getDataIdDesc(), userTypeList);
        for (CustomerScoreRuleVO datum : scoreConfigNow.getData()) {

            // 每个任务的周期：校验该配置是否已存在
            if (datum.getExecType() == 3) {
                return buildCycleTask(dto.getTaskDate(),dto.getTaskTime(), dto.getDataIdDesc(),datum, conditionInfo);
            }

            datum.setConditionInfo(conditionInfo);
            datum.setStartDate(dto.getTaskDate());
            datum.setStartTime(dto.getTaskTime());
            if (new Integer(1).equals(dto.getIsOrNoScoreVer())) {
                datum.setExecType(2);
                datum.setIsOrNoScoreVer(dto.getIsOrNoScoreVer());
                datum.setDataLimit(dto.getDataLimit());
            }
            datum.setBuildType(1);

            Result<Long> result = buildScoreTaskOfSelect(datum, userTypeList);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                resIds.add(result.getData());
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(resIds);
    }

    @Override
    public Result buildCycleTask(String startDate, String startTime, List<Long> syncReportIds, CustomerScoreRuleVO datum, String conditionInfo) {
        MarketingTaskAutoBuildConfigExample example = new MarketingTaskAutoBuildConfigExample();
        example.createCriteria().andIsDeletedEqualTo(0).andScoreRuleIdEqualTo(datum.getId().intValue())
                .andDataConditionEqualTo(conditionInfo);

        List<MarketingTaskAutoBuildConfig> buildConfigList = buildConfigMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(buildConfigList)) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该周期任务已存在，不能重复生成!");
        }

        // 第一次生成周期任务
        MarketingTaskAutoBuildConfig buildConfig = new MarketingTaskAutoBuildConfig();
        buildConfig.setScoreRuleId(datum.getId().intValue());
        buildConfig.setSyncReportId(Joiner.on(",").join(syncReportIds));
        buildConfig.setDataCondition(conditionInfo);
        buildConfig.setStartDate(startDate);
        buildConfig.setStartTime(startTime);
        buildConfig.setCloseDate(datum.getCycleEndDay());
        buildConfig.setCycleDay(datum.getCycleDay());
        int insert = buildConfigMapper.insertSelective(buildConfig);

        ScoreRuleConfig scoreRuleConfig = new ScoreRuleConfig();
        scoreRuleConfig.setAutoBuild(1);
        scoreRuleConfig.setId(datum.getId());
        scoreRuleConfig.setUpdateTime(new Date());
        int update = scoreRuleConfigMapper.updateByPrimaryKeySelective(scoreRuleConfig);

        if (insert > 0 && update > 0) {
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    /**
     * 根据前端选中的跑分数据id选择
     * @param ids 跑分数据ID
     * @param userTypeList 跑分数据id对应的场景值
     * @return java.lang.String 返回条件
     */
    private String getConditionInfo(List<Long> ids, List<String> userTypeList) {
        MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
        reportExample.createCriteria().andIdIn(ids);
        List<MarketingSyncReport> marketingSyncReports = marketingSyncReportMapper.selectByExample(reportExample);
        JSONArray resObj = new JSONArray();
        marketingSyncReports.forEach(t -> {
            JSONObject simpleCondition = new JSONObject();
            JSONArray simpleConditionDetail = new JSONArray();
            JSONObject jsonDate = new JSONObject();
            JSONObject jsonUserType = new JSONObject();
            simpleConditionDetail.add(jsonDate);
            simpleConditionDetail.add(jsonUserType);

            simpleCondition.put("logicalOperation", "and");
            simpleCondition.put("operationFactor", simpleConditionDetail);

            jsonDate.put("fieldName", "appletDate");
            jsonDate.put("fieldValue", t.getAppletDate());
            jsonDate.put("operation", "=");

            jsonUserType.put("fieldName", "userType");
            jsonUserType.put("fieldValue", t.getUserType());
            jsonUserType.put("operation", "=");
            userTypeList.add(t.getUserType());
            resObj.add(simpleCondition);
        });
        return JSON.toJSONString(resObj);
    }

    /**
     * 保存任务
     * @param apiCode apiCode
     * @param batchNumber 预生成的跑分批次
     * @param ruleVO 跑分规则配置
     * @param taskStart 任务的开始时间
     * @param preNum 按照条件查询的总条数
     * @param conditionType 1-自动；2-手动
     * @param showDataStr
     * @param userTypeList 本次跑分数据对应的场景集合
     * @return com.br.marketing.common.commondto.Result<java.lang.Long> b_marketing_task 表中唯一主键
     */
    private Result<Long> saveTask(String apiCode, String batchNumber
            , CustomerScoreRuleVO ruleVO, String taskStart
            , Integer preNum, Integer conditionType, String showDataStr, List<String> userTypeList) {

        MarketingTask hasTask = marketingTaskMapper.getByBatchNumber(batchNumber);
        if (hasTask != null) {
            return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(hasTask.getId());
        }

        //此逻辑暂时去掉，后期优化 需要和前端配合修改
//        if (Integer.valueOf(4).equals(ruleVO.getExecType()) || Integer.valueOf(3).equals(ruleVO.getExecType())) {
//            String closeDate = "";
//            if (Integer.valueOf(4).equals(ruleVO.getExecType())) {
//                MarketingTask task1 = marketingTaskMapper.selectCycleTopByApiCode(apiCode);
//                if (task1 != null) {
//                    closeDate = task1.getCloseDate();
//                } else {
//                    closeDate = ruleVO.getCycleEndDay();
//                }
//            } else if (Integer.valueOf(3).equals(ruleVO.getExecType())) {
//                closeDate = ruleVO.getCycleEndDay();
//            }
//            LocalDate closeDay = LocalDate.parse(closeDate, ymd);
//            if (closeDay.compareTo(LocalDate.now()) <= 0) {
//                return new Result<Long>().setCode(ResultCode.FAIL.getValue()).setMessage("规则的结束时间小于等于当前时间");
//            }
//        }

        //region 处理task
        MarketingTask task = new MarketingTask();
        task.setApiCode(apiCode);
        task.setBatchNumber(batchNumber);
        task.setMonitorStatus(1);
        task.setStatus(1);
        task.setTaskNumber(preNum);
        task.setStartTime(ruleVO.getStartTime());
        task.setTaskType(ruleVO.getTaskType());
        task.setStrategyId(ruleVO.getStrategyId());
        task.setProductInfo(ruleVO.getProductInfo());
        task.setFileName(String.format("%s_%s", ruleVO.getId().toString(), ruleVO.getRuleNameShort()));
        task.setCusBatch(ruleVO.getId().toString());
        task.setMonitorType(ruleVO.getExecType());
        task.setIsOnline(ruleVO.getIsOnline());
        if(ruleVO.getPriority() != null){
            task.setPriority(ruleVO.getPriority());
        }
        if (Integer.valueOf(4).equals(ruleVO.getExecType())) {
            task.setMonitorType(4);
        } else if (Integer.valueOf(3).equals(ruleVO.getExecType())) {
            task.setMonitorType(3);
        }
        String taskEnd = LocalDate.parse(taskStart, ymd)
                .plusDays(1L).format(ymd);
        task.setStartDate(taskStart);
        task.setCloseDate(taskEnd);
        task.setCreateTime(LocalDateTime.now().format(ymdhms));
        task.setContextId(iApiToDbService.getTaskContextId());
        marketingTaskMapper.insertSelective(task);
        //endregion

        //region跑分扩展表
        MarketingTaskExtend taskExtend = new MarketingTaskExtend();
        taskExtend.setApiCode(apiCode);
        taskExtend.setTaskId(Long.valueOf(task.getId()));
        taskExtend.setCreateTime(new Date());
        taskExtend.setExtendShowTitle(ruleVO.getBaseInfo());
        taskExtend.setRuleId(ruleVO.getId());
        taskExtend.setStrategyProductJson(ruleVO.getStrategyProductJson());
        taskExtend.setDataCondition(ruleVO.getConditionInfo());
        taskExtend.setConditionType(conditionType);
        taskExtend.setConditionInfoShow(showDataStr);
        //跑分扩展信息 3K加密方式
        TaskExtendExtendFieldDTO taskExtendExtendFieldDTO = new TaskExtendExtendFieldDTO().setThreekEncryptType(ruleVO.getThreekEncryptType());
        //规则验证保存验证条数
        if (new Integer(1).equals(ruleVO.getIsOrNoScoreVer())) {
            taskExtendExtendFieldDTO.setDataLimit(ruleVO.getDataLimit());
        }
        taskExtend.setExtendConfigInfo(JSON.toJSONString(taskExtendExtendFieldDTO));
        marketingTaskExtendMapper.insertSelective(taskExtend);
        //endregion
        userTypeList.stream().forEach((String t) -> {
            MarketingTaskUserType marketingTaskUserType = new MarketingTaskUserType();
            marketingTaskUserType.setApiCode(apiCode);
            marketingTaskUserType.setBatchNumber(batchNumber);
            marketingTaskUserType.setUserType(t);
            marketingTaskUserType.setCreateTime(new Date());
            marketingTaskUserTypeMapper.insert(marketingTaskUserType);
        });

        //region 跑分编号表
        if (conditionType.equals(0)) {
            TaskBatchnumberPreExample updateBatchExample = new TaskBatchnumberPreExample();
            updateBatchExample.createCriteria().andBatchNumberEqualTo(batchNumber);
            TaskBatchnumberPre updateBatchnumber = new TaskBatchnumberPre();
            updateBatchnumber.setStatus(2);
            taskBatchnumberPreMapper.updateByExampleSelective(updateBatchnumber, updateBatchExample);
        }
        //endregion

        // 当任务类型为一次性全量时，生成任务后禁用跑分配置规则
        if (task.getMonitorType() == 1) {
            ScoreRuleConfig scoreRuleConfig = new ScoreRuleConfig();
            scoreRuleConfig.setStatus(2);
            scoreRuleConfig.setId(ruleVO.getId());
            scoreRuleConfigMapper.updateByPrimaryKeySelective(scoreRuleConfig);
        }

        //region 发送通知
        StringBuilder content = new StringBuilder();
        content.append("apiCode：".concat(apiCode).concat("\r\n"))
                .append("ruleId：".concat(ruleVO.getId().toString()).concat("\r\n"))
                .append("ruleName：".concat(ruleVO.getRuleName()).concat("\r\n"))
                .append("time：".concat(task.getStartDate().concat(" ").concat(task.getStartTime())).concat("\r\n"))
                .append("batchNumber：".concat(batchNumber).concat("\r\n"))
                .append(String.format("预计数量: %d", preNum));
        alarmClient.sendAlarm(content.toString(), "任务创建", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        //endregion

        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(task.getId());
    }

    private String createMarketingTaskBatchNumber(String apiCode, String time) {
        int i = (int) ((Math.random() * 9 + 1) * 1000);
        String batchNumber = String.format("%s_%s_%d", apiCode, time, i);
        return batchNumber;
    }

    private String whereSqlToShow(String whereSql) {
        StringBuilder str = new StringBuilder();
        String[] andStrs = whereSql.split("and|or");
        for (String andStr : andStrs) {
            if (StringUtils.isNotBlank(andStr)) {
                String[] split = andStr.split(judgmentRegex);
                str.append(split[1].replace("'", "").trim()).append(" ");
            }
        }
        return str.toString();
    }

    @Override
    public Result<List<StatisticsDataDayVO>> getStatisticsDataDay(String apiCode) {
        MarketingSyncReportExample syncReportExample = new MarketingSyncReportExample();
        syncReportExample.setOrderByClause(" applet_date desc limit 30");
        syncReportExample.createCriteria()
                .andApiCodeEqualTo(apiCode);
        List<MarketingSyncReport> marketingSyncReports = marketingSyncReportMapper.selectByExample(syncReportExample);
        ArrayList<StatisticsDataDayVO> statisticsDataDayVOS = new ArrayList<>();
        marketingSyncReports.forEach(t -> {
            StatisticsDataDayVO statisticsDataDayVO = new StatisticsDataDayVO();
            statisticsDataDayVOS.add(statisticsDataDayVO);
            statisticsDataDayVO.setDay(t.getAppletDate());
            statisticsDataDayVO.setNum(t.getDuplicateRemovalNum());
            statisticsDataDayVO.setId(t.getId());
        });
        return new Result<List<StatisticsDataDayVO>>().setCode(ResultCode.SUCCESS.getValue()).setDate(statisticsDataDayVOS);
    }

    @Override
    public Result<ResultPreviewVO> resultPreview(Long tasId) {
        ResultPreviewVO resData = new ResultPreviewVO();
        MarketingTaskResultPreviewExample example = new MarketingTaskResultPreviewExample();
        example.createCriteria().andTaskIdEqualTo(tasId);
        List<MarketingTaskResultPreview> marketingTaskResultPreviews = marketingTaskResultPreviewMapper.selectByExample(example);
        Optional<MarketingTaskResultPreview> first = marketingTaskResultPreviews.stream().filter(t -> new Integer(1).equals(t.getIsTitle())).findFirst();
        if (!first.isPresent()) {
            return new Result<ResultPreviewVO>().setCode(ResultCode.SUCCESS.getValue()).setMessage("表头不存在");
        }
        MarketingTaskResultPreview marketingTaskResultPreview = first.get();
        String[] titleArray = marketingTaskResultPreview.getContent().split(",");
        List<HashMap> titleDesc = new ArrayList<>();
        List<HashMap> contentDesc = new ArrayList<>();
        for (String s : titleArray) {
            HashMap titleHs = new HashMap();
            titleHs.put("name", s);
            titleHs.put("status", 0);
            titleDesc.add(titleHs);
        }
        marketingTaskResultPreviews.forEach(t -> {
            if (!new Integer(1).equals(t.getIsTitle())) {
                String[] field = t.getContent().split(",", -1);
                HashMap<String, String> contentHs = new HashMap<>();
                for (int i = 0; i < field.length; i++) {
                    String fieldValue = field[i];
                    String fieldTitle = titleArray[i];
                    if (!StringUtils.isBlank(fieldValue)) {
                        titleDesc.get(i).put("status", 1);
                    }
                    contentHs.put(fieldTitle, StringUtils.isBlank(fieldValue) ? "" : fieldValue);
                }
                contentDesc.add(contentHs);
            }
        });
        resData.setHeadDesc(titleDesc);
        resData.setContent(contentDesc);
        return new Result<ResultPreviewVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(resData);
    }

    @Override
    public void saveScoreResult(MarketingTaskResultPreview preview) {
        marketingTaskResultPreviewMapper.insertSelective(preview);
    }

    @Override
    public Result offLineCallBack(OffLineCallBackDTO dto) {
        Long id = Long.valueOf(dto.getRequestId());
        String lockValue = UUID.randomUUID().toString();
        boolean b = offLineCallBackLock(id, lockValue);
        if (!b) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该requestid调用过快");
        }
        boolean suc = "success".equals(dto.getStatus());
        StraHisFile straHisFile = straHisFileMapper.selectByPrimaryKey(id);
        if (straHisFile == null) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该requestid的数据不存在");
        }
        if (!ScoreStatusEnum.OFFLINECALLBACK.getValue().equals(straHisFile.getStatus())
                && !ScoreStatusEnum.OFFLINEFAIL.getValue().equals(straHisFile.getStatus())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该requestid已经回调过");
        }
        StraHisFile updateEntity = new StraHisFile();
        updateEntity.setId(id);
        updateEntity.setZipfileName(dto.getFileName());
        updateEntity.setStatus(suc ? ScoreStatusEnum.OFFLINESUCCESS.getValue() : ScoreStatusEnum.OFFLINEFAIL.getValue());
        updateEntity.setOfflineFilePath(dto.getFilePath());
        straHisFileMapper.updateByPrimaryKeySelective(updateEntity);
        if (suc) {
            producter.send(MQConstants.ROUTING_KEY_OFFLINETASK_FILE_CALLBACK, id.toString());
        }
        removeOffLineLock(id, lockValue);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private boolean offLineCallBackLock(Long id, String value) {
        String key = RedisKeyConstant.offLineLock.concat(":").concat(id.toString());
        return redisChgService.setnx(key, value, 3);
    }

    private void removeOffLineLock(Long id, String value) {
        String key = RedisKeyConstant.offLineLock.concat(":").concat(id.toString());
        String s = redisChgService.get(key);
        if (value.equals(s)) {
            redisChgService.del(key);
        }
    }

    @Override
    public Result delTask(Long id) {
        MarketingTask task = marketingTaskMapper.selectByPrimaryKey(id);
        if (task == null) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该跑分不存在");
        }
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria().andBatchNumberEqualTo(task.getBatchNumber());
        List<StraHisFile> files = straHisFileMapper.selectByExample(fileExample);
        Boolean isFinish = Boolean.FALSE;
        if (files.size() > 0) {
            long count = files.stream().filter(t -> !ScoreStatusEnum.FINISH.getValue().equals(t.getStatus())).count();
            isFinish = task.getStatus().equals(1) && count <= 0;
        }
        if (task.getStatus().equals(2) || isFinish) {
            MarketingTask update = new MarketingTask();
            update.setId(id);
            update.setStatus(0);
            marketingTaskMapper.updateByPrimaryKeySelective(update);
            entityOptService.writeOptLog(id, update, task);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("删除成功");
        }
        return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("禁用或者已跑分结束的才能删除");
    }

    private static Integer mo = 10;

    @Override
    public Integer getPart(Integer sum, Long index) {
        if(sum==null||sum==0||index==null||index==0){
            throw new RuntimeException("参数不能为空或者0");
        }
        Integer zu = 1;
        Integer zuNum = marketingCommonConfig.getQuantileValue() == null ? 50000000:marketingCommonConfig.getQuantileValue();
        while (sum>zuNum*zu){
            zu++;
        }
        return ((zu-1)*mo)+(index.intValue()%mo);
    }

    @Override
    public Integer getPart(Integer index) {
        if(index==null||index==0){
            throw new RuntimeException("参数不能为空或者0");
        }
         return index%mo;
    }

    @Override
    public Integer getPartNum(Integer sum) {
        Integer zu = 1;
        Integer zuNum = marketingCommonConfig.getQuantileValue() == null ? 50000000:marketingCommonConfig.getQuantileValue();
        while (sum>zuNum*zu){
            zu++;
        }
        return zu*mo;
    }
}
