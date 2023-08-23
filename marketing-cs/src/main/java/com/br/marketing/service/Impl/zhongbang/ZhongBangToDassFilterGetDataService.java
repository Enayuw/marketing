package com.br.marketing.service.Impl.zhongbang;

import com.br.marketing.entity.MarketingTransferSyncUser;

import java.util.List;

/**
 * @Description 众邦转化数据推人工转化过滤，基础数据获取接口
 * @Author hong.chen
 * @CreateTime 2023/08/23
 */
public interface ZhongBangToDassFilterGetDataService {
    /**
     * 首次JOB，获取转化数据：request_date=T日且(ifApply=1或ifLent=1)
     * @return 转化数据列表
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserListFirst(String tCid, String apiCode, String requestDate, Long indexId);

    /**
     * 非首次JOB
     * @return 转化数据列表
     */
    List<MarketingTransferSyncUser> getMarketingTransferSyncUserListNoFirst(String tCid, String apiCode, String requestDate, Long indexId);
}
