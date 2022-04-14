package com.br.marketing.client.robotaiapi.input;

import lombok.Data;

import java.util.List;

/**
 * @author lizhen
 * @date 2022/03/25 16:55
 * @Description 宜信黑名单查询客服入参
 */
@Data
public class ReqBlackPhoneQueryDTO {

    private String apiCode;

    private List<BlackQueryDetailDTO> detailBlackPhoneDTO;



}
