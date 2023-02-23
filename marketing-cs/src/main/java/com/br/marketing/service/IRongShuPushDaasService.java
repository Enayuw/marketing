package com.br.marketing.service;

public interface IRongShuPushDaasService {


    /**
     * 榕树推电销Ibu过滤条件
     *
     * @param apiCode  apiCode
     * @param custNum custNum
     * @return boolean
     * @desc  需要过滤的返回true，不需要过滤返回false
     */
    public boolean isFilter(String apiCode, String custNum, String tcId);
}
