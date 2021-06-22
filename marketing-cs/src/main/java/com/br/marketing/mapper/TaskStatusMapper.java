package com.br.marketing.mapper;

import com.br.marketing.entity.TaskStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Bairong on 2019/10/23.
 */
@Repository
public interface TaskStatusMapper extends TaskStatusMapperBase {

    void insertTaskStatus(TaskStatus bts);

    List<TaskStatus> queryOnceBts(String batchNumber);
}
