package com.br.marketing.client.robotaiapi.input;

import com.br.marketing.rule.InterfaceParams;
import lombok.Data;

/**
 * InterfaceData
 */
@Data
public class InterfaceData<T> extends InterfaceParams {

    private T data;

}