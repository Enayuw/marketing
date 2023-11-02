package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.dassservice.input.transfer.ShuheBlackPhoneTransferDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ShuHeRuleCollectDataImpl;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.ShuheBlackPhoneRecordMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IShuheBlackPhoneRecordService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * D20220526数禾全场景转化过滤-黑名单（营销→Daas） (clc_usr_max_dx_rrt_end 实时部分)
 *
 * @author songjuanjuan
 * @dateTime 2022/5/27 15:45
 */
@Service
@Slf4j
public class ShuHeToArtificialTransferImpl implements AssembleData<ShuheBlackPhoneTransferDataDTO> {

    @Resource
    private IShuheBlackPhoneRecordService iShuheBlackPhoneRecordService;

    @Autowired
    ShuheBlackPhoneRecordMapper shuheBlackPhoneRecordMapper;

    protected final String Y = "Y";
    protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");

    @Override
    public ShuheBlackPhoneTransferDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        LocalDate todayDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext = (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
        ShuheBlackPhoneTransferDataDTO transferDataDTO = new ShuheBlackPhoneTransferDataDTO();
        transferDataDTO.setApiCode(transfer.getApiCode());
        transferDataDTO.setCustNum(transfer.getCustNum());
        transferDataDTO.setPhone(shuHeContext.getCaseShuheUser().getCell());
        transferDataDTO.setPushDate(todayDate.toString());
        return transferDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context){
        LocalDate todayDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
        boolean bool1 = Boolean.FALSE;
        boolean bool2 = Boolean.FALSE;
        boolean bool3 = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData shuHeContext = (ShuHeRuleCollectDataImpl.ShuHeRuleNecessaryData) context.getRuleNecessaryData();
            //clc_usr_max_dx_rrt_end>=当前日期，计算到年月日,取数禾前置转化接口用户上传的手机号推送至Daas转化接口
            //或者is_black=Y
            final CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
            JSONObject jsonObject = caseShuheUser.getJsonObject();
            String clcUsrMaxDxRrtEnd = jsonObject.getString("clc_usr_max_dx_rrt_end");
            String usrForbidCallEndTim = caseShuheUser.getUsrForbidCallEndTim();
            if (!StringUtils.isEmpty(shuHeContext.getCaseShuheUser().getCell())) {
                String cell = BrCipherMaker.getInstance().encode(shuHeContext.getCaseShuheUser().getCell());
                boolean isRepeatPhone = iShuheBlackPhoneRecordService.isRepeatPhone(cell, todayDate.toString());
                if(isRepeatPhone){
                    log.warn("cell={}今日已推过。",cell);
                }else{
                    if(!StringUtils.isEmpty(clcUsrMaxDxRrtEnd)){
                        LocalDate rrtEndDate;
                        try {
                            rrtEndDate = LocalDate.parse(clcUsrMaxDxRrtEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        } catch (Exception e) {
                            rrtEndDate = LocalDateTime.parse(clcUsrMaxDxRrtEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalDate();
                        }
                        bool1 = (rrtEndDate.isAfter(todayDate) || rrtEndDate.isEqual(todayDate) );
                    }
                    bool2 = Y.equals(caseShuheUser.getIsBlack());
                    if(!StringUtils.isEmpty(usrForbidCallEndTim)){
                        LocalDate rrtEndTime;
                        try {
                            rrtEndTime = LocalDate.parse(usrForbidCallEndTim.substring(0,10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            bool3 = (rrtEndTime.isAfter(todayDate) || rrtEndTime.isEqual(todayDate) );
                        } catch (Exception e) {
                            log.error(e.getMessage(),e);
                        }
                    }
                }
            }
        }
        return bool1 || bool2 || bool3;
    }

    @Override
    public String label() {
        return "ShuHe_AllUsertypeTo_ArtificialTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_SHUHE_BLACK_DATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.SHU_HE_RULE_DATA_COLLECTION.getCode();
    }
}
