package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ShuHeCustomerCallRecordFromDelay implements AssembleData<RealTimeUserDataDTO> {

    @Autowired
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Autowired
    private CallRecordMapper callRecordMapper;

    @Autowired
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public RealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        CallRecordBO dto = (CallRecordBO) transmitFact;
        Date day = new Date();
        SimpleDateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dfSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //select * from b_marketing_sync_7410437 bms where cust_num ='' order by applet_date desc limit 1;
        MarketingSyncUser marketingSyncUser = marketingSyncInfoMapper.getNewestByCusnum(dto.getApiCode(), dto.getCaseNum());
        if(marketingSyncUser==null){
            log.info("上传数据表中(apicode=%s)不存在 custNum=%s 的数据！",dto.getApiCode(),dto.getCaseNum());
            return null;
        }
        //select * from b_marketing_transfer_sync_762 where cust_num='000071'  order by create_time desc limit 1;
        Integer tcid = (Math.abs(dto.getCid()));
        MarketingTransferSyncUser marketingTransferSyncUser = marketingTransferSyncUserMapper.getNewestByCusnum(tcid.toString(), dto.getCaseNum());

        RealTimeUserDataDTO realTimeUserDataDTO = new RealTimeUserDataDTO();
        DassSingleImportAdapDTO dassSingleImportAdapDTO = new DassSingleImportAdapDTO();
        PhoneSaleExtendShuhe phoneSaleExtendShuhe = new PhoneSaleExtendShuhe();
        DassSingleImportDataDTO dassSingleImportDataDTO = new DassSingleImportDataDTO();//单条

        dassSingleImportDataDTO.setUid(dto.getCaseNum());
        String s = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
        dassSingleImportDataDTO.setPhone(s);//b_marketing_sync_{apicode}的cell，明文
        dassSingleImportDataDTO.setName("");
        dassSingleImportDataDTO.setOrgname("shuheshenwan");
        dassSingleImportDataDTO.setSource("16");
        dassSingleImportDataDTO.setUserType("2");
        dassSingleImportDataDTO.setType("2");
        dassSingleImportDataDTO.setPrioritySymbol("2");
        Map extendMap = new HashMap();
        extendMap.put("face_recognitiion","0");
        extendMap.put("is_usr_idt","0");
        extendMap.put("is_bindcard","0");
        extendMap.put("is_usr_inf","0");
        extendMap.put("is_usr_lst_app_sta_tim","0");
        extendMap.put("typeSign","2");
        if(marketingTransferSyncUser!=null){
            ////b_marketing_transfer_sync_{cid} 的login_time
            dassSingleImportDataDTO.setLoginTime(StringUtils.isNotEmpty(marketingTransferSyncUser.getLoginTime())?marketingTransferSyncUser.getLoginTime():"");
            if(StringUtils.isNotEmpty(marketingTransferSyncUser.getReserveField1())) {
                Map map = JSON.parseObject(marketingTransferSyncUser.getReserveField1(), Map.class);
                //clc_usr_iso_pho_tim如果有值且为接收转化数据当天赋1 ，非1为0
                extendMap.put("face_recognitiion",getValueByCreateTime(map.get("clc_usr_iso_pho_tim"),marketingTransferSyncUser.getCreateTime()));
                extendMap.put("is_usr_idt",getValueByCreateTime(map.get("clc_usr_iso_idt_tim"),marketingTransferSyncUser.getCreateTime()));
                extendMap.put("is_bindcard",getValueByCreateTime(map.get("clc_usr_iso_crd_tim"),marketingTransferSyncUser.getCreateTime()));
                extendMap.put("is_usr_inf",getValueByCreateTime(map.get("clc_usr_iso_inf_tim"),marketingTransferSyncUser.getCreateTime()));
                extendMap.put("is_usr_lst_app_sta_tim",getValueByCreateTime(map.get("clc_usr_iso_inf_tim"),marketingTransferSyncUser.getCreateTime()));
                extendMap.put("typeSign","2");
            }
        }else {
            dassSingleImportDataDTO.setLoginTime("");
        }
        dassSingleImportDataDTO.setExtend(JSON.toJSONString(extendMap));

        phoneSaleExtendShuhe.setCustNum(dto.getCaseNum());
        phoneSaleExtendShuhe.setAppletDate(dfDay.format(day));
        phoneSaleExtendShuhe.setAppletTime(dfSecond.format(day));
        phoneSaleExtendShuhe.setStatus("b");

        dassSingleImportAdapDTO.setDassSingleImportDataDTO(dassSingleImportDataDTO);
        realTimeUserDataDTO.setDassSingleImportAdapDTO(dassSingleImportAdapDTO);
        realTimeUserDataDTO.setPhoneSaleExtendShuhe(phoneSaleExtendShuhe);
        log.info("推电销数据{}",realTimeUserDataDTO.toString());
        return realTimeUserDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        //延迟队列消费&剔除-->false
        //延迟队列消费&不剔除-->推电销
        CallRecordBO bo = (CallRecordBO) transmitFact;
        log.info("进入ShuHeCustomerCallRecordFromDelay规则，获取的数据id为{}",bo.getId());
        Boolean isEliminate = isEliminate(bo);
        if(StringUtils.isNotEmpty(bo.getDataSource()) && bo.getDataSource()==1 && !isEliminate){
            return true;
        }
        return false;
    }

    @Override
    public String label() {
        return "ShuHe_CallRecordData_PhoneSale";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA.getCode();
    }

    /**
     * target如果有值且=createTime(日期)返回1,否则为0
     * @param target
     * @param createTime
     * @return
     */
    private String getValueByCreateTime(Object target, Date createTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String createTimeString = formatter.format(createTime);
        if(StringUtils.isNotEmpty(target)&&createTimeString.equals(target.toString().split(" ")[0])){
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
        MarketingTransferSyncUser newest = marketingTransferSyncUserMapper.getNewestByCusnumAndApicode(bo.getCid().toString(), bo.getCaseNum(), bo.getApiCode());
        if(newest == null){
            return false;
        }
        if(StringUtils.isEmpty(newest.getReserveField1())){
            return false;
        }
        Map map = JSON.parseObject(newest.getReserveField1(), Map.class);
        Boolean isTurn = false;
        Boolean isBlack = false;
        Boolean isoAtoTimIsSatisfy = false;
        if(StringUtils.isNotEmpty(map.get("is_turn"))){
            isTurn = "Y".equals(map.get("is_turn").toString());
        }
        if(StringUtils.isNotEmpty(map.get("is_black"))){
            isBlack = "Y".equals(map.get("is_black").toString());
        }
        if(StringUtils.isNotEmpty(map.get("clc_usr_iso_ato_tim"))){
            String timTime = DateUtils.format(addDay(map.get("clc_usr_iso_ato_tim").toString(), 1, "yyyy-MM-dd"), "yyyy-MM-dd");
            int count = callRecordMapper.selectIsIsSatisfyByCreateTime(bo.getId(), timTime);
            isoAtoTimIsSatisfy = count > 0;
        }
        if(isTurn || isBlack || isoAtoTimIsSatisfy){
            return true;
        }
        return false;
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
}
