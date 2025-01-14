package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueReportDTO;

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

}
