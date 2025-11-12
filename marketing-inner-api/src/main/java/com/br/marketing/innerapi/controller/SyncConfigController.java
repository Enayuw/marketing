package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.vo.SyncConfigEditVO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * sftp账号配置
 *
 * @author songjuanjuan
 * @dateTime 2021/10/27 13:12
 */
@RestController
@RequestMapping(value = "/rule/sftp")
@Tag(value = "sftp账号配置", tags = "sftp账号配置", produces = "application/json", consumes = "application/json", protocols = "http")
public class SyncConfigController {

    private static final Logger log = LoggerFactory.getLogger(SyncConfigController.class);

    @Resource
    private SyncConfigService syncConfigService;


    @GetMapping("/getSftpList")
    @Operation(summary = "客户sftp账号列表", description = "客户sftp账号列表")
    @Parameters({@Parameter(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
        , @Parameter(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
        , @Parameter(name = "apiCode", paramType = "query", dataType = "string")
        , @Parameter(name = "dataType", value = "文件类型", paramType = "query", dataType = "integer")
    })
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = MarketingCustomer.class)})
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getSftpList(@RequestParam(defaultValue = "1") int current,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) String apiCode,
                                                   @RequestParam(required = false) Integer dataType) {
        PageResultReturn listPage = syncConfigService.getSftpList(current, size, apiCode, dataType);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }


    @Operation(summary = "复制sftp配置信息", description = "复制sftp配置信息")
    @Parameters({@Parameter(name = "id", value = "被复制的SFTP配置id", paramType = "query", dataType = "string")
        , @Parameter(name = "apiCode", value = "apiCode", paramType = "query", dataType = "string")
        , @Parameter(name = "srcPath", value = "源目录", paramType = "query", dataType = "string")
        , @Parameter(name = "targetPath", value = "目标目录", paramType = "query", dataType = "string")
        , @Parameter(name = "type", value = "同步文件的类型", paramType = "query", dataType = "int")
        , @Parameter(name = "dataType", value = "文件类型", paramType = "query", dataType = "int")
        , @Parameter(name = "suffix", value = "文件后缀", paramType = "query", dataType = "string")
        , @Parameter(name = "srcSftpHost", value = "源sftp host", paramType = "query", dataType = "string")
        , @Parameter(name = "srcSftpPort", value = "源sftp port", paramType = "query", dataType = "int")
        , @Parameter(name = "srcSftpUser", value = "源sftp账号", paramType = "query", dataType = "string")
        , @Parameter(name = "srcSftpPwd", value = "源sftp密码", paramType = "query", dataType = "string")
        , @Parameter(name = "targetSftpHost", value = "目的sftp host", paramType = "query", dataType = "string")
        , @Parameter(name = "targetSftpPort", value = "目的sftp port", paramType = "query", dataType = "int")
        , @Parameter(name = "targetSftpUser", value = "目的sftp账号", paramType = "query", dataType = "string")
        , @Parameter(name = "targetSftpPwd", value = "目的sftp密码", paramType = "query", dataType = "string")
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
                                       @RequestParam(required = false) String targetSftpPwd) {
        try {
            return syncConfigService.copySftp(id, apiCode, srcPath, targetPath, type, dataType, suffix, srcSftpHost, srcSftpPort,
                srcSftpUser, srcSftpPwd, targetSftpHost, targetSftpPort, targetSftpUser, targetSftpPwd);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "编辑sftp配置信息", description = "编辑sftp配置信息")
    @PostMapping("/editSftp")
    public ApiResult<Boolean> editSftp(@RequestBody @Validated SyncConfigEditVO vo) {
        try {
            return syncConfigService.editSftp(vo);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "获取文件类型列表", description = "获取文件类型列表")
    @GetMapping("/getDataTypeList")
    public ApiResult<JSONArray> getDataTypeList() {
        try {
            JSONArray dataTypeList = syncConfigService.getDataTypeList();
            if (dataTypeList != null) {
                return new ApiResult<JSONArray>().success(dataTypeList);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<JSONArray>().fail(ServiceResultEnum.FAILED);
        }
        return new ApiResult<JSONArray>().fail(ServiceResultEnum.FAILED);
    }

    @GetMapping("/batchDeleteSftpList")
    @Operation(summary = "批量删除客户sftp账号", description = "批量删除客户sftp账号")
    public ApiResult<Boolean> batchDeleteSftpList(@RequestParam List<Long> ids) {
        return syncConfigService.batchDeleteSftpList(ids);
    }

}
