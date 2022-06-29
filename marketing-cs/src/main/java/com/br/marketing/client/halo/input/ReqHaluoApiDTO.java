package com.br.marketing.client.halo.input;

import lombok.Data;
/**
 * @author lizhen
 * @Date 2022/05/12 16:46 AM
 * -----------------------------
 * @Description 哈啰openApi接口入参
 */
@Data
public class ReqHaluoApiDTO {
    /**
     * 业务参数的JSON字符串
     */
    private String data;

    /**
     * API接口名称
     */
    private String method;


}
