package com.br.marketing.entity.auth;

import java.util.List;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 用户详情
 * @Date 2022/3/11 5:58 PM
 * ------------------------------
 */
public class MarketingUserDetail extends MarketingUserInfo{
    /**
     * 登录人sessionid：主要用于redis session
     */
    private String sessionid;

    private List<MarketingRole> roleList;

    private List<MarketingResource> resourcesList;

    public MarketingUserDetail(MarketingUserInfo user, List<MarketingRole> roleList, List<MarketingResource> resourcesList) {
        super(user);
        this.roleList = roleList;
        this.resourcesList = resourcesList;
    }

    public MarketingUserDetail() {
    }
}
