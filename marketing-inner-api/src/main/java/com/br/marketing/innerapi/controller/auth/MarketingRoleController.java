package com.br.marketing.innerapi.controller.auth;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.auth.MarketingRole;
import com.br.marketing.entity.auth.ResourceTreeBean;
import com.br.marketing.service.auth.MarketingResourceService;
import com.br.marketing.service.auth.MarketingRoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static com.br.marketing.common.enums.ServiceResultEnum.DELETE_ROLE_ERROR;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 角色控制器
 * @Date 2022/3/10 11:34 AM
 * ------------------------------
 */
@RestController
@RequestMapping("role")
public class MarketingRoleController {

    @Resource
    private MarketingRoleService marketingRoleService;

    @Resource
    private MarketingResourceService marketingResourceService;

    /**
     * 创建角色
     */
    @GetMapping("/save")
    public ApiResult<Boolean> saveRole(MarketingRole role) {
        //创建角色
        marketingRoleService.saveRole(role);
        return new ApiResult<Boolean>().success();
    }

    /**
     * 批量删除角色
     *
     * @param ids 多个id用逗号分隔
     */
    @GetMapping("/delete")
    public ApiResult<Boolean> delete(String ids) {
        if(marketingRoleService.deleteByIds(ids)){
            return new ApiResult<Boolean>().success();
        }else {
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.DELETE_ROLE_ERROR);
        }

    }


    /**
     * 编辑角色
     */
    @GetMapping("/update")
    public ApiResult<Boolean> update(MarketingRole role) {
        marketingRoleService.updateRole(role);
        return new ApiResult<Boolean>().success();
    }
    /**
     * 角色列表
     *
     */
    @GetMapping("/list")
    public ApiResult<PageResultReturn> list(String createStart, String createEnd, String updateStart,
                                            String updateEnd, String key, Integer current, Integer size) {
        PageResultReturn listPage =  marketingRoleService.selectRoleListBySearch(
                createStart,createEnd,updateStart,updateEnd,key,
                current,size);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    /**
     * 获取角色信息
     *
     */
    @GetMapping("/getById")
    public ApiResult<MarketingRole> getRoleById(Integer roleId) {
        return new ApiResult<MarketingRole>().success(marketingRoleService.selectById(roleId));
    }

    /**
     * 获取角色权限
     *
     */
    @GetMapping("/getResourceTree")
    public ApiResult<List<ResourceTreeBean>> getTree(Integer roleId) {
        return new ApiResult<List<ResourceTreeBean>>().success(marketingResourceService.getResourcesTree(roleId));
    }

    /**
     * 校验角色名称
     *
     */
    @GetMapping("/checkName")
    public ApiResult<Boolean> checkName(Integer id, String roleName) {
        return new ApiResult<Boolean>().success(marketingRoleService.checkName(id, roleName));
    }
}
