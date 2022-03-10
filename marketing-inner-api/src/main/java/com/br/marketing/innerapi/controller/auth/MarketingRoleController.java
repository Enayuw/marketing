package com.br.marketing.innerapi.controller.auth;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.auth.CodeEnum;
import com.br.marketing.entity.auth.MarketingRole;
import com.br.marketing.entity.auth.ResourceTreeBean;
import com.br.marketing.service.auth.MarketingResourceService;
import com.br.marketing.service.auth.MarketingRoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
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
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<Boolean> delete(String ids) {
       return new ApiResult<Boolean>().success( marketingRoleService.deleteByIds(ids));
        if (StringUtils.isNotBlank(ids)) {
            List<MarketingRole> marketingRoles = marketingRoleService.selectList(ids);
            for (MarketingRole role : marketingRoles) {
                role.setStatus(0);
                role.setUpdateTime(new Date());
                //更新角色表角色的状态--删除
                marketingRoleService.updateById(role);
                //更新用户-角色表之间的关联关系
                marketingRoleService.deleteUserRoleByRid(role.getId());
            }
        }
        return new ApiResult<Boolean>().success();
    }


    /**
     * 编辑角色
     */
    @GetMapping("/update")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<Boolean> update(MarketingRole role) {
        marketingRoleService.updateRole(role);
        return new ApiResult<Boolean>().success();
    }
    ///**
    // * 角色列表
    // *
    // * @param createStart
    // * @param createEnd
    // * @param updateStart
    // * @param updateEnd
    // * @param key
    // * @return
    // */
    //@GetMapping("/list")
    //@PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    //public ApiResult list(String createStart, String createEnd, String updateStart, String updateEnd, String key) {
    //    return respJson(CodeEnum.SUCC, roleService.getList(this.getPage(Role.class), createStart, createEnd, updateStart, updateEnd, key));
    //}

    /**
     * 获取角色信息
     *
     * @param roleId
     * @return
     */
    @GetMapping("/getById")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<MarketingRole> getRoleById(Integer roleId) {
        return new ApiResult<MarketingRole>().success(marketingRoleService.selectById(roleId));
    }

    /**
     * 获取角色权限
     *
     * @param roleId
     * @return
     */
    @GetMapping("/getResourceTree")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<List<ResourceTreeBean>> getTree(Integer roleId) {
        return new ApiResult<List<ResourceTreeBean>>().success(marketingResourceService.getResourcesTree(roleId));
    }

    /**
     * 校验角色名称
     *
     * @param id
     * @param roleName
     * @return
     */
    @GetMapping("/checkName")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<Boolean> checkName(Integer id, String roleName) {
        return new ApiResult<Boolean>().success(marketingRoleService.checkName(id, roleName));
    }
}
