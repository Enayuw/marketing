package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.vo.SyncConfigEditVO;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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
    @AddDataAuthBusiness
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
            , @ApiImplicitParam(name = "targetPath", value = "目标目录", paramType = "query", dataType = "string")

            , @ApiImplicitParam(name = "type", value = "同步文件的类型", paramType = "query", dataType = "int")
            , @ApiImplicitParam(name = "dataType", value = "文件类型", paramType = "query", dataType = "int")
            , @ApiImplicitParam(name = "suffix", value = "文件后缀", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "srcSftpHost", value = "源sftp host", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "srcSftpPort", value = "源sftp port", paramType = "query", dataType = "int")
            , @ApiImplicitParam(name = "srcSftpUser", value = "源sftp账号", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "srcSftpPwd", value = "源sftp密码", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "targetSftpHost", value = "目的sftp host", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "targetSftpPort", value = "目的sftp port", paramType = "query", dataType = "int")
            , @ApiImplicitParam(name = "targetSftpUser", value = "目的sftp账号", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "targetSftpPwd", value = "目的sftp密码", paramType = "query", dataType = "string")
    })
    @GetMapping("/copySftp")
    public ApiResult<Boolean> copySftp(@RequestParam(required = true) String id,
                                       @RequestParam(required = true) String apiCode,
                                       @RequestParam(required = true) String srcPath,
                                       @RequestParam(required = true) String targetPath,
                                       @RequestParam(required = false) Integer type,
                                       @RequestParam(required = false) Integer dataType,
                                       @RequestParam(required = false) String suffix,
                                       @RequestParam(required = false) String srcSftpHost,
                                       @RequestParam(required = false) Integer srcSftpPort,
                                       @RequestParam(required = false) String srcSftpUser,
                                       @RequestParam(required = false) String srcSftpPwd,
                                       @RequestParam(required = false) String targetSftpHost,
                                       @RequestParam(required = false) Integer targetSftpPort,
                                       @RequestParam(required = false) String targetSftpUser,
                                       @RequestParam(required = false) String targetSftpPwd){
        try {
            return syncConfigService.copySftp(id,apiCode,srcPath,targetPath,type,dataType,suffix,srcSftpHost,srcSftpPort,
                    srcSftpUser,srcSftpPwd,targetSftpHost,targetSftpPort,targetSftpUser,targetSftpPwd);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "编辑sftp配置信息",notes = "编辑sftp配置信息")
    @PostMapping("/editSftp")
    public ApiResult<Boolean> editSftp(@RequestBody @Validated SyncConfigEditVO vo){
        try {
            return syncConfigService.editSftp(vo);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "获取文件类型列表",notes = "获取文件类型列表")
    @GetMapping("/getDataTypeList")
    public ApiResult<List<Map>> getDataTypeList(){
        try {
            List<Map> dataTypeList = syncConfigService.getDataTypeList();
            if (dataTypeList != null) {
                return new ApiResult<List<Map>>().success(dataTypeList);
            }
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<List<Map>>().fail(ServiceResultEnum.FAILED);
        }
        return new ApiResult<List<Map>>().fail(ServiceResultEnum.FAILED);
    }

}
