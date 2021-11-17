package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.service.SyncConfigService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * sftp账号配置
 *
 * @author songjuanjuan
 * @dateTime 2021/10/27 13:12
 */
@RestController
@RequestMapping(value = "/rule/sftp")
@Api(value = "sftp账号配置", tags = "sftp账号配置", produces = "application/json", consumes = "application/json", protocols = "http")
public class SyncConfigController {

    private static final Logger log = LoggerFactory.getLogger(SyncConfigController.class);

    @Resource
    private SyncConfigService syncConfigService;


    @GetMapping("/getSftpList")
    @ApiOperation(value = "客户sftp账号列表", notes = "客户sftp账号列表", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "apiCode", paramType = "query", dataType = "string")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingCustomer.class)})
    public ApiResult<PageResultReturn> getSftpList(@RequestParam(defaultValue = "1") int current
                                                        , @RequestParam(defaultValue = "10") int size
                                                        , @RequestParam(required = false) String apiCode) {
        PageResultReturn listPage = syncConfigService.getSftpList(current, size, apiCode);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }


    @ApiOperation(value = "复制sftp配置信息",notes = "复制sftp配置信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "被复制的SFTP配置id",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "apiCode", value = "apiCode", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "srcPath", value = "源目录", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "targePath", value = "目标目录", paramType = "query", dataType = "string")
    })
    @GetMapping("/copySftp")
    public ApiResult<Boolean> copySftp(@RequestParam(required = true) String id,
                                       @RequestParam(required = true) String apiCode,
                                       @RequestParam(required = true) String srcPath,
                                       @RequestParam(required = true) String targePath){
        try {
            return syncConfigService.copySftp(id,apiCode,srcPath,targePath);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "编辑sftp配置信息",notes = "编辑sftp配置信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "SFTP配置id",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "apiCode", value = "apiCode", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "srcPath", value = "源目录", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "targePath", value = "目标目录", paramType = "query", dataType = "string")
    })
    @GetMapping("/editSftp")
    public ApiResult<Boolean> editSftp(@RequestParam(required = true) String id,
                                       @RequestParam(required = true) String apiCode,
                                       @RequestParam(required = true) String srcPath,
                                       @RequestParam(required = true) String targePath){
        try {
            return syncConfigService.editSftp(id,apiCode,srcPath,targePath);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }

}
