package com.br.marketing.service;

import com.br.marketing.entity.ShuheBlackPhoneRecord;

import java.util.List;

public interface IShuheBlackPhoneRecordService {

    /**
     * 判断推送电销手机号是否重复
     * @param phone 手机号
     * @param date 今日（yyyy-MM-dd）
     * @return true 表示重复，false，表示不重复
     */
    boolean isRepeatPhone(String phone,String date);




    void saveBatch(List<ShuheBlackPhoneRecord> shuheBlackPhoneRecordList);

}
