package com.br.marketing.innerapi.controller.auth;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.constants.auth.CodeEnum;
import com.br.marketing.entity.auth.MarketingResource;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 营销资源管理器
 * @Date 2022/3/9 5:21 PM
 * ------------------------------
 */

@RestController
@Api(value = "权限", tags = "resource", description = "权限相关")
@RequestMapping(value = "/resource")
public class MarketingResourceController {
    /**
     * 保存权限
     *
     * @param resource
     * @return
     */
    @GetMapping("/save")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public String save(MarketingResource resource) {
        if (!this.checkAuthority(resource.getAuthority())) {
            return respJson(CodeEnum.PARAM_ERROR.getCode(), "权限名不能以*开头");
        }
        resourceService.saveResources(resource);
        return respJson(CodeEnum.SUCC);
    }
    /**
     * 检查正则合法性
     *
     * @return
     */
    private boolean checkAuthority(String authority) {
        if (StringUtils.isBlank(authority)) {
            return false;
        }
        //不能以*开头
        if (authority.startsWith("*")) {
            return false;
        }
        return true;
    }
}
