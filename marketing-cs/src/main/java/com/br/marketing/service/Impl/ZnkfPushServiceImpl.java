package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.PushShDXDTO;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.customer.CallRecordDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.ZnkfPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private IMarketingSyncUserService iMarketingSyncUserService;

    @Autowired
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private AlarmApiClient alarmClient;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    private RabbitMqProducter producter;

    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    private final String title = "客服->推送电销";


    @Override
    public String znkfPushCallBack(CallRecordDTO dto) {
        try {
            String paramOfValidity = paramOfValidity(dto);
            if(!"true".equals(paramOfValidity)){
                log.error("客服数据落库失败，"+paramOfValidity);
                return paramOfValidity;
            }
            //参数校验通过，客服拨打记录落库
            CallRecord callRecord = new CallRecord();
            callRecord.setCreateTime(new Date());
            BeanUtils.copyProperties(dto,callRecord);
            BeanUtils.copyProperties(dto.getDetail(),callRecord);
            callRecord.setCallStartTime(StringUtils.isNotEmpty(dto.getDetail().getCallStartTime())?new Date(dto.getDetail().getCallStartTime()):null);
            callRecord.setCallConnectTime(StringUtils.isNotEmpty(dto.getDetail().getCallConnectTime())?new Date(dto.getDetail().getCallConnectTime()):null);
            callRecord.setCallEndTime(StringUtils.isNotEmpty(dto.getDetail().getCallEndTime())?new Date(dto.getDetail().getCallEndTime()):null);
            //校验是否已经落库
            CallRecordExample callRecordExample = new CallRecordExample();
            callRecordExample.createCriteria().andTaskIdEqualTo(callRecord.getTaskId())
                    .andCaseNumEqualTo(callRecord.getCaseNum())
                    .andSessionIdEqualTo(callRecord.getSessionId());
            List<CallRecord> callRecords = callRecordMapper.selectByExample(callRecordExample);
            if(callRecords!=null && callRecords.size()>0){
                log.info("taskId={},caseNum={},sessionId={} 的拨打记录已落库！",callRecord.getTaskId(),callRecord.getCaseNum(),callRecord.getSessionId());
                return "success";
            }else {
                callRecordMapper.insertSelective(callRecord);
                //推mq
                final MqFact mqFact = new MqFact();
                mqFact.setSourceId(callRecord.getId());
                mqFact.setSource(TransferSource.CUSTOMER_CALL_RECORD.getCode());
                producter.sendToUniversalTransferQueue(mqFact);
            }
        }catch (Exception ex){
            log.error("taskId={},caseNum={},sessionId={}的客服拨打数据落库失败！错误信息为{}",dto.getTaskId(),dto.getCaseNum(),dto.getDetail().getSessionId(),ex);
            return "客服拨打记录落库失败(insert b_call_record fail)!";
        }
        return "success";
    }

    /**
     * 判断是否符合情况b：userType=促申完 && intentionGrade="A级(有明确意向）" && cusNun && 有效期内
     * @param dto
     * @return
     */
    @Override
    public Boolean isSatisfyPushDX(CallRecordBO dto) {
        Map map = (Map) JSONObject.parse(dto.getDetail().getUserProperties());
        if(StringUtils.isEmpty(map) || StringUtils.isEmpty(map.get("groupType"))){
            log.warn("caseNum={}的数据groupType缺失！",dto.getCaseNum());
            return false;
        }
        if(StringUtils.isEmpty(dto.getDetail().getIntentionGrade())){
            log.warn("caseNum={}的数据intentionGrade缺失！",dto.getCaseNum());
            return false;
        }
        String groupType = map.get("groupType").toString();
        boolean intentionGrade = false;
        if("A类".equals(dto.getDetail().getIntentionGrade()) || "A".equals(dto.getDetail().getIntentionGrade())){
            intentionGrade = true;
        }
        if(!intentionGrade){
            log.info("taskId={},caseNum={},sessionId={}的数据不符合情况b的A！",dto.getTaskId(),dto.getCaseNum(),dto.getDetail().getSessionId());
            return false;
        }
        if(!"促申完".equals(groupType) || !"促首借".equals(groupType)){
            log.info("taskId={},caseNum={},sessionId={}的数据不符合情况b的促申完/促首借场景！",dto.getTaskId(),dto.getCaseNum(),dto.getDetail().getSessionId());
            return false;
        }
        Boolean isPeriod = false;
        if("促申完".equals(groupType)){
            isPeriod = iMarketingSyncUserService.isPeriodOfValidity(
                    dto.getApiCode(), dto.getCaseNum(), groupType, new Date(), 14);
        }else if("促首借".equals(groupType)){
            //先返回false，需要加促首借的有效期
            return false;
        }
        if (!isPeriod) {
            //不在有效期内
            log.info("taskId={},caseNum={},sessionId={}的数据不在情况b的有效期内！",dto.getTaskId(),dto.getCaseNum(),dto.getDetail().getSessionId());
            return false;
        }
        return true;
    }

    /**
     * key存在-->不是首次；key不存在-->是首次传输，redis过期时间为第二天凌晨0点
     * @param key
     * @return
     */
    @Override
    public Boolean cusNumIsFirstToday(String key) {
        if (redisChgService.exists(key)) {
            return false;
        }
        Integer seconds = DateHelper.getRemainSecondsOneDay(new Date());
        redisChgService.setex(key,"1",seconds);
        return true;
    }

    private String goShDX(CallRecordDTO dto) {
        try {
            Date day = new Date();
            SimpleDateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dfSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            //select * from b_marketing_sync_7410437 bms where cust_num ='' order by applet_date desc limit 1;
            MarketingSyncUser marketingSyncUser = marketingSyncInfoMapper.getNewestByCusnum(dto.getApiCode(), dto.getCaseNum());
            if(marketingSyncUser==null){
                log.info("上传数据表中(apicode=%s)不存在 custNum=%s 的数据！",dto.getApiCode(),dto.getCaseNum());
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
            if(marketingTransferSyncUser!=null){
                ////b_marketing_transfer_sync_{cid} 的login_time
                phoneSale.setLoginTime(StringUtils.isNotEmpty(marketingTransferSyncUser.getLoginTime())?marketingTransferSyncUser.getLoginTime():"");
                //b_marketing_transfer_sync_{cid} reserve_field1
                phoneSale.setExtend(StringUtils.isNotEmpty(marketingTransferSyncUser.getReserveField1())?marketingTransferSyncUser.getReserveField1():"");
            }else {
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
            }else {
                String msg = String.format("客服->营销(custNum=%s)推送电销失败！失败信息：%s", marketingTransferSyncUser.getCustNum(), result.getData());
                log.error(msg);
                //alarmClient.sendAlarm(msg, title, appName, secretKey, Constants.sendCodeMap.get("sysError"));
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
            //alarmClient.sendAlarm("保存到电销失败" + e.getMessage(), title, appName, secretKey, Constants.sendCodeMap.get("sysError"));
        }
        return "success";
    }

    private String paramOfValidity(CallRecordDTO dto) {
        //taskid、caseNum、CID、apicode，sessionId；
        if(StringUtils.isEmpty(dto.getDetail().getSessionId())){
            log.warn("taskId={},caseNum={},sessionId={}的数据sessionId缺失！",dto.getTaskId(),dto.getCaseNum(),dto.getDetail().getSessionId());
            return "no param sessionId!";
        }
        if(StringUtils.isEmpty(dto.getApiCode()) || StringUtils.isEmpty(dto.getCid())){
            log.warn("taskId={},caseNum={},sessionId={}的数据apicode或者cid缺失！",dto.getTaskId(),dto.getCaseNum(),dto.getDetail().getSessionId());
            return "no param apicode or cid！";
        }
        if(StringUtils.isEmpty(dto.getCaseNum())){
            log.warn("taskId={},caseNum={},sessionId={}的数据caseNum缺失！",dto.getTaskId(),dto.getCaseNum(),dto.getDetail().getSessionId());
            return "no param caseNum!";
        }
        if(StringUtils.isEmpty(dto.getTaskId())){
            log.warn("taskId={},caseNum={},sessionId={}的数据taskId缺失！",dto.getTaskId(),dto.getCaseNum(),dto.getDetail().getSessionId());
            return "no param taskId!";
        }
        return "true";
    }
}
