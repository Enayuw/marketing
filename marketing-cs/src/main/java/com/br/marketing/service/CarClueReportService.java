package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.vo.CarClueInfoVo;
import com.br.marketing.vo.SyncConfigEditVO;

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
     * @param vo
     * @return
     */
    ApiResult<Boolean> editCarClues(List<CarClueInfoVo> voList);

}
