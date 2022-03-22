package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.CallRecordExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendShuhe;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushDataService;
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

/**
 * 消费延迟队列，推电销
 */
@Service
@Slf4j
public class ShuHeCustomerCallRecordToPhoneSale implements AssembleData<RealTimeUserDataDTO> {

    @Autowired
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Autowired
    private CallRecordMapper callRecordMapper;

    @Autowired
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    private PushDataService pushDataService;

    @Override
    public RealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        CallRecordBO dto = (CallRecordBO) transmitFact;
        log.info("匹配上ShuHeCustomerCallRecordToPhoneSale规则，获取的拨打记录数据id为{}",dto.getId());
        Date day = new Date();
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
                JSONObject json = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
                Date createTime = marketingTransferSyncUser.getCreateTime();
                String time = new SimpleDateFormat("yyyy-MM-dd").format(createTime);
                //clc_usr_iso_pho_tim如果有值且为接收转化数据当天赋1 ，非1为0
                extendMap.put("face_recognitiion",getValueByCreateTime(json.getString("clc_usr_iso_pho_tim"),time));
                extendMap.put("is_usr_idt",getValueByCreateTime(json.getString("clc_usr_iso_idt_tim"),time));
                extendMap.put("is_bindcard",getValueByCreateTime(json.getString("clc_usr_iso_crd_tim"),time));
                extendMap.put("is_usr_inf",getValueByCreateTime(json.getString("clc_usr_iso_inf_tim"),time));
                extendMap.put("is_usr_lst_app_sta_tim",getValueByCreateTime(json.getString("clc_usr_iso_inf_tim"),time));
            }
        }else {
            dassSingleImportDataDTO.setLoginTime("");
        }
        dassSingleImportDataDTO.setExtend(JSON.toJSONString(extendMap));

        phoneSaleExtendShuhe.setCustNum(dto.getCaseNum());
        phoneSaleExtendShuhe.setAppletDate(dfDay.format(day));
        phoneSaleExtendShuhe.setAppletTime(dfSecond.format(day));
        phoneSaleExtendShuhe.setStatus("b");
        phoneSaleExtendShuhe.setApiCode(dto.getApiCode());
        phoneSaleExtendShuhe.setTaskId(dto.getTaskId().toString());

        dassSingleImportAdapDTO.setDassSingleImportDataDTO(dassSingleImportDataDTO);
        realTimeUserDataDTO.setDassSingleImportAdapDTO(dassSingleImportAdapDTO);
        realTimeUserDataDTO.setPhoneSaleExtendShuhe(phoneSaleExtendShuhe);
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

    /**
     * target如果有值且=createTime(日期)返回1,否则为0
     * @param reserveFieldTime
     * @param createTime
     * @return
     */

    private String getValueByCreateTime(String reserveFieldTime, String createTime) {
        if (StringUtils.isNotBlank(reserveFieldTime)&&reserveFieldTime.startsWith(createTime)){
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
        MarketingTransferSyncUser newest = marketingTransferSyncUserMapper.getNewestByCusnumAndApicode(tcid, bo.getCaseNum(), bo.getApiCode(),userType);
        if (StringUtils.isNotEmpty(newest)&&StringUtils.isNotBlank(newest.getReserveField1())){
            JSONObject json = JSON.parseObject(newest.getReserveField1());
            boolean isTurn = "Y".equals(json.getString("is_turn"));
            boolean isBlack = "Y".equals(json.getString("is_black"));
            boolean isoAtoTimIsSatisfy = false;
            String clcUsrIsoAtoTim = json.getString("clc_usr_iso_ato_tim");
            if (StringUtils.isNotBlank(clcUsrIsoAtoTim)){
                Date date = json.getDate("clc_usr_iso_ato_tim");
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.DAY_OF_MONTH, 1);
                String time = DateUtils.format(c.getTime(), "yyyy-MM-dd");
                Date date2 = null;
                try {
                    date2 = DateUtils.parse(time, "yyyy-MM-dd");
                } catch (ParseException e) {
                    e.printStackTrace();
                    log.warn("日期转换出错！");
                }
                CallRecordExample example = new CallRecordExample();
                example.createCriteria().andIdEqualTo(bo.getId()).andCreateTimeLessThan(date2);
                isoAtoTimIsSatisfy = callRecordMapper.countByExample(example)>0;
            }
            return isTurn || isBlack || isoAtoTimIsSatisfy;
        }
        return false;
    }
}
