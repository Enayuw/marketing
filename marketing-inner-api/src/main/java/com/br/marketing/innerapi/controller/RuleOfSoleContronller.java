package com.br.marketing.innerapi.controller;

import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.innerapi.config.ThreadContextInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rule/sole")
public class RuleOfSoleContronller {

    @GetMapping("/test")
    public String test(){
        /**
         * 获取用户上线文
         */
        UserDetail user = ThreadContextInfo.getUser();
        return "Success";
    }
}
