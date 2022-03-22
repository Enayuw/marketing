package com.br.marketing.service.Impl.auth;

import com.br.marketing.entity.auth.*;
import com.br.marketing.mapper.auth.MarketingResourceMapper;
import com.br.marketing.mapper.auth.MarketingRoleResourceMapper;
import com.br.marketing.service.auth.MarketingResourceService;
import com.google.common.base.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 权限接口实现类
 * @Date 2022/3/9 5:25 PM
 * ------------------------------
 */
@Service
public class MarketingResourceServiceImpl implements MarketingResourceService {

    @Resource
    private MarketingResourceMapper marketingResourceMapper;

    @Resource
    private MarketingRoleResourceMapper marketingRoleResourceMapper;

    @Override
    public void deleteResource(Integer id) {
        // 删除当前菜单
        MarketingResourceExample mre = new MarketingResourceExample();
        mre.createCriteria().andIdEqualTo(id);
        MarketingResource marketingResource = new MarketingResource();
        marketingResource.setStatus(0);
        marketingResourceMapper.updateByExampleSelective(marketingResource,mre);
        //递归删除子菜单
        recursiveDeleteResource(id);
    }

    /**
     * 递归删除子菜单
     */
    private void recursiveDeleteResource(Integer resourceId) {
        //查询当前菜单的子菜单
        MarketingResourceExample mre = new MarketingResourceExample();
        mre.createCriteria().andParentIdEqualTo(resourceId).andStatusEqualTo(1);
        List<MarketingResource> marketingResources = marketingResourceMapper.selectByExample(mre);
        List<Integer> ids = marketingResources.stream().map(MarketingResource::getId).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            //删除当前菜单的所有子菜单
            MarketingResourceExample subMre = new MarketingResourceExample();
            subMre.createCriteria().andIdIn(ids);
            MarketingResource marketingResource = new MarketingResource();
            marketingResource.setStatus(0);
            marketingResourceMapper.updateByExampleSelective(marketingResource,subMre);
            // 递归删除子菜单
            for (Integer integer : ids) {
                recursiveDeleteResource(integer);
            }
        }
    }

    /**
     * 根据id获取系统资源
     */
    @Override
    public List<MarketingResource> getResourcesByUid(Integer id) {
        return marketingResourceMapper.getResourcesByUid(id);
    }

    /**
     * 保存系统资源
     *
     * @param resource resource
     */
    @Override
    public void saveResources(MarketingResource resource) {
        if (resource.getParentId() != null) {
            //查父id 判断其类型  1 一级标题 2 二级标题 3 三级标题
            MarketingResource parentResource = marketingResourceMapper.selectByPrimaryKey(resource.getParentId());
            if (parentResource == null) {
                resource.setType(1);
            } else {
                resource.setType(parentResource.getType() + 1);
            }
            resource.setUpdateTime(new Date());
            resource.setCreateTime(new Date());
            resource.setStatus(1);
            //添加到数据库
            marketingResourceMapper.insert(resource);
        }
    }

    @Override
    public void updateResources(MarketingResource resource) {

        resource.setUpdateTime(new Date());
        resource.setIcon(StringUtils.isNotBlank(resource.getIcon()) ? resource.getIcon() : " ");
        //更新信息
        marketingResourceMapper.updateByPrimaryKeySelective(resource);
    }

    @Override
    public List<ResourceTreeBean> getResourcesTree(Integer roleId) {
        MarketingResourceExample mre = new MarketingResourceExample();
        mre.createCriteria().andStatusEqualTo(1);
        mre.setOrderByClause("type asc,sort asc");
        List<MarketingResource> marketingResources = marketingResourceMapper.selectByExample(mre);

        Map<Integer, ResourceTreeBean> title1 = new HashMap<>(16);
        Map<Integer, ResourceTreeBean> title2 = new HashMap<>(16);
        Map<Integer, ResourceTreeBean> title3 = new HashMap<>(16);
        for (MarketingResource resource : marketingResources) {
            ResourceTreeBean resourceTreeBean = null;
            //是否为一级标题
            if (resource.getType() == 1) {
                resourceTreeBean = parseResource(resource);
                title1.put(resource.getId(), resourceTreeBean);
            }
            if (resource.getType() == 2) {
                resourceTreeBean = parseResource(resource);
                ResourceTreeBean tempTitle = title1.get(resource.getParentId());
                if (tempTitle != null) {
                    tempTitle.getChildren().add(resourceTreeBean);
                    title2.put(resource.getId(), resourceTreeBean);
                }
            }
            if (resource.getType() == 3) {
                resourceTreeBean = parseResource(resource);
                ResourceTreeBean tempBean = title2.get(resource.getParentId());
                if (tempBean != null) {
                    tempBean.getChildren().add(resourceTreeBean);
                    title3.put(resource.getId(), resourceTreeBean);
                }
            }
            if (resource.getType() == 4) {
                resourceTreeBean = parseResource(resource);
                ResourceTreeBean tempBean = title3.get(resource.getParentId());
                if (tempBean != null) {
                    tempBean.getChildren().add(resourceTreeBean);
                }
            }
            MarketingRoleResourceExample marketingRoleResourceExample = new MarketingRoleResourceExample();
            //判断是否选中状态
            if (resourceTreeBean != null) {
                Integer id = resourceTreeBean.getId();
                List<MarketingRoleResource> marketingRoleResources = new ArrayList<>();
                if (roleId != null) {
                    marketingRoleResourceExample.createCriteria().andRoleIdEqualTo(roleId).andStatusEqualTo(1);
                    marketingRoleResources =  marketingRoleResourceMapper.selectByExample(marketingRoleResourceExample);
                }
                List<Integer> marketingRoleResourcesList = marketingRoleResources.stream().map(MarketingRoleResource::getResourceId).collect(Collectors.toList());
                if (marketingRoleResourcesList.contains(id)) {
                    resourceTreeBean.setSelect(true);
                }
            }
        }
        ArrayList<ResourceTreeBean> resultList = new ArrayList<>(title1.values());
        //一级标题排序
        Collections.sort(resultList);
        return resultList;
    }

    private ResourceTreeBean parseResource(MarketingResource resource) {
        Map<String, Object> attr = new HashMap<>();
        attr.put("type", resource.getType());
        attr.put("parentId", resource.getParentId());
        attr.put("url", resource.getUrl());
        attr.put("englishName", resource.getEnglishName());
        attr.put("category", resource.getCategory());
        Map<String, Object> data = new HashMap<>();
        data.put("authority", resource.getAuthority());
        data.put("updateTime", resource.getUpdateTime());
        data.put("createTime", resource.getCreateTime());
        return new ResourceTreeBean(resource.getId()
                , resource.getName(), resource.getIcon(), resource.getSort(), attr, data);
    }


    @Override
    public List<ResourceTreeBean> getResourcesById(Integer resourceId) {
        List<ResourceTreeBean> resources = getResourcesTree(null);
        List<ResourceTreeBean> resultList = new ArrayList<>();
        if (resourceId == 0) {
            resultList.addAll(resources);
        } else {
            resultList.add(((Function<List<ResourceTreeBean>, ResourceTreeBean>) input -> {
                for (ResourceTreeBean tempBean : input) {
                    if (resourceId.equals(tempBean.getId())) {
                        return tempBean;
                    }
                }
                return null;
            }).apply(resources));
        }
        return resultList;
    }

    @Override
    public MarketingResource selectById(Integer resourceId) {
        return marketingResourceMapper.selectByPrimaryKey(resourceId);
    }
}
