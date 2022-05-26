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
import com.br.marketing.common.constants.auth.CodeEnum;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.exception.auth.AppException;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.TaskSelectSaveDTO;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.*;
import com.br.marketing.service.*;
import com.br.marketing.vo.CustomerScoreRuleVO;
import com.br.marketing.vo.FastTaskRuleDetailVO;
import com.br.marketing.vo.FastTaskRuleListVO;
import com.br.marketing.vo.MarketingTaskVO;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    CustomerRuleMapper customerRuleMapper;

    static final String judgmentRegex = "<=|>=|=|>|<";

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

    @Override
    public PageResultReturn list(int current, int size, String search, Integer status, String createTimeStart, String createTimeEnd,
                                 String updateTimeStart, String updateTimeEnd, Integer taskStatus) {

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
                createTimeStart, createTimeEnd, updateTimeStart, updateTimeEnd, taskStatus, null);

        return PageResultReturn.setPageResult(fastTaskRuleListVOS, current, size);
    }

    @Override
    public ApiResult<Boolean> editPriority(String id, Integer priority) {
        MarketingTask marketingTask = new MarketingTask();
        marketingTask.setPriority(priority);
        marketingTask.setId(Long.valueOf(id));
        Integer exist = marketingTaskMapper.selectByPriority(priority);
        if (exist > 0) {
            throw new AppException(CodeEnum.TASK_PRIORITY_EXIST);
        }
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
        List<MarketingTaskVO> marketingTaskVO = marketingTaskMapper.selectList(null, null, null, null, null, null, null, id);
        if (marketingTaskVO.size() > 0) {
            return marketingTaskVO.get(0);
        }
        return null;
    }

    @Override
    public Integer getTaskPercent(String hisFileId,String id) {
        String status = marketingTaskMapper.selectHisFileById(hisFileId);
        if("2".equals(status)){
            return 100;
        }else if("3".equals(status)){
            MarketingTask marketingTask = marketingTaskMapper.selectByPrimaryKey(Long.valueOf(id));
            Integer taskNumber = marketingTask.getTaskNumber();
            String s = redisChgService.get(RedisKeyConstant.taskScoreNum + ":" + hisFileId);
            if(s==null) s="0";
            return Integer.valueOf(s)/taskNumber;
        }
        return 0;
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
    public void addTaskPercent(Long fileId,Long number) {
        String key = RedisKeyConstant.taskScoreNum.concat(":").concat(fileId.toString());
        redisChgService.incrBy(key,number);
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
        String sDate = LocalDateTime.parse(sTimeStr,ymdhms).format(ymd);
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
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(String.format("概规则的历史数据不予生成 规则id：%d",vo.getId()));
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
            String errorMsg = String.format("自动规则生成任务 上传数据还未解析完成" + warnTemp, vo.getApiCode(), vo.getId());
            log.warn(errorMsg);
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg);
        }
        String number = "";

        Long minId = syncInfoMapper
                .getMinIdByRuleScoreWithDate(apiCode, sDate, eTimeStr, conditionRes.getData());
        if (minId != null && minId > 0) {
            String time = LocalDateTime.parse(eTimeStr, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Result<String> batchNumberRes = iApiToDbService.buildBatchNumber(apiCode
                    , vo.getId().toString(), vo.getRuleNameShort()
                    , time, null);
            if (!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())) {
                String errorMsg = String.format("自动规则生成任务 批次号生成错误" + warnTemp, vo.getApiCode(), vo.getId());
                log.warn(errorMsg);
                return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg);
            }
            number = batchNumberRes.getData();
        } else {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("没有符合条件的数据");
        }
        //endregion

        //跑分条件转化
        Result<String> conditionTransferRes = soleStrategyService.analysisTransferConditions(vo.getConditionInfo(), sDate, eTimeStr);

        if(!ResultCode.SUCCESS.getValue().equals(conditionTransferRes.getCode())){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("数据条件转化错误");
        }

        String transferData = conditionTransferRes.getData();
        //获取查询sql条件
        Result<List<String>> transferWhereRes = soleStrategyService.analysisConditions(transferData);
        if(!ResultCode.SUCCESS.getValue().equals(transferWhereRes.getCode())){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(transferWhereRes.getMessage());
        }
        StringBuilder showStr = new StringBuilder();
        Integer count = 0;
        for (int i = 0; i < transferWhereRes.getData().size(); i++) {
            String datum = transferWhereRes.getData().get(i);
            String s = whereSqlToShow(datum);
            Integer integer = iDynamicSqlService.countByRuleScoreWithDate(apiCode, datum);
            count += integer;
            showStr.append(s).append("总数据"+integer);
            if(i<transferWhereRes.getData().size()-1){
                showStr.append(",");
            }
        }
        vo.setConditionInfo(transferData);
        Long aLong = saveTask(apiCode, number, vo, taskStart, count,0,showStr.toString());

        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(aLong);
    }

    @Override
    public Result<Long> buildScoreTaskOfSelect(CustomerScoreRuleVO vo) {

        String apiCode = vo.getApiCode();
        Result<List<String>> listResult = soleStrategyService.analysisConditions(vo.getConditionInfo());
        if (!ResultCode.SUCCESS.getValue().equals(listResult.getCode())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(listResult.getMessage());
        }

        List<String> data = listResult.getData();

        Integer count = 0;
        StringBuilder showStr = new StringBuilder();
        for (int i = 0; i < data.size(); i++) {
            String whereStr = data.get(i);
            String s = whereSqlToShow(whereStr);
            Integer integer = iDynamicSqlService.countByRuleScoreWithDate(apiCode, whereStr);
            count += integer;
            showStr.append(s).append("总数据"+integer);
            if(i<data.size()-1){
                showStr.append(",");
            }
        }

        String number = "";
        if (count > 0) {
            String concatTime = vo.getStartDate().concat(" ").concat(vo.getStartTime() + ":00");
            String time = LocalDateTime.parse(concatTime, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            number = createMarketingTaskBatchNumber(apiCode,time);
        }
        Long aLong = saveTask(apiCode, number, vo, vo.getStartDate(), count,1,showStr.toString());

        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(aLong);

    }

    @Override
    public Result<List<Long>> saveTaskSelect(TaskSelectSaveDTO dto) {
        List<Long> resIds = new ArrayList<>();

        Result<List<CustomerScoreRuleVO>> scoreConfigNow = iRuleConfigService.getScoreConfigNow(dto.getRuleIds());
        AssertResult.assertResult(scoreConfigNow);
        String conditionInfo = getConditionInfo(dto.getDataIdDesc());
        for (CustomerScoreRuleVO datum : scoreConfigNow.getData()) {

            datum.setConditionInfo(conditionInfo);
            datum.setStartDate(dto.getTaskDate());
            datum.setStartTime(dto.getTaskTime());
            Result<Long> result = buildScoreTaskOfSelect(datum);
            if(ResultCode.SUCCESS.getValue().equals(result.getCode())){
                resIds.add(result.getData());
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(resIds);
    }

    private String getConditionInfo(List<Long> ids){
        MarketingSyncReportExample reportExample = new MarketingSyncReportExample();
        reportExample.createCriteria().andIdIn(ids);
        List<MarketingSyncReport> marketingSyncReports = marketingSyncReportMapper.selectByExample(reportExample);
        JSONArray resObj = new JSONArray();
        marketingSyncReports.forEach(t->{
            JSONObject simpleCondition = new JSONObject();
            JSONArray simpleConditionDetail = new JSONArray();
            JSONObject jsonDate = new JSONObject();
            JSONObject jsonUserType = new JSONObject();
            simpleConditionDetail.add(jsonDate);
            simpleConditionDetail.add(jsonUserType);

            simpleCondition.put("logicalOperation","and");
            simpleCondition.put("operationFactor",simpleConditionDetail);

            jsonDate.put("fieldName","appletDate");
            jsonDate.put("fieldValue",t.getAppletDate());
            jsonDate.put("operation","=");

            jsonUserType.put("fieldName","userType");
            jsonUserType.put("fieldValue",t.getUserType());
            jsonUserType.put("operation","=");

            resObj.add(simpleCondition);
        });
        return JSON.toJSONString(resObj);
    }

    private Long saveTask(String apiCode, String batchNumber
            , CustomerScoreRuleVO ruleVO, String taskStart
            , Integer preNum,Integer conditionType,String showDataStr) {

        MarketingTask hasTask = marketingTaskMapper.getByBatchNumber(batchNumber);
        if (hasTask != null) {
            return hasTask.getId();
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
        if (Integer.valueOf(4).equals(ruleVO.getExecType())) {
            MarketingTask task1 = marketingTaskMapper.selectCycleTopByApiCode(apiCode);
            if (task1 != null) {
                task.setStartDate(task1.getStartDate());
                task.setCloseDate(task1.getCloseDate());
            } else {
                task.setStartDate(taskStart);
                task.setCloseDate(ruleVO.getCycleEndDay());
            }
            task.setCycleDay(ruleVO.getCycleDay().toString());
        } else if (Integer.valueOf(3).equals(ruleVO.getExecType())) {
            task.setMonitorType(4);
            task.setStartDate(taskStart);
            task.setCloseDate(ruleVO.getCycleEndDay());
            task.setCycleDay(ruleVO.getCycleDay().toString());
        } else {
            String taskEnd = LocalDate.parse(taskStart, ymd)
                    .plusDays(1L).format(ymd);
            task.setStartDate(taskStart);
            task.setCloseDate(taskEnd);
        }
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
        marketingTaskExtendMapper.insertSelective(taskExtend);
        //endregion

        //region 跑分编号表
        if(conditionType.equals(0)) {
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
                .append("ruleName：".concat(ruleVO.getRuleName()).concat("\r\n"))
                .append("time：".concat(task.getStartDate().concat(" ").concat(task.getStartTime())).concat("\r\n"))
                .append("batchNumber：".concat(batchNumber).concat("\r\n"))
                .append(String.format("预计数量: %d", preNum));
        alarmClient.sendAlarm(content.toString(), "任务创建", appName, secretKey,
                Constants.sendCodeMap.get("uploadSuccess"));
        //endregion
        return task.getId();
    }

    private String createMarketingTaskBatchNumber(String apiCode,String time){
        int i = (int) ((Math.random() * 9 + 1) * 1000);
        String batchNumber = String.format("%s_%s_%d", apiCode, time, i);
        return batchNumber;
    }

    private String whereSqlToShow(String whereSql){
        StringBuilder str = new StringBuilder();
        String[] andStrs = whereSql.split("and|or");
        for (String andStr : andStrs) {
            if(StringUtils.isNotBlank(andStr)){
                String[] split = andStr.split(judgmentRegex);
                str.append(split[1].replace("'","").trim()).append(" ");
            }
        }
        return str.toString();
    }

}
