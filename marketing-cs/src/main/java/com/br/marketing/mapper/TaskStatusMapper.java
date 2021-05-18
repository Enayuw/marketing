package com.br.marketing.mapper;

import com.br.marketing.entity.TaskStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Bairong on 2019/10/23.
 */
@Repository
public interface TaskStatusMapper extends TaskStatusMapperBase {
    TaskStatus queryBts(String batchNumber);

    void insertTaskStatus(TaskStatus bts);

    List<TaskStatus> queryOnceBts(String batchNumber);

    TaskStatus queryNewestBts(String batchNumber);

    void updateTaskStatus(TaskStatus bts);

    TaskStatus queryTodayIncrBts(String batchNumber);

    List<TaskStatus> queryBtsList(String batchNumber);
}
