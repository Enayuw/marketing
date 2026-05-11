package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.vo.SyncConfigEditVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "sftp账号配置", description = "sftp账号配置")
public class SyncConfigController {

    private static final Logger log = LoggerFactory.getLogger(SyncConfigController.class);

    @Resource
    private SyncConfigService syncConfigService;


    @GetMapping("/getSftpList")
    @Operation(summary = "客户sftp账号列表", description = "客户sftp账号列表")
    @Parameters({@Parameter(name = "current", description = "页号")
        , @Parameter(name = "size", description = "页大小")
        , @Parameter(name = "apiCode", description = "API编码")
        , @Parameter(name = "dataType", description = "文件类型")
    })
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
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
    @Parameters({@Parameter(name = "id", description = "被复制的SFTP配置id")
        , @Parameter(name = "apiCode", description = "apiCode")
        , @Parameter(name = "srcPath", description = "源目录")
        , @Parameter(name = "targetPath", description = "目标目录")
        , @Parameter(name = "type", description = "同步文件的类型")
        , @Parameter(name = "dataType", description = "文件类型")
        , @Parameter(name = "suffix", description = "文件后缀")
        , @Parameter(name = "srcSftpHost", description = "源sftp host")
        , @Parameter(name = "srcSftpPort", description = "源sftp port")
        , @Parameter(name = "srcSftpUser", description = "源sftp账号")
        , @Parameter(name = "srcSftpPwd", description = "源sftp密码")
        , @Parameter(name = "targetSftpHost", description = "目的sftp host")
        , @Parameter(name = "targetSftpPort", description = "目的sftp port")
        , @Parameter(name = "targetSftpUser", description = "目的sftp账号")
        , @Parameter(name = "targetSftpPwd", description = "目的sftp密码")
        , @Parameter(name = "srcType", description = "源服务器类型")
        , @Parameter(name = "targetType", description = "目标服务器类型")
        , @Parameter(name = "isUnzip", description = "是否需要解压：0-否 1-是")
        , @Parameter(name = "unzipfilenameCharset", description = "解压文件名编码，默认 GBK")

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
                                       @RequestParam(required = false) String targetSftpPwd,
                                       @RequestParam(required = false) String srcType,
                                       @RequestParam(required = false) String targetType,
                                       @RequestParam(required = false) Integer isUnzip,
                                       @RequestParam(required = false) String unzipfilenameCharset) {
        try {
            return syncConfigService.copySftp(id, apiCode, srcPath, targetPath, type, dataType, suffix, srcSftpHost, srcSftpPort,
                srcSftpUser, srcSftpPwd, targetSftpHost, targetSftpPort, targetSftpUser, targetSftpPwd, srcType, targetType,
                isUnzip, unzipfilenameCharset);
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

    @Operation(summary = "SFTP文件后缀可选项", description = "来自 speed 配置 sftpFileSuffixOptions")
    @GetMapping("/getSuffixConfigs")
    public ApiResult<List<String>> getSuffixConfigs() {
        return new ApiResult<List<String>>().success(syncConfigService.getSftpSuffixConfigs());
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
