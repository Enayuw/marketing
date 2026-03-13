package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.datamap.*;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.datamap.TrackingLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 跟踪链路配置管理控制器
 * 
 * @author bingxu.kong
 * @since 2025/10/16
 */
@Slf4j
@RestController
@RequestMapping("/dataMap/tracking")
@Tag(name = "数据地图", description = "标签配置数据地图管理")
public class TrackingLinkController {
    
    @Resource
    private TrackingLinkService trackingLinkService;

    @GetMapping("/getNodesByApiCode")
    @Operation(summary = "查询节点列表", description = "分页获取节点列表信息")
    @AddDataAuthBusiness
    public ApiResult<List<NodeDictVO>> getNodesByApiCode(@RequestParam String apiCode) {
        try {
            return trackingLinkService.selectNodesByApiCode(apiCode);
        } catch (Exception e) {
            log.error("Failed to query node list: apiCode={}", apiCode, e);
            return new ApiResult<List<NodeDictVO>>().fail("Failed to query node list: " + e.getMessage());
        }
    }

    @PostMapping("/saveLink")
    @Operation(summary = "保存链路", description = "创建或更新链路，linkId为空时创建，不为空时更新")
    @AddDataAuthBusiness
    public ApiResult<CreateLinkResponse> saveLink(@RequestBody @Validated CreateLinkRequest request) {
        try {
            return trackingLinkService.saveLink(request);
        } catch (Exception e) {
            log.error("保存链路失败: linkId={}", request.getLinkId(), e);
            return new ApiResult<CreateLinkResponse>().fail("保存链路失败: " + e.getMessage());
        }
    }

    @PostMapping("/getLinkDetail")
    @Operation(summary = "获取链路详情信息", description = "获取链路详情信息，支持按日期查询，日期格式：yyyy-MM-dd，若不传则默认查询当天数据")
    @AddDataAuthBusiness
    public ApiResult<LinkDetailResponse> getLinkDetail(@RequestBody @Validated QueryLinkRequest request) {
        try {
            return trackingLinkService.getLinkDetail(request);
        } catch (Exception e) {
            log.error("查询链路详情失败: linkId={}, startDate={}, endDate={}", 
                    request.getLinkId(), request.getStartDate(), request.getEndDate(), e);
            return new ApiResult<LinkDetailResponse>().fail("查询链路详情失败: " + e.getMessage());
        }
    }

    @PostMapping("/getLinkDetailListByApiCode")
    @Operation(summary = "根据apiCode和日期查询链路详情列表", description = "根据apiCode和日期查询链路详情列表，支持按日期查询，日期格式：yyyy-MM-dd，若不传则默认查询当天数据")
    @AddDataAuthBusiness
    public ApiResult<List<LinkDetailResponse>> getLinkDetailListByApiCode(@RequestBody @Validated QueryLinkByApiCodeRequest request) {
        try {
            return trackingLinkService.getLinkDetailListByApiCode(request);
        } catch (Exception e) {
            log.error("根据apiCode查询链路详情列表失败: apiCode={}, startDate={}, endDate={}", 
                    request.getApiCode(), request.getStartDate(), request.getEndDate(), e);
            return new ApiResult<List<LinkDetailResponse>>().fail("根据apiCode查询链路详情列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/getLinkList")
    @Operation(summary = "获取链路列表", description = "获取链路列表信息")
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getLinkList(@RequestBody @Validated LinkListRequest request) {
        try {
            PageResultReturn pageResultReturn = trackingLinkService.selectLinkList(request);

            return new ApiResult<PageResultReturn>().success(pageResultReturn);
        } catch (Exception e) {
            log.error("Failed to query link list", e);
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/updateLinkStatus")
    @Operation(summary = "开启/禁用链路", description = "开启/禁用链路")
    @AddDataAuthBusiness
    public ApiResult<Boolean> updateLinkStatus(@RequestBody @Validated UpdateLinkStatusRequest request) {
        try {
            return trackingLinkService.updateLinkStatus(request);
        } catch (Exception e) {
            log.error("Failed to update link status", e);
            return new ApiResult<Boolean>().fail("Failed to update link status: " + e.getMessage());
        }
    }

    @GetMapping("/deleteLink")
    @Operation(summary = "删除链路", description = "删除链路及其关联的节点、边")
    @AddDataAuthBusiness
    public ApiResult<Boolean> deleteLink(@RequestParam List<Long> ids) {
        try {
            return trackingLinkService.deleteLink(ids);
        } catch (Exception e) {
            log.error("Failed to deleteLink link", e);
            return new ApiResult<Boolean>().fail("删除链路失败: " + e.getMessage());
        }
    }


}
