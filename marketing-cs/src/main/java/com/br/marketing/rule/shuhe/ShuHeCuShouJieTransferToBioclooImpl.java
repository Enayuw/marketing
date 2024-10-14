package com.br.marketing.rule.shuhe;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.biocloo.input.DataSoleDTO;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.api.client.util.Sets;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * <a href="https://c.100credit.cn/pages/viewpage.action?pageId=178192891">【紧急】D20240906数禾促首借自动化转黑名单（营销→bkl）-3710166</a>
 *
 * @author senyang.zheng
 * @date 2024/09/07
 */
@Service
@Slf4j
public class ShuHeCuShouJieTransferToBioclooImpl implements AssembleData<DataSoleDTO> {

    public final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private TransferDataValidityPeriodService validityPeriodService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public DataSoleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        String reserveField1 = transfer.getReserveField1();
        JSONObject json = JSON.parseObject(reserveField1);
        Set<String> custNumSet = Sets.newHashSet();
        String custNum = transfer.getCustNum();
        custNumSet.add(custNum);
        JSONObject userTypeJson = marketingCommonConfig.getShuHeToBioclooUserTypeAndApiCodeMapping();
        JSONObject proxyJson = userTypeJson.getJSONObject("促首借");
        Map<String, SyncUserValidityPeriodsBO> boMap =
            validityPeriodService.getValidityPeriodsByCustNum(custNumSet, proxyJson.getString(context.getApiCode()), new Date());
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = boMap.get(transfer.getCustNum());
        if (syncUserValidityPeriodsBO == null) {
            return null;
        }
        DataSoleDTO dataSoleDTO = new DataSoleDTO();
        dataSoleDTO.setStatus("1");
        dataSoleDTO.setApiCode(proxyJson.getString(context.getApiCode()));
        dataSoleDTO.setCaseNum(transfer.getCustNum());
        dataSoleDTO.setDataId(String.valueOf(transfer.getId()));
        String decode = BrCipherMaker.getInstance().decode(json.getString("cell"));
        dataSoleDTO.setPhone(Md5Utils.cell32(decode));
        // 去重参数设置
        dataSoleDTO.setInitId(transfer.getId());
        dataSoleDTO.setSoleField(SoleFieldEnum.CELL_STATUS_SOLE.getValue());
        dataSoleDTO.setSoleType(1);
        PeriodOfValidityBO periodOfValidityBO = syncUserValidityPeriodsBO.getBuilders().get(0).addDateString().addOfDayTimeStrString().builder();
        dataSoleDTO.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        dataSoleDTO.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        dataSoleDTO.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        log.warn("数禾促首借推送百可录黑名单,apiCode={},custNum={}", proxyJson.getString(context.getApiCode()), transfer.getCustNum());
        return dataSoleDTO;

    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {

        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
            String userType = transfer.getUserType();
            if (!"促首借".equals(userType)) {
                return false;
            }
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.isNotEmpty(reserveField1) && JSON.isValid(reserveField1)) {
                JSONObject reserveFieldObject = JSONObject.parseObject(reserveField1);
                String usrLoanSucBtcashLimt1st = reserveFieldObject.getString("usr_loan_suc_btcash_limt_1st");
                return StringUtils.isNotEmpty(usrLoanSucBtcashLimt1st);
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "ShuHe_CuShouJie_TransferData_To_Biocloo";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.BIOCLOO_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
