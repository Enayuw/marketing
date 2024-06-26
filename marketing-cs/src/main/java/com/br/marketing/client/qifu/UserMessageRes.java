package com.br.marketing.client.qifu;

import java.util.List;

public class UserMessageRes {

    private String age;

    private String lastLoginTime;

    private String name;

    private String sex;

    private List<UserExtraInfo> userExtraInfo;

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public List<UserExtraInfo> getUserExtraInfo() {
        return userExtraInfo;
    }

    public void setUserExtraInfo(List<UserExtraInfo> userExtraInfo) {
        this.userExtraInfo = userExtraInfo;
    }
}

class UserExtraInfo {

    /**
     *
     */
    private String isLightMarkting;
    /**
     *
     */
    private String operationScene;

    public String getIsLightMarkting() {
        return isLightMarkting;
    }

    public void setIsLightMarkting(String isLightMarkting) {
        this.isLightMarkting = isLightMarkting;
    }

    public String getOperationScene() {
        return operationScene;
    }

    public void setOperationScene(String operationScene) {
        this.operationScene = operationScene;
    }
}