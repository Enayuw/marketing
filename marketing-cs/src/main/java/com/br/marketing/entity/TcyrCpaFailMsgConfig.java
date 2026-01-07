package com.br.marketing.entity;

import lombok.Data;

@Data
public class TcyrCpaFailMsgConfig {

    private Integer failMsg;

    private String desc;

    private boolean ifWithReleaseTime;

    private Integer lockBelong;

}
