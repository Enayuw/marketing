package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingDataValidConfig;

/**
 * -------------------------------
 *
 * @author guangxiu.li
 * @Description 有效期更改接口类
 * @Date 2023/10/08 10:19 AM
 * ------------------------------
 */
public interface MarketingDataValidityChangeService {

    /**
     * 获取有效期记录列表
     * @param current
     * @param size
     * @param apiCode
     * @param isDel
     * @param userType
     * @param validStartDate
     * @param validEndDate
     * @param validDays
     * @param validType
     * @param appletDate
     * @param createTime
     * @param updateTime
     * @return
     */
    PageResultReturn list(int current, int size, String apiCode, int isDel, String userType, String validStartDate, String validEndDate , String validDays , int validType, String appletDate, String createTime, String updateTime);

    /**
     * 新增有效期
     * @param marketingDataValidConfig
     * @return
     */
    boolean save(MarketingDataValidConfig marketingDataValidConfig);

    /**
     * 根据id删除有效期记录
     * @param id
     * @return
     */
    Result delTask(Long id);

    /**
     * 修改有效期记录
     * @param id
     * @param validStartDate
     * @param validEndDate
     * @return
     */
    boolean updateById(Long id, String validStartDate, String validEndDate);
}
