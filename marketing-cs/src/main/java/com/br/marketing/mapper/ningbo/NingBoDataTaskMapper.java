package com.br.marketing.mapper.ningbo;

import com.br.marketing.entity.ningbo.NingBoDataTask;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface NingBoDataTaskMapper extends NingBoDataTaskMapperBase {

    /**
     * 创建或更新运行中的任务状态
     *
     * @param collectDate 任务日期
     * @param taskType    任务类型：1-下载，2-上传
     * @return 任务对象
     */
    NingBoDataTask createOrUpdateRunningTask(@Param("collectDate") Date collectDate,
                                             @Param("taskType") Integer taskType);

    /**
     * 根据ID更新任务状态
     *
     * @param id            任务ID
     * @param status        状态：0-待执行，1-执行中，2-执行成功，3-执行失败
     * @param resultMessage 结果信息
     * @return 影响的行数
     */
    int updateTaskStatus(@Param("id") Long id,
                         @Param("status") Integer status,
                         @Param("resultMessage") String resultMessage);

    /**
     * 根据日期和任务类型查询任务
     *
     * @param taskDate 任务日期
     * @param taskType 任务类型
     * @return 任务对象
     */
    NingBoDataTask selectByDateAndType(@Param("taskDate") Date taskDate,
                                       @Param("taskType") Integer taskType);

    /**
     * 更新任务
     *
     * @param task 任务对象
     * @return 影响的行数
     */
    int update(NingBoDataTask task);
}