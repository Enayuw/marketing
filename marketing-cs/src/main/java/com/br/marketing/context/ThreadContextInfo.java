package com.br.marketing.context;


import com.br.marketing.entity.auth.MarketingUserDetail;

public class ThreadContextInfo {
    static ThreadLocal<MarketingUserDetail> user = new ThreadLocal<MarketingUserDetail>();

    public static MarketingUserDetail getUser(){
        if(user !=null){
            return user.get();
        }
            throw new NullPointerException("没有用户上线文信息");
    }

    public static void setUser(MarketingUserDetail userDetail){
        user.set(userDetail);
    }

    public static void removeUser(){
        if(user != null){
            user.remove();
        }
    }
}
