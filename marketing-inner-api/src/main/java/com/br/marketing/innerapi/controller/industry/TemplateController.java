package com.br.marketing.innerapi.controller.industry;

import com.br.marketing.common.commondto.ApiResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TemplateController
 * @Author hang.zhou
 * @Date 2025/10/27
 */
@RestController
public class TemplateController {

    public ApiResult getTemplateById(@RequestParam(name = "id") Integer id) {
        return new ApiResult();
    }

}
