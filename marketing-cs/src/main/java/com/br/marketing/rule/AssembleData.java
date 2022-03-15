package com.br.marketing.rule;

import com.br.marketing.origin.TransmitFact;

public interface AssembleData<T extends InterfaceParams> {

    T assemble(TransmitFact transmitFact);

    boolean isNeedAssemble(TransmitFact transmitFact);

    String label();

    Integer dataDirection();
}
