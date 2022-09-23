package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.LocalFileService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 文件上传历史记录
 * <p>
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.inner api.controller
 * @Description: 文件上传历史记录
 * @CreateTime: 2022-09-15 15 :28
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@RestController
@RequestMapping(value = "/rule/fileUploadHistory")
@Api(value = "文件上传历史", tags = "文件上传历史", produces = "application/json", consumes = "application/json", protocols = "http")
public class FileUploadHistoryController {

    @Resource
    private LocalFileService localfileService;

    @GetMapping("/list")
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> list(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String search
            , @RequestParam(required = false) String apiCode
            , @RequestParam(required = false) String uploadTimeStart
            , @RequestParam(required = false) String uploadTimeEnd
            , @RequestParam(required = false) String fileType
    ) {
        return new ApiResult<PageResultReturn>().success(localfileService.list(current, size, search, apiCode, uploadTimeStart, uploadTimeEnd, fileType));
    }

    @GetMapping("/allCount")
    @AddDataAuthBusiness
    public ApiResult<Integer> allCount() {
        return new ApiResult<Integer>().success(localfileService.allCount());
    }

}
