package com.br.marketing.rule;

import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.origin.TransmitFact;

public interface AssembleData<T extends InterfaceParams> {

    T assemble(TransmitFact transmitFact, ProcessHandlerContext context);

    boolean isNeedAssemble(TransmitFact transmitFact, ProcessHandlerContext context);

    String label();

    Integer dataDirection();
}
