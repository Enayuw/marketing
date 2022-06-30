package com.br.marketing.service;

import com.br.marketing.entity.TransferSyncReport;
import com.br.marketing.entity.TransferSyncReportExample;

import java.util.List;
import java.util.Set;

/**
 * 转化数据报表接口
 *
 * @author Guo Zeqiang
 * @dateTime 2022/6/29 10:29
 */
public interface TransferSyncReportService {

    /**
     * 处理转化数据生成数据报表
     *
     * @param dateStrSet         日期字符串集合
     * @param shardingTotalCount 总分片数
     * @param shardingItems      分片
     * @author Guo Zeqiang
     * @dateTime 2022/6/29 16:05
     */
    void reportProcess(Set<String> dateStrSet, int shardingTotalCount, List<Integer> shardingItems);

    /**
     * 处理转化数据生成数据报表
     *
     * @param dateStrSet 日期字符串集合
     * @author Guo Zeqiang
     * @dateTime 2022/6/29 16:05
     */
    void reportProcess(Set<String> dateStrSet);

    /**
     * 获取列表
     *
     * @param example 查询实例
     * @author Guo Zeqiang
     * @dateTime 2022/6/30 10:19
     */
    List<TransferSyncReport> findTransferSyncReportList(TransferSyncReportExample example);
}
