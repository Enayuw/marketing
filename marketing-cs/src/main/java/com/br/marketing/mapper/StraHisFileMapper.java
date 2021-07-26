package com.br.marketing.mapper;


import com.br.marketing.vo.TaskExtendInfoVO;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
@Repository
public interface StraHisFileMapper extends StraHisFileMapperBase {

    List<TaskExtendInfoVO> getExtendInfosByFileIds(List<Long> fileIds);
}