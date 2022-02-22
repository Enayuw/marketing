package com.br.marketing.dto;

import com.br.marketing.entity.PhoneBlack;

import java.util.List;

public class PushBlackReqDTO {

    private String apiCode;

    private List<PhoneBlack> users;

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public List<PhoneBlack> getUsers() {
        return users;
    }

    public void setUsers(List<PhoneBlack> users) {
        this.users = users;
    }
}
