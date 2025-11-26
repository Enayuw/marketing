package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.constants.rocketmq.MarketingTransferConstants;
import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.SnowflakeIdGenerator;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.customer.CallRecordDTO;
import com.br.marketing.dto.customer.SmsRecordDTO;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.BaseUserType;
import com.br.marketing.dto.shuhe.strategy.CuFuJie;
import com.br.marketing.dto.xiecheng.XieChengReportMessageDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.XcReportTypeEnum;
import com.br.marketing.enums.XieChengConsumer;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.mapper.*;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.MrpMqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Slf4j
public class ZnkfPushServiceImpl implements ZnkfPushService {

    @Autowired
    private CallRecordMapper callRecordMapper;

    @Autowired
    private SmsCallbackMapper smsCallbackMapper;

    @Autowired
    private SmsCallbackAtOnceMapper smsCallbackAtOnceMapper;

    @Autowired
    private RoboAIBlackPhoneMarkMapperBase roboAIBlackPhoneMarkMapper;

    @Autowired
    private IMarketingSyncUserService iMarketingSyncUserService;

    @Autowired
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    private RabbitMqProducter producter;

    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Resource
    private RocketMqTemplate template;

    @Resource
    private DataLoadingHandlerService handlerService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;;

    @Autowired
    private MarketingCallRecordVersionMapper marketingCallRecordVersionMapper;

    @Autowired
    private CallRecordingMapper callRecordingMapper;

    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    private final String title = "客服->推送电销";

    @Override
    public String znkfPushCallBack(CallRecordDTO dto) {
        try {
            String paramOfValidity = paramOfValidity(dto);
            if (!"true".equals(paramOfValidity)) {
                log.error("客服拨打数据缺失必填参数，" + paramOfValidity);
                return paramOfValidity;
            }
            //参数校验通过，客服拨打记录落库
            CallRecord callRecord = new CallRecord();
            callRecord.setCreateTime(new Date());
            BeanUtils.copyProperties(dto, callRecord);
            BeanUtils.copyProperties(dto.getDetail(), callRecord);
            callRecord.setCallStartTime(StringUtils.isNotEmpty(dto.getDetail().getCallStartTime()) ? new Date(dto.getDetail().getCallStartTime()) : null);
            callRecord.setCallConnectTime(StringUtils.isNotEmpty(dto.getDetail().getCallConnectTime()) ? new Date(dto.getDetail().getCallConnectTime()) : null);
            callRecord.setCallEndTime(StringUtils.isNotEmpty(dto.getDetail().getCallEndTime()) ? new Date(dto.getDetail().getCallEndTime()) : null);
            // 增加联合唯一索引，去掉查询 提升性能
            try {
                callRecordMapper.insertSelective(callRecord);
            }catch (DuplicateKeyException keyException) {
                return "success";
            }
                // 2022-5-17 15:13:23 修改为可配置的apiCode
//                if ("3710004".equals(callRecord.getApiCode()) || "3710023".equals(callRecord.getApiCode()) || "7410785".equals(callRecord.getApiCode())) {
                List<String> apiCodes = marketingCommonConfig.getCallRecordDataPushMqApiCodes();
                if (apiCodes == null) {
                    apiCodes = Arrays.asList("3710004", "3710023", "3710043", "7410785");
                }
                String apiCode = callRecord.getApiCode();
                if (apiCodes.contains(apiCode)) {
                    //推mq
                    final MqFact mqFact = new MqFact();
                    mqFact.setSourceId(callRecord.getId());
                    mqFact.setSource(TransferSource.CUSTOMER_CALL_RECORD.getCode());
                    if(rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE)){
                        String message = JSON.toJSONString(mqFact);
                        rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                                , MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE, message);
                    }else{
                        producter.sendToUniversalTransferQueue(mqFact);
                    }
                }
            // 携程定制逻辑
            if (marketingCommonConfig.getXieChengReportMqConfig().containsKey(apiCode)) {
                String message = genMessage(callRecord.getId(), XcReportTypeEnum.CALL.getValue());
                if (isMockData(callRecord) && marketingCommonConfig.getXieChengCpaApiCodeList().contains(apiCode)) {
                    sendToRocketMQ(MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_REPORT_MOCK_DELAY,
                            MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT_MOCK_DELAY,
                            message, marketingCommonConfig.getXieChengReportMockDelaySeconds());
                } else {
                    // 使用负载均衡消费者逻辑
                    handleWithConsumerRotation(message);
                }
            }
            List<String> mrpApiCodes = marketingCommonConfig.getMrpCallRecordDataPushMqApiCodes();
            if (!CollectionUtils.isEmpty(mrpApiCodes) && mrpApiCodes.contains(callRecord.getApiCode())) {
                MrpMqFact mrpMqFact = new MrpMqFact();
                mrpMqFact.setSourceId(callRecord.getId());
                mrpMqFact.setSource(TransferSource.CUSTOMER_CALL_RECORD.getCode());
                mrpMqFact.setApiCode(callRecord.getApiCode());
                if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferConstants.TAG_MARKETING_MRP_UNIVERSAL_TRANSFER_RECEIVE)) {
                    String message = JSON.toJSONString(mrpMqFact);
                    rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                            , MarketingTransferConstants.TAG_MARKETING_MRP_UNIVERSAL_TRANSFER_RECEIVE, message);
                } else {
                    producter.sendToUniversalTransferQueue(mrpMqFact);
                }
            }
        } catch (Exception ex) {
            log.error("taskId={},caseNum={},sessionId={}的客服拨打数据落库失败！错误信息为{}", dto.getTaskId(), dto.getCaseNum(), dto.getDetail().getSessionId(), ex);
            return "客服拨打记录落库失败(insert b_call_record fail)!";
        }
        return "success";
    }

    private String genMessage(Long originId, Integer type) {
        XieChengReportMessageDTO messageDTO = new XieChengReportMessageDTO();
        messageDTO.setSourceId(originId);
        messageDTO.setType(type);
        messageDTO.setIdempotentKey(String.valueOf(snowflakeRedisGeneratorHandle.nextId()));
        return JSONObject.toJSONString(messageDTO);
    }

    // 3. 提取的方法
    private void handleWithConsumerRotation(String message) {
        initializeConsumerQueue();
        String consumerName = redisChgService.rpoplpush(RedisKeyConstant.XIECHENG_REPORT_CONSUME_RNAME);
        XieChengConsumer consumer = XieChengConsumer.fromName(consumerName);
        sendToRocketMQ(consumer, message);
    }

    private boolean isMockData(CallRecord callRecord) {
        return StringUtils.isNotEmpty(callRecord.getLineName())
                && callRecord.getLineName().contains("挡板");
    }

    private void initializeConsumerQueue() {
        Long queueLength = redisChgService.llen(RedisKeyConstant.XIECHENG_REPORT_CONSUME_RNAME);
        if (queueLength == 0) {
            String[] consumers = Arrays.stream(XieChengConsumer.values())
                    .map(XieChengConsumer::getConsumerName)
                    .toArray(String[]::new);
            redisChgService.rpush(RedisKeyConstant.XIECHENG_REPORT_CONSUME_RNAME, consumers);
            log.warn("初始化消费者队列: {}", Arrays.toString(consumers));
        }
    }

    private void sendToRocketMQ(XieChengConsumer consumer, String message) {
        try {
            rocketMqSwitch.syncSend(consumer.getTopic(), consumer.getTag(), message);
        } catch (Exception e) {
            String errorMessage = String.format("携程上报消息发送失败,消息发送失败 [consumer: %s, topic: %s, tag: %s,message: %s",
                    consumer.name(), consumer.getTopic(), consumer.getTag(), message);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), errorMessage + e.getMessage()
                    , "携程上报消息发送失败,消息发送失败!"));
        }
    }

    private void sendToRocketMQ(String topic, String tag, String message, long delayTime) {
        try {
            rocketMqSwitch.syncSendDelaySecond(topic, tag, message, delayTime);
        } catch (Exception e) {
            String errorMessage = String.format("携程挡板上报消息发送失败,消息发送失败 [topic: %s, tag: %s,message: %s",
                    topic, tag, message);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), errorMessage + e.getMessage()
                    , "携程挡板上报消息发送失败,消息发送失败!"));
        }
    }
    /**
     * 判断是否符合情况b：userType=促申完 && intentionGrade="A级(有明确意向）" && cusNun && 有效期内
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean isSatisfyPushDX(CallRecordBO dto) throws IllegalAccessException {
        Map map = (Map) JSONObject.parse(dto.getDetail().getUserProperties());
        if (StringUtils.isEmpty(map) || StringUtils.isEmpty(map.get("groupType"))) {
            log.warn("caseNum={}的数据groupType缺失！", dto.getCaseNum());
            return false;
        }
        if (StringUtils.isEmpty(dto.getDetail().getIntentionGrade())) {
            log.warn("caseNum={}的数据intentionGrade缺失！", dto.getCaseNum());
            return false;
        }
        String groupType = map.get("groupType").toString();
        BaseUserType baseUserType = UserTypeStrategyFactory.getUserTypeStrategy(groupType);
        if (baseUserType instanceof CuFuJie) {
            return cuFuJie(dto, groupType);
        }
        boolean intentionGrade = false;
        if (!"促申完".equals(groupType) && !"促首借".equals(groupType)) {
            log.info("taskId={},caseNum={},sessionId={}的数据不符合情况b的促申完/促首借场景！", dto.getTaskId(), dto.getCaseNum(), dto.getDetail().getSessionId());
            return false;
        }
        Boolean isPeriod = false;
        if ("促申完".equals(groupType)) {
            if ("A类".equals(dto.getDetail().getIntentionGrade()) || "A".equals(dto.getDetail().getIntentionGrade()) || "B".equals(dto.getDetail().getIntentionGrade())) {
                intentionGrade = true;
            }
            isPeriod = iMarketingSyncUserService.isPeriodOfValidity(
                    dto.getApiCode(), dto.getCaseNum(), groupType, new Date(), 14);
        } else if ("促首借".equals(groupType)) {
            if ("A类".equals(dto.getDetail().getIntentionGrade()) || "A".equals(dto.getDetail().getIntentionGrade())) {
                intentionGrade = true;
            }
            //促首借的有效期:T+31日
            Integer day = handlerService.getShuHePeriodOfValidityDay(dto.getUserType());
            isPeriod = iMarketingSyncUserService.isPeriodOfValidity(dto.getApiCode(), dto.getCaseNum(), groupType, new Date(), day);
        }
        if (!intentionGrade) {
            log.info("拨打记录数据不符合intentionGrade推送条件,id={}", dto.getId());
            return false;
        }
        if (!isPeriod) {
            //不在有效期内
            log.warn("{}场景,id={}不在有效期内", groupType, dto.getId());
            return false;
        }
        return true;
    }

    /**
     * key存在-->不是首次；key不存在-->是首次传输，redis过期时间为第二天凌晨0点
     *
     * @param key
     * @return
     */
    @Override
    public Boolean cusNumIsFirstToday(String key) {
        if (redisChgService.exists(key)) {
            return false;
        }
        Integer seconds = DateHelper.getRemainSecondsOneDay(new Date());
        //redisChgService.setex(key, "1", seconds);
        return redisChgService.setnx(key, "1", seconds);
    }

    @Override
    public ApiResult znkfPushBlackPhoneMark(String apiCode, String pushDate) {
        String pushEndDate = "";
        try {
            pushEndDate = DateUtils.format(DateUtils.parse(pushDate, "yyyy-MM-dd HH:mm:ss"));
        } catch (ParseException e) {
            log.error("格式化日期错误", e);
            return new ApiResult().fail("pushDate 格式化日期错误");
        }
        List<String> yiXinApiCode = marketingCommonConfig.getYiXinApiCode();
        if (!CollectionUtils.isEmpty(yiXinApiCode) && yiXinApiCode.contains(apiCode)) {
            RoboAIBlackPhoneMark roboAIBlackPhoneMark = new RoboAIBlackPhoneMark();
            roboAIBlackPhoneMark.setApiCode(apiCode);
            roboAIBlackPhoneMark.setPushEndTime(pushDate);
            roboAIBlackPhoneMark.setCreateTime(new Date());
            roboAIBlackPhoneMark.setPushEndDate(pushEndDate);
            roboAIBlackPhoneMarkMapper.insertSelective(roboAIBlackPhoneMark);
            return new ApiResult().setCode("00").setMessage("推送成功");
        } else {
            return new ApiResult().fail("非宜信的apiCode，请检查配置中心");
        }
    }

    @Override
    public Boolean isPushBlackPhoneEnd(String apiCode, String pushDate) {
        Boolean isPushEnd = false;
        RoboAIBlackPhoneMarkExample aiBlackPhoneMarkExample = new RoboAIBlackPhoneMarkExample();
        aiBlackPhoneMarkExample.createCriteria().andApiCodeEqualTo(apiCode).andPushEndDateEqualTo(pushDate);
        List<RoboAIBlackPhoneMark> roboAIBlackPhoneMarkList = roboAIBlackPhoneMarkMapper.selectByExample(aiBlackPhoneMarkExample);
        if (!CollectionUtils.isEmpty(roboAIBlackPhoneMarkList)) {
            isPushEnd = true;
        }
        return isPushEnd;
    }

    @Override
    public String smsCallBack(SmsRecordDTO dto) {
        try {
            String value = checkValues(dto);
            if (!value.isEmpty()) {
                return value;
            }
            String thirdCallNo = dto.getThirdCallNo();
            //校验是否已经落库
            SmsCallbackExample smsCallbackExample = new SmsCallbackExample();
            smsCallbackExample.createCriteria().andThirdCallNoEqualTo(thirdCallNo);
            int i = smsCallbackMapper.countByExample(smsCallbackExample);
            if (i > 0) {
                log.warn("短信流水号重复：" + thirdCallNo);
                return "短信流水号重复：" + thirdCallNo;
            }
            SmsCallback smsCallback = new SmsCallback();
            smsCallback.setCreateDate(String.valueOf(LocalDate.now()));
            smsCallback.setCreateTime(new Date());
            BeanUtils.copyProperties(dto, smsCallback);
            smsCallback.setApiCode(dto.getApiCode());
            smsCallbackMapper.insertSelective(smsCallback);
            List<String> apiCodes = marketingCommonConfig.getSmsCallBackDataPushMqApiCodes();
            String apiCode = dto.getApiCode();
            if (apiCodes != null && apiCodes.contains(apiCode)) {
                //推mq
                final MqFact mqFact = new MqFact();
                mqFact.setSourceId(smsCallback.getId());
                mqFact.setSource(TransferSource.CUSTOMER_SMS_CALLBACK.getCode());
                if (rocketMqSwitch.rocketMQSwitchFlag(dto.getApiCode(), MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE)) {
                    String message = JSON.toJSONString(mqFact);
                    rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                            , MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE, message);
                } else {
                    producter.sendToUniversalTransferQueue(mqFact);
                }
            }
        } catch (Exception ex) {
            log.error("外呼短信记录落库失败！短信流水号={},错误信息为{}", dto.getThirdCallNo(), ex);
            return "外呼短信记录落库失败(insert b_sms_callback fail)!";
        }
        return "success";
    }

    /**
     * 外呼短信发送即回调实现
     * @param dto
     * @return
     */
    @Override
    public String smsCallBackAtOnce(SmsRecordDTO dto) {
        try {
            if(StringUtils.isEmpty(dto.getThirdCallNo())){
                return "短信流水号 thirdCallNo 为空";
            }
            String thirdCallNo = dto.getThirdCallNo();
            // 校验是否已经落库
            SmsCallbackAtOnceExample smsCallbackAtOnceExample = new SmsCallbackAtOnceExample();
            smsCallbackAtOnceExample.createCriteria().andThirdCallNoEqualTo(thirdCallNo);
            int i = smsCallbackAtOnceMapper.countByExample(smsCallbackAtOnceExample);
            if (i > 0) {
                log.warn("短信流水号重复：" + thirdCallNo);
                return "短信流水号重复：" + thirdCallNo;
            }
            SmsCallbackAtOnce smsCallbackAtOnce = new SmsCallbackAtOnce();
            smsCallbackAtOnce.setCreateDate(String.valueOf(LocalDate.now()));
            smsCallbackAtOnce.setCreateTime(new Date());
            BeanUtils.copyProperties(dto, smsCallbackAtOnce);
            smsCallbackAtOnceMapper.insertSelective(smsCallbackAtOnce);

            if (Objects.equals(5, dto.getCallBackType()) && marketingCommonConfig.getXieChengCpaApiCodeList().contains(dto.getApiCode())) {
                String message = genMessage(smsCallbackAtOnce.getId(), XcReportTypeEnum.SMS.getValue());
                rocketMqSwitch.syncSend(MarketingXieChengConstants.TOPIC_MARKETING_XIECHENG_SMS_REPORT,
                        MarketingXieChengConstants.TAG_MARKETING_XIECHENG_SMS_REPORT, message);
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                            "外呼短信即回调入库失败，流水号:" + dto.getThirdCallNo() + "。" + ex.getMessage()), ex);
            return "外呼短信即回调，记录落库失败(insert b_sms_callback_at_once fail)!";
        }

        return "success";
    }

    private String checkValues(SmsRecordDTO dto) {
        if(StringUtils.isEmpty(dto.getApiCode())){
            return "apiCode为空";
        }
        if(StringUtils.isEmpty(dto.getCid())){
            return "cid为空";
        }
        if(StringUtils.isEmpty(dto.getThirdCallNo())){
            return "短信流水号 thirdCallNo 为空";
        }
        if(StringUtils.isEmpty(dto.getCaseNum())){
            return "案件编号 caseNum 为空";
        }
        if(StringUtils.isEmpty(dto.getSmsSendStatus())){
            return "短信发送状态 smsSendStatus 为空";
        }
        return "";
    }

    private String paramOfValidity(CallRecordDTO dto) {
        //taskid、caseNum、CID、apicode，sessionId；
        if (ObjectUtils.isEmpty(dto)) {
            log.warn("dto数据为null！");
            return "dto is null!";
        }
        if (StringUtils.isEmpty(dto.getDetail()) || StringUtils.isEmpty(dto.getDetail().getSessionId())) {
            log.warn("taskId={},caseNum={},sessionId={}的数据sessionId缺失！", dto.getTaskId(), dto.getCaseNum(), dto.getDetail().getSessionId());
            return "no param sessionId!";
        }
        if (StringUtils.isEmpty(dto.getApiCode()) || StringUtils.isEmpty(dto.getCid())) {
            log.warn("taskId={},caseNum={},sessionId={}的数据apicode或者cid缺失！", dto.getTaskId(), dto.getCaseNum(), dto.getDetail().getSessionId());
            return "no param apicode or cid！";
        }
        if (StringUtils.isEmpty(dto.getCaseNum())) {
            log.warn("taskId={},caseNum={},sessionId={}的数据caseNum缺失！", dto.getTaskId(), dto.getCaseNum(), dto.getDetail().getSessionId());
            return "no param caseNum!";
        }
        if (StringUtils.isEmpty(dto.getTaskId())) {
            log.warn("taskId={},caseNum={},sessionId={}的数据taskId缺失！", dto.getTaskId(), dto.getCaseNum(), dto.getDetail().getSessionId());
            return "no param taskId!";
        }
        return "true";
    }

    private boolean cuFuJie(CallRecordBO dto, String groupType) throws IllegalAccessException {
        HashMap<String, List<String>> statusMap = marketingCommonConfig.getShuHePushDXStatusMap();
        List<String> status;
        if (statusMap == null
                || (status = statusMap.getOrDefault(groupType, null)) == null) {
            status = Collections.singletonList("c");
        }
        if (status.contains("c") && StringUtils.isNotBlank(dto.getCaseNum())
                && StringUtils.isNotBlank(dto.getDetail().getIntentionGrade())
                && dto.getDetail().getIntentionGrade().contains("A")) {
            Date creatTime = iMarketingSyncUserService.getCreatTimeByCustNumAndUserType(dto.getApiCode()
                    , dto.getCaseNum(), groupType);
            Integer day = handlerService.getShuHePeriodOfValidityDay(groupType);
            Boolean periodOfValidity = iMarketingSyncUserService.isPeriodOfValidity(dto.getCreateTime(), day, creatTime);
            if (!periodOfValidity) {
                return false;
            }
            return periodOfValidityTransform(dto, day, creatTime);
        }
        return false;
    }

    private CaseShuheUser caseShuheUserAdapter(MarketingTransferSyncUser transfer) {
        CaseShuheUser user = new CaseShuheUser();
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.isNotEmpty(reserveField1)) {
            JSONObject object = JSONObject.parseObject(reserveField1);
            user.setIsTurn(object.getString("is_turn"));
            user.setCell(BrCipherMaker.getInstance().decode(object.getString("cell")));
            user.setJsonObject(object);
        }
        user.setUserType(transfer.getUserType());
        user.setApiCode(transfer.getApiCode());
        user.setCustNum(transfer.getCustNum());
        user.setReserveField1(reserveField1);
        return user;
    }

    /**
     * 2022/5/9 17:22
     * 查询有效期内是否存在已转化的数据
     */
    private boolean periodOfValidityTransform(CallRecordBO dto, Integer day, Date creatTime) {
        String cId = redisChgService.get("marketing:api:shuhe:transfer:cid:".concat(dto.getApiCode()));
        String tcId;
        if (StringUtils.isEmpty(cId)) {
            tcId = tableCreateService.getTcId(dto.getApiCode());
        } else {
            tcId = cId.replaceFirst("-", "");
        }
        if (ObjectUtils.isEmpty(creatTime)) {
            creatTime = new Date();
        }
        LocalDateTime dateTime;
        if (day == null) {
            dateTime = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().with(
                    TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(0).atZone(
                    ZoneId.systemDefault()).toLocalDateTime();
        } else {
            dateTime = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusDays(day).withHour(23)
                    .withMinute(59).withSecond(59).withNano(0).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        LocalDateTime time = creatTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay().atZone(ZoneId.systemDefault()).toLocalDateTime();
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.settCid(tcId);
        example.createCriteria().andApiCodeEqualTo(dto.getApiCode())
                .andCustNumEqualTo(dto.getCaseNum())
                .andUserTypeEqualTo(dto.getUserType()).andCreateTimeBetween(
                Date.from(time.atZone(ZoneId.systemDefault()).toInstant())
                , Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()))
                .andIfTransformEqualTo("1");
        int count = marketingTransferSyncUserMapper.countByExample(example);
        return count < 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String callbackDataInsert(String jsonData) {
        try {
            // 解析JSON获取vision版本字段
            JSONObject jsonObject = JSONObject.parseObject(jsonData);
            String version = jsonObject.getString("version");
            if (StringUtils.isEmpty(version)) {
                log.warn("JSON数据中缺少version字段");
                return "lack version";
            }

            // 生成版本明细表名
            String tableName = "b_marketing_call_record_" + version;

            // 判断表是否存在
            boolean tableExists = false;
            try {
                List<Map<String, Object>> tableInfo = marketingCallRecordVersionMapper.checkTableExist(tableName);
                if (tableInfo != null && !tableInfo.isEmpty()) {
                    tableExists = true;
                }
            } catch (Exception e) {
                log.error("表{}不存在，需要创建", tableName);
                tableExists = false;
            }

            // 如果表不存在，解析数据结构生成CREATE语句并执行
            // 注意：DDL操作（CREATE TABLE）在MySQL中通常是自动提交的，但使用CREATE TABLE IF NOT EXISTS可以避免重复创建
            // 如果后续DML操作失败，虽然DDL已提交，但DML操作会回滚，保证数据一致性
            if (!tableExists) {
                String createSql = buildCreateTableSqlByJson(tableName, jsonObject);
                marketingCallRecordVersionMapper.createTable(createSql);
                log.warn("创建版本明细表成功：{}", tableName);
            }

            // 解析数据结构，获取sessionId
            String sessionId = getSessionIdFromJson(jsonObject);
            if (StringUtils.isEmpty(sessionId)) {
                log.error("JSON数据中缺少sessionId字段");
                return "lack sessionId";
            }

            // 判断数据是否存在
            Integer count = marketingCallRecordVersionMapper.countBySessionId(tableName, sessionId);
            if (count != null && count > 0) {
                log.warn("数据已存在，sessionId={}", sessionId);
                return "The data already exists";
            }

            // 生成insert语句并执行插入
            String insertSql = buildInsertSqlByJson(tableName, jsonObject);
            marketingCallRecordVersionMapper.insertData(insertSql);
            log.warn("插入版本明细表成功，tableName={}, sessionId={}", tableName, sessionId);

            // 判断version版本是不是 LLMResultV2
            if ("LLMResultV2".equals(version)) {
                // 构建CallRecording实体对象
                CallRecording callRecording = buildCallRecordingEntity(jsonObject, sessionId);
                callRecordingMapper.insertSelective(callRecording);
                log.warn("插入记录表成功，sessionId={}，插入ID={}", sessionId, callRecording.getId());
            }
            return "success";
        } catch (Exception ex) {
            log.error("回调数据入库失败，错误信息：{}", ex.getMessage(), ex);
            // 重新抛出异常，确保事务回滚所有DML操作
            throw ex;
        }
    }

    /**
     * 从JSON中获取sessionId，优先从外层获取，如果没有则从detail中获取
     */
    private String getSessionIdFromJson(JSONObject jsonObject) {
        String sessionId = jsonObject.getString("sessionId");
        if (StringUtils.isEmpty(sessionId)) {
            Object detailObj = jsonObject.get("detail");
            if (detailObj instanceof JSONObject) {
                JSONObject detail = (JSONObject) detailObj;
                sessionId = detail.getString("sessionId");
            }
        }
        return sessionId;
    }

    /**
     * 根据JSON动态构建建表SQL（不能写死字段，只能解析接口入参去生成表结构）
     */
    private String buildCreateTableSqlByJson(String tableName, JSONObject jsonObject) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (");
        sql.append("`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',");

        // 用于记录已添加的列名，避免重复
        Set<String> addedColumns = new HashSet<>();
        addedColumns.add("id");

        // 遍历JSON中的所有字段，动态生成表结构
        for (String key : jsonObject.keySet()) {

            Object value = jsonObject.get(key);
            String columnName = camelToSnake(key);
            
            // 如果detail字段是JSONObject，需要展开其内部字段
            if ("detail".equals(key) && value instanceof JSONObject) {
                JSONObject detailObj = (JSONObject) value;
                
                // 先添加detail字段本身（json类型）
                if (!addedColumns.contains(columnName)) {
                    sql.append("`").append(columnName).append("` json DEFAULT NULL COMMENT '拨打明细详情',");
                    addedColumns.add(columnName);
                }
                
                // 遍历detail里的所有字段，作为独立列添加
                for (String detailKey : detailObj.keySet()) {
                    Object detailValue = detailObj.get(detailKey);
                    String detailColumnName = camelToSnake(detailKey);
                    
                    // 避免与外层字段冲突，如果冲突则跳过（外层字段优先）
                    if (!addedColumns.contains(detailColumnName)) {
                        String columnDefinition = getColumnDefinition(detailKey, detailValue);
                        sql.append("`").append(detailColumnName).append("` ").append(columnDefinition).append(",");
                        addedColumns.add(detailColumnName);
                    }
                }
            } else {
                // 普通字段处理
                if (!addedColumns.contains(columnName)) {
                    String columnDefinition = getColumnDefinition(key, value);
                    sql.append("`").append(columnName).append("` ").append(columnDefinition).append(",");
                    addedColumns.add(columnName);
                }
            }
        }
        // 添加固定字段
        if (!addedColumns.contains("create_time")) {
            sql.append("`create_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',");
        }
        if (!addedColumns.contains("update_time")) {
            sql.append("`update_time` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',");
        }
        sql.append("PRIMARY KEY (`id`)");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='通话回调记录表'");

        return sql.toString();
    }

    /**
     * 根据字段名和值推断数据库字段类型定义
     */
    private String getColumnDefinition(String fieldName, Object value) {
        if (value == null) {
            // 如果值为null，默认使用varchar(255)
            return "varchar(255) DEFAULT NULL";
        }

        // 根据字段名特殊处理
        if ("detail".equals(fieldName) || fieldName.toLowerCase().contains("detail")) {
            return "json DEFAULT NULL COMMENT '拨打明细详情'";
        }

        // 根据值的类型推断
        if (value instanceof String) {
            String strValue = (String) value;
            int length = strValue.length();
            if (length > 1000) {
                return "text DEFAULT NULL";
            } else if (length > 255) {
                return "varchar(1000) DEFAULT NULL";
            } else {
                return "varchar(255) DEFAULT NULL";
            }
        } else if (value instanceof Number) {
            Number numValue = (Number) value;
            // 判断是整数还是小数
            if (numValue.doubleValue() == numValue.longValue()) {
                // 整数
                long longValue = numValue.longValue();
                if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
                    return "bigint(20) DEFAULT NULL";
                } else {
                    return "int(11) DEFAULT NULL";
                }
            } else {
                // 小数
                return "decimal(18,2) DEFAULT NULL";
            }
        } else if (value instanceof Boolean) {
            return "int(1) DEFAULT NULL";
        } else if (value instanceof JSONObject || value instanceof Map) {
            return "json DEFAULT NULL";
        } else if (value instanceof List) {
            return "text DEFAULT NULL";
        } else {
            // 默认使用text
            return "text DEFAULT NULL";
        }
    }

    /**
     * 驼峰命名转下划线命名
     */
    private String camelToSnake(String camelCase) {
        if (StringUtils.isEmpty(camelCase)) {
            return camelCase;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 根据JSON动态构建插入SQL
     */
    private String buildInsertSqlByJson(String tableName, JSONObject jsonObject) {
        StringBuilder sql = new StringBuilder();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        // 用于记录已添加的列名，避免重复
        Set<String> addedColumns = new HashSet<>();

        // 遍历JSON中的所有字段，生成INSERT语句
        for (String key : jsonObject.keySet()) {

            Object value = jsonObject.get(key);
            
            // 如果detail字段是JSONObject，需要展开其内部字段
            if ("detail".equals(key) && value instanceof JSONObject) {
                JSONObject detailObj = (JSONObject) value;
                
                // 先添加detail字段本身（json类型）
                String columnName = camelToSnake(key);
                if (!addedColumns.contains(columnName)) {
                    columns.append("`").append(columnName).append("`,");
                    // JSON对象转为JSON字符串
                    String jsonStr = JSON.toJSONString(value);
                    jsonStr = jsonStr.replace("\\", "\\\\").replace("'", "\\'");
                    values.append("'").append(jsonStr).append("',");
                    addedColumns.add(columnName);
                }
                
                // 遍历detail里的所有字段，作为独立列插入
                for (String detailKey : detailObj.keySet()) {
                    Object detailValue = detailObj.get(detailKey);
                    String detailColumnName = camelToSnake(detailKey);
                    
                    // 避免与外层字段冲突，如果冲突则跳过（外层字段优先）
                    if (!addedColumns.contains(detailColumnName) && detailValue != null) {
                        columns.append("`").append(detailColumnName).append("`,");
                        appendValue(values, detailValue);
                        addedColumns.add(detailColumnName);
                    }
                }
            } else if (value != null) {
                // 普通字段处理
                String columnName = camelToSnake(key);
                if (!addedColumns.contains(columnName)) {
                    columns.append("`").append(columnName).append("`,");
                    appendValue(values, value);
                    addedColumns.add(columnName);
                }
            }
        }

        // 移除最后的逗号
        if (columns.length() > 0 && columns.charAt(columns.length() - 1) == ',') {
            columns.setLength(columns.length() - 1);
        }
        if (values.length() > 0 && values.charAt(values.length() - 1) == ',') {
            values.setLength(values.length() - 1);
        }

        sql.append("INSERT INTO `").append(tableName).append("` (");
        sql.append(columns);
        sql.append(") VALUES (");
        sql.append(values);
        sql.append(")");

        return sql.toString();
    }

    /**
     * 追加值到values字符串
     */
    private void appendValue(StringBuilder values, Object value) {
        if (value instanceof String) {
            String strValue = (String) value;
            // 转义单引号和反斜杠，防止SQL注入
            strValue = strValue.replace("\\", "\\\\").replace("'", "\\'");
            values.append("'").append(strValue).append("',");
        } else if (value instanceof Number || value instanceof Boolean) {
            values.append(value).append(",");
        } else if (value instanceof JSONObject || value instanceof Map) {
            // JSON对象转为JSON字符串
            String jsonStr = JSON.toJSONString(value);
            jsonStr = jsonStr.replace("\\", "\\\\").replace("'", "\\'");
            values.append("'").append(jsonStr).append("',");
        } else if (value instanceof List) {
            // List转为JSON字符串
            String jsonStr = JSON.toJSONString(value);
            jsonStr = jsonStr.replace("\\", "\\\\").replace("'", "\\'");
            values.append("'").append(jsonStr).append("',");
        } else {
            // 其他类型转为字符串
            String strValue = String.valueOf(value);
            strValue = strValue.replace("\\", "\\\\").replace("'", "\\'");
            values.append("'").append(strValue).append("',");
        }
    }

    /**
     * 构建CallRecording实体对象
     */
    private CallRecording buildCallRecordingEntity(JSONObject jsonObject, String sessionId) {
        CallRecording callRecording = new CallRecording();
        
        // 用于记录已处理的字段，避免重复
        Set<String> processedFields = new HashSet<>();

        // 遍历JSON中的所有字段，设置实体属性
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            
            // 如果detail字段是JSONObject，需要展开其内部字段
            if ("detail".equals(key) && value instanceof JSONObject) {
                JSONObject detailObj = (JSONObject) value;
                
                // 设置detail字段本身（转换为JSON字符串，MySQL JSON字段需要字符串格式）
                if (!processedFields.contains("detail")) {
                    String detailJsonStr = JSON.toJSONString(value);
                    callRecording.setDetail(detailJsonStr);
                    processedFields.add("detail");
                }
                
                // 遍历detail里的所有字段，设置对应的实体属性
                for (String detailKey : detailObj.keySet()) {
                    Object detailValue = detailObj.get(detailKey);
                    // 将字段名转换为驼峰命名（支持驼峰和下划线两种格式）
                    String fieldName = normalizeFieldName(detailKey);
                    
                    // 避免与外层字段冲突，如果冲突则跳过（外层字段优先）
                    if (!processedFields.contains(fieldName) && detailValue != null) {
                        setFieldValue(callRecording, fieldName, detailValue);
                        processedFields.add(fieldName);
                    }
                }
            } else if ("detail".equals(key) && value != null) {
                // detail字段是其他类型（如字符串），直接设置
                if (!processedFields.contains("detail")) {
                    // 如果是字符串，直接使用（假设已经是有效的JSON字符串）
                    // 如果是其他对象，转换为JSON字符串
                    if (value instanceof String) {
                        callRecording.setDetail(value);
                    } else {
                        // 非字符串类型，转换为JSON字符串
                        callRecording.setDetail(JSON.toJSONString(value));
                    }
                    processedFields.add("detail");
                }
            } else if (value != null) {
                // 普通字段处理
                String fieldName = normalizeFieldName(key);
                if (!processedFields.contains(fieldName)) {
                    setFieldValue(callRecording, fieldName, value);
                    processedFields.add(fieldName);
                }
            }
        }

        // 确保sessionId字段存在（如果JSON中没有，使用传入的参数）
        if (StringUtils.isEmpty(callRecording.getSessionId()) && StringUtils.isNotEmpty(sessionId)) {
            callRecording.setSessionId(sessionId);
        }

        // 设置status字段（默认0）
        if (callRecording.getStatus() == null) {
            callRecording.setStatus(0);
        }

        // 设置receive_date（当日）
        if (StringUtils.isEmpty(callRecording.getReceiveDate())) {
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            callRecording.setReceiveDate(currentDate);
        }

        // 设置创建时间
        if (callRecording.getCreateTime() == null) {
            callRecording.setCreateTime(new Date());
        }

        return callRecording;
    }

    /**
     * 设置实体字段值
     */
    private void setFieldValue(CallRecording callRecording, String fieldName, Object value) {
        try {
            if (value == null) {
                return;
            }
            
            // 根据字段名设置对应的属性值
            switch (fieldName) {
                case "cid":
                    callRecording.setCid(value.toString());
                    break;
                case "apiCode":
                    callRecording.setApiCode(value.toString());
                    break;
                case "callBackType":
                    if (value instanceof Number) {
                        callRecording.setCallBackType(((Number) value).intValue());
                    }
                    break;
                case "taskName":
                    callRecording.setTaskName(value.toString());
                    break;
                case "taskId":
                    if (value instanceof Number) {
                        callRecording.setTaskId(((Number) value).intValue());
                    }
                    break;
                case "custNum":
                    callRecording.setCustNum(value.toString());
                    break;
                case "callStartTime":
                    if (value instanceof Number) {
                        callRecording.setCallStartTime(((Number) value).longValue());
                    }
                    break;
                case "callConnectTime":
                    if (value instanceof Number) {
                        callRecording.setCallConnectTime(((Number) value).longValue());
                    }
                    break;
                case "callEndTime":
                    if (value instanceof Number) {
                        callRecording.setCallEndTime(((Number) value).longValue());
                    }
                    break;
                case "dialogTurn":
                    if (value instanceof Number) {
                        callRecording.setDialogTurn(((Number) value).intValue());
                    }
                    break;
                case "callStatus":
                    if (value instanceof Number) {
                        callRecording.setCallStatus(((Number) value).intValue());
                    }
                    break;
                case "isConnect":
                    if (value instanceof Number) {
                        callRecording.setIsConnect(((Number) value).intValue());
                    }
                    break;
                case "callDialog":
                    callRecording.setCallDialog(value.toString());
                    break;
                case "recordingPath":
                    callRecording.setRecordingPath(value.toString());
                    break;
                case "intentionGrade":
                    callRecording.setIntentionGrade(value.toString());
                    break;
                case "tagList":
                    callRecording.setTagList(value.toString());
                    break;
                case "reserveField1":
                    callRecording.setReserveField1(value.toString());
                    break;
                case "version":
                    callRecording.setVersion(value.toString());
                    break;
                default:
                    // 忽略未知字段
                    break;
            }
        } catch (Exception e) {
            log.warn("设置字段{}的值失败：{}", fieldName, e.getMessage());
        }
    }

    /**
     * 规范化字段名：将字段名统一转换为驼峰命名
     * 如果字段名已经是驼峰命名，保持不变；如果是下划线命名，转换为驼峰命名
     */
    private String normalizeFieldName(String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            return fieldName;
        }
        // 如果包含下划线，说明是下划线命名，需要转换为驼峰命名
        if (fieldName.contains("_")) {
            return snakeToCamel(fieldName);
        }
        // 如果已经是驼峰命名，直接返回（首字母小写）
        if (fieldName.length() > 0 && Character.isLowerCase(fieldName.charAt(0))) {
            return fieldName;
        }
        // 如果首字母是大写，转换为小写（处理特殊情况）
        return Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * 下划线命名转驼峰命名
     */
    private String snakeToCamel(String snakeCase) {
        if (StringUtils.isEmpty(snakeCase)) {
            return snakeCase;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < snakeCase.length(); i++) {
            char c = snakeCase.charAt(i);
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }


}
