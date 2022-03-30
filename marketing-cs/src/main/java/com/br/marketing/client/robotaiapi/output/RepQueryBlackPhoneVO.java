package com.br.marketing.client.robotaiapi.output;

import lombok.Data;

@Data
public class RepQueryBlackPhoneVO extends RobotParentVO {

    private String swiftNumber;

    private String accessNumber;

    private RepQueryBlackPhoneDetailVO data;


}
