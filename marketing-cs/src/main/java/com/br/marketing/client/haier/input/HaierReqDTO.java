package com.br.marketing.client.haier.input;

import com.br.marketing.client.haier.output.PushDTO;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class HaierReqDTO {
    List<Long> ids;

    PushDTO.FormData formData;

    String apiCode;

    HashMap<String,String> ruleMap;
}
