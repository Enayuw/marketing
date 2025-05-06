package com.br.marketing.service.carclue.web;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.dto.ExecuteCarClueDTO;
import com.br.marketing.entity.CarClueInfo;

import java.util.List;

/**
 * 车线索列表
 * return null
 * @author guangxiu.li
 * @date 2025/1/14
 * @description
 */
public interface CarClueReportService {

    /**
     * @param request:
     * return PageResultReturn
     * @author guangxiu.li
     * @date 2025/1/14
     * {@link PageResultReturn}
     * @description
     */
    PageResultReturn getReportList(CarClueReportDTO request);

    /**
     * 编辑车线索信息
     * @param voList
     * @return
     */
    ApiResult<Boolean> editCarClues(List<CarClueInfo> voList);


    ApiResult<Boolean> executeClueData(ExecuteCarClueDTO dto);
}
