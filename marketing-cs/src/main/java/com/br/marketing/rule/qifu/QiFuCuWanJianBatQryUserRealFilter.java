package com.br.marketing.rule.qifu;

import com.br.marketing.client.robotaiapi.input.InterfaceData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 360促完件用户信息批量查询
 *
 * @Author lixiang
 * @Date 2024-10-19
 */
@Service
@Slf4j
public class QiFuCuWanJianBatQryUserRealFilter implements AssembleData<InterfaceData<MarketingSyncUser>> {

    private static final String TITLE = "【360促完件用户信息批量查询-自动化过滤】";

    @Override
    public InterfaceData<MarketingSyncUser> assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingSyncUser marketingSyncUser = (MarketingSyncUser) transmitFact;
        InterfaceData<MarketingSyncUser> interfaceData = new InterfaceData<>();
        interfaceData.setData(marketingSyncUser);
        return interfaceData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (!(transmitFact instanceof MarketingSyncUser)) {
            return false;
        }
        return true;
    }

    @Override
    public String label() {
        return "QiFu_CuWanJian_BatQryUserReal_Filter";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.QIFU_CUWANJIAN_BAT_QRY_USER_REAL.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
