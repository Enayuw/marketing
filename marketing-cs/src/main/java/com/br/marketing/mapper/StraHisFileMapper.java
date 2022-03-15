package com.br.marketing.mapper;


import com.br.marketing.vo.TaskExtendInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface StraHisFileMapper extends StraHisFileMapperBase {

    List<TaskExtendInfoVO> getExtendInfosByFileIds(@Param("fileIds") List<Long> fileIds);
}