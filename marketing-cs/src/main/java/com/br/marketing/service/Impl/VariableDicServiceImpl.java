package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.msg.mq.ApiDataInfoDTO;
import com.br.marketing.dto.msg.mq.UserTypeCollectionDTO;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.mapper.MarketingValidityChangeMapper;
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
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
    private MarketingValidityChangeMapper validityChangeMapper;

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
    private TransactionTemplate transactionTemplate;

    private final static Lock LOCK = new ReentrantLock();


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
            List<VariableDicListVO> list = variableDicMapper.getVariableDicList(cid,apiCode);
            for (VariableDicListVO variableDicListVO : list) {
                apiCode = variableDicListVO.getApiCode();
                String userType = null;
                if ("userType".equals(variableDicListVO.getFieldName())){
                    userType = variableDicListVO.getFieldValue();
                }
                Integer validDaysDefault = validityChangeMapper.selectValidDaysDefault(apiCode, userType);
                if (ObjectUtil.isNotEmpty(validDaysDefault)){
                    variableDicListVO.setValidDaysDefault("T+" + validDaysDefault);
                } else {
                    log.warn("不存在有效期天数配置,apiCode={},userType={}", apiCode, userType);
                }

            }
            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ApiResult<Boolean> saveOrUpdateVariableDic(VariableDicListVO vo, MarketingUserDetail user) {
        String apiCode, userType = null;
        apiCode = vo.getApiCode();
        if (vo.getValidDaysDefault() == null){
            vo.setValidDaysDefault("0");
        }
        VariableDic variableDic = new VariableDic();
        variableDic.setFieldName(vo.getFieldName());
        variableDic.setFieldValue(vo.getFieldValue());
        variableDic.setFieldDesc(vo.getFieldDesc());
        variableDic.setIsDel(vo.getIsDel());
        variableDic.setUpdateTime(new Date());
        MarketingDataValidConfigDefault validConfigDefault = new MarketingDataValidConfigDefault();
        if ("userType".equals(vo.getFieldName())){
            userType = vo.getFieldValue();
            validConfigDefault.setUserType(userType);
        }
        validConfigDefault.setValidDaysDefault(Integer.valueOf(vo.getValidDaysDefault()));
        validConfigDefault.setIsDel(vo.getIsDel());
        if(StringUtils.isEmpty(vo.getId())){
            //新增
            variableDic.setCid(vo.getCid());
            variableDic.setApiCode(apiCode);
            variableDic.setCreateTime(new Date());
            variableDicMapper.insertSelective(variableDic);
            entityOptService.writeOptLog(variableDic.getId(), variableDic, null);
            Integer i = validityChangeMapper.selectNum(apiCode, userType);
            MarketingDataValidConfigDefault date = validityChangeMapper.selectId(apiCode,userType);
            if (i >= 1){
                log.warn("该apiCode={} , userType={}维度下已存在有效期配置", apiCode, userType);
                validConfigDefault.setId(date.getId());
                validConfigDefault.setApiCode(apiCode);
                validConfigDefault.setUpdateTime(new Date());
                validityChangeMapper.updateMarketingDataValidConfigDefault(validConfigDefault);
                entityOptService.writeOptLog(date.getId(), validConfigDefault, date);
                return new ApiResult<Boolean>().success(true);
            }
            validConfigDefault.setApiCode(apiCode);
            validConfigDefault.setCreateTime(new Date());
            validityChangeMapper.insertSelective(validConfigDefault);
            entityOptService.writeOptLog(validConfigDefault.getId(), validConfigDefault, null);
        }else {
            VariableDic data = variableDicMapper.selectByPrimaryKey(vo.getId());
            //编辑
            variableDic.setId(vo.getId());
            variableDicMapper.updateByPrimaryKeySelective(variableDic);
            entityOptService.writeOptLog(vo.getId(), variableDic, data);
            MarketingDataValidConfigDefault dataValidConfigDefault = validityChangeMapper.selectId(apiCode,userType);
            if (ObjectUtil.isNotEmpty(dataValidConfigDefault)){
                validConfigDefault.setId(dataValidConfigDefault.getId());
                validConfigDefault.setApiCode(apiCode);
                validConfigDefault.setUpdateTime(new Date());
                validityChangeMapper.updateMarketingDataValidConfigDefault(validConfigDefault);
                entityOptService.writeOptLog(dataValidConfigDefault.getId(), validConfigDefault, dataValidConfigDefault);
            } else {
                log.warn("该apiCode={} , userType={}维度不存在代运营默认有效期配置", apiCode, userType);
            }

        }

        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public List<Map> findListByCidsAndApiCodes(List<CustomerSelectVO> vos) {
        List<Map> list = new ArrayList<>();
        if(vos!=null && vos.size()>0){
            for (CustomerSelectVO vo :vos) {
                String cid = vo.getCid();
                String apiCode = vo.getApiCode();
                List<VariableDicSelectVO> userTypeList = findListByCidAndApiCode(cid, apiCode);
                Map map = new HashMap();
                map.put("cid", cid);
                map.put("apiCode", apiCode);
                map.put("userTypeList", userTypeList);
                list.add(map);
            }
        }

        return list;
    }

    @Override
    public Result<Boolean> batchAddUserTypeVariableDicTry(String msgStr) {
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(false);
        if (!StringUtils.hasText(msgStr)) {
            return result;
        }
        LOCK.lock();
        try {
            ApiDataInfoDTO<UserTypeCollectionDTO> apiDataInfoDTO = JSONObject.parseObject(msgStr
                    , new TypeReference<ApiDataInfoDTO<UserTypeCollectionDTO>>() {
                    }.getType());
            String apiCode = apiDataInfoDTO.getApiCode();
            String cId = StringUtils.hasText(apiDataInfoDTO.getCid()) ? apiDataInfoDTO.getCid()
                    : tableCreateService.getCId(apiCode);
            if (StringUtils.isEmpty(apiCode) || StringUtils.isEmpty(cId)) {
                log.error("未获取到cid，消息内容：{}", msgStr);
                return result;
            }
            String key = RedisKeyConstant.USERTYPE_DICT.concat(cId).concat(":").concat(apiCode);
            String fieldName = "userType";
            for (UserTypeCollectionDTO collectionDTO : apiDataInfoDTO.getArgList()) {
                String userType = collectionDTO.getUserType();
                LocalDateTime localDateTime = LocalDateTime.now();
                String redisKey = key.concat(":").concat(userType);
                boolean exists = true;
                boolean isError = false;
                try {
                    // 添加缓存
                    exists = redisChgService.lock(redisKey, apiDataInfoDTO.getRawDataSaveDateStr(), RandomUtils.nextLong(
                            3600 * 24 * 3 * 1000L, 3600 * 24 * 7 * 1000L));
                } catch (Exception e) {
                    isError = true;
                    log.error(e.getMessage(), e);
                }
                if (exists) {
                    // 缓存不存在，检查db中是否存在
                    VariableDicExample variableDicExample = new VariableDicExample();
                    variableDicExample.createCriteria().andCidEqualTo(cId).andApiCodeEqualTo(apiCode)
                            .andFieldNameEqualTo(fieldName).andFieldValueEqualTo(userType).andIsDelEqualTo(1);
                    if (isError) {
                        variableDicExample.setOrderByClause("id for update");
                    }
                    transactionTemplate.execute(status -> {
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
                            int i = variableDicMapper.insertSelective(variableDic);
                            if (i > 0) {
                                // 发送告警通知
                                sendUserTypeAddDingDingMgs(localDateTime, apiCode, userType);
                            } else {
                                log.error("自动化场景维护入库失败,cid:{},apiCode:{},userType:{},上传时间:{},数据来源:{}"
                                        , cId, apiCode, userType, apiDataInfoDTO.getRawDataSaveTimeStr()
                                        , apiDataInfoDTO.getMsgSource());
                            }
                            return variableDic;
                        }
                        return null;
                    });
                }
                // 生成有效期
                createValidDateConfig(apiDataInfoDTO, apiDataInfoDTO.getRawDataSaveTimeStr(), collectionDTO, apiCode
                        , userType);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setCode(ResultCode.FAIL.getValue());
        } finally {
            LOCK.unlock();
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
    private void sendUserTypeAddDingDingMgs(LocalDateTime localDateTime, String apiCode, String userType) {
        try {
            Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
            Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.USERTYPE_ADD_SENDUSERTYPEADDDINGDINGMGS
                    .toString());
            if (CollectionUtils.isEmpty(map)) {
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
                if (localTime.isBefore(startParse) || localTime.equals(startParse)) {
                    // T日定时发送消息
                    key = RedisKeyConstant.USERTYPE_DICT.concat("delay:mgs:today:").concat(startParse.toString())
                            .concat(":").concat(localDateTime.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE));
                    ttl = ChronoUnit.MILLIS.between(localDateTime, localDateTime.toLocalDate().atTime(startParse)
                            .atZone(ZoneId.systemDefault()));
                } else {
                    // T+1日延时定时发送消息
                    key = RedisKeyConstant.USERTYPE_DICT.concat("delay:mgs:tomorrow:").concat(endParse.toString())
                            .concat(":").concat(localDateTime.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE));
                    ttl = ChronoUnit.MILLIS.between(localDateTime, localDateTime.toLocalDate().plusDays(1)
                            .atTime(endParse).atZone(ZoneId.systemDefault()));
                }
                Boolean exists = redisChgService.exists(key);
                if (!exists) {
                    // 不存在添加延迟队列
                    producter.sendByExpiration(MQConstants.ROUTING_KEY_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE, key
                            , String.valueOf(ttl), priority);
                }
                // 缓存批量结果
                redisChgService.saddMember(key, apiCode.concat("  ").concat(userType));
                if (!exists) {
                    // 设置过期时间
                    redisChgService.expire(key, 3600 * 25);
                }
                return;
            }
            // 当开始startTime在endTime之前时表示在startTime与endTime闭区间内实时发送，区间外定时发送
            boolean isRealTimeSend = (localTime.isAfter(startParse) || localTime.equals(startParse))
                    && (localTime.isBefore(endParse) || localTime.equals(endParse));
            if (isRealTimeSend) {
                String content = ("apiCode  userType\n".concat(apiCode).concat("  ").concat(userType).concat("\n"));
                sendDingDingTextMessage(content, map);
            } else {
                // T+1日延时定时发送消息
                int day = (LocalTime.MIN.isBefore(localTime) || LocalTime.MIN.equals(localTime)) && startParse
                        .isAfter(localTime) ? 0 : 1;
                String key = RedisKeyConstant.USERTYPE_DICT.concat("delay:mgs:" + day + ":").concat(startParse.toString())
                        .concat(":").concat(localDateTime.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE));
                Boolean exists = redisChgService.exists(key);
                if (!exists) {
                    // 不存在添加延迟队列
                    producter.sendByExpiration(MQConstants.ROUTING_KEY_MARKETING_SEND_USERTYPE_MESSAGE_DELAY_QUEUE,
                            key, String.valueOf(ChronoUnit.MILLIS.between(localDateTime, localDateTime.toLocalDate()
                                    .plusDays(day).atTime(startParse).atZone(ZoneId.systemDefault()))), priority);
                }
                // 缓存批量结果
                redisChgService.saddMember(key, apiCode.concat("  ").concat(userType));
                if (!exists) {
                    // 设置过期时间
                    redisChgService.expire(key, 3600 * 25);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 2024-03-02 16:17
     * 创建有效期
     * <p>
     * <p>
     * 2024-03-05 14:55 经过与测试同学、需求同学确认，转化和上传数据都要生成默认的有效期配置
     *
     * @param apiDataInfoDTO 消息源
     * @param dateTimeStr    数据接收时间
     * @param apiCode        客户编号
     * @param userType       场景
     */
    private void createValidDateConfig(ApiDataInfoDTO<UserTypeCollectionDTO> apiDataInfoDTO, String dateTimeStr
            , UserTypeCollectionDTO collectionDTO, String apiCode, String userType) {
        if (apiDataInfoDTO.isUploadMsgSource() && collectionDTO.getStatus() == MonitorTypeEnum.STATUS_2.getTypeCode()) {
            return;
        }
        Set<String> apiCodes = marketingCommonConfig.getNonConfigValidDefaultApiCodes();
        if ((apiCodes != null && apiCodes.contains(apiCode))) {
            return;
        }
        LocalDateTime parseTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        MarketingSyncUser marketingSyncUser = new MarketingSyncUser();
        marketingSyncUser.setUserType(userType);
        marketingSyncUser.setApiCode(apiCode);
        marketingSyncUser.setAppletDate(parseTime.toLocalDate().toString());
        String basicDate = parseTime.format(DateTimeFormatter.BASIC_ISO_DATE);
        // 添加有效期范围
        if (marketingCommonConfig.getCustomizeConfigValidDefaultApiCodes().contains(apiCode)) {
            marketingSyncUser.setCusBatch(collectionDTO.getTaskId());
            // 定制生成有效期
            configValidDateDefault(marketingSyncUser
                    , syncUser -> apiCode.concat(":").concat(userType).concat(":").concat(syncUser.getCusBatch())
                            .concat(":").concat(basicDate)
                    , syncUser -> periodOfValidityService.customizeConfigValidDateDefault(syncUser));
        } else {
            // 通用生成有效期
            configValidDateDefault(marketingSyncUser
                    , syncUser -> apiCode.concat(":").concat(userType).concat(":").concat(basicDate)
                    , syncUser -> periodOfValidityService.configValidDateDefault(syncUser));
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
                log.error("设置默认有效期,上锁失败key:" + key + e.getMessage(), e);
            }
            if (lock) {
                function.apply(syncUser);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Result<Boolean> delaySendUserTypeMessage(String redisKey) {
        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.USERTYPE_ADD_SENDUSERTYPEADDDINGDINGMGS
                .toString());
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(false);
        if (CollectionUtils.isEmpty(map)) {
            return result;
        }
        Set<String> userTypeSet = redisChgService.smembers(redisKey);
        if (CollectionUtils.isEmpty(userTypeSet)) {
            return result;
        }
        String contentHeld = "apiCode  userType\n";
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
