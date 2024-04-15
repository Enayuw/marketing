package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.TransferSyncReport;
import com.br.marketing.entity.TransferSyncReportExample;

import java.util.List;
import java.util.Map;
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
    void reportProcess(Set<String> dateStrSet, int shardingTotalCount, List<Integer> shardingItems,String JobName);

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
     * @return List
     * @author Guo Zeqiang
     * @dateTime 2022/6/30 10:19
     */
    List<TransferSyncReport> findTransferSyncReportList(TransferSyncReportExample example);

    /**
     * 客户转化数据统计报表列表
     *
     * @param current         当前页
     * @param size            页面大小
     * @param cidOrName       模糊查询
     * @param appletTimeStart 上传开始时间
     * @param appletTimeEnd   上传结束时间
     * @param apiCodes        code字符串
     * @param userTypes       type 字符串
     * @return Page
     * @author Guo Zeqiang
     * @dateTime 2022/6/30 10:19
     */
    PageResultReturn getTransferSyncReportList(int current, int size, String cidOrName, String appletTimeStart
            , String appletTimeEnd, String apiCodes, String userTypes);

    /**
     * 客户转化数据统计报表总计
     *
     * @param cidOrName       模糊查询
     * @param appletTimeStart 上传开始时间
     * @param appletTimeEnd   上传结束时间
     * @param apiCodes        code字符串
     * @param userTypes       type 字符串
     * @return Map
     * @author Guo Zeqiang
     * @dateTime 2022/6/30 10:19
     */
    Map<String, String> getTransferSyncReportListTotal(String cidOrName, String appletTimeStart, String appletTimeEnd
            , String apiCodes, String userTypes);

    /**
     * 准实时数据计数碎片统计（转化）
     *
     * @param dataCountFragmentsMgs 碎片消息
     * @return 消费结果
     * @author Guo Zeqiang
     * @dateTime 2024-03-06 15:47
     */
    Result<Boolean> nearRealtimeDataCountFragmentsStatis(String dataCountFragmentsMgs);

}
