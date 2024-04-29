package com.br.marketing.service.Impl;

import com.alibaba.fastjson.*;
import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarProducer;
import com.br.cloud.counter.BrCounter;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.*;
import com.br.marketing.client.robotaiapi.output.ReqBlackPhoneVO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.common.constants.common.LastEnum;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.exception.CommonException;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.common.utils.*;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.commonentity.StatusConstants;
import com.br.marketing.context.RuntimeDataContext;
import com.br.marketing.dto.*;
import com.br.marketing.dto.customer.PushCustomerRequestDTO;
import com.br.marketing.dto.msg.mq.ApiDataInfoDTO;
import com.br.marketing.dto.msg.mq.UserTypeCollectionDTO;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.enums.CustomerQueueEnum;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.mapper.*;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import com.br.marketing.origin.CaffeineCache;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.service.*;
import com.br.marketing.service.Impl.transferfieldprocess.TransferFiledProcessImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.br.marketing.util.xiecheng.XieChengEsJsonHandler;
import com.br.marketing.vo.*;
import com.br.marketing.vo.xiecheng.PushViewVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PushRuleServiceImpl implements PushRuleService {

    private static final Logger log = LoggerFactory.getLogger(PushRuleServiceImpl.class);

    private static HashMap<String, String> errorCodeHm;

    static {
        errorCodeHm = new HashMap();
        errorCodeHm.put("1001", "无客户编号");
        errorCodeHm.put("1002", "无grouptype或userType或cell");
        errorCodeHm.put("1003", "重复客户编号");
        errorCodeHm.put("1004", "重复电话");
        errorCodeHm.put("1005", "入库异常");
        errorCodeHm.put("1006", "参数过长");
    }

    @Resource
    CaffeineCache caffeineCache;

    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    MarketingTaskUserTypeMapper marketingTaskUserTypeMapper;

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Resource
    CustomerInfoPushLogMapper customerInfoPushLogMapper;

    @Resource
    MarketingUserMapper marketingUserMapper;

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    PhoneSaleMapper phoneSaleMapper;

    @Resource
    private ZhongyouFileDataMapper zhongyouFileDataMapper;

    @Resource
    private ZhongbangCaifuDataMapper zhongbangCaifuDataMapper;

    @Resource
    private RestTemplate restTemplate;

    @Value("#{${api.pushTransfer.robotAi.tailor.apiCodeMap:{'7410787':true}}}")
    private Map<String, Boolean> tailorApiCodeMap;

    @Value("${api.pushTransfer.robotAi.robotOutboundUrl:'http://robotai-api-service/api/robotOutbound'}")
    private String robotOutboundUrl;

    @Resource
    private PushTransferCustomerLogMapper pushTransferCustomerLogMapper;

    @Resource
    private PushTransferRobotaiLogService pushTransferRobotaiLogService;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    private static final String msTimeRegex = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$";

    @Resource
    private AlarmApiClient alarmClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    private XiechengCollidingDataPackageRuleMapper xiechengCollidingDataPackageRuleMapper;

    @Resource
    private XiechengCollidingDataProcessTaskMapper xiechengCollidingDataProcessTaskMapper;

    @Resource
    private XieChengCollidingDataPackageMapper xieChengCollidingDataPackageMapper;

    @Resource
    private XieChengRuleScoreRecordMapper scoreRecordMapper;

    @Override
    public Result<Map<String, Object>> getCompanyAndModule(String apiCode) {
        String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotEmpty(companyMsg)) {
            JSONObject companyJSONObj = JSON.parseObject(companyMsg);
            map.put("compName", companyJSONObj.getString("COMP_SHORT_NAME"));
        } else {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("该ApiCode不存在，请核验输入的apiCode");
        }
        List<Map<String, Object>> module = marketingTaskMapper.getModule(apiCode);
        map.put("model", module);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(map).setMessage("查询成功");
    }
    @Override
    public Result<String> getUserType(String apiCode){
        List<String> userTypeList = marketingTaskUserTypeMapper.queryUserTypeByApiCodetikv_(apiCode);
        String userType = userTypeList.stream().collect(Collectors.joining(","));
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(userType).setMessage("查询成功");
    }

    @Override
    public PageResultReturn getBatchInfos(CustomerBatchNumDTO dto) {
        dto = getCustomerBatchNumDTO(dto);
        PageHelper.startPage(dto.getCurrent(), dto.getSize()).setOrderBy(" scoreBeginTime desc,fileId desc ");
        List<ScoreDetailVo> scoreDetailVos = marketingTaskMapper.queryBatchs(dto);
        scoreDetailVos.stream().forEach((ScoreDetailVo t)->{
            String batchNumber = t.getBatchNumber();
            List<String> batchNumberList = marketingTaskUserTypeMapper.queryUserTypeByBatchNumbertikv_(batchNumber);
            String allUserType = batchNumberList.stream().collect(Collectors.joining(","));
            t.setUserType(allUserType);
        });
        return PageResultReturn.setPageResult(scoreDetailVos, dto.getCurrent(), dto.getSize());
    }

    @Override
    public Long getBatchInfosCounts(CustomerBatchNumDTO dto) {
        dto = getCustomerBatchNumDTO(dto);
        return marketingTaskMapper.queryBatchsCount(dto);
    }

    @Override
    public Result<List<PushInfoDetailVO>> getPushInfos(RequestPushInfoDTO dto) {
        Date date = addDay(dto.getPushEndTime(), 1, "yyyy-MM-dd");
        dto.setPushEndTime(DateUtils.format(date, "yyyy-MM-dd"));
        List<PushInfoDetailVO> pushInfos = customerInfoPushMainMapper.getPushInfos(dto);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushInfos);
    }

    public CustomerBatchNumDTO getCustomerBatchNumDTO(CustomerBatchNumDTO dto) {
        //if(StringUtils.isNotBlank(dto.getUploadBeginTime())){
        //    Date dateUpdate = addDay(dto.getUploadEndTime(), 1, "yyyy-MM-dd");
        //    dto.setUploadEndTime(DateUtils.format(dateUpdate, "yyyy-MM-dd"));
        //}
        //if(StringUtils.isNotBlank(dto.getScoreBeginTime())){
        //    Date date = addDay(dto.getScoreEndTime(), 1, "yyyy-MM-dd");
        //    dto.setScoreEndTime(DateUtils.format(date, "yyyy-MM-dd"));
        //}
        if (StringUtils.isNotBlank(dto.getProductName())) {
            String productName = dto.getProductName();
            String[] module = productName.split(",");
            dto.setModuleList(Arrays.asList(module));
        }
        return dto;
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

    @Autowired
    RabbitMqProducter producter;

    @Autowired
    @Qualifier("apipool")
    ThreadPoolExecutor threadPoolExecutor;


    @Autowired
    @Qualifier("currentDbpool")
    ThreadPoolExecutor currentDbPoolExecutor;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Autowired
    MarketingHistoryEsServiceImpl marketingHistoryEsService;

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    MarketingSyncErrorInfoMapper marketingSyncErrorInfoMapper;

    @Resource
    MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Autowired
    AlarmApiClient alarmApiClient;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Autowired
    RobotaiApiServiceClient robotaiApiServiceClient;

    final String redisKey_apiCode_taskId = "marketing:preuser:";

    final String redisKeySoleNum = "sole:thread:num";

    final String redisKeyPushCustomer = "marketing:transfer:pushcustomer:apicode";

    final String redisKeyPushHaluo = "marketing:transfer:pushhaluo:apicode";

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    IRuleConfigService iRuleConfigService;

    @Autowired
    SoleStrategyService soleStrategyService;

    @Resource
    TaskTimeMapper taskTimeMapper;

    @Resource
    PhoneSaleExtendHaluoMapper phoneSaleExtendHaluoMapper;

    @Resource
    PhoneBlackMapper phoneBlackMapper;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    HaluoCallRelationMapper haluoCallRelationMapper;

    static Set<String> taskApiCodeSet = new CopyOnWriteArraySet<String>();

    final static Byte customerStatus = Byte.valueOf("1");

    @Resource
    ScoreSearchConditionMapper scoreSearchConditionMapper;

    @Resource
    ScoreSearchConditionMappingMapper scoreSearchConditionMappingMapper;

    @Autowired
    EntityOptServiceImpl entityOptService;

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Autowired
    TransferFiledProcessImpl transferFiledProcess;


    @Override
    public Result<CustomerInfoPushMain> getPushTask() {
        Date date = Date.from(LocalDate.now().minusDays(2L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        CustomerInfoPushMainExample pushMainExample = new CustomerInfoPushMainExample();
        pushMainExample.setOrderByClause(" create_time,id limit 1");
        pushMainExample.createCriteria()
                .andMStatusEqualTo(PushRuleStatusEnum.TO_BE_RUNNING.getValue())
                .andCreateTimeGreaterThanOrEqualTo(date);
        List<CustomerInfoPushMain> customerInfoPushMains = customerInfoPushMainMapper.selectByExample(pushMainExample);
        if (customerInfoPushMains.size() > 0) {
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(customerInfoPushMains.get(0));
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    @Override
    public Result isCanPushTask(Long taskId) {
        String lockValue = getCanPushTaskLock(taskId);
        if (StringUtils.isNotBlank(lockValue)) {
            CustomerInfoPushMain customerInfoPushMain = customerInfoPushMainMapper.selectByPrimaryKey(taskId);
            if (!customerInfoPushMain.getmStatus().equals(PushRuleStatusEnum.TO_BE_RUNNING.getValue())) {
                removeCanPushTaskLock(taskId, lockValue);
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            CustomerInfoPushMain updateEntity = new CustomerInfoPushMain();
            updateEntity.setId(taskId);
            updateEntity.setmStatus(PushRuleStatusEnum.RUNNING.getValue());
            customerInfoPushMainMapper.updateByPrimaryKeySelective(updateEntity);
            removeCanPushTaskLock(taskId, lockValue);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        return new Result().setCode(ResultCode.FAIL.getValue());
    }

    String getCanPushTaskLock(Long taskId) {
        try {
            String taskByPushRuleGetLock = RedisKeyConstant.TASK_PUSH_RULE_GET_LOCK.concat(":" + taskId);
            UUID uuid = UUID.randomUUID();
            Boolean setnx = redisChgService.setnx(taskByPushRuleGetLock, uuid.toString(), 3);
            if (!setnx) {
                return null;
            }
            return uuid.toString();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    void removeCanPushTaskLock(Long taskId, String lockValue) {
        String taskByPushRuleGetLock = RedisKeyConstant.TASK_PUSH_RULE_GET_LOCK.concat(":" + taskId);
        String s = redisChgService.get(taskByPushRuleGetLock);
        if (lockValue.equals(s)) {
            redisChgService.del(taskByPushRuleGetLock);
        }
    }

    //@Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> pushCustomer(PushCustomerDTO dto) {
        AssertResult.assertResult(checkThreekEnc(dto.getFileIdList()));
        /**
         * 先校验下 传过来的批次和 模型是否匹配
         * 推送mq
         */
        //region check
        if (dto.getBatchNumberList().size() > 50) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("批次最多选择50个");
        }
        if (dto.getmPlanNum() != null && dto.getmPlanNum() <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送数量不能小于等于0");
        }
        if (dto.getmPercentage() != null && dto.getmPercentage().compareTo(new BigDecimal(0)) <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("百分比不能小于等于0");
        }
        JSONObject jsonObject = JSON.parseObject(dto.getmRuleCondition());
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria().andIdIn(dto.getFileIdList());
        List<StraHisFile> files = straHisFileMapper.selectByExample(fileExample);

        Result<PushViewVO> totalRes = getTotal(dto);
        if (!ResultCode.SUCCESS.getValue().equals(totalRes.getCode())) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage(totalRes.getMessage());
        }
        Integer pushNum = totalRes.getData().getTotal();
        //endregion

        //region insert db
        try {
            StraHisFileExample straHisFileExample = new StraHisFileExample();
            straHisFileExample.createCriteria().andIdIn(dto.getFileIdList());
            List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
            List<String> showTitles = straHisFiles.stream().map(t -> t.getBatchNumber()).collect(Collectors.toList());
            CustomerInfoPushMain customerInfoPushMain = new CustomerInfoPushMain();
            customerInfoPushMain.setmApiCode(dto.getApiCode());
            customerInfoPushMain.setmRuleCondition(dto.getmRuleCondition());
            customerInfoPushMain.setmRuleConditionShow(dto.getmRuleConditionShow());
            customerInfoPushMain.setmPercentage(dto.getmPercentage());
            customerInfoPushMain.setmPlanNum(dto.getmPlanNum());
            customerInfoPushMain.setmRealyNum(pushNum);
            Date date = new Date();
            customerInfoPushMain.setCreateTime(date);
            customerInfoPushMain.setUpdateTime(date);
            customerInfoPushMain.setmCusBatchNumberList(Joiner.on(",").join(showTitles));
            customerInfoPushMain.setmStatus(PushRuleStatusEnum.TO_BE_RUNNING.getValue());
            customerInfoPushMain.setOptUserId(String.valueOf(dto.getUserDetail().getId()));
            customerInfoPushMain.setOptUserName(dto.getUserDetail().getRealName());
            Object result = JSON.parseObject(dto.getmRuleCondition()).getJSONArray("data").stream().filter(obj ->
                    ((JSONObject) obj).getString("key").equals("result")).findAny().orElse(null);
            if (marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().contains(dto.getApiCode()) && (!ObjectUtils.isEmpty(result))) {
                customerInfoPushMain.setFilterType(1);
                customerInfoPushMain.setExtend(cycleDataQuery(jsonObject, dto.getBatchNumberList(), collidingFilterDTO.getReleaseTime()));
            }
            customerInfoPushMainMapper.insertSelective(customerInfoPushMain);

            files.forEach(t -> {
                CustomerInfoPushBatch customerInfoPushBatch = new CustomerInfoPushBatch();
                customerInfoPushBatch.setmId(customerInfoPushMain.getId());
                customerInfoPushBatch.setmApiCode(dto.getApiCode());
                customerInfoPushBatch.setmBatchNumber(t.getBatchNumber());
                customerInfoPushBatch.setCreateTime(date);
                customerInfoPushBatch.setUpdateTime(date);
                customerInfoPushBatch.setmFileId(t.getId());
                customerInfoPushBatchMapper.insertSelective(customerInfoPushBatch);
            });
        }catch(Exception e){
            log.error("规则中心推送决策插入表失败，请检查推决策任务",e.getMessage());
            new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则中心推送决策插入表失败");
        }
        //endregion

        //region push mq
//        producter.send("Marketing.Push.CustomerService", customerInfoPushMain.getId().toString());
        //endregion

        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(customerInfoPushMain.getId().toString());
    }

    private Result<PushViewVO> getTotal(PushCustomerDTO dto) {
        int total;
        PushViewVO pushViewVO = new PushViewVO();
        Object result = JSON.parseObject(dto.getmRuleCondition()).getJSONArray("data").stream().filter(obj ->
                ((JSONObject) obj).getString("key").equals("result")).findAny().orElse(null);
        if (marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().contains(dto.getApiCode()) && (!ObjectUtils.isEmpty(result))) {
            total = getXieChengDataNum(dto.getmRuleCondition(), dto.getBatchNumberList(),pushViewVO);
        } else {
            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(dto.getApiCode());
            queryBaseBean.setBatchNumbers(Joiner.on(",").join(dto.getBatchNumberList()));
            queryBaseBean.setFileIds(Joiner.on(",").join(dto.getFileIdList()));
            queryBaseBean.setJsonData(dto.getmRuleCondition());
            if (dto.getmPlanNum() != null && dto.getmPlanNum() <= 0) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送数量不能小于等于0");
            }
            if (dto.getmPlanNum() != null && dto.getmPlanNum() > 0) {
                queryBaseBean.setAmountTop("0,".concat(dto.getmPlanNum().toString()));
            }
            total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        }
        if (total <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合的数据");
        }
        if (dto.getmPercentage() != null) {
            if (dto.getmPercentage().compareTo(new BigDecimal(0)) <= 0) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("百分比不能小于等于0");
            }
            Integer res = dto.getmPercentage().multiply(new BigDecimal(total)).setScale(0, RoundingMode.UP).intValue();
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
        }
        pushViewVO.setTotal(total);
        return new Result<PushViewVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushViewVO);
    }

    private int getXieChengDataNum(String mRuleCondition, List<String> batchNumberList,PushViewVO pushViewVO) {
        int total = 0;
        String querySql = "";
        JSONObject jsonObject = JSON.parseObject(mRuleCondition);
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        pushViewVO.setResult(collidingFilterDTO.getResult());
        if ("true".equals(collidingFilterDTO.getResult())) {
            querySql = cycleDataQuery(jsonObject, batchNumberList, collidingFilterDTO.getReleaseTime());
        } else {
            querySql = falseDataQuery(jsonObject, batchNumberList, collidingFilterDTO.getCleanTime());
        }
        // 查询Doris
        try {
            total = scoreRecordMapper.getXieChengDataNumdoris_(querySql);
        } catch (Exception e) {
            log.error("规则中心-携程撞库筛选查询Doris异常,sql={}", querySql, e);
        }
        return total;
    }

    private String falseDataQuery(JSONObject jsonObject, List<String> batchNumberList, String cleanTime) {
        String cycleDataSql = "select  cell_sha256_code_list as cell,id from  b_xiecheng_colliding_data_loop_cycle where is_delete =0";
        String scoreSql = scoreSql(jsonObject, batchNumberList);
        StringBuilder falseAndscoreSql = new StringBuilder();
        StringBuilder whereSql = new StringBuilder();
        //与True的全量数据去重
        falseAndscoreSql.append("select count(1) from (").append(scoreSql).append(") score left join (").append(cycleDataSql)
                .append(") cycle on score.cell = cycle.cell ");
        //where条件拼接
        whereSql.append(" where cycle.id is null");
        if (StringUtils.isNotEmpty(cleanTime)) {
            XiechengCollidingDataPackageRuleExample packageRuleExample = new XiechengCollidingDataPackageRuleExample();
            packageRuleExample.createCriteria().andCollidingStartTimeLessThanOrEqualTo(DateHelper.parseDate(cleanTime))
                    .andCollidingEndTimeGreaterThanOrEqualTo(DateHelper.parseDate(cleanTime));
            List<XiechengCollidingDataPackageRule> packageRules = xiechengCollidingDataPackageRuleMapper.selectByExample(packageRuleExample);
            String packageId = packageRules.stream().map(xiechengCollidingDataPackageRule -> xiechengCollidingDataPackageRule.getPackageId().toString())
                    .collect(Collectors.joining(","));
            //清洗时间在撞库区间内
            if (StringUtils.isNotEmpty(packageId)) {
                String FalseDataSql = "select cell_sha256_code_list as cell,id from b_xiecheng_colliding_data_rob where package_id in (" + packageId + ") and " +
                        "is_delete=0";
                falseAndscoreSql.append("left join (").append(FalseDataSql).append(") rob on score.cell = rob.cell ");
                whereSql.append(" and rob.id is null");
            }
        }
        //与待清洗去重
    /*    XiechengCollidingDataProcessTaskExample processTaskExample = new XiechengCollidingDataProcessTaskExample();
        processTaskExample.createCriteria().andTaskTypeEqualTo(0).andTaskStatusEqualTo(0);
        List<XiechengCollidingDataProcessTask> processTasks = xiechengCollidingDataProcessTaskMapper.selectByExample(processTaskExample);
        processTasks.forEach(processTask -> {
            falseAndscoreSql.append(" left join (").append(processTask.getTaskExecutionConditions()).append(") d").append(processTask.getId())
                    .append(" on score.cell = ").append("d").append(processTask.getId()).append(".cell ");
            whereSql.append(" and  d").append(processTask.getId()).append(".id is null");
        });*/
        falseAndscoreSql.append(whereSql).append(";");
        return falseAndscoreSql.toString();
    }

    private String cycleDataQuery(JSONObject jsonObject, List<String> batchNumberList, Map<String, String> releaseTime) {
        String scoreSql = scoreSql(jsonObject, batchNumberList);
        String cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where release_time>= " +
                "DATE_ADD(CURDATE(), INTERVAL 1 DAY)  and  release_time<= DATE_ADD(CURDATE(), INTERVAL 6 DAY) and is_delete=0";
        //True关联查询
        //传输releaseTime处理
        if (!CollectionUtils.isEmpty(releaseTime)) {
            String releaseTimeSql = EsConditionTransferSqlUtil.assemblefiled("release_time", releaseTime.get("operation"), releaseTime.get("value"));
            cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where " + releaseTimeSql + " and is_delete=0";
        }
        StringBuilder cycleAndscoreSql = new StringBuilder();
        cycleAndscoreSql.append("select count(1) from (").append(cycleSql).append(") cycle inner join (").append(scoreSql).append(") score on " +
                "score.cell = cycle.cell;");
        return cycleAndscoreSql.toString();
    }

    private String cycleDataDeleteQuery(JSONObject jsonObject, List<String> batchNumberList, Map<String, String> releaseTime) {
        String scoreSql = scoreSql(jsonObject, batchNumberList);
        String cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where release_time>= " +
                "DATE_ADD(CURDATE(), INTERVAL 1 DAY)  and  release_time<= DATE_ADD(CURDATE(), INTERVAL 6 DAY) and is_delete=0";
        //True关联查询
        StringBuilder cycleAndscoreSql = new StringBuilder();
        cycleAndscoreSql.append("select count(1) from (").append(cycleSql).append(") cycle left join (").append(scoreSql).append(") score on " +
                "score.cell = cycle.cell where score.id is null;");
        return cycleAndscoreSql.toString();
    }

    /**
     * 组装跑分筛选SQL
     *
     * @param jsonObject      入参jsonsql
     * @param batchNumberList batchNumber集合
     * @return String
     */
    private String scoreSql(JSONObject jsonObject, List<String> batchNumberList) {

        String sqlCondition = EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, "");
        String scoreSql = "";
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSql = scoreSql.concat("select id,cell from b_xiecheng_colliding_").concat(batchNumberList.get(i)).concat(" where ").concat(sqlCondition);
            } else {
                scoreSql = scoreSql.concat("select id,cell from b_xiecheng_colliding_").concat(batchNumberList.get(i)).concat(" where ").concat(sqlCondition).concat(" union all ");
            }

        }
        return scoreSql;
    }

    @Override
    public Result<PushViewVO> pushPreview(PushCustomerDTO dto) {

        AssertResult.assertResult(checkThreekEnc(dto.getFileIdList()));
        return getTotal(dto);

    }

    @Override
    public Result<Integer> checkThreekEnc(List<Long> fileIds) {
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(fileIds);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        List<String> batchNumbers = straHisFiles.stream().map(t -> t.getBatchNumber()).collect(Collectors.toList());

        MarketingTaskExample taskExample = new MarketingTaskExample();
        taskExample.createCriteria().andBatchNumberIn(batchNumbers);

        List<MarketingTask> marketingTasks = marketingTaskMapper.selectByExample(taskExample);
        List<Long> taskIds = marketingTasks.stream().map(t -> t.getId()).collect(Collectors.toList());

        MarketingTaskExtendExample taskExtendExample = new MarketingTaskExtendExample();
        taskExtendExample.createCriteria().andTaskIdIn(taskIds);
        List<MarketingTaskExtend> marketingTaskExtends = marketingTaskExtendMapper.selectByExample(taskExtendExample);
        Set<Integer> encrgyTypes = marketingTaskExtends.stream()
                .map(t -> {
                    if (StringUtils.isBlank(t.getExtendConfigInfo())) {
                        return ScoreThreeKeyEncryptEnum.md5.getValue();
                    }
                    Integer threekEncryptType = JSONObject.parseObject(t.getExtendConfigInfo(), TaskExtendExtendFieldDTO.class).getThreekEncryptType();
                    return threekEncryptType == null ? ScoreThreeKeyEncryptEnum.md5.getValue() : threekEncryptType;
                })
                .collect(Collectors.toSet());
        if (encrgyTypes.size() > 1) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("多个跑分记录包含不同的加密类型");
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(encrgyTypes.stream().findFirst().get());
    }

    @Override
    public Result collidingDataDelete(PushCustomerDTO dto) {
        JSONObject jsonObject = JSON.parseObject(dto.getmRuleCondition());
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        XiechengCollidingDataProcessTask xiechengCollidingDataProcessTask = new XiechengCollidingDataProcessTask();
        xiechengCollidingDataProcessTask.setApiCode(dto.getApiCode());
        xiechengCollidingDataProcessTask.setBatchNumber(String.join(",", dto.getBatchNumberList()));
        xiechengCollidingDataProcessTask.setTaskStatus(0);
        xiechengCollidingDataProcessTask.setDiscreetNumber(dto.getmPlanNum());
        try {
            xiechengCollidingDataProcessTask.setTaskStartTime(DateHelper.parseDate(collidingFilterDTO.getCleanTime()));
        }catch(Exception e){
            log.error("clean_time日期格式异常",e.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("clean_time日期格式异常");
        }
        xiechengCollidingDataProcessTask.setTaskType(1);
        xiechengCollidingDataProcessTask.setTaskExecutionConditions(EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, ""));
        xiechengCollidingDataProcessTask.setTaskExecutionSql(cycleDataDeleteQuery(jsonObject, dto.getBatchNumberList(), collidingFilterDTO.getReleaseTime()));
        xiechengCollidingDataProcessTask.setCreateTime(new Date());
        xiechengCollidingDataProcessTask.setUpdateTime(new Date());
        xiechengCollidingDataProcessTaskMapper.insertSelective(xiechengCollidingDataProcessTask);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result collidingDataPachageMake(PushCustomerDTO dto) {
        JSONObject jsonObject = JSON.parseObject(dto.getmRuleCondition());
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        XiechengCollidingDataProcessTask xiechengCollidingDataProcessTask = new XiechengCollidingDataProcessTask();
        xiechengCollidingDataProcessTask.setApiCode(dto.getApiCode());
        xiechengCollidingDataProcessTask.setBatchNumber(String.join(",", dto.getBatchNumberList()));
        xiechengCollidingDataProcessTask.setTaskStatus(0);
        xiechengCollidingDataProcessTask.setDiscreetNumber(dto.getmPlanNum());
        try {
            xiechengCollidingDataProcessTask.setTaskStartTime(DateHelper.parseDate(collidingFilterDTO.getCleanTime()));
        } catch (Exception e) {
            log.error("clean_time日期格式异常", e.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("clean_time日期格式异常");
        }
        xiechengCollidingDataProcessTask.setTaskType(0);
        xiechengCollidingDataProcessTask.setTaskExecutionConditions(EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, ""));
        xiechengCollidingDataProcessTask.setTaskExecutionSql(falseDataQuery(jsonObject, dto.getBatchNumberList(), collidingFilterDTO.getCleanTime()));
        xiechengCollidingDataProcessTask.setCreateTime(new Date());
        xiechengCollidingDataProcessTask.setUpdateTime(new Date());
        xiechengCollidingDataProcessTaskMapper.insertSelective(xiechengCollidingDataProcessTask);
        XieChengCollidingDataPackage xieChengCollidingDataPackage = new XieChengCollidingDataPackage();
        xieChengCollidingDataPackage.setPackageName(dto.getDataPackageName());
        xieChengCollidingDataPackage.setCreateTime(new Date());
        xieChengCollidingDataPackage.setUpdateTime(new Date());
        xieChengCollidingDataPackage.setCollidingDataTaskId(xiechengCollidingDataProcessTask.getId());
        xieChengCollidingDataPackage.setDiscreetNumber(dto.getmPlanNum());
        xieChengCollidingDataPackageMapper.insertSelective(xieChengCollidingDataPackage);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<Integer> collidingDataDeleteNum(PushCustomerDTO dto) {
        int num = 0;
        JSONObject jsonObject = JSON.parseObject(dto.getmRuleCondition());
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        String deleteSql = cycleDataDeleteQuery(jsonObject, dto.getBatchNumberList(), collidingFilterDTO.getReleaseTime());
        // doris查询
        // 查询Doris
        try {
            num = scoreRecordMapper.getXieChengDataNumdoris_(deleteSql);
        } catch (Exception e) {
            log.error("规则中心-携程撞库筛选查询Doris异常,sql={}",deleteSql, e);
        }
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(num);
    }

    public String encrypt3k(Integer type, String content) {
        if (com.br.marketing.common.utils.StringUtils.isBlank(content)) {
            return "";
        }
        if (ScoreThreeKeyEncryptEnum.md5.getValue().equals(type)) {
            return DigestUtils.md5DigestAsHex(content.getBytes());
        }

        if (ScoreThreeKeyEncryptEnum.sha256.getValue().equals(type)) {
            return Sha256Util.getSHA256Encrypt(content);
        }
        return content;
    }

    //    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Boolean> consumerPushCustomer(Long id) {
        long initTime = System.currentTimeMillis();
        CustomerInfoPushMain customerInfoPushMain = customerInfoPushMainMapper.selectByPrimaryKey(id);
        CustomerInfoPushBatchExample searchPushBatch = new CustomerInfoPushBatchExample();
        searchPushBatch.createCriteria().andMIdEqualTo(customerInfoPushMain.getId());
        List<CustomerInfoPushBatch> customerInfoPushBatches = customerInfoPushBatchMapper.selectByExample(searchPushBatch);
        int total = customerInfoPushMain.getmRealyNum();
        List<String> numList = new ArrayList<>();
        List<Long> fileIds = new ArrayList<>();
        for (CustomerInfoPushBatch customerInfoPushBatch : customerInfoPushBatches) {
            numList.add(customerInfoPushBatch.getmBatchNumber());
            fileIds.add(customerInfoPushBatch.getmFileId());
        }
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria().andIdIn(fileIds);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(fileExample);
        String scoreFileYhTime = marketingCommonConfig.getScoreFileYhTime();
        Date yhTime = null;
        try {
            yhTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(scoreFileYhTime);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        Date yh = yhTime;
        long beforeCount = straHisFiles.stream().filter(t -> t.getCreateTime().compareTo(yh) <= 0).count();
        Optional<StraHisFile> first = straHisFiles.stream().sorted(Comparator.comparing(StraHisFile::getIndexNum).reversed()).findFirst();
        Integer parNum = 0;
        if (first.isPresent()) {
            parNum = first.get().getIndexNum();
        }
        Result<Integer> integerResult = checkThreekEnc(fileIds);
        if (!ResultCode.SUCCESS.getValue().equals(integerResult.getCode())) {
            log.error(String.format("该推送不符合推送决策的限制条件 流水号：%s,原因：%s", id.toString(), integerResult.getMessage()));
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
        Integer _3kEncrypt = integerResult.getData();
        HashMap<Long, TaskExtendInfoVO> hsTaskExtend = new HashMap<>();
        List<TaskExtendInfoVO> extendInfosByFileIds = straHisFileMapper.getExtendInfosByFileIds(fileIds);
        extendInfosByFileIds.forEach(t -> {
            hsTaskExtend.put(t.getFileId(), t);
        });

//        if (customerInfoPushMain.getmNumMin() != null && customerInfoPushMain.getmNumMax() != null) {
//            queryBaseBean.setAmountTop(customerInfoPushMain.getmNumMin().toString()
//                    .concat(",").concat(customerInfoPushMain.getmNumMax().toString()));
//        }
        //region push Intelligent Customer Service

        //调用es查询接口
//        int minTop = (customerInfoPushMain.getmNumMin() == null) ? 0 : customerInfoPushMain.getmNumMin();
//        int startPageYushu = minTop % 10000;
//        Integer startPage = minTop / 10000 + (startPageYushu > 0 ? 1 : 0);
//        for (int i = 1; i <= startPage; i++) {
//
//            if (i == startPage && startPageYushu > 0) {
//                queryBaseBean.setPageSize(startPageYushu);
//            } else {
//                queryBaseBean.setPageSize(10000);
//            }
//            queryBaseBean.setSearchAfter(searchAfterStr);
//            String s = marketingHistoryEsService.builderMarketingWithSearchAfter(queryBaseBean);
//            searchAfterStr = s;
//        }
        Integer getEsNum = marketingCommonConfig.getScoreByEsThreadNum() != null
                && marketingCommonConfig.getScoreByEsThreadNum() > 0
                ? marketingCommonConfig.getScoreByEsThreadNum()
                : 10;
        Integer getJcNum = marketingCommonConfig.getScoreToJcThreadNum() != null
                && marketingCommonConfig.getScoreToJcThreadNum() > 0
                ? marketingCommonConfig.getScoreToJcThreadNum()
                : 2;
        boolean isSigle = (customerInfoPushMain.getmPercentage() != null
                && customerInfoPushMain.getmPercentage().compareTo(BigDecimal.ZERO) > 0)
                || (customerInfoPushMain.getmPlanNum() != null && customerInfoPushMain.getmPlanNum() > 0)
                || beforeCount > 0;
        if (isSigle) {
            parNum = 1;
            getEsNum = 1;
        }
        Integer realTotalNum = 0;
        CustomerInfoPushMain main = new CustomerInfoPushMain();
        main.setmStatus(PushRuleStatusEnum.TO_BE_CONFIRMED.getValue());
        ThreadPoolExecutor actionEs = BrExecutors.getThreadPool(getEsNum, getEsNum, 50);
        ThreadPoolExecutor pushJc = BrExecutors.getThreadPool(getJcNum, getJcNum, 50);
        List<Future<List<Future<Result<Integer>>>>> res = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        HashMap<Integer, Integer> partDataNum = new HashMap<>();
        if (!isSigle) {
            Integer nowSum = 0;
            for (Integer i = 0; i < parNum; i++) {
                QueryBaseBean queryBaseBean = new QueryBaseBean();
                queryBaseBean.setApiCode(customerInfoPushMain.getmApiCode());
                queryBaseBean.setBatchNumbers(Joiner.on(",").join(numList));
                queryBaseBean.setFileIds(Joiner.on(",").join(fileIds));
                queryBaseBean.setJsonData(customerInfoPushMain.getmRuleCondition());
                queryBaseBean.setPart(i.toString());
                Integer nowNum = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
                partDataNum.put(i, nowNum);
                nowSum += nowNum;
            }
            if (!customerInfoPushMain.getmRealyNum().equals(nowSum)) {
                log.error("任务id：{}，分组查询和预览总数不一致，请手动处理！，分组查询的总数：{}，预览总数：{}"
                        , customerInfoPushMain.getId(), nowSum.toString(), customerInfoPushMain.getmRealyNum().toString());
                return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
            }
        }

        for (Integer i = 0; i < parNum; i++) {
            res.add(actionEs.submit(new actionEs(pushJc, customerInfoPushMain
                    , fileIds, numList, i.toString(), _3kEncrypt, isSigle, partDataNum.get(i))));
        }
        log.warn("推送决策 任务id：{}；获取所有分组数据耗时：{}", customerInfoPushMain.getId(), System.currentTimeMillis() - startTime);
        try {
            for (Future<List<Future<Result<Integer>>>> actionFuture : res) {
                List<Future<Result<Integer>>> futures = actionFuture.get();
                for (Future<Result<Integer>> pushFuture : futures) {
                    Result<Integer> pushRes = pushFuture.get();
                    if (!ResultCode.SUCCESS.getValue().equals(pushRes.getCode())) {
                        main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
                    } else {
                        realTotalNum += pushRes.getData();
                    }
                }
            }
        } catch (Exception ex) {
            log.error("推送决策 获取线程结果异常" + ex.getMessage(), ex);
            main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
        }
        try {
            actionEs.shutdown();
            pushJc.shutdown();
            while (!pushJc.awaitTermination(5L, TimeUnit.SECONDS)) {

            }
            while (!actionEs.awaitTermination(5L, TimeUnit.SECONDS)) {

            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        log.warn("推送决策 任务id：{}；查询推送耗时：{}；整体耗时：{}；计划数量：{}；实际数量：{}；"
                , customerInfoPushMain.getId()
                , System.currentTimeMillis() - startTime
                , System.currentTimeMillis() - initTime
                , customerInfoPushMain.getmRealyNum(), realTotalNum);
        main.setId(customerInfoPushMain.getId());
        customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
        //endregion

        //region push mq
//        producter.send(MQConstants.ROUTING_KEY_MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH_DELAY
//                , customerInfoPushMain.getId().toString());
        //endregion

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    class actionEs implements Callable<List<Future<Result<Integer>>>> {

        private ThreadPoolExecutor pushJcPool;

        private CustomerInfoPushMain customerInfoPushMain;

        private List<Long> fileIds;

        private List<String> numList;

        private String part;

        private Integer _3kEncrypt;

        private Boolean isPerOrTop;

        private Integer partDataNum;

        public actionEs(ThreadPoolExecutor pushJcPool
                , CustomerInfoPushMain customerInfoPushMain
                , List<Long> fileIds, List<String> numList
                , String part, Integer _3kEncrypt, Boolean isPerOrTop, Integer partDataNum) {
            this.pushJcPool = pushJcPool;
            this.customerInfoPushMain = customerInfoPushMain;
            this.fileIds = fileIds;
            this.numList = numList;
            this.part = part;
            this._3kEncrypt = _3kEncrypt;
            this.isPerOrTop = isPerOrTop;
            this.partDataNum = partDataNum;
        }

        @Override
        public List<Future<Result<Integer>>> call() {
            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(customerInfoPushMain.getmApiCode());
            queryBaseBean.setBatchNumbers(Joiner.on(",").join(numList));
            queryBaseBean.setFileIds(Joiner.on(",").join(fileIds));
            queryBaseBean.setJsonData(customerInfoPushMain.getmRuleCondition());
            if (!isPerOrTop) {
                queryBaseBean.setPart(part);
            }
            Integer pageSize = 2000;
            Integer total = isPerOrTop ? customerInfoPushMain.getmRealyNum()
                    : partDataNum;
            int totalYuShu = total % pageSize;
            String searchAfterStr = "";
            int totalPage = total / pageSize + (totalYuShu > 0 ? 1 : 0);
            log.warn("任务id：{}，当前片：{}，总数：{}，页数：{}"
                    , customerInfoPushMain.getId()
                    , StringUtils.isBlank(part) ? "" : part
                    , total
                    , totalPage);
            List<Future<Result<Integer>>> resList = new ArrayList<>();
            for (int i = 1; i <= totalPage; i++) {
                try {
                    String sn = String.valueOf(i);
                    if (i == totalPage && totalYuShu > 0) {
                        queryBaseBean.setPageSize(totalYuShu);
                    } else {
                        queryBaseBean.setPageSize(pageSize);
                    }
                    queryBaseBean.setSearchAfter(searchAfterStr);
                    List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
                    Integer realNum = marketingHistories.size();
                    log.warn("任务id：{}，当前片：{}，获取的数量：{}，当前页码：{}"
                            , customerInfoPushMain.getId()
                            , StringUtils.isBlank(part) ? "" : part
                            , realNum
                            , i);
                    List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
                    for (int k = 0; k < marketingHistories.size(); k++) {
                        MarketingHistory marketingHistory = marketingHistories.get(k);
                        if (k == (marketingHistories.size() - 1)) {
                            searchAfterStr = marketingHistory.getSearchAfter();
                        }
                        //人员信息
                        PushMarketingUserDetailDTO dto1 = new PushMarketingUserDetailDTO();
//                dto1.setCaseNumber("test_202106020100".concat("_").concat(String.valueOf(System.currentTimeMillis())));
                        if (log.isInfoEnabled()) {
                            log.info("人员信息：cusnum:{};batchnumber:{}", marketingHistory.getCusNum(),
                                    (StringUtils.isNotBlank(marketingHistory.getBatchNumber()) ? marketingHistory.getBatchNumber() : ""));
                        }
                        dto1.setCaseNumber(marketingHistory.getCusNum());
                        dto1.setPhone(encrypt3k(_3kEncrypt, marketingHistory.getCell()));
                        JSONObject varObject = JSON.parseObject(marketingHistory.getReserveField());
                        if (varObject == null) {
                            varObject = new JSONObject();
                        }
                        for (MarketingCondition marketingCondition : marketingHistory.getCondition()) {
                            if (StringUtils.isNotBlank(marketingCondition.getCode())) {
                                varObject.put(marketingCondition.getFieldKey(), marketingCondition.getDValue());
                            } else {
                                varObject.put(marketingCondition.getFieldKey(), marketingCondition.getStrValue());
                            }
                        }
                        varObject.put("custNum", marketingHistory.getCusNum());
                        varObject.put("idCard", encrypt3k(_3kEncrypt, marketingHistory.getIdCard()));
                        varObject.put("name", encrypt3k(_3kEncrypt, marketingHistory.getName()));
                        varObject.put("batchNumber", marketingHistory.getBatchNumber());
                        varObject.put("taskId", marketingHistory.getTaskId());
                        varObject.put("userType", marketingHistory.getUserType());
                        varObject.put("scoreDate", new SimpleDateFormat("yyyy-MM-dd").format(marketingHistory.getRequestTime()));
                        dto1.setVariables(varObject);
                        userDetailDTOS.add(dto1);
                    }

                    //推送任务基础信息
                    PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
                    pushMarketingUserTaskInfoDTO.setMethod("caseAdd");
                    pushMarketingUserTaskInfoDTO.setBatchNumber(customerInfoPushMain.getId().toString());
                    pushMarketingUserTaskInfoDTO.setAccessNumber(customerInfoPushMain.getId() + "_" + (StringUtils.isBlank(part) ? "0" : part) + "_" + sn);
                    pushMarketingUserTaskInfoDTO.setData(userDetailDTOS);
                    pushMarketingUserTaskInfoDTO.setTaskId(customerInfoPushMain.getId().toString());
                    //传输参数信息
                    PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
                    pushMarketingUserDTO.setApiCode(customerInfoPushMain.getmApiCode());
                    pushMarketingUserDTO.setPlatApiCode(customerInfoPushMain.getmApiCode());
                    pushMarketingUserDTO.setJsonData(pushMarketingUserTaskInfoDTO);


                    resList.add(pushJcPool.submit(new PushJcAction(pushMarketingUserDTO
                            , pushMarketingUserTaskInfoDTO.getAccessNumber()
                            , customerInfoPushMain.getId()
                            , userDetailDTOS.size())));
                } catch (Exception ex) {
                    String error = String.format("任务id：%s，当前片：%s，当前页码：%d，异常："
                            , customerInfoPushMain.getId().toString()
                            , StringUtils.isBlank(part) ? "" : part
                            , i);
                    log.error(error + ex.getMessage(), ex);
                }
            }
            return resList;
        }
    }

    class PushJcAction implements Callable<Result<Integer>> {

        private PushMarketingUserDTO pushMarketingUserDTO;

        private String accessNumber;

        private Long mainId;

        private Integer size;

        public PushJcAction(PushMarketingUserDTO pushMarketingUserDTO, String accessNumber, Long mainId, Integer size) {
            this.pushMarketingUserDTO = pushMarketingUserDTO;
            this.accessNumber = accessNumber;
            this.mainId = mainId;
            this.size = size;
        }

        @Override
        public Result<Integer> call() {
            Result<Integer> result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, mainId,
                    accessNumber, size);
            if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, mainId,
                        accessNumber, size);
            }
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                log.error("推送决策重试失败 accessNumber:{}", accessNumber);
            }
            result.setDate(size);
            return result;
        }
    }

    @Override
    public Result<Boolean> getCustomerStatus(CustomerInfoPushMain customerInfoPushMain) {
        Long mId = customerInfoPushMain.getId();
        Boolean isContinue = Boolean.FALSE;

        ArrayList<String> realStatus = new ArrayList<>();
        realStatus.add("1");
        realStatus.add("900013");
        List<CustomerPushLogVO> customerInfoPushLogs = customerInfoPushLogMapper.getPushLog(mId, realStatus);
        for (CustomerPushLogVO t : customerInfoPushLogs) {
            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(customerInfoPushMain.getmApiCode());
            pushMarketingUserDTO.setPlatApiCode("");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("method", "uploadResult");
            jsonObject.put("accessNumber", t.getBatch());
            pushMarketingUserDTO.setJsonData(jsonObject);
            Result<String> userStatus = intelligentCustomerServiceClient.getUserStatus(pushMarketingUserDTO);
            if (ResultCode.SUCCESS.getValue().equals(userStatus.getCode())) {
                CustomerInfoPushLog updateLog = new CustomerInfoPushLog();
                updateLog.setId(t.getId());
                updateLog.setRealStauts(userStatus.getData());
                if ("900013".equals(userStatus.getData())) {
                    isContinue = Boolean.TRUE;
                } else if ("900016".equals(userStatus.getData())) {
                    if (StringUtils.isNotBlank(userStatus.getMessage())) {
                        updateLog.setErrorContent(userStatus.getMessage());
                        JSONObject error = JSONObject.parseObject(userStatus.getMessage());
                        if (error != null && error.keySet() != null) {
                            updateLog.setFailNum(error.keySet().size());
                        }
                    }
                }
                if (StringUtils.isNotBlank(userStatus.getMessage())) {
                    updateLog.setErrorContent(userStatus.getMessage());
                }
                customerInfoPushLogMapper.updateByPrimaryKeySelective(updateLog);
            } else {
                isContinue = Boolean.TRUE;
            }
        }
        if (!isContinue) {
            List<CustomerPushLogVO> pushLog = customerInfoPushLogMapper.getPushLog(mId, null);
            long count = pushLog.stream().filter(t -> !"00".equals(t.getRealStauts())).count();
            CustomerInfoPushMain updateMain = new CustomerInfoPushMain();
            updateMain.setId(mId);
            updateMain.setmStatus(count > 0 ? PushRuleStatusEnum.CONFIRMED_FAIL.getValue() : PushRuleStatusEnum.CONFIRMED_SUCCESS.getValue());
            customerInfoPushMainMapper.updateByPrimaryKeySelective(updateMain);
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue);
    }

    /**
     * 接受异步推送人员文本信息
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @Override
    public Result insertMarketingPreUserText(String apiCode, String jsonData) {

        //region check
        long l1 = System.currentTimeMillis();
        RequestCommonDTO<MarketingPreUserDTO> dto = new RequestCommonDTO<>();
        dto.setApiCode(apiCode);
        try {
            dto.setJsonData(JSON.parseObject(jsonData, new TypeReference<MarketingPreUserDTO>() {
            }.getType()));
        } catch (JSONException ex) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        if (log.isInfoEnabled()) {
            log.info("反序列化耗时:{}", (System.currentTimeMillis() - l1));
        }
        if (dto.getJsonData() == null) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        RuntimeDataContext.getData().setCusBatch(dto.getJsonData().getTaskId());
        if (!StringUtils.isNotBlank(dto.getJsonData().getTaskId())) {
            throw new CommonException(MarketingErrorInfo.TASK_ID_ERROR);
        }
        RuntimeDataContext.getData().setRequestBatch(dto.getJsonData().getRequestId());
        if (!StringUtils.isNotBlank(dto.getJsonData().getRequestId())) {
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }
        /**
         * 兼容旧逻辑,如果没传，则last=0，非最后一次，
         * */
        byte last = 0;
        String lastStr = dto.getJsonData().getLast();
        if (StringUtils.isNotBlank(lastStr)) {
            if (LastEnum.isLegal(lastStr)) {
                last = Byte.valueOf(lastStr);
            } else {
                throw new CommonException(MarketingErrorInfo.LAST_ERROR);
            }
        }
        /**
         * 兼容旧逻辑,如果没传，则total=0
         * */
        Long total = 0L;
        String totalStr = dto.getJsonData().getTotal();
        if (StringUtils.isNotBlank(totalStr)) {
            try {
                total = Long.valueOf(totalStr);
            } catch (NumberFormatException numberFormatException) {
                throw new CommonException(MarketingErrorInfo.TOTAL_ERROR);
            }
        }
        int size = dto.getJsonData().getDataItems().size();
        RuntimeDataContext.getData().setActualNum(size);
        if (size > 2000) {
            throw new CommonException(MarketingErrorInfo.QUANTITY_ERROR);
        }
        if (log.isInfoEnabled()) {
            log.info("check耗时:{}", (System.currentTimeMillis() - l1));
        }
        //endregion

        long l = System.currentTimeMillis();
        String uploadKey = RedisKeyConstant.uploadKey.concat(":").concat(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        String syncInfoId = "";
        Boolean dbException = Boolean.FALSE;

        //region 数据入库
        try {
            MarketingSyncInfo syncInfo = new MarketingSyncInfo();
            syncInfo.setApiCode(dto.getApiCode());
            syncInfo.setCusBatch(dto.getJsonData().getTaskId());
            syncInfo.setRequestBatch(dto.getJsonData().getRequestId());
            syncInfo.setLast(last);
            syncInfo.setTotal(total);
            syncInfo.setCreateTime(new Date());
            syncInfo.setJsonData(jsonData);
            syncInfo.setActualNum(size);
            mockDbOrRedisError(1, apiCode);
            marketingUserMapper.insertMarketingPreUserByText(syncInfo);
            syncInfoId = syncInfo.getId().toString();
            if (log.isInfoEnabled()) {
                log.info("文本插入耗时:{}", (System.currentTimeMillis() - l));
            }
            requestIdWriteRedis(uploadKey, dto.getJsonData().getRequestId());

        } catch (DuplicateKeyException keyException) {
            if (log.isInfoEnabled()) {
                log.error("文本插入耗时:{}", (System.currentTimeMillis() - l));
            }
            throw new CommonException(MarketingErrorInfo.REPEAT_ERROR);
        } catch (Exception ex) {
            log.error(String.format("返回DB异常耗时：%d", System.currentTimeMillis() - l));
            dbException = Boolean.TRUE;
        }
        //endregion

        //region db异常数据写入pulsar
        if (dbException) {
            ProductPulsarProducer producer = null;
            try {
                producer = ProductPulsarClientManager.newProducer(PulsarTopic.upLoadTopic);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("apiCode", apiCode);
                jsonObject.put("jsonData", jsonData);
                jsonObject.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                String jsonString = jsonObject.toJSONString();
                byte[] message = jsonString.getBytes();
                producer.send(message);
                log.warn(String.format("写入Pulsar 主题:%s 数据:%s", PulsarTopic.upLoadTopic, jsonString));
                Long res = requestIdWriteRedis(uploadKey, dto.getJsonData().getRequestId());
                if (res != null && res < 1) {
                    throw new CommonException(MarketingErrorInfo.REPEAT_ERROR);
                }
            } catch (PulsarClientException e) {
                throw new KnowException(e.getMessage());
            }
        }
        //endregion

        //region 写入上传明细MQ
        if (!dbException) {
            sendToMqByConfig(apiCode, MQConstants.ROUTING_KEY_MARKETING_PRE_USER_RECEIVE, syncInfoId, CustomerQueueEnum.ORG_SYNC);
        }
        //endregion

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }


    /**
     * 根据配置表发送到对应MQ
     * 配置表：b_marketing_customer_routingKey_mapping
     * @param apiCode
     * @param defaultRoutingKey 默认路由键
     * @param infoId 原始数据表id
     * @param queueEnum 队列类型
     */
    private void sendToMqByConfig(String apiCode, String defaultRoutingKey, String infoId, CustomerQueueEnum queueEnum) {
        try {
            long l3 = System.currentTimeMillis();
            // 根据apicode和bizType获取路由键
            String apiCodeJointBizType = apiCode + "," + queueEnum.getValue();
            CustomerRoutingKeyConfig routingKeyConfig = caffeineCache.getRountingKey(apiCodeJointBizType);
            if (null == routingKeyConfig) {
                producter.send(defaultRoutingKey, infoId);
            } else {
                // 大队列不支持优先级
                if (routingKeyConfig.getQueueType() == 1) {
                    producter.send(routingKeyConfig.getRoutingKey(), infoId);
                } else {
                    producter.send(routingKeyConfig.getRoutingKey(), infoId, routingKeyConfig.getPriority());
                }
            }
            if (log.isInfoEnabled()) {
                log.info("推送" + queueEnum.getDesc() + "队列耗时:{}", (System.currentTimeMillis() - l3));
            }
        } catch (Exception ex) {
            log.error("推送" + queueEnum.getDesc() + "队列失败,数据id：{}", infoId);
        }
    }

    private Long requestIdWriteRedis(String key, String requestId) {
        try {
            mockDbOrRedisError(2, null);
            Long res = redisChgService.saddMember(key, requestId);
            return res;
        } catch (Exception ex) {
            log.error(String.format("requestId写入redis失败。key【%s】,requestId【%s】", key, requestId));
        }
        return null;
    }

    @Resource
    RetryMainLogMapper retryMainLogMapper;

    /**
     * 消费异步推送人员信息
     *
     * @param infoId
     * @return
     */
    @Override
    public Result<Boolean> insertMarketingPreUserSync(Long infoId) {
        Integer soleNum = marketingCommonConfig.getSoleNum();
        if (log.isInfoEnabled()) {
            log.info(String.format("去重线程数：%d", soleNum));
        }
        Boolean isContinue = Boolean.FALSE;
        MarketingSyncInfo marketingSyncInfo = marketingSyncInfoMapper.selectByPrimaryKey(infoId);
        MarketingPreUserDTO dto = JSON.parseObject(marketingSyncInfo.getJsonData(), new TypeReference<MarketingPreUserDTO>() {
        }.getType());
        //规则校验方式-isCheck
        MerchantParam merchantParam = null;
        String apiCode = marketingSyncInfo.getApiCode();
        Result<List<CustomerSoleRuleVO>> soleConfig = iRuleConfigService.getSoleConfig(apiCode);
        try {
            merchantParam = RpcClientProxy.getMerchantParam(apiCode);
        } catch (Exception e) {
            log.error("从用户中心请求用户信息出错--apiCode:{}--{}", apiCode, e);
        }
        Integer isCheck = 0;
        if (merchantParam != null) {
            isCheck = merchantParam.getIsCheck();
        }
        long l = System.currentTimeMillis();
        tableCreateService.createMarketingSyncUserTable(marketingSyncInfo.getApiCode());
        ArrayList<Callable<Result<MarketingPreUserErrorDetailVO>>> list = new ArrayList<>();
        Map<String, UserTypeCollectionDTO> localUserTypeCache = new ConcurrentHashMap<>(16);
        for (int i = 0; i < dto.getDataItems().size(); i++) {
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = dto.getDataItems().get(i);
            //此处会处理三种场景的数据
            //1 数禾、萨摩耶：只有groupType
            //2 宜信：既有groupType,又有userType
            //3 未来客户：只有userType
            String reserveField1Str = marketingPreUserDetailDTO.getReserveField1();
            ReserveField1DTO reserveField1 = null;
            JSONObject reserveFileld1Json = null;
            if (StringUtils.isBlank(reserveField1Str)) {
                reserveField1 = new ReserveField1DTO();
                reserveField1.setUserType(marketingPreUserDetailDTO.getGroupType());
            } else {
                try {
                    reserveField1 = JSON.parseObject(reserveField1Str, new TypeReference<ReserveField1DTO>() {
                    }.getType());
                    if (StringUtils.isBlank(reserveField1.getUserType())) {
                        reserveField1.setUserType(marketingPreUserDetailDTO.getGroupType());
                    }
                    reserveFileld1Json = JSON.parseObject(reserveField1Str);
                } catch (JSONException ex) {
                    reserveField1 = new ReserveField1DTO();
                    reserveField1.setUserType(marketingPreUserDetailDTO.getGroupType());
                    reserveField1.setExtStr(reserveField1Str);
                }
            }
            Integer finalIsCheck = isCheck;
            ReserveField1DTO finalReserveField = reserveField1;
            JSONObject finalReserveFileld1Json = reserveFileld1Json;
            list.add(() -> {
                if (!StringUtils.isNotBlank(marketingPreUserDetailDTO.getCustNum())) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setErrorCode("1001");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1001"));
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                if (!StringUtils.isNotBlank(finalReserveField.getUserType())
                        || !StringUtils.isNotBlank(marketingPreUserDetailDTO.getCell())) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setCustNum(marketingPreUserDetailDTO.getCustNum());
                    errorDetailVO.setErrorCode("1002");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1002"));
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                //解密、规则校验
                marketingPreUserDetailDTO.setStatus(MonitorTypeEnum.STATUS_1.getTypeCode());
                encodeMapping(marketingPreUserDetailDTO, "cell", finalIsCheck);
                encodeMapping(marketingPreUserDetailDTO, "id", finalIsCheck);
                encodeMapping(marketingPreUserDetailDTO, "name", finalIsCheck);
                Date nowData = new Date();
                String appletDate = DateUtils.format(marketingSyncInfo.getCreateTime(), "yyyy-MM-dd");
                MarketingSyncUser marketingSyncUser = new MarketingSyncUser();
                marketingSyncUser.setApiCode(apiCode);
                marketingSyncUser.setCusBatch(marketingSyncInfo.getCusBatch());
                marketingSyncUser.setRequestBatch(marketingSyncInfo.getRequestBatch());
                marketingSyncUser.setCustNum(marketingPreUserDetailDTO.getCustNum());
                marketingSyncUser.setIdCard(marketingPreUserDetailDTO.getId());
                marketingSyncUser.setName(marketingPreUserDetailDTO.getName());
                marketingSyncUser.setCell(marketingPreUserDetailDTO.getCell());
                marketingSyncUser.setGroupType(marketingPreUserDetailDTO.getGroupType());
                marketingSyncUser.setRegisterDate(marketingPreUserDetailDTO.getRegisterDate());
                marketingSyncUser.setReserveField1(assembleReserveField1(finalReserveField, finalReserveFileld1Json));
                marketingSyncUser.setReserveField2(marketingPreUserDetailDTO.getReserveField2());
                marketingSyncUser.setCreateTime(nowData);
                marketingSyncUser.setUpdateTime(nowData);
                marketingSyncUser.setAppletDate(appletDate);
                marketingSyncUser.setStatus(marketingPreUserDetailDTO.getStatus());
                marketingSyncUser.setFailType(marketingPreUserDetailDTO.getFailType());
                marketingSyncUser.setAppletTime(marketingSyncInfo.getCreateTime());
                marketingSyncUser.setUserType(finalReserveField.getUserType());
                try {
                    Long st1 = System.currentTimeMillis();
                    Long et1;
                    Long et2 = null;
                    marketingSyncUserMapper.insertMarketingSyncUser(marketingSyncUser);
                    try {
                        //上传请求监控统计
                        BrCounter.count(PrometheusMonitorUtils.COUNT_UPLOAD_API_REQUEST_APICODE_METRIC_NAME, apiCode, marketingSyncUser.getUserType());
                    } catch (Exception ex) {
                        log.error("客户上传接口统计异常" + ex.getMessage(), ex);
                    }
                    et1 = System.currentTimeMillis() - st1;
                    if (ResultCode.SUCCESS.getValue().equals(soleConfig.getCode())) {
                        Long st2 = System.currentTimeMillis();
                        soleStrategyService.actionSole(soleConfig.getData(), marketingSyncUser);
                        et2 = System.currentTimeMillis() - st2;
                    }
                    if (log.isInfoEnabled()) {
                        log.info(String.format("去重数据：%d,数据入库和去重时间耗时：%d，数据去重时间：%d"
                                , marketingSyncUser.getId(), et1, et2));
                    }
                    Set<String> startsWith = marketingCommonConfig.getUserTypeAndSumRealtimeApiCodeStartsWith();
                    boolean isCreate = startsWith.stream().anyMatch(apiCode::startsWith)
                            && marketingSyncUser.getId() != null && (marketingSyncUser.getIsRepeat() == null
                            || marketingSyncUser.getIsRepeat().equals(2) || marketingSyncUser.getIsRepeat().equals(1));
                    if (isCreate) {
                        // 入库成功后将userType、cusBatch(taskId)、status为key，并且唯一
                        String key = marketingSyncUser.getUserType() + marketingSyncInfo.getCusBatch()
                                + marketingSyncUser.getStatus();
                        // 缓存场景数据
                        if (!localUserTypeCache.containsKey(key)) {
                            localUserTypeCache.put(key, new UserTypeCollectionDTO(marketingSyncUser.getUserType()
                                    , marketingSyncInfo.getCusBatch(), marketingSyncUser.getStatus())
                            );
                        }
                    }
                } catch (Exception ex) {
                    if (ex.getMessage().contains("IDX_taskId_custNum")) {
                        MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                        errorDetailVO.setCustNum(marketingPreUserDetailDTO.getCustNum());
                        errorDetailVO.setErrorCode("1003");
                        errorDetailVO.setErrorMsg(errorCodeHm.get("1003"));
                        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                    } else if (ex.getMessage().contains("uk_taskId_cell")) {
                        MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                        errorDetailVO.setCustNum(marketingPreUserDetailDTO.getCustNum());
                        errorDetailVO.setErrorCode("1004");
                        errorDetailVO.setErrorMsg(errorCodeHm.get("1004"));
                        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                    } else {
                        MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                        errorDetailVO.setCustNum(marketingPreUserDetailDTO.getCustNum());
                        errorDetailVO.setErrorCode("1005");
                        errorDetailVO.setErrorMsg(errorCodeHm.get("1005"));
                        log.error(ex.getMessage(), ex);
                        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                    }
                }
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            });
        }
        List<MarketingPreUserErrorDetailVO> errorBuild = new ArrayList<>();
        Integer errorSize = 0;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(soleNum, soleNum);
        List<Future<Result<MarketingPreUserErrorDetailVO>>> futures = null;
        try {
            futures = threadPool.invokeAll(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            threadPool.shutdown();
        }
        if (futures != null && !futures.isEmpty()) {
            for (int i = 0; i < futures.size(); i++) {
                try {
                    Result<MarketingPreUserErrorDetailVO> result = futures.get(i).get();
                    if (ResultCode.FAIL.getValue().equals(result.getCode())) {
                        errorSize++;
                        errorBuild.add(result.getData());
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        // 发送场景收集队列
        sendUserTypeCollectionMsg(localUserTypeCache, (Map<String, UserTypeCollectionDTO> localUserTypeCacheMap) -> {
            ApiDataInfoDTO<UserTypeCollectionDTO> dataInfoDTO = new ApiDataInfoDTO<>();
            dataInfoDTO.setApiCode(apiCode);
            dataInfoDTO.setRawDataSaveTimeStr(marketingSyncInfo.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            List<UserTypeCollectionDTO> collections = new ArrayList<>(localUserTypeCacheMap.values());
            dataInfoDTO.setArgList(collections);
            dataInfoDTO.setRequestId(marketingSyncInfo.getRequestBatch());
            return dataInfoDTO.addUploadMsgSource();
        }, MQConstants.ROUTING_KEY_MARKETING_UPLOAD_API_USERTYPE_COLLECTION_COUNT_FRAGMENTS);
        MarketingSyncInfo updateSyncInfo = new MarketingSyncInfo();
        updateSyncInfo.setId(marketingSyncInfo.getId());
        updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_running);
        Date nowData2 = new Date();
        Boolean status = Boolean.TRUE;
        if (errorSize == 0) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success);
        } else if (errorSize == futures.size()) {
            status = Boolean.FALSE;
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_fail);
            MarketingSyncErrorInfo errorInfo = new MarketingSyncErrorInfo();
            errorInfo.setApiCode(marketingSyncInfo.getApiCode());
            errorInfo.setCusBatch(marketingSyncInfo.getCusBatch());
            errorInfo.setRequestBatch(marketingSyncInfo.getRequestBatch());
            errorInfo.setCreateTime(nowData2);
            errorInfo.setErrorInfo(JSON.toJSONString(errorBuild));
            marketingSyncErrorInfoMapper.insertMarketingSigle(errorInfo);
            updateSyncInfo.setErrorId(errorInfo.getId());
        } else if (errorSize < futures.size()) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success_part);
            MarketingSyncErrorInfo errorInfo = new MarketingSyncErrorInfo();
            errorInfo.setApiCode(marketingSyncInfo.getApiCode());
            errorInfo.setCusBatch(marketingSyncInfo.getCusBatch());
            errorInfo.setRequestBatch(marketingSyncInfo.getRequestBatch());
            errorInfo.setCreateTime(nowData2);
            errorInfo.setErrorInfo(JSON.toJSONString(errorBuild));
            marketingSyncErrorInfoMapper.insertMarketingSigle(errorInfo);
            updateSyncInfo.setErrorId(errorInfo.getId());
        }
        marketingSyncInfoMapper.updateByPrimaryKeySelective(updateSyncInfo);
        if (log.isInfoEnabled()) {
            log.info("数据解析插入耗时:{}", (System.currentTimeMillis() - l));
        }
        List<String> initDataPushApiCode = marketingCommonConfig.getInitDataPushRule() == null ? new ArrayList<String>() : marketingCommonConfig.getInitDataPushRule();
        if (status && initDataPushApiCode.contains(apiCode)) {
            MqFact mqFact = new MqFact();
            mqFact.setSourceId(infoId);
            mqFact.setSource(TransferSource.INIT_DATA_SET_PROCESS.getCode());
            producter.sendToUniversalTransferQueue(mqFact);
        }
        List<String> apiCodeOfRecordTaskTime = marketingCommonConfig.getApiCodeOfRecordTaskTime();
        if (apiCodeOfRecordTaskTime.contains(apiCode)) {
            String concat = apiCode.concat(":").concat(marketingSyncInfo.getCusBatch());
            if (!taskApiCodeSet.contains(concat)) {
                try {
                    TaskTime taskTime = new TaskTime();
                    taskTime.setApiCode(apiCode);
                    taskTime.setTaskId(marketingSyncInfo.getCusBatch());
                    taskTime.setStartDate(new SimpleDateFormat("yyyy-MM-dd").format(marketingSyncInfo.getCreateTime()));
                    taskTime.setCreateTime(new Date());
                    taskTimeMapper.insertSelective(taskTime);
                } catch (DuplicateKeyException ex) {

                }
                if (taskApiCodeSet.size() >= 1000) {
                    taskApiCodeSet.clear();
                }
                taskApiCodeSet.add(concat);

            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue).setMessage("成功");
    }


    /**
     * 2024-02-29 10:12
     * 上传数据发送场景消息到收集队列
     */
    private void sendUserTypeCollectionMsg(Map<String, UserTypeCollectionDTO> localUserTypeCache
            , Function<Map<String, UserTypeCollectionDTO>, ApiDataInfoDTO<UserTypeCollectionDTO>> function
            , String routingKey) {
        String msg = "";
        try {
            msg = JSONObject.toJSONString(function.apply(localUserTypeCache));
            producter.send(routingKey, msg);
        } catch (Exception e) {
            log.error("推送场景信息到队列失败,发送队列路由键" + routingKey + ",消息内容:" + msg + "\n" + e.getMessage(), e);
        } finally {
            // 辅助gc
            localUserTypeCache.clear();
        }
    }

    @Override
    public Result<Boolean> consumerSyncInfo(String msg) {
        JSONObject jb = JSON.parseObject(msg);
        String apiCode = jb.getString("apiCode");
        String jdStr = jb.getString("jsonData");
        String time = jb.getString("time");
        Date dataTime = null;
        try {
            dataTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        MarketingPreUserDTO jsonData = JSON.parseObject(jdStr, MarketingPreUserDTO.class);
        byte last = 0;
        String lastStr = jsonData.getLast();
        if (StringUtils.isNotBlank(lastStr)) {
            if (LastEnum.isLegal(lastStr)) {
                last = Byte.valueOf(lastStr);
            } else {
                throw new CommonException(MarketingErrorInfo.LAST_ERROR);
            }
        }
        /**
         * 兼容旧逻辑,如果没传，则total=0
         * */
        Long total = 0L;
        String totalStr = jsonData.getTotal();
        if (StringUtils.isNotBlank(totalStr)) {
            try {
                total = Long.valueOf(totalStr);
            } catch (NumberFormatException numberFormatException) {
                throw new CommonException(MarketingErrorInfo.TOTAL_ERROR);
            }
        }
        int size = jsonData.getDataItems().size();
        String syncInfoId = "0";
        Boolean dbException = Boolean.FALSE;

        //region 数据入库
        try {
            MarketingSyncInfo syncInfo = new MarketingSyncInfo();
            syncInfo.setApiCode(apiCode);
            syncInfo.setCusBatch(jsonData.getTaskId());
            syncInfo.setRequestBatch(jsonData.getRequestId());
            syncInfo.setLast(last);
            syncInfo.setTotal(total);
            syncInfo.setCreateTime(dataTime);
            syncInfo.setJsonData(jdStr);
            syncInfo.setActualNum(size);
            //todo 模拟异常
            mockDbOrRedisError(1, apiCode);
            marketingUserMapper.insertMarketingPreUserByText(syncInfo);
            syncInfoId = syncInfo.getId().toString();
        } catch (DuplicateKeyException keyException) {
            alarmClient.sendAlarm(String.format("pulsar上传数据消费requestId冲突 requestId：%s", jsonData.getRequestId())
                    , "pulsar上传数据消费异常", AlarmSendCodeEnum.REQUESTID_CONFLICT.getCode());
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            dbException = Boolean.TRUE;
        }
        //endregion

        //region 写入上传明细MQ
        if (!dbException) {
            sendToMqByConfig(apiCode, MQConstants.ROUTING_KEY_MARKETING_PRE_USER_RECEIVE, syncInfoId, CustomerQueueEnum.ORG_SYNC);
        } else {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        //endregion

        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }

    //ReserveField1DTO中的属性是固定的，无法满足，客户动态增加字段的需求,
    //所以检查下客户上传的原始JSON，如果有些字段没有在ReserveField1DTO中，则动态拼装到数据中。
    private String assembleReserveField1(ReserveField1DTO finalReserveField, JSONObject finalReserveFileld1Json) {
        JSONObject finalReserveFieldObject = (JSONObject) JSONObject.toJSON(finalReserveField);
        if (null != finalReserveFileld1Json) {
            finalReserveFileld1Json.keySet().stream().forEach(k -> {
                if (!finalReserveFieldObject.containsKey(k)) {
                    finalReserveFieldObject.put(k, finalReserveFileld1Json.get(k));
                }
            });
        }
        return JSONObject.toJSONString(finalReserveFieldObject);
    }

    @Override
    public Result insertTransferData(String apiCode, String jsonData) {
        TransferDataDTO transferDataDTO = null;
        try {
            transferDataDTO = JSON.parseObject(jsonData, new TypeReference<TransferDataDTO>() {
            }.getType());
        } catch (JSONException ex) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        return insertTransferData(apiCode, jsonData, transferDataDTO);
    }

    @Override
    public Result insertTransferData(String apiCode, String jsonData, TransferDataDTO transferDataDTO) {
        //region check
        long l1 = System.currentTimeMillis();
        if (transferDataDTO == null) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        RuntimeDataContext.getData().setRequestBatch(transferDataDTO.getRequestId());
        if (!StringUtils.isNotBlank(transferDataDTO.getRequestId()) || transferDataDTO.getRequestId().length() > 100) {
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }
        int size = transferDataDTO.getDataItems().size();
        RuntimeDataContext.getData().setActualNum(size);
        if (size > 2000) {
            throw new CommonException(MarketingErrorInfo.QUANTITY_ERROR);
        }
        //endregion
        long l = System.currentTimeMillis();
        String transferKey = RedisKeyConstant.transferKey.concat(":").concat(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        String transferInfoId = "";
        Boolean dbException = Boolean.FALSE;

        try {
            //todo 测试pulsar 上线删除
            if ("transfer_20230803_wjm_test_pulsar".equals(transferDataDTO.getRequestId())) {
                throw new RuntimeException("模拟DB错误");
            }
            MarketingTransferInfo transferInfo = new MarketingTransferInfo();
            transferInfo.setApiCode(apiCode);
            transferInfo.setRequestId(transferDataDTO.getRequestId());
            transferInfo.setOrgName(transferDataDTO.getOrgName());
            transferInfo.setCreateTime(new Date());
            transferInfo.setJsonData(jsonData);
            transferInfo.setActualNum(size);
            transferInfo.setLast(transferDataDTO.getLast());
            transferInfo.setTotal(transferDataDTO.getTotal());
            //todo 模拟异常上线后要删除
            mockDbOrRedisError(1, apiCode);
            marketingTransferInfoMapper.insertSelective(transferInfo);
            transferInfoId = transferInfo.getId().toString();
            requestIdWriteRedis(transferKey, transferDataDTO.getRequestId());

        } catch (DuplicateKeyException keyException) {
            throw new CommonException(MarketingErrorInfo.REPEAT_ERROR);
        } catch (Exception ex) {
            dbException = Boolean.TRUE;
        }

        if (dbException) {
            ProductPulsarProducer producer = null;
            try {
                producer = ProductPulsarClientManager.newProducer(PulsarTopic.transferTopic);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("apiCode", apiCode);
                jsonObject.put("jsonData", jsonData);
                jsonObject.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                String jsonString = jsonObject.toJSONString();
                byte[] message = jsonString.getBytes();
                producer.send(message);
                log.warn(String.format("写入Pulsar 主题:%s 数据:%s", PulsarTopic.transferTopic, jsonString));
                Long res = requestIdWriteRedis(transferKey, transferDataDTO.getRequestId());
                if (res != null && res < 1) {
                    throw new CommonException(MarketingErrorInfo.REPEAT_ERROR);
                }
            } catch (PulsarClientException e) {
                throw new KnowException(e.getMessage());
            }
        }

        if (!dbException) {
            sendToMqByConfig(apiCode, MQConstants.ROUTING_KEY_MARKETING_TRANSFER_RECEIVE, transferInfoId, CustomerQueueEnum.ORG_TRANSFER);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }

    @Override
    public Result<Boolean> consumerTransferInfo(String msg) {
        JSONObject jb = JSON.parseObject(msg);
        String apiCode = jb.getString("apiCode");
        String jsonData = jb.getString("jsonData");
        String time = jb.getString("time");
        Date dataTime = null;
        try {
            dataTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        TransferDataDTO transferDataDTO = null;
        try {
            transferDataDTO = JSON.parseObject(jsonData, new TypeReference<TransferDataDTO>() {
            }.getType());
        } catch (JSONException ex) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        int size = transferDataDTO.getDataItems().size();
        Boolean dbException = Boolean.FALSE;
        String transferInfoId = "";

        try {
            MarketingTransferInfo transferInfo = new MarketingTransferInfo();
            transferInfo.setApiCode(apiCode);
            transferInfo.setRequestId(transferDataDTO.getRequestId());
            transferInfo.setOrgName(transferDataDTO.getOrgName());
            transferInfo.setCreateTime(dataTime);
            transferInfo.setJsonData(jsonData);
            transferInfo.setActualNum(size);
            transferInfo.setLast(transferDataDTO.getLast());
            transferInfo.setTotal(transferDataDTO.getTotal());
            //todo 模拟异常
            mockDbOrRedisError(1, apiCode);
            marketingTransferInfoMapper.insertSelective(transferInfo);
            transferInfoId = transferInfo.getId().toString();

        } catch (DuplicateKeyException keyException) {
            alarmClient.sendAlarm(String.format("pulsar转化数据消费requestId冲突 requestId：%s", transferDataDTO.getRequestId())
                    , "pulsar转化数据消费异常", AlarmSendCodeEnum.REQUESTID_CONFLICT.getCode());
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            dbException = Boolean.TRUE;
        }

        if (!dbException) {
            sendToMqByConfig(apiCode, MQConstants.ROUTING_KEY_MARKETING_TRANSFER_RECEIVE, transferInfoId, CustomerQueueEnum.ORG_TRANSFER);
        } else {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }

        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result consumerTransferData(Long id) {
        List<String> pushCustomerApiCodes = marketingCommonConfig.getApiCodeOfpushCustomer();
        List<String> haluoApiCodes = marketingCommonConfig.getApiCodeOfpushHaluoByTransfer();
        List<String> universalProcessApiCode = marketingCommonConfig.getUniversalProcessApiCode();
        Integer soleNumTrans = marketingCommonConfig.getSoleNumTrans();
        Boolean isContinue = Boolean.FALSE;
        MarketingTransferInfo transferInfo = marketingTransferInfoMapper.selectByPrimaryKey(id);
        TransferFieldProcessFactory transferFieldProcessFactory = transferFiledProcess.getTransferFieldProcessFactory(transferInfo.getApiCode());
        TransferDataDTO<TransferDataItemDTO> dto = null;
        if (transferFieldProcessFactory != null && transferFieldProcessFactory.isFormat()) {
            dto = transferFieldProcessFactory.formatTransferObj(transferInfo.getJsonData());
        } else {
            dto = JSON.parseObject(transferInfo.getJsonData(), new TypeReference<TransferDataDTO<TransferDataItemDTO>>() {
            }.getType());
        }
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(transferInfo.getApiCode()).andStatusEqualTo(customerStatus);
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if (marketingCustomers.size() == 0) {
            throw new RuntimeException(String.format("该apicode:%s 没有维护cid信息,消费有问题", transferInfo.getApiCode()));
        }
        String cid = marketingCustomers.get(0).getCid();
        String tcid = cid.replaceFirst("-", "");
        tableCreateService.createMarketingTransferUserTable(tcid);
        ArrayList<Callable<Result<MarketingPreUserErrorDetailVO>>> list = new ArrayList<>();
        Map<String, UserTypeCollectionDTO> localUserTypeCache = new ConcurrentHashMap<>(16);
        for (int i = 0; i < dto.getDataItems().size(); i++) {
            TransferDataItemDTO transferDataItemDTO = dto.getDataItems().get(i);
            list.add(() -> {
                if (!StringUtils.isNotBlank(transferDataItemDTO.getCustNum())) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setErrorCode("1001");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1001"));
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                if (!StringUtils.isNotBlank(transferDataItemDTO.getUserType())) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setCustNum(transferDataItemDTO.getCustNum());
                    errorDetailVO.setErrorCode("1002");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1002"));
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                if (transferDataItemDTO.getUserType().length() > 100) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setCustNum(transferDataItemDTO.getCustNum());
                    errorDetailVO.setErrorCode("1006");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1006"));
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                Date nowData = new Date();
                String requestDate = DateUtils.format(transferInfo.getCreateTime(), "yyyy-MM-dd");
                String requestTime = DateUtils.format(transferInfo.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                MarketingTransferSyncUser transferSyncUser = new MarketingTransferSyncUser();
                BeanUtils.copyProperties(transferDataItemDTO, transferSyncUser);
                transferSyncUser.setRequestId(transferInfo.getRequestId());
                transferSyncUser.setApiCode(transferInfo.getApiCode());
                transferSyncUser.setOrgName(transferInfo.getOrgName());
                transferSyncUser.setRequestData(requestDate);
                transferSyncUser.setRequestTime(requestTime);
                transferSyncUser.setCreateTime(nowData);
                transferSyncUser.setCid(cid);
                transferSyncUser.settCid(tcid);
                transferSyncUser.setRegisterTime(dateTimeComplet(transferDataItemDTO.getRegisterTime()));
                transferSyncUser.setLoginTime(dateTimeComplet(transferDataItemDTO.getLoginTime()));
                transferSyncUser.setApplyDt(dateTimeComplet(transferDataItemDTO.getApplyDt()));
                transferSyncUser.setApplyTime(dateTimeComplet(transferDataItemDTO.getApplyTime()));
                transferSyncUser.setRefuseTime(dateTimeComplet(transferDataItemDTO.getRefuseTime()));
                transferSyncUser.setAuditTime(dateTimeComplet(transferDataItemDTO.getAuditTime()));
                transferSyncUser.setLentTime(dateTimeComplet(transferDataItemDTO.getLentTime()));
                transferSyncUser.setSettleTime(dateTimeComplet(transferDataItemDTO.getSettleTime()));
                transferSyncUser.setTransformTime(dateTimeComplet(transferDataItemDTO.getTransformTime()));
                if (transferFieldProcessFactory != null) {
                    transferFieldProcessFactory.fieldProcess(transferSyncUser, transferDataItemDTO);
                }
                try {
                    marketingTransferSyncUserMapper.insertSelective(transferSyncUser);
                    String key = transferSyncUser.getUserType();
                    Set<String> startsWith = marketingCommonConfig.getUserTypeAndSumRealtimeApiCodeStartsWith();
                    if (StringUtils.isNotBlank(key)
                            && startsWith.stream().anyMatch(transferInfo.getApiCode()::startsWith)
                            && transferSyncUser.getId() != null && !localUserTypeCache.containsKey(key)) {
                        // 入库成功后将userType为key，并且唯一
                        // 缓存场景数据
                        localUserTypeCache.put(key, new UserTypeCollectionDTO(transferSyncUser.getUserType()));
                    }
                    //转化请求监控统
                    //是否影响性能待观察
                    try {
                        BrCounter.count(PrometheusMonitorUtils.COUNT_TRANSFER_API_REQUEST_CID_METRIC_NAME, transferSyncUser.getApiCode(), transferSyncUser.getUserType());
                    } catch (Exception ex) {
                        log.error("客户转化接口统计异常" + ex.getMessage(), ex);
                    }
                } catch (Exception ex) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setCustNum(transferDataItemDTO.getCustNum());
                    errorDetailVO.setErrorCode("1005");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1005"));
                    log.error(ex.getMessage(), ex);
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            });
        }
        List<MarketingPreUserErrorDetailVO> errorBuild = new ArrayList<>();
        Integer errorSize = 0;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(soleNumTrans, soleNumTrans);
        List<Future<Result<MarketingPreUserErrorDetailVO>>> futures = null;
        try {
            futures = threadPool.invokeAll(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            threadPool.shutdown();
        }
        if (futures != null && !futures.isEmpty()) {
            for (int i = 0; i < futures.size(); i++) {
                try {
                    Result<MarketingPreUserErrorDetailVO> result = futures.get(i).get();
                    if (ResultCode.FAIL.getValue().equals(result.getCode())) {
                        errorSize++;
                        errorBuild.add(result.getData());
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        // 发送场景收集队列
        transferSendUserTypeCollectionMsg(cid, transferInfo, localUserTypeCache);
        MarketingTransferInfo updateSyncInfo = new MarketingTransferInfo();
        updateSyncInfo.setId(id);
        updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_running);
        if (errorSize == 0) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success);
        } else if (errorSize == futures.size()) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_fail);
            updateSyncInfo.setErrorInfo(JSON.toJSONString(errorBuild));
        } else if (errorSize < futures.size()) {
            updateSyncInfo.setStatus(StatusConstants.MarketingPreUserStatus_success_part);
            updateSyncInfo.setErrorInfo(JSON.toJSONString(errorBuild));
        }
        marketingTransferInfoMapper.updateByPrimaryKeySelective(updateSyncInfo);
        if (pushCustomerApiCodes.contains(transferInfo.getApiCode())
                && (updateSyncInfo.getStatus().equals(StatusConstants.MarketingPreUserStatus_success)
                || updateSyncInfo.getStatus().equals(StatusConstants.MarketingPreUserStatus_success_part))) {
            if (transferInfo.getRequestId().startsWith("black_")) {
                producter.send(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_PUSH_BLACK, id.toString());
            } else {
                producter.send(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_PUSH_CUSTOMER, id.toString());
            }
        }
        if (universalProcessApiCode.contains(transferInfo.getApiCode())) {
            MqFact mqFact = new MqFact();
            mqFact.setSourceId(id);
            mqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
            producter.sendToUniversalTransferQueue(mqFact);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue).setMessage("成功");
    }

    private void transferSendUserTypeCollectionMsg(String cid, MarketingTransferInfo transferInfo
            , Map<String, UserTypeCollectionDTO> localUserTypeCache) {
        sendUserTypeCollectionMsg(localUserTypeCache, (Map<String, UserTypeCollectionDTO> localUserTypeCacheMap) -> {
            ApiDataInfoDTO<UserTypeCollectionDTO> dataInfoDTO = new ApiDataInfoDTO<>();
            List<UserTypeCollectionDTO> collections = new ArrayList<>(localUserTypeCacheMap.values());
            dataInfoDTO.setArgList(collections);
            dataInfoDTO.setCid(cid);
            dataInfoDTO.setApiCode(transferInfo.getApiCode());
            dataInfoDTO.setRawDataSaveTimeStr(transferInfo.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            dataInfoDTO.setRequestId(transferInfo.getRequestId());
            return dataInfoDTO.addTransferMsgSource();
        }, MQConstants.ROUTING_KEY_MARKETING_TRANSFER_API_USERTYPE_COLLECTION_COUNT_FRAGMENTS);
    }


    private String dateTimeComplet(String data) {
        if (data == null) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String res = "";
        try {
            if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$", data)) {
                String s = data.replaceAll("/", "-");
                res = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}$", data)) {
                String s = data.replaceAll("/", "-");
                res = LocalDateTime.parse(s.concat(":000"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}$", data)) {
                String s = data.replaceAll("/", "-");
                res = LocalDateTime.parse(s.concat(":00:000"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}$", data)) {
                String s = data.replaceAll("/", "-");
                res = LocalDateTime.parse(s.concat(":00:00:000"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
            } else if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2}$|^\\d{4}/\\d{1,2}/\\d{1,2}$", data)) {
                String s = data.replaceAll("/", "-");
                res = df.format(df.parse(s.concat(" 00:00:00:000")));
            } else {
                res = data;
            }
        } catch (Exception ex) {
            res = data;
//            log.error(ex.getMessage(), ex);
        }
        return res;
    }

    @Override
    public Result<MarketingTransferUserStatusVO> getTransferDataStatus(String apiCode, String requestId) {
        if (StringUtils.isBlank(requestId)) {
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }
        Boolean dbBad = Boolean.FALSE;
        Boolean redisBad = Boolean.FALSE;
        Boolean selectBad = Boolean.FALSE;
        MarketingTransferInfoExample transferInfoExample = new MarketingTransferInfoExample();
        transferInfoExample.createCriteria().andRequestIdEqualTo(requestId).andApiCodeEqualTo(apiCode);
        List<MarketingTransferInfo> marketingTransferInfos = new ArrayList<>();
        try {
            //todo 模拟异常上线后要删除
            mockDbOrRedisError(1, apiCode);
            marketingTransferInfos = marketingTransferInfoMapper.selectByExample(transferInfoExample);
            if (marketingTransferInfos.size() <= 0) {
                selectBad = Boolean.TRUE;
            }
        } catch (Exception ex) {
            dbBad = Boolean.TRUE;
        }
        String transferKey = RedisKeyConstant.transferKey.concat(":").concat(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        if (dbBad || selectBad) {
            Boolean sismember = Boolean.FALSE;
            try {
                //todo 模拟异常上线后要删除
                mockDbOrRedisError(2, null);
                sismember = redisChgService.sismember(transferKey, requestId);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                redisBad = Boolean.TRUE;
            }
            if (!redisBad && sismember) {
                MarketingTransferUserStatusVO vo = new MarketingTransferUserStatusVO();
                vo.setApiCode(apiCode);
                vo.setRequestId(requestId);
                vo.setStatus(1);
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(vo).setMessage("成功");
            } else if (!redisBad) {
                throw new CommonException(MarketingErrorInfo.DATA_NOT_EXIST_ERROR);
            }
        }
        if (dbBad && redisBad) {
            throw new CommonException(MarketingErrorInfo.UNKNOWN_ERROR);
        }
        if (selectBad) {
            throw new CommonException(MarketingErrorInfo.DATA_NOT_EXIST_ERROR);
        }
        MarketingTransferInfo transferInfo = marketingTransferInfos.get(0);
        MarketingTransferUserStatusVO vo = new MarketingTransferUserStatusVO();
        vo.setApiCode(transferInfo.getApiCode());
        vo.setRequestId(transferInfo.getRequestId());
        vo.setStatus(transferInfo.getStatus());
        if (StringUtils.isNotBlank(transferInfo.getErrorInfo())) {
            List<MarketingPreUserErrorDetailVO> o = JSON.parseObject(transferInfo.getErrorInfo(), new TypeReference<List<MarketingPreUserErrorDetailVO>>() {
            }.getType());
            vo.setErrorInfo(o);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(vo).setMessage("成功");
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result insertBatchTransferUser(String apiCode, String jsonData) {
        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(jsonData);
        } catch (Exception ex) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        String requestId = jsonObject.getString("requestId");
        RuntimeDataContext.getData().setRequestBatch(requestId);
        if (StringUtils.isBlank(requestId)) {
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }
        if (requestId.length() > 100) {
            throw new CommonException(MarketingErrorInfo.REQUEST_ID_ERROR);
        }

        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(apiCode);
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if (marketingCustomers.size() <= 0) {
            throw new CommonException(MarketingErrorInfo.API_CODE_AUTH_ERROR);
        }
        String cid = marketingCustomers.get(0).getCid();
        if (StringUtils.isBlank(cid)) {
            throw new CommonException(MarketingErrorInfo.API_CODE_AUTH_ERROR);
        }

        marketingSyncInfoMapper.createMarketingTransferTable("b_marketing_transfer_".concat(apiCode));
        Integer hasData = marketingSyncInfoMapper.selectTransfersByRequestId(apiCode, requestId);
        if (hasData > 0) {
            throw new CommonException(MarketingErrorInfo.REPEAT_ERROR);
        }

        List<TransferUserVO> transfers = new ArrayList<>();
        try {
            transfers = JSON.parseObject(jsonObject.getString("dataItems"), new TypeReference<List<TransferUserVO>>() {
            }.getType());
        } catch (Exception ex) {
            throw new CommonException(MarketingErrorInfo.JSON_DATA_ERROR);
        }
        RuntimeDataContext.getData().setActualNum(transfers.size());
        if (transfers.size() > 100) {
            throw new CommonException(MarketingErrorInfo.QUANTITY_ERROR);
        }
        if (transfers.size() == 0) {
            throw new CommonException(MarketingErrorInfo.QUANTITY_ERROR);
        }


        StringBuilder sqlByTaskAndCustNum = new StringBuilder();
        String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        for (int i = 0; i < transfers.size(); i++) {
//            StringBuilder sql = new StringBuilder();
            TransferUserVO transferUserVO = transfers.get(i);
            //region 校验参数
            if (StringUtils.isBlank(transferUserVO.getTaskId())) {
                throw new CommonException(MarketingErrorInfo.TASK_ID_ERROR);
            }

            if (transferUserVO.getTaskId().length() > 50) {
                throw new CommonException(MarketingErrorInfo.TASK_ID_ERROR);
            }

            if (StringUtils.isBlank(transferUserVO.getCustNum())) {
                throw new CommonException(MarketingErrorInfo.CUST_NUM_ERROR);
            }

            if (transferUserVO.getCustNum().length() > 100) {
                throw new CommonException(MarketingErrorInfo.CUST_NUM_ERROR);
            }

            if (StringUtils.isBlank(transferUserVO.getGroupType())) {
                throw new CommonException(MarketingErrorInfo.GROUP_TYPE_ERROR);
            }

            if (transferUserVO.getGroupType().length() > 100) {
                throw new CommonException(MarketingErrorInfo.GROUP_TYPE_ERROR);
            }

            //endregion

            //region 拼接sql
            Date parse = null;
            try {
                if (StringUtils.isNotBlank(transferUserVO.getTransformTime())) {
                    parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(transferUserVO.getTransformTime());
                }
            } catch (Exception e) {
                throw new CommonException(MarketingErrorInfo.TIME_FORMAT_ERROR);
            }
            MarketingTransfer transfer = new MarketingTransfer();
            transfer.setApiCode(apiCode);
            transfer.setRequestId(requestId);
            transfer.setTaskId(transferUserVO.getTaskId());
            transfer.setCustNum(transferUserVO.getCustNum());
            transfer.setTransformTime(parse == null ? null : (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parse)));
            transfer.setCreateTime(new Date());
            transfer.setGroupType(transferUserVO.getGroupType());
            transfer.setReserveField1(transferUserVO.getReserveField1());
            transfer.setReserveField2(transferUserVO.getReserveField2());
            if (i == 0) {
                sqlByTaskAndCustNum.append(String.format("(cus_batch = '%s' and cust_num = '%s')"
                        , transferUserVO.getTaskId()
                        , transferUserVO.getCustNum()));
            }
            if (i > 0) {
//                sql.append(",");
                sqlByTaskAndCustNum.append(" or ")
                        .append(String.format("(cus_batch = '%s' and cust_num = '%s')"
                                , transferUserVO.getTaskId()
                                , transferUserVO.getCustNum()));
            }

            //endregion
            marketingSyncInfoMapper.insertTransfer(transfer);
            transferUserVO.setId(transfer.getId());
        }

//        if(StringUtils.isNotBlank(sql.toString())){
//            marketingSyncInfoMapper.insertBatchTransfer(sql.toString());
//        }
        HashMap<String, MarketingSyncUser> hmPreUser = new HashMap();
        if (StringUtils.isNotBlank(sqlByTaskAndCustNum.toString())) {
            List<MarketingSyncUser> preUserByTaskAndCust = marketingSyncInfoMapper.getPreUserByTaskAndCust(apiCode, sqlByTaskAndCustNum.toString());
            for (MarketingSyncUser marketingSyncUser : preUserByTaskAndCust) {
                hmPreUser.put(marketingSyncUser.getCusBatch()
                        .concat("_")
                        .concat(marketingSyncUser.getCustNum()), marketingSyncUser);
            }
        }
        //region 拼接客服接口参数
        TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
        List<ConversionData> conversionDataList = new ArrayList<>();
        for (TransferUserVO transfer : transfers) {
            if (!(marketingCommonConfig.getGroupTypeSaMoye().contains(transfer.getGroupType()) && "1".equals(transfer.getReserveField1()))) {
                continue;
            }
            ConversionData data = new ConversionData();
            data.setCaseNum(transfer.getCustNum());
            data.setInversionDate(transfer.getTransformTime());
            data.setInversionStatus("0");
            data.setInversionInfo(JSON.toJSONString(transfer));
            data.setTaskId(transfer.getTaskId());
            data.setPartnerProcessDate(nowDate);
            data.setGroupType(transfer.getGroupType());
            data.setCid(cid);
            MarketingSyncUser marketingSyncUser = hmPreUser.get(transfer.getTaskId()
                    .concat("_")
                    .concat(transfer.getCustNum()));
            if (marketingSyncUser != null) {
                data.setPhone(StringUtils.isBlank(marketingSyncUser.getFailType())
                        ? BrCipherMaker.getInstance().decode(marketingSyncUser.getCell())
                        : marketingSyncUser.getCell());
            }
            data.setDataId(transfer.getId().toString());
            conversionDataList.add(data);
        }
        TransferJsonDataDTO jsonDataDTO = new TransferJsonDataDTO();
        jsonDataDTO.setConversionData(conversionDataList);
        jsonDataDTO.setMethod("conversionData");
        jsonDataDTO.setAccessNumber(UUID.randomUUID().toString());
        robotOutboundDTO.setApiCode(apiCode);
        robotOutboundDTO.setJsonData(jsonDataDTO);
        //endregion

        //todo 调用客服接口
        if (conversionDataList.size() > 0) {
            TransferRobotOutboundVO transferRobotOutboundVO = robotaiApiServiceClient.pushRobotai(robotOutboundDTO, requestId);
            if (String.valueOf("9999").equals(transferRobotOutboundVO.getCode())) {
                throw new CommonException(MarketingErrorInfo.REQUEST_FAIL_ERROR, transferRobotOutboundVO.getMessage());
            }
            if ("00".equals(transferRobotOutboundVO.getCode())) {
                JSONObject object = JSON.parseObject(transferRobotOutboundVO.getData().toString());
                JSONArray array = object.getJSONArray("unsuccessfulData");
                if (array.size() > 0) {
                    String content = String.format("客服接口返回错误列表数据：%s", transferRobotOutboundVO.getData().toString());
                    alarmApiClient.sendAlarm(content, "客服接口返回警示信息", AlarmSendCodeEnum.EXCEPTION_SAMOYE.getCode());
                }
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 解密、规则校验
     *
     * @param user
     * @param isCheck
     * @return
     */
    private void encodeMapping(MarketingPreUserDetailDTO user, String type, Integer isCheck) {
        String content = "";
        switch (type) {
            case "cell":
                content = StringUtils.isBlank(user.getCell()) ? "" : user.getCell();
                break;
            case "id":
                content = StringUtils.isBlank(user.getId()) ? "" : user.getId();
                break;
            case "name":
                content = StringUtils.isBlank(user.getName()) ? "" : user.getName();
                break;
        }
        if (DecodeGrpcClient.isMd5(content)) {
            //cell md5
            content = RpcClientProxy.decode(content, type, "md5", "");
            if (StringUtils.isBlank(content) && "cell".equals(type)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_1.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
        } else if (content.length() == 64) {
            //cell sha256
            content = RpcClientProxy.decode(content, type, "sha", "");
            if (StringUtils.isBlank(content) && "cell".equals(type)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_2.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
        }
        //明文规则校验
        UserValidator userValidator = new UserValidator(isCheck);
        if (StringUtils.isNotBlank(content) && "cell".equals(type)) {
            if (!userValidator.validatePhone(content)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_3.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            user.setCell(BrCipherMaker.getInstance().encode(content));
        }
        if (StringUtils.isNotBlank(content) && "id".equals(type)) {
            if (!userValidator.validateId(content)) {
                user.setId(content);
                //user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            user.setId(BrCipherMaker.getInstance().encode(content));
        }
        if (StringUtils.isNotBlank(content) && "name".equals(type)) {
            if (!userValidator.validateName(content)) {
                user.setName(content);
                /** 2022/8/11 17:14 业务需求变更，name字段是否成功解密不影响数据状态 */
//                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            user.setName(BrCipherMaker.getInstance().encode(content));
        }
    }

    /**
     * 获取营销人员数据状态
     *
     * @param dto
     * @return
     */
    @Override
    public Result<MarketingPreUserSyncDetailVO> getMarketingPreUserSyncStatus(MarketingPreUserSyncStatusDTO dto) {
        Result<MarketingPreUserSyncDetailVO> marketingPreUserSyncDetailVOResult = new Result<>();
        MarketingPreUserSyncDetailVO vo = new MarketingPreUserSyncDetailVO();
        MarketingSyncInfoExample syncInfoExample = new MarketingSyncInfoExample();
        syncInfoExample.createCriteria().andApiCodeEqualTo(dto.getApiCode()).andCusBatchEqualTo(dto.getTaskId())
                .andRequestBatchEqualTo(dto.getRequestId());
        List<MarketingSyncInfo> marketingSyncInfos = new ArrayList<>();
        String uploadKey = RedisKeyConstant.uploadKey.concat(":").concat(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        Boolean dbBad = Boolean.FALSE;
        Boolean redisBad = Boolean.FALSE;
        Boolean selectBad = Boolean.FALSE;
        try {
            //todo 模拟异常上线后要删除
            mockDbOrRedisError(1, dto.getApiCode());
            marketingSyncInfos = marketingSyncInfoMapper.selectByExample(syncInfoExample);
            if (marketingSyncInfos.size() <= 0) {
                selectBad = Boolean.TRUE;
            }
        } catch (Exception ex) {
            dbBad = Boolean.TRUE;
        }
        //数据库未查得 查询redis
        if (dbBad || selectBad) {
            Boolean sismember = Boolean.FALSE;
            try {
                //todo 模拟异常上线后要删除
                mockDbOrRedisError(2, null);
                sismember = redisChgService.sismember(uploadKey, dto.getRequestId());
            } catch (Exception ex) {
                log.error(ex.getMessage());
                redisBad = Boolean.TRUE;
            }
            if (!redisBad && sismember) {
                vo.setApiCode(dto.getApiCode());
                vo.setTaskId(dto.getTaskId());
                vo.setRequestId(dto.getRequestId());
                vo.setStatus(1);
                marketingPreUserSyncDetailVOResult.setMessage("运行中");
                return marketingPreUserSyncDetailVOResult.setCode(ResultCode.SUCCESS.getValue()).setDate(vo);
            } else if (!redisBad) {
                throw new CommonException(MarketingErrorInfo.DATA_NOT_EXIST_ERROR);
            }
        }
        //数据库异常并且redis异常
        if (dbBad && redisBad) {
            throw new CommonException(MarketingErrorInfo.UNKNOWN_ERROR);
        }
        if (selectBad) {
            throw new CommonException(MarketingErrorInfo.DATA_NOT_EXIST_ERROR);
        }
        MarketingSyncInfo syncInfo = marketingSyncInfos.get(0);
        vo.setApiCode(syncInfo.getApiCode());
        vo.setTaskId(syncInfo.getCusBatch());
        vo.setRequestId(syncInfo.getRequestBatch());
        vo.setStatus(syncInfo.getStatus());

        if (StatusConstants.MarketingPreUserStatus_fail.equals(syncInfo.getStatus())
                || StatusConstants.MarketingPreUserStatus_success_part.equals(syncInfo.getStatus())) {
            MarketingSyncErrorInfo errorInfo = marketingSyncErrorInfoMapper.selectByPrimaryKey(syncInfo.getErrorId());
            if (errorInfo != null) {
                List<MarketingPreUserErrorDetailVO> o = JSON.parseObject(errorInfo.getErrorInfo(), new TypeReference<List<MarketingPreUserErrorDetailVO>>() {
                }.getType());
                vo.setErrorInfo(o);
            }
        }
        switch (syncInfo.getStatus()) {
            case 1:
                marketingPreUserSyncDetailVOResult.setMessage("运行中");
                break;
            case 2:
                marketingPreUserSyncDetailVOResult.setMessage("全部成功");
                break;
            case 3:
                marketingPreUserSyncDetailVOResult.setMessage("全部失败");
                break;
            case 4:
                marketingPreUserSyncDetailVOResult.setMessage("部分成功");
                break;
            default:
        }
        return marketingPreUserSyncDetailVOResult.setCode(ResultCode.SUCCESS.getValue()).setDate(vo);
    }

    /**
     * 查询客户信息接口
     *
     * @param cid
     * @param apiCode
     * @param custNum
     * @return
     */
    @Override
    public Result<MarketingSyncUser> queryCustInfo(String cid, String apiCode, String custNum, String cell) {
        Result<MarketingSyncUser> result = new Result<>();
        //校验
        if ((StringUtils.isBlank(cid) && StringUtils.isBlank(apiCode)) || (StringUtils.isBlank(custNum) && StringUtils.isBlank(cell))) {
            return result.setCode(ResultCode.PARAM_ERROR.getValue()).setMessage("参数缺失");
        }
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        if (StringUtils.isNotBlank(apiCode)) {
            customerExample.createCriteria().andApiCodeEqualTo(apiCode);
        } else if (StringUtils.isNotBlank(cid)) {
            customerExample.createCriteria().andCidEqualTo(cid);
        }
        List<MarketingCustomer> cList = marketingCustomerMapper.selectByExample(customerExample);
        if (cList != null && !cList.isEmpty()) {
            List<MarketingSyncUser> list = new ArrayList<>();
            for (MarketingCustomer customer : cList) {
                String ac = customer.getApiCode();
                if (StringUtils.isNotBlank(ac)) {
                    try {
                        MarketingSyncUser vo = marketingUserMapper.selectSyncUserByCustNum(ac, custNum, cell);
                        if (vo != null) {
                            list.add(vo);
                        }
                    } catch (BadSqlGrammarException sqlGrammarException) {
                        log.warn(String.format("apiCode表不存在：%s", ac), sqlGrammarException);
                    }
                }
            }

            // 不同apicode上传数据，根据applet_time取最新一条
            Optional<MarketingSyncUser> optional = list.stream().sorted(Comparator.comparing(MarketingSyncUser::getAppletTime).reversed()).findFirst();
            if (optional.isPresent()) {
                MarketingSyncUser vo = optional.get();
                return result.setCode(ResultCode.SUCCESS.getValue()).setDate(vo).setMessage("成功");
            }
        }
        return result.setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }

    //初始化一个ForkJoinPool
    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool(Math.min(0x7fff, Runtime.getRuntime().availableProcessors()),
//            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            new ForkJoinPool.ForkJoinWorkerThreadFactory() {
                @Override
                public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                    ForkJoinWorkerThread workerThread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    workerThread.setName("br-push-forkJoin-pool-" + workerThread.getPoolIndex());
                    return workerThread;
                }
            },
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    log.error("推送客服任务异常：任务线程:[{}]\n{}", t.getName(), e.getMessage(), e);
                }
            },
            // 队列模式，false 后人先出，true 先进先出
            false);

    private final String cidKey = "marketing:innerapi:transfer:cid:";

    @Override
    @Transactional
    public synchronized Result<Boolean> pushPersonalTransferData(Long infoId) {
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        try {
            // 传输标记
            final int transferStatus;
            // 1 根据保存到队列的ID查询记录对应的ApiCode、RequestId
            List<MarketingTransferInfo> list = marketingTransferInfoMapper.findApiCodeRequestIdByIdList(infoId);
            if (CollectionUtils.isEmpty(list)) {
                result.setDate(false);
                String smg = String.format("主键为[%s]的客户转化基础信息不存在,该信息直接消费,不再重放队列", infoId);
                result.setMessage(smg);
                alarmClient.sendAlarm(smg, "接口转化数据同步到智能客服警告", AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
                return result;
            }
            MarketingTransferInfo info = list.get(0);
            String apiCode = info.getApiCode();
            Date createTime = ObjectUtils.isEmpty(info.getCreateTime()) ? new Date() : info.getCreateTime();
            result.setDate(true);
            tailorApiCodeMap = marketingCommonConfig.getCustomerTransferIsYx();
            if (!tailorApiCodeMap.getOrDefault(apiCode, false)) {
                try {
                    info.setId(infoId);
                    pushTransferData(info);
                    result.setDate(false);
                } catch (Exception e) {
                    String smg = String.format("主键[%d];apiCode[%s];requestId[%s]推送错误！\n%s", infoId, apiCode, info.getRequestId(), e.getMessage());
                    log.error(smg, e);
                }
                return result;
            }
            //        String yyyyMMdd = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            // 格式化入库时间
            String yyyyMMdd = createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    .format(DateTimeFormatter.BASIC_ISO_DATE);
            // 如果是最后一次传
            if ("1".equals(info.getLast())) {
                transferStatus = 2;
            } else {
                transferStatus = 0;
            }
            String requestId = info.getRequestId();
            // 2 获取分表后缀
            String key = cidKey.concat(apiCode);
            String cId;
            try {
                cId = redisChgService.get(key);
                if (StringUtils.isEmpty(cId)) {
                    cId = tableCreateService.getTcId(apiCode);
                    // 缓存七天
                    redisChgService.setex(key, cId, 7 * 86400);
                }
            } catch (Exception e) {
                cId = tableCreateService.getTcId(apiCode);
                log.error(e.getMessage(), e);
            }
            final String tcId = cId;
            // 3 获取转化数据,
            MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
            example.createCriteria().andApiCodeEqualTo(apiCode).andRequestIdEqualTo(requestId);
            example.settCid(tcId);
            int page = 1;
            final int pageSize = 2000;
            final int retrySum = 2;
            List<PushTransferCustomerLog> logListAll = new ArrayList<>();
            label:
            for (; ; ) {
                PageHelper.startPage(page, pageSize, true).setOrderBy(" id ASC");
                List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
                PageInfo<MarketingTransferSyncUser> pageList = new PageInfo<>(transferList);
                transferList = transferList.stream().filter(syncUser -> StringUtils.isNotBlank(syncUser.getInsertTime()))
                        .collect(Collectors.toList());
                int size = transferList.size();
                // 总页数
                int pages = pageList.getPages();
                boolean b = true;
                // 处理开始标记
                switch (transferStatus) {
                    case 0:
                        if (pageList.getTotal() < 1) {
                            if (info.getActualNum() < 1) {
                                PushTransferCustomerLog pushTransferCustomerLog = sendTransferDataToCustomer(
                                        new PushCustomerRequestDTO(apiCode, transferStatus, transferList), 3, size);
                                pushTransferCustomerLog.setTransferStatus(transferStatus);
                                logListAll.add(pushTransferCustomerLog);
                                break label;
                            } else {
                                String smg = String.format("last[0];infoId[%d];apiCode[%s];requestId[%s];tcId[%s]在[%s]转化未完成，未获取到转化数据"
                                        , infoId, apiCode, requestId, tcId, yyyyMMdd);
                                sendAlarm(smg);
                                return result;
                            }
                        } else if (size < 1) {
                            PushTransferCustomerLog pushTransferCustomerLog = sendTransferDataToCustomer(
                                    new PushCustomerRequestDTO(apiCode, transferStatus, transferList), 3, size);
                            pushTransferCustomerLog.setTransferStatus(transferStatus);
                            logListAll.add(pushTransferCustomerLog);
                            break;
                        }
                        b = asyncPush(transferList, logListAll);
                        break;
                    case 2:
                        if (page == pages) {
                            List<MarketingTransferSyncUser> listEnd;
                            if (size > 200) {
                                int len = (size - 200);
                                b = asyncPush(transferList.subList(0, len), logListAll);
                                listEnd = transferList.subList(len, size);
                            } else {
                                // 检查是否有开始标记
                                int countStatus = pushTransferCustomerLogMapper.countByApiCodeAndTransferInfoTimeAndPushStatus(apiCode, createTime, "0,2");
                                if (countStatus > 0 || size < 1) {
                                    listEnd = transferList;
                                } else {
                                    // 检查转化信息表是否出现过last为0数据
                                    List<Long> ids = marketingTransferInfoMapper.countByApiCodAndLast(apiCode, createTime, "0");
                                    if (ids.size() == 0) {
                                        int len = size / 2;
                                        PushTransferCustomerLog pushLog = sendTransferDataToCustomer(
                                                new PushCustomerRequestDTO(apiCode, 0, transferList.subList(0, len)), retrySum, len);
                                        pushLog.setTransferStatus(0);
                                        logListAll.add(pushLog);
                                        listEnd = transferList.subList(len, size);
                                    } else {
                                        List<Long> infoIds = pushTransferCustomerLogMapper.findInfoIdListByCodeAndInfoTimeAndTransferStatus(apiCode, createTime, 0);
                                        if (infoIds.size() < ids.size()) {
                                            ids.removeAll(infoIds);
                                            listEnd = null;
                                            for (Long idf : ids) {
                                                pushPersonalTransferData(idf);
                                            }
                                        } else {
                                            listEnd = transferList;
                                        }
                                    }
                                }
                            }
                            if (listEnd != null) {
                                // 检查是否全部推送完成
                                int countStatus = pushTransferCustomerLogMapper.countByApiCodeAndTransferInfoTimeAndPushStatus(apiCode, createTime, "1,3");
                                if (countStatus < 1) {
                                    PushTransferCustomerLog pushLog = sendTransferDataToCustomer(
                                            new PushCustomerRequestDTO(apiCode, transferStatus, listEnd), retrySum, listEnd.size());
                                    pushLog.setTransferStatus(transferStatus);
                                    logListAll.add(pushLog);
                                } else {
                                    PushCustomerRequestDTO pushCustomerRequestDTO = new PushCustomerRequestDTO(apiCode, transferStatus, listEnd);
                                    logListAll.add(new PushTransferCustomerLog(apiCode
                                            , pushCustomerRequestDTO.getJsonData()
                                            , listEnd.size()
                                            , 1
                                            , transferStatus
                                    ));
                                }
                            }
                        } else if (pages < 1) {
                            if (info.getActualNum() < 1) {
                                int countStatus = pushTransferCustomerLogMapper.countByApiCodeAndTransferInfoTimeAndPushStatus(apiCode, createTime, "0,2");
                                if (countStatus > 0) {
                                    countStatus = pushTransferCustomerLogMapper.countByApiCodeAndTransferInfoTimeAndPushStatus(apiCode, createTime, "1,3");
                                    if (countStatus < 1) {
                                        PushTransferCustomerLog pushTransferCustomerLog = sendTransferDataToCustomer(
                                                new PushCustomerRequestDTO(apiCode, transferStatus, transferList), 3, size);
                                        pushTransferCustomerLog.setTransferStatus(transferStatus);
                                        logListAll.add(pushTransferCustomerLog);
                                        break label;
                                    }
                                }
                                PushCustomerRequestDTO pushCustomerRequestDTO = new PushCustomerRequestDTO(apiCode, transferStatus, null);
                                logListAll.add(new PushTransferCustomerLog(apiCode
                                        , pushCustomerRequestDTO.getJsonData()
                                        , size
                                        , 1
                                        , transferStatus
                                ));
                            } else {
                                String smg = String.format("last[1]infoId[%d];apiCode[%s];requestId[%s];tcId[%s]在[%s]转化未完成，未获取到转化数据"
                                        , infoId, apiCode, requestId, tcId, yyyyMMdd);
                                sendAlarm(smg);
                                return result;
                            }
                        } else {
                            if (size < 1) {
                                PushTransferCustomerLog pushTransferCustomerLog = sendTransferDataToCustomer(
                                        new PushCustomerRequestDTO(apiCode, 0, transferList), 3, size);
                                pushTransferCustomerLog.setTransferStatus(0);
                                logListAll.add(pushTransferCustomerLog);
                                break;
                            }
                            b = asyncPush(transferList, logListAll);
                        }
                        break;
                    default:
                        log.error("未知的标记:{}", transferStatus);
                }
                if (!b) {
                    String smg = String.format("infoId[%d];apiCode[%s];requestId[%s];tcId[%s]在[%s]中推送中线程任务失败"
                            , infoId, apiCode, requestId, tcId, yyyyMMdd);
                    sendAlarm(smg);
                    return result;
                }
                // 总记录数
                long total = pageList.getTotal();
                if ((pages == page && transferList.size() <= total) || transferList.size() == 0) {
                    break;
                }
                page++;
            }
            // 5 推送记录日志
            if (logListAll.size() > 0) {
                List<PushTransferCustomerLog> collect = logListAll.stream().peek(failLog -> {
                    failLog.setRequestId(requestId);
                    failLog.settCid(tcId);
                    failLog.setTransferInfoId(infoId);
                    failLog.setTransferInfoTime(createTime);
                }).collect(Collectors.toList());
                boolean bool = pushTransferCustomerLogMapper.bathInsert(collect);
                if (bool) {
                    log.info("推送客服数据已保存记录，本次保存[{}]", collect.size());
                } else {
                    result.setMessage("保存记录失败！");
                    String smg = String.format("保存推送记录失败：apiCode:{%s};requestId:{%s};tcId:{%s};infoId:{%d};失败数据量:{%d}"
                            , apiCode, requestId, tcId, infoId, collect.size());
                    sendAlarm(smg);
                    return result;
                }
            } else {
                return result;
            }
            result.setMessage("成功");
            result.setDate(false);
            return result;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(e.getMessage());
            return result;
        }
    }


    private boolean asyncPush(List<MarketingTransferSyncUser> transferSyncUserList, List<PushTransferCustomerLog> logList) throws Throwable {
        // 4 推送转化数据,每次200条，失败后重试3次，标记为同步中
        PushTransferDataToCustomerTask task = new PushTransferDataToCustomerTask(transferSyncUserList, 0, transferSyncUserList.size());
        List<PushTransferCustomerLog> logs = FORK_JOIN_POOL.invoke(task);
        logList.addAll(logs);
        boolean b = FORK_JOIN_POOL.awaitQuiescence(5, TimeUnit.SECONDS);
        if (b) {
            if (task.isCompletedAbnormally()) {
                Throwable exception = task.getException();
                if (exception != null) {
                    log.error(exception.getMessage(), exception);
                    throw exception;
                }
                return false;
            }
            return true;
        }
        if (!task.isDone()) {
            task.isCancelled();
        }
        log.warn("等待任务超时，任务已经处理完成");
        return false;
    }


    private void sendAlarm(String smg) {
        log.warn(smg);
        alarmClient.sendAlarm(smg, "接口转化(私人订制)数据同步到智能客服警告", AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
    }

    private void sendAlarm(String smg, String key) {
        log.warn(smg);
        alarmClient.sendAlarm(smg, "接口转化(私人订制)数据同步到智能客服警告", AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
        redisChgService.incrBy(key, -1);
        redisChgService.expire(key, getKeyExpiration());
    }

    private synchronized Long getApiCodeCount(String key) {
        Long incr = redisChgService.incr(key);
        int keyExpiration = getKeyExpiration();
        redisChgService.expire(key, keyExpiration);
        return incr;
    }

    /**
     * 获取当前时间到第二天凌晨的秒
     *
     * @dateTime 2021/10/19 9:21
     */
    private int getKeyExpiration() {
        final LocalDateTime now = LocalDateTime.now();
        // 当前毫秒数
        long l = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime localDateTime = now.plusDays(1);
        // 第二天凌晨毫秒数
        long l1 = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (int) (l1 - l) / 1000;
    }


    /**
     * 发送转化数据到客服
     *
     * @param requestDTO 数据集合
     * @param retrySum   指定最大重试次数，包括第一次执行，默认1次
     * @param rowSize    发送数据量
     * @return false 失败；true 成功； 失败需要写入失败日志，以便后续补发
     * @author Guo Zeqiang
     * @dateTime 2021/10/13 17:51
     */
    private PushTransferCustomerLog sendTransferDataToCustomer(final PushCustomerRequestDTO requestDTO
            , int retrySum
            , final int rowSize) {
        int count = 1;
        if (retrySum < 1) {
            retrySum = 1;
        }
        final HttpHeaders tempHeaders = new HttpHeaders();
        tempHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        tempHeaders.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
        tempHeaders.setAccept(Collections.singletonList(MediaType.ALL));
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("apiCode", requestDTO.getApiCode());
        postParameters.add("jsonData", requestDTO.getJsonData());
        final HttpEntity<MultiValueMap<String, Object>> stringHttpEntity = new HttpEntity<>(postParameters, tempHeaders);
        ResponseEntity<String> responseEntity = null;
        HttpStatus statusCode = null;
        String body;
        JSONObject result;
        String code;
        int value;
        do {
            log.info("########################第【{}/{}】次调用接口", count, retrySum);
            try {
                responseEntity = restTemplate.postForEntity(robotOutboundUrl
                        , stringHttpEntity, String.class);
                statusCode = responseEntity.getStatusCode();
                value = statusCode.value();
                body = responseEntity.getBody();
                result = JSONObject.parseObject(body);
                code = String.valueOf(result.get("code"));
                // 重试休眠
                TimeUnit.SECONDS.sleep(count < 4 ? count : 3);
            } catch (RestClientException | InterruptedException e) {
                value = -1;
                body = "";
                result = null;
                code = "";
                log.error(e.getMessage(), e);
            }
            count++;
        } while ((value != 200 || !"00".equals(code)) && count <= retrySum);
        int pushStatus = 0;
        if (ObjectUtils.isEmpty(responseEntity) || ObjectUtils.isEmpty(statusCode)) {
            String smg = String.format("%s : apiCode[%s]发送重试[%d]次后依然失败！接口不能正常访问"
                    , LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), requestDTO.getApiCode(), count - 1);
            alarmClient.sendAlarm(smg, "\n接口转化(私人订制)数据同步到智能客服失败", AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
            return new PushTransferCustomerLog(
                    requestDTO.getApiCode()
                    , requestDTO.getJsonData()
                    , rowSize
                    , 1
            );
        }
        String reasonPhrase = statusCode.getReasonPhrase();
        log.info("智能客服接口HttpStatus[code:{};reasonPhrase:{}]", value, reasonPhrase);
        if (value != 200 || !"00".equals(code)) {
            // 客服业务中出现的非正常状态码全部补偿
            pushStatus = 1;
            String smg = String.format("apiCode:[%s]发送重试[%d]次后依然失败！" +
                    "\n接口返回http状态码[%d],http短语[%s];" +
                    "\n应答消息[%s]", requestDTO.getApiCode(), count, value, reasonPhrase, body);
            alarmClient.sendAlarm(smg, "\n接口转化(私人订制)数据同步到智能客服失败", AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
        }
        return new PushTransferCustomerLog(
                requestDTO.getApiCode()
                , requestDTO.getJsonData()
                , body
                , code
                , result.get("message") == null ? "" : result.get("message").toString()
                , result.get("accessNumber") == null ? result.get("swiftNumber") == null
                ? "" : result.get("swiftNumber").toString() : result.get("accessNumber").toString()
                , rowSize
                , value
                , reasonPhrase
                , pushStatus
        );
    }


    // 处理任务
    private class PushTransferDataToCustomerTask extends RecursiveTask<List<PushTransferCustomerLog>> {

        private static final long serialVersionUID = 4930523513045970753L;
        private final List<MarketingTransferSyncUser> list;
        private final int start;
        private final int end;

        public PushTransferDataToCustomerTask(List<MarketingTransferSyncUser> list, int start, int end) {
            super();
            this.list = list;
            this.start = start;
            this.end = end;
        }

        @SneakyThrows
        @Override
        protected List<PushTransferCustomerLog> compute() {
            List<PushTransferCustomerLog> logList = new ArrayList<>();
            int threshold = 200;
            if ((end - start) <= threshold) {
                log.info("++++++++++++++++=====分段数据：【{}】-【{}】", start, end);
                List<MarketingTransferSyncUser> transferSyncUserList = list.subList(start, end);
                int transferStatus = 0;
                int retrySum = 2;
                PushTransferCustomerLog log = sendTransferDataToCustomer(
                        new PushCustomerRequestDTO(transferSyncUserList.get(0).getApiCode(), transferStatus, transferSyncUserList)
                        , retrySum, transferSyncUserList.size());
                log.setTransferStatus(transferStatus);
                logList.add(log);
            } else {
                int middle = (end + start) / 2;
                PushTransferDataToCustomerTask taskLeft = new PushTransferDataToCustomerTask(list, start, middle);
                PushTransferDataToCustomerTask taskRight = new PushTransferDataToCustomerTask(list, middle, end);
                invokeAll(taskLeft, taskRight);
                logList.addAll(taskLeft.join());
                logList.addAll(taskRight.join());
            }
            return logList;
        }
    }


    @Override
    public List<TransferRobotOutboundVO<UnsuccessfulData>> pushTransferData(MarketingTransferInfo transferInfo) {
        Assert.notNull(transferInfo, "转化信息不可为null");
        String apiCode = transferInfo.getApiCode();
        Assert.notNull(apiCode, "'apiCode'不可为null");
        String requestId = transferInfo.getRequestId();
        Assert.notNull(transferInfo, "'requestId'不可为null");
        String title = "接口转化(通用标准)数据同步到智能客服警告";
        // 1 获取分表后缀
        String key = cidKey.concat(apiCode);
        String tcId;
        try {
            tcId = redisChgService.get(key);
            if (StringUtils.isEmpty(tcId)) {
                tcId = tableCreateService.getTcId(apiCode);
                SecureRandom random = new SecureRandom();
                // 缓存3~7天
                redisChgService.setex(key, tcId, (random.nextInt(7) % 5 + 3) * 86400);
            }
        } catch (Exception e) {
            tcId = tableCreateService.getTcId(apiCode);
            log.error(e.getMessage(), e);
        }
        // 2 获取转化数据
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andRequestIdEqualTo(requestId);
        example.settCid(tcId);
        int page = 1;
        final int pageSize = 500;
        List<TransferRobotOutboundVO<UnsuccessfulData>> list = new ArrayList<>();
        for (; ; ) {
            PageHelper.startPage(page, pageSize, true).setOrderBy(" id ASC");
            List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
            PageInfo<MarketingTransferSyncUser> pageInfo = new PageInfo<>(transferList);
            if (CollectionUtils.isEmpty(transferList)) {
                String smg;
                if (transferInfo.getActualNum() < 1) {
                    smg = String.format("转化信息为【apiCode:[%s],RequestId:[%s],infoId:[%s],tcId:[%s]】没有找到对应的转化数据，此消息不再放回队列！日期:%s"
                            , apiCode, transferInfo.getRequestId(), transferInfo.getId(), tcId, DateUtils.getNowyyyy_MM_dd());
                } else {
                    smg = String.format("转化信息为【apiCode:[%s],RequestId:[%s],infoId:[%s],tcId:[%s]】转化接口接收(%d)条转化数据，" +
                                    "但未在转化详情中找到，该消息直接消费！日期:%s", apiCode
                            , transferInfo.getRequestId(), transferInfo.getId(), tcId, transferInfo.getActualNum()
                            , DateUtils.getNowyyyy_MM_dd());
                }
                alarmClient.sendAlarm(smg, title, AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
                PushTransferRobotaiLog robotaiLog = new PushTransferRobotaiLog(
                        transferInfo.getId()
                        , apiCode
                        , transferInfo.getRequestId()
                        , ""
                        , ""
                        , smg
                        , transferList.size()
                        , ""
                        , tcId
                );
                robotaiLog.setPushStatus(3);
                pushTransferRobotaiLogService.save(robotaiLog);
                break;
            }
            /*
             * D20211128海尔消金-转化需求-3710018
             * 海尔消金通过转化接口usertype判断转化状态。
             * usertype	3、4	推送	已转化
             * usertype	非3、4	不推送	未转化
             */
            List<String> haierApiCode = marketingCommonConfig.getHaierApiCode();
            if (haierApiCode.contains(apiCode)) {
                transferList = transferList.stream().filter(syncUser -> {
                    String userType = syncUser.getUserType();
                    if (userType.equals("3") || userType.equals("4")) {
                        syncUser.setIfTransform("1"); // 2021-12-8 10:39:29 添加默认转化状态
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());
            }

            /**
             * D20220214玖富转化数据传输逻辑-玖富apiCode
             * 转化数据剔除是否申请提现（is_apply）为Y，以及授信审核结果（shouxin_result）为DENY，所有场景都是。
             * applyLoan=1	applyResult=0
             */

            List<String> jfApiCode = marketingCommonConfig.getJfApiCode();
            if (jfApiCode.contains(apiCode)) {
                transferList = transferList.stream().filter(user -> {
                    String reserveField1 = user.getReserveField1();
                    if (StringUtils.isNotBlank(reserveField1)) {
                        JSONObject jsonObject = JSONObject.parseObject(reserveField1);
                        if ("0".equals(user.getApplyResult()) && "1".equals(jsonObject.getString("applyLoan"))) {
                            user.setIfTransform("1");
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
            }

            if (transferList.size() > 0) {
                TransferRobotOutboundDTO robotOutboundDTO = getTransferRobotOutbound(transferInfo, transferList);
                TransferRobotOutboundVO<UnsuccessfulData> outboundVO = pushTransferData(robotOutboundDTO, transferInfo);
                if (!outboundVO.getAccessNumber().equals("-1")) {
                    pushTransferRobotaiLogService.saveLog(transferInfo, robotOutboundDTO, outboundVO);
                }
                list.add(outboundVO);
            }
            if (page >= pageInfo.getPages()) {
                break;
            }
            page++;
        }
        return list;
    }

    @Override
    public TransferRobotOutboundVO<UnsuccessfulData> pushTransferData(TransferRobotOutboundDTO dto, MarketingTransferInfo transferInfo) {
        Assert.notNull(dto, String.format("转化数据不存在或已经规则过滤掉!\n转化信息[transferInfoId=%d;apiCode=%s;requestId=%s]"
                , transferInfo.getId(), transferInfo.getApiCode(), transferInfo.getRequestId()));
        TransferRobotOutboundVO<UnsuccessfulData> outboundVO;
        try {
            outboundVO = robotaiApiServiceClient.pushRobotai(dto, transferInfo.getRequestId());
            if (outboundVO.getCode().equals("00")) {
                Object o = (outboundVO.getData());
                JSONObject object = JSON.parseObject(o.toString());
                Object unsuccessfulData = object.get("unsuccessfulData");
                JSONArray array = JSON.parseArray(unsuccessfulData.toString());
                if (array.size() < 1) {
                    outboundVO.setAccessNumber("-1");
                    return outboundVO;
                }
            } else if (outboundVO.getCode().equals("9999")) {
                outboundVO.setCode("");
                outboundVO.setAccessNumber("");
                outboundVO.setData(new UnsuccessfulData());
            } else {
                if (outboundVO.getAccessNumber() == null) {
                    outboundVO.setAccessNumber("");
                }
                if (outboundVO.getData() == null) {
                    outboundVO.setData(new UnsuccessfulData());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            outboundVO = new TransferRobotOutboundVO<>();
            outboundVO.setMessage(e.getMessage());
        }
        return outboundVO;
    }

    final static String hasTransfer = "1";

    final static String noHasTransfer = "0";

    @Override
    public TransferRobotOutboundDTO getTransferRobotOutbound(MarketingTransferInfo transferInfo
            , List<MarketingTransferSyncUser> transferList) {
        String title = "接口转化(通用标准)数据同步到智能客服警告";
        Assert.notNull(transferInfo, "转化信息不存在!");
        TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
        String apiCode = transferInfo.getApiCode();
        if (CollectionUtils.isEmpty(transferList)) {
            String smg = String.format("apiCode:[%s],RequestId:[%s],transferInfoId:[%s]转化结果不存在！日期:%s", apiCode
                    , transferInfo.getRequestId(), transferInfo.getId(), DateUtils.getNowyyyy_MM_dd());
            alarmClient.sendAlarm(smg, title, AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
            return null;
        }
        Set<String> set = transferList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
        Map<String, MarketingSyncUser> map = preUserByTask.stream().collect(Collectors.toMap(
                MarketingSyncUser::getCustNum, syncUser -> syncUser
                , (v1, v2) -> StringUtils.isNotBlank(v2.getCell()) && !ObjectUtils.isEmpty(v2.getCreateTime())
                        && v2.getCreateTime().after(v1.getCreateTime()) ? v2 : v1));
        Assert.notNull(preUserByTask, "'MarketingSyncUser'不可为null");
        List<ConversionData> conversionDataArray = new ArrayList<>();
        transferList.forEach(transfer -> {
            ConversionData conversionData = new ConversionData();
            conversionData.setDataId(transfer.getId().toString());
            conversionData.setCid(transfer.getCid());
            conversionData.setCaseNum(transfer.getCustNum());
            conversionData.setGroupType(transfer.getUserType());
            conversionData.setInversionStatus(hasTransfer.equals(transfer.getIfTransform())
                    ? "0"
                    : (noHasTransfer.equals(transfer.getIfTransform()) ? "1" : transfer.getIfTransform()));
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            if (map.containsKey(transfer.getCustNum())) {
                MarketingSyncUser marketingSyncUser = map.get(transfer.getCustNum());
                conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
                conversionData.setTaskId(marketingSyncUser.getCusBatch());
            } else {
                conversionData.setPhone("");
                conversionData.setTaskId("");
            }
            TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
            BeanUtils.copyProperties(transfer, vo);
            conversionData.setInversionInfo(JSON.toJSONString(vo));
            conversionDataArray.add(conversionData);
        });
        robotOutboundDTO.setApiCode(apiCode);
        robotOutboundDTO.setJsonData(new TransferJsonDataDTO(conversionDataArray));
        return robotOutboundDTO;
    }

    @Override
    public Result<Boolean> consumerCommonBlack(Long id) {
        Boolean isContiue = false;
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }
        Long minId = null;
        boolean action = Boolean.TRUE;
        while (action) {
            List<PhoneBlack> phoneBlacks = phoneBlackMapper.selectDateByIdRang(id, minId);
            if (phoneBlacks.size() <= 0) {
                action = Boolean.FALSE;
                continue;
            }
            minId = phoneBlacks.get(phoneBlacks.size() - 1).getId();
            PushBlackReqDTO pushBlackReqDTO = new PushBlackReqDTO();
            pushBlackReqDTO.setUsers(phoneBlacks);
            pushBlackReqDTO.setApiCode(localFile.getApiCode());
            Result result = pushCommonBlack(pushBlackReqDTO);
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                log.error(String.format("推送黑名单报错：%s", result.getData()));
                if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                    RetryMainLog retryMainLog = new RetryMainLog();
                    retryMainLog.setRetryType(1);
                    retryMainLog.setRetryParam(JSON.toJSONString(pushBlackReqDTO));
                    retryMainLog.setRetryParamType(pushBlackReqDTO.getClass().getName());
                    retryMainLog.setRetryService("pushRuleServiceImpl");
                    retryMainLog.setRetryMethod("pushCommonBlack");
                    retryMainLog.setRetryNum(0);
                    retryMainLog.setRetryMaxNum(3);
                    retryMainLog.setRetryStatus(1);
                    retryMainLog.setCreateTime(new Date());
                    retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                    retryMainLogMapper.insertSelective(retryMainLog);
                }
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

    @Override
    public Result<Boolean> consumerBlack(Long id) {
        Integer soleNum = 20;
        Boolean isContinue = Boolean.FALSE;
        MarketingTransferInfo transferInfo = marketingTransferInfoMapper.selectByPrimaryKey(id);
        if (transferInfo == null) {
            return new Result<>()
                    .setCode(ResultCode.SUCCESS.getValue())
                    .setDate(isContinue)
                    .setMessage("数据不存在");
        }
        String tcId = tableCreateService.getTcId(transferInfo.getApiCode());
        if (tcId == null) {
            return new Result<>()
                    .setCode(ResultCode.SUCCESS.getValue())
                    .setDate(isContinue)
                    .setMessage(String.format("apiCode:%s 未维护cid信息", transferInfo.getApiCode()));
        }
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(transferInfo.getApiCode()).
                andRequestIdEqualTo(transferInfo.getRequestId());
        example.settCid(tcId);
        List<MarketingTransferSyncUser> marketingTransferSyncUsers = marketingTransferSyncUserMapper.selectByExample(example);
        int page = marketingTransferSyncUsers.size() / 500 + (marketingTransferSyncUsers.size() % 500) == 0 ? 0 : 1;
        int yu = marketingTransferSyncUsers.size() % 500;
        for (int i = 1; i <= page; i++) {
            int start = (i - 1) * 500;
            int end = 0;
            if (i == page && yu > 0) {
                end = (i - 1) * 500 + yu;
            } else {
                end = i * 500 - 1;
            }
            List<MarketingTransferSyncUser> users = marketingTransferSyncUsers.subList(start, end);
            Result result = pushBlack(users);
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                log.error(String.format("推送黑名单报错：%s", result.getData()));
                if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                    RetryMainLog retryMainLog = new RetryMainLog();
                    retryMainLog.setRetryType(1);
                    retryMainLog.setRetryParam(JSON.toJSONString(users));
                    retryMainLog.setRetryParamType(users.getClass().getName());
                    retryMainLog.setRetryService("pushRuleServiceImpl");
                    retryMainLog.setRetryMethod("pushBlack");
                    retryMainLog.setRetryNum(0);
                    retryMainLog.setRetryMaxNum(3);
                    retryMainLog.setRetryStatus(1);
                    retryMainLog.setCreateTime(new Date());
                    retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                    retryMainLogMapper.insertSelective(retryMainLog);
                }
            }
        }
        return new Result<>()
                .setCode(ResultCode.SUCCESS.getValue())
                .setDate(isContinue);
    }

    @Override
    public Result<Boolean> consumerHaLuo(Long id) {
        Boolean isContinue = Boolean.FALSE;
        MarketingTransferInfo transferInfo = marketingTransferInfoMapper.selectByPrimaryKey(id);
        String apiCode = transferInfo.getApiCode();
        if (transferInfo == null) {
            return new Result<>()
                    .setCode(ResultCode.SUCCESS.getValue())
                    .setDate(isContinue)
                    .setMessage("数据不存在");
        }
        String tcId = tableCreateService.getTcId(apiCode);
        if (tcId == null) {
            return new Result<>()
                    .setCode(ResultCode.FAIL.getValue())
                    .setDate(isContinue)
                    .setMessage(String.format("apiCode:%s 未维护cid信息", apiCode));
        }
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).
                andRequestIdEqualTo(transferInfo.getRequestId());
        example.settCid(tcId);
        List<MarketingTransferSyncUser> marketingTransferSyncUsers = marketingTransferSyncUserMapper.selectByExample(example);
        LocalFile localFile = new LocalFile();
        validData:
        for (MarketingTransferSyncUser marketingTransferSyncUser : marketingTransferSyncUsers) {
            JSONObject jb = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
            boolean a = "1".equals(marketingTransferSyncUser.getIfLogin())
                    && (jb != null && StringUtils.isNotBlank(jb.getString("applyInformation")) && "0".equals(jb.getString("applyInformation")))
                    && !"1".equals(marketingTransferSyncUser.getIfApply());

            boolean b = "1".equals(marketingTransferSyncUser.getIfLogin())
                    && (jb != null && StringUtils.isNotBlank(jb.getString("applyInformation")) && "1".equals(jb.getString("applyInformation")))
                    && !"1".equals(marketingTransferSyncUser.getIfApply());

//            boolean c = "1".equals(marketingTransferSyncUser.getIfLogin())
//                    && (jb != null && StringUtils.isNotBlank(jb.getString("applyInformation")) && "1".equals(jb.getString("applyInformation")))
//                    && "1".equals(marketingTransferSyncUser.getIfApply())
//                    && "0".equals(marketingTransferSyncUser.getApplyResult());

            Double unlentAmount = Double.valueOf(StringUtils.isNotBlank(marketingTransferSyncUser.getUnlentAmount()) ? marketingTransferSyncUser.getUnlentAmount() : "0");
            boolean d = unlentAmount > 0;
//            if (!a && !b && !c && !d) {
//                continue;
//            }
            if (!a && !b && !d) {
                continue;
            }
            MarketingSyncUser marketingSyncUser = marketingSyncUserMapper.selectSynsUserByCustNumLast(apiCode, marketingTransferSyncUser.getCustNum());
            if (marketingSyncUser == null) {
                continue;
            }
            String cusBatch = marketingSyncUser.getCusBatch();
            TaskTimeExample timeExample = new TaskTimeExample();
            timeExample.createCriteria().andApiCodeEqualTo(apiCode).andTaskIdEqualTo(cusBatch);
            List<TaskTime> taskTimes = taskTimeMapper.selectByExample(timeExample);
            if (taskTimes.size() <= 0) {
                continue;
            }
            TaskTime taskTime = taskTimes.get(0);
            LocalDate startDate = LocalDate.parse(taskTime.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate now = LocalDate.now();
            long days = startDate.until(now, ChronoUnit.DAYS);
            if (days > 29) {
                continue;
            }
            LocalDate dxStartDate = now.minusDays(6);
            PhoneSaleExtendHaluoExample phoneSaleExtendHaluoExample = new PhoneSaleExtendHaluoExample();
            phoneSaleExtendHaluoExample.createCriteria()
                    .andCustNumEqualTo(marketingTransferSyncUser.getCustNum())
                    .andTaskIdEqualTo(cusBatch)
                    .andAppletDateGreaterThanOrEqualTo(dxStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .andAppletDateLessThanOrEqualTo(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            List<PhoneSaleExtendHaluo> phoneSaleExtendHaluos = phoneSaleExtendHaluoMapper.selectByExample(phoneSaleExtendHaluoExample);
            if (phoneSaleExtendHaluos.size() > 0) {
                long dLen = phoneSaleExtendHaluos.stream().filter(t -> "d".equals(t.getStatus())).count();
                long abcLen = phoneSaleExtendHaluos.size() - dLen;
                if (dLen > 0) {
                    continue;
                }
//                if (dLen == 0 && abcLen > 0 && (a || b || c) && !d) {
//                    continue;
//                }
                if (dLen == 0 && abcLen > 0 && (a || b) && !d) {
                    continue;
                }
            }

            String status = "";
            if (d) {
                status = "d";
            } else if (b) {
                status = "b";
            }
//            else if (c) {
//                status = "c";
//            }
            else if (a) {
                status = "a";
            }
            Boolean lock = Boolean.FALSE;
            while (!lock) {
                Result<Boolean> booleanResult = addHaluoLock(apiCode, cusBatch, marketingTransferSyncUser.getCustNum(), status);
                //不需要等待
                if (!ResultCode.SUCCESS.getValue().equals(booleanResult.getCode())) {
                    continue validData;
                }
                lock = booleanResult.getData();
                //如满足需要等待再次获取
                if (!lock) {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (localFile.getId() == null || localFile.getId() <= 0) {
                localFile.setApiCode(apiCode);
                localFile.setCreateTime(new Date());
                localFile.setFileType(SftpFileTypeEnum.HLBYTRANSFORM.getValue());
                localFile.setFileName("哈罗—".concat(marketingTransferSyncUser.getRequestId()));
                localFileMapper.insertSelective(localFile);

                HaluoCallRelation haluoCallRelation = new HaluoCallRelation();
                haluoCallRelation.setTransferId(transferInfo.getId());
                haluoCallRelation.setDxId(localFile.getId());
                haluoCallRelation.setBlackId(localFile.getId());
                haluoCallRelation.setRequestId(transferInfo.getRequestId());
                haluoCallRelation.setCreateTime(new Date());
                haluoCallRelationMapper.insertSelective(haluoCallRelation);

            }
            String cell = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
            String s = AESUtil.aesEncrypty(cell, aesKey);
            String name = StringUtils.isNotBlank(marketingSyncUser.getName()) ?
                    BrCipherMaker.getInstance().decode(marketingSyncUser.getName())
                    : "";
            PhoneSale sale = new PhoneSale();
            sale.setLocalId(localFile.getId().toString());
            sale.setApiCode(apiCode);
            sale.setOrgname("hellobike");
            sale.setName(name);
            sale.setPhone(s);
            sale.setPhoneAes(marketingSyncUser.getCell());
            sale.setUid(marketingTransferSyncUser.getCustNum());
            sale.setUserType("d".equals(status) ? "3" : "2");
            sale.setLoginTime(haluoBydxTimeFormat(marketingTransferSyncUser.getLoginTime()));
            sale.setSource("96");
            sale.setAuditTime(haluoBydxTimeFormat(marketingTransferSyncUser.getAuditTime()));
            sale.setAuditAmount(marketingTransferSyncUser.getAuditAmount());
            sale.setIfApply(marketingTransferSyncUser.getIfApply());
            sale.setApplyDt(haluoBydxTimeFormat(marketingTransferSyncUser.getApplyDt()));
            sale.setUnlentAmount(marketingTransferSyncUser.getUnlentAmount());
            sale.setCreateTime(new Date());
//            sale.setApplyResult(marketingTransferSyncUser.getApplyResult());
            if (StringUtils.isNotBlank(marketingTransferSyncUser.getReserveField1())) {
                JSONObject jsonObject = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
                if (jsonObject != null) {
                    String applyInformation = jsonObject.getString("applyInformation");
                    if (StringUtils.isNotBlank(applyInformation)) {
                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("applyInformation", applyInformation);
                        sale.setExtend(JSON.toJSONString(jsonObject1));
                    }
                }
            }
            phoneSaleMapper.insertSelective(sale);

            PhoneSaleExtendHaluo haluo = new PhoneSaleExtendHaluo();
            haluo.setpId(sale.getId());
            haluo.setLocalId(localFile.getId());
            haluo.setTaskId(cusBatch);
            haluo.setCustNum(marketingTransferSyncUser.getCustNum());
            haluo.setAppletDate(marketingTransferSyncUser.getRequestData());
            haluo.setAppletTime(marketingTransferSyncUser.getRequestTime());
            haluo.setCreateTime(new Date());
            haluo.setStatus(status);
            phoneSaleExtendHaluoMapper.insertSelective(haluo);

            String expiredate = LocalDate.parse(marketingTransferSyncUser.getRequestData(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(6)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:00";
            PhoneBlack phoneBlack = new PhoneBlack();
            phoneBlack.setLocalId(localFile.getId());
            phoneBlack.setName(name);
            phoneBlack.setPhone(marketingSyncUser.getCell());
            phoneBlack.setExpiredate(expiredate);
            phoneBlack.setCreateTime(new Date());
            phoneBlack.setUpdateTime(new Date());
            phoneBlackMapper.insertSelective(phoneBlack);
            removeHaluoLock(apiCode, cusBatch, marketingTransferSyncUser.getCustNum(), status);
        }
        if (localFile.getId() != null && localFile.getId() > 0) {
            producter.send(MQConstants.ROUTING_KEY_MARKETING_PUSH_DASS_SCORE, localFile.getId().toString());
            producter.send(MQConstants.ROUTING_KEY_MARKETING_PUSH_BLACK, localFile.getId().toString());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> saveCondition(ConditionSaveDTO dto) {

        SearchConditionDTO searchConditionDTO = new SearchConditionDTO();
        searchConditionDTO.setApiCode(dto.getApiCode());
        searchConditionDTO.setName(dto.getName());
        searchConditionDTO.setStatus(1);
        Integer scoreCountBySearch = scoreSearchConditionMapper.getScoreCountBySearch(searchConditionDTO);
        if (scoreCountBySearch > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("规则模板名称重复");
        }

        Date date = new Date();
        ScoreSearchCondition searchCondition = new ScoreSearchCondition();
        searchCondition.setName(dto.getName());
        searchCondition.setConditionNumber(buildConditionNumber(dto.getApiCode()));
        searchCondition.setConditionType(1);
        searchCondition.setContent(dto.getmRuleCondition());
        searchCondition.setContentShow(dto.getmRuleConditionShow());
        searchCondition.setCreateTime(date);
        searchCondition.setUpdateTime(date);
        scoreSearchConditionMapper.insertSelective(searchCondition);
        entityOptService.writeOptLog(searchCondition.getId(), searchCondition, null);

        ScoreSearchConditionMapping scoreSearchConditionMapping = new ScoreSearchConditionMapping();
        scoreSearchConditionMapping.setApiCode(dto.getApiCode());
        scoreSearchConditionMapping.setConditionId(searchCondition.getId());
        scoreSearchConditionMapping.setCreateTime(date);
        scoreSearchConditionMapping.setUpdateTime(date);
        scoreSearchConditionMappingMapper.insertSelective(scoreSearchConditionMapping);
        entityOptService.writeOptLog(scoreSearchConditionMapping.getId(), scoreSearchConditionMapping, null);

        return new Result<Integer>().setCode(ResultCode.SUCCESS.getValue()).setDate(searchCondition.getId());
    }

    String buildConditionNumber(String apiCode) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = RedisKeyConstant.conditionNumber.concat(":").concat(yyyyMMdd);
        Long incr = redisChgService.incr(key);
        redisChgService.expire(key, getKeyExpiration());
        String s = incr.toString();
        int length = s.length();
        for (int i = 3; i > length; i--) {
            s = "0" + s;
        }
        return yyyyMMdd.concat("_").concat(apiCode).concat("_").concat(s);
    }

    @Override
    public Result<List<ConditionOfScoreVO>> getConditionByRule(String apiCode, String name) {
        ScoreSearchConditionMappingExample mappingExample = new ScoreSearchConditionMappingExample();
        mappingExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID).andApiCodeEqualTo(apiCode);
        List<ScoreSearchConditionMapping> scoreSearchConditionMappings = scoreSearchConditionMappingMapper.selectByExample(mappingExample);
        if (scoreSearchConditionMappings.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("未有符合条件的数据");
        }
        List<Long> conditionIds = scoreSearchConditionMappings.stream().map(t -> t.getConditionId()).collect(Collectors.toList());
        if (conditionIds.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合条件的数据");
        }
        List<ConditionOfScoreVO> scoreByNameNumberList = scoreSearchConditionMapper.getScoreByNameNumberList(conditionIds, name);
        if (scoreByNameNumberList.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合条件的数据");
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(scoreByNameNumberList);
    }

    @Override
    public Result<PageResultReturn<ScoreConditionDetailVO>> getConditionPageData(SearchConditionDTO dto) {
        if (dto.getSize() == null) {
            dto.setSize(10);
        }
        PageHelper.startPage(dto.getCurrent(), dto.getSize());
        List<ScoreConditionDetailVO> scoreListBySearch = scoreSearchConditionMapper.getScoreListBySearch(dto);
        PageResultReturn pageResultReturn = PageResultReturn.setPageResult(scoreListBySearch, dto.getCurrent(), dto.getSize());
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(pageResultReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result optCondition(OptConditionDTO dto) {

        ScoreSearchCondition searchCondition = scoreSearchConditionMapper.selectByPrimaryKey(dto.getId());
        if (!new Integer(1).equals(searchCondition.getIsDel())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该规则不存在");
        }
        ScoreSearchCondition updateEntity = new ScoreSearchCondition();
        updateEntity.setId(dto.getId());
        updateEntity.setStatus(dto.getStatus());
        scoreSearchConditionMapper.updateByPrimaryKeySelective(updateEntity);
        entityOptService.writeOptLog(dto.getId(), updateEntity, searchCondition);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private Result<Boolean> addHaluoLock(String apiCode, String taskId, String custNum, String status) {
        String key = RedisKeyConstant.haluoPushDx.concat(":")
                .concat(apiCode).concat(":")
                .concat(taskId).concat(":")
                .concat(custNum);
        Boolean setnx = redisChgService.setnx(key, status, 3);
        //已经被其他数据抢占锁了
        if (!setnx) {

            //如果当前数据不是d就不推
            if (!status.equals("d")) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }

            String s = redisChgService.get(key);

            //分布式锁的数据状态如果是d则都不推
            if (s.equals("d")) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }

            //如果当前数据状态是d 并且锁里的数据不是d 需要等待500ms然后再次获取锁
            if (status.equals("d")) {
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }

    private void removeHaluoLock(String apiCode, String taskId, String custNum, String status) {
        String key = RedisKeyConstant.haluoPushDx.concat(":")
                .concat(apiCode).concat(":")
                .concat(taskId).concat(":")
                .concat(custNum);
        String s = redisChgService.get(key);
        if (status.equals(s)) {
            redisChgService.del(key);
        }
    }

    private String haluoBydxTimeFormat(String time) {
        if (StringUtils.isBlank(time)) {
            return time;
        }

        if (Pattern.matches(msTimeRegex, time)) {
            return time.replace(":000", "");
        }

        return time;
    }

    public Result<String> pushBlack(List<MarketingTransferSyncUser> marketingTransferSyncUsers) {
        ArrayList<BlackDetailDTO> blackDetailDTOS = new ArrayList<>();
        String apiCode = "";
        String requestId = "";
        String idRang = marketingTransferSyncUsers.get(0).getId()
                + "-"
                + marketingTransferSyncUsers.get(marketingTransferSyncUsers.size() - 1).getId();
        for (MarketingTransferSyncUser marketingTransferSyncUser : marketingTransferSyncUsers) {
            if (StringUtils.isEmpty(apiCode)) {
                apiCode = marketingTransferSyncUser.getApiCode();
            }
            if (StringUtils.isEmpty(requestId)) {
                requestId = marketingTransferSyncUser.getRequestId();
            }
            BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
            JSONObject jsonObject = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
            String cell = jsonObject.getString("cell");
            String createTime = jsonObject.getString("createTime");
            LocalDateTime time = LocalDateTime.parse(createTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            blackDetailDTO.setPhone(BrCipherMaker.getInstance().decode(cell));
            LocalDateTime lastDay = null;
            if (marketingTransferSyncUser.getUserType().equals("促首登")) {
                lastDay = time.with(TemporalAdjusters.lastDayOfMonth());
            } else if (marketingTransferSyncUser.getUserType().equals("促申完")) {
                lastDay = time.plusDays(14);
            } else if (marketingTransferSyncUser.getUserType().equals("重申")) {
                lastDay = time.with(TemporalAdjusters.lastDayOfMonth());
            } else if (marketingTransferSyncUser.getUserType().equals("首借")) {
                lastDay = time.plusDays(29);
            } else {
                lastDay = time.with(TemporalAdjusters.lastDayOfMonth());
            }
            blackDetailDTO.setEffectiveDate(lastDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            blackDetailDTOS.add(blackDetailDTO);
        }
        BlackPhoneDTO<BlackDetailDTO> jsondata = new BlackPhoneDTO<>();
        jsondata.setMethod("blackData");
        jsondata.setData(blackDetailDTOS);
        ReqBlackPhoneDTO dto = new ReqBlackPhoneDTO();
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(jsondata));
        ReqBlackPhoneParentDTO parentDTO = new ReqBlackPhoneParentDTO();
        parentDTO.setDto(dto);
        parentDTO.setExtendInfo(requestId.concat(":").concat(idRang).concat(":").concat(String.valueOf(blackDetailDTOS.size())));
        ReqBlackPhoneVO reqBlackPhoneVO = robotaiApiServiceClient.pushBlack(parentDTO);
        if ("00".equals(reqBlackPhoneVO.getCode()) && (reqBlackPhoneVO.getData() == null || reqBlackPhoneVO.getData().size() <= 0)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        if (("00".equals(reqBlackPhoneVO.getCode()) && reqBlackPhoneVO.getData() != null && reqBlackPhoneVO.getData().size() > 0)
                || "9999".equals(reqBlackPhoneVO.getCode())) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue())
                    .setDate("9999".equals(reqBlackPhoneVO.getCode()) ? "9999" : "部分成功");
        }
        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(reqBlackPhoneVO.getCode());
    }

    public Result<String> pushCommonBlack(PushBlackReqDTO pushBlackReqDTO) {
        List<PhoneBlack> users = pushBlackReqDTO.getUsers();
        ArrayList<BlackDetailDTO> blackDetailDTOS = new ArrayList<>();
        Long localId = users.get(0).getLocalId();
        String idRang = users.get(0).getId()
                + "-"
                + users.get(users.size() - 1).getId();
        for (PhoneBlack phoneBlack : users) {
            BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
            blackDetailDTO.setPhone(BrCipherMaker.getInstance().decode(phoneBlack.getPhone()));
            blackDetailDTO.setEffectiveDate(phoneBlack.getEffectivedate());
            blackDetailDTO.setExpireDate(phoneBlack.getExpiredate());
            blackDetailDTOS.add(blackDetailDTO);
        }
        BlackPhoneDTO<BlackDetailDTO> jsondata = new BlackPhoneDTO<>();
        jsondata.setMethod("blackData");
        jsondata.setData(blackDetailDTOS);
        ReqBlackPhoneDTO dto = new ReqBlackPhoneDTO();
        dto.setApiCode(pushBlackReqDTO.getApiCode());
        dto.setJsonData(JSON.toJSONString(jsondata));
        ReqBlackPhoneParentDTO parentDTO = new ReqBlackPhoneParentDTO();
        parentDTO.setDto(dto);
        parentDTO.setExtendInfo(localId.toString().concat(":").concat(idRang).concat(":").concat(String.valueOf(blackDetailDTOS.size())));
        ReqBlackPhoneVO reqBlackPhoneVO = robotaiApiServiceClient.pushBlack(parentDTO);
        if ("00".equals(reqBlackPhoneVO.getCode()) && (reqBlackPhoneVO.getData() == null || reqBlackPhoneVO.getData().size() <= 0)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        if (("00".equals(reqBlackPhoneVO.getCode()) && reqBlackPhoneVO.getData() != null && reqBlackPhoneVO.getData().size() > 0)
                || "9999".equals(reqBlackPhoneVO.getCode())) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue())
                    .setDate("9999".equals(reqBlackPhoneVO.getCode()) ? "9999" : "部分成功");
        }
        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(reqBlackPhoneVO.getCode());
    }

    @Override
    public Result<Boolean> HandleZhongYouData(Long id) {
        Long st1 = System.currentTimeMillis();
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        String fileName = localFile.getFileName();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5, 20);
        List<String> strategyIdList = zhongyouFileDataMapper.selectZhongYoustrategyIds(id);
        //根据策略ID分组查询
        strategyIdList.forEach(strategyId -> {
            Long minId = null;
            Boolean isContiue = Boolean.TRUE;
            while (isContiue) {
                if (marketingCommonConfig.getZhongYouCleanDataThreadNum() != null) {
                    pool.setCorePoolSize(marketingCommonConfig.getZhongYouCleanDataThreadNum());
                    pool.setMaximumPoolSize(marketingCommonConfig.getZhongYouCleanDataThreadNum());
                    log.warn("中邮清洗数据线程调整，taskId={},corePoolSize={},maxPoolSize={}", strategyId, pool.getCorePoolSize(), pool.getMaximumPoolSize());
                }
                List<ZhongyouFileData> zhongyouFileDataList = zhongyouFileDataMapper.selectZhongYouDataPage(id, minId, strategyId);
                if (zhongyouFileDataList.size() <= 0) {
                    isContiue = Boolean.FALSE;
                    continue;
                }
                minId = zhongyouFileDataList.get(zhongyouFileDataList.size() - 1).getId() + 1;
                pool.submit(() -> {
                    try {
                        Result result = cleanData(zhongyouFileDataList, fileName);
                        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            log.warn(result.getMessage());
                        }
                    } catch (Exception ex) {
                        log.error("中邮数据清洗异常", ex);
                    }
                });
            }
        });
        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        log.warn("中邮清洗数据耗时：{} ms", System.currentTimeMillis() - st1);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(false).setMessage("成功");
    }

    private Result cleanData(List<ZhongyouFileData> zhongyouFileDataList, String fileName) {
        String apiCode = zhongyouFileDataList.get(0).getApiCode();
        MarketingPreUserDTO uploadDataDTO = new MarketingPreUserDTO();
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        //构造上传,转化参数
        buildParam(apiCode, zhongyouFileDataList, uploadDataDTO, transferDataDTO, fileName);
        //插入上传info表
        MarketingSyncInfo syncInfo = new MarketingSyncInfo();
        try {
            syncInfo.setApiCode(apiCode);
            syncInfo.setCusBatch(uploadDataDTO.getTaskId());
            syncInfo.setRequestBatch(uploadDataDTO.getRequestId());
            syncInfo.setCreateTime(new Date());
            syncInfo.setJsonData(JSON.toJSONString(uploadDataDTO));
            syncInfo.setActualNum(uploadDataDTO.getDataItems().size());
            marketingUserMapper.insertMarketingPreUserByText(syncInfo);
        } catch (DuplicateKeyException keyException) {
            log.error("中邮上传数据request_batch重复，requestBatch = {}", uploadDataDTO.getRequestId());
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("中邮上传数据request_batch重复");
        } catch (Exception ex) {
            log.error("中邮上传数据插入异常", ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("中邮上传数据插入异常");
        }
        //插入上传明细表
        insertMarketingPreUserSync(syncInfo.getId());

        //插入转化info表
        MarketingTransferInfo transferInfo = new MarketingTransferInfo();
        try {
            transferInfo.setApiCode(apiCode);
            transferInfo.setRequestId(transferDataDTO.getRequestId());
            transferInfo.setCreateTime(new Date());
            transferInfo.setJsonData(JSON.toJSONString(transferDataDTO));
            transferInfo.setActualNum(transferDataDTO.getDataItems().size());
            marketingTransferInfoMapper.insertSelective(transferInfo);
        } catch (DuplicateKeyException keyException) {
            log.error("中邮转化数据request_id重复，requestId = {}", transferDataDTO.getRequestId());
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("中邮转化数据request_id重复");
        } catch (Exception ex) {
            log.error("中邮转化数据插入异常", ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("中邮转化数据插入异常");
        }
        //插入转化明细表
        consumerTransferData(transferInfo.getId());
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");

    }

    private void buildParam(String apiCode, List<ZhongyouFileData> zhongyouFileDataList, MarketingPreUserDTO uploadDataDTO, TransferDataDTO transferDataDTO, String fileName) {
        List<MarketingPreUserDetailDTO> dataItems = new ArrayList<>();
        List<TransferDataItemDTO> transferDataItemDTOS = new ArrayList<>();
        zhongyouFileDataList.forEach(zhongyouFileData -> {
            List<String> list = new ArrayList<>(Arrays.asList(zhongyouFileData.getFileData().split("\\|\\|", -1)));
            MarketingPreUserDetailDTO detailDTO = new MarketingPreUserDetailDTO();
            TransferDataItemDTO transferDataItemDTO = new TransferDataItemDTO();
            JSONObject uploadJsonObject = new JSONObject();
            JSONObject transferJsonObject = new JSONObject();
            //同一批taskId一样
            uploadDataDTO.setTaskId(list.get(0));
            transferJsonObject.put("taskId", list.get(0));
            detailDTO.setCell(list.get(4));
            transferJsonObject.put("cell", list.get(4));
            detailDTO.setCustNum(list.get(3));
            transferDataItemDTO.setCustNum(list.get(3));
            uploadJsonObject.put("firstName", list.get(5));
            transferJsonObject.put("firstName", list.get(5));
            uploadJsonObject.put("userType", "00");
            transferDataItemDTO.setUserType("00");
            String gender = list.get(6);
            if ("女".equals(gender)) {
                uploadJsonObject.put("gender", 0);
                transferJsonObject.put("gender", 0);
            } else if ("男".equals(gender)) {
                uploadJsonObject.put("gender", 1);
                transferJsonObject.put("gender", 1);
            } else {
                uploadJsonObject.put("gender", "");
                transferJsonObject.put("gender", "");
            }
            uploadJsonObject.put("customName", list.get(1));
            transferDataItemDTO.setCustomName(list.get(1));
            uploadJsonObject.put("registerTime", list.get(7));
            transferDataItemDTO.setRegisterTime(list.get(7));
            uploadJsonObject.put("ifLogin", list.get(19));
            transferDataItemDTO.setIfLogin(list.get(19));
            if (StringUtils.isEmpty(list.get(8)) || StringUtils.isEmpty(list.get(20))) {
                uploadJsonObject.put("loginTime", "");
            } else {
                uploadJsonObject.put("loginTime", StringUtils.isNotEmpty(list.get(8)) ? list.get(8) : list.get(20));
                transferDataItemDTO.setLoginTime(StringUtils.isNotEmpty(list.get(8)) ? list.get(8) : list.get(20));
            }
            uploadJsonObject.put("ifApply", list.get(21));
            transferDataItemDTO.setIfApply(list.get(21));
            uploadJsonObject.put("applyDt", list.get(22));
            transferDataItemDTO.setApplyDt(list.get(22));
            uploadJsonObject.put("applyResult", list.get(23));
            transferDataItemDTO.setApplyResult(list.get(23));
            if (StringUtils.isEmpty(list.get(10)) || StringUtils.isEmpty(list.get(24))) {
                uploadJsonObject.put("auditTime", "");
            } else {
                uploadJsonObject.put("auditTime", StringUtils.isNotEmpty(list.get(10)) ? list.get(10) : list.get(24));
                transferDataItemDTO.setAuditTime(StringUtils.isNotEmpty(list.get(10)) ? list.get(10) : list.get(24));
            }
            uploadJsonObject.put("auditAmount", list.get(11));
            transferDataItemDTO.setAuditAmount(list.get(11));
            uploadJsonObject.put("ifLent", list.get(26));
            transferDataItemDTO.setIfLent(list.get(26));
            uploadJsonObject.put("lentTime", list.get(29));
            transferDataItemDTO.setLentTime(list.get(29));
            uploadJsonObject.put("lentAmount", list.get(30));
            transferDataItemDTO.setLentAmount(list.get(30));
            uploadJsonObject.put("unlentAmount", list.get(14));
            transferDataItemDTO.setUnlentAmount(list.get(14));
            uploadJsonObject.put("pushTime", list.get(2));
            transferJsonObject.put("pushTime", list.get(2));
            uploadJsonObject.put("loginChannel", list.get(9));
            transferJsonObject.put("loginChannel", list.get(9));
            uploadJsonObject.put("auditRate", list.get(12));
            transferJsonObject.put("auditRate", list.get(12));
            uploadJsonObject.put("couponType", list.get(13));
            transferJsonObject.put("couponType", list.get(13));
            uploadJsonObject.put("validityAmt", list.get(15));
            transferJsonObject.put("validityAmt", list.get(15));
            uploadJsonObject.put("rateType", list.get(16));
            transferJsonObject.put("rateType", list.get(16));
            uploadJsonObject.put("lentRate", list.get(17));
            transferJsonObject.put("lentRate", list.get(17));
            uploadJsonObject.put("validityRate", list.get(18));
            transferJsonObject.put("validityRate", list.get(18));
            uploadJsonObject.put("applyLentTime", list.get(25));
            transferJsonObject.put("applyLentTime", list.get(25));
            uploadJsonObject.put("extend01", list.get(32));
            transferJsonObject.put("extend01", list.get(32));
            uploadJsonObject.put("extend02", list.get(33));
            transferJsonObject.put("extend02", list.get(33));
            uploadJsonObject.put("lentAmountFirst", list.get(28));
            transferJsonObject.put("lentAmountFirst", list.get(28));
            uploadJsonObject.put("lentTimeFirst", list.get(27));
            transferJsonObject.put("lentTimeFirst", list.get(27));
            uploadJsonObject.put("cpsRate", list.get(31));
            transferJsonObject.put("cpsRate", list.get(31));
            uploadJsonObject.put("fileName", fileName);
            transferJsonObject.put("fileName", fileName);

            detailDTO.setReserveField1(uploadJsonObject.toJSONString());
            dataItems.add(detailDTO);

            transferDataItemDTO.setReserveField1(transferJsonObject.toJSONString());
            transferDataItemDTOS.add(transferDataItemDTO);
        });
        uploadDataDTO.setRequestId(apiCode + System.currentTimeMillis() + UUID.randomUUID());
        uploadDataDTO.setDataItems(dataItems);
        transferDataDTO.setDataItems(transferDataItemDTOS);
        transferDataDTO.setRequestId(apiCode + System.currentTimeMillis() + UUID.randomUUID());

    }

    /**
     * 模拟数据库或者redis异常
     *
     * @param mockType 1-数据库异常；2-redis异常
     * @param apiCode
     */
    @Override
    public void mockDbOrRedisError(Integer mockType, String apiCode) {
        HashMap<String, Boolean> mockError = marketingCommonConfig.getMockError();
        if (mockError == null) {
            return;
        }
        if (new Integer(1).equals(mockType)) {
            if (mockError.get(apiCode) != null && mockError.get(apiCode)) {
                throw new KnowException(apiCode + ":DB异常");
            } else if (mockError.get(apiCode) != null && !mockError.get(apiCode)) {
                return;
            }
            if (mockError.get("db") != null && mockError.get("db")) {
                throw new KnowException("DB全局异常");
            }
        }
        if (new Integer(2).equals(mockType)) {
            if (mockError.get(apiCode) != null && mockError.get(apiCode)) {
                throw new KnowException(apiCode + ":redis异常");
            } else if (mockError.get(apiCode) != null && !mockError.get(apiCode)) {
                return;
            }
            if (mockError.get("redis") != null && mockError.get("redis")) {
                throw new KnowException("redis异常");
            }
        }
    }

    /**
     * 众邦财富定制标签数据推送
     */

    @Override
    public Result<Boolean> cunsumerZhongBangLabelData(Long id) {
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在");
        }
        Long st1 = System.currentTimeMillis();
        localFile.setPushStartTime(new Date());
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5, 20);
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            if (marketingCommonConfig.getZhongBangCaifuLabelThreadNum() != null) {
                pool.setCorePoolSize(marketingCommonConfig.getZhongBangCaifuLabelThreadNum());
                pool.setMaximumPoolSize(marketingCommonConfig.getZhongBangCaifuLabelThreadNum());
                log.warn("众邦财富定制标签线程调整，corePoolSize={},maxPoolSize={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());
            }
            List<ZhongbangCaifuData> zhongbangCaifuDataList = zhongbangCaifuDataMapper.zhongBangLabelDataPage(id, minId);
            if (zhongbangCaifuDataList.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = zhongbangCaifuDataList.get(zhongbangCaifuDataList.size() - 1).getId() + 1;
            pool.submit(() -> {
                try {
                    List<List<ZhongbangCaifuData>> labelList = Lists.partition(zhongbangCaifuDataList, 1000);
                    //组装数据调接口
                    labelList.forEach(labels -> {
                        List<Long> ids = labels.stream().map(t -> t.getId()).collect(Collectors.toList());
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("TskId", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                + "_" + labels.get(0).getApiCode()
                                + "_" + RandomStringUtils.randomNumeric(5)
                                + System.currentTimeMillis());
                        jsonObject.put("PrimKey", labels.get(0).getId());
                        JSONArray cstIndoList = new JSONArray();
                        labels.forEach(label -> {
                            JSONObject cstInfo = new JSONObject();
                            cstInfo.put("CstNo", label.getCstNo());
                            cstInfo.put("TagGrd", label.getTagGrd());
                            cstInfo.put("Rmk", label.getRmk());
                            cstIndoList.add(cstInfo);
                        });
                        jsonObject.put("CstInfoArray", cstIndoList);
                        jsonObject.put("ids", ids);
                        Result result = methodRetryHandlerService.pushZbankLabelRatingRe(jsonObject, null);
                        //更新数据表状态
                        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            //更新成功
                            updateStatus(ids, 2);
                        } else {
                            //更新失败
                            updateStatus(ids, 3);
                        }
                    });
                } catch (Exception ex) {
                    log.error("众邦财富定制标签推送异常", ex);
                }
            });
        }
        ;
        pool.shutdown();
        try {
            while (!pool.awaitTermination(5L, TimeUnit.SECONDS)) {
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        //更新文件表推送数据量
        ZhongbangCaifuDataExample zhongbangCaifuDataExample = new ZhongbangCaifuDataExample();
        zhongbangCaifuDataExample.createCriteria().andLocalIdEqualTo(localFile.getId())
                .andPushStatusEqualTo(2)
                .andStatusEqualTo(1);
        Long num = zhongbangCaifuDataMapper.countByExample(zhongbangCaifuDataExample);
        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(num.intValue());
        //更新状态推送成功
        localFile.setPushStatus("2");
        localFileMapper.updateByPrimaryKeySelective(localFile);
        //统计告警
        if (!localFile.getPushNumber().equals(localFile.getActualNumber())) {
            sendAlarm(localFile.getActualNumber() - localFile.getPushNumber(), "众邦财富定制标签推送失败数量统计");
        }
        log.warn("众邦财富定制标签推送结束，耗时：{} ms", System.currentTimeMillis() - st1);

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(false).setMessage("成功");
    }

    private void updateStatus(List<Long> ids, int status) {
        if (ids.size() > 0) {
            ZhongbangCaifuDataExample updateExample = new ZhongbangCaifuDataExample();
            updateExample.createCriteria().andIdIn(ids);
            ZhongbangCaifuData record = new ZhongbangCaifuData();
            record.setPushStatus(status);
            zhongbangCaifuDataMapper.updateByExampleSelective(record, updateExample);
        }
    }


    public void updateZhongBangRetryStatus(List<Long> ids) {
        //更新数据表状态
        ZhongbangCaifuDataExample updateExample = new ZhongbangCaifuDataExample();
        updateExample.createCriteria().andIdIn(ids);
        ZhongbangCaifuData record = new ZhongbangCaifuData();
        record.setPushStatus(2);
        zhongbangCaifuDataMapper.updateByExampleSelective(record, updateExample);
        Long localId = zhongbangCaifuDataMapper.selectByPrimaryKey(ids.get(0)).getLocalId();
        //更新文件表推送数据量
        LocalFile localFile = localFileMapper.selectByPrimaryKey(localId);
        ZhongbangCaifuDataExample zhongbangCaifuDataExample = new ZhongbangCaifuDataExample();
        zhongbangCaifuDataExample.createCriteria().andLocalIdEqualTo(localId)
                .andPushStatusEqualTo(2)
                .andStatusEqualTo(1);
        Long num = zhongbangCaifuDataMapper.countByExample(zhongbangCaifuDataExample);
        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(num.intValue());
        localFileMapper.updateByPrimaryKeySelective(localFile);

    }

    private void sendAlarm(Integer failNum, String title) {
        if (failNum > 0) {
            try {
                alarmClient.sendAlarm("推送失败条数=" + failNum, title, AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

}
