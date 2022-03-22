package com.br.marketing.rule;

import com.br.marketing.context.ProcessHandlerContext;

public interface AssembleData<T extends InterfaceParams> {

    T assemble(Object transmitFact, ProcessHandlerContext context);

    boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context);

    String label();

    Integer dataDirection();

    Integer ruleDataCollection();
}
