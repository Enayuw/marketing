package com.br.marketing.service.auth;

import com.br.marketing.commonentity.PageResultReturn;
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
     * @param role 角色入参
     * @return 更新情况
     */
    void updateRole(MarketingRole role);

    /**
     * 获取用户角色列表
     * @param ids 用户id的集合
     * @return 返回角色集合
     */
    List<MarketingRole> selectList(String ids);

    /**
     * @param id 角色id
     */
    void deleteUserRoleByRid(Integer id);

    /**
     * 根据角色 id 查询角色信息
     * @param roleId 角色id
     * @return 返回角色信息
     */
    MarketingRole selectById(Integer roleId);


    /**
     * 检查用户名是否合法
     * @param id 用户id
     * @param roleName 用户真实姓名
     * @return true 合法
     */
    boolean checkName(Integer id, String roleName);

    /**
     * 根据用户传入的的角色id  删除角色
     * @param ids 入参id的集合
     * @return  返回删除结果
     */
    Boolean deleteByIds(String ids);

    /**
     * 查询所有角色集合
     * @return 角色集合
     */
    List<MarketingRole> selectRoleList();

    /**
     * 搜索列表页
     * @param createStart 开始时间
     * @param createEnd 结束时间
     * @param updateStart 更新开始时间
     * @param updateEnd 更新结束时间
     * @param key 关键字
     * @return 返回查询结果
     */
    PageResultReturn selectRoleListBySearch(String createStart, String createEnd,
                                            String updateStart, String updateEnd,
                                            String key,
                                            Integer current, Integer size);
}
