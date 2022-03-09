package com.br.marketing.service.Impl.auth;

import com.br.marketing.entity.auth.MarketingResource;
import com.br.marketing.entity.auth.MarketingResourceExample;
import com.br.marketing.entity.auth.ResourceTreeBean;
import com.br.marketing.mapper.auth.MarketingResourceMapper;
import com.br.marketing.service.auth.MarketingResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void deleteResource(Integer id) {
        MarketingResourceExample mre = new MarketingResourceExample();
        mre.createCriteria().andIdEqualTo(id);
        marketingResourceMapper.deleteByExample(mre);
        //递归删除子菜单
        recursiveDeleteResource(id);
    }
    /**
     * 递归删除子菜单
     *
     * @param resourceId
     */
    private void recursiveDeleteResource(Integer resourceId) {
        MarketingResourceExample mre = new MarketingResourceExample();
        mre.createCriteria().andParentidEqualTo(resourceId).andIsdeleteEqualTo(0);
        MarketingResource mr = new MarketingResource();
        mr.

        List<Integer> ids = marketingResourceMapper.getIdsByParent(resourceId);
        if (ids != null && !ids.isEmpty()) {
            resourceMapper.deleteResources(ids);
            for (Integer integer : ids) {
                recursiveDeleteResource(integer);
            }
        }
    }

    @Override
    public List<MarketingResource> getResourcesByUid(Integer id) {
        return null;
    }

    @Override
    public void saveResources(MarketingResource resource) {

    }

    @Override
    public void updateResources(MarketingResource resource) {

    }

    @Override
    public List<ResourceTreeBean> getResourcesTree(Integer roleId) {
        return null;
    }

    @Override
    public List<ResourceTreeBean> getResourcesById(Integer resourceId) {
        return null;
    }
}
