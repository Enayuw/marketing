package com.br.marketing.service;

/**
 * 数据清洗处理接口
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-24
 */
public interface IDataCleaningGeneralService {

    void isAction();
    /**
     * 读取文件并进行清洗
     * @Author yu.xia@brgroup.com
     * @Date 2024/5/24 11:31
     * @param
     */
    void action();
    /**
     * 数据清洗试跑
     * @Author yu.xia@brgroup.com
     * @Date 2024/5/24 11:31
     * @param id 试跑任务的id
     */
    void pilotAction(Long id);

}
