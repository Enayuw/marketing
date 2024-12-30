package com.br.marketing.rule.samoye;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * D20241218萨摩耶转化自动化过滤
 * https://c.100credit.cn/pages/viewpage.action?pageId=190665894
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-12-20
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SaMoYeTransferBlackDataAutoFiltrationImpl implements AssembleData<ConversionData> {

    /**
     * 是否已转化 2 客服接口字段对应关系
     */
    private final static String INVERSION_STATUS_2="2";
    private final MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser marketingTransferSyncUser = (MarketingTransferSyncUser) transmitFact;
        String cid = marketingTransferSyncUser.getCid();
        String custNum = marketingTransferSyncUser.getCustNum();
        String apiCode = marketingTransferSyncUser.getApiCode();
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(marketingTransferSyncUser.getId().toString());
        conversionData.setCid(marketingTransferSyncUser.getCid());
        conversionData.setCaseNum(custNum);
        conversionData.setGroupType(marketingTransferSyncUser.getUserType());
        conversionData.setInitId(marketingTransferSyncUser.getId());
        conversionData.setPartnerProcessDate(
                DateUtils.format(marketingTransferSyncUser.getCreateTime(), DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        conversionData.setInversionStatus(INVERSION_STATUS_2);
        MarketingSyncUser syncUser = marketingSyncUserMapper.selectSynsUserByCustNumLastWithStatus(apiCode, custNum);
        if (syncUser == null || StringUtils.isEmpty(syncUser.getCell())) {
            conversionData.setPhone("");
        } else {
            conversionData.setPhone(BrCipherMaker.getInstance().decode(syncUser.getCell()));
        }
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(marketingTransferSyncUser, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if(StringUtils.isNotBlank(reserveField1)){
                JSONObject jsonObject = JSON.parseObject(reserveField1);
                if(null != getBlackInversionStatus(jsonObject)){
                    return true;
                }
            }
        }
        return false;
    }

    private String getBlackInversionStatus(JSONObject reserveField1) {
        // 情况6
        String accountClose = reserveField1.getString("accountclose");
        // 情况1
        String isBlack = reserveField1.getString("isBlack");
        if("1".equals(accountClose) || "1".equals(isBlack)){
            return INVERSION_STATUS_2;
        }
        return null;
    }

    @Override
    public String label() {
        return "SaMoYe_TransferDataBlack_Customer_Auto_Filtration";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
