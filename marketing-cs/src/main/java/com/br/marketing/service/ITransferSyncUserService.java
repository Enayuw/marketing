package com.br.marketing.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.CaseShuheUploadData;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;

/**
 * 客户转化数据记录业务接口
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/15 10:00
 */
public interface ITransferSyncUserService {

    int insertSelective(MarketingTransferSyncUser marketingTransferSyncUser);

    /**
     * 2022/10/21 17:36
     * 保存业务数据，写入失败时全部回滚
     * 1. 先保存info表信息
     * 2. 再保存详情表
     */
    void insertInfoAndSync(MarketingTransferSyncUser marketingTransferSyncUser
            , MarketingTransferInfo transferInfo, CaseShuheUser caseShuheUser) throws Exception;

    int updateByPrimaryKeySelective(MarketingTransferSyncUser marketingTransferSyncUser);

}
