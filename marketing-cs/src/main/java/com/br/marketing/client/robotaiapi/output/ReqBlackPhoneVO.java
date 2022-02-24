package com.br.marketing.client.robotaiapi.output;

import lombok.Data;

import java.util.List;

@Data
public class ReqBlackPhoneVO extends RobotParentVO {
    private String swiftNumber;
    private List<RepBlackPhoneDetailVO> data;
}
