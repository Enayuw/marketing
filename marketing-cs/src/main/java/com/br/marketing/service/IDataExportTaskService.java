package com.br.marketing.service;


import com.br.marketing.dto.DataExportTaskDTO;
import com.br.marketing.entity.auth.MarketingUserDetail;

/**
 * 车线索列表
 * return null
 * @author guangxiu.li
 * @date 2025/1/14
 * @description
 */
public interface IDataExportTaskService {
    /**
     * 创建任务
     * @param dto
     * @param user
     * @return
     */
    Boolean createTask(DataExportTaskDTO dto, MarketingUserDetail user);
}
