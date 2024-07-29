package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.ShuheBlackPhoneRecordMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
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
import java.util.HashMap;

/**
 * D20231218数禾全场景推送黑名单逻辑变更-337
 *
 * @author zhen.Li1
 * @dateTime 2023/12/23 14:45
 */
@Service
@Slf4j
public class ShuHeDxCustomerBlackListImpl implements AssembleData<BlackDetailDTO> {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    private ShuheBlackPhoneRecordMapper shuheBlackPhoneRecordMapper;


    @Override
    public BlackDetailDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        String endTime = "";
        String reserveField1 = transfer.getReserveField1();
        JSONObject jsonObject = JSON.parseObject(reserveField1);
        String clcUsrMaxDxRrtEnd = jsonObject.getString("clc_usr_max_dx_rrt_end");
        String usrForbidCallEndTim = jsonObject.getString("usr_forbid_call_end_tim");
        Integer blackDays = 30;
        if (StringUtils.isEmpty(clcUsrMaxDxRrtEnd) && StringUtils.isEmpty(usrForbidCallEndTim)) {
            HashMap<String, Integer> shuhePushBlackDay = marketingCommonConfig.getShuhePushBlackDay();
            if (shuhePushBlackDay != null) {
                blackDays = shuhePushBlackDay.getOrDefault("customerBlack", 30);
            }
            LocalDateTime expireData = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).plusDays(blackDays);
            endTime = expireData.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
            if (!StringUtils.isEmpty(usrForbidCallEndTim)) {
                endTime = usrForbidCallEndTim;
            } else {
                endTime = clcUsrMaxDxRrtEnd;
            }
        }
        BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
        blackDetailDTO.setDataId(String.valueOf(transfer.getId()));
        blackDetailDTO.setExpireDate(endTime);
        blackDetailDTO.setPhone(BrCipherMaker.getInstance().decode(jsonObject.getString("cell")));
        return blackDetailDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        boolean bool1 = Boolean.FALSE;
        boolean bool2 = Boolean.FALSE;
        boolean bool3 = Boolean.FALSE;
        LocalDate todayDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)) {
                JSONObject jsonObject = JSON.parseObject(reserveField1);
                String clcUsrMaxDxRrtEnd = jsonObject.getString("clc_usr_max_dx_rrt_end");
                String usrForbidCallEndTim = jsonObject.getString("usr_forbid_call_end_tim");
                String isBlack = jsonObject.getString("is_black");
                if (!StringUtils.isEmpty(clcUsrMaxDxRrtEnd)) {
                    LocalDate rrtEndDate;
                    try {
                        rrtEndDate = LocalDate.parse(clcUsrMaxDxRrtEnd.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } catch (Exception e) {
                        log.error("数禾clc_usr_max_dx_rrt_end={}，时间转换异常,转化id={}", clcUsrMaxDxRrtEnd, transfer.getId(), e.getMessage());
                        return false;
                    }
                    bool2 = rrtEndDate.isAfter(todayDate) || rrtEndDate.isEqual(todayDate);
                }
                if (!StringUtils.isEmpty(usrForbidCallEndTim)) {
                    LocalDate callEndTim;
                    try {
                        callEndTim = LocalDate.parse(usrForbidCallEndTim.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } catch (Exception e) {
                        log.error("数禾usr_forbid_call_end_tim={}时间转换异常,转化id={}", usrForbidCallEndTim, transfer.getId(), e.getMessage());
                        return false;
                    }
                    bool1 = callEndTim.isAfter(todayDate) || callEndTim.isEqual(todayDate);
                }
                bool3 = "Y".equals(isBlack);
                if (marketingCommonConfig.getShuHeNonBlackListApiCodeSet().contains(transfer.getApiCode())) {
                    String cell = jsonObject.getString("cell");
                    int i = shuheBlackPhoneRecordMapper.countTmpNonBlackListByCell(cell);
                    if (i > 0) {
                        return false;
                    }
                }
            }
        }
        return bool1 || bool2 || bool3;
    }

    @Override
    public String label() {
        return "ShuHe_Dx_TransferData_CustomerBlackList";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
