package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.constants.rocketmq.MarketingDelayedConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.dto.msg.mq.ApiDataInfoDTO;
import com.br.marketing.dto.msg.mq.UserTypeCollectionDTO;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingDataValidConfigDefaultMapper;
import com.br.marketing.mapper.VariableDicMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.VariableDicService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.VariableDicListVO;
import com.br.marketing.vo.VariableDicSelectVO;
import com.br.marketing.webhook.dingding.msgtype.At;
import com.br.marketing.webhook.dingding.msgtype.DingDingTextMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import com.github.pagehelper.PageHelper;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客户配置变量值字典
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 17:29
 */
@Service
@Slf4j
public class VariableDicServiceImpl implements VariableDicService {

    @Autowired
    EntityOptServiceImpl entityOptService;

    @Resource
    private VariableDicMapper variableDicMapper;


    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private IPeriodOfValidityService periodOfValidityService;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    @Resource
    private RabbitMqProducter producter;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private MarketingDataValidConfigDefaultMapper validConfigDefaultMapper;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    TrackingService trackingService;

    @Override
    public List<VariableDicSelectVO> findListByCidAndApiCode(String cid, String apiCode) {
        VariableDicExample example = new VariableDicExample();
        example.createCriteria().andCidEqualTo(cid).andApiCodeEqualTo(apiCode).andIsDelEqualTo(1);
        example.setOrderByClause("create_time desc, update_time desc");
        List<VariableDic> variableDics = variableDicMapper.selectByExample(example);
        if (ObjectUtils.isEmpty(variableDics)) {
            return Collections.emptyList();
        }
        return variableDics.stream().map(v -> new VariableDicSelectVO(
                v.getFieldName(), v.getFieldValue(), v.getFieldDesc())).collect(Collectors.toList());
    }

    @Override
    public PageResultReturn getVariableDicList(int page, int pageSize, String cid, String apiCode) {
        PageHelper.startPage(page, pageSize);
        try {
            List<VariableDicListVO> list = variableDicMapper.getVariableDicList(cid, apiCode);
            for (VariableDicListVO variableDicListVO : list) {
                apiCode = variableDicListVO.getApiCode();
                String userType = null;
                if ("userType".equals(variableDicListVO.getFieldName())) {
                    userType = variableDicListVO.getFieldValue();
                }
                MarketingDataValidConfigDefaultExample exampleConfig = new MarketingDataValidConfigDefaultExample();
                exampleConfig.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType).andIsDelEqualTo(1);
                exampleConfig.setOrderByClause("create_time DESC limit 1");
                List<MarketingDataValidConfigDefault> validConfigDefaultList = validConfigDefaultMapper.selectByExample(exampleConfig);
                if (!CollectionUtils.isEmpty(validConfigDefaultList)) {
                    MarketingDataValidConfigDefault configDefault = validConfigDefaultList.get(0);
                    if (configDefault.getValidType().equals(0)) {
                        variableDicListVO.setValidDaysDefault("T+" + configDefault.getValidDaysDefault());
                    } else {
                        variableDicListVO.setValidDaysDefault(configDefault.getValidDaysDefault().toString());
                    }
                    variableDicListVO.setValidType(configDefault.getValidType());
                } else {
                    log.warn("不存在有效期天数配置,apiCode={},userType={}", apiCode, userType);
                }
            }
            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(), e.getMessage()), e);
        }
        return null;
    }

    @Override
    public ApiResult<Boolean> saveOrUpdateVariableDic(VariableDicListVO vo, MarketingUserDetail user) {
        String apiCode, userType = null;
        apiCode = vo.getApiCode();
        if (vo.getValidDaysDefault() == null) {
            vo.setValidDaysDefault("0");
        }
        VariableDic variableDic = new VariableDic();
        variableDic.setFieldName(vo.getFieldName());
        variableDic.setFieldValue(vo.getFieldValue());
        variableDic.setFieldDesc(vo.getFieldDesc());
        variableDic.setIsDel(vo.getIsDel());
        variableDic.setUpdateTime(new Date());
        MarketingDataValidConfigDefault validConfigDefault = new MarketingDataValidConfigDefault();
        if ("userType".equals(vo.getFieldName())) {
            userType = vo.getFieldValue();
            validConfigDefault.setUserType(userType);
        }
        validConfigDefault.setValidDaysDefault(Integer.valueOf(vo.getValidDaysDefault()));
        validConfigDefault.setIsDel(vo.getIsDel());
        validConfigDefault.setValidType(vo.getValidType());
        if (StringUtils.isEmpty(vo.getId())) {
            //新增
            variableDic.setCid(vo.getCid());
            variableDic.setApiCode(apiCode);
            variableDic.setCreateTime(new Date());
            variableDicMapper.insertSelective(variableDic);
            entityOptService.writeOptLog(variableDic.getId(), variableDic, null);
            MarketingDataValidConfigDefaultExample exampleConfig = new MarketingDataValidConfigDefaultExample();
            exampleConfig.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType).andIsDelEqualTo(1);
            exampleConfig.setOrderByClause("create_time DESC");
            List<MarketingDataValidConfigDefault> validConfigDefaultList = validConfigDefaultMapper.selectByExample(exampleConfig);
            if (!CollectionUtils.isEmpty(validConfigDefaultList)) {
                if (validConfigDefaultList.size() > 1) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode()
                            , "默认有效期配置表存在多条配置;apiCode:" + apiCode));
                }
                validConfigDefault.setId(validConfigDefaultList.get(0).getId());
                validConfigDefault.setApiCode(apiCode);
                validConfigDefault.setUpdateTime(new Date());
                validConfigDefaultMapper.updateByPrimaryKeySelective(validConfigDefault);
                entityOptService.writeOptLog(validConfigDefaultList.get(0).getId(), validConfigDefault, validConfigDefaultList.get(0));
                return new ApiResult<Boolean>().success(true);
            }
            validConfigDefault.setApiCode(apiCode);
            validConfigDefault.setCreateTime(new Date());
            validConfigDefaultMapper.insertSelective(validConfigDefault);
            entityOptService.writeOptLog(validConfigDefault.getId(), validConfigDefault, null);
        } else {
            VariableDic data = variableDicMapper.selectByPrimaryKey(vo.getId());
            //编辑
            variableDic.setId(vo.getId());
            variableDicMapper.updateByPrimaryKeySelective(variableDic);
            entityOptService.writeOptLog(vo.getId(), variableDic, data);
            MarketingDataValidConfigDefaultExample exampleConfig = new MarketingDataValidConfigDefaultExample();
            exampleConfig.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType).andIsDelEqualTo(1);
            exampleConfig.setOrderByClause("create_time DESC");
            List<MarketingDataValidConfigDefault> validConfigDefaultList = validConfigDefaultMapper.selectByExample(exampleConfig);
            if (!CollectionUtils.isEmpty(validConfigDefaultList)) {
                if (validConfigDefaultList.size() > 1) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode()
                            , "默认有效期配置表存在多条配置;apiCode:" + apiCode));
                }
                validConfigDefault.setId(validConfigDefaultList.get(0).getId());
                validConfigDefault.setApiCode(apiCode);
                validConfigDefault.setUpdateTime(new Date());
                validConfigDefaultMapper.updateByPrimaryKeySelective(validConfigDefault);
                entityOptService.writeOptLog(validConfigDefaultList.get(0).getId(), validConfigDefault, validConfigDefaultList.get(0));
            } else {
                MarketingDataValidConfigDefault configDefault = new MarketingDataValidConfigDefault();
                configDefault.setApiCode(apiCode);
                configDefault.setUserType(userType);
                configDefault.setValidDaysDefault(Integer.valueOf(vo.getValidDaysDefault()));
                configDefault.setCreateTime(data.getCreateTime());
                configDefault.setUpdateTime(new Date());
                configDefault.setIsDel(1);
                configDefault.setValidType(vo.getValidType());
                validConfigDefaultMapper.insertSelective(configDefault);
                entityOptService.writeOptLog(configDefault.getId(), configDefault, null);
            }
        }

        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public List<Map> findListByCidsAndApiCodes(List<CustomerSelectVO> vos) {
        List<Map> list = new ArrayList<>();
        if(CollectionUtils.isEmpty(vos)){
            return list;
        }
        List<String> apiCodes = vos.stream().map(CustomerSelectVO::getApiCode).collect(Collectors.toList());
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeIn(apiCodes).andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        for (CustomerSelectVO vo : vos) {
            String cid = vo.getCid();
            String apiCode = vo.getApiCode();
            List<VariableDicSelectVO> userTypeList = findListByCidAndApiCode(cid, apiCode);

            Map map = new HashMap();
            map.put("cid", cid);
            map.put("apiCode", apiCode);
            map.put("userTypeList", userTypeList);
            Optional<MarketingCustomer> first = marketingCustomers.stream()
                    .filter(t -> apiCode.equals(t.getApiCode()) && cid.equals(t.getCid())).findFirst();
            if (first.isPresent()) {
                MarketingCustomer marketingCustomer = first.get();
                map.put("name", marketingCustomer.getName());
                map.put("shortName", marketingCustomer.getShortName());
            }
            list.add(map);
        }

        return list;
    }

    @Override
    public Result<Boolean> batchAddUserTypeVariableDicTry(String msgStr) {
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(false);
        if (StringUtils.isEmpty(msgStr)) {
            return result;
        }
        try {
            ApiDataInfoDTO<UserTypeCollectionDTO> apiDataInfoDTO = JSONObject.parseObject(msgStr
                    , new TypeReference<ApiDataInfoDTO<UserTypeCollectionDTO>>() {
                    }.getType());
            String apiCode = apiDataInfoDTO.getApiCode();
            if (StringUtils.isEmpty(apiCode)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode()
                        , "未获取到apiCode，消息内容："+ msgStr));
                return result;
            }
            if (CollectionUtils.isEmpty(apiDataInfoDTO.getArgList())) {
                log.warn("场景内容新增中，新增数量为0，消息内容：{}", msgStr);
                return result;
            }
            String cId = StringUtils.hasText(apiDataInfoDTO.getCid()) ? apiDataInfoDTO.getCid()
                    : tableCreateService.getCId(apiCode);
            if (StringUtils.isEmpty(cId)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode()
                        , "未获取到cid，消息内容："+ msgStr));
                return result;
            }
            String key = RedisKeyConstant.USERTYPE_DICT + cId.concat(":").concat(apiCode);
            String fieldName = "userType";
            for (UserTypeCollectionDTO collectionDTO : apiDataInfoDTO.getArgList()) {
                String userType = collectionDTO.getUserType();
                LocalDateTime localDateTime = LocalDateTime.now();
                String redisKey = key.concat(":") + (userType);
                boolean exists = true;
                boolean isError = false;
                try {
                    // 添加缓存
                    exists = redisChgService.lock(redisKey, apiDataInfoDTO.getRawDataSaveDateStr(), RandomUtils.nextLong(
                            3600 * 24 * 3 * 1000L, 3600 * 24 * 7 * 1000L));
                } catch (Exception e) {
                    isError = true;
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(), e.getMessage()), e);
                }
                if (exists) {
                    // 缓存不存在，检查db中是否存在
                    VariableDicExample variableDicExample = new VariableDicExample();
                    variableDicExample.createCriteria().andCidEqualTo(cId).andApiCodeEqualTo(apiCode)
                            .andFieldNameEqualTo(fieldName).andFieldValueEqualTo(userType).andIsDelEqualTo(1);
                    if (isError) {
                        variableDicExample.setOrderByClause("id for update");
                    }
                    VariableDic dic = transactionTemplate.execute((TransactionStatus status) -> {
                        int count = variableDicMapper.countByExample(variableDicExample);
                        if (count < 1) {
                            LocalDateTime parseTime = LocalDateTime.parse(apiDataInfoDTO.getRawDataSaveTimeStr()
                                    , DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            // db中不存在，添加记录
                            VariableDic variableDic = new VariableDic();
                            variableDic.setFieldName(fieldName);
                            variableDic.setFieldValue(userType);
                            variableDic.setFieldDesc("");
                            variableDic.setIsDel(1);
                            variableDic.setCreateTime(Date.from(parseTime.atZone(ZoneId.systemDefault()).toInstant()));
                            variableDic.setUpdateTime(variableDic.getCreateTime());
                            variableDic.setCid(cId);
                            variableDic.setApiCode(apiCode);
                            variableDic.setFieldValueSource(apiDataInfoDTO.getMsgSource());
                            variableDicMapper.insertSelective(variableDic);
                            //region 埋点
                            try{
                                trackingService.trackPointLog(DataFlowDirection.IN
                                        , apiCode
                                        , "添加新的场景"
                                        , 1L
                                        , String.format("场景表id：%d",variableDic.getId())
                                        , TrackingContext.generateBatchId());
                            }catch (Exception ex){
                                log.warn(
                                        AlertLog.buildWarnMessage(
                                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                                , ex.getMessage()
                                                , "埋点异常")
                                        , ex);
                            }
                            //endregion
                            return variableDic;
                        }
                        return null;
                    });
                    if (dic != null) {
                        if (dic.getId() != null && dic.getId() > 0) {
                            // 发送告警通知
                            sendUserTypeAddDingDingMgs(localDateTime, apiCode, userType, String.valueOf(dic.getId()));
                        } else {
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(),
                                    "自动化场景维护入库失败,cid:" + cId + ",apiCode:" + apiCode + ",userType:" + userType
                                            + ",上传时间:" + apiDataInfoDTO.getRawDataSaveTimeStr() + ",数据来源:" + apiDataInfoDTO.getMsgSource()));
                        }
                    }
                }
                // 自动生成有效期
                createValidDateConfig(apiDataInfoDTO, collectionDTO, apiCode, userType);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(), e.getMessage() + "\n" + msgStr), e);
        }
        return result;
    }

    /**
     * 2024-03-02 14:02
     * 新增场景告警
     *
     * @param localDateTime 新增场景的时间
     * @param apiCode       客户编号
     * @param userType      场景
     */
    private void sendUserTypeAddDingDingMgs(LocalDateTime localDateTime, String apiCode, String userType, String id) {
        try {
            Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
            Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.USERTYPE_ADD_SENDUSERTYPEADDDINGDINGMGS
                    .toString());
            if (CollectionUtils.isEmpty(map) || apiCode.startsWith("7")) {
                return;
            }
            // 添加新增场景通知信息
            LocalTime startParse = LocalTime.parse(map.getOrDefault("startTime", "18:00").toString());
            LocalTime endParse = LocalTime.parse(map.getOrDefault("endTime", "10:00").toString());
            LocalTime localTime = localDateTime.toLocalTime();
            // 当开始startTime在endTime之后时表示定时发送
            int priority = 0;
            if (endParse.isBefore(startParse)) {
                String key;
                long ttl;
                int delayLevel;
                if (localTime.isBefore(startParse) || localTime.equals(startParse)) {
                    // T日定时发送消息
                    key = RedisKeyConstant.USERTYPE_DICT.concat("delay:mgs:today:").concat(startParse.toString())
                            .concat(":").concat(localDateTime.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE));
                    ttl = ChronoUnit.MILLIS.between(localDateTime, localDateTime.toLocalDate().atTime(startParse)
                            .atZone(ZoneId.systemDefault()));
//                    delayLevel = ;
                } else {
                    // T+1日延时定时发送消息
                    key = RedisKeyConstant.USERTYPE_DICT.concat("delay:mgs:tomorrow:").concat(endParse.toString())
                            .concat(":").concat(localDateTime.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE));
                    ttl = ChronoUnit.MILLIS.between(localDateTime, localDateTime.toLocalDate().plusDays(1)
                            .atTime(endParse).atZone(ZoneId.systemDefault()));
//                    delayLevel = ;
                }
                redisChgService.lock(key.concat(":lock"), id);
                Boolean exists = redisChgService.exists(key);
                if (!exists) {
                    // 不存在添加延迟队列
                    if(rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingDelayedConstants.TAG_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE)){
                        rocketMqSwitch.syncSendDelaySecond(MarketingDelayedConstants.TOPIC
                                , MarketingDelayedConstants.TAG_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE, key,(int)ttl/1000);
                    }else{
                        producter.sendByExpiration(MQConstants.ROUTING_KEY_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE, key
                                , String.valueOf(ttl), priority);
                    }
                }
                // 缓存批量结果
                redisChgService.saddMember(key, apiCode.concat("  ") + (userType));
                if (!exists) {
                    // 设置过期时间
                    redisChgService.expire(key, 3600 * 25);
                }
                redisChgService.unlock(key.concat(":lock"), id);
                return;
            }
            // 当开始startTime在endTime之前时表示在startTime与endTime闭区间内实时发送，区间外定时发送
            boolean isRealTimeSend = (localTime.isAfter(startParse) || localTime.equals(startParse))
                    && (localTime.isBefore(endParse) || localTime.equals(endParse));
            if (isRealTimeSend) {
                String content = ("新增场景通知 " + LocalDate.now() + "\napiCode  userType\n".concat(apiCode).concat("  "
                        + (userType)).concat("\n"));
                sendDingDingTextMessage(content, map);
            } else {
                // T+1日延时定时发送消息
                int day = (LocalTime.MIN.isBefore(localTime) || LocalTime.MIN.equals(localTime)) && startParse
                        .isAfter(localTime) ? 0 : 1;
                String key = RedisKeyConstant.USERTYPE_DICT.concat("delay:mgs:" + day + ":").concat(startParse.toString())
                        .concat(":").concat(localDateTime.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE));
                redisChgService.lock(key.concat(":lock"), id);
                Boolean exists = redisChgService.exists(key);
                if (!exists) {
                    long ttl = ChronoUnit.MILLIS.between(localDateTime, localDateTime.toLocalDate()
                            .plusDays(day).atTime(startParse).atZone(ZoneId.systemDefault()));
                    // 不存在添加延迟队列
                    if(rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingDelayedConstants.TAG_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE)){
                        rocketMqSwitch.syncSendDelaySecond(MarketingDelayedConstants.TOPIC
                                , MarketingDelayedConstants.TAG_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE, key,(int)ttl/1000);
                    }else{
                        producter.sendByExpiration(MQConstants.ROUTING_KEY_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE,
                                key, String.valueOf(ttl), priority);
                    }
                }
                // 缓存批量结果
                redisChgService.saddMember(key, apiCode.concat("  " + userType));
                if (!exists) {
                    // 设置过期时间
                    redisChgService.expire(key, 3600 * 25);
                }
                redisChgService.unlock(key.concat(":lock"), id);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(),
                    e.getMessage() + "\napiCode:" + apiCode + ";userType:" + userType), e);
        }
    }

    /**
     * 2024-03-02 16:17
     * 创建有效期
     * <p>
     * <p>
     * 生成有效期的数据为上传数据且状态为非剔除的数据
     *
     * @param apiDataInfoDTO 消息源
     * @param apiCode        客户编号
     * @param userType       场景
     */
    private void createValidDateConfig(ApiDataInfoDTO<UserTypeCollectionDTO> apiDataInfoDTO
            , UserTypeCollectionDTO collectionDTO, String apiCode, String userType) {
        boolean bool = apiDataInfoDTO.transferMsgSource() || (apiDataInfoDTO.uploadMsgSource()
                && collectionDTO.getStatus() == MonitorTypeEnum.STATUS_2.getTypeCode());
        if (bool) {
            // 转化数据或上传数据状态为提出的数据不生产有效期配置
            return;
        }
        Set<String> apiCodes = marketingCommonConfig.getNonConfigValidDefaultApiCodes();
        if ((apiCodes != null && apiCodes.contains(apiCode))) {
            return;
        }
        LocalDateTime parseTime = LocalDateTime.parse(apiDataInfoDTO.getRawDataSaveTimeStr()
                , DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        MarketingSyncUser marketingSyncUser = new MarketingSyncUser();
        marketingSyncUser.setUserType(userType);
        marketingSyncUser.setApiCode(apiCode);
        marketingSyncUser.setAppletDate(parseTime.toLocalDate().toString());
        String basicDate = parseTime.format(DateTimeFormatter.BASIC_ISO_DATE);

        HashMap<String, String> mappingConfig = marketingCommonConfig.getThirdPartnerApiCodeMappingConfig();
        if (mappingConfig.containsValue(apiCode)) {
            configValidDateDefault(marketingSyncUser
                    , syncUser -> apiCode.concat(":" + userType).concat(":" + basicDate)
                    , syncUser -> periodOfValidityService.generateConfigValidByStartAndEndDate(syncUser));
            return;
        }

        // 通用生成有效期
        configValidDateDefault(marketingSyncUser
                , syncUser -> apiCode.concat(":" + userType).concat(":" + basicDate)
                , syncUser -> periodOfValidityService.configValidDateDefault(syncUser));
        // 定制生成有效期
        if (marketingCommonConfig.getCustomizeConfigValidDefaultApiCodes().contains(apiCode)
                && StringUtils.hasText(collectionDTO.getTaskId())) {
            marketingSyncUser.setCusBatch(collectionDTO.getTaskId());
            configValidDateDefault(marketingSyncUser
                    , syncUser -> apiCode.concat(":" + userType).concat(":" + syncUser.getCusBatch())
                            .concat(":" + basicDate)
                    , syncUser -> periodOfValidityService.customizeConfigValidDateDefault(syncUser));
        }
    }

    /**
     * 2024-02-29 15:52
     * 配置默认有效期
     *
     * @param syncUser 上传数据
     */
    private void configValidDateDefault(MarketingSyncUser syncUser
            , Function<MarketingSyncUser, String> functionRedisKey, Function<MarketingSyncUser, Result<Boolean>> function) {
        try {
            // 遍历缓存中需要设置默认有效期的apiCode与userType
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime localDateTime = now.plusDays(1);
            ZonedDateTime zonedDateTime = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault());
            String key = RedisKeyConstant.prefix.concat("valid:lock:") + functionRedisKey.apply(syncUser);
            boolean lock;
            try {
                // 将主键保存到锁的key中
                lock = redisChgService.lock(key, syncUser.getUserType(), ChronoUnit.MILLIS.between(now, zonedDateTime));
                syncUser.setStatus(MonitorTypeEnum.STATUS_1.getTypeCode());
            } catch (Exception e) {
                lock = true;
                syncUser.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(), "设置默认有效期,上锁失败key:" + key), e);
            }
            if (lock) {
                function.apply(syncUser);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(),
                    "ApiCode:" + syncUser.getApiCode() + ";UserType:" + syncUser.getUserType() + ";TaskId:" + syncUser.getCusBatch()), e);
        }
    }

    @Override
    public Result<Boolean> delaySendUserTypeMessage(String redisKey) {
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(false);
        if (StringUtils.isEmpty(redisKey)) {
            return result;
        }
        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.USERTYPE_ADD_SENDUSERTYPEADDDINGDINGMGS
                .toString());
        if (CollectionUtils.isEmpty(map)) {
            log.warn("场景告警redis主键：{}", redisKey);
            return result;
        }
        try {
            Set<String> userTypeSet = redisChgService.smembers(redisKey);
            if (CollectionUtils.isEmpty(userTypeSet)) {
                log.warn("场景告警redis主键{}中不存在内容！", redisKey);
                return result;
            }
            String contentHeld = "新增场景通知 " + LocalDate.now() + "\napiCode  userType\n";
            String content = "";
            int count = 0;
            for (String mgs : userTypeSet) {
                count++;
                content = content.concat(mgs).concat("\n");
                if (count >= 100) {
                    count = 0;
                    sendDingDingTextMessage(contentHeld + content, map);
                }
            }
            if (count > 0) {
                sendDingDingTextMessage(contentHeld + content, map);
            }
            // 清理
            redisChgService.delBigSet(redisKey, 500);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(), "redisKey:" + redisKey), e);
        }
        return result;
    }


    /**
     * 2024-03-05 17:47
     * 发送钉钉文本消息
     */
    private void sendDingDingTextMessage(String content, Map<String, Object> sendMgsInfoMap) {
        DingDingTextMessage dingDingTextMessage = new DingDingTextMessage();
        DingDingTextMessage.Text text = new DingDingTextMessage.Text();
        dingDingTextMessage.setText(text);
        JSONArray ats = (JSONArray) sendMgsInfoMap.get("at");
        if (ats != null) {
            At at = new At();
            at.setAtMobiles(ats.toJavaList(String.class));
            dingDingTextMessage.setAt(at);
        }
        text.setContent(content);
        log.warn(dingDingTextMessage.toString());
        // 发送实时消息
        dingDingRobotHookService.sendMessageGroup(sendMgsInfoMap.get("token").toString()
                , sendMgsInfoMap.get("secret").toString()
                , dingDingTextMessage);
    }
}
