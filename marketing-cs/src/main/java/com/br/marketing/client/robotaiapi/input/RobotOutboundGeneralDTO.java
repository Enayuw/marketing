package com.br.marketing.client.robotaiapi.input;

import lombok.Data;

@Data
public class RobotOutboundGeneralDTO<T> {

    private String apiCode;

    private T jsonData;
}
