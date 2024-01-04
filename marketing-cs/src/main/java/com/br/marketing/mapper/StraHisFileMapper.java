package com.br.marketing.mapper;


import com.br.marketing.entity.StraHisFile;
import com.br.marketing.vo.TaskExtendInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
@Repository
public interface StraHisFileMapper extends StraHisFileMapperBase {

    List<TaskExtendInfoVO> getExtendInfosByFileIds(@Param("fileIds") List<Long> fileIds);

    List<StraHisFile> getFileByRule(@Param("time") Date time, @Param("ruleNumber") String ruleNumber);
}