package com.br.marketing.service.auth;

import com.br.marketing.entity.auth.MarketingRole;

import java.util.List;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 营销角色接口
 * @Date 2022/3/10 11:39 AM
 * ------------------------------
 */
public interface MarketingRoleService {
    /**
     * 新增角色
     *
     * @param role
     * @return
     */
    void saveRole(MarketingRole role);

    /**
     * 更新角色
     *
     * @param role
     * @return
     */
    void updateRole(MarketingRole role);

    List<MarketingRole> selectList(String ids);

    void deleteUserRoleByRid(Integer id);

    MarketingRole selectById(Integer roleId);


    boolean checkName(Integer id, String roleName);

    Boolean deleteByIds(String ids);

    List<MarketingRole> selectRoleList();
}
