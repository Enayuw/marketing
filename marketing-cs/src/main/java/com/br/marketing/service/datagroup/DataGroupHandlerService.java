package com.br.marketing.service.datagroup;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.datagroup.DataGroupConfgDTO;
import com.br.marketing.entity.DataGroupTask;
import com.br.marketing.vo.datagroup.DataGroupConfigVO;

import java.util.HashMap;
import java.util.List;

/**
 * 数据分组service
 */
public interface DataGroupHandlerService {


    List<DataGroupConfigVO> configList(String ids,String apiCode);

    ApiResult updateConfig(DataGroupConfgDTO dto);

    ApiResult addOrDeleteConfig(DataGroupConfgDTO dto);

    void dataGroupHandler(DataGroupTask dataGroupTask);

    List<String> extendField(String ids, String apiCode);

    HashMap getGroupFieldPercent(String field, Long id);
}
