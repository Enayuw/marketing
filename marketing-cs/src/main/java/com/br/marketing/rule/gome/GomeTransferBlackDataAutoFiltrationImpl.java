package com.br.marketing.rule.gome;

import com.alibaba.fastjson.JSON;
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
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * D20241029国美数据清洗&自动化过滤-3710076（营销→外呼）
 * 需求：https://c.100credit.cn/pages/viewpage.action?pageId=186188189
 *
 * @Author: bin.li1@brgroup.com
 * @Date: 2024-11-01
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GomeTransferBlackDataAutoFiltrationImpl implements AssembleData<ConversionData> {

    private final static String IS_BLACK_1 = "1";
    private final static String INVERSION_STATUS_2 = "2";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    private final MarketingSyncUserMapper marketingSyncInfoMapper;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        final String apiCode = transfer.getApiCode();
        final String custNum = transfer.getCustNum();
        final String cid = transfer.getCid();
        conversionData.setCid(cid);
        conversionData.setCaseNum(custNum);
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transfer.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transfer.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        conversionData.setInversionStatus(INVERSION_STATUS_2);
        MarketingSyncUser syncUser = marketingSyncInfoMapper.selectSynsUserByCustNumLastWithStatus(apiCode, custNum);
        if (syncUser == null || StringUtils.isEmpty(syncUser.getCell())) {
            String message = String.format("国美黑名单自动化过滤未查询到手机号apiCode:[%s]和cid:[%s]和custNum:[%s]", apiCode,cid, custNum);
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.GUOMEI_PHONENOTFUND.getCode(),
                    message));
            return null;
        } else {
            conversionData.setPhone(BrCipherMaker.getInstance().decode(syncUser.getCell()));
        }
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.isNotBlank(reserveField1)) {
                String isBlack = JSON.parseObject(reserveField1).getString("isBlack");
                return IS_BLACK_1.equals(isBlack);
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "Gome_Transfer_BlackData_Auto_Filtration";
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
