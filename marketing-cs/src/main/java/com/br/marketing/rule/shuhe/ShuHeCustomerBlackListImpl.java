package com.br.marketing.rule.shuhe;

import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.origin.ShuHeProcessHandlerContext;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IPushShuheTransferDataService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 数禾转化推送客服黑名单 业务
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/18 14:45
 */
@Service
public class ShuHeCustomerBlackListImpl implements AssembleData<BlackDetailDTO> {
    @Resource
    private IPushShuheTransferDataService iPushShuheTransferDataService;

    @Override
    public BlackDetailDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ShuHeProcessHandlerContext shuHeContext = (ShuHeProcessHandlerContext) context;
        final IUserType iUserType = shuHeContext.getiUserType();
        final Date creatTime = shuHeContext.getCreatTime();
        final CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
        BlackDetailDTO blackDetailDTO = new BlackDetailDTO();
        blackDetailDTO.setDataId(String.valueOf(transfer.getId()));
        blackDetailDTO.setExpireDate(iUserType.getBlackExpireDate(creatTime));
        blackDetailDTO.setPhone(caseShuheUser.getCell());
        return blackDetailDTO;
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
            final CaseShuheUser caseShuheUser = shuHeContext.getCaseShuheUser();
            if (iUserType.isBlack(caseShuheUser)) {
                ((ShuHeProcessHandlerContext) context).setContinueJudgeRule(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "ShuHe_TransferData_CustomerBlackList";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_BLACK_LIST.getCode();
    }
}
