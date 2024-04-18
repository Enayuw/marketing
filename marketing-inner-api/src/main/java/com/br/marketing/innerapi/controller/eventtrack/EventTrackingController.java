package com.br.marketing.innerapi.controller.eventtrack;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.service.eventtrack.EventTrackService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 【统计管理】-【上传记录】-【手机号查询】功能对应的【日志统计tab页签】-【手机号查询统计】埋点查询功能
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-04-15
 */
@RestController
@RequestMapping(value = "/eventTracking")
public class EventTrackingController {

    @Resource
    private EventTrackService eventTrackService;

    @PostMapping("/cellReport/getCellReport")
    public ApiResult<PageResultReturn> getCellReport(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String startTime
            , @RequestParam(required = false) String endTime
            , @RequestParam(required = false) String userName
            , @RequestParam(defaultValue = "create_time") String orderField
            , @RequestParam(defaultValue = "desc") String descField){
        PageResultReturn listPage = eventTrackService.getCellReport(current, size
                , startTime, endTime, userName, orderField, descField);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    @PostMapping("/cellReport/getCellReportDetail")
    public ApiResult<PageResultReturn> getCellReportDetail(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String startTime
            , @RequestParam(required = false) String endTime
            , @RequestParam(required = false) String userName
            , @RequestParam(required = false) String apiCodes
            , @RequestParam(defaultValue = "create_time") String orderField
            , @RequestParam(defaultValue = "desc") String descField){
        PageResultReturn listPage = eventTrackService.getCellReportDetail(current, size
                , startTime, endTime, userName, apiCodes, orderField, descField);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

}
