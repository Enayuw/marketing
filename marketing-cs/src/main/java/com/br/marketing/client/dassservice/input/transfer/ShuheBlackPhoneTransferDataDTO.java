package com.br.marketing.client.dassservice.input.transfer;

import com.br.marketing.rule.InterfaceParams;
import lombok.Data;
/**
 *
 * @Description :数禾黑名单数据推转化 入参
 * ---------------------------------
 * @Author : lizhen
 * @Date : Create in 2022/5/28 14:39
 */
@Data
public class ShuheBlackPhoneTransferDataDTO extends InterfaceParams {

    /**
     * 营销中台编号
     */
    private String apiCode;

    /**
     * 案件编号
     */
    private String custNum;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 推送日期（yyyy-MM-dd）
     */
    private String pushDate;


}
