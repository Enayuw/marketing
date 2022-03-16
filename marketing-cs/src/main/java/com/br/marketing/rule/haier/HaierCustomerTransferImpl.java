package com.br.marketing.rule.haier;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Service
@Slf4j
public class HaierCustomerTransferImpl implements AssembleData<ConversionData> {

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser)transmitFact;
        MarketingSyncUser syncUser = context.getCustomerMap().get(transferSyncUser.getCustNum());
        try {
            if ("4".equals(transferSyncUser.getUserType())) {
                return true;
            }
            if (syncUser == null) {
                return false;
            }
            Date applydt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(transferSyncUser.getApplyDt());
            Date appletTime = syncUser.getAppletTime();
            if ("0".equals(transferSyncUser.getApplyResult()) && applydt.compareTo(appletTime) > 0) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {

        MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser)transmitFact;
        MarketingSyncUser syncUser = context.getCustomerMap().get(transferSyncUser.getCustNum());
        try {
            if (syncUser == null) {
                log.error(String.format("海尔该转化数据没有匹配到原始上传数据 dataId:%d",transferSyncUser.getId()));
                return null;
            }
            String status = "";
            if ("4".equals(transferSyncUser.getUserType())) {
                status = "0";
            } else {
                Date applydt = null;
                try {
                    applydt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(transferSyncUser.getApplyDt());
                    Date appletTime = syncUser.getAppletTime();
                    if ("0".equals(transferSyncUser.getApplyResult()) && applydt.compareTo(appletTime) > 0) {
                        status = "2";
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (StringUtils.isEmpty(status)) {
                return null;
            }
            ConversionData conversionData = new ConversionData();
            conversionData.setDataId(transferSyncUser.getId().toString());
            conversionData.setCid(transferSyncUser.getCid());
            conversionData.setCaseNum(transferSyncUser.getCustNum());
            conversionData.setGroupType(transferSyncUser.getUserType());
            conversionData.setPhone(BrCipherMaker.getInstance().decode(syncUser.getCell()));
            conversionData.setInversionStatus(status);
            if (!StringUtils.isEmpty(transferSyncUser.getCreateTime())) {
                conversionData.setPartnerProcessDate(DateUtils.format(transferSyncUser.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            }
            TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
            BeanUtils.copyProperties(transferSyncUser, vo);
            conversionData.setInversionInfo(JSON.toJSONString(vo));
            return conversionData;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public String label() {
        return "Haier_OverdueData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }
}
