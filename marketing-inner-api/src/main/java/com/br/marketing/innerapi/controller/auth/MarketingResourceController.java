package com.br.marketing.innerapi.controller.auth;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.entity.auth.MarketingResource;
import com.br.marketing.entity.auth.ResourceTreeBean;
import com.br.marketing.service.auth.MarketingResourceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 营销资源管理器
 * @Date 2022/3/9 5:21 PM
 * ------------------------------
 */

@RestController
@Tag(name = "权限", description = "resource")
@RequestMapping(value = "/resource")
public class MarketingResourceController {
    @Resource
    private MarketingResourceService marketingResourceService;

    /**
     * 保存权限
     *
     */
    @GetMapping("/save")
    public ApiResult<Boolean> save(MarketingResource resource) {
        if (this.checkAuthority(resource.getAuthority())) {
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.AUTH_FAILED_ERROR_HEADER);
        }
        marketingResourceService.saveResources(resource);
        return new ApiResult<Boolean>().success(true);
    }
    /**
     * 删除权限
     *
     */
    @GetMapping("/delete")
    public ApiResult<Boolean> delete(Integer resourceId) {
        marketingResourceService.deleteResource(resourceId);
        return new ApiResult<Boolean>().success(true);
    }
    /**
     * 更新权限
     *
     */
    @GetMapping("/update")
    public ApiResult<Boolean> update(MarketingResource resource) {
        if (this.checkAuthority(resource.getAuthority())) {
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.AUTH_FAILED_ERROR_HEADER);
        }
        marketingResourceService.updateResources(resource);
        return new ApiResult<Boolean>().success(true);
    }

    @GetMapping("/getResourceTree")
    public ApiResult<ResourceTreeBean> getAllResourceById(Integer resourceId) {
        if (resourceId != null) {
            List<ResourceTreeBean> resourceTree = marketingResourceService.getResourcesById(resourceId);
            ResourceTreeBean treeBean = new ResourceTreeBean();
            treeBean.setText("root");
            treeBean.setId(0);
            treeBean.setChildren(resourceTree);
            return new ApiResult<ResourceTreeBean>().success(treeBean);
        }
        return new ApiResult<ResourceTreeBean>().fail(ServiceResultEnum.AUTH_FAILED_ERROR_HEADER);
    }

    /**
     * 获取资源树信息
     */
    @GetMapping("/getById")
    public ApiResult<MarketingResource> getResourceById(Integer resourceId) {
        return new ApiResult<MarketingResource>().success(marketingResourceService.selectById(resourceId));
    }

    /**
     * 检查正则合法性
     *
     */
    private boolean checkAuthority(String authority) {
        if (StringUtils.isBlank(authority)) {
            return true;
        }
        //不能以*开头
        return authority.startsWith("*");
    }
}
