package com.br.marketing.mapper;


import com.br.marketing.entity.StraHisFile;
import com.br.marketing.vo.TaskExtendInfoVO;
import com.br.marketing.vo.TaskTemplateVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
@Repository
public interface StraHisFileMapper extends StraHisFileMapperBase {

    List<TaskExtendInfoVO> getExtendInfosByFileIds(@Param("fileIds") List<Long> fileIds);

    List<StraHisFile> getFileByRule(@Param("time") Date time, @Param("ruleNumber") String ruleNumber
            ,@Param("pushStatusList") List<Integer> pushStatusList, @Param("isOrNoStatus")Integer isOrNoStatus);

    List<String> getFileById(@Param("ids") List<String> ids);

    List<TaskTemplateVO> getFileByruleNameShorts(@Param("ruleNameShorts") List<String> ruleNameShorts, @Param("apiCode") String apiCode);

    List<StraHisFile> getLatestRecord(@Param("apiCode") String apiCode, @Param("scoreDate") String scoreDate);


    StraHisFile getTaskbyDataContion(@Param("apiCode") String apiCode,@Param("condition") String condition);

    StraHisFile getStFileByBatchNumber(@Param("batchNumber") String batchNumber);


    String getCondition(@Param("batchNumber") String batchNumber);

    StraHisFile getLastTaskByApiCode(@Param("apiCode")String apiCode);



}