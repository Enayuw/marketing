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
     * @Date 2024/3/20 15:15
     */
    void loopCycleHandle(String daysAgo14,int limit);
    /**
     * 非周期表备份
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/20 15:15
     */
    void robHandle(String daysAgo14,int limit);
    /**
     * 日志表备份方法
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/20 15:15
     * @paramd
     */
    void logHandle(String daysAgo14,int limit);
    /**
     * 对比表
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/20 15:15
     */
    void contrastHandle(String nowString,int limit);

    void testInsert();

}
