package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.PushShDXDTO;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.customer.CallRecordDTO;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.CuFuJie;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.RoboAIBlackPhoneMarkMapperBase;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Slf4j
public class ZnkfPushServiceImpl implements ZnkfPushService {

    @Autowired
    private CallRecordMapper callRecordMapper;

    @Autowired
    private PushDataService pushDataService;

    @Autowired
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    private RoboAIBlackPhoneMarkMapperBase roboAIBlackPhoneMarkMapper;

    @Autowired
    private IMarketingSyncUserService iMarketingSyncUserService;

    @Autowired
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private AlarmApiClient alarmClient;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    private RabbitMqProducter producter;

    @Resource
    private DataLoadingHandlerService handlerService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TableCreateServiceImpl tableCreateService;

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
            //校验是否已经落库
            CallRecordExample callRecordExample = new CallRecordExample();
            callRecordExample.createCriteria().andTaskIdEqualTo(callRecord.getTaskId())
                    .andCaseNumEqualTo(callRecord.getCaseNum())
                    .andSessionIdEqualTo(callRecord.getSessionId());
            List<CallRecord> callRecords = callRecordMapper.selectByExample(callRecordExample);
            if (callRecords != null && callRecords.size() > 0) {
                log.info("taskId={},caseNum={},sessionId={} 的拨打记录已落库！", callRecord.getTaskId(), callRecord.getCaseNum(), callRecord.getSessionId());
                return "success";
            } else {
                callRecordMapper.insertSelective(callRecord);
                // 2022-5-17 15:13:23 修改为可配置的apiCode
//                if ("3710004".equals(callRecord.getApiCode()) || "3710023".equals(callRecord.getApiCode()) || "7410785".equals(callRecord.getApiCode())) {
                List<String> apiCodes = marketingCommonConfig.getCallRecordDataPushMqApiCodes();
                if (apiCodes == null) {
                    apiCodes = Arrays.asList("3710004", "3710023", "3710043", "7410785");
                }
                if (apiCodes.contains(callRecord.getApiCode())) {
                    //推mq
                    final MqFact mqFact = new MqFact();
                    mqFact.setSourceId(callRecord.getId());
                    mqFact.setSource(TransferSource.CUSTOMER_CALL_RECORD.getCode());
                    producter.sendToUniversalTransferQueue(mqFact);
                }

            }
        } catch (Exception ex) {
            log.error("taskId={},caseNum={},sessionId={}的客服拨打数据落库失败！错误信息为{}", dto.getTaskId(), dto.getCaseNum(), dto.getDetail().getSessionId(), ex);
            return "客服拨打记录落库失败(insert b_call_record fail)!";
        }
        return "success";
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
        IUserType iUserType = UserTypeStrategyFactory.getUserTypeStrategy(groupType);
        if (iUserType instanceof CuFuJie) {
            return cuFuJie(dto, groupType, iUserType);
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

    private String goShDX(CallRecordDTO dto) {
        try {
            Date day = new Date();
            SimpleDateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dfSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            //select * from b_marketing_sync_7410437 bms where cust_num ='' order by applet_date desc limit 1;
            MarketingSyncUser marketingSyncUser = marketingSyncInfoMapper.getNewestByCusnum(dto.getApiCode(), dto.getCaseNum());
            if (marketingSyncUser == null) {
                log.info("上传数据表中(apicode=%s)不存在 custNum=%s 的数据！", dto.getApiCode(), dto.getCaseNum());
                return "true";
            }
            //select * from b_marketing_transfer_sync_762 where cust_num='000071'  order by create_time desc limit 1;
            Integer tcid = (Math.abs(dto.getCid()));
            MarketingTransferSyncUser marketingTransferSyncUser = marketingTransferSyncUserMapper.getNewestByCusnum(tcid.toString(), dto.getCaseNum());

            LocalFile localFile = new LocalFile();
            PhoneSale phoneSale = new PhoneSale();
            PhoneSaleExtendShuhe phoneSaleExtendShuhe = new PhoneSaleExtendShuhe();
            PushShDXDTO pushShDXDTO = new PushShDXDTO()
                    .setLocalFile(localFile)
                    .setPhoneSale(phoneSale)
                    .setPhoneSaleExtendShuhe(phoneSaleExtendShuhe);
            localFile.setCid(dto.getCid().toString());
            localFile.setApiCode(dto.getApiCode());
            localFile.setFileName("客服");
            phoneSale.setUid(dto.getCaseNum());
            String s = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
            phoneSale.setPhone(s);//b_marketing_sync_{apicode}的cell，明文
            phoneSale.setName("");
            phoneSale.setOrgname("shuheshenwan");
            phoneSale.setSource("16");
            phoneSale.setUserType("2");
            phoneSale.setType("2");
            if (marketingTransferSyncUser != null) {
                ////b_marketing_transfer_sync_{cid} 的login_time
                phoneSale.setLoginTime(StringUtils.isNotEmpty(marketingTransferSyncUser.getLoginTime()) ? marketingTransferSyncUser.getLoginTime() : "");
                //b_marketing_transfer_sync_{cid} reserve_field1
                phoneSale.setExtend(StringUtils.isNotEmpty(marketingTransferSyncUser.getReserveField1()) ? marketingTransferSyncUser.getReserveField1() : "");
            } else {
                phoneSale.setLoginTime("");
                phoneSale.setExtend("");
            }

            phoneSaleExtendShuhe.setCustNum(dto.getCaseNum());
            phoneSaleExtendShuhe.setAppletDate(dfDay.format(day));
            phoneSaleExtendShuhe.setAppletTime(dfSecond.format(day));
            phoneSaleExtendShuhe.setStatus("b");
            Result<Boolean> result = pushDataService.pushShDX(pushShDXDTO);
            if (result.getData()) {
                log.info("推送电销成功！");
            } else {
                String msg = String.format("客服->营销(custNum=%s)推送电销失败！失败信息：%s", marketingTransferSyncUser.getCustNum(), result.getData());
                log.error(msg);
                //alarmClient.sendAlarm(msg, title, appName, secretKey, Constants.sendCodeMap.get("sysError"));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //alarmClient.sendAlarm("保存到电销失败" + e.getMessage(), title, appName, secretKey, Constants.sendCodeMap.get("sysError"));
        }
        return "success";
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

    private boolean cuFuJie(CallRecordBO dto, String groupType, IUserType iUserType) throws IllegalAccessException {
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
}
