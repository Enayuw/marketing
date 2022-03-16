package com.br.marketing.rule.shuhe;

import com.br.marketing.client.dassservice.input.black.BlackListDTO;
import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.rule.AssembleData;
import org.springframework.stereotype.Service;

@Service
public class ShuHeCustomerCallRecord implements AssembleData<BlackListDTO> {
    @Override
    public BlackListDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
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
}
