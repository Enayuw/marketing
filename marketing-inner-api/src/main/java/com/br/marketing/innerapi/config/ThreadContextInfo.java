package com.br.marketing.innerapi.config;

import com.br.marketing.dto.userinfo.UserDetail;

public class ThreadContextInfo {
    static ThreadLocal<UserDetail> user = new ThreadLocal<UserDetail>();;

    public static UserDetail getUser(){
        if(user !=null){
            return user.get();
        }
            throw new NullPointerException("没有用户上线文信息");
    }

    public static void setUser(UserDetail userDetail){
        user.set(userDetail);
    }

    public static void removeUser(){
        if(user != null){
            user.remove();
        }
    }
}
