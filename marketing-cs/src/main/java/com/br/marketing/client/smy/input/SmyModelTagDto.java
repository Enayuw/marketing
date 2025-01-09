package com.br.marketing.client.smy.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description 萨摩耶 client
 * @Author bin.li1
 * @CreateTime 2024-12-19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmyModelTagDto {
    /**
     * 营销时间 名单计划营销的时间。归因周期要用 格式：2023-08-22 10:00:00
     */
    private String marketingTime;
    /**
     *名单列表
     */
    private List<BatchHitValue> nameList;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchHitValue {
        /**
         * 名单值  手机号md5
         */
        private  String nameValue;
        /**
         * 营销标识 营销明细：mk_record 短信明细：dx_mk_record AI语音明细：ai_mk_record 沉睡户拒营销明细：wp_black_record
         */
        private  String modelTag;

    }

}

