package com.br.marketing.rule.shuhe;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.origin.ShuHeProcessHandlerContext;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.IPushShuheTransferDataService;
import com.br.marketing.service.ITransferSyncUserService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
 * 数禾推送转化至客服转化 业务
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/17 19:35
 */
@Service
public class ShuHeCustomerTransferImpl implements AssembleData<ConversionData> {
    private final static String HAS_TRANS_FER = "1";
    private final static String NO_HAS_TRANSFER = "0";
    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;
    @Resource
    private IPushShuheTransferDataService iPushShuheTransferDataService;
    @Resource
    private ITransferSyncUserService iTransferSyncUserService;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setInversionStatus(HAS_TRANS_FER.equals(transfer.getIfTransform())
                ? NO_HAS_TRANSFER : (NO_HAS_TRANSFER.equals(transfer.getIfTransform())
                ? HAS_TRANS_FER : transfer.getIfTransform()));
        conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        Map<String, MarketingSyncUser> map = context.getCustomerMap();
        if (map != null && map.containsKey(transfer.getCustNum())) {
            MarketingSyncUser marketingSyncUser = map.get(transfer.getCustNum());
            conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
            conversionData.setTaskId(marketingSyncUser.getCusBatch());
        } else {
            conversionData.setPhone("");
            conversionData.setTaskId("");
        }
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        if (!(context instanceof ShuHeProcessHandlerContext)) {
            ShuHeProcessHandlerContext shuHeContext = new ShuHeProcessHandlerContext(context);
            iPushShuheTransferDataService.handlerContext(shuHeContext, transfer);
            context = shuHeContext;
        }
        ShuHeProcessHandlerContext shuHeContext = (ShuHeProcessHandlerContext) context;
        if (shuHeContext.isContinueJudgeRule()) {
            final IUserType iUserType = shuHeContext.getiUserType();
            final Date creatTime = shuHeContext.getCreatTime();
            final CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
            if (iUserType.dataPeriodOfValidity(iMarketingSyncUserService, creatTime)) {
                MarketingTransferSyncUser transferSyncUser = new MarketingTransferSyncUser();
                transferSyncUser.setId(transfer.getId());
                if (iUserType.isTurn(caseShuheUser) || iUserType.isEmpty(caseShuheUser)) {
                    transferSyncUser.setIfTransform("2");
                    iTransferSyncUserService.insertSelective(transferSyncUser);
                    ((ShuHeProcessHandlerContext) context).setContinueJudgeRule(false);
                } else if (iUserType.ifTransfer(caseShuheUser, creatTime)) {
                    // 转化
                    transferSyncUser.setIfTransform("1");
                    iTransferSyncUserService.insertSelective(transferSyncUser);
                    ((ShuHeProcessHandlerContext) context).setContinueJudgeRule(false);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "ShuHe_OverdueData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }
}
