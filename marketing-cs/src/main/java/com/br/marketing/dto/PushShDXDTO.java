package com.br.marketing.dto;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.entity.PhoneSaleExtendShuhe;

public class PushShDXDTO {
    private LocalFile localFile;
    private PhoneSale phoneSale;
    private PhoneSaleExtendShuhe phoneSaleExtendShuhe;
    private String stauts;

    public LocalFile getLocalFile() {
        return localFile;
    }

    public PushShDXDTO setLocalFile(LocalFile localFile) {
        this.localFile = localFile;
        return this;
    }

    public PhoneSale getPhoneSale() {
        return phoneSale;
    }

    public PushShDXDTO setPhoneSale(PhoneSale phoneSale) {
        this.phoneSale = phoneSale;
        return this;
    }

    public PhoneSaleExtendShuhe getPhoneSaleExtendShuhe() {
        return phoneSaleExtendShuhe;
    }

    public PushShDXDTO setPhoneSaleExtendShuhe(PhoneSaleExtendShuhe phoneSaleExtendShuhe) {
        this.phoneSaleExtendShuhe = phoneSaleExtendShuhe;
        return this;
    }

    public String getStauts() {
        return stauts;
    }

    public PushShDXDTO setStauts(String stauts) {
        this.stauts = stauts;
        return this;
    }
}
