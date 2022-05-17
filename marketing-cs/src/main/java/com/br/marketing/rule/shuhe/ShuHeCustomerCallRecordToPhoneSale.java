package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushDataService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 消费延迟队列，推电销
 */
@Service
@Slf4j
public class ShuHeCustomerCallRecordToPhoneSale implements AssembleData<RealTimeUserDataDTO> {

    @Autowired
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Autowired
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    private PushDataService pushDataService;

    @Resource
    private ShuHeArtificialRealTimeUserDataFromDelayImpl shuHeArtificialRealTimeUserDataFromDelay;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");

    @Override
    public RealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        CallRecordBO dto = (CallRecordBO) transmitFact;
        log.warn("符合推电销规则，callrecord数据id为{}", dto.getId());
        Date day = dto.getCreateTime();
        SimpleDateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dfSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //select * from b_marketing_sync_7410437 bms where cust_num ='' order by applet_date desc limit 1;
        MarketingSyncUser marketingSyncUser = marketingSyncInfoMapper.getNewestByCusnum(dto.getApiCode(), dto.getCaseNum());
        if(marketingSyncUser==null){
            log.warn("上传数据表中(apicode=%s)不存在 custNum=%s 的数据！",dto.getApiCode(),dto.getCaseNum());
            return null;
        }

        Date dtoCreateTime = dto.getCreateTime();
        Calendar c = Calendar.getInstance();
        c.setTime(dtoCreateTime);
        c.add(Calendar.HOUR_OF_DAY, 1);
        String timeAddHour = DateUtils.format(c.getTime(), "yyyy-MM-dd HH:mm:ss");
        //select * from b_marketing_transfer_sync_762 where cust_num='000071'  order by create_time desc limit 1;
        Integer tcid = (Math.abs(dto.getCid()));
        MarketingTransferSyncUser marketingTransferSyncUser = marketingTransferSyncUserMapper.getNewestByCusnumInHour(tcid.toString(), dto.getCaseNum(),timeAddHour);

        RealTimeUserDataDTO realTimeUserDataDTO = new RealTimeUserDataDTO();
        DassSingleImportAdapDTO dassSingleImportAdapDTO = new DassSingleImportAdapDTO();
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        DassSingleImportDataDTO dassSingleImportDataDTO = new DassSingleImportDataDTO();//单条

        dassSingleImportDataDTO.setUid(dto.getCaseNum());
        String s = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
        dassSingleImportDataDTO.setPhone(s);//b_marketing_sync_{apicode}的cell，明文
        dassSingleImportDataDTO.setName("1");
        dassSingleImportDataDTO.setPrioritySymbol("2");
        Map extendMap = new HashMap();
        extendMap.put("face_recognitiion","0");
        extendMap.put("is_usr_idt","0");
        extendMap.put("is_bindcard","0");
        extendMap.put("is_usr_inf","0");
        extendMap.put("is_usr_lst_app_sta_tim","0");
        extendMap.put("typeSign","2");
        phoneSaleExtendInfo.setStatus("b");
        if("促申完".equals(dto.getUserType())){
            if("B".equals(dto.getDetail().getIntentionGrade())){
                phoneSaleExtendInfo.setStatus("c");
                dassSingleImportDataDTO.setPrioritySymbol("3");
                extendMap.put("typeSign","3");
            }
            dassSingleImportDataDTO.setOrgname("shuheshenwan");
            dassSingleImportDataDTO.setSource("16");
            dassSingleImportDataDTO.setUserType("2");
            dassSingleImportDataDTO.setType("2");
        }else if ("促首借".equals(dto.getUserType())) {
            dassSingleImportDataDTO.setOrgname("shuheshoujie");
            dassSingleImportDataDTO.setSource("18");
            dassSingleImportDataDTO.setUserType("1");
            dassSingleImportDataDTO.setType("4");
        } else if ("促复借".equals(dto.getUserType())) {
            dassSingleImportDataDTO.setOrgname("shuhefujie");
            dassSingleImportDataDTO.setSource("16");
            dassSingleImportDataDTO.setUserType("1");
            dassSingleImportDataDTO.setType("4");
            extendMap.put("typeSign", "3");
            if (marketingTransferSyncUser != null) {
                String reserveField1 = marketingTransferSyncUser.getReserveField1();
                if (org.apache.commons.lang3.StringUtils.isNotBlank(reserveField1)) {
                    JSONObject jsonObject = JSONObject.parseObject(reserveField1);
                    String lv = jsonObject.getOrDefault("clc_usr_avl_lmt_lv0", "").toString();
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(lv)) {
                        extendMap.put("clc_usr_avl_lmt_lv0", lv);
                    }
                }
            }
            dassSingleImportDataDTO.setPrioritySymbol("3");
            String name = marketingSyncUser.getName();
            if (StringUtils.isNotBlank(name)) {
                try {
                    dassSingleImportDataDTO.setName(BrCipherMaker.getInstance().decode(name));
                } catch (Exception ignored) {
                }
            }
            phoneSaleExtendInfo.setStatus("c");
        }
        dassSingleImportDataDTO.setUid(dto.getCaseNum());
        if(marketingTransferSyncUser!=null){
            if(StringUtils.isNotEmpty(marketingTransferSyncUser.getReserveField1())) {
                JSONObject json = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
                Date createTime = marketingTransferSyncUser.getCreateTime();
                String time = new SimpleDateFormat("yyyy-MM-dd").format(createTime);
                String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                //clc_usr_iso_pho_tim如果有值且为接收转化数据当天赋1 ，非1为0
                extendMap.put("face_recognitiion",getValueByCreateTime(json.getString("clc_usr_iso_pho_tim"),time));
                extendMap.put("is_usr_idt",getValueByCreateTime(json.getString("clc_usr_iso_idt_tim"),time));
                extendMap.put("is_bindcard",getValueByCreateTime(json.getString("clc_usr_iso_crd_tim"),time));
                extendMap.put("is_usr_inf",getValueByCreateTime(json.getString("clc_usr_iso_inf_tim"),time));
                extendMap.put("is_usr_lst_app_sta_tim",getValueByCreateTime(json.getString("clc_usr_lst_app_sta_tim"),today));
                dassSingleImportDataDTO.setLoginTime(StringUtils.isNotEmpty(json.getString("clc_usr_lst_app_sta_tim"))?json.getString("clc_usr_lst_app_sta_tim"):"");
            }
            dassSingleImportDataDTO.setAuditAmount(StringUtils.isNotEmpty(marketingTransferSyncUser.getAuditAmount())?marketingTransferSyncUser.getAuditAmount():"");
        }else {
            dassSingleImportDataDTO.setLoginTime("");
        }
        dassSingleImportDataDTO.setExtend(JSON.toJSONString(extendMap));

        phoneSaleExtendInfo.setCustNum(dto.getCaseNum());
        phoneSaleExtendInfo.setAppletDate(dfDay.format(day));
        phoneSaleExtendInfo.setAppletTime(dfSecond.format(day));
        phoneSaleExtendInfo.setApiCode(dto.getApiCode());
        phoneSaleExtendInfo.setTaskId(dto.getTaskId().toString());
        phoneSaleExtendInfo.setUserType(dto.getUserType());

        dassSingleImportAdapDTO.setDassSingleImportDataDTO(dassSingleImportDataDTO);
        realTimeUserDataDTO.setDassSingleImportAdapDTO(dassSingleImportAdapDTO);
        realTimeUserDataDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
        return realTimeUserDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        //延迟队列消费&剔除-->false
        //延迟队列消费&不剔除-->推电销
        boolean flag = Boolean.FALSE;
        if (transmitFact instanceof CallRecordBO){
            CallRecordBO bo = (CallRecordBO) transmitFact;
            flag = StringUtils.isNotEmpty(bo.getDataSource()) && bo.getDataSource() == 1 && !isEliminate(bo) && pushDataService.pushShDXSingleMutex(bo.getApiCode(),bo.getCaseNum(),"b",bo.getUserType());
        }
        return flag;
    }

    @Override
    public String label() {
        return "ShuHe_CallRecordData_PhoneSale";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

    /**
     * target如果有值且=createTime(日期)返回1,否则为0
     * @param reserveFieldTime
     * @param createTime
     * @return
     */

    private String getValueByCreateTime(String reserveFieldTime, String createTime) {
        if (StringUtils.isNotBlank(reserveFieldTime) && reserveFieldTime.startsWith(createTime)){
            return "1";
        }
        return "0";
    }

    /**
     * 是否剔除逻辑：剔除为true,不剔除为false
     * @param bo
     * @return
     */
    public Boolean isEliminate(CallRecordBO bo) {
        JSONObject userProperties = JSON.parseObject(bo.getDetail().getUserProperties());
        String userType = userProperties.get("groupType").toString();
        String tcid = bo.getCid().toString().replaceFirst("-", "");
        Date dtoCreateTime = bo.getCreateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dtoCreateTime);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        String timeAddHour = DateUtils.format(calendar.getTime(), "yyyy-MM-dd HH:mm:ss");
        MarketingTransferSyncUser newest = marketingTransferSyncUserMapper.getNewestByCusnumAndApicode(tcid, bo.getCaseNum(), bo.getApiCode(),userType,timeAddHour);
        if (StringUtils.isNotEmpty(newest)&&StringUtils.isNotBlank(newest.getReserveField1())){
            JSONObject json = JSON.parseObject(newest.getReserveField1());
            boolean isTurn = "Y".equals(json.getString("is_turn"));
            boolean isBlack = "Y".equals(json.getString("is_black"));
            boolean isRemoveFlag = false;
            if("促申完".equals(bo.getUserType())){
                String clcUsrIsoAtoTim = newest.getApplyTime();
                if(StringUtils.isNotEmpty(clcUsrIsoAtoTim)){
                    isRemoveFlag = isRemove(bo, clcUsrIsoAtoTim);
                }
            } else if ("促首借".equals(bo.getUserType())) {
                String ordTim = json.getString("applyLoanTime");
                if (StringUtils.isNotEmpty(ordTim)) {
                    isRemoveFlag = isRemove(bo, ordTim);
                }
            } else if ("促复借".equals(bo.getUserType())) {
                Date createTime = newest.getCreateTime();
                Date createTimeB = bo.getCreateTime();
                newest.setCreateTime(createTimeB);
                if (shuHeArtificialRealTimeUserDataFromDelay.queryStopPushRecord(newest)) {
                    newest.setCreateTime(createTime);
                    isRemoveFlag = true;
                    log.info("#促复借c情况存在转化数据,查询存在b情况且在停止推送时间内:{}", isRemoveFlag);
                } else {
                    newest.setCreateTime(createTime);
                    MarketingSyncUser user = marketingSyncInfoMapper.getNewestByCusnum(bo.getApiCode(), bo.getCaseNum());
                    isRemoveFlag = shuHeArtificialRealTimeUserDataFromDelay.queryBlackFlag(newest, ObjectUtils.isEmpty(user)
                            ? null : user.getCell());
                    log.info("#促复借c情况存在转化数据,命中黑名单情况：{}", isRemoveFlag);
                    if (!isRemoveFlag) {
                        isRemoveFlag = phoneSaleExtendInfo(newest.getCustNum(), newest.getApiCode(), newest.getUserType()
                                , bo.getCreateTime());
                        log.info("#促复借c情况存在转化数据,查询到推送过a或b情况：{}", isRemoveFlag);
                    }
                }
            }
            if (isTurn || isBlack || isRemoveFlag) {
                log.info("callrecord数据id为{}符合前置剔除规则", bo.getId());
            }
            return isTurn || isBlack || isRemoveFlag;
        } else if ("促复借".equals(bo.getUserType())) {
            newest = new MarketingTransferSyncUser();
            newest.setCustNum(bo.getCaseNum());
            newest.setApiCode(bo.getApiCode());
            newest.setUserType(bo.getUserType());
            newest.setId(0L);
            if (shuHeArtificialRealTimeUserDataFromDelay.queryStopPushRecord(newest)) {
                log.info("#促复借c情况存在不存在转化数据,查询存在b情况且在停止推送时间内");
                return true;
            }
            MarketingSyncUser user = marketingSyncInfoMapper.getNewestByCusnum(bo.getApiCode(), bo.getCaseNum());
            if (shuHeArtificialRealTimeUserDataFromDelay.queryBlackFlag(newest, ObjectUtils.isEmpty(user)
                    ? null : user.getCell())) {
                log.info("#促复借c情况存在不存在转化数据,命中黑名单");
                return true;
            }
            return phoneSaleExtendInfo(bo.getCaseNum(), bo.getApiCode(), bo.getUserType(), bo.getCreateTime());
        }
        return false;
    }

    private boolean isRemove(CallRecordBO bo, String clcUsrIsoAtoTim) {
        boolean isRemoveFlag;
        LocalDate clcUsrIsoAtoTimDate = LocalDateTime.parse(clcUsrIsoAtoTim, dateTimeFormatter)
                .toLocalDate();
        LocalDate createDate = bo.getCreateTime().toInstant().atZone(
                ZoneId.systemDefault()).toLocalDate();
        isRemoveFlag = (clcUsrIsoAtoTimDate.isAfter(createDate) || clcUsrIsoAtoTimDate.isEqual(createDate));
        return isRemoveFlag;
    }

    /**
     * 2022/5/9 18:20
     * 是否存a或b
     * true 存在
     * false 不存在
     */
    private boolean phoneSaleExtendInfo(String custNum, String apiCode, String userType, Date date) {
        PhoneSaleExtendInfoExample example = new PhoneSaleExtendInfoExample();
        if (ObjectUtils.isEmpty(date)) {
            date = new Date();
        }
        ZonedDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().minusDays(6).atStartOfDay().atZone(ZoneId.systemDefault());
        example.createCriteria().andStatusIn(Arrays.asList("a", "b"))
                .andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                .andCustNumEqualTo(custNum).andCreateTimeBetween(
                Date.from(dateTime.toInstant()), date);
        int count = phoneSaleExtendInfoMapper.countByExample(example);
        log.info("#促复借c情况是否7内天推送过a或b情况:{}", count);
        return count > 0;
    }


}
