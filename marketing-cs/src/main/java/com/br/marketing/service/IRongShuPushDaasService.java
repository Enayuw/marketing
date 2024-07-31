package com.br.marketing.service;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;

public interface IRongShuPushDaasService {


    /**
     * 榕树推电销Ibu过滤条件
     *
     * @param apiCode  apiCode
     * @param custNum custNum
     * @return boolean
     * @desc  需要过滤的返回true，不需要过滤返回false
     */
    boolean isFilter(String apiCode, String custNum, String tcId);
    /**
     * 使用前提：使用新版本有效期已经筛选过数据了，不然需要注意是否满足使用场景
     * 剔除数据中转化数据是userType=4或userType=5的
     * @Author yu.xia@brgroup.com
     * @Date 2024/7/24 21:37
     * @param apiCode apiCode
     * @param custNum custNum
     * @param tcId  tcId
     * @param boMap  查询有效期得到的结果（包含多个有效期对应时间段和）
     * @return boolean
     */
    boolean isFilterUserUserType(String apiCode, String custNum, String tcId, SyncUserValidityPeriodsBO boMap);

}
