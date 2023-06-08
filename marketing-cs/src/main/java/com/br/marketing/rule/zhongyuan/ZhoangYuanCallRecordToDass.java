package com.br.marketing.rule.zhongyuan;

import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.rule.AssembleData;

/**
 * 中原消金通话明细推送人工
 *
 * @author Guo Zeqiang
 * @dateTime 2023-06-08 16:44
 */
public class ZhoangYuanCallRecordToDass implements AssembleData<RealTimeUserDataDTO> {
    @Override
    public RealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return false;
    }

    @Override
    public String label() {
        return null;
    }

    @Override
    public Integer dataDirection() {
        return null;
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
