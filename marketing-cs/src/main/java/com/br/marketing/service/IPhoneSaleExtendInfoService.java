package com.br.marketing.service;

public interface IPhoneSaleExtendInfoService {

    /**
     * 判断推送电销手机号是否重复
     * @param phone 手机号
     * @param dxType 推电销类型  shuheBlack
     * @param date 今日（yyyy-MM-dd）
     * @return true 表示重复，false，表示不重复
     */
    boolean isRepeatPhone(String phone,String dxType,String date);

}
