package com.br.marketing.entity.auth;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 营销用户详情
 * @Date 2022/3/11 4:16 PM
 * ------------------------------
 */
@Data
public class MarketingUserDetail extends MarketingUserInfo{
    private String sessionId;
    private List<MarketingRole> roleList;
    private List<MarketingResource> resourcesList;
    private Map<String, JSONArray> permissionGroups;

    public MarketingUserDetail(MarketingUserInfo user, List<MarketingRole> roleList, List<MarketingResource> resourcesList, Map<String, JSONArray> permissionGroups) {
        super(user);
        this.roleList = roleList;
        this.resourcesList = resourcesList;
        this.permissionGroups = permissionGroups;
    }
    public MarketingUserDetail() {
    }

}
