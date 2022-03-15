package com.br.marketing.service.auth;

import com.br.marketing.entity.auth.MarketingResource;
import com.br.marketing.entity.auth.ResourceTreeBean;

import java.util.List;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 权限接口
 * @Date 2022/3/9 5:24 PM
 * ------------------------------
 */
public interface MarketingResourceService {
    /**
     * 根据ids删除系统资源,递归删除子菜单
     *
     * @param id 资源id
     */
    void deleteResource(Integer id);

    /**
     * 根据id获取系统资源
     *
     * @param id 资源id
     * @return list
     */
    List<MarketingResource> getResourcesByUid(Integer id);

    /**
     * 保存资源表
     *
     * @param resource marketingResource
     */
    void saveResources(MarketingResource resource);

    /**
     * 更新资源表
     *
     * @param resource marketingResource
     */
    void updateResources(MarketingResource resource);

    /**
     * 根据角色id获取权限资源树
     *
     * @param roleId roleId
     * @return list
     */
    List<ResourceTreeBean> getResourcesTree(Integer roleId);

    /**
     * 根据资源id获取权限资源树
     *
     * @param resourceId resourceId
     * @return list
     */
    List<ResourceTreeBean> getResourcesById(Integer resourceId);

    /**
     * 根据id查询数据
     *
     * @param resourceId resourceId
     * @return MarketingResource
     */
    MarketingResource selectById(Integer resourceId);

}
