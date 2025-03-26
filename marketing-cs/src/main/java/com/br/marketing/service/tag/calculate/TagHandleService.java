package com.br.marketing.service.tag.calculate;

public interface TagHandleService {
    void calculateTagData();


    Boolean tagIsEnabled(String apiCode, String tagCode);

}