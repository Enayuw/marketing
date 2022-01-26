package com.br.marketing.dto;

import com.br.marketing.entity.PhoneBlack;

import java.util.List;

public class PushBlackReqDTO {
    private List<PhoneBlack> users;

    public List<PhoneBlack> getUsers() {
        return users;
    }

    public void setUsers(List<PhoneBlack> users) {
        this.users = users;
    }
}
