package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ScoreRuleCheckStatusEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.CommonConstants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.dto.OffLineCallBackDTO;
import com.br.marketing.dto.score.ProductCatalogValidationResult;
import com.br.marketing.dto.TaskExtendExtendFieldDTO;
import com.br.marketing.dto.TaskSelectSaveDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.*;
import com.br.marketing.service.Impl.datagroup.DataGroupHandlerServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.CustomerScoreRuleVO;
import com.br.marketing.vo.MarketingTaskVO;
import com.br.marketing.vo.ResultPreviewVO;
import com.br.marketing.vo.StatisticsDataDayVO;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
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
    private MarketingTaskOptService marketingTaskOptService;

    @Autowired
    private IRuleConfigService iRuleConfigService;

    @Resource
    private ProductCatalogValidationService productCatalogValidationService;

    @Resource
    private MarketingTaskModelCheckMapper marketingTaskModelCheckMapper;

    @Resource
    private MarketingSyncReportMapper marketingSyncReportMapper;

    @Resource
    MarketingTaskResultPreviewMapper marketingTaskResultPreviewMapper;
    @Resource
    DataGroupHandlerServiceImpl dataGroupHandlerService;

    static final String judgmentRegex = "<=|>=|=|>|<";

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    @Autowired
    RabbitMqProducter producter;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;

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

    @Resource
    private MarketingCustomerConfigMapper marketingCustomerConfigMapper;

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
        List<String> apiCodeList = new ArrayList<>();
        if(!StringUtils.isEmpty(apiCode)){
            String[] apiCodeArray = apiCode.split(",");
            apiCodeList = Arrays.asList(apiCodeArray);
        }
        List<ScoreRuleConfig> list = scoreRuleConfigMapper.getScoreRules(apiCodeList);
        List<ScoreRuleConfig> soleList =list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                ()->new TreeSet<>(Comparator.comparing(ScoreRuleConfig::getId))), ArrayList::new));
        return soleList;
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
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDate nowDate = LocalDate.now();
        Date nowDateStart = Date.from(nowDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        String startTime = vo.getStartTime();
        String validTimeStr = nowDate.format(ymd) + " " + startTime + ":00";
        // 获取任务计划开始时间：当前日期+跑分开始时间
        LocalDateTime validTime = LocalDateTime.parse(validTimeStr, ymdhms);

        // 判断是否达到开始时间
        if (nowTime.isBefore(validTime)) {
            log.warn("该规则没达到开始时间, 暂不生成任务, ruleId: {}", vo.getId());
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setMessage("该规则没达到开始时间，暂不生成任务");
        }

        // 生成任务方式置为自动
        vo.setBuildType(2);
        vo.setStartDate(nowDate.toString());

        String apiCode = vo.getApiCode();
        Integer execType = vo.getExecType();
        // 每个任务的周期
        if (execType == 3 && vo.getAutoBuild() == 1) {
            int idValue = vo.getId().intValue();
            String nowDateString = LocalDate.now().toString();
            // 查询中间表，条件：ruleid且结束日期大于等于当前日期
            MarketingTaskAutoBuildConfigExample buildConfigExample = new MarketingTaskAutoBuildConfigExample();
            buildConfigExample.createCriteria().andIsDeletedEqualTo(0)
                    .andScoreRuleIdEqualTo(idValue)
                    .andCloseDateGreaterThanOrEqualTo(nowDateString);
            List<MarketingTaskAutoBuildConfig> autoBuildConfigList = buildConfigMapper.selectByExample(buildConfigExample);
            if (CollectionUtils.isEmpty(autoBuildConfigList)) {
                log.warn("该周期生成任务规则, 已过期或已删除, ruleId: {}", vo.getId());
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setMessage("该周期生成任务规则，已过期或已删除");
            }

            for (MarketingTaskAutoBuildConfig autoBuildConfig : autoBuildConfigList) {
                Integer cycleDay = autoBuildConfig.getCycleDay();
                String labelName = autoBuildConfig.getLabelName();
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
                    vo.setStartTime(autoBuildConfig.getStartTime());

                    List<String> userTypeList = new ArrayList<>();

                    vo.setAutoBuildConfigId(autoBuildConfig.getId());
                    vo.setLabelName(labelName);
                    Result<Long> result = buildScoreTaskOfSelect(vo, userTypeList);
                    if (! ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        return new Result<>().setCode(ResultCode.FAIL.getValue());
                    }
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

            Date nowDateEnd = Date.from(validTime.atZone(ZoneId.systemDefault()).toInstant());
            MarketingSyncInfoExample syncInfoIngExample = new MarketingSyncInfoExample();
            syncInfoIngExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andCreateTimeGreaterThanOrEqualTo(nowDateStart)
                    .andCreateTimeLessThan(nowDateEnd)
                    .andStatusEqualTo(1);
            int isUploadCount = syncInfoMapper.countByExample(syncInfoIngExample);
            if (isUploadCount > 0) {
                String errorMsg = String.format("自动规则生成任务 上传数据还未解析完成" + warnTemp, vo.getApiCode(), vo.getId(), "");
                log.warn(errorMsg);
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg);
            }

            // 是否叠加有效期数据 0-否，1-是
            Integer isStackValidity = vo.getIsStackValidity();
            if (isStackValidity == 0) {
                return notStackValidtyBuildTask(vo, apiCode, nowDate, validTimeStr, conditionRes);
            }

            if (isStackValidity == 1) {
                return stackValidtyBuildTask(vo, apiCode, nowDate, conditionRes, validTimeStr);
            }
        }

        return null;
    }

    private Result stackValidtyBuildTask(CustomerScoreRuleVO vo, String apiCode, LocalDate nowDate, Result<String> conditionRes,
                                         String validTimeStr) {
        List<MarketingDataValidConfig> configList = marketingDataValidConfigMapper
                .findListByApiCodeAndUserTypeSetPagetikv_(
                        apiCode, nowDate.toString(), null, null, null);

        if (CollectionUtils.isEmpty(configList)) {
            log.warn("自动规则生成任务，每日定时叠加有效期类型：所有数据均已失效，不生成跑分任务");
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("没有符合条件的数据");
        }

        Long minId = syncInfoMapper
                .getMinIdByRuleScoreWithValidConfig(apiCode, configList, conditionRes.getData(), validTimeStr);

        if (minId == null || minId <= 0) {
            log.warn("自动规则生成任务，没有符合条件的数据");
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

        return buildShowAndSaveTask(vo, conditionTransferRes, apiCode, batchNumber, nowDate, userTypeList);
    }

    private Result notStackValidtyBuildTask(CustomerScoreRuleVO vo, String apiCode, LocalDate nowDate, String validTimeStr,
                                            Result<String> conditionRes) {
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

        String conditionInfo = vo.getConditionInfo();
        String nowDateString = LocalDate.now().toString();
        //跑分条件转化
        Result<String> conditionTransferRes = soleStrategyService.analysisTransferConditions(conditionInfo, nowDateString, validTimeStr);

        return buildShowAndSaveTask(vo, conditionTransferRes, apiCode, batchNumber, nowDate, userTypeList);
    }

    private Result buildShowAndSaveTask(CustomerScoreRuleVO vo, Result<String> conditionTransferRes, String apiCode,
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
            Integer integer = iDynamicSqlService.countByRuleScoreWithDate(apiCode, datum, vo.getLabelName());
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
            Integer integer = iDynamicSqlService.countByRuleScoreWithDate(apiCode, datum, vo.getLabelName());
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
        Result<List<String>> analysisResult = soleStrategyService.analysisConditions(vo.getConditionInfo());
        if (!ResultCode.SUCCESS.getValue().equals(analysisResult.getCode())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(analysisResult.getMessage());
        }

        List<String> conditionList = analysisResult.getData();
        String labelName = vo.getLabelName();

        Integer count = 0;
        Integer preMaxNum = vo.getDataLimit() != null && vo.getDataLimit() > 0 ? vo.getDataLimit() : 500;
        StringBuilder showStr = new StringBuilder();
        // 是否需要根据ConditionInfos获取多条件下的场景
        boolean userTypeFromConditionInfosFlag = false;
        if(null == userTypeList || userTypeList.size()<1){
            userTypeFromConditionInfosFlag = true;
            userTypeList = new ArrayList<>();
        }
        for (int i = 0; i < conditionList.size(); i++) {
            if (isVer && preMaxNum <= 0) {
                continue;
            }
            String whereStr = conditionList.get(i);
            String s = whereSqlToShow(whereStr);
            Integer integer = iDynamicSqlService.countByRuleScoreWithDate(apiCode, whereStr, labelName);
            if (isVer) {
                integer = integer >= preMaxNum ? preMaxNum : integer;
                preMaxNum = preMaxNum - integer;
            }
            count += integer;
            if(StringUtils.isNotBlank(labelName)){
                showStr.append(labelName).append("：");
            }
            showStr.append(s).append("总数据").append(integer.toString());
            if (i < conditionList.size() - 1) {
                showStr.append(",");
            }
            if(userTypeFromConditionInfosFlag){
                List<String> userTypeByList;
                // 查询符合跑分数据的场景
                if (StringUtils.isNotBlank(vo.getLabelName())){
                    userTypeByList = syncInfoMapper.queryUserTypeListLabelWithDatetikv_(apiCode, null, null, whereStr);
                }else {
                    userTypeByList = syncInfoMapper.queryUserTypeListWithDatetikv_(apiCode, null, null, whereStr);
                }
                if(null != userTypeByList && userTypeByList.size() > 0){
                    userTypeList.addAll(userTypeByList);
                }
                log.warn("生成跑分任务，根据数据范围查询usertype，数据范围：{}，usertypeList：{}", whereStr, JSON.toJSONString(userTypeByList));
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
                // 每个任务的周期类型：一个配置规则可以生成多个任务
                String buildId = StringUtils.isEmpty(vo.getAutoBuildConfigId()) ? vo.getId().toString() : vo.getAutoBuildConfigId().toString();

                // 生成跑分批次号
                String validTimeStr = LocalDate.now() + " " + vo.getStartTime() + ":00";
                String time = LocalDateTime.parse(validTimeStr, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                Result<String> batchNumberRes = iApiToDbService.buildBatchNumber(apiCode
                        , buildId, vo.getRuleNameShort()
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
    public Result<List<Long>> saveTaskSelectV2(TaskSelectSaveDTO dto) {
        MarketingTaskServiceImpl service = (MarketingTaskServiceImpl) AopContext.currentProxy();
        Result<List<Long>> res;
        String value = UUID.randomUUID().toString();
        try {
            //加锁-跑分配置获取最新
            dataGroupHandlerService.addLockGroupScoreConfig(dto.getApiCode(), value);
            res = service.saveTaskSelectByCreateMethod(dto);
        } catch (Exception e) {
            return new Result<>().failure().setMessage(e.getMessage());
        } finally {
            dataGroupHandlerService.unlockGroupScoreConfig(dto.getApiCode(), value);
        }
        return res;
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<List<Long>> saveTaskSelectByCreateMethod(TaskSelectSaveDTO dto) throws Exception {
        String taskCreateMethod = dto.getTaskCreateMethod();
        if("2".equals(taskCreateMethod)) {
            List<Long> dataIdDesc = dto.getDataIdDesc();
            List<Long> resIds = new ArrayList<>();
            for(Long dataId : dataIdDesc){
                TaskSelectSaveDTO singeDTO = new TaskSelectSaveDTO();
                BeanUtils.copyProperties(dto, singeDTO);
                List<Long> dataIdList = new ArrayList<>();
                dataIdList.add(dataId);
                singeDTO.setDataIdDesc(dataIdList);
                Result<List<Long>> singleResult = saveTaskSelect(singeDTO);
                if (singleResult == null) {
                    throw new Exception("生成任务失败");
                }
                if (!singleResult.isSuccess()){
                    return new Result<>().failure().setMessage(singleResult.getMessage());
                }
                if (singleResult.getData() == null) {
                    continue;
                }
                resIds.addAll(singleResult.getData());
            }
            return new Result<>().success().setDate(resIds);
        }
        return saveTaskSelect(dto);
    }

    @Override
    public Result<List<Long>> saveTaskSelect(TaskSelectSaveDTO dto) {
        List<Long> resIds = new ArrayList<>();
        // 查询符合跑分数据的场景
        List<String> userTypeList = new ArrayList<>();

        Result<List<CustomerScoreRuleVO>> scoreConfigResult = iRuleConfigService.getScoreConfigNow(dto.getRuleIds(), dto.getApiCode());
        AssertResult.assertResult(scoreConfigResult);
        // getConditionInfo
        String conditionInfo = getConditionInfo(dto.getDataIdDesc(), userTypeList);
        for (CustomerScoreRuleVO customerScoreRuleVO : scoreConfigResult.getData()) {
            customerScoreRuleVO.setLabelName(dto.getLabelName());
            // 每个任务的周期：校验该配置是否已存在
            if (customerScoreRuleVO.getExecType() == 3) {
                return buildCycleTaskBySelect(dto.getTaskDate(), dto.getTaskTime(), dto.getDataIdDesc(), customerScoreRuleVO, conditionInfo);
            }

            customerScoreRuleVO.setConditionInfo(conditionInfo);
            customerScoreRuleVO.setStartDate(dto.getTaskDate());
            customerScoreRuleVO.setStartTime(dto.getTaskTime());
            if (new Integer(1).equals(dto.getIsOrNoScoreVer())) {
                customerScoreRuleVO.setExecType(2);
                customerScoreRuleVO.setIsOrNoScoreVer(dto.getIsOrNoScoreVer());
                customerScoreRuleVO.setDataLimit(dto.getDataLimit());
            }
            customerScoreRuleVO.setBuildType(1);

            Result<Long> result = buildScoreTaskOfSelect(customerScoreRuleVO, userTypeList);
            if (!result.isSuccess()) {
                return new Result<>().failure().setMessage(result.getMessage());
            }
            resIds.add(result.getData());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(resIds);
    }

    @Override
    public Result buildCycleTaskBySelect(String startDate, String startTime, List<Long> syncReportIds,
                                         CustomerScoreRuleVO datum, String conditionInfo) {
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
        buildConfig.setLabelName(datum.getLabelName());
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
     * 尝试暂停并暂停跑分任务，最多重试一次，失败会有日志和告警
     */
    @Override
    public void disableTask(MarketingTask task) {
        int maxAttempts = 2;
        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                // 暂停跑分任务
                Result result = marketingTaskOptService.pauseTask(task.getFileId(), 1);
                String logMessage = result.getCode().equals(ResultCode.SUCCESS.getValue())
                        ? String.format("跑分异常，已暂停该跑分任务，任务编号=%s", task.getBatchNumber())
                        : String.format("跑分异常，自动暂停跑分异常，请手动操作暂停，任务编号=%s，错误原因=%s", task.getBatchNumber(), result.getMessage());

                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode(), logMessage));
                break;
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    // 最终失败
                    log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode(),
                            String.format("跑分异常，禁用跑分任务失败，请手动操作暂停，任务编号=%s", task.getBatchNumber()), e.getMessage()), e);
                }
            }
        }
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
            log.warn("生成跑分任务失败，该跑分编号已存在：{}", batchNumber);
            return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(hasTask.getId());
        }
        String scoreSeparator = CommonConstants.COMMA;
        MarketingCustomerConfigExample configExample = new MarketingCustomerConfigExample();
        configExample.createCriteria()
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andApiCodeEqualTo(apiCode);
        List<MarketingCustomerConfig> marketingCustomerConfigs = marketingCustomerConfigMapper.selectByExample(configExample);
        if(!CollectionUtils.isEmpty(marketingCustomerConfigs)){
            scoreSeparator = marketingCustomerConfigs.get(0).getScoreSeparator();
        }
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

        // region 202406新增
        if (ruleVO.getPriority() != null) {
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
        // endregion

        task.setCreateTime(LocalDateTime.now().format(ymdhms));
        task.setContextId(iApiToDbService.getTaskContextId());
        task.setScoreSeparator(scoreSeparator);

        ProductCatalogValidationResult catalogValidation = productCatalogValidationService.validate(task);
        if (!catalogValidation.isPassed()) {
            log.warn("生成跑分任务时产管目录校验未通过，不入库。batchNumber={}, apiCode={}, ruleId={}, detail={}",
                    batchNumber, apiCode, ruleVO.getId(), JSON.toJSONString(catalogValidation.getFailedItems()));
            persistProductCatalogValidationFailureOnTaskBuild(task, ruleVO, catalogValidation);
            if (ruleVO.getId() != null) {
                try {
                    ScoreRuleConfig blockRule = new ScoreRuleConfig();
                    blockRule.setId(ruleVO.getId());
                    blockRule.setCheckStatus(ScoreRuleCheckStatusEnum.CATALOG_BLOCK_AUTO.getValue());
                    blockRule.setUpdateTime(new Date());
                    scoreRuleConfigMapper.updateByPrimaryKeySelective(blockRule);
                    log.warn("跑分规则已标记check_status={}暂停自动生成,ruleId={}",
                            ScoreRuleCheckStatusEnum.CATALOG_BLOCK_AUTO.getValue(), ruleVO.getId());
                } catch (Exception e) {
                    log.error("更新跑分规则check_status失败,ruleId={}", ruleVO.getId(), e);
                }
            }
            return new Result<Long>().setCode(ResultCode.FAIL.getValue())
                    .setMessage(JSON.toJSONString(catalogValidation.getFailedItems()) + "\n" + "不在产管系统里");
        }

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
        if (StringUtils.isNotBlank(ruleVO.getLabelName())){
            taskExtend.setLabelName(ruleVO.getLabelName());
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

        //region 发送通知
        StringBuilder content = new StringBuilder();
        content.append("apiCode：".concat(apiCode).concat("\r\n"))
                .append("ruleId：".concat(ruleVO.getId().toString()).concat("\r\n"))
                .append("ruleName：".concat(ruleVO.getRuleName()).concat("\r\n"));
        if (StringUtils.isNotBlank(ruleVO.getLabelName())){
            content.append("labelName：".concat(ruleVO.getLabelName()).concat("\r\n"));
        }
        content.append("time：".concat(task.getStartDate().concat(" ").concat(task.getStartTime())).concat("\r\n"))
                .append("batchNumber：".concat(batchNumber).concat("\r\n"))
                .append(String.format("预计数量: %d", preNum));
        alarmClient.sendAlarm(content.toString(), "任务创建", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        //endregion

        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(task.getId());
    }

    /**
     * 任务生成阶段产管校验未通过时写入 {@code b_marketing_task_model_check}（此时任务尚未入库，规则名来自跑分配置 VO）。
     */
    private void persistProductCatalogValidationFailureOnTaskBuild(
            MarketingTask task, CustomerScoreRuleVO ruleVO, ProductCatalogValidationResult catalogValidation) {
        try {
            if (StringUtils.isBlank(task.getBatchNumber())) {
                insertProductCatalogValidationFailureRowOnTaskBuild(task, ruleVO, catalogValidation);
            } else {
                MarketingTaskModelCheckExample existExample = new MarketingTaskModelCheckExample();
                existExample.createCriteria()
                        .andBatchNumberEqualTo(task.getBatchNumber())
                        .andFailedModelInfoEqualTo(JSON.toJSONString(catalogValidation.getFailedItems()));
                if (marketingTaskModelCheckMapper.countByExample(existExample) == 0) {
                    insertProductCatalogValidationFailureRowOnTaskBuild(task, ruleVO, catalogValidation);
                }
            }
        } catch (Exception e) {
            log.error("写入产管校验结果表失败(任务生成阶段),batchNumber={}", task.getBatchNumber(), e);
        }
    }

    private void insertProductCatalogValidationFailureRowOnTaskBuild(
            MarketingTask task, CustomerScoreRuleVO ruleVO, ProductCatalogValidationResult catalogValidation) {
        MarketingTaskModelCheck record = new MarketingTaskModelCheck();
        record.setApiCode(task.getApiCode());
        record.setBatchNumber(task.getBatchNumber());
        record.setCusBatch(task.getCusBatch());
        record.setRuleName(ruleVO.getRuleName());
        record.setRuleNameShort(ruleVO.getRuleNameShort());
        record.setModelCheckStatus(0);
        record.setFailedModelInfo(JSON.toJSONString(catalogValidation.getFailedItems()));
        record.setIsDel(1);
        record.setCreateTime(new Date());
        marketingTaskModelCheckMapper.insertSelective(record);
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

        MarketingTask task =marketingTaskMapper.selectByPrimaryKey(tasId);
        String scoreSeparator = task.getScoreSeparator();
        MarketingTaskResultPreviewExample example = new MarketingTaskResultPreviewExample();
        example.createCriteria().andTaskIdEqualTo(tasId);
        List<MarketingTaskResultPreview> marketingTaskResultPreviews = marketingTaskResultPreviewMapper.selectByExample(example);
        Optional<MarketingTaskResultPreview> first = marketingTaskResultPreviews.stream().filter(t -> new Integer(1).equals(t.getIsTitle())).findFirst();
        if (!first.isPresent()) {
            return new Result<ResultPreviewVO>().setCode(ResultCode.FAIL.getValue()).setMessage("表头不存在");
        }
        MarketingTaskResultPreview marketingTaskResultPreview = first.get();
        String[] titleArray = marketingTaskResultPreview.getContent().split(Pattern.quote(scoreSeparator));
        List<HashMap> titleDesc = new ArrayList<>();
        List<HashMap> contentDesc = new ArrayList<>();
        for (String s : titleArray) {
            HashMap titleHs = new HashMap();
            titleHs.put("name", s);
            titleHs.put("status", 0);
            titleDesc.add(titleHs);
        }
        MarketingTaskResultPreview resultDate = marketingTaskResultPreviews.stream().filter(taskResult -> !new Integer(1).
                equals(taskResult.getIsTitle())).collect(Collectors.toList()).get(0);
        if (resultDate.getContent().split(Pattern.quote(scoreSeparator), -1).length != titleArray.length) {
            return new Result<ResultPreviewVO>().setCode(ResultCode.FAIL.getValue()).setMessage("分隔符".concat(scoreSeparator).concat("数量不匹配"));
        }
        marketingTaskResultPreviews.forEach(t -> {
            if (!new Integer(1).equals(t.getIsTitle())) {
                String[] field = t.getContent().split(Pattern.quote(scoreSeparator), -1);
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
        try {
            marketingTaskResultPreviewMapper.insertSelective(preview);
        } catch (MyBatisSystemException e) {
            // 检查线程是否被中断
            if (Thread.currentThread().isInterrupted()) {
                // 清除中断标志，以便重新插入数据库
                boolean wasInterrupted = Thread.interrupted();
                try {
                    // 重新插入数据库
                    marketingTaskResultPreviewMapper.insertSelective(preview);

                    // 重新插入成功后，恢复中断标志
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt();
                    }
                } catch (Exception retryEx) {
                    // 重新插入失败，恢复中断标志
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt();
                    }
                    throw retryEx;
                }
            } else {
                // 线程未被中断，是其他原因导致的异常，直接抛出
                throw e;
            }
        }
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
            String idString = id.toString();
            if(rocketMqSwitch.rocketMQSwitchFlag(null, MarketingAssistConstants.TAG_MARKETING_OFFLINETASK_FILE_CALLBACK)){
                rocketMqSwitch.syncSend(MarketingAssistConstants.TOPIC
                        , MarketingAssistConstants.TAG_MARKETING_OFFLINETASK_FILE_CALLBACK, idString);
            }else{
                producter.send(MQConstants.ROUTING_KEY_OFFLINETASK_FILE_CALLBACK, idString);
            }
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
