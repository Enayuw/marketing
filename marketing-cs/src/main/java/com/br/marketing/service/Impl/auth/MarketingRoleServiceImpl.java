package com.br.marketing.service.Impl.auth;

import com.br.marketing.entity.auth.*;
import com.br.marketing.mapper.auth.MarketingRoleMapper;
import com.br.marketing.mapper.auth.MarketingRoleResourceMapper;
import com.br.marketing.mapper.auth.MarketingUserInfoRoleMapper;
import com.br.marketing.service.auth.MarketingRoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 营销用户角色实现
 * @Date 2022/3/10 11:41 AM
 * ------------------------------
 */
@Service
public class MarketingRoleServiceImpl implements MarketingRoleService {
    @Resource
    private MarketingRoleMapper marketingRoleMapper;

    @Resource
    private MarketingRoleResourceMapper marketingRoleResourceMapper;

    @Resource
    private MarketingUserInfoRoleMapper marketingUserInfoRoleMapper;

    @Override
    public void saveRole(MarketingRole role) {
        role.setCreateTime(new Date());
        role.setUpdateTime(new Date());
        role.setStatus(1);
        marketingRoleMapper.insertSelective(role);
        //角色授权
        if (role.getId() != null && role.getId() > 0 && StringUtils.isNotBlank(role.getAllResource())) {
            //更新资源权限
            updateRoleResource(role);
        }
    }

    @Override
    public void updateRole(MarketingRole role) {
        role.setNameRemark(StringUtils.isNotBlank(role.getNameRemark()) ? role.getNameRemark() : " ");
        role.setUpdateTime(new Date());
        int state = marketingRoleMapper.updateByPrimaryKeySelective(role);
        if (state > 0) {
            //更新权限信息
            MarketingRoleResourceExample marketingRoleResourceExample = new MarketingRoleResourceExample();
            marketingRoleResourceExample.createCriteria().andRoleIdEqualTo(role.getId());
            marketingRoleResourceMapper.deleteByExample(marketingRoleResourceExample);
            //更新资源权限
            updateRoleResource(role);
        }
    }



    @Override
    public List<MarketingRole> selectList(String ids) {
        MarketingRoleExample marketingRoleExample = new MarketingRoleExample();
        String[] split = ids.split(",");
        List<Integer> collect = Arrays.stream(split).map(Integer::parseInt).collect(Collectors.toList());
        marketingRoleExample.createCriteria().andIdIn(collect);
        return marketingRoleMapper.selectByExample(marketingRoleExample);
    }

    public void deleteUserRoleByRid(Integer id) {
        MarketingUserInfoRoleExample marketingUserInfoRoleExample = new MarketingUserInfoRoleExample();
        marketingUserInfoRoleExample.createCriteria().andRoleIdEqualTo(id);
        MarketingUserInfoRole marketingUserInfoRole = new MarketingUserInfoRole();
        marketingUserInfoRole.setUpdateTime(new Date());
        marketingUserInfoRole.setStatus(0);
        marketingUserInfoRoleMapper.updateByExampleSelective(marketingUserInfoRole, marketingUserInfoRoleExample);
    }

    @Override
    public MarketingRole selectById(Integer roleId) {
        return marketingRoleMapper.selectByPrimaryKey(roleId);
    }

    @Override
    public boolean checkName(Integer id, String roleName) {
        if (StringUtils.isNotBlank(roleName)) {
            if (id == null) {
                MarketingRoleExample marketingRoleExample = new MarketingRoleExample();
                marketingRoleExample.createCriteria().andNameEqualTo(roleName).andStatusEqualTo(1);
                int i = marketingRoleMapper.countByExample(marketingRoleExample);
                return i == 0;
            } else {
                MarketingRoleExample marketingRoleExample = new MarketingRoleExample();
                marketingRoleExample.createCriteria().andNameEqualTo(roleName).andStatusEqualTo(1).andIdEqualTo(id);
                int i = marketingRoleMapper.countByExample(marketingRoleExample);
                return i > 0;
            }
        }
        return false;
    }

    @Override
    public Boolean deleteByIds(String ids) {
        if (StringUtils.isNotBlank(ids)) {
            MarketingRoleExample marketingRoleExample = new MarketingRoleExample();
            String[] split = ids.split(",");
            List<Integer> collect = Arrays.stream(split).map(Integer::parseInt).collect(Collectors.toList());
            marketingRoleExample.createCriteria().andIdIn(collect);
            List<MarketingRole> marketingRoles = marketingRoleMapper.selectByExample(marketingRoleExample);
            for (MarketingRole role : marketingRoles) {
                role.setStatus(0);
                role.setUpdateTime(new Date());
                //更新角色表角色的状态--删除
                marketingRoleMapper.updateByPrimaryKeySelective(role);
                //更新用户-角色表之间的关联关系
                deleteUserRoleByRid(role.getId())
;            }
        }
        return null;
    }

    @Override
    public List<MarketingRole> selectRoleList() {
        MarketingRoleExample marketingRoleExample = new MarketingRoleExample();
        marketingRoleExample.createCriteria().andStatusEqualTo(1);
        return marketingRoleMapper.selectByExample(marketingRoleExample);
    }


    /***
     * 更新权限资源
     */
    private void updateRoleResource(MarketingRole role) {
        if (StringUtils.isNotBlank(role.getAllResource())) {
            String[] split = role.getAllResource().split(",");
            for (String id : split) {
                MarketingRoleResource marketingRoleResource = new MarketingRoleResource();
                marketingRoleResource.setStatus(1);
                marketingRoleResource.setCreateTime(new Date());
                marketingRoleResource.setUpdateTime(new Date());
                marketingRoleResource.setRoleId(role.getId());
                marketingRoleResource.setResourceId(Integer.valueOf(id));
                marketingRoleResourceMapper.insertSelective(marketingRoleResource);
            }
        }
    }
}
