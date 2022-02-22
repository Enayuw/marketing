package com.br.marketing.client.robotaiapi.input;

import lombok.Data;

import java.util.List;

@Data
public class BlackPhoneDTO<T> {
    private String method;
    private List<T> data;
}
