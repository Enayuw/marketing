package com.br.marketing.service.Impl.xc;


/**
 * 携程先关表备份接口
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-03-20
 */
public interface TableBackupService {

    /**
     * 周期表处理
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/21 11:09
     * @param daysAgo14 查询时间
     * @param limit 查询page大小
     */
    void loopCycleHandle(String daysAgo14,int limit);
    /**
     * 非周期表备份
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/21 11:09
     * @param daysAgo14 查询时间
     * @param limit 查询page大小
     */
    void robHandle(String daysAgo14,int limit);
    /**
     * 日志表备份方法
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/21 11:09
     * @param daysAgo14 查询时间
     * @param limit 查询page大小
     */
    void logHandle(String daysAgo14,int limit);
    /**
     * 对比表
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/21 11:09
     * @param daysAgo14 查询时间
     * @param limit 查询page大小
     */
    void contrastHandle(String daysAgo14,int limit);

}
