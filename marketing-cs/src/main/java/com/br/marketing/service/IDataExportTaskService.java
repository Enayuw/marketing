package com.br.marketing.service;


import com.br.marketing.dto.DataExportTaskDTO;
import com.br.marketing.entity.DataExportTask;
import com.br.marketing.entity.auth.MarketingUserDetail;

/**
 * 数据导出任务服务接口
 * @author guangxiu.li
 * @date 2025/1/14
 * @description
 */
public interface IDataExportTaskService {
    /**
     * 创建任务
     * @param dto 任务参数
     * @param user 用户信息
     * @return 返回创建成功的任务ID，失败返回null
     */
    Long createTask(DataExportTaskDTO dto, MarketingUserDetail user);

}
