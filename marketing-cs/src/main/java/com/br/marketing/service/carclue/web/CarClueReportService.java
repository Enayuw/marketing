package com.br.marketing.service.carclue.web;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.dto.DataExportTaskDTO;
import com.br.marketing.dto.ExecuteCarClueDTO;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.entity.auth.MarketingUserDetail;

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


    ApiResult<Boolean> executeClueData(ExecuteCarClueDTO dto, MarketingUserDetail user);

    /**
     * 创建任务
     * @param dto 任务参数
     * @param user 用户信息
     * @return 返回创建成功的任务ID，失败返回null
     */
    ApiResult<Boolean> createTask(DataExportTaskDTO dto, MarketingUserDetail user);

}
