package com.br.marketing.service.Impl;

import com.br.marketing.dto.DataExportTaskDTO;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.service.IDataExportTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 创建任务
 *
 * @author guangxiu.li
 * @date 2025/06/10
 * @description
 */
@Service
@Slf4j
public class IDataExportTaskServiceImpl implements IDataExportTaskService {

    @Override
    public Boolean createTask(DataExportTaskDTO dto, MarketingUserDetail user) {
        log.info("创建任务：{}", dto);
        Long userId = Long.valueOf(user.getId());
        String userName = user.getUserName();
        return null;
    }
}
