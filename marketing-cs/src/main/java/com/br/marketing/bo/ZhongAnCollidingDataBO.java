package com.br.marketing.bo;

import com.br.marketing.entity.MarketingSyncUser;
import lombok.Data;

@Data
public class ZhongAnCollidingDataBO {
    private Long smsId;
    private Long callId;
    private String apiCode;
    private String caseNum;
    private String mobileMd5;
    private String userType;
    private String bizDate;
    private Integer isConnect;
    private Integer smsSendStatus;

    private MarketingSyncUser syncUser;
}
