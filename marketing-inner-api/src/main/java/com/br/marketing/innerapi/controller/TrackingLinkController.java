package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.datamap.*;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.datamap.TrackingLinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
@Api(value = "数据地图", tags = "标签配置数据地图管理", produces = "application/json", consumes = "application/json", protocols = "http")
public class TrackingLinkController {
    
    @Resource
    private TrackingLinkService trackingLinkService;

    @GetMapping("/getNodesByApiCode")
    @ApiOperation(value = "查询节点列表", notes = "分页获取节点列表信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "apiCode", value = "查询参数", required = true, dataType = "apiCode")
    })
    @AddDataAuthBusiness
    public ApiResult<List<NodeDictVO>> getNodesByApiCode(@RequestParam String apiCode) {
        try {
            return trackingLinkService.selectNodesByApiCode(apiCode);
        } catch (Exception e) {
            log.error("Failed to query node list: apiCode={}", apiCode, e);
            return new ApiResult<List<NodeDictVO>>().fail("Failed to query node list: " + e.getMessage());
        }
    }

    @PostMapping("/createLink")
    @ApiOperation(value = "创建链路", notes = "创建链路")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "查询参数", required = true, dataType = "CreateLinkRequest")
    })
    @AddDataAuthBusiness
    public ApiResult<CreateLinkResponse> createLink(@RequestBody @Validated CreateLinkRequest request) {
        try {
            return trackingLinkService.createLink(request);
        } catch (Exception e) {
            log.error("Failed to create link", e);
            return new ApiResult<CreateLinkResponse>().fail("Failed to create link: " + e.getMessage());
        }
    }

    @GetMapping("/getLinkDetail")
    @ApiOperation(value = "获取链路详情信息", notes = "获取链路详情信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "linkId", value = "查询参数", required = true, dataType = "linkId")
    })
    @AddDataAuthBusiness
    public ApiResult<LinkDetailResponse> getLinkDetail(@RequestParam Long linkId) {
        try {
            return trackingLinkService.getLinkDetail(linkId);
        } catch (Exception e) {
            log.error("Failed to get link details: linkId={}", linkId, e);
            return new ApiResult<LinkDetailResponse>().fail("Failed to get link details: " + e.getMessage());
        }
    }

    @PostMapping("/updateLink")
    @ApiOperation(value = "更新链路配置", notes = "更新链路配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "查询参数", required = true, dataType = "CreateLinkRequest")
    })
    @AddDataAuthBusiness
    public ApiResult<Boolean> updateLink(@RequestBody @Validated CreateLinkRequest request) {
        try {
            return trackingLinkService.updateLink(request);
        } catch (Exception e) {
            log.error("Failed to update link: linkId={}", request.getLinkId(), e);
            return new ApiResult<Boolean>().fail("Failed to update link: " + e.getMessage());
        }
    }

    @GetMapping("/getLinkList")
    @ApiOperation(value = "获取链路列表", notes = "获取链路列表信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "查询参数", required = true, dataType = "LinkListRequest")
    })
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getLinkList(LinkListRequest request) {
        try {
            PageResultReturn pageResultReturn = trackingLinkService.selectLinkList(request);

            return new ApiResult<PageResultReturn>().success(pageResultReturn);
        } catch (Exception e) {
            log.error("Failed to query link list", e);
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/updateLinkStatus")
    @ApiOperation(value = "开启/禁用链路", notes = "开启/禁用链路")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "查询参数", required = true, dataType = "UpdateLinkStatusRequest")
    })
    @AddDataAuthBusiness
    public ApiResult<Boolean> updateLinkStatus(@RequestBody @Validated UpdateLinkStatusRequest request) {
        try {
            return trackingLinkService.updateLinkStatus(request);
        } catch (Exception e) {
            log.error("Failed to update link status", e);
            return new ApiResult<Boolean>().fail("Failed to update link status: " + e.getMessage());
        }
    }
}
