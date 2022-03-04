package com.br.marketing.dto.userinfo;

import com.alibaba.fastjson.JSONArray;

import java.util.List;
import java.util.Map;


public class UserDetail extends User{
    private String sessionId;
    private List<Role> roleList;
    private List<Resources> resourcesList;
    private Map<String, JSONArray> permissionGroups;

    public UserDetail(User user, List<Role> roleList, List<Resources> resourcesList, Map<String, JSONArray> permissionGroups) {
        super(user);
        this.roleList = roleList;
        this.resourcesList = resourcesList;
        this.permissionGroups = permissionGroups;
    }

    public UserDetail() {
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public List<Role> getRoleList() {
        return this.roleList;
    }

    public List<Resources> getResourcesList() {
        return this.resourcesList;
    }

    public Map<String, JSONArray> getPermissionGroups() {
        return this.permissionGroups;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }

    public void setResourcesList(List<Resources> resourcesList) {
        this.resourcesList = resourcesList;
    }

    public void setPermissionGroups(Map<String, JSONArray> permissionGroups) {
        this.permissionGroups = permissionGroups;
    }
}
