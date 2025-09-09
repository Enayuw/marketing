package com.br.marketing.service;

import com.br.marketing.dto.linkgo.CreateTaskDataDTO;
import java.util.List;

/**
 * Link rule service interface
 * @author system
 * @date 2025/01/17
 */
public interface LinkRuleService {

    /**
     * Create export tasks in batch
     * Receive task data list from marketingkit_cn project
     * @param taskDataList task data list
     * @return creation result
     */
    Boolean batchCreateTasks(List<CreateTaskDataDTO> taskDataList);
}
