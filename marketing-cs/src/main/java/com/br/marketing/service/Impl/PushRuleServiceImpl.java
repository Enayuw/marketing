package com.br.marketing.service.Impl;

import java.util.Date;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.*;
import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarProducer;
import com.br.cloud.counter.BrCounter;
import com.br.common.encryption.Md5Utils;
import com.br.common.encryption.Sha256Util;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.aspect.MqIdempotent;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.client.intelligentcustomerservice.output.PolicyResultByTaskIdsDTO;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.*;
import com.br.marketing.client.robotaiapi.output.ReqBlackPhoneVO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.common.bean.ScoreLable;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.constants.PulsarSubscription;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.common.constants.cache.CaffeineCacheKeyConstant;
import com.br.marketing.common.constants.common.LastEnum;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.constants.rocketmq.*;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SwitchMessageQueueEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.enums.rocketmq.AiPreUserReceiveEnum;
import com.br.marketing.common.enums.rocketmq.AiUniversalReceiveEnum;
import com.br.marketing.common.exception.CommonException;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.commonentity.StatusConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.context.MqIdempotentContext;
import com.br.marketing.context.RuntimeDataContext;
import com.br.marketing.dto.*;
import com.br.marketing.dto.customer.PushCustomerRequestDTO;
import com.br.marketing.dto.dataclean.mq.CommonMqDataJsonParse;
import com.br.marketing.dto.dataclean.mq.MqDataJsonParse;
import com.br.marketing.dto.msg.mq.ApiDataInfoDTO;
import com.br.marketing.dto.msg.mq.UserTypeCollectionDTO;
import com.br.marketing.dto.rulecenter.XcCycleDeleteDTO;
import com.br.marketing.dto.rulecenter.XcCycleDeleteNumDTO;
import com.br.marketing.dto.rulecenter.XcDeleteMagnitudeDistDTO;
import com.br.marketing.dto.rulecenter.XieChengCollidingFilterDTO;
import com.br.marketing.entity.*;
import com.br.marketing.entity.common.TimeRange;
import com.br.marketing.entity.common.TimeRangePlus;
import com.br.marketing.enums.*;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.enums.clean.DataSourceTypeEnum;
import com.br.marketing.es.bean.ESQueryRequest;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.mapper.*;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import com.br.marketing.origin.*;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.rule.common.CommonRuleLabelEnum;
import com.br.marketing.service.*;
import com.br.marketing.service.Impl.ai.QueueBalancer;
import com.br.marketing.service.Impl.transferfieldprocess.TransferFiledProcessImpl;
import com.br.marketing.service.clean.common.DataCleanService;
import com.br.marketing.service.customertagsprocess.CustomerTagsProcessServiceImpl;
import com.br.marketing.service.customertagsprocess.IUploadCheckService;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.service.datagroup.rulecenter.RuleCenterLabelService;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.br.marketing.service.rulecenter.IEsActionService;
import com.br.marketing.service.rulecenter.IRuleCenterFilterTemplateService;
import com.br.marketing.service.rulecenter.RuleCenterBySourceTypeFactory;
import com.br.marketing.service.rulecenter.impl.push.UploadRePushPolicyStrategy;
import com.br.marketing.service.strategy.pushpreview.IPushPreviewStrategy;
import com.br.marketing.service.strategy.pushpreview.PushPreviewStrategyEnum;
import com.br.marketing.service.strategy.pushpreview.PushPreviewStrategyFactory;
import com.br.marketing.service.strategy.pushcustomer.IPushCustomerStrategy;
import com.br.marketing.service.strategy.pushcustomer.PushCustomerStrategyFactory;
import com.br.marketing.service.tag.calculate.TagHandleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.TimeUtils;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.utils.PulsarConsumerSkipUtil;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.br.marketing.util.EsNewIndexRuleUtils;
import com.br.marketing.util.GeneScriptUtil;
import com.br.marketing.util.xiecheng.XieChengEsJsonHandler;
import com.br.marketing.vo.*;
import com.br.marketing.vo.xiecheng.PushViewVO;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import com.github.pagehelper.PageHelper;
import com.marketingkit.tracking.service.TrackingService;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.marketingkit.tracking.util.TrackingContext;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.marketingkit.tracking.model.indicator.DataFlowDirection;

@Service
public class PushRuleServiceImpl implements PushRuleService {

    private static final Logger log = LoggerFactory.getLogger(PushRuleServiceImpl.class);
    public static final String AI_TO_POLICY = CommonRuleLabelEnum.AI_TO_POLICY.getCode();

    private static HashMap<String, String> errorCodeHm;

    static {
        errorCodeHm = new HashMap();
        errorCodeHm.put("1001", "无客户编号");
        errorCodeHm.put("1002", "无grouptype或userType或cell");
        errorCodeHm.put("1003", "重复客户编号");
        errorCodeHm.put("1004", "重复电话");
        errorCodeHm.put("1005", "入库异常");
        errorCodeHm.put("1006", "参数过长");
        errorCodeHm.put("1007", "清洗异常");
        errorCodeHm.put("1008", "存在4个字节字符");

    }

    @Resource
    CaffeineCache caffeineCache;

    @Resource
    private TrackingService trackingService;

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
    private PulsarConsumerSkipUtil pulsarConsumerSkipUtil;


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

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private AlarmApiClient alarmClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;

    @Resource
    private XiechengCollidingDataPackageRuleMapper xiechengCollidingDataPackageRuleMapper;

    @Resource
    private XiechengCollidingDataProcessTaskMapper xiechengCollidingDataProcessTaskMapper;

    @Resource
    private XieChengCollidingDataPackageMapper xieChengCollidingDataPackageMapper;

    @Resource
    private XieChengRuleScoreRecordMapper scoreRecordMapper;

    @Resource
    RuleCenterBySourceTypeFactory ruleCenterBySourceTypeFactory;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    @Resource
    PushDecisionsMapper pushDecisionsMapper;
    @Resource
    ErrorMarkMapper errorMarkMapper;

    @Resource
    TagDataDetailMapper tagDataDetailMapper;
    @Resource
    private ToPolicyByRuleService toPolicyByRuleService;

    @Resource
    private DataCleanService dataCleanService;

    @Resource
    private RuleCleaningService ruleCleaningService;

    @Resource
    private DataLoadingHandlerService dataLoadingHandlerService;

    @Resource
    private CustomerRuleMapper customerRuleMapper;

    @Autowired
    WuBaServiceClient wuBaServiceClient;

    @Autowired
    @Qualifier("clusterEnvironment")
    private String clusterEnvironment;

    @Autowired
    private QueueBalancer queueBalancer;

    @Resource
    private SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;

    @Resource
    private UploadRePushPolicyStrategy uploadRePushPolicyStrategy;

    @Resource
    private RuleCenterLabelService ruleCenterLabelService;

    @Resource
    private MarketingSyncReportMapper syncReportMapper;

    @Autowired
    private TagDataRuleCalculateMapper tagDataRuleCalculateMapper;

    @Resource
    private PushPreviewStrategyFactory pushPreviewStrategyFactory;

    @Resource
    private PushCustomerStrategyFactory pushCustomerStrategyFactory;

    @Resource
    private VariableAllocationServiceImpl variableAllocationService;

    @Resource
    XieChengCollidingDataLoopCycleMapper xieChengCollidingDataLoopCycleMapper;

    private static final String TITLE = "【通用跑分文件推决策】";


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
    public Result<String> getUserType(String apiCode) {
        List<String> userTypeList = marketingTaskUserTypeMapper.queryUserTypeByApiCodetikv_(apiCode);
        String userType = userTypeList.stream().collect(Collectors.joining(","));
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(userType).setMessage("查询成功");
    }

    @Override
    public PageResultReturn getBatchInfos(CustomerBatchNumDTO dto) {
        dto = getCustomerBatchNumDTO(dto);
        PageHelper.startPage(dto.getCurrent(), dto.getSize()).setOrderBy(" scoreBeginTime desc,fileId desc ");
        List<ScoreDetailVo> scoreDetailVos = marketingTaskMapper.queryBatchs(dto);
        scoreDetailVos.stream().forEach((ScoreDetailVo t) -> {
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

    @Resource
    XiechengCollidingTaskBatchMapper xiechengCollidingTaskBatchMapper;

    @Autowired
    TransferFiledProcessImpl transferFiledProcess;

    @Resource
    CustomerTagsProcessServiceImpl customerTagsProcessService;

    @Resource
    TagHandleService tagHandleService;

    @Resource
    IEsActionService iEsActionService;


    @Override
    public Result<CustomerInfoPushMain> getPushTask() {
        Date date = Date.from(LocalDate.now().minusDays(2L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        List<Integer> list = Lists.newArrayList();
        list.add(PushRuleStatusEnum.TO_BE_RUNNING.getValue());
        list.add(PushRuleStatusEnum.EXCEPTIONS_TO_REFILLED.getValue());
        CustomerInfoPushMainExample pushMainExample = new CustomerInfoPushMainExample();
        pushMainExample.setOrderByClause(" create_time,id limit 1");
        pushMainExample.createCriteria()
                .andMStatusIn(list)
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
            if (customerInfoPushMain.getmStatus().equals(PushRuleStatusEnum.TO_BE_RUNNING.getValue())) {
                CustomerInfoPushMain updateEntity = new CustomerInfoPushMain();
                updateEntity.setId(taskId);
                updateEntity.setmStatus(PushRuleStatusEnum.RUNNING.getValue());
                customerInfoPushMainMapper.updateByPrimaryKeySelective(updateEntity);
                removeCanPushTaskLock(taskId, lockValue);
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            } else if (customerInfoPushMain.getmStatus().equals(PushRuleStatusEnum.EXCEPTIONS_TO_REFILLED.getValue())) {
                CustomerInfoPushMain updateEntity = new CustomerInfoPushMain();
                updateEntity.setId(taskId);
                updateEntity.setmStatus(PushRuleStatusEnum.EXCEPTIONS_RUNNING.getValue());
                customerInfoPushMainMapper.updateByPrimaryKeySelective(updateEntity);
                removeCanPushTaskLock(taskId, lockValue);
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }
            removeCanPushTaskLock(taskId, lockValue);
            return new Result().setCode(ResultCode.FAIL.getValue());
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
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), ex.getMessage()), ex);
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> pushCustomer(PushCustomerDTO dto) {
        try {
            PushPreviewStrategyEnum strategyType;

            // 判断任务类型，选择对应策略
            if (Objects.equals(dto.getTaskType(), TaskTypeEnum.UPLOAD_TASKS.getValue())) {
                strategyType = PushPreviewStrategyEnum.UPLOAD_TASK;
            } else {
                // 跑分任务需要校验加密类型
                AssertResult.assertResult(checkThreekEnc(dto.getFileIdList()));

                if (isXieChengData(dto)) {
                    strategyType = PushPreviewStrategyEnum.XIE_CHENG_SCORE;
                } else if (Objects.nonNull(dto.getIsScoreMerge()) && dto.getIsScoreMerge()) {
                    strategyType = PushPreviewStrategyEnum.MERGE_SCORE;
                } else {
                    strategyType = PushPreviewStrategyEnum.COMMON_SCORE;
                }
            }

            IPushCustomerStrategy strategy = pushCustomerStrategyFactory.getStrategy(strategyType);
            return strategy.execute(dto);
        } catch (Exception e) {
            log.error("推送客户执行失败，apiCode: {}, taskType: {}", dto.getApiCode(), dto.getTaskType(), e);
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送客户执行失败: " + e.getMessage());
        }
    }

    @Override
    public Result<PushViewVO> queryFederation(PushCustomerDTO dto, PushViewVO pushViewVO) {
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(dto.getApiCode());
        queryBaseBean.setBatchNumbers(Joiner.on(",").join(dto.getBatchNumberList()));
        queryBaseBean.setFileIds(Joiner.on(",").join(dto.getFileIdList()));
        queryBaseBean.setJsonData(dto.getmRuleCondition());
        StraHisFileExample fileExampleForIndex = new StraHisFileExample();
        fileExampleForIndex.createCriteria().andIdIn(dto.getFileIdList());
        List<StraHisFile> straHisFilesForIndex = straHisFileMapper.selectByExample(fileExampleForIndex);
        queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(straHisFilesForIndex, marketingCommonConfig));
        if (dto.getmPlanNum() != null && dto.getmPlanNum() <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送数量不能小于等于0");
        }
        if (dto.getmPlanNum() != null && dto.getmPlanNum() > 0) {
            queryBaseBean.setAmountTop("0,".concat(dto.getmPlanNum().toString()));
        }

        int total = 0;
        String federatedQuerySql = "";
        try {
            String mTagCondition = dto.getmTagCondition();
            if (mTagCondition == null) {
                total = iEsActionService.getTotal(queryBaseBean);
            } else {
                // 解析标签规则
                JSONObject jsonObject = JSON.parseObject(mTagCondition);
                String tagCode = jsonObject.getString("tagCode");
                int type = jsonObject.getIntValue("type");
                if (!tagHandleService.tagIsEnabled(dto.getApiCode(), tagCode)) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.TAG_SERVICEERROR.getCode(),
                            "该apiCode：" + dto.getApiCode() + "，该tag：" + tagCode + "已失效"));
                    return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("该apiCode：" + dto.getApiCode() + ",该tag：" + tagCode + "已失效");
                }

                StraHisFileExample fileExample = new StraHisFileExample();
                fileExample.createCriteria().andIdIn(dto.getFileIdList());
                List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(fileExample);

                Optional<StraHisFile> first = straHisFiles.stream().sorted(Comparator.comparing(StraHisFile::getIndexNum).reversed()).findFirst();
                Integer parNum = 0;
                if (first.isPresent()) {
                    parNum = first.get().getIndexNum();
                }

                List<Future<Result<Integer>>> resList = new ArrayList<>();
                Integer pushRuleThreadNum = marketingCommonConfig.getToPolicyThreadNum().get("pushRuleThreadNum");
                Integer pushRuleQueueNum = marketingCommonConfig.getToPolicyThreadNum().get("pushRuleQueueNum");
                ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(pushRuleThreadNum, pushRuleThreadNum, pushRuleQueueNum);
                for (int i = 0; i < parNum; i++) {
                    int finalI = i;
                    QueryBaseBean queryBaseBean1 = new QueryBaseBean();
                    BeanUtils.copyProperties(queryBaseBean, queryBaseBean1);
                    resList.add(threadPool.submit(() -> federatedQueryTotal(dto, queryBaseBean1, finalI, tagCode, type)));
                }
                try {
                    for (Future<Result<Integer>> pushFuture : resList) {
                        Result<Integer> pushRes = pushFuture.get();
                        if (ResultCode.SUCCESS.getValue().equals(pushRes.getCode())) {
                            total += pushRes.getData();
                        } else {
                            return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setMessage("推送决策分片返回量级异常");
                        }
                    }
                } catch (Exception ex) {
                    log.error("推送决策 获取线程结果异常" + ex.getMessage(), ex);
                }
                // 关闭线程池
                threadPool.shutdown();
                try {
                    while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                        log.info("推送决策查询量级：线程池关闭");
                    }
                } catch (InterruptedException ex) {
                    threadPool.shutdownNow();
                    log.error("推送决策查询量级：日志保存线程池结束异常！", ex);
                    Thread.currentThread().interrupt();
                }

            }
        } catch (Exception e) {
            log.warn("规则中心推送预览 SQL: " + federatedQuerySql);
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("构建联邦查询sql有误,sql" + federatedQuerySql);
        }
        pushViewVO.setTotal(total);
        return new Result<PushViewVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushViewVO);
    }

    private Result<Integer> federatedQueryTotal(PushCustomerDTO dto, QueryBaseBean queryBaseBean, int part, String tagCode, int type) {
        queryBaseBean.setPart(String.valueOf(part));
        ESQueryRequest esQueryRequest = marketingHistoryEsService.builderDslConditionOfQueryBaseBean(queryBaseBean);
        String queryDsl = esQueryRequest.getQueryDsl();
        List<String> indexNames = esQueryRequest.getIndexName();

        if (CollectionUtils.isEmpty(indexNames)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("查询es索引为空，batchNumberList：" + dto.getBatchNumberList());
        }

        // 构建联邦查询 SQL
        String federatedQuerySql = buildFederatedQuerySql(indexNames, queryDsl, tagCode, type);
        if (StringUtils.isEmpty(federatedQuerySql)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("查询有误，请联系开发人员");
        }
        try {
            tagDataDetailMapper.refreshbI_("refresh catalog es");
        } catch (Exception e) {
            log.error("refresh catalog es异常");
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("refresh catalog es异常");
        }
        int total = tagDataDetailMapper.queryPreviewTotalbI_(federatedQuerySql);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(total);
    }

    /**
     * 构建联邦查询 SQL
     *
     * @param indexNames Elasticsearch 索引列表
     * @param queryDsl   Elasticsearch 查询条件
     * @param tagCode    tagCode
     * @param type       type
     * @return 联邦查询 SQL
     */
    private String buildFederatedQuerySql(List<String> indexNames, String queryDsl, String tagCode, int type) {

        // 1. 构建 UNION ALL 部分
        StringBuilder unionAllBuilder = new StringBuilder();
        for (int i = 0; i < indexNames.size(); i++) {
            String indexName = indexNames.get(i);
            unionAllBuilder.append("SELECT cell FROM es.default_db.").append(indexName);
            if (i < indexNames.size() - 1) {
                unionAllBuilder.append("\nUNION ALL\n");
            }
        }
        // 2. 根据 type 构建不同的 SQL
        String sql;
        switch (type) {
            case 0: // 交集
                sql = String.format(
                        "SELECT COUNT(1)\n" +
                                "FROM (\n" +
                                "    %s\n" +
                                ") esIndex\n" +
                                "JOIN t_tag_data_detail dorisCall ON esIndex.cell = dorisCall.cell AND dorisCall.calculate_date = curdate() AND dorisCall.tag_code = '%s'\n" +
                                "WHERE esquery(esIndex.cell, '%s');",
                        unionAllBuilder,
                        tagCode,
                        queryDsl.replace("'", "''")
                );
                break;
            case 1: // 剔除
                sql = String.format(
                        "SELECT COUNT(1)\n" +
                                "FROM (\n" +
                                "    %s\n" +
                                ") esIndex\n" +
                                "LEFT JOIN t_tag_data_detail dorisCall ON esIndex.cell = dorisCall.cell AND dorisCall.calculate_date = curdate() AND dorisCall.tag_code = '%s'\n" +
                                "WHERE dorisCall.cell IS NULL\n" +
                                "AND esquery(esIndex.cell, '%s');",
                        unionAllBuilder,
                        tagCode,
                        queryDsl.replace("'", "''")
                );
                break;
            default:
                sql = "";
        }
        return sql;
    }

    public int getXieChengDataNum(String mRuleCondition, List<String> batchNumberList, PushViewVO pushViewVO) {
        //blacklist_delete有值时，前端控制不会做量级预览
        int total = 0;
        String querySql = "";
        String opeLabel = "";
        List<String> querySqls = new ArrayList<>();
        JSONObject jsonObject = JSON.parseObject(mRuleCondition);
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        pushViewVO.setResult(collidingFilterDTO.getResult());
        if ("true".equals(collidingFilterDTO.getResult())) {
            if (StringUtils.isEmpty(collidingFilterDTO.getCleanTime())) {
                //推决策
                opeLabel = "推送决策";
                Boolean xcTruePushCustomerPushPreviewOptFlag = marketingCommonConfig.getXcTruePushCustomerPushPreviewOptFlag();
                if (xcTruePushCustomerPushPreviewOptFlag) {
                    cycleDataQueryOpt(jsonObject, batchNumberList, collidingFilterDTO, querySqls);
                } else {
                    querySql = cycleDataQuery(jsonObject, batchNumberList, collidingFilterDTO);
                }
            } else {
                //true包剔除
                opeLabel = "true包剔除";
                Boolean xcTrueDeletePushPreviewOptFlag = marketingCommonConfig.getXcTrueDeletePushPreviewOptFlag();
                if (xcTrueDeletePushPreviewOptFlag) {
                    cycleDataQueryForDeleteOpt(jsonObject, batchNumberList, collidingFilterDTO, querySqls);
                } else {
                    querySql = cycleDataQueryForDelete(jsonObject, batchNumberList, collidingFilterDTO);
                }
            }
        } else if ("false".equals(collidingFilterDTO.getResult())) {
            String info = collidingFilterDTO.getInfo();
            if (Objects.isNull(info)) {
                //false包补充
                opeLabel = "false包补充";
                Boolean xcFalsePackagePushPreviewOptFlag = marketingCommonConfig.getXcFalsePackagePushPreviewOptFlag();
                if (xcFalsePackagePushPreviewOptFlag) {
                    falseDataQueryOpt(jsonObject, batchNumberList, querySqls, collidingFilterDTO.getCleanTime(), true);
                } else {
                    querySql = falseDataQuery(jsonObject, batchNumberList, collidingFilterDTO.getCleanTime());
                }
            } else {
                if (info.equals("") || info.equalsIgnoreCase("NULL")) {
                    //false动态包剔除
                    opeLabel = "false动态包剔除";
                    Boolean xcFalsePackageDynaPushPreviewOptFlag = marketingCommonConfig.getXcFalsePackageDynaPushPreviewOptFlag();
                    if (xcFalsePackageDynaPushPreviewOptFlag) {
                        dynaPackageDeleteConditionOpt(
                                jsonObject, batchNumberList, marketingCommonConfig.getXcDynaFalsePackageIds(), querySqls, true);
                    } else {
                        querySql = dynaPackageDeleteCondition(jsonObject, batchNumberList,
                                marketingCommonConfig.getXcDynaFalsePackageIds(), true);
                    }
                }
            }
        }
        // 查询Doris
        try {
            if (CollectionUtils.isEmpty(querySqls)) {
                log.warn("规则中心携程={} 的试算量级sql={}", opeLabel, querySql);
                total = scoreRecordMapper.getXieChengDataNumdoris_(querySql);
            } else {
                log.warn("规则中心携程={} 的试算量级样例sql={}", opeLabel, querySqls.get(0));
                total = getTotalOpt(querySqls, marketingCommonConfig.getXcFalsePackageOptSoleNum());
            }
        } catch (Exception e) {
            if (CollectionUtils.isEmpty(querySqls)) {
                log.error("规则中心-携程撞库筛选查询Doris异常,sql={}", querySql, e);
            } else {
                log.error("规则中心-携程撞库筛选查询Doris异常,sqls={}", String.join("", querySqls), e);
            }
        }
        return total;
    }

    private void cycleDataQueryForDeleteOpt(JSONObject jsonObject, List<String> batchNumberList, XieChengCollidingFilterDTO collidingFilterDTO, List<String> querySqls) {
        for (String batchNumber : batchNumberList) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }
            List<String> batchNumbers = Arrays.asList(batchNumber);
            querySqls.add(cycleDataQueryForDelete(jsonObject, batchNumbers, collidingFilterDTO));
        }
    }


    /**
     * @param querySqls
     * @param xcFalsePackageOptSoleNum
     * @return int
     * @description 多线程获取量级
     * @author hedongshuo
     * @date 2025/1/7 16:01
     **/
    private int getTotalOpt(List<String> querySqls, Integer xcFalsePackageOptSoleNum) {
        AtomicInteger batchCount = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(xcFalsePackageOptSoleNum, xcFalsePackageOptSoleNum);
        for (String sql : querySqls) {
            futures.add(CompletableFuture.runAsync(() -> {
                batchCount.addAndGet(scoreRecordMapper.getXieChengDataNumdoris_(sql));
            }, threadPool));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程页面量级预览线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程页面量级预览线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
        return batchCount.get();
    }

    private void dynaPackageDeleteConditionOpt(
            JSONObject jsonObject, List<String> batchNumberList, List<String> xcDynaFalsePackageIds, List<String> querySqls, boolean isPreview) {
        if (CollectionUtils.isEmpty(xcDynaFalsePackageIds)) {
            xcDynaFalsePackageIds = Arrays.asList("120007");
        }
        String xcDynaFalsePackageIdString = xcDynaFalsePackageIds.stream()
                .collect(Collectors.joining(",", "(", ")"));
        String conditions = EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, "");
        for (String batchNumber : batchNumberList) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }
            sqlCollect(querySqls, isPreview, xcDynaFalsePackageIdString, conditions, batchNumber);
        }
    }

    private static void sqlCollect(List<String> querySqls, boolean isPreview, String xcDynaFalsePackageId, String conditions, String batchNumber) {
        String dynaDataSql;
        if (isPreview) {
            dynaDataSql = "select cell_sha256_code_list as cell,id from b_xiecheng_colliding_data_rob where is_delete = 0 and package_id in "
                    + xcDynaFalsePackageId;
        } else {
            dynaDataSql = "select rob.cell_sha256_code_list as cell, rob.id as id from b_xiecheng_colliding_data_rob rob " +
                    "inner join b_xiecheng_colliding_" + batchNumber + " batch on rob.cell_sha256_code_list = batch.cell " +
                    "and rob.is_delete = 0 and rob.package_id in " + xcDynaFalsePackageId;
        }
        StringBuilder scoreSql = new StringBuilder();
        scoreSql.append("select id, cell from b_xiecheng_colliding_")
                .append(batchNumber)
                .append(" where ")
                .append(conditions)
                .append(" and is_delete = 0");
        StringBuilder condition = new StringBuilder();
        condition.append("select count(0) from (")
                .append(dynaDataSql).append(") dyna left join (")
                .append(scoreSql)
                .append(") score on dyna.cell = score.cell ")
                .append(" where score.id is ");
        if (isPreview) {
            condition.append("not null");
        } else {
            condition.append("null");
        }
        querySqls.add(condition.toString());
    }

    /**
     * @param blacklistDelete
     * @return boolean
     * @description 黑名单剔除类型校验
     * @author hedongshuo
     * @date 2024/11/7 21:51
     **/
    private boolean checkBlacklistDelete(String blacklistDelete) {
        return StringUtils.isNotEmpty(blacklistDelete) && blacklistDelete.equalsIgnoreCase("true");
    }

    private String falseDataQuery(JSONObject jsonObject, List<String> batchNumberList, String cleanTime) {
        StringBuilder querySql = new StringBuilder();
        String condition = falseDataCondition(jsonObject, batchNumberList, cleanTime);
        querySql.append("select count(1) from (").append(condition).append(") a ;");
        return querySql.toString();
    }

    /**
     * @param jsonObject
     * @param batchNumberList
     * @param querySqls
     * @param cleanTime
     * @description false包预估量级sql优化
     * @author hedongshuo
     * @date 2025/1/2 15:32
     **/
    private void falseDataQueryOpt(JSONObject jsonObject, List<String> batchNumberList, List<String> querySqls, String cleanTime, Boolean isPreviewForOpt) {
        String conditions = EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, "");
        for (String batchNumber : batchNumberList) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }
//            int total = getQueryRuleScoreCountSql(batchNumber);
//            if (total > 40000000) {
//                for (int i = 0; i <= total / 30000000; i++) {
//                    sqlCollect(querySqls, isPreviewForOpt, conditions, batchNumber, i);
//                }
//            }
            sqlCollect(querySqls, isPreviewForOpt, conditions, batchNumber, cleanTime, null);
        }
    }

    private void sqlCollect(List<String> querySqls, Boolean isPreviewForOpt, String conditions, String batchNumber, String cleanTime, Integer pageIndex) {
        String querySql = falseQuerySqlOpt(conditions, batchNumber, cleanTime, pageIndex);
        if (isPreviewForOpt) {
            String queryCountSql = "select count(0) from (" + querySql + ") countSql;";
            querySqls.add(queryCountSql);
        } else {
            querySqls.add(querySql);
        }
    }

    private String falseQuerySqlOpt(String conditions, String batchNumber, String cleanTime, Integer pageIndex) {
        //将已生成还未清洗的补充包的cell剔除
        XiechengCollidingDataProcessTaskExample taskExample = new XiechengCollidingDataProcessTaskExample();
        taskExample.createCriteria()
                .andTaskTypeEqualTo(0)
                .andTaskStatusEqualTo(0)
                .andBatchNumberLike("%" + batchNumber + "%")
                .andIsDeleteEqualTo(0);
        List<XiechengCollidingDataProcessTask> taskList = xiechengCollidingDataProcessTaskMapper.selectByExample(taskExample);
        if (!CollectionUtils.isEmpty(taskList)) {
            StringBuilder conditionsBuilder = new StringBuilder(conditions);
            for (XiechengCollidingDataProcessTask processTask : taskList) {
                conditionsBuilder
                        .append("and !(")
                        .append(processTask.getTaskExecutionConditions())
                        .append(")");
            }
            conditions = conditionsBuilder.toString();
        }
        //组装左表sql
        StringBuilder queryRuleScoreDataSql = new StringBuilder();
        queryRuleScoreDataSql
                .append("select id, cell, is_delete from b_xiecheng_colliding_")
                .append(batchNumber)
                .append(" where ")
                .append(conditions);
        if (pageIndex != null) {
            queryRuleScoreDataSql.append(" order by id limit ").append(pageIndex * 30000000).append(", 30000000");
        }
        //清洗时间在撞库区间内去重，业务应规避此条件
        String packageIds = "";
        if (StringUtils.isNotEmpty(cleanTime)) {
            Integer xcFalsePackageCleanHour = marketingCommonConfig.getXcFalsePackageCleanHour();
            XiechengCollidingDataPackageRuleExample packageRuleExample = new XiechengCollidingDataPackageRuleExample();
            packageRuleExample.createCriteria()
                    .andCollidingEndTimeGreaterThanOrEqualTo(DateHelper.getDateByHour(DateHelper.parseDate(cleanTime), xcFalsePackageCleanHour))
                    .andIsDeleteEqualTo(0);
            List<XiechengCollidingDataPackageRule> packageRules = xiechengCollidingDataPackageRuleMapper.selectByExample(packageRuleExample);
            packageIds = packageRules.stream().map(xiechengCollidingDataPackageRule -> xiechengCollidingDataPackageRule.getPackageId()
                    .toString()).collect(Collectors.toSet()).stream().collect(Collectors.joining(","));
        }
        //组装全sql
        StringBuilder querySqlBuilder = new StringBuilder();
        querySqlBuilder
                .append("SELECT a.id, a.cell FROM ")
                .append("(")
                .append(queryRuleScoreDataSql.toString())
                .append(") AS a ")
                .append("LEFT JOIN b_xiecheng_colliding_data_loop_cycle AS b ON a.cell = b.cell_sha256_code_list AND b.is_delete = 0 ");
        if (StringUtils.isNotEmpty(packageIds)) {
            querySqlBuilder.append("LEFT JOIN b_xiecheng_colliding_data_rob AS c ON a.cell = c.cell_sha256_code_list and c.package_id in (")
                    .append(packageIds)
                    .append(") and c.is_delete = 0 ");
        }
        querySqlBuilder.append("WHERE b.id IS NULL AND a.is_delete = 0");
        if (StringUtils.isNotEmpty(packageIds)) {
            querySqlBuilder.append(" AND c.id IS NULL ");
        }
        return querySqlBuilder.toString();
    }

    /**
     * 动态补充包与跑分数据交集量级sql
     *
     * @param jsonObject
     * @param batchNumberList
     * @param xcDynaFalsePackageIds
     * @return
     */
    private String dynaPackageDeleteCondition(JSONObject jsonObject, List<String> batchNumberList,
                                              List<String> xcDynaFalsePackageIds, Boolean isPreview) {
        if (CollectionUtils.isEmpty(xcDynaFalsePackageIds)) {
            xcDynaFalsePackageIds = Arrays.asList("120007");
        }
        String xcDynaFalsePackageIdString = xcDynaFalsePackageIds.stream()
                .collect(Collectors.joining(",", "(", ")"));
        String dynaDataSql = "select cell_sha256_code_list as cell,id from b_xiecheng_colliding_data_rob where is_delete = 0 and package_id in "
                + xcDynaFalsePackageIdString;
        String scoreSql = scoreSql(jsonObject, batchNumberList);
        StringBuilder condition = new StringBuilder();
        String whereCondition;
        if (isPreview) {
            whereCondition = " where score.id is not null";
        } else {
            whereCondition = " where score.id is null";
        }
        condition.append("select count(0) from (").append(dynaDataSql).append(") dyna left join (").append(scoreSql)
                .append(") score on dyna.cell = score.cell ").append(whereCondition);
        return condition.toString();
    }

    private String falseDataCondition(JSONObject jsonObject, List<String> batchNumberList, String cleanTime) {
        String cycleDataSql = "select cell_sha256_code_list as cell,id from b_xiecheng_colliding_data_loop_cycle where is_delete = 0";
        String scoreSql = scoreSql(jsonObject, batchNumberList);
        StringBuilder falseAndscoreSql = new StringBuilder();
        StringBuilder whereSql = new StringBuilder();
        //与True的全量数据去重
        falseAndscoreSql.append("select score.cell,score.id from (").append(scoreSql).append(") score left join (").append(cycleDataSql)
                .append(") cycle on score.cell = cycle.cell ");
        //where条件拼接
        whereSql.append(" where cycle.id is null");
        if (StringUtils.isNotEmpty(cleanTime)) {
            XiechengCollidingDataPackageRuleExample packageRuleExample = new XiechengCollidingDataPackageRuleExample();
            packageRuleExample.createCriteria().andCollidingEndTimeGreaterThanOrEqualTo(DateHelper.parseDate(cleanTime)).andIsDeleteEqualTo(0);
            List<XiechengCollidingDataPackageRule> packageRules = xiechengCollidingDataPackageRuleMapper.selectByExample(packageRuleExample);
            String packageId = packageRules.stream().map(xiechengCollidingDataPackageRule -> xiechengCollidingDataPackageRule.getPackageId()
                    .toString()).collect(Collectors.toSet()).stream().collect(Collectors.joining(","));
            //清洗时间在撞库区间内去重，业务应规避此条件
            if (StringUtils.isNotEmpty(packageId)) {
                String FalseDataSql = "select cell_sha256_code_list as cell,id from b_xiecheng_colliding_data_rob where package_id in (" +
                        packageId + ") and " + "is_delete=0";
                falseAndscoreSql.append("left join (").append(FalseDataSql).append(") rob on score.cell = rob.cell ");
                whereSql.append(" and rob.id is null");
            }
        }
        //与待清洗去重
        XiechengCollidingDataProcessTaskExample processTaskExample = new XiechengCollidingDataProcessTaskExample();
        processTaskExample.createCriteria().andTaskTypeEqualTo(0).andTaskStatusEqualTo(0).andIsDeleteEqualTo(0);
        List<XiechengCollidingDataProcessTask> processTasks = xiechengCollidingDataProcessTaskMapper.selectByExample(processTaskExample);
        processTasks.forEach((XiechengCollidingDataProcessTask processTask) -> {
            falseAndscoreSql.append(" left join (").append(processTask.getTaskExecutionSql()).append(") d").append(processTask.getId())
                    .append(" on score.cell = ").append("d").append(processTask.getId()).append(".cell ");
            whereSql.append(" and  d").append(processTask.getId()).append(".id is null");
        });
        return falseAndscoreSql.append(whereSql).toString();
    }

    /**
     * 跑分数据和周期表数据交集量级预览
     *
     * @param jsonObject
     * @param batchNumberList
     * @param collidingFilterDTO
     * @param querySqls
     */
    public void cycleDataQueryOpt(JSONObject jsonObject, List<String> batchNumberList,
                                  XieChengCollidingFilterDTO collidingFilterDTO, List<String> querySqls) {
        String cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where release_time>= " +
                "DATE_ADD(CURDATE(), INTERVAL 1 DAY)  and  release_time< DATE_ADD(CURDATE(), INTERVAL 7 DAY) and is_delete=0";
        //True关联查询
        //true筛选字段处理
        String condition = XieChengEsJsonHandler.zkTrueCondition(collidingFilterDTO);
        if (StringUtils.isNotEmpty(condition)) {
            cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where " + condition
                    + " and is_delete=0";
        }
        String sqlCondition = EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, "");
        for (String batchNumber : batchNumberList) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }
            String scoreSql = "select id,cell from b_xiecheng_colliding_".concat(batchNumber)
                    .concat(" where ").concat(sqlCondition).concat(" and is_delete=0");
            StringBuilder cycleAndScoreSql = new StringBuilder();
            cycleAndScoreSql.append("select count(1) from (").append(scoreSql).append(") score inner join (").append(cycleSql).append(") cycle on " +
                    "score.cell = cycle.cell;");
            querySqls.add(cycleAndScoreSql.toString());
        }
    }

    public String cycleDataQuery(JSONObject jsonObject, List<String> batchNumberList, XieChengCollidingFilterDTO xieChengCollidingFilterDTO) {
        String scoreSql = scoreSql(jsonObject, batchNumberList);
        String cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where release_time>= " +
                "DATE_ADD(CURDATE(), INTERVAL 1 DAY)  and  release_time< DATE_ADD(CURDATE(), INTERVAL 7 DAY) and is_delete=0";
        //True关联查询
        //true筛选字段处理
        String condition = XieChengEsJsonHandler.zkTrueCondition(xieChengCollidingFilterDTO);
        if (StringUtils.isNotEmpty(condition)) {
            cycleSql = "select  cell_sha256_code_list as cell from  b_xiecheng_colliding_data_loop_cycle where " + condition
                    + " and is_delete=0";
        }
        StringBuilder cycleAndscoreSql = new StringBuilder();
        cycleAndscoreSql.append("select count(1) from (").append(scoreSql).append(") score inner join (").append(cycleSql).append(") cycle on " +
                "score.cell = cycle.cell;");
        return cycleAndscoreSql.toString();
    }

    private String cycleDataQueryForDelete(JSONObject jsonObject, List<String> batchNumberList
            , XieChengCollidingFilterDTO xieChengCollidingFilterDTO) {
        String scoreSql = scoreSql(jsonObject, batchNumberList);
        Date cleanTime = DateHelper.parseDate(xieChengCollidingFilterDTO.getCleanTime());
        Date cleanTimeEnd = DateHelper.addDays(cleanTime, 1);
        Date endTime = DateHelper.addDays(cleanTime, 7);
        String cleanDateTime = DateHelper.dateToDateTime(cleanTime);
        String cleanEndTime = DateHelper.dateToDateTime(cleanTimeEnd);
        String endDateTime = DateHelper.dateToDateTime(endTime);
        String cycleSql = String.format
                ("select cell_sha256_code_list as cell from b_xiecheng_colliding_data_loop_cycle " +
                                "where (release_time < '%s' or (release_time >= '%s' and release_time < '%s')) and is_delete=0"
                        , cleanDateTime, cleanEndTime, endDateTime);
        //True关联查询
        //true筛选字段处理
        String condition = XieChengEsJsonHandler.zkTrueCondition(xieChengCollidingFilterDTO);
        if (StringUtils.isNotEmpty(condition)) {
            if (condition.contains("release_time")) {
                cycleSql = String.format
                        ("select cell_sha256_code_list as cell from b_xiecheng_colliding_data_loop_cycle where %s and is_delete=0"
                                , condition);
            } else {
                cycleSql = String.format
                        ("select cell_sha256_code_list as cell from b_xiecheng_colliding_data_loop_cycle " +
                                        "where %s and (release_time < '%s' or (release_time >= '%s' and release_time < '%s')) and is_delete=0"
                                , condition, cleanDateTime, cleanEndTime, endDateTime);
            }
        }
        StringBuilder cycleAndscoreSql = new StringBuilder();
        cycleAndscoreSql.append("select count(1) from (").append(cycleSql).append(") cycle inner join (").append(scoreSql).append(") score on " +
                "score.cell = cycle.cell;");
        return cycleAndscoreSql.toString();
    }

    private String cycleDataDeleteQuery(JSONObject jsonObject, List<String> batchNumberList, String cleanDate) {
        String scoreSql = scoreSql(jsonObject, batchNumberList);
        Date cleanTime = DateHelper.parseDate(cleanDate);
        Date cleanTimeEnd = DateHelper.addDays(cleanTime, 1);
        Date endTime = DateHelper.addDays(cleanTime, 7);
        String cleanDateTime = DateHelper.dateToDateTime(cleanTime);
        String cleanEndTime = DateHelper.dateToDateTime(cleanTimeEnd);
        String endDateTime = DateHelper.dateToDateTime(endTime);
        String cycleSql = String.format
                ("select cell_sha256_code_list as cell from b_xiecheng_colliding_data_loop_cycle " +
                                "where (release_time < '%s' or (release_time >= '%s' and release_time < '%s')) and is_delete=0"
                        , cleanDateTime, cleanEndTime, endDateTime);
        //True关联查询
        StringBuilder cycleAndscoreSql = new StringBuilder();
        cycleAndscoreSql.append("select count(1) from (").append(cycleSql).append(") cycle left join (").append(scoreSql).append(") score on " +
                "score.cell = cycle.cell where score.id is null;");
        return cycleAndscoreSql.toString();
    }

    private String cycleDataDeleteQueryForOpt(JSONObject jsonObject, String batchNumber, String cleanDate) {
        String conditions = EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, "");
        Date cleanTime = DateHelper.parseDate(cleanDate);
        Date cleanTimeEnd = DateHelper.addDays(cleanTime, 1);
        Date endTime = DateHelper.addDays(cleanTime, 7);
        String cleanDateTime = DateHelper.dateToDateTime(cleanTime);
        String cleanEndTime = DateHelper.dateToDateTime(cleanTimeEnd);
        String endDateTime = DateHelper.dateToDateTime(endTime);
        return String.format("select count(0) from b_xiecheng_colliding_data_loop_cycle cycle " +
                        "join b_xiecheng_colliding_%s score on cycle.cell_sha256_code_list = score.cell and score.is_delete = 0 " +
                        "left join (select id, cell, is_delete from b_xiecheng_colliding_%s where %s) scoreCd " +
                        "on score.cell = scoreCd.cell and scoreCd.is_delete = 0 " +
                        "where cycle.is_delete = 0 and scoreCd.id is null " +
                        "and (cycle.release_time < '%s' or (cycle.release_time >= '%s' and cycle.release_time < '%s'))"
                , batchNumber, batchNumber, conditions, cleanDateTime, cleanEndTime, endDateTime);
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
                scoreSql = scoreSql.concat("select id,cell from b_xiecheng_colliding_").concat(batchNumberList.get(i)).concat(" where ")
                        .concat(sqlCondition).concat(" and is_delete=0 ");
            } else {
                scoreSql = scoreSql.concat("select id,cell from b_xiecheng_colliding_").concat(batchNumberList.get(i)).concat(" where ")
                        .concat(sqlCondition).concat(" and is_delete=0 ").concat(" union all ");
            }
        }
        return scoreSql;
    }

    private String scoreSql(List<String> batchNumberList) {
        String scoreSql = "";
        for (int i = 0; i < batchNumberList.size(); i++) {
            if (i == batchNumberList.size() - 1) {
                scoreSql = scoreSql.concat("select id,cell from b_xiecheng_colliding_").concat(batchNumberList.get(i)).concat(" where ")
                        .concat(" is_delete=0 ");
            } else {
                scoreSql = scoreSql.concat("select id,cell from b_xiecheng_colliding_").concat(batchNumberList.get(i)).concat(" where ")
                        .concat(" is_delete=0 ").concat(" union all ");
            }
        }
        return scoreSql;
    }


    private Boolean isXieChengData(PushCustomerDTO dto) {
        Boolean isXieCheng = Boolean.FALSE;
        JSONArray datas = JSON.parseObject(dto.getmRuleCondition()).getJSONArray("data");
        if (!CollectionUtils.isEmpty(datas)) {
            Object result = datas.stream().filter(obj -> ("result").equals(
                    ((JSONObject) obj).getString("key"))).findAny().orElse(null);
            Object blacklistDelete = datas.stream().filter(obj -> ("blacklist_delete").equals(
                    ((JSONObject) obj).getString("key"))).findAny().orElse(null);
            //api_code为携程且筛选条件传入result
            if (marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().contains(dto.getApiCode())
                    && (!ObjectUtils.isEmpty(result) || !ObjectUtils.isEmpty(blacklistDelete))) {
                isXieCheng = Boolean.TRUE;
            }
        }
        return isXieCheng;
    }


    @Override
    public Result<PushViewVO> pushPreview(PushCustomerDTO dto) {
        // 使用策略模式处理推送预览
        try {
            // 1. 判断任务类型，确定使用哪个策略
            PushPreviewStrategyEnum strategyType;

            if (Objects.equals(dto.getTaskType(), TaskTypeEnum.UPLOAD_TASKS.getValue())) {
                // 上传任务策略
                strategyType = PushPreviewStrategyEnum.UPLOAD_TASK;
            } else {
                // 跑分任务需要参数校验
                if (dto.getBatchNumberList().isEmpty()) {
                    return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("批次号不能为空");
                }
                if (dto.getFileIdList().isEmpty()) {
                    return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("fileIdList不能为空");
                }
                // 校验加密类型一致性
                AssertResult.assertResult(checkThreekEnc(dto.getFileIdList()));

                // 判断跑分任务的具体类型
                if (isXieChengData(dto)) {
                    // 携程跑分任务策略
                    strategyType = PushPreviewStrategyEnum.XIE_CHENG_SCORE;
                } else if (Objects.nonNull(dto.getIsScoreMerge()) && dto.getIsScoreMerge()) {
                    // 合并跑分任务策略
                    strategyType = PushPreviewStrategyEnum.MERGE_SCORE;
                } else {
                    // 通用跑分任务策略
                    strategyType = PushPreviewStrategyEnum.COMMON_SCORE;
                }
            }

            // 2. 根据策略类型获取策略并执行
            IPushPreviewStrategy strategy = pushPreviewStrategyFactory.getStrategy(strategyType);
            return strategy.execute(dto);
        } catch (Exception e) {
            log.error("推送预览执行失败，apiCode: {}, taskType: {}", dto.getApiCode(), dto.getTaskType(), e);
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送预览执行失败: " + e.getMessage());
        }
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
    @Transactional(rollbackFor = Exception.class)
    public Result collidingDataDelete(PushCustomerDTO dto) {
        //result = true,result = false && info = NULL && blacklist_delete = true
        if (!isXieChengData(dto)) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("缺失result或blacklist_delete筛选条件");
        }
        JSONObject jsonObject = JSON.parseObject(dto.getmRuleCondition());
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        XcProcessTaskEnum xcProcessTaskEnum = getTaskType(collidingFilterDTO);
        if (xcProcessTaskEnum == XcProcessTaskEnum.PROCESS_BALCKLIST_DELETE
                && jsonObject.getJSONArray("data").size() == 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("未选择剔除条件，无法剔除！");
        }
        List<String> batchNumberList = dto.getBatchNumberList();
        XiechengCollidingDataProcessTask xiechengCollidingDataProcessTask = new XiechengCollidingDataProcessTask();
        xiechengCollidingDataProcessTask.setApiCode(dto.getApiCode());
        xiechengCollidingDataProcessTask.setBatchNumber(String.join(",", batchNumberList));
        xiechengCollidingDataProcessTask.setTaskStatus(0);
        xiechengCollidingDataProcessTask.setDiscreetNumber(dto.getmPlanNum());
        try {
            xiechengCollidingDataProcessTask.setTaskStartTime(DateHelper.parseDate(collidingFilterDTO.getCleanTime()));
        } catch (Exception e) {
            log.error("clean_time日期格式异常", e.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("clean_time日期格式异常");
        }
        xiechengCollidingDataProcessTask.setTaskType(xcProcessTaskEnum.getTaskType());
        xiechengCollidingDataProcessTask.setTaskExecutionConditions(EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, ""));
        xiechengCollidingDataProcessTask.setTaskExecutionSql(
                getExecutionSql(jsonObject, batchNumberList, collidingFilterDTO.getCleanTime(), xcProcessTaskEnum));
        xiechengCollidingDataProcessTask.setCreateTime(new Date());
        xiechengCollidingDataProcessTask.setUpdateTime(new Date());
        int i = xiechengCollidingDataProcessTaskMapper.insertSelective(xiechengCollidingDataProcessTask);
        if (i > 0 && !CollectionUtils.isEmpty(batchNumberList) && xcProcessTaskEnum == XcProcessTaskEnum.PROCESS_DYNA_FALSE) {
            for (String batchNumber : batchNumberList) {
                XiechengCollidingTaskBatch xiechengCollidingTaskBatch = new XiechengCollidingTaskBatch();
                xiechengCollidingTaskBatch.setApiCode(dto.getApiCode());
                xiechengCollidingTaskBatch.setCollidingDataTaskId(xiechengCollidingDataProcessTask.getId());
                xiechengCollidingTaskBatch.setBatchNumber(batchNumber);
                xiechengCollidingTaskBatch.setType(xcProcessTaskEnum.getBatchType());
                xiechengCollidingTaskBatch.setStatus(0);
                xiechengCollidingTaskBatch.setCreateTime(new Date());
                xiechengCollidingTaskBatch.setUpdateTime(new Date());
                xiechengCollidingTaskBatchMapper.insertSelective(xiechengCollidingTaskBatch);
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result collidingDataCycleDelete(XcCycleDeleteDTO dto) {
        JSONObject jsonObject = JSON.parseObject(dto.getMRuleCondition());
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        List<String> batchNumberList = dto.getBatchNumberList();
        String batchNumbers = String.join(",", batchNumberList);
        String condition = EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, "");
        Date taskStartTime = DateHelper.parseDate(collidingFilterDTO.getCleanTime());
        List<XcDeleteMagnitudeDistDTO> magnitudeDistList = dto.getDeleteMagnitudeDistList();
        magnitudeDistList.sort(Comparator.comparing(XcDeleteMagnitudeDistDTO::getReleaseTimeBegin));
        for (XcDeleteMagnitudeDistDTO magnitudeDistDTO : magnitudeDistList) {
            XiechengCollidingDataProcessTask task = new XiechengCollidingDataProcessTask();
            task.setApiCode(dto.getApiCode());
            task.setBatchNumber(batchNumbers);
            task.setTaskStatus(XcProcessTaskStatusEnum.EXECUTE_WAITED.getValue());
            task.setTaskStartTime(taskStartTime);
            task.setTaskType(XcProcessTaskEnum.PROCESS_DELETE.getTaskType());
            task.setTaskExecutionConditions(condition);
            task.setTaskExecutionSql(getCycleDeleteSql(
                    condition, batchNumberList, magnitudeDistDTO.getReleaseTimeBegin(), magnitudeDistDTO.getReleaseTimeEnd()));
            task.setReleaseTimeBegin(Date.from(magnitudeDistDTO.getReleaseTimeBegin().atZone(ZoneId.systemDefault()).toInstant()));
            task.setReleaseTimeEnd(Date.from(magnitudeDistDTO.getReleaseTimeEnd().atZone(ZoneId.systemDefault()).toInstant()));
            task.setDiscreetNumber(magnitudeDistDTO.getDeleteNum());
            task.setRemainingNum(magnitudeDistDTO.getRemainingNum());
            task.setFreeNum(magnitudeDistDTO.getFreeNum());
            task.setCreateTime(new Date());
            task.setUpdateTime(new Date());
            xiechengCollidingDataProcessTaskMapper.insertSelective(task);
            for (String batchNumber : batchNumberList) {
                XiechengCollidingTaskBatch batch = new XiechengCollidingTaskBatch();
                batch.setApiCode(dto.getApiCode());
                batch.setCollidingDataTaskId(task.getId());
                batch.setBatchNumber(batchNumber);
                batch.setType(XcProcessTaskEnum.PROCESS_DELETE.getBatchType());
                batch.setStatus(XcProcessBatchStatusEnum.EXECUTE_WAITED.getValue());
                batch.setCreateTime(new Date());
                batch.setUpdateTime(new Date());
                xiechengCollidingTaskBatchMapper.insertSelective(batch);
            }
            if (magnitudeDistDTO.getReleaseTimeBegin().toLocalDate().isEqual(LocalDate.now())) {
                Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
                Map<String, Object> groupInfo = webHookInfo.get(DingDingAlarmFunctionEnum.XIECHENG_PPD_NOTICE.toString());
                dingDingRobotHookService.sendDingDingTextMessage("今天的周期数据配置了剔除任务，请关注！", groupInfo);
            }

        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * @param condition
     * @param batchNumberList
     * @param releaseTimeBegin
     * @param releaseTimeEnd
     * @return
     */
    private String getCycleDeleteSql(String condition, List<String> batchNumberList, LocalDateTime releaseTimeBegin, LocalDateTime releaseTimeEnd) {
        List<String> querySqls = new ArrayList<>();
        String begin = releaseTimeBegin.format(formatter);
        String end = releaseTimeEnd.format(formatter);
        for (String batchNumber : batchNumberList) {
            String sql = String.format("select count(0) from b_xiecheng_colliding_data_loop_cycle cycle " +
                            "join b_xiecheng_colliding_%s score on cycle.cell_sha256_code_list = score.cell and score.is_delete = 0 " +
                            "left join (select id, cell, is_delete from b_xiecheng_colliding_%s where %s) scoreCd " +
                            "on score.cell = scoreCd.cell and scoreCd.is_delete = 0 " +
                            "where cycle.is_delete = 0 and scoreCd.id is null " +
                            "and cycle.release_time > '%s' and cycle.release_time <= '%s'"
                    , batchNumber, batchNumber, condition, begin, end);
            querySqls.add(sql);
        }
        return String.join(";", querySqls);
    }

    /**
     * @param jsonObject
     * @param batchNumberList
     * @param cleanTime
     * @param xcProcessTaskEnum
     * @return java.lang.String
     * @description 生成预估量级的sql：true剔除、false动态补充包剔除、黑名单剔除
     * @author hedongshuo
     * @date 2024/11/10 14:28
     **/
    private String getExecutionSql(JSONObject jsonObject, List<String> batchNumberList,
                                   String cleanTime, XcProcessTaskEnum xcProcessTaskEnum) {
        if (xcProcessTaskEnum == XcProcessTaskEnum.PROCESS_DELETE) {
            Boolean xcTrueDeletePushPreviewOptFlag = marketingCommonConfig.getXcTrueDeletePushPreviewOptFlag();
            if (xcTrueDeletePushPreviewOptFlag) {
                List<String> querySqls = new ArrayList<>();
                cycleDataDeleteQueryOpt(jsonObject, batchNumberList, cleanTime, querySqls);
                return String.join(";", querySqls);
            } else {
                return cycleDataDeleteQuery(jsonObject, batchNumberList, cleanTime);
            }
        }
        if (xcProcessTaskEnum == XcProcessTaskEnum.PROCESS_DYNA_FALSE) {
            Boolean xcFalsePackageDynaPushPreviewOptFlag = marketingCommonConfig.getXcFalsePackageDynaPushPreviewOptFlag();
            if (xcFalsePackageDynaPushPreviewOptFlag) {
                List<String> querySqls = new ArrayList<>();
                dynaPackageDeleteConditionOpt(
                        jsonObject, batchNumberList, marketingCommonConfig.getXcDynaFalsePackageIds(), querySqls, false);
                return String.join(";", querySqls);
            } else {
                return dynaPackageDeleteCondition(
                        jsonObject, batchNumberList, marketingCommonConfig.getXcDynaFalsePackageIds(), false);
            }
        }
        return "";
    }

    private XcProcessTaskEnum getTaskType(XieChengCollidingFilterDTO collidingFilterDTO) {
        String result = collidingFilterDTO.getResult();
        String info = collidingFilterDTO.getInfo();
        String blacklistDelete = collidingFilterDTO.getBlacklist_delete();
        if (StringUtils.isNotEmpty(result) && result.equalsIgnoreCase("true")) {
            return XcProcessTaskEnum.PROCESS_DELETE;
        }
        if (StringUtils.isNotEmpty(result) && result.equalsIgnoreCase("false")
                && !Objects.isNull(info) && (info.equals("") || info.equalsIgnoreCase("NULL"))) {
            return XcProcessTaskEnum.PROCESS_DYNA_FALSE;
        }
        if (StringUtils.isNotEmpty(blacklistDelete) && blacklistDelete.equalsIgnoreCase("true")) {
            return XcProcessTaskEnum.PROCESS_BALCKLIST_DELETE;
        }
        return null;
    }

    @Override
    public Result collidingDataPachageMake(PushCustomerDTO dto) {
        if (!isXieChengData(dto)) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("缺失result或clean_time筛选条件");
        }
        JSONObject jsonObject = JSON.parseObject(dto.getmRuleCondition());
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        XiechengCollidingDataProcessTask xiechengCollidingDataProcessTask = new XiechengCollidingDataProcessTask();
        xiechengCollidingDataProcessTask.setApiCode(dto.getApiCode());
        xiechengCollidingDataProcessTask.setBatchNumber(String.join(",", dto.getBatchNumberList()));
        xiechengCollidingDataProcessTask.setTaskStatus(0);
        xiechengCollidingDataProcessTask.setDiscreetNumber(dto.getmPrePlanNum());
        Integer xcFalsePackageCleanHour = marketingCommonConfig.getXcFalsePackageCleanHour();
        try {
            xiechengCollidingDataProcessTask
                    .setTaskStartTime(DateHelper.getDateByHour(
                            DateHelper.parseDate(collidingFilterDTO.getCleanTime()), xcFalsePackageCleanHour));
        } catch (Exception e) {
            log.error("clean_time日期格式异常", e.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("clean_time日期格式异常");
        }
        xiechengCollidingDataProcessTask.setTaskType(0);
        xiechengCollidingDataProcessTask.setTaskExecutionConditions(EsConditionTransferSqlUtil.jsonTransferSql(jsonObject, ""));
        Boolean xcFalsePackagePushPreviewOptFlag = marketingCommonConfig.getXcFalsePackagePushPreviewOptFlag();
        if (!xcFalsePackagePushPreviewOptFlag) {
            xiechengCollidingDataProcessTask.setTaskExecutionSql(falseDataCondition(jsonObject, dto.getBatchNumberList(),
                    collidingFilterDTO.getCleanTime()));
        } else {
            List<String> querySqls = new ArrayList<>();
            falseDataQueryOpt(jsonObject, dto.getBatchNumberList(), querySqls, collidingFilterDTO.getCleanTime(), false);
            xiechengCollidingDataProcessTask.setTaskExecutionSql(String.join(";", querySqls));
        }
        xiechengCollidingDataProcessTask.setCreateTime(new Date());
        xiechengCollidingDataProcessTask.setUpdateTime(new Date());
        xiechengCollidingDataProcessTaskMapper.insertSelective(xiechengCollidingDataProcessTask);
        XieChengCollidingDataPackage xieChengCollidingDataPackage = new XieChengCollidingDataPackage();
        xieChengCollidingDataPackage.setPackageName(dto.getDataPackageName());
        xieChengCollidingDataPackage.setCreateTime(new Date());
        xieChengCollidingDataPackage.setUpdateTime(new Date());
        xieChengCollidingDataPackage.setCollidingDataTaskId(xiechengCollidingDataProcessTask.getId());
        xieChengCollidingDataPackage.setDiscreetNumber(dto.getmPrePlanNum());
        xieChengCollidingDataPackageMapper.insertSelective(xieChengCollidingDataPackage);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * @param dto
     * @return com.br.marketing.common.commondto.Result<java.lang.Integer>
     * @description 动态补充包剔除量级展示，result = false 且 info = NULL
     * @author hedongshuo
     * @date 2025/10/27 11:06
     **/
    @Override
    public Result<Integer> collidingDataDeleteNum(PushCustomerDTO dto) {
        int num = 0;
        JSONObject jsonObject = JSON.parseObject(dto.getmRuleCondition());
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(jsonObject, collidingFilterDTO);
        String result = collidingFilterDTO.getResult();
        String info = collidingFilterDTO.getInfo();
        String blacklist_delete = collidingFilterDTO.getBlacklist_delete();
        if (StringUtils.isNotEmpty(result) && StringUtils.isNotEmpty(blacklist_delete)) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("result与blacklist_delete不能同时传入！");
        }
        String deleteSql = null;
        List<String> querySqls = new ArrayList<>();
        if (StringUtils.isNotEmpty(result)) {
            if ("false".equals(result)) {
                if (Objects.isNull(info)) {
                    return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("result=false时，info不能为空！");
                } else {
                    if (info.equals("") || info.equalsIgnoreCase("NULL")) {
                        Boolean xcFalsePackageDynaPushPreviewOptFlag = marketingCommonConfig.getXcFalsePackageDynaPushPreviewOptFlag();
                        if (xcFalsePackageDynaPushPreviewOptFlag) {
                            dynaPackageDeleteConditionOpt(
                                    jsonObject, dto.getBatchNumberList(), marketingCommonConfig.getXcDynaFalsePackageIds(), querySqls, false);
                        } else {
                            deleteSql = dynaPackageDeleteCondition(jsonObject, dto.getBatchNumberList(),
                                    marketingCommonConfig.getXcDynaFalsePackageIds(), false);
                        }
                    }
                }
            }
        }
        // doris查询
        try {
            if (CollectionUtils.isEmpty(querySqls)) {
                num = scoreRecordMapper.getXieChengDataNumdoris_(deleteSql);
            } else {
                num = getTotalOpt(querySqls, marketingCommonConfig.getXcFalsePackageOptSoleNum());
            }
        } catch (Exception e) {
            if (CollectionUtils.isEmpty(querySqls)) {
                log.error("规则中心-动态补充包剔除量级查询Doris异常,sql={}", deleteSql, e);
            } else {
                log.error("规则中心-动态补充包剔除量级查询Doris异常,sqls={}", String.join(";", querySqls), e);
            }
        }
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(num);
    }

    @Override
    public Result<List<XcDeleteMagnitudeDistDTO>> collidingDataCycleDeleteMagnitudeDist(XcCycleDeleteNumDTO dto) {
        //1.将mRuleCOndition中的result和clean_time放到collidingFilterDTO中；将condition放到jsonObject中
        JSONObject conditionJson = JSON.parseObject(dto.getMRuleCondition());
        XieChengCollidingFilterDTO collidingFilterDTO = new XieChengCollidingFilterDTO();
        XieChengEsJsonHandler.handlerJson(conditionJson, collidingFilterDTO);
        String result = collidingFilterDTO.getResult();
        if (StringUtils.isEmpty(result) || !result.equals("true")) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("result必须为true！");
        }
        //2.校验releaseTimeBegin是否大于当前时间4h以上
        LocalDateTime releaseTimeBegin = dto.getReleaseTimeBegin();
        LocalDateTime releaseTimeEnd = dto.getReleaseTimeEnd();
        if (releaseTimeBegin.isBefore(LocalDateTime.now().plusHours(4))) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("剔除周期数据的释放时间范围的开始时间必须大于当前时间4h以上！");
        }
        //3.校验releaseTimeRange与存量【b_xiecheng_colliding_data_process_task】是否有交叉周期数据剔除范围
        //releaseTimeRange在使用的时候，是左闭右闭去筛选数据，即[A,B]，判断其和[C,D]是否有交叉,需要A<=D且B>=C
        List<XcDeleteMagnitudeDistDTO> stockReleaseTimeRanges =
                xiechengCollidingDataProcessTaskMapper.selectReleaseTimeRanges(dto.getApiCode());
        if (stockReleaseTimeRanges.size() != 0) {
            for (XcDeleteMagnitudeDistDTO stockReleaseTimeRange : stockReleaseTimeRanges) {
                if ((!releaseTimeBegin.isAfter(stockReleaseTimeRange.getReleaseTimeEnd())
                        && !releaseTimeEnd.isBefore(stockReleaseTimeRange.getReleaseTimeBegin()))) {
                    return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("剔除周期数据的施放时间范围与已生成的剔除任务时间有交叉，请检查！");
                }
            }
        }
        //4.按自然日分割releaseTimeRange
        List<TimeRange> timeRanges = TimeUtils.splitByNaturalDays(releaseTimeBegin, releaseTimeEnd);
        //5.计算量级
        if (marketingCommonConfig.getXcDeleteMagnitudeOptSwitch()) {
            log.warn("规则中心-周期数据分天剔除查询-opt");
            return getResultOpt(dto.getBatchNumberList(), conditionJson, timeRanges);
        }
        log.warn("规则中心-周期数据分天剔除查询");
        return getResult(dto, conditionJson, timeRanges);
    }

    private Result<List<XcDeleteMagnitudeDistDTO>> getResultOpt
            (List<String> batchNumbers, JSONObject conditionJson, List<TimeRange> timeRanges) {
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory
                .getThreadPool(ThreadPoolNameEnum.XIECHENG_CYCLE_DELETE_EST.getName(), 16, 20);
        String sqlCondition = EsConditionTransferSqlUtil.jsonTransferSql(conditionJson, "");
        //1.获取timeRangePlusList
        List<TimeRangePlus> timeRangePlusList = new ArrayList<>();
        int order = 0;
        for (TimeRange timeRange : timeRanges) {
            timeRangePlusList.add(new TimeRangePlus(timeRange, ++order));
        }
        //2.获取timeRange范围外的量级
        Map<String, Long> outCycleMagnitudeMap = getOutCycleMagnitudes(timeRangePlusList);
        List<Future<ImmutablePair<Map<String, Long>, Map<String, Long>>>> futures = new ArrayList<>();
        //3.获取周期数据与跑分数据的交集量级
        try {
            for (String batchNumber : batchNumbers) {
                futures.add(threadPool.submit(() ->
                        magnitudeDistCalOpt(timeRangePlusList, batchNumber, sqlCondition)
                ));
            }
            List<Pair<Map<String, Long>, Map<String, Long>>> futureResults = new ArrayList<>();
            long globalStart = System.currentTimeMillis();
            long globalTimeout = TimeUnit.MINUTES.toMillis(2); // 总超时时间2分钟
            for (Future<ImmutablePair<Map<String, Long>, Map<String, Long>>> future : futures) {
                long timeLeft = globalTimeout - (System.currentTimeMillis() - globalStart);
                if (timeLeft <= 0) {
                    log.error("规则中心-周期数据剔除量级查询整体处理超时");
                    return new Result<List<XcDeleteMagnitudeDistDTO>>()
                            .setCode(ResultCode.FAIL.getValue())
                            .setMessage("服务异常");
                }
                try {
                    // 单任务超时限制
                    futureResults.add(future.get(timeLeft, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    // 只要有一个异常，取消所有任务
                    futures.forEach(fu -> fu.cancel(true));
                    log.error("规则中心-周期数据剔除量级查询异常，{}", e.getMessage(), e);
                    return new Result<List<XcDeleteMagnitudeDistDTO>>()
                            .setCode(ResultCode.FAIL.getValue())
                            .setMessage("服务异常");
                }
            }
            //4.并行聚合结果
            Pair<Map<String, Long>, Map<String, Long>> aggregateResult = aggregateResultsParallel(futureResults);
            //5.汇总量级
            return calculateMagnitudeOpt(timeRangePlusList, outCycleMagnitudeMap, aggregateResult);
        } catch (Exception e) {
            log.error("规则中心-周期数据剔除量级查询异常，{}", e.getMessage(), e);
            return new Result<List<XcDeleteMagnitudeDistDTO>>()
                    .setCode(ResultCode.FAIL.getValue())
                    .setMessage("服务异常");
        } finally {
            threadPool.shutdownAndAwaitTermination();
        }
    }

    /**
     * 汇总量级
     * @param timeRangePlusList
     * @param outCycleMagnitudeMap
     * @param aggregateResult
     * @return
     */
    private Result<List<XcDeleteMagnitudeDistDTO>> calculateMagnitudeOpt(List<TimeRangePlus> timeRangePlusList, 
                                                                         Map<String, Long> outCycleMagnitudeMap,
                                                                         Pair<Map<String, Long>, Map<String, Long>> aggregateResult) {
        List<XcDeleteMagnitudeDistDTO> dtos = new ArrayList<>();
        //撞得量级阈值
        int totalThreshold = variableAllocationService.getVariableAllocation().getNormalQuantity();
        Map<String, Long> interWithoutConMagnitudeMap = aggregateResult.getLeft();
        Map<String, Long> interWithConMagnitudeMap = aggregateResult.getRight();
        for (TimeRangePlus timeRangePlus : timeRangePlusList) {
            //T日timeRange范围外量级
            long outMagnitude = outCycleMagnitudeMap.
                    get(XcDeletePrefixEnum.OUT.getAlias() + "_" + timeRangePlus.getOrder());
            //timeRange范围内，与跑分数据的交集量级
            long interWithoutConMagnitude = interWithoutConMagnitudeMap.
                    get(XcDeletePrefixEnum.INTER_WITHOUT_CON.getAlias() + "_" + timeRangePlus.getOrder());
            //timeRange范围内，与符合条件的跑分数据的交集量级
            long interWithConMagnitude = interWithConMagnitudeMap.
                    get(XcDeletePrefixEnum.INTER_WITH_CON.getAlias() + "_" + timeRangePlus.getOrder());
            //timeRange范围内的剔除量级
            long deleteNum = Math.max(0, interWithoutConMagnitude - interWithConMagnitude);
            //空挡量级
            int freeNum = (int) (totalThreshold - outMagnitude - interWithConMagnitude);
            dtos.add(new XcDeleteMagnitudeDistDTO(
                    timeRangePlus.getBegin(), timeRangePlus.getEnd(), (int) deleteNum, (int) interWithConMagnitude, freeNum));
        }
        return new Result<List<XcDeleteMagnitudeDistDTO>>()
                .setCode(ResultCode.SUCCESS.getValue())
                .setDate(dtos);
    }

    /**
     * 并行聚合结果
     */
    private static Pair<Map<String, Long>, Map<String, Long>> aggregateResultsParallel(
            List<Pair<Map<String, Long>, Map<String, Long>>> results) {
        // 使用并行流合并左 Map
        Map<String, Long> leftResult = results.parallelStream()
                .flatMap(pair -> pair.getLeft().entrySet().stream())
                .collect(Collectors.toConcurrentMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Long::sum
                ));

        // 使用并行流合并右 Map
        Map<String, Long> rightResult = results.parallelStream()
                .flatMap(pair -> pair.getRight().entrySet().stream())
                .collect(Collectors.toConcurrentMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Long::sum
                ));
        return new ImmutablePair<>(leftResult, rightResult);
    }

    /**
     * 获取timeRange范围外的量级
     * @param timeRangePlusList
     * @return outCycleMagnitudeMap
     * @description
     * @author hedongshuo
     * @date 2025/12/20 12:47
     **/
    private Map<String, Long> getOutCycleMagnitudes(List<TimeRangePlus> timeRangePlusList) {
        //1.将timeRangePlusList分为今天的和非今天的
        List<TimeRangePlus> notTodayTimeRangePlusList = new ArrayList<>();
        //timeRange范围外量级 <out_order, count>
        Map<String, Long> outCycleMagnitudeMap = new HashMap<>();
        for (TimeRangePlus timeRange : timeRangePlusList) {
            if (timeRange.isToday()) {
                int todayOutCycleMagnitude = xieChengCollidingDataLoopCycleMapper
                        .selectTimeRangeOutMagnitudeForTodaytiflash_(timeRange.getBegin(), timeRange.getEnd());
                outCycleMagnitudeMap.put(XcDeletePrefixEnum.OUT.getAlias() + "_" + timeRange.getOrder(), (long) todayOutCycleMagnitude);
            } else {
                notTodayTimeRangePlusList.add(timeRange);
            }
        }
        //2.非今天的量级
        if (!CollectionUtils.isEmpty(notTodayTimeRangePlusList)) {
            LocalDateTime minBegin = notTodayTimeRangePlusList.get(0).getBegin();
            LocalDateTime maxEnd = notTodayTimeRangePlusList.get(notTodayTimeRangePlusList.size() - 1).getEnd();
            Map<String, Long> notTodayOutMagnitudeMap = xieChengCollidingDataLoopCycleMapper
                    .selectTimeRangeOutMagnitudeForNotTodaystiflash_(notTodayTimeRangePlusList, minBegin, maxEnd, XcDeletePrefixEnum.OUT.getAlias());
            outCycleMagnitudeMap.putAll(notTodayOutMagnitudeMap);
        }
        return outCycleMagnitudeMap;
    }

    private ImmutablePair<Map<String, Long>, Map<String, Long>> magnitudeDistCalOpt(
            List<TimeRangePlus> timeRangePlusList, String batchNumber, String sqlCondition) {
        //1.获取
        LocalDateTime minBegin = timeRangePlusList.get(0).getBegin();
        LocalDateTime maxEnd = timeRangePlusList.get(timeRangePlusList.size() - 1).getEnd();
        //3.周期表与跑分数据交集
        Map<String, Long> interWithoutConMagnitudes = xieChengCollidingDataLoopCycleMapper.selectTimeRangeBetweenWithScoreMagnitudestiflash_(
                timeRangePlusList, minBegin, maxEnd, batchNumber, null, XcDeletePrefixEnum.INTER_WITHOUT_CON.getAlias());
        Map<String, Long> interWithConMagnitudes = xieChengCollidingDataLoopCycleMapper.selectTimeRangeBetweenWithScoreMagnitudestiflash_(
                timeRangePlusList, minBegin, maxEnd, batchNumber, sqlCondition, XcDeletePrefixEnum.INTER_WITH_CON.getAlias());
        return ImmutablePair.of(interWithoutConMagnitudes, interWithConMagnitudes);
    }

    /**
     * 计算量级，方法中主要是多线程的处理
     *
     * @param dto
     * @param conditionJson
     * @param timeRanges
     * @return
     */
    private Result getResult(XcCycleDeleteNumDTO dto, JSONObject conditionJson, List<TimeRange> timeRanges) {
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory
                .getThreadPool(ThreadPoolNameEnum.XIECHENG_CYCLE_DELETE_EST.getName(), 8, 8);
        List<Future<XcDeleteMagnitudeDistDTO>> futures = new ArrayList<>();
        try {
            // 异步提交任务
            for (TimeRange timeRange : timeRanges) {
                futures.add(threadPool.submit(() ->
                        magnitudeDistCal(timeRange, dto.getBatchNumberList(), conditionJson)
                ));
            }
            List<XcDeleteMagnitudeDistDTO> results = new ArrayList<>(futures.size());
            // 设置整体超时
            long globalStart = System.currentTimeMillis();
            long globalTimeout = TimeUnit.MINUTES.toMillis(2); // 总超时时间2分钟
            for (Future<XcDeleteMagnitudeDistDTO> future : futures) {
                long timeLeft = globalTimeout - (System.currentTimeMillis() - globalStart);
                if (timeLeft <= 0) {
                    log.error("规则中心-周期数据剔除量级查询整体处理超时");
                    return new Result<List<XcDeleteMagnitudeDistDTO>>()
                            .setCode(ResultCode.FAIL.getValue())
                            .setMessage("服务异常");
                }
                try {
                    // 单任务超时限制
                    results.add(future.get(timeLeft, TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    // 只要有一个异常，取消所有任务
                    futures.forEach(fu -> fu.cancel(true));
                    log.error("规则中心-周期数据剔除量级查询异常，{}", e.getMessage(), e);
                    return new Result<List<XcDeleteMagnitudeDistDTO>>()
                            .setCode(ResultCode.FAIL.getValue())
                            .setMessage("服务异常");
                }
            }
            // 所有成功
            return new Result<List<XcDeleteMagnitudeDistDTO>>()
                    .setCode(ResultCode.SUCCESS.getValue())
                    .setDate(results);
        } catch (Exception e) {
            log.error("规则中心-周期数据剔除量级查询异常，{}", e.getMessage(), e);
            return new Result<List<XcDeleteMagnitudeDistDTO>>()
                    .setCode(ResultCode.FAIL.getValue())
                    .setMessage("服务异常");
        } finally {
            threadPool.shutdownAndAwaitTermination();
        }
    }

    /**
     * 计算量级核心方法
     *
     * @param timeRange
     * @param batchNumberList
     * @param conditionJson
     * @return
     */
    private XcDeleteMagnitudeDistDTO magnitudeDistCal(TimeRange timeRange, List<String> batchNumberList, JSONObject conditionJson) {
        //1.撞得量级阈值
        int totalThreshold = variableAllocationService.getVariableAllocation().getNormalQuantity();
        //2.T日timeRange范围外量级
        int timeRangeOutMagnitude = 0;
        if (LocalDate.now().equals(timeRange.getBegin().toLocalDate())) {
            timeRangeOutMagnitude = xieChengCollidingDataLoopCycleMapper
                    .selectTimeRangeOutMagnitudeForTodaytiflash_(timeRange.getBegin(), timeRange.getEnd());
        } else {
            timeRangeOutMagnitude = xieChengCollidingDataLoopCycleMapper
                    .selectTimeRangeOutMagnitudeForNotTodaytiflash_(timeRange.getBegin(), timeRange.getEnd());
        }
        //3.timeRange范围内，与跑分数据的交集量级
        String scoreSqlBlanked = scoreSql(batchNumberList);
        int timeRangeBetweenMagnitude = xieChengCollidingDataLoopCycleMapper
                .selectTimeRangeBetweenWithScoreMagnitudetiflash_(timeRange.getBegin(), timeRange.getEnd(), scoreSqlBlanked);
        //4.timeRange范围内，与符合条件的跑分数据的交集量级
        String scoreSql = scoreSql(conditionJson, batchNumberList);
        int remainingNum = xieChengCollidingDataLoopCycleMapper
                .selectTimeRangeBetweenWithScoreMagnitudetiflash_(timeRange.getBegin(), timeRange.getEnd(), scoreSql);
        //5.timeRange范围内的剔除量级
        int deleteNum = timeRangeBetweenMagnitude - remainingNum;
        //6.空挡量级
        int freeNum = totalThreshold - timeRangeOutMagnitude - remainingNum;
        return new XcDeleteMagnitudeDistDTO(timeRange.getBegin(), timeRange.getEnd(), deleteNum, remainingNum, freeNum);
    }

    private void cycleDataDeleteQueryOpt(JSONObject jsonObject, List<String> batchNumberList, String cleanTime, List<String> querySqls) {
        for (String batchNumber : batchNumberList) {
            if (StringUtils.isEmpty(batchNumber)) {
                continue;
            }
            querySqls.add(cycleDataDeleteQueryForOpt(jsonObject, batchNumber, cleanTime));
        }
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

    @Override
    public Result<Boolean> consumerPushCustomer(Long id) {
        long initTime = System.currentTimeMillis();
        CustomerInfoPushMain customerInfoPushMain = customerInfoPushMainMapper.selectByPrimaryKey(id);
        CustomerInfoPushBatchExample searchPushBatch = new CustomerInfoPushBatchExample();
        searchPushBatch.createCriteria().andMIdEqualTo(customerInfoPushMain.getId());
        List<CustomerInfoPushBatch> customerInfoPushBatches = customerInfoPushBatchMapper.selectByExample(searchPushBatch);
        // 补推逻辑
        if (PushRuleStatusEnum.EXCEPTIONS_RUNNING.getValue()
                .equals(customerInfoPushMain.getmStatus())) {
            // 推决策重试
            toPolicyByRuleService.makeUpPolicyData(customerInfoPushMain,
                    MockSwitchEnum.GENERAL.getValue());
            // 是否存在ES重试数据
            int i = retryEsData(customerInfoPushMain);
            if (i == 0) {
                Integer status = toPolicyByRuleService.queryExistError(customerInfoPushMain.getId(),
                        FilterTypeEnum.GENERAL_POLICY.getValue());
                CustomerInfoPushMain main = new CustomerInfoPushMain();
                main.setId(customerInfoPushMain.getId());
                main.setmStatus(status);
                customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
            }
        }

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
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), e.getMessage()), e);
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
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                    String.format("该推送不符合推送决策的限制条件 流水号：%s,原因：%s", id.toString(), integerResult.getMessage())));
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
        Integer _3kEncrypt = integerResult.getData();
        HashMap<Long, TaskExtendInfoVO> hsTaskExtend = new HashMap<>();
        List<TaskExtendInfoVO> extendInfosByFileIds = straHisFileMapper.getExtendInfosByFileIds(fileIds);
        extendInfosByFileIds.forEach(t -> {
            hsTaskExtend.put(t.getFileId(), t);
        });
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
        CustomerInfoPushMain main = new CustomerInfoPushMain();
        main.setmStatus(PushRuleStatusEnum.TO_BE_CONFIRMED.getValue());
        ThreadPoolExecutor actionEs = BrExecutors.getThreadPool(getEsNum, getEsNum, 50);
        ThreadPoolExecutor pushJc = BrExecutors.getThreadPool(getJcNum, getJcNum, 50);
        List<Future<List<Future<Result<Integer>>>>> res = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        HashMap<Integer, Integer> partDataNum = new HashMap<>();

        if (!isSigle) {
            Integer nowSum = 0;
            List<Future<Result<Integer>>> resList = new ArrayList<>();
            Integer toPolicyThreadNum = marketingCommonConfig.getToPolicyThreadNum().get("toPolicyThreadNum");
            Integer toPolicyQueueNum = marketingCommonConfig.getToPolicyThreadNum().get("toPolicyQueueNum");
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(toPolicyThreadNum, toPolicyThreadNum, toPolicyQueueNum);

            for (Integer i = 0; i < parNum; i++) {
                QueryBaseBean queryBaseBean = createQueryBaseBean(customerInfoPushMain, numList, fileIds, i, straHisFiles);
                Integer nowNum = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
                partDataNum.put(i, nowNum);
                if (customerInfoPushMain.getTagContent() != null) {
                    resList.add(threadPool.submit(() -> queryTotal(customerInfoPushMain, numList, queryBaseBean)));
                } else {
                    nowSum += nowNum;
                }
            }
            try {
                for (Future<Result<Integer>> pushFuture : resList) {
                    Result<Integer> pushRes = pushFuture.get();
                    if (ResultCode.SUCCESS.getValue().equals(pushRes.getCode())) {
                        nowSum += pushRes.getData();
                    } else {
                        return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setMessage("推送决策分片返回量级异常");
                    }
                }
            } catch (Exception ex) {
                log.error("推送决策 获取线程结果异常" + ex.getMessage(), ex);
            }
            // 关闭线程池
            threadPool.shutdown();
            try {
                while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                    log.info("推送决策查询量级：线程池关闭");
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
                log.error("推送决策查询量级：日志保存线程池结束异常！", ex);
                Thread.currentThread().interrupt();
            }
            if (!customerInfoPushMain.getmRealyNum().equals(nowSum)) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                        "任务id：" + customerInfoPushMain.getId() + "，分组查询和预览总数不一致，请手动处理！，分组查询的总数：" + nowSum.toString()
                                + "，预览总数：" + customerInfoPushMain.getmRealyNum().toString()));
                return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
            }
        }
        log.warn("推送决策查询量级核对完成，任务id：{}", customerInfoPushMain.getId());
        Boolean markWithEsFlag = marketingCommonConfig.getPushPolicyMarkWithEsFlag();
        String scoreCondition = customerInfoPushMain.getmScoreCondition();
        Object lableObject = null;
        if (StringUtils.isNotEmpty(scoreCondition)) {
            if (markWithEsFlag) {
                lableObject = GeneScriptUtil.esLableScript(scoreCondition);
            } else {
                lableObject = GeneScriptUtil.getScoreLables(scoreCondition, markWithEsFlag);
            }
        }
        for (Integer i = 0; i < parNum; i++) {
            res.add(actionEs.submit(new actionEs(pushJc, customerInfoPushMain
                    , fileIds, numList, i.toString(), _3kEncrypt, isSigle, partDataNum.get(i), markWithEsFlag, lableObject, straHisFiles)));
        }
        log.warn("推送决策 任务id：{}；获取所有分组数据耗时：{}", customerInfoPushMain.getId(), System.currentTimeMillis() - startTime);

        try {
            int retryCount = 0;
            int failCount = 0;
            try {
                for (Future<List<Future<Result<Integer>>>> actionFuture : res) {
                    List<Future<Result<Integer>>> futures = actionFuture.get();
                    for (Future<Result<Integer>> pushFuture : futures) {
                        Result<Integer> pushRes = pushFuture.get();
                        if (ResultCode.TIME_OUT.getValue().equals(pushRes.getCode())
                                || ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(pushRes.getCode())) {
                            retryCount++;
                        } else if (ResultCode.FAIL.getValue().equals(pushRes.getCode())) {
                            failCount++;
                        }
                    }
                }
                if (failCount > 0) {
                    main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
                } else {
                    Integer status = toPolicyByRuleService.queryExistError(customerInfoPushMain.getId(),
                            FilterTypeEnum.GENERAL_POLICY.getValue());
                    main.setmStatus(status);
                }
            } catch (Exception ex) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), "推送决策 获取线程结果异常!"), ex);
                main.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
            }
            log.warn(TITLE + "规则中心推送决策结果：retryCount：" + retryCount + ",failCount:" + failCount);
            if (failCount > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("推送决策失败：\n");
                sb.append("apiCode：" + customerInfoPushMain.getmApiCode());
                sb.append("，任务id：" + customerInfoPushMain.getId());
                sendAlert("推送决策失败", sb.toString());
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), "推送决策 获取线程结果异常!"), ex);
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
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), ex.getMessage()), ex);
        }
        log.warn("推送决策 任务id：{}；查询推送耗时：{}；整体耗时：{}；计划数量：{}"
                , customerInfoPushMain.getId()
                , System.currentTimeMillis() - startTime
                , System.currentTimeMillis() - initTime
                , customerInfoPushMain.getmRealyNum());
        main.setId(customerInfoPushMain.getId());
        customerInfoPushMainMapper.updateByPrimaryKeySelective(main);
        //endregion
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    public Result<Integer> queryTotal(CustomerInfoPushMain customerInfoPushMain, List<String> numList, QueryBaseBean queryBaseBean) {
        try {
            // 解析标签规则
            JSONObject jsonObject = JSON.parseObject(customerInfoPushMain.getTagContent());
            String tagCode = jsonObject.getString("tagCode");
            int type = jsonObject.getIntValue("type");
            if (!tagHandleService.tagIsEnabled(customerInfoPushMain.getmApiCode(), tagCode)) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                        "该apiCode：" + customerInfoPushMain.getmApiCode() + ",该tag：" + tagCode + "已失效"));

                CustomerInfoPushMain customer = new CustomerInfoPushMain();
                customer.setId(customerInfoPushMain.getId());
                customer.setmStatus(PushRuleStatusEnum.PUSH_FAIL.getValue());
                customerInfoPushMainMapper.updateByPrimaryKeySelective(customer);
                return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setMessage("该apiCode：" + customerInfoPushMain.getmApiCode() + ",该tag：" + tagCode + "已失效");
            }

            // 页面规则查询es
            ESQueryRequest esQueryRequest = marketingHistoryEsService.builderDslConditionOfQueryBaseBean(queryBaseBean);
            String queryDsl = esQueryRequest.getQueryDsl();
            List<String> indexNames = esQueryRequest.getIndexName();
            if (CollectionUtils.isEmpty(indexNames)) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("查询es索引为空，batchNumberList：" + numList);
            }

            // 构建联邦查询 SQL
            String querySql = buildFederatedQuerySql(indexNames, queryDsl, tagCode, type);
            if (StringUtils.isEmpty(querySql)) {
                return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("查询有误，请联系开发人员");
            }
            log.warn("标签查询sql：" + querySql);
            Integer total = tagDataDetailMapper.queryPreviewTotalbI_(querySql);
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(total);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                    "任务id：" + customerInfoPushMain.getId() + "，分组查询和预览总数异常！"), e);
            return new Result<Boolean>().setCode(ResultCode.FAIL.getValue()).setMessage("任务id：" + customerInfoPushMain.getId() + "，分组查询和预览总数异常！");
        }
    }

    /**
     * 创建 QueryBaseBean
     */
    private QueryBaseBean createQueryBaseBean(CustomerInfoPushMain customerInfoPushMain, List<String> numList,
                                              List<Long> fileIds, Integer part, List<StraHisFile> straHisFiles) {
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(customerInfoPushMain.getmApiCode());
        queryBaseBean.setBatchNumbers(Joiner.on(",").join(numList));
        queryBaseBean.setFileIds(Joiner.on(",").join(fileIds));
        queryBaseBean.setJsonData(customerInfoPushMain.getmRuleCondition());
        queryBaseBean.setPart(part.toString());
        queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(straHisFiles, marketingCommonConfig));
        return queryBaseBean;
    }

    private int retryEsData(CustomerInfoPushMain customerInfoPushMain) {
        // 查询ES重试数据
        ErrorMarkExample errorMarkExample = new ErrorMarkExample();
        errorMarkExample.createCriteria().andMIdEqualTo(customerInfoPushMain.getId())
                .andRetryStatusEqualTo(RetryStatusEnum.AWAIT_COMPLETE.getValue())
                .andTypeEqualTo(ErrorMarkTypeEnum.ES_ERROR.getValue())
                .andRetryTotalAttemptsLessThan(3);

        return errorMarkMapper.countByExample(errorMarkExample);
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

        private Boolean markWithEsFlag;

        private Object lableObject;

        private List<StraHisFile> straHisFiles;

        public actionEs(ThreadPoolExecutor pushJcPool
                , CustomerInfoPushMain customerInfoPushMain
                , List<Long> fileIds, List<String> numList
                , String part, Integer _3kEncrypt, Boolean isPerOrTop, Integer partDataNum, Boolean markWithEsFlag,
                Object lableObject, List<StraHisFile> straHisFiles) {
            this.pushJcPool = pushJcPool;
            this.customerInfoPushMain = customerInfoPushMain;
            this.fileIds = fileIds;
            this.numList = numList;
            this.part = part;
            this._3kEncrypt = _3kEncrypt;
            this.isPerOrTop = isPerOrTop;
            this.partDataNum = partDataNum;
            this.markWithEsFlag = markWithEsFlag;
            this.lableObject = lableObject;
            this.straHisFiles = straHisFiles;
        }

        @Override
        public List<Future<Result<Integer>>> call() {
            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(customerInfoPushMain.getmApiCode());
            queryBaseBean.setBatchNumbers(Joiner.on(",").join(numList));
            queryBaseBean.setFileIds(Joiner.on(",").join(fileIds));
            queryBaseBean.setJsonData(customerInfoPushMain.getmRuleCondition());
            queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(straHisFiles, marketingCommonConfig));
            boolean scFlag = !ObjectUtils.isEmpty(lableObject);
            List<ScoreLable> scoreLables = null;
            if (scFlag) {
                if (markWithEsFlag) {
                    //赋值es脚本
                    queryBaseBean.setScriptFields(lableObject.toString());
                } else {
                    scoreLables = (List<ScoreLable>) lableObject;
                }
            }
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

            ErrorMark errorMark = new ErrorMark();
            int i1 = 1;
            // 判断该任务是否为异常待补推任务
            if (PushRuleStatusEnum.EXCEPTIONS_RUNNING.getValue()
                    .equals(customerInfoPushMain.getmStatus())) {

                // 查询待补推数据
                ErrorMarkExample errorMarkExample = new ErrorMarkExample();
                errorMarkExample.createCriteria().andMIdEqualTo(customerInfoPushMain.getId())
                        .andPartEqualTo(part)
                        .andRetryStatusEqualTo(RetryStatusEnum.AWAIT_COMPLETE.getValue());
                List<ErrorMark> errorMarks = errorMarkMapper.selectByExample(errorMarkExample);
                // 查询当前part下的异常数据
                if (!CollectionUtils.isEmpty(errorMarks)) {
                    errorMark = errorMarks.get(0);
                    i1 = errorMark.getPageSize();
                    searchAfterStr = errorMark.getSearchAfter();
                }
            }

            for (int i = i1; i <= totalPage; i++) {
                try {
                    String sn = String.valueOf(i);
                    if (i == totalPage && totalYuShu > 0) {
                        queryBaseBean.setPageSize(totalYuShu);
                    } else {
                        queryBaseBean.setPageSize(pageSize);
                    }
                    queryBaseBean.setSearchAfter(searchAfterStr);

                    List<MarketingHistory> marketingHistories;

                    // 模拟es异常
                    boolean mockEsError = toPolicyByRuleService.mockSwitch(customerInfoPushMain.getmApiCode(),
                            MockSwitchEnum.GENERAL.getValue(), MockSwitchEnum.ESRETRY.getValue());
                    try {
                        if (mockEsError) {
                            throw new Exception("模拟ES异常场景");
                        }
                        marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);

                        if (marketingHistories == null) {
                            throw new Exception();
                        }

                        // 获取最后一条记录的searchAfter值
                        if (!marketingHistories.isEmpty()) {
                            searchAfterStr = marketingHistories.get(marketingHistories.size() - 1).getSearchAfter();
                        }

                        if (customerInfoPushMain.getTagContent() != null && !marketingHistories.isEmpty()) {
                            // 解析标签规则
                            JSONObject jsonObject = JSON.parseObject(customerInfoPushMain.getTagContent());
                            String tagCode = jsonObject.getString("tagCode");
                            int type = jsonObject.getIntValue("type");

                            if (!tagHandleService.tagIsEnabled(customerInfoPushMain.getmApiCode(), tagCode)) {
                                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                                        "该apiCode：" + customerInfoPushMain.getmApiCode() + "，该tag：" + tagCode + "已失效"));
                                Result<Integer> result = new Result<>();
                                result.setCode(ResultCode.FAIL.getValue());
                                Callable<Result<Integer>> resultCallable = (Callable) () -> result;
                                resList.add(pushJcPool.submit(resultCallable));
                                return resList;
                            }

                            // 查询es 提取跑分文件中cells
                            List<String> esCells = marketingHistories.stream()
                                    .map(MarketingHistory::getCell_log)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());

                            // 获取 TiDB 中存在的 cells
                            List<String> tidbCells = tagDataDetailMapper.queryCells(esCells, tagCode, LocalDate.now().toString());

                            if (type == 0) {
                                // 交集：跑分文件 与 标签数据 都存在
                                marketingHistories = marketingHistories.stream()
                                        .filter(history -> tidbCells.contains(history.getCell_log()))
                                        .collect(Collectors.toList());
                            } else {
                                // 剔除：去掉标签存在跑分文件中cell
                                marketingHistories = marketingHistories.stream()
                                        .filter(history -> !tidbCells.contains(history.getCell_log()))
                                        .collect(Collectors.toList());
                            }
                        }
                    } catch (Exception e) {
                        if (errorMark.getId() != null) {
                            // 已存在补推记录
                            if (errorMark.getRetryTotalAttempts() < 3) {
                                updateErrorMark(errorMark, errorMark.getRetryTotalAttempts() + 1);
                            }
                        } else {
                            insertNewErrorMark(customerInfoPushMain, part, i, searchAfterStr, JSONObject.toJSONString(queryBaseBean));
                        }
                        return resList;
                    }

                    if (errorMark.getId() != null) {
                        ErrorMark errorMark1 = new ErrorMark();
                        errorMark1.setId(errorMark.getId());
                        errorMark1.setRetryStatus(RetryStatusEnum.PUSH_COMPLETE.getValue());
                        errorMarkMapper.updateByPrimaryKeySelective(errorMark1);
                    }

                    Integer realNum = marketingHistories.size();
                    log.warn("任务id：{}，当前片：{}，获取的数量：{}，当前页码：{}"
                            , customerInfoPushMain.getId()
                            , StringUtils.isBlank(part) ? "" : part
                            , realNum
                            , i);
                    if (realNum == 0) {
                        continue;
                    }
                    List<PushMarketingUserDetailDTO> userDetailDTOS = new ArrayList<>();
                    for (int k = 0; k < marketingHistories.size(); k++) {
                        MarketingHistory marketingHistory = marketingHistories.get(k);
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
                        if (scFlag) {
                            //es处理
                            if (markWithEsFlag) {
                                markForCell(varObject, marketingHistory.getFields());
                                //代码处理逻辑
                            } else {
                                List<MarketingCondition> conditions = marketingHistory.getCondition();
                                if (!CollectionUtils.isEmpty(conditions)) {
                                    Map<String, Object> scoreMap = conditions.stream()
                                            .filter(condition -> condition.getDValue() != null)
                                            .collect(Collectors.toMap(MarketingCondition::getFieldKey
                                                    , MarketingCondition::getDValue
                                                    , (existing, replacement) -> replacement));
                                    ScoreLable scoreLable = GeneScriptUtil.scoreLableWithSpel(scoreMap, scoreLables);
                                    if (scoreLable != null) {
                                        varObject.put("listValue", scoreLable.getListValue());
                                        varObject.put("valueType", scoreLable.getValueType());
                                    }
                                }
                            }
                        }
                        dto1.setVariables(varObject);
                        if (StringUtils.isNotBlank(customerInfoPushMain.getStrategyCode())) {
                            dto1.setStrategyCode(customerInfoPushMain.getStrategyCode());
                        }
                        userDetailDTOS.add(dto1);
                    }
                    //推送任务基础信息
                    List<List<PushMarketingUserDetailDTO>> partition =
                            toPolicyByRuleService.splitParam(customerInfoPushMain.getmApiCode(), userDetailDTOS);
                    Integer batch = 0;
                    for (List<PushMarketingUserDetailDTO> userDetailDTOList : partition) {
                        PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
                        pushMarketingUserTaskInfoDTO.setMethod("caseAdd");
                        pushMarketingUserTaskInfoDTO.setBatchNumber(customerInfoPushMain.getId().toString());
                        pushMarketingUserTaskInfoDTO.setAccessNumber(customerInfoPushMain.getId() + "_" + (StringUtils.isBlank(part) ? "0" : part) + "_" + sn + "_" + batch);
                        pushMarketingUserTaskInfoDTO.setData(userDetailDTOList);
                        pushMarketingUserTaskInfoDTO.setTaskId(customerInfoPushMain.getId().toString());
                        pushMarketingUserTaskInfoDTO.setBatchName(customerInfoPushMain.getBatchName());
                        if (StringUtils.isNotBlank(customerInfoPushMain.getStrategyCode())) {
                            pushMarketingUserTaskInfoDTO.setStrategyCode(customerInfoPushMain.getStrategyCode());
                        }
                        //传输参数信息
                        PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
                        pushMarketingUserDTO.setApiCode(customerInfoPushMain.getmApiCode());
                        pushMarketingUserDTO.setPlatApiCode(customerInfoPushMain.getmApiCode());
                        pushMarketingUserDTO.setJsonData(pushMarketingUserTaskInfoDTO);
                        resList.add(pushJcPool.submit(new PushJcAction(pushMarketingUserDTO
                                , pushMarketingUserTaskInfoDTO.getAccessNumber()
                                , customerInfoPushMain.getId()
                                , userDetailDTOList.size(), null)));
                        batch++;
                    }
                } catch (Exception ex) {
                    String error = String.format("任务id：%s，当前片：%s，当前页码：%d，异常："
                            , customerInfoPushMain.getId().toString()
                            , StringUtils.isBlank(part) ? "" : part
                            , i);
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), error), ex);
                    Result<Integer> result = new Result<>();
                    result.setCode(ResultCode.FAIL.getValue());
                    Callable<Result<Integer>> resultCallable = (Callable) () -> result;
                    resList.add(pushJcPool.submit(resultCallable));
                }
            }
            return resList;
        }
    }

    private void insertNewErrorMark(CustomerInfoPushMain customerInfoPushMain, String part, int pageSize, String searchAfterStr, String esCondition) {
        // 新增异常待补推数据
        ErrorMark errorMark = new ErrorMark();
        errorMark.setApiCode(customerInfoPushMain.getmApiCode());
        errorMark.setmId(customerInfoPushMain.getId());
        errorMark.setPart(part);
        errorMark.setPageSize(pageSize);
        errorMark.setSearchAfter(searchAfterStr);
        errorMark.setEsCondition(esCondition);
        errorMark.setRetryStatus(RetryStatusEnum.AWAIT_COMPLETE.getValue());
        errorMark.setAppletDate(LocalDate.now().toString());
        errorMark.setCreateTime(new Date());
        errorMark.setUpdateTime(new Date());
        errorMarkMapper.insertSelective(errorMark);
    }

    private void markForCell(JSONObject varObject, JSONObject fields) {
        if (fields == null) {
            return;
        }
        JSONObject listValueJson = fields.getJSONObject("listValue");
        JSONObject valueTypeJson = fields.getJSONObject("valueType");
        if (listValueJson != null) {
            varObject.put("listValue", listValueJson.getString("value"));
        }
        if (valueTypeJson != null) {
            varObject.put("valueType", valueTypeJson.getString("value"));
        }
    }

    class PushJcAction implements Callable<Result<Integer>> {

        private PushMarketingUserDTO pushMarketingUserDTO;

        private String accessNumber;

        private Long mainId;

        private Integer size;

        private ErrorMark errorMark;

        public PushJcAction(PushMarketingUserDTO pushMarketingUserDTO, String accessNumber, Long mainId, Integer size, ErrorMark errorMark) {
            this.pushMarketingUserDTO = pushMarketingUserDTO;
            this.accessNumber = accessNumber;
            this.mainId = mainId;
            this.size = size;
            this.errorMark = errorMark;
        }

        @Override
        public Result<Integer> call() {
            Result<Integer> result = new Result<>();
            // 模拟推决策异常
            boolean b = toPolicyByRuleService.mockSwitch(pushMarketingUserDTO.getApiCode(),
                    MockSwitchEnum.GENERAL.getValue(), MockSwitchEnum.POLICYRETRY.getValue());
            if (b) {
                result.setCode(ResultCode.TIME_OUT.getValue());
            } else {
                result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, mainId,
                        accessNumber, size);
                if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())
                        || ResultCode.TIME_OUT.getValue().equals(result.getCode())) {
                    result = intelligentCustomerServiceClient.pushRuleCenterToPolicy(pushMarketingUserDTO, mainId,
                            accessNumber, size);
                }
            }

            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode()
                        , "推送决策重试失败 accessNumber:" + accessNumber + " - " + JSON.toJSONString(result)));
            }

            if (ResultCode.TIME_OUT.getValue().equals(result.getCode()) ||
                    ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                insertErrorMark(pushMarketingUserDTO, mainId, accessNumber, size);
            }
            result.setDate(size);
            return result;
        }
    }

    // 更新错误标记
    private void updateErrorMark(ErrorMark errorMark, int retryAttempts) {
        ErrorMark errorMark1 = new ErrorMark();
        errorMark1.setId(errorMark.getId());
        errorMark1.setRetryTotalAttempts(retryAttempts);
        errorMark1.setUpdateTime(new Date());
        errorMarkMapper.updateByPrimaryKeySelective(errorMark1);
    }

    // 插入错误标记
    private void insertErrorMark(PushMarketingUserDTO pushMarketingUserDTO, Long mainId, String accessNumber, int size) {
        ErrorMark errorMark = new ErrorMark();
        errorMark.setApiCode(pushMarketingUserDTO.getApiCode());
        errorMark.setmId(mainId);
        errorMark.setAccessNumber(accessNumber);
        errorMark.setPushSize(size);
        errorMark.setPolicyCondition(JSONObject.toJSONString(pushMarketingUserDTO));
        errorMark.setRetryStatus(RetryStatusEnum.AWAIT_COMPLETE.getValue());
        errorMark.setType(ErrorMarkTypeEnum.POLICY_ERROR.getValue());
        errorMark.setAppletDate(LocalDate.now().toString());
        errorMark.setCreateTime(new Date());
        errorMark.setUpdateTime(new Date());
        errorMarkMapper.insertSelective(errorMark);
    }


    @Override
    public Result<Boolean> getCustomerStatus(CustomerInfoPushMain customerInfoPushMain) {
        Long mId = customerInfoPushMain.getId();
        Boolean isContinue = Boolean.FALSE;

        ArrayList<String> realStatus = new ArrayList<>();
        // 数据库字段默认值
        realStatus.add("1");
        // 900013-数据正在导入
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
                } else if ("900006".equals(userStatus.getData())) {
                    if (StringUtils.isNotBlank(userStatus.getMessage())) {
                        updateLog.setErrorContent(userStatus.getMessage());
                        JSONObject error = JSONObject.parseObject(userStatus.getMessage());
                        if (error != null && error.keySet() != null) {
                            updateLog.setFailNum(error.keySet().size());
                        }
                    }
                    log.warn("推送决策后，查询决策结果出错，原始参数:{}--查询参数:{}", JSON.toJSONString(t), pushMarketingUserDTO);
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
            log.warn("推送决策状态确认，count：{}", count);
            CustomerInfoPushMain updateMain = new CustomerInfoPushMain();
            updateMain.setId(mId);
            updateMain.setmStatus(count > 0 ? PushRuleStatusEnum.CONFIRMED_FAIL.getValue() : PushRuleStatusEnum.CONFIRMED_SUCCESS.getValue());
            customerInfoPushMainMapper.updateByPrimaryKeySelective(updateMain);
            // 推决策报警
            if (count > 0) {
                List<String> pushAlarmApiCode = marketingCommonConfig.getPushAlarmApiCode();
                if (pushAlarmApiCode.contains(customerInfoPushMain.getmApiCode())) {
                    pushDecisionsAlarm(customerInfoPushMain);
                }
            }
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue);
    }

    public void pushDecisionsAlarm(CustomerInfoPushMain customerInfoPushMain) {
        List<String> ids = new ArrayList<>();
        Long mId = customerInfoPushMain.getId();
        ids.add(String.valueOf(mId));
        // 失败原因
        Result<List<PolicyResultByTaskIdsDTO>> result = intelligentCustomerServiceClient.getTaskIdsResult(customerInfoPushMain.getmApiCode(), ids);
        log.warn("决策查询接口返回值 result={}", JSON.toJSONString(result));
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            List<PolicyResultByTaskIdsDTO> resultByTaskIdsDTOS = result.getData();
            if (!resultByTaskIdsDTOS.isEmpty()) {
                PolicyResultByTaskIdsDTO policyResultByTaskIdsDTO = resultByTaskIdsDTOS.get(0);
                String verification = policyResultByTaskIdsDTO.getVerification();
                String verificationReason = policyResultByTaskIdsDTO.getVerificationReason();
                StringBuilder sb = new StringBuilder();
                sb.append("apiCode：").append(customerInfoPushMain.getmApiCode()).append("，");
                sb.append("【推送完成，请求批次号】：" + verification).append("，【推送结果】：" + verificationReason);
                sendAlert("【营销自动化推决策确认失败】", sb.toString());
            }
        } else {
            log.warn("决策查询接口异常result={}", JSON.toJSONString(result));
        }
    }

    public void sendAlert(String title, String text) {
        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.ZHIJIA_CLUEFEEDBACK_MSG.toString());
        DingDingMarkdownMessage.Markdown markdown = new DingDingMarkdownMessage.Markdown();
        markdown.setTitle(title);
        markdown.setText(text);
        DingDingMarkdownMessage dingDingMarkdownMessage = new DingDingMarkdownMessage();
        dingDingMarkdownMessage.setMarkdown(markdown);
        dingDingRobotHookService.sendMessageGroup(map.get("token").toString(), map.get("secret").toString(), dingDingMarkdownMessage, true);
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
        Integer dataSourceType = dto.getJsonData().getDataSourceType();
        if (Objects.isNull(dataSourceType)) {
            dataSourceType = 0;
        }
        MarketingPreUserDTO preUserDTO = dto.getJsonData();
        List<MarketingPreUserDetailDTO> dataItems = preUserDTO.getDataItems();
        //batchAddUniqueId(dataItems, MarketingPreUserDetailDTO::setFingerprint, MarketingPreUserDetailDTO::getFingerprint);
        jsonData = addUniqueId(jsonData);
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
            syncInfo.setDataSourceType(dataSourceType);
            mockDbOrRedisError(1, apiCode);
            marketingUserMapper.insertMarketingPreUserByText(syncInfo);

            // 模拟数据入库成功，但返回异常入Pulsar的场景
            Map<String, Boolean> pushDataSwitch = marketingCommonConfig.getPushDataSwitch();
            if (pushDataSwitch.get(PushDataEnum.MARKETING_UPLOAD_BASE.getValue())) {
                log.warn(String.format("【模拟异常写入Pulsar】通用上传数据infoId infoId:%s", syncInfo.getId()));
                throw new Exception();
            }

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
                log.warn(String.format("通用写入Pulsar 主题:%s 数据:%s", PulsarTopic.upLoadTopic, jsonString));
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
            boolean intoAiQueue = routeToAiQueue(apiCode, syncInfoId, jsonData);
            // 非ai客户
            if (Objects.equals(intoAiQueue, false)) {
                if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingUploadConstants.TAG_MARKETING_PRE_USER_RECEIVE)) {
                    sendToRocketMqByConfig(apiCode, MarketingUploadConstants.TOPIC
                            , MarketingUploadConstants.TAG_MARKETING_PRE_USER_RECEIVE, syncInfoId, CustomerQueueEnum.ORG_SYNC);
                } else {
                    sendToMqByConfig(apiCode, MQConstants.ROUTING_KEY_MARKETING_PRE_USER_RECEIVE, syncInfoId, CustomerQueueEnum.ORG_SYNC);
                }
            }
            sendJsonParseMq(apiCode, syncInfoId, dataSourceType);
        }
        try {
            // 埋点
            log.warn("开始埋点调用 - apiCode: {}, event: marketing_pre_user_receive, syncInfoId: {}", apiCode, syncInfoId);
            JSONObject condition = new JSONObject();
            condition.put("request_batch", dto.getJsonData().getRequestId());
            trackingService.trackBusinessLog(DataFlowDirection.IN
                    , apiCode
                    , "通用上传接口"
                    , "b_marketing_sync_info"
                    , JSON.toJSONString(condition)
                    , Long.valueOf(size)
                    , TrackingContext.generateBatchId());
            log.warn("埋点调用成功 - apiCode: {}, event: marketing_pre_user_receive, syncInfoId: {}", apiCode, syncInfoId);
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }

    /**
     * 批量添加唯一ID
     *
     * @param jsonData
     */
    private String addUniqueId(String jsonData) {
        try {
            JSONObject originalJson = JSONObject.parseObject(jsonData);
            Object dataItemsObj = originalJson.get("dataItems");
            if (Objects.nonNull(dataItemsObj)) {
                if (dataItemsObj instanceof JSONArray) {
                    JSONArray dataItems = (JSONArray) dataItemsObj;
                    // 处理数组情况
                    int size = dataItems.size();
                    List<Long> ids;
                    try {
                        ids = snowflakeRedisGeneratorHandle.nextIds(size);
                    } catch (Exception e) {
                        log.error("雪花算法生成唯一ID异常,唯一ID添加失败,{}", e.getMessage(), e);
                        return jsonData;
                    }
                    for (int i = 0; i < size; i++) {
                        JSONObject itemObject = dataItems.getJSONObject(i);
                        itemObject.put("fingerprint", ids.get(i));
                    }
                    return originalJson.toJSONString();
                } else if (dataItemsObj instanceof JSONObject) {
                    JSONObject dataItem = (JSONObject) dataItemsObj;
                    Long uinqueId;
                    try {
                        uinqueId = snowflakeRedisGeneratorHandle.nextId();
                    } catch (Exception e) {
                        log.error("雪花算法生成唯一ID异常,唯一ID添加失败,{}", e.getMessage(), e);
                        return jsonData;
                    }
                    dataItem.put("fingerprint", uinqueId);
                    return originalJson.toJSONString();
                } else {
                    // 处理其他类型或null
                    log.warn("dataItems 字段类型异常或为空");
                }
            }
        } catch (Exception e) {
            log.error("添加唯一ID异常{}", e.getMessage(), e);
        }
        return jsonData;
    }


    /**
     * 批量添加唯一ID
     *
     * @param list        数据列表
     * @param setConsumer 赋值函数
     * @param getFunction 获取ID函数，如果获取ID为空，则添加ID,可为 null
     */
    public <T> void batchAddUniqueId(List<T> list, BiConsumer<T, Long> setConsumer, Function<T, Long> getFunction) {
        int size = list.size();
        List<Long> ids;
        try {
            ids = snowflakeRedisGeneratorHandle.nextIds(size);
        } catch (Exception e) {
            log.error("批量添加唯一ID异常,唯一ID添加失败,{}", e.getMessage(), e);
            return;
        }
        if (getFunction == null) {
            for (int i = 0; i < size; i++) {
                setConsumer.accept(list.get(i), ids.get(i));
            }
            return;
        }
        for (int i = 0; i < size; i++) {
            if (getFunction.apply(list.get(i)) == null) {
                setConsumer.accept(list.get(i), ids.get(i));
            }
        }
    }

    /**
     * 发送json解析MQ
     *
     * @param apiCode
     * @param syncInfoId
     */
    public void sendJsonParseMq(String apiCode, String syncInfoId, Integer dataSourceType) {
        //发送Json解析消息,定制清洗不在发送MQ
        if (1 == dataSourceType) {
            return;
        }
        try {
            //使用caffeineCache存储 mq发送标识
            String cacheKey = CaffeineCacheKeyConstant.JSON_PARSE.concat(apiCode)
                    .concat(":").concat(DataProcessEnum.SystemTypeEnum.MARKETING.getCode().toString())
                    .concat(":").concat(DataProcessEnum.DataTypeEnum.UPLOAD.getCode().toString())
                    .concat(":").concat(DataProcessEnum.AcceptTypeEnum.GENERAL.getCode().toString());
            boolean exists = caffeineCache.hasIdentifier(cacheKey);
            if (exists) {
                return;
            }
            MqDataJsonParse mqDataJsonParse = new MqDataJsonParse();
            mqDataJsonParse.setDataId(Long.valueOf(syncInfoId));
            mqDataJsonParse.setSystemType(DataProcessEnum.SystemTypeEnum.MARKETING.getCode());
            mqDataJsonParse.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
            mqDataJsonParse.setAcceptType(DataProcessEnum.AcceptTypeEnum.GENERAL.getCode());

            rocketMqSwitch.sendMessage(apiCode, MarketingAssistConstants.TOPIC, MarketingAssistConstants.TAG_MARKETING_CUSTOMER_DATA_JSON_PARSE,
                    JSON.toJSONString(mqDataJsonParse), MQConstants.ROUTING_KEY_MARKETING_CUSTOMER_DATA_JSON_PARSE);
            //存储标识
            caffeineCache.storeIdentifier(cacheKey, Boolean.TRUE.toString());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage(), "上传数据清洗-发送JSON结构解析消息异常"), e);
        }

    }

    /**
     * 发送定制json解析MQ
     *
     * @param apiCode
     * @param id
     */
    public void sendJsonParseMq(String apiCode, Long id, Integer dataSourceType,
                                Integer dataType, Integer acceptType) {
        //发送Json解析消息,定制清洗不在发送MQ
        if (dataSourceType != null && 1 == dataSourceType) {
            return;
        }
        try {
            //使用caffeineCache存储 mq发送标识
            String cacheKey = CaffeineCacheKeyConstant.JSON_PARSE.concat(apiCode).concat(":").concat(dataType.toString())
                    .concat(":").concat(acceptType.toString());
            boolean exists = caffeineCache.hasIdentifier(cacheKey);
            if (exists) {
                return;
            }
            MqDataJsonParse mqDataJsonParse = new MqDataJsonParse();
            mqDataJsonParse.setDataId(id);
            mqDataJsonParse.setSystemType(DataProcessEnum.SystemTypeEnum.MARKETING.getCode());
            mqDataJsonParse.setDataType(dataType);
            mqDataJsonParse.setAcceptType(acceptType);
            rocketMqSwitch.sendMessage(apiCode, MarketingAssistConstants.TOPIC, MarketingAssistConstants.TAG_MARKETING_CUSTOMER_DATA_JSON_PARSE,
                    JSON.toJSONString(mqDataJsonParse), MQConstants.ROUTING_KEY_MARKETING_CUSTOMER_DATA_JSON_PARSE);
            //存储标识
            caffeineCache.storeIdentifier(cacheKey, Boolean.TRUE.toString());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage(), "上传数据清洗-发送JSON结构解析消息异常"), e);
        }

    }

    @Override
    public Result<List<ConditionVO>> getConditionList(String apiCode, String content) {
        ScoreSearchConditionMappingExample mappingExample = new ScoreSearchConditionMappingExample();
        mappingExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID).andApiCodeEqualTo(apiCode);
        List<ScoreSearchConditionMapping> scoreSearchConditionMappings = scoreSearchConditionMappingMapper.selectByExample(mappingExample);
        if (scoreSearchConditionMappings.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("未有符合条件的数据");
        }
        List<Long> conditionIds = scoreSearchConditionMappings.stream().map(ScoreSearchConditionMapping::getConditionId).collect(Collectors.toList());

        List<ConditionOfScoreVO> scoreByNameNumberList = scoreSearchConditionMapper.getScoreByNameNumberList(conditionIds, content);
        if (scoreByNameNumberList.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合条件的数据");
        }
        List<ConditionVO> conditionVOS = new ArrayList<>();
        scoreByNameNumberList.forEach(score -> {
            ConditionVO conditionVO = new ConditionVO();
            conditionVO.setId(score.getId());
            conditionVO.setConditionId(score.getId());
            conditionVO.setName(score.getName());
            conditionVO.setSourceType(score.getSourceType());
            conditionVOS.add(conditionVO);
        });
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(conditionVOS);
    }

    @Override
    public Result<ConditionVO> getConditionById(String apiCode, Long conditionId) {
        List<ConditionOfScoreVO> scoreByNameNumberList = scoreSearchConditionMapper
                .getScoreByNameNumberList(
                        Collections.singletonList(conditionId),
                        null
                );
        if (scoreByNameNumberList.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合条件的数据");
        }
        ConditionVO conditionVO = new ConditionVO();
        BeanUtils.copyProperties(scoreByNameNumberList.get(0), conditionVO);
        conditionVO.setConditionId(scoreByNameNumberList.get(0).getId());
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(conditionVO);
    }

    @Override
    public Result<String> queryUploadOverAmt(String custNum, HttpServletRequest request) {

        Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
        String testApiCode = request.getHeader("Test-ApiCode");
        String apiCode = testApiCode != null ? testApiCode : zhongYuanIdentity.get("apiCode");

        MarketingSyncUser marketingSyncUser = marketingUserMapper.selectSyncUserByCustNum(apiCode, custNum, null);
        if(marketingSyncUser == null){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("数据为空！");
        }
        // overAmt
        String reserveField1 = marketingSyncUser.getReserveField1();
        JSONObject jsonObject = JSONObject.parseObject(reserveField1);
        String overAmt = jsonObject.getString("overAmt");
        if(StringUtils.isEmpty(overAmt)){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("overAmt字段不存在！");
        }
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(overAmt);
    }

    @Override
    public Result<MarketingSyncUserVO> queryLatestSyncUser(String apiCode, String custNum, String userType) {
        if (!CollectionUtils.isEmpty(marketingCommonConfig.getQueryLatestSyncUserTransferApiCodes())
                && marketingCommonConfig.getQueryLatestSyncUserTransferApiCodes().contains(apiCode)) {
            String tcId = tableCreateService.getTcId(apiCode);
            if (StringUtils.isBlank(tcId)) {
                return new Result<MarketingSyncUserVO>().setCode(ResultCode.FAIL.getValue()).setMessage("数据为空！");
            }
            MarketingTransferSyncUser transferUser = marketingTransferSyncUserMapper.selectLatestTransferSyncUser(tcId, apiCode, custNum, userType);
            if (transferUser == null) {
                return new Result<MarketingSyncUserVO>().setCode(ResultCode.FAIL.getValue()).setMessage("数据为空！");
            }
            MarketingSyncUserVO marketingSyncUserVO = MarketingSyncUserVO.fromTransferEntity(transferUser);

            return new Result<MarketingSyncUserVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(marketingSyncUserVO);
        }
        MarketingSyncUser user = marketingUserMapper.selectLatestSyncUser(apiCode, custNum, userType);
        if (user == null) {
            return new Result<MarketingSyncUserVO>().setCode(ResultCode.FAIL.getValue()).setMessage("数据为空！");
        }
        return new Result<MarketingSyncUserVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(MarketingSyncUserVO.fromEntity(user));
    }

    public void sendJsonParseMq(String apiCode,Integer dataSourceType,Integer systemType,Integer dataType,Integer acceptType,String jsonData){
        //发送Json解析消息,定制清洗不在发送MQ
        if (dataSourceType != null && 1 == dataSourceType) {
            return;
        }

        try {
            //使用caffeineCache存储 mq发送标识
            String cacheKey = CaffeineCacheKeyConstant.JSON_PARSE.concat(apiCode)
                    .concat(":").concat(systemType.toString())
                    .concat(":").concat(DataProcessEnum.DataTypeEnum.UPLOAD.getCode().toString())
                    .concat(":").concat(DataProcessEnum.AcceptTypeEnum.GENERAL.getCode().toString());
            boolean exists = caffeineCache.hasIdentifier(cacheKey);
            if (exists) {
                return;
            }
            CommonMqDataJsonParse commonMqDataJsonParse = new CommonMqDataJsonParse();
            commonMqDataJsonParse.setApiCode(apiCode);
            commonMqDataJsonParse.setSystemType(systemType);
            commonMqDataJsonParse.setDataType(dataType);
            commonMqDataJsonParse.setAcceptType(acceptType);
            commonMqDataJsonParse.setJsonData(jsonData);
            rocketMqSwitch.sendMessage(apiCode, MarketingAssistConstants.TOPIC, MarketingAssistConstants.TAG_MARKETING_COMMON_DATA_JSON_PARSE,
                    JSON.toJSONString(commonMqDataJsonParse), MQConstants.ROUTING_KEY_MARKETING_COMMON_DATA_JSON_PARSE);
            //存储标识
            caffeineCache.storeIdentifier(cacheKey, Boolean.TRUE.toString());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据apiCode与operateType，区分AI与非AI客户，AI客户返回true，并发送到AI队列，非AI客户返回false
     * 使用范围：上传数据入库mq队列、pulsar队列
     *
     * @param apiCode    API代码
     * @param syncInfoId 同步信息ID
     * @param jsonData   JSON数据
     */

    private boolean routeToAiQueue(String apiCode, String syncInfoId, String jsonData) {
        if (StringUtils.isEmpty(jsonData) || marketingCommonConfig.getInitDataPushRule().contains(apiCode)) {
            return false;
        }

        boolean isBatch = judgeIsBatch(jsonData);
        if (marketingCommonConfig.getAiApiCodeList().contains(apiCode)) {
            getRoutingKeyAndSendToAiMq(syncInfoId, isBatch);
            return true;
        }

        for (String aiOperateType : marketingCommonConfig.getAiToPolicyOperateTypeList()) {
            if (containsOperateType(jsonData, aiOperateType)) {
                getRoutingKeyAndSendToAiMq(syncInfoId, isBatch);
                return true;
            }
        }

        // 发消息到通用入明细队列
        return false;
    }

    private boolean judgeIsBatch(String jsonData) {
        JSONArray dataArray;
        try {
            dataArray = JSON.parseObject(jsonData).getJSONArray(Constants.JSON_DATA_KEYARR);
        } catch (Exception e) {
            log.warn("客户上传数据格式错误！jsonData:{}，errMsg:{}", jsonData, e.getMessage(), e);
            return false;
        }
        if (CollectionUtils.isEmpty(dataArray)) {
            return false;
        }

        return dataArray.size() > 1;
    }

    /**
     * 检查JSON数据是否包含指定的操作类型
     * 支持字符串和数字格式的操作类型值
     *
     * @param jsonData    JSON数据字符串
     * @param operateType 操作类型值（字符串格式）
     * @return 是否包含该操作类型
     */
    private boolean containsOperateType(String jsonData, String operateType) {
        // 检查字符串格式的操作类型
        boolean hasStringFormat = jsonData.contains("\"operateType\":\"" + operateType + "\"") ||
                jsonData.contains("\"operateType\": \"" + operateType + "\"");

        // 检查数字格式的操作类型（不带引号）
        boolean hasNumberFormat = jsonData.contains("\"operateType\":" + operateType) ||
                jsonData.contains("\"operateType\": " + operateType);

        return hasStringFormat || hasNumberFormat;
    }

    /**
     * 生成ai客户的规则映射
     */
    private void generateCustomerRuleMapping(String apiCode) {
        int i = customerRuleMapper.countByApiCodeAndRuleLabel(apiCode, AI_TO_POLICY);
        if (i > 0) {
            return;
        }

        Long ruleId = customerRuleMapper.selectIdByRuleLabel(AI_TO_POLICY);
        try {
            customerRuleMapper.saveCustomerRuleMapping(apiCode, ruleId);
        } catch (DuplicateKeyException e) {
            log.warn("Ai客户数据写入明细队列，规则映射已生成，apiCode:{}, ruleLabel:{}", apiCode, AI_TO_POLICY);
        } catch (Exception e) {
            try {
                customerRuleMapper.saveCustomerRuleMapping(apiCode, ruleId);
            } catch (DuplicateKeyException ee) {
                log.warn("Ai客户数据写入明细队列，规则映射已生成，apiCode:{}, ruleLabel:{}", apiCode, AI_TO_POLICY);
            } catch (Exception ee) {
                String title = "Ai客户，自动配置规则映射，入库再次异常！！！";
                String msg = title + " 需要立即检查规则是否存在，b_marketing_customer_rule_mapping,apiCode："
                        + apiCode + "，规则标签：" + AI_TO_POLICY + "，异常内容" + ee.getMessage();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), msg, title));
                wuBaServiceClient.sendDingDingAlert(title, msg);
            }
        }
    }

    private void getRoutingKeyAndSendToAiMq(String syncInfoId, boolean isBatch) {
        if (marketingCommonConfig.getAiUseRocketMq()) {
            if (isBatch) {
                rocketMqSwitch.syncSend(AiRocketMQConstants.TOPIC_MARKETING_AI_PREUSER_RECEIVE_BATCH,
                        AiRocketMQConstants.TAG_MARKETING_AI_PREUSER_RECEIVE_BATCH, syncInfoId);
                return;
            }

            AiPreUserReceiveEnum queueByPop = queueBalancer.getQueueByPop(AiPreUserReceiveEnum.class,
                    RedisKeyConstant.AI_PREUSER_RECEIVE_MQ_BALANCER);
            rocketMqSwitch.syncSend(queueByPop.getTopic(), queueByPop.getTag(), syncInfoId);
            return;
        }

        String redisKey = RedisKeyConstant.SWITCH_MESSAGE_QUEUE + ":" + clusterEnvironment;
        String field = SwitchMessageQueueEnum.MARKETING_AI_PREUSER_RECEIVE.name();
        String aiQueueRoutingKey = SwitchMessageQueueEnum.MARKETING_AI_PREUSER_RECEIVE.getDefault_route_key();
        String routingKeyFromRedis = getRoutingKeyFromRedis(redisKey, field, aiQueueRoutingKey);
        producter.send(routingKeyFromRedis, syncInfoId);
    }

    @Override
    public String getRoutingKeyFromRedis(String key, String field, String defaultValue) {
        String routingKey = "";
        try {
            routingKey = redisChgService.hget(key, field);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                    e.getMessage(),
                    "ai客户数据，获取redis路由键失败，数据进入默认队列"
            ), e);
        }

        routingKey = StringUtils.isEmpty(routingKey) ? defaultValue : routingKey;
        return routingKey;
    }

    @Override
    public void judgeEncryptType(PushMarketingUserDetailByRuleDTO pushData, MarketingSyncUser syncUser, Integer jc3keyType) {
        Boolean isOpenNewEncrypt = marketingCommonConfig.getIsOpenNewEncrypt();
        if (!isOpenNewEncrypt) {
            if (jc3keyType == null) {
                jc3keyType = CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue();
            }
            pushData.setPhone(getOld3keyValue(syncUser.getCell(), "cell", jc3keyType));
            return;
        }

        String cellOriginal = syncUser.getCellOriginal();

        if (jc3keyType == null || jc3keyType.equals(CustomerTagsValue.PushJc3keyTypeEnum.PLAINTEXT.getValue())) {
            String decodedCell = BrCipherMaker.getInstance().decode(cellOriginal);
            // 未配置加密类型，判断是否log加密
            if (cellOriginal.equals(decodedCell)) {
                // 非log加密
                pushData.setPhone(cellOriginal);
            } else {
                // log加密
                pushData.setPhone(decodedCell);
            }
            pushData.setLogCell(syncUser.getCell());
        } else if (jc3keyType.equals(CustomerTagsValue.PushJc3keyTypeEnum.INIT.getValue())) {
            // 软交换
            pushData.setPhone(cellOriginal);
        } else {
            // 其他加密类型
            pushData.setPhone(cellOriginal);
            pushData.setLogCell(syncUser.getCell());
        }
    }

    /**
     * 处理敏感信息(姓名和身份证)的加密逻辑
     */
    @Override
    public void processSensitiveInfo(JSONObject jsonObject, MarketingSyncUser syncUser, Integer jc3keyType) {

        if (jc3keyType == null) {
            jc3keyType = CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue();
        }

        jsonObject.put("idCard", emptyDefault(get3keyValue(syncUser.getIdCard(), "idCard", jc3keyType)));
        jsonObject.put("name", emptyDefault(get3keyValue(syncUser.getName(), "name", jc3keyType)));
    }

    private String emptyDefault(String value) {
        return StringUtils.isNotEmpty(value) ? value : "";
    }

    private String get3keyValue(String content, String contentType, Integer encryptionType) {

        if (StringUtils.isBlank(content)) {
            return content;
        }

        if (CustomerTagsValue.PushJc3keyTypeEnum.INIT.getValue().equals(encryptionType)) {
            return content;
        }

        Boolean isOpenNewEncrypt = marketingCommonConfig.getIsOpenNewEncrypt();
        if (isOpenNewEncrypt) {
            if (CustomerTagsValue.PushJc3keyTypeEnum.PLAINTEXT.getValue().equals(encryptionType)) {
                return StringUtils.isNotBlank(content) ? BrCipherMaker.getInstance().decode(content) : content;
            }

            if (CustomerTagsValue.PushJc3keyTypeEnum.AES_COMMON.getValue().equals(encryptionType)
                    || CustomerTagsValue.PushJc3keyTypeEnum.AES_NMD.getValue().equals(encryptionType)) {
                String decode = BrCipherMaker.getInstance().decode(content);
                return StringUtils.isNotBlank(decode) ? DigestUtils.md5DigestAsHex(decode.getBytes()) : content;
            }
        }

        if (CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue().equals(encryptionType)) {
            String decode = BrCipherMaker.getInstance().decode(content);
            return StringUtils.isNotBlank(decode) ? DigestUtils.md5DigestAsHex(decode.getBytes()) : content;
        }

        if (CustomerTagsValue.PushJc3keyTypeEnum.SHA256_ALL.getValue().equals(encryptionType)) {
            String decode = BrCipherMaker.getInstance().decode(content);
            return StringUtils.isNotBlank(decode) ? Sha256Util.getSHA256Encrypt(decode) : content;
        }
        return null;
    }

    private String getOld3keyValue(String content, String contentType, Integer encryptionType) {

        if (StringUtils.isBlank(content)) {
            return content;
        }

        if (CustomerTagsValue.PushJc3keyTypeEnum.INIT.getValue().equals(encryptionType)) {
            return content;
        }

        if (CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue().equals(encryptionType)) {
            String decode = BrCipherMaker.getInstance().decode(content);
            return StringUtils.isNotBlank(decode) ? DigestUtils.md5DigestAsHex(decode.getBytes()) : content;
        }

        if (CustomerTagsValue.PushJc3keyTypeEnum.SHA256_ALL.getValue().equals(encryptionType)) {
            String decode = BrCipherMaker.getInstance().decode(content);
            return StringUtils.isNotBlank(decode) ? Sha256Util.getSHA256Encrypt(decode) : content;
        }
        return null;
    }

    /**
     * 根据配置表发送到对应MQ
     * 配置表：b_marketing_customer_routingKey_mapping
     *
     * @param apiCode
     * @param defaultRoutingKey 默认路由键
     * @param infoId            原始数据表id
     * @param queueEnum         队列类型
     */
    @Override
    public void sendToMqByConfig(String apiCode, String defaultRoutingKey, String infoId, CustomerQueueEnum queueEnum) {
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

    @Override
    public void sendToRocketMqByConfig(String apiCode, String topic, String tag, String infoId, CustomerQueueEnum queueEnum) {
        try {
            long l3 = System.currentTimeMillis();
            // 根据apicode和bizType获取路由键
            String apiCodeJointBizType = apiCode + "," + queueEnum.getValue();
            CustomerRoutingKeyConfig routingKeyConfig = caffeineCache.getRountingKey(apiCodeJointBizType);
            if (null == routingKeyConfig) {
                rocketMqSwitch.syncSend(topic, tag, infoId);
            } else {
                // RocketMQ不支持优先级
                String tagFromDb = routingKeyConfig.getRoutingKey();
                if (MarketingUploadSmallConstants.TAG_MARKETING_PRE_USER_RECEIVE_SMALL.equalsIgnoreCase(tagFromDb)) {
                    rocketMqSwitch.syncSend(MarketingUploadSmallConstants.TOPIC, tagFromDb, infoId);
                } else if (MarketingUploadEmergencyConstants.TAG_MARKETING_PRE_USER_RECEIVE_EMERGENCY.equalsIgnoreCase(tagFromDb)) {
                    rocketMqSwitch.syncSend(MarketingUploadEmergencyConstants.TOPIC, tagFromDb, infoId);
                } else if (MarketingTransferSmallConstants.TAG_MARKETING_TRANSFER_RECEIVE_SMALL.equalsIgnoreCase(tagFromDb)) {
                    rocketMqSwitch.syncSend(MarketingTransferSmallConstants.TOPIC, tagFromDb, infoId);
                } else if (MarketingTransferEmergencyConstants.TAG_MARKETING_TRANSFER_RECEIVE_EMERGENCY.equalsIgnoreCase(tagFromDb)) {
                    rocketMqSwitch.syncSend(MarketingTransferEmergencyConstants.TOPIC, tagFromDb, infoId);
                } else if (MarketingUploadConstants.TAG_MARKETING_PRE_USER_RECEIVE.equalsIgnoreCase(tagFromDb)) {
                    rocketMqSwitch.syncSend(MarketingUploadConstants.TOPIC, tagFromDb, infoId);
                } else if (MarketingTransferConstants.TAG_MARKETING_TRANSFER_RECEIVE.equalsIgnoreCase(tagFromDb)) {
                    rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC, tagFromDb, infoId);
                } else {
                    log.warn("[{}]RocketMQ的tag（RoutingKey）配置错误-tag[{}]consumerGroup[{}]-",
                            apiCode, tagFromDb, routingKeyConfig.getQueueName());
                }
            }
            if (log.isInfoEnabled()) {
                log.info("RocketMQ推送" + queueEnum.getDesc() + "队列耗时:{}", (System.currentTimeMillis() - l3));
            }
        } catch (Exception ex) {
            log.error("RocketMQ推送" + queueEnum.getDesc() + "队列失败,数据id：{}", infoId);
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
        CustomerTagsVO tags = customerTagsProcessService.getTags(apiCode);
        IUploadCheckService iUploadCheckService = customerTagsProcessService.getIUploadCheckService(tags);
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
        //通用调用,查询清洗规则配置
        Map<String, MarketingDataCleanGeneralRuleConfig> configRule = new HashMap<>();
        if (Objects.nonNull(marketingSyncInfo.getDataSourceType()) && (0 == marketingSyncInfo.getDataSourceType())) {
            configRule = dataCleanService.getConfigRule(apiCode, DataProcessEnum.SystemTypeEnum.MARKETING.getCode(),
                    DataProcessEnum.DataTypeEnum.UPLOAD.getCode(), DataProcessEnum.AcceptTypeEnum.GENERAL.getCode(),
                    DataProcessEnum.RuleStatusEnum.PRE_SUCCESS.getCode());
            if (!CollectionUtils.isEmpty(configRule)) {
                //剔除规则中的基础字段
                List<String> generalFields = Lists.newArrayList("dataItems", "item", "reserveField1", "reserveField2");
                configRule.keySet().removeIf(key -> generalFields.contains(key));
            }
        }
        ArrayList<Callable<Result<MarketingPreUserErrorDetailVO>>> list = new ArrayList<>();
        Map<String, UserTypeCollectionDTO> localUserTypeCache = new ConcurrentHashMap<>(16);
        // 是否为ai客户数据
        AtomicBoolean isAiOperateType = new AtomicBoolean(false);

        for (int i = 0; i < dto.getDataItems().size(); i++) {
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = dto.getDataItems().get(i);
            if (Objects.nonNull(marketingPreUserDetailDTO) && StringUtils.isEmpty(marketingPreUserDetailDTO.getTaskId())) {
                marketingPreUserDetailDTO.setTaskId(marketingSyncInfo.getCusBatch());
                marketingPreUserDetailDTO.setRequestId(marketingSyncInfo.getRequestBatch());
            }
            Integer finalIsCheck = isCheck;
            Map<String, MarketingDataCleanGeneralRuleConfig> finalConfigRule = configRule;
            list.add(() -> {
                if (contains4ByteChar(marketingPreUserDetailDTO.getName())) {
                    MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                    errorDetailVO.setCustNum(marketingPreUserDetailDTO.getCustNum());
                    errorDetailVO.setErrorCode("1008");
                    errorDetailVO.setErrorMsg(errorCodeHm.get("1008"));
                    return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                }
                //数据清洗处理
                if (!CollectionUtils.isEmpty(finalConfigRule)) {
                    Boolean cleanResult = handlerDataClean(marketingPreUserDetailDTO, finalConfigRule);
                    if (!cleanResult) {
                        MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                        errorDetailVO.setErrorCode("1007");
                        errorDetailVO.setErrorMsg(errorCodeHm.get("1007"));
                        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                    }
                }
                //此处会处理三种场景的数据
                //1 数禾、萨摩耶：只有groupType
                //2 宜信：既有groupType,又有userType
                //3 未来客户：只有userType
                String reserveField1Str = marketingPreUserDetailDTO.getReserveField1();
                ReserveField1DTO reserveField1 = null;
                JSONObject reserveFileld1Json = new JSONObject();
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
                ReserveField1DTO finalReserveField = reserveField1;
                //扩展字段添加手机号
                addCellReserveFileld1(reserveFileld1Json, marketingPreUserDetailDTO.getCell(), finalIsCheck, iUploadCheckService, tags);
                JSONObject finalReserveFileld1Json = reserveFileld1Json;
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
                iUploadCheckService.process3keyCheck(marketingPreUserDetailDTO, finalIsCheck, tags);
                Date nowData = new Date();
                String appletDate = DateUtils.format(marketingSyncInfo.getCreateTime(), "yyyy-MM-dd");
                MarketingSyncUser marketingSyncUser = new MarketingSyncUser();
                marketingSyncUser.setApiCode(apiCode);
                marketingSyncUser.setCusBatch(marketingSyncInfo.getCusBatch());
                marketingSyncUser.setRequestBatch(marketingSyncInfo.getRequestBatch());
                marketingSyncUser.setCustNum(marketingPreUserDetailDTO.getCustNum());
                marketingSyncUser.setOperateType(marketingPreUserDetailDTO.getOperateType());
                if (marketingCommonConfig.getAiToPolicyOperateTypeList().contains(marketingPreUserDetailDTO.getOperateType())) {
                    isAiOperateType.set(true);
                }
                marketingSyncUser.setIdCard(marketingPreUserDetailDTO.getId());
                marketingSyncUser.setName(marketingPreUserDetailDTO.getName());
                marketingSyncUser.setCell(marketingPreUserDetailDTO.getCell());
                marketingSyncUser.setCellSha256(marketingPreUserDetailDTO.getCellSha256());
                marketingSyncUser.setCellMd5(marketingPreUserDetailDTO.getCellMd5());
                marketingSyncUser.setCellOriginal(marketingPreUserDetailDTO.getCellOriginal());
                marketingSyncUser.setIdCardOriginal(marketingPreUserDetailDTO.getIdOriginal());
                marketingSyncUser.setNameOriginal(marketingPreUserDetailDTO.getNameOriginal());
                marketingSyncUser.setGroupType(marketingPreUserDetailDTO.getGroupType());
                marketingSyncUser.setRegisterDate(marketingPreUserDetailDTO.getRegisterDate());
                marketingSyncUser.setReserveField1(assembleReserveField1(finalReserveField,
                        finalReserveFileld1Json, apiCode));
                marketingSyncUser.setReserveField2(marketingPreUserDetailDTO.getReserveField2());
                marketingSyncUser.setCreateTime(nowData);
                marketingSyncUser.setUpdateTime(nowData);
                marketingSyncUser.setAppletDate(appletDate);
                marketingSyncUser.setStatus(marketingPreUserDetailDTO.getStatus());
                marketingSyncUser.setFailType(marketingPreUserDetailDTO.getFailType());
                marketingSyncUser.setAppletTime(marketingSyncInfo.getCreateTime());
                marketingSyncUser.setUserType(finalReserveField.getUserType());
                marketingSyncUser.setFingerprint(marketingPreUserDetailDTO.getFingerprint());
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
                } catch (DuplicateKeyException e) {
                    log.warn("insertMarketingSyncUser数据重复,{},{}", e.getMessage(), JSON.toJSON(marketingSyncUser), e);
                } catch (Exception ex) {
                    if (ex.getMessage() != null && ex.getMessage().contains("Incorrect string value")) {
                        MarketingPreUserErrorDetailVO errorDetailVO = new MarketingPreUserErrorDetailVO();
                        errorDetailVO.setCustNum(marketingPreUserDetailDTO.getCustNum());
                        errorDetailVO.setErrorCode("1008");
                        errorDetailVO.setErrorMsg(errorCodeHm.get("1008"));
                        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(errorDetailVO);
                    }
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
        List<Future<Result<MarketingPreUserErrorDetailVO>>> futures;
        try {
            futures = threadPool.invokeAll(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            threadPool.shutdown();
        }
        for (Future<Result<MarketingPreUserErrorDetailVO>> future : futures) {
            try {
                Result<MarketingPreUserErrorDetailVO> result = future.get();
                if (ResultCode.FAIL.getValue().equals(result.getCode())) {
                    errorSize++;
                    errorBuild.add(result.getData());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        // 发送场景收集队列
        if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingAssistConstants.TAG_MARKETING_UPLOAD_API_USERTYPE_COLLECTION)) {
            sendUserTypeCollectionMsg(localUserTypeCache, (Map<String, UserTypeCollectionDTO> localUserTypeCacheMap) -> {
                ApiDataInfoDTO<UserTypeCollectionDTO> dataInfoDTO = new ApiDataInfoDTO<>();
                dataInfoDTO.setApiCode(apiCode);
                dataInfoDTO.setRawDataSaveTimeStr(marketingSyncInfo.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                List<UserTypeCollectionDTO> collections = new ArrayList<>(localUserTypeCacheMap.values());
                dataInfoDTO.setArgList(collections);
                dataInfoDTO.setRequestId(marketingSyncInfo.getRequestBatch());
                return dataInfoDTO.addUploadMsgSource();
            }, MarketingAssistConstants.TOPIC, MarketingAssistConstants.TAG_MARKETING_UPLOAD_API_USERTYPE_COLLECTION);
        } else {
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
        }
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
        if (errorSize > 0) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.INITDATA_MUST_ERROR.getCode()
                    , String.format("apiCode【%s】,代运营数据原始表id【%d】", apiCode, infoId)
                    , AlarmSendCodeEnum.INITDATA_MUST_ERROR.getMessage()));
        }

        boolean isBatch = dto.getDataItems().size() > 1;
        // 发消息到推送下游队列
        sendToUniversalQueue(infoId, status, apiCode, isBatch, isAiOperateType);

        List<String> mrpApiCodes = marketingCommonConfig.getMrpUploadDataPushMqApiCodes();
        if (!CollectionUtils.isEmpty(mrpApiCodes) && mrpApiCodes.contains(apiCode)) {
            MrpMqFact mrpMqFact = new MrpMqFact();
            mrpMqFact.setSourceId(infoId);
            mrpMqFact.setSource(TransferSource.INIT_DATA_SET_PROCESS.getCode());
            mrpMqFact.setApiCode(apiCode);
            mrpMqFact.setIdempotentKey(snowflakeRedisGeneratorHandle.nextId());

            if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferConstants.TAG_MARKETING_MRP_UNIVERSAL_TRANSFER_RECEIVE)) {
                String message = JSON.toJSONString(mrpMqFact);
                rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                        , MarketingTransferConstants.TAG_MARKETING_MRP_UNIVERSAL_TRANSFER_RECEIVE, message);
            } else {
                producter.sendToUniversalTransferQueue(mrpMqFact);
            }
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
        try {
            JSONObject condition = new JSONObject();
            condition.put("request_batch", marketingSyncInfo.getRequestBatch());
            trackingService.trackBusinessLog(DataFlowDirection.IN
                    , apiCode
                    , "通用上传接口明细入库"
                    , String.format("b_marketing_sync_%s", apiCode)
                    , JSON.toJSONString(condition)
                    , Long.valueOf(dto.getDataItems().size() - errorSize)
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue).setMessage("成功");
    }

    /**
     * 判断字符串是否包含4字节字符（生僻字、emoji等）
     */
    public boolean contains4ByteChar(String str) {
        try {
            if (StringUtils.isEmpty(str)) {
                return false;
            }
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                // 判断是否是高代理项（4字节字符的第一部分）
                if (Character.isHighSurrogate(c)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            log.error("name字段判断4字节异常，name={}", str, ex);
        }
        return false;
    }


    private void addCellReserveFileld1(JSONObject reserveFileld1Json, String cell, Integer isCheck,
                                       IUploadCheckService iUploadCheckService, CustomerTagsVO tags) {
        if (StringUtils.isNotEmpty(cell)) {
            //明文规则校验
            UserValidator userValidator = new UserValidator(isCheck);
            if (userValidator.validatePhone(cell)) {
                reserveFileld1Json.put("originalCell", BrCipherMaker.getInstance().encode(cell));
            } else {
                reserveFileld1Json.put("originalCell", cell);

            }

        }

        // 处理debtorCell1-4字段，传入cell用于去重
        processDebtorCells(reserveFileld1Json, cell, isCheck, iUploadCheckService, tags);
    }

    /**
     * 处理debtorCell1-4字段，生成debtorCellList
     * 基于原始值去重，保留顺序：cell → debtorCell1 → debtorCell2 → debtorCell3 → debtorCell4
     *
     * @param reserveFileld1Json 扩展字段JSON对象
     * @param cell 主手机号字段值，用于去重
     * @param isCheck 是否校验
     * @param iUploadCheckService 解密服务
     * @param tags                客户标签配置
     */
    private void processDebtorCells(JSONObject reserveFileld1Json, String cell, Integer isCheck,
                                    IUploadCheckService iUploadCheckService, CustomerTagsVO tags) {
        String[] debtorCellFields = {"debtorCell1", "debtorCell2", "debtorCell3", "debtorCell4"};
        JSONArray debtorCellList = new JSONArray();
        int order = 1;

        // 用于去重的Set，存储已经出现过的原始值
        Set<String> existingValues = new HashSet<>();

        // 先将cell的原始值加入去重集合
        if (StringUtils.isNotEmpty(cell)) {
            existingValues.add(cell);
        }

        // 遍历debtorCell1-4字段
        for (String fieldName : debtorCellFields) {
            if (reserveFileld1Json.containsKey(fieldName)) {
                String orgDebtorCellValue = reserveFileld1Json.getString(fieldName);
                if (StringUtils.isNotEmpty(orgDebtorCellValue)) {
                    // 检查是否重复（基于原始值）
                    if (existingValues.contains(orgDebtorCellValue)) {
                        // 重复，跳过
                        continue;
                    }

                    // 解密
                    String decryptedValue = decryptDebtorCell(orgDebtorCellValue, isCheck, iUploadCheckService, tags);

                    // 只有解密成功才添加到list中
                    if (StringUtils.isNotEmpty(decryptedValue)) {
                        JSONObject debtorCellItem = new JSONObject();

                        // 设置原值
                        debtorCellItem.put("orgDebtorCell", orgDebtorCellValue);

                        // 设置log加密后的值
                        debtorCellItem.put("logDebtorCell", decryptedValue);

                        // 设置字段名
                        debtorCellItem.put("debtorCell", fieldName);

                        // 设置顺序
                        debtorCellItem.put("order", order);

                        debtorCellList.add(debtorCellItem);

                        // 添加到去重集合
                        existingValues.add(orgDebtorCellValue);

                        order++;
                    }
                }
            }
        }

        // 如果有debtorCell数据，则添加到reserveField1中
        if (!debtorCellList.isEmpty()) {
            reserveFileld1Json.put("debtorCellList", debtorCellList);
        }
    }

    /**
     * 解密debtorCell字段值
     * 使用iUploadCheckService.process3keyCheck的解密逻辑
     *
     * @param encryptedValue 加密值
     * @param isCheck 是否校验
     * @param iUploadCheckService 解密服务
     * @param tags                客户标签配置
     * @return 解密后的明文值，解密失败返回空字符串
     */
    private String decryptDebtorCell(String encryptedValue, Integer isCheck,
                                     IUploadCheckService iUploadCheckService, CustomerTagsVO tags) {
        if (StringUtils.isEmpty(encryptedValue)) {
            return "";
        }

        try {
            // 创建临时对象用于解密
            MarketingPreUserDetailDTO tempUser = new MarketingPreUserDetailDTO();
            tempUser.setCell(encryptedValue);
            tempUser.setStatus(MonitorTypeEnum.STATUS_1.getTypeCode());

            // 调用解密服务
            iUploadCheckService.process3keyCheck(tempUser, isCheck, tags);

            // 检查解密是否成功
            // 如果状态变为失败状态，说明解密失败
            if (!Integer.valueOf(MonitorTypeEnum.STATUS_1.getTypeCode()).equals(tempUser.getStatus())) {
                return "";
            }

            // 获取解密后的明文值
            return tempUser.getCell();

        } catch (Exception e) {
            // 解密过程出现异常，返回空字符串
            return "";
        }
    }

    private void sendToUniversalQueue(Long infoId, Boolean status, String apiCode, boolean isBatch, AtomicBoolean isAiOperateType) {
        if (!status) {
            return;
        }
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(infoId);
        mqFact.setSource(TransferSource.INIT_DATA_SET_PROCESS.getCode());
        mqFact.setIdempotentKey(snowflakeRedisGeneratorHandle.nextId());

        List<String> initDataPushApiCode = marketingCommonConfig.getInitDataPushRule() == null ? new ArrayList<String>() :
                marketingCommonConfig.getInitDataPushRule();
        if (initDataPushApiCode.contains(apiCode)) {
            if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE)) {
                String message = JSON.toJSONString(mqFact);
                rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                        , MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE, message);
            } else {
                producter.sendToUniversalTransferQueue(mqFact);
            }
            return;
        }

        List<String> aiApiCodeList = marketingCommonConfig.getAiApiCodeList();
        if (!CollectionUtils.isEmpty(aiApiCodeList) && aiApiCodeList.contains(apiCode)) {
            sendToAIUniversalQueue(mqFact, isBatch);
            return;
        }

        if (isAiOperateType.get()) {
            generateCustomerRuleMapping(apiCode);
            sendToAIUniversalQueue(mqFact, isBatch);
        }
    }

    private void sendToAIUniversalQueue(MqFact mqFact, boolean isBatch) {
        if (marketingCommonConfig.getAiUseRocketMq()) {
            String message = JSON.toJSONString(mqFact);
            if (isBatch) {
                rocketMqSwitch.syncSend(AiRocketMQConstants.TOPIC_MARKETING_AI_UNIVERSAL_RECEIVE_BATCH,
                        AiRocketMQConstants.TAG_MARKETING_AI_UNIVERSAL_RECEIVE_BATCH, message);
                return;
            }

            AiUniversalReceiveEnum queueByPop = queueBalancer.getQueueByPop(AiUniversalReceiveEnum.class,
                    RedisKeyConstant.AI_UNIVERSAL_RECEIVE_MQ_BALANCER);
            rocketMqSwitch.syncSend(queueByPop.getTopic(), queueByPop.getTag(), message);
            return;
        }

        producter.sendToAIUniversalQueue(mqFact);
    }

    /**
     * 根据清洗配置进行数据清洗
     *
     * @param marketingPreUserDetailDTO
     * @Date 2025/05/06 14:48
     */
    private Boolean handlerDataClean(MarketingPreUserDetailDTO marketingPreUserDetailDTO, Map<String, MarketingDataCleanGeneralRuleConfig> configRule) {
        Boolean isSuccess = Boolean.FALSE;
        try {
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(marketingPreUserDetailDTO);
            //数据清洗
            dataCleanService.dataCleanHandler(jsonObject, configRule.values(), marketingPreUserDetailDTO);
            isSuccess = Boolean.TRUE;
        } catch (Exception e) {
            log.error("上传数据清洗过程异常，custNum= {}", marketingPreUserDetailDTO.getCustNum(), e);
        }
        return isSuccess;
    }

    /**
     * （RocketMQ使用）上传数据发送场景消息到收集队列
     *
     * @param localUserTypeCache
     * @param function
     * @param topic
     * @param tag
     * @Date 2024/8/22 17:09
     */
    private void sendUserTypeCollectionMsg(Map<String, UserTypeCollectionDTO> localUserTypeCache
            , Function<Map<String, UserTypeCollectionDTO>, ApiDataInfoDTO<UserTypeCollectionDTO>> function
            , String topic, String tag) {
        String msg = "";
        try {
            msg = JSONObject.toJSONString(function.apply(localUserTypeCache));
            rocketMqSwitch.syncSend(topic, tag, msg);
        } catch (Exception e) {
            log.error("推送场景信息到队列失败,topic[{}]tag[{}]msg[{}]异常信息:", topic, tag, msg, e);
        } finally {
            // 辅助gc
            localUserTypeCache.clear();
        }
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
        boolean b = pulsarConsumerSkipUtil.shouldSkipBusinessLogic(PulsarSubscription.upLoadSubscription);
        if (b) {
            log.warn("【pulsar】标准上传数据执行跳过逻辑");
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }

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
            syncInfo.setDataSourceType(DataSourceTypeEnum.GENERAL_INTERFACE.getCode());
            //todo 模拟异常
            mockDbOrRedisError(1, apiCode);
            marketingUserMapper.insertMarketingPreUserByText(syncInfo);
            syncInfoId = syncInfo.getId().toString();
            //发送json解析MQ
            sendJsonParseMq(apiCode, syncInfoId, DataSourceTypeEnum.GENERAL_INTERFACE.getCode());
        } catch (DuplicateKeyException keyException) {
            alarmClient.sendAlarm(String.format("pulsar上传数据消费requestId冲突 requestId：%s", jsonData.getRequestId())
                    , "pulsar上传数据消费异常", AlarmSendCodeEnum.REQUESTID_CONFLICT.getCode());

            // 查询数据库中是否存在该requestId的数据且status为1
            try {
                MarketingSyncInfo existingSyncInfo = marketingSyncInfoMapper.getByApiCodeAndRequestBatch(apiCode, jsonData.getRequestId());
                if (existingSyncInfo != null && existingSyncInfo.getStatus() != null && existingSyncInfo.getStatus() == 1) {
                    log.warn("【模拟异常写入Pulsar】通用上传requestId重复但数据已存在且status=1，继续执行后续逻辑。requestId：{}", jsonData.getRequestId());
                    syncInfoId = existingSyncInfo.getId().toString();
                    //发送json解析MQ
                    sendJsonParseMq(apiCode, syncInfoId, DataSourceTypeEnum.GENERAL_INTERFACE.getCode());
                    // 继续执行后续的写入上传明细MQ逻辑，不直接返回
                } else {
                    log.warn("【模拟异常写入Pulsar】通用上传requestId重复但数据不存在或status!=1，直接返回。requestId：{}，existingSyncInfo：{}",
                            jsonData.getRequestId(), existingSyncInfo);
                    return new Result<>().setCode(ResultCode.SUCCESS.getValue());
                }
            } catch (Exception e) {
                log.error("【模拟异常写入Pulsar】通用上传查询重复requestId数据异常，requestId：{}", jsonData.getRequestId(), e);
                return new Result<>().setCode(ResultCode.SUCCESS.getValue());
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            dbException = Boolean.TRUE;
        }
        //endregion

        //region 写入上传明细MQ
        if (!dbException) {
            boolean intoAiQueue = routeToAiQueue(apiCode, syncInfoId, jdStr);
            // 非ai客户
            if (Objects.equals(intoAiQueue, false)) {
                if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingUploadConstants.TAG_MARKETING_PRE_USER_RECEIVE)) {
                    sendToRocketMqByConfig(apiCode, MarketingUploadConstants.TOPIC
                            , MarketingUploadConstants.TAG_MARKETING_PRE_USER_RECEIVE, syncInfoId, CustomerQueueEnum.ORG_SYNC);
                } else {
                    sendToMqByConfig(apiCode, MQConstants.ROUTING_KEY_MARKETING_PRE_USER_RECEIVE, syncInfoId, CustomerQueueEnum.ORG_SYNC);
                }
            }
        } else {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        //endregion

        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }

    //ReserveField1DTO中的属性是固定的，无法满足，客户动态增加字段的需求,
    //所以检查下客户上传的原始JSON，如果有些字段没有在ReserveField1DTO中，则动态拼装到数据中。
    private String assembleReserveField1(ReserveField1DTO finalReserveField, JSONObject finalReserveFileld1Json,
                                         String apiCode) {
        JSONObject finalReserveFieldObject = (JSONObject) JSONObject.toJSON(finalReserveField);
        if (null != finalReserveFileld1Json) {
            finalReserveFileld1Json.keySet().stream().forEach(k -> {
                if (!finalReserveFieldObject.containsKey(k)) {
                    finalReserveFieldObject.put(k, finalReserveFileld1Json.get(k));
                }
            });
            cleanData(finalReserveFieldObject, finalReserveFileld1Json, apiCode);
            // 携程贷后定制
            if (marketingCommonConfig.getXieChengPostLoanApiCodeLists().contains(apiCode)) {
                xieChengPostLoanHandle(finalReserveFieldObject);
            }
        }
        return JSONObject.toJSONString(finalReserveFieldObject);
    }

    private void cleanData(JSONObject finalReserveFieldObject, JSONObject finalReserveFileld1Json,
                           String apiCode) {
        // 定制化清洗apiCode
        HashMap<String, String> dataCleanMappingMap = marketingCommonConfig.getDataCleanMappingMap();
        String value = dataCleanMappingMap.get(apiCode);
        if (value != null) {
            List<String> dataCleanValue = marketingCommonConfig.getDataCleanValue();
            String customNameType = dataCleanValue != null ? dataCleanValue.get(0) : "customNameType";
            finalReserveFieldObject.put(customNameType, finalReserveFileld1Json.get(value));
        }
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
        //batchAddUniqueId(transferDataDTO.getDataItems(), TransferDataItemDTO::setFingerprint, TransferDataItemDTO::getFingerprint);
        jsonData = addUniqueId(jsonData);
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
            // 模拟数据入库成功，但返回异常入Pulsar的场景
            Map<String, Boolean> pushDataSwitch = marketingCommonConfig.getPushDataSwitch();
            if (pushDataSwitch.get(PushDataEnum.MARKETING_TRANSFER_BASE.getValue())) {
                log.warn(String.format("【模拟异常写入Pulsar】通用转化数据infoId infoId:%s", transferInfo.getId()));
                throw new Exception();
            }
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
            if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferConstants.TAG_MARKETING_TRANSFER_RECEIVE)) {
                sendToRocketMqByConfig(apiCode, MarketingTransferConstants.TOPIC
                        , MarketingTransferConstants.TAG_MARKETING_TRANSFER_RECEIVE, transferInfoId, CustomerQueueEnum.ORG_TRANSFER);
            } else {
                sendToMqByConfig(apiCode, MQConstants.ROUTING_KEY_MARKETING_TRANSFER_RECEIVE, transferInfoId, CustomerQueueEnum.ORG_TRANSFER);
            }
        }

        try {
            // 埋点
            JSONObject condition = new JSONObject();
            condition.put("request_id", transferDataDTO.getRequestId());
            trackingService.trackBusinessLog(DataFlowDirection.IN
                    , apiCode
                    , "通用转化接口上传数据"
                    , "b_marketing_transfer_info"
                    , JSON.toJSONString(condition)
                    , Long.valueOf(size)
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }

    @Override
    public Result<Boolean> consumerTransferInfo(String msg) {
        boolean b = pulsarConsumerSkipUtil.shouldSkipBusinessLogic(PulsarSubscription.transferSubscription);
        if (b) {
            log.warn("【pulsar】标准转化数据执行跳过逻辑：" + PulsarSubscription.transferSubscription);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }

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

            // 查询数据库中是否存在该requestId的数据且status为1（进行中状态）
            try {
                MarketingTransferInfo existingTransferInfo = marketingTransferInfoMapper
                        .getByApiCodeAndRequestId(apiCode, transferDataDTO.getRequestId());
                if (existingTransferInfo != null && existingTransferInfo.getStatus() != null && existingTransferInfo.getStatus() == 1) {
                    log.warn("【模拟异常写入Pulsar】通用转化数据requestId重复但数据已存在且status=1，继续执行后续逻辑。requestId：{}", transferDataDTO.getRequestId());
                    transferInfoId = existingTransferInfo.getId().toString();
                    // 继续执行后续的写入转化明细MQ逻辑，不直接返回
                } else {
                    log.warn("【模拟异常写入Pulsar】通用转化数据requestId重复但数据不存在或status!=1，直接返回。requestId：{}，existingTransferInfo：{}",
                            transferDataDTO.getRequestId(), existingTransferInfo);
                    return new Result<>().setCode(ResultCode.SUCCESS.getValue());
                }
            } catch (Exception e) {
                log.error("【模拟异常写入Pulsar】通用转化查询重复requestId数据异常，requestId：{}", transferDataDTO.getRequestId(), e);
                return new Result<>().setCode(ResultCode.SUCCESS.getValue());
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            dbException = Boolean.TRUE;
        }

        if (!dbException) {
            if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferConstants.TAG_MARKETING_TRANSFER_RECEIVE)) {
                sendToRocketMqByConfig(apiCode, MarketingTransferConstants.TOPIC
                        , MarketingTransferConstants.TAG_MARKETING_TRANSFER_RECEIVE, transferInfoId, CustomerQueueEnum.ORG_TRANSFER);
            } else {
                sendToMqByConfig(apiCode, MQConstants.ROUTING_KEY_MARKETING_TRANSFER_RECEIVE, transferInfoId, CustomerQueueEnum.ORG_TRANSFER);
            }
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
        List<String> mrpUniversalProcessApiCode = marketingCommonConfig.getMrpTransferDataPushMqApiCodes();
        Integer soleNumTrans = marketingCommonConfig.getSoleNumTrans();
        Boolean isContinue = Boolean.FALSE;
        MarketingTransferInfo transferInfo = marketingTransferInfoMapper.selectByPrimaryKey(id);
        String apiCode = transferInfo.getApiCode();
        TransferFieldProcessFactory transferFieldProcessFactory = transferFiledProcess.getTransferFieldProcessFactory(apiCode);
        TransferDataDTO<TransferDataItemDTO> dto = null;
        if (transferFieldProcessFactory != null && transferFieldProcessFactory.isFormat()) {
            dto = transferFieldProcessFactory.formatTransferObj(transferInfo.getJsonData());
        } else {
            dto = JSON.parseObject(transferInfo.getJsonData(), new TypeReference<TransferDataDTO<TransferDataItemDTO>>() {
            }.getType());
        }
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeEqualTo(apiCode).andStatusEqualTo(customerStatus);
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        if (marketingCustomers.size() == 0) {
            throw new RuntimeException(String.format("该apicode:%s 没有维护cid信息,消费有问题", apiCode));
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
                transferSyncUser.setApiCode(apiCode);
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
                transferSyncUser.setFingerprint(transferDataItemDTO.getFingerprint());
                if (transferFieldProcessFactory != null) {
                    transferFieldProcessFactory.fieldProcess(transferSyncUser, transferDataItemDTO);
                }
                try {
                    marketingTransferSyncUserMapper.insertSelective(transferSyncUser);
                    String key = transferSyncUser.getUserType();
                    Set<String> startsWith = marketingCommonConfig.getUserTypeAndSumRealtimeApiCodeStartsWith();
                    if (StringUtils.isNotBlank(key)
                            && startsWith.stream().anyMatch(apiCode::startsWith)
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
                } catch (DuplicateKeyException e) {
                    log.warn("insertMarketingTransferSyncUser数据重复,{},{}", e.getMessage(), JSON.toJSON(transferSyncUser), e);
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
        List<Future<Result<MarketingPreUserErrorDetailVO>>> futures;
        try {
            futures = threadPool.invokeAll(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            threadPool.shutdown();
        }
        for (Future<Result<MarketingPreUserErrorDetailVO>> future : futures) {
            try {
                Result<MarketingPreUserErrorDetailVO> result = future.get();
                if (ResultCode.FAIL.getValue().equals(result.getCode())) {
                    errorSize++;
                    errorBuild.add(result.getData());
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
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

        if (errorSize > 0) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TRANSFER_MUST_ERROR.getCode()
                    , String.format("apiCode【%s】,转化数据原始表id【%d】", transferInfo.getApiCode(), id)
                    , AlarmSendCodeEnum.TRANSFER_MUST_ERROR.getMessage()));
        }

        JSONObject pushCustomerMqFact = new JSONObject();
        pushCustomerMqFact.put("id", id);
        pushCustomerMqFact.put("idempotentKey", snowflakeRedisGeneratorHandle.nextId());
        String pushCustomerMessage = JSON.toJSONString(pushCustomerMqFact);

        if (pushCustomerApiCodes.contains(apiCode)
                && (updateSyncInfo.getStatus().equals(StatusConstants.MarketingPreUserStatus_success)
                || updateSyncInfo.getStatus().equals(StatusConstants.MarketingPreUserStatus_success_part))) {
            if (transferInfo.getRequestId().startsWith("black_")) {
                if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingOutsideInterfaceConstants.TAG_MARKETING_TRANSFER_PUSH_BLACK)) {
                    rocketMqSwitch.syncSend(MarketingOutsideInterfaceConstants.TOPIC
                            , MarketingOutsideInterfaceConstants.TAG_MARKETING_TRANSFER_PUSH_BLACK, pushCustomerMessage);
                } else {
                    producter.send(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_PUSH_BLACK, pushCustomerMessage);
                }
            } else {
                if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingOutsideInterfaceConstants.TAG_MARKETING_TRANSFER_PUSH_CUSTOMER)) {
                    rocketMqSwitch.syncSend(MarketingOutsideInterfaceConstants.TOPIC
                            , MarketingOutsideInterfaceConstants.TAG_MARKETING_TRANSFER_PUSH_CUSTOMER, pushCustomerMessage);
                } else {
                    producter.send(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_PUSH_CUSTOMER, pushCustomerMessage);
                }
            }
        }
        if (universalProcessApiCode.contains(apiCode)) {
            MqFact mqFact = new MqFact();
            mqFact.setSourceId(id);
            mqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
            mqFact.setIdempotentKey(snowflakeRedisGeneratorHandle.nextId());

            if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE)) {
                String message = JSON.toJSONString(mqFact);
                rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                        , MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE, message);
            } else {
                producter.sendToUniversalTransferQueue(mqFact);
            }
        }
        List<String> mrpApiCodes = marketingCommonConfig.getMrpTransferDataPushMqApiCodes();
        if (!CollectionUtils.isEmpty(mrpApiCodes) && mrpApiCodes.contains(apiCode)) {
            MrpMqFact mrpMqFact = new MrpMqFact();
            mrpMqFact.setSourceId(id);
            mrpMqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
            mrpMqFact.setApiCode(apiCode);
            mrpMqFact.setIdempotentKey(snowflakeRedisGeneratorHandle.nextId());
            if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferConstants.TAG_MARKETING_MRP_UNIVERSAL_TRANSFER_RECEIVE)) {
                String message = JSON.toJSONString(mrpMqFact);
                rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                        , MarketingTransferConstants.TAG_MARKETING_MRP_UNIVERSAL_TRANSFER_RECEIVE, message);
            } else {
                producter.sendToUniversalTransferQueue(mrpMqFact);
            }
        }
        //region 埋点
        try {
            JSONObject condition = new JSONObject();
            condition.put("request_id", transferInfo.getRequestId());
            trackingService.trackBusinessLog(DataFlowDirection.IN
                    , apiCode
                    , "通用转化接口明细入库"
                    , String.format("b_marketing_transfer_sync_%s", tcid)
                    , JSON.toJSONString(condition)
                    , Long.valueOf(dto.getDataItems().size() - errorSize)
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }
        //endregion
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContinue).setMessage("成功");
    }

    private void transferSendUserTypeCollectionMsg(String cid, MarketingTransferInfo transferInfo
            , Map<String, UserTypeCollectionDTO> localUserTypeCache) {
        String apiCode = transferInfo.getApiCode();
        if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingAssistConstants.TAG_MARKETING_TRANSFER_API_USERTYPE_COLLECTION)) {
            sendUserTypeCollectionMsg(localUserTypeCache, (Map<String, UserTypeCollectionDTO> localUserTypeCacheMap) -> {
                ApiDataInfoDTO<UserTypeCollectionDTO> dataInfoDTO = new ApiDataInfoDTO<>();
                List<UserTypeCollectionDTO> collections = new ArrayList<>(localUserTypeCacheMap.values());
                dataInfoDTO.setArgList(collections);
                dataInfoDTO.setCid(cid);
                dataInfoDTO.setApiCode(apiCode);
                dataInfoDTO.setRawDataSaveTimeStr(transferInfo.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                dataInfoDTO.setRequestId(transferInfo.getRequestId());
                return dataInfoDTO.addTransferMsgSource();
            }, MarketingAssistConstants.TOPIC, MarketingAssistConstants.TAG_MARKETING_TRANSFER_API_USERTYPE_COLLECTION);
        } else {
            sendUserTypeCollectionMsg(localUserTypeCache, (Map<String, UserTypeCollectionDTO> localUserTypeCacheMap) -> {
                ApiDataInfoDTO<UserTypeCollectionDTO> dataInfoDTO = new ApiDataInfoDTO<>();
                List<UserTypeCollectionDTO> collections = new ArrayList<>(localUserTypeCacheMap.values());
                dataInfoDTO.setArgList(collections);
                dataInfoDTO.setCid(cid);
                dataInfoDTO.setApiCode(apiCode);
                dataInfoDTO.setRawDataSaveTimeStr(transferInfo.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                dataInfoDTO.setRequestId(transferInfo.getRequestId());
                return dataInfoDTO.addTransferMsgSource();
            }, MQConstants.ROUTING_KEY_MARKETING_TRANSFER_API_USERTYPE_COLLECTION_COUNT_FRAGMENTS);
        }
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
        try {
            String remark = String.format("转化数据查询条件：%s，转化明细情况：%s"
                    , transferInfo.getRequestId()
                    , StringUtils.isNotBlank(transferInfo.getErrorInfo()) ? "转化数据有错误的数据" : "全部成功");
            trackingService.trackPointLog(DataFlowDirection.IN
                    , transferInfo.getApiCode()
                    , "通用转化查询数据状态接口"
                    , Optional.ofNullable(transferInfo.getActualNum()).map(Long::valueOf).orElse(0L)
                    , remark
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
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
            } else {
                user.setCellMd5(Md5Utils.cell32(content));
                user.setCellSha256(Sha256Util.getSHA256Encrypt(content));
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
        try {
            JSONObject condition = new JSONObject();
            condition.put("request_batch", syncInfo.getRequestBatch());
            trackingService.trackPointLog(DataFlowDirection.IN
                    , syncInfo.getApiCode()
                    , "通用上传查询数据状态接口"
                    , Optional.ofNullable(syncInfo.getActualNum()).map(Long::valueOf).orElse(0L)
                    , JSON.toJSONString(condition)
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
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
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                            "推送客服任务异常：任务线程:" + t.getName()), e);
                }
            },
            // 队列模式，false 后人先出，true 先进先出
            false);

    private final String cidKey = "marketing:innerapi:transfer:cid:";

    @MqIdempotent
    public Result<Boolean> pushPersonalTransferDataWrapper(String msg) {
        Long infoId;
        // 兼容老消息（纯数字字符串）和新消息（JSON格式）
        if (msg.startsWith("{")) {
            JSONObject jsonObject = JSON.parseObject(msg);
            if (jsonObject != null && jsonObject.containsKey("id")) {
                infoId = jsonObject.getLong("id");
            } else {
                log.warn("JSON消息中未找到id字段，msg: {}", msg);
                throw new RuntimeException("消息格式错误，JSON中未找到id字段");
            }
        } else {
            try {
                infoId = JSON.parseObject(msg, new TypeReference<Long>() {}.getType());
            } catch (Exception e) {
                log.warn("解析数字消息失败，msg: {}", msg, e);
                throw new RuntimeException("消息格式错误，无法解析为数字", e);
            }
        }

        if (infoId == null) {
            throw new RuntimeException("消息中未找到id字段");
        }

        return pushPersonalTransferData(infoId);
    }

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
                    String smg =
                            String.format("主键[%d];apiCode[%s];requestId[%s]推送错误！\n%s", infoId, apiCode, info.getRequestId(), e.getMessage());
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), smg), e);
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
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), e.getMessage()), e);
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
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), "未知的标记:" + transferStatus));
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
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), e.getMessage()), e);
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
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), exception.getMessage()), exception);
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
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), e.getMessage()), e);
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
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), e.getMessage()), e);
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
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), e.getMessage()), e);
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
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                        String.format("推送黑名单报错：%s", result.getData())));
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

    @MqIdempotent
    @Override
    public Result<Boolean> consumerBlack(String msg) {
        Long id;
        // 兼容老消息（纯数字字符串）和新消息（JSON格式）
        if (msg.startsWith("{")) {
            JSONObject jsonObject = JSON.parseObject(msg);
            if (jsonObject != null && jsonObject.containsKey("id")) {
                id = jsonObject.getLong("id");
            } else {
                log.warn("JSON消息中未找到id字段，msg: {}", msg);
                throw new RuntimeException("消息格式错误，JSON中未找到id字段");
            }
        } else {
            try {
                id = JSON.parseObject(msg, new TypeReference<Long>() {}.getType());
            } catch (Exception e) {
                log.warn("解析数字消息失败，msg: {}", msg, e);
                throw new RuntimeException("消息格式错误，无法解析为数字", e);
            }
        }

        if (id == null) {
            throw new RuntimeException("消息中未找到id字段");
        }

        Boolean isContinue = Boolean.FALSE;
        MarketingTransferInfo transferInfo = marketingTransferInfoMapper.selectByPrimaryKey(id);
        if (transferInfo == null) {
            return new Result<>()
                    .setCode(ResultCode.SUCCESS.getValue())
                    .setDate(isContinue)
                    .setMessage("数据不存在");
        }

        String apiCode = transferInfo.getApiCode();
        MqIdempotentContext.setApiCode(apiCode);
        String tcId = tableCreateService.getTcId(apiCode);
        if (tcId == null) {
            return new Result<>()
                    .setCode(ResultCode.SUCCESS.getValue())
                    .setDate(isContinue)
                    .setMessage(String.format("apiCode:%s 未维护cid信息", apiCode));
        }
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).
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
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                        String.format("推送黑名单报错：%s", result.getData())));
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
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> saveCondition(ConditionSaveDTO dto) {

        SearchConditionDTO searchConditionDTO = new SearchConditionDTO();
        searchConditionDTO.setApiCode(dto.getApiCode());
        searchConditionDTO.setName(dto.getName());
        searchConditionDTO.setStatus(Constants.DATA_VALID);
        Integer scoreCountBySearch = scoreSearchConditionMapper.getScoreCountBySearch(searchConditionDTO);
        if (scoreCountBySearch > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("规则模板名称重复");
        }

        IRuleCenterFilterTemplateService fileterTemplate = ruleCenterBySourceTypeFactory.getFileterTemplate(dto.getSourceType());
        String source = fileterTemplate.getSource(dto.getSources());
        if (StringUtils.isBlank(source)) {
            return new Result<>()
                    .setCode(ResultCode.FAIL.getValue())
                    .setMessage("模板数据源处理失败");
        }

        Date date = new Date();
        ScoreSearchCondition searchCondition = new ScoreSearchCondition();
        searchCondition.setName(dto.getName());
        searchCondition.setConditionNumber(buildConditionNumber(dto.getApiCode()));
        searchCondition.setConditionType(1);
        searchCondition.setContent(dto.getmRuleCondition());
        searchCondition.setContentShow(dto.getmRuleConditionShow());
        searchCondition.setScoreContent(dto.getmScoreCondition());
        searchCondition.setTagContent(dto.getmTagCondition());
        searchCondition.setCreateTime(date);
        searchCondition.setUpdateTime(date);
        searchCondition.setSourceType(dto.getSourceType());
        searchCondition.setSourceCondition(source);
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
        // 若置为失效 则需判断该规则模板是否被推送决策配置引用
        if (new Integer(2).equals(dto.getStatus())) {
            PushDecisionsExample pushDecisionsExample = new PushDecisionsExample();
            pushDecisionsExample.createCriteria().andDependencyTemplateIdEqualTo(dto.getId()).andIsDelEqualTo(1);
            List<PushDecisions> pushDecisions = pushDecisionsMapper.selectByExample(pushDecisionsExample);
            if (!pushDecisions.isEmpty()) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该规则模板已被引用，不能修改为失效");
            }
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
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, marketingCommonConfig.getZhongBangCaifuLabelThreadNum());
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

    @Override
    public Result<Boolean> deleteRule(Long id) {
        try {
            // 判断该规则模板是否被推送决策配置引用
            PushDecisionsExample pushDecisionsExample = new PushDecisionsExample();
            pushDecisionsExample.createCriteria().andDependencyTemplateIdEqualTo(id).andIsDelEqualTo(Constants.DATA_VALID);
            List<PushDecisions> pushDecisions = pushDecisionsMapper.selectByExample(pushDecisionsExample);
            if (!pushDecisions.isEmpty()) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该规则模板已被引用，不能删除");
            }
            ScoreSearchCondition updateEntity = new ScoreSearchCondition();
            updateEntity.setId(id);
            updateEntity.setIsDel(9);
            scoreSearchConditionMapper.updateByPrimaryKeySelective(updateEntity);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), "删除规则模板报错，id=" + id), e);
        }
        return null;
    }

    private void xieChengPostLoanHandle(JSONObject finalReserveFieldObject) {
        try {
            // 如果ifMinRepay=false，则ifMinRepay和minRepayAmt的值置空
            if (finalReserveFieldObject.containsKey("ifMinRepay") && !finalReserveFieldObject.getBoolean("ifMinRepay")) {
                finalReserveFieldObject.put("ifMinRepay", "");
                finalReserveFieldObject.put("minRepayAmt", "");
            }
            // 如果supportWx=0时，置空
            if (finalReserveFieldObject.containsKey("supportWx") && (finalReserveFieldObject.getInteger("supportWx") == 0
                    || "0".equals(finalReserveFieldObject.getString("supportWx")))) {
                finalReserveFieldObject.put("supportWx", "");
            }
            // 如果supportDeduct中不包含“协议”则置空
            if (finalReserveFieldObject.containsKey("supportDeduct") && !finalReserveFieldObject.getString("supportDeduct").contains("协议")) {
                finalReserveFieldObject.put("supportDeduct", "");
            }
            // 把loanTime的值20251120调整成2025-11-20的格式，并赋值到loanTimes上
            String loanTime = finalReserveFieldObject.getString("loanTime");
            if (StringUtils.isNotBlank(loanTime)) {
                Date date = DateUtil.parse(loanTime);
                // 格式化为yyyy-MM-dd格式
                String formattedDate = DateUtil.format(date, DatePattern.NORM_DATE_PATTERN);
                finalReserveFieldObject.put("loanTime", formattedDate);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.INITDATA_MUST_ERROR.getCode(),
                    "携程贷后定制清洗异常" + e + "，reserve_field1:" + JSON.toJSONString(finalReserveFieldObject) + "msg:" + e.getMessage()));
        }
    }
}
