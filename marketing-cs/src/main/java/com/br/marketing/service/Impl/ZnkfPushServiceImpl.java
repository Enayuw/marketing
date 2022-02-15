package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dos.PeriodOfValidityDO;
import com.br.marketing.dto.PushShDXDTO;
import com.br.marketing.dto.customer.CallRecordDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.ZnkfPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    @Override
    public Boolean znkfPushCallBack(CallRecordDTO dto) {
        //客服拨打记录落库
        CallRecord callRecord = new CallRecord();
        callRecord.setCreateTime(new Date());
        BeanUtils.copyProperties(dto,callRecord);
        BeanUtils.copyProperties(dto.getDetail(),callRecord);
        callRecord.setCallStartTime(new Date(dto.getDetail().getCallStartTime()));
        callRecord.setCallConnectTime(new Date(dto.getDetail().getCallConnectTime()));
        callRecord.setCallEndTime(new Date(dto.getDetail().getCallEndTime()));
        int insertSelective = callRecordMapper.insertSelective(callRecord);
        if(StringUtils.isEmpty(insertSelective) || insertSelective<1){
            log.warn("客服拨打记录落库失败！");
            return false;
        }

        //判断是否符合情况b：userType=促申完&intentionGrade=A&cusNun&有效期内
        Map map = (Map) JSONObject.parse(dto.getDetail().getUserProperties());
        String groupType = map.get("groupType").toString();

        boolean intentionGrade = dto.getDetail().getIntentionGrade().equals("A级(有明确意向）");

        if(!"促申完".equals(groupType) || !intentionGrade){
            log.info("不符合情况b的userType='促申完'或者A意向！");
            return true;
        }
        if(StringUtils.isBlank(dto.getCaseNum())){
            log.info("客服传入的案件编号caseNum为空！");
            return true;
        }
        //select * from b_marketing_sync_7410437 bms where cust_num ='' order by applet_date desc limit 1;
        MarketingSyncUser marketingSyncUser = marketingSyncInfoMapper.getNewestByCusnum(dto.getApiCode(), dto.getCaseNum());
        //select * from b_marketing_transfer_sync_762 where cust_num='000071'  order by create_time desc limit 1;
        MarketingTransferSyncUser marketingTransferSyncUser = marketingTransferSyncUserMapper.getNewestByCusnum(dto.getCid().toString(),dto.getCaseNum());

        PeriodOfValidityDO periodOfValidityDO = PeriodOfValidityDO.closInterval15Day();
        Boolean isPeriod = iMarketingSyncUserService.isPeriodOfValidity(dto.getApiCode(), dto.getCaseNum(), periodOfValidityDO);

        if(!isPeriod) {
            //不在有效期内
            log.info("不符合情况b的有效期！");
            return true;
        }
        Date day = new Date();
        SimpleDateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dfSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        phoneSale.setPhone(s);//b_marketing_sync_{apicode}的cell，明文？
        phoneSale.setName("");
        phoneSale.setOrgname("shuheshenwan");
        phoneSale.setSource("16");
        phoneSale.setUserType("2");
        phoneSale.setLoginTime(marketingTransferSyncUser.getLoginTime());//b_marketing_transfer_sync_{cid} 的login_time
        phoneSale.setExtend(marketingTransferSyncUser.getReserveField1());//b_marketing_transfer_sync_{cid} reserve_field1
        phoneSaleExtendShuhe.setCustNum(dto.getCaseNum());
        phoneSaleExtendShuhe.setAppletDate(dfDay.format(day));
        phoneSaleExtendShuhe.setAppletTime(dfSecond.format(day));

        //调用 数禾推送电销方法
        Result<Boolean> result = pushDataService.pushShDX(pushShDXDTO);
        if (!result.getData()) {
            log.warn("调用推送电销方法失败！");
            return false;
        }
        return true;
    }
}
