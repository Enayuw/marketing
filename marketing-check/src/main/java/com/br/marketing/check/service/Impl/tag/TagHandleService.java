package com.br.marketing.check.service.Impl.tag;

public interface TagHandleService {
    void calculateTagData();


    Boolean tagIsEnabled(String apiCode, String tagCode);

}