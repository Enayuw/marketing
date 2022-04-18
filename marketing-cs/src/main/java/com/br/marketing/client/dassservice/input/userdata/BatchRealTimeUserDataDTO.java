package com.br.marketing.client.dassservice.input.userdata;

import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.InterfaceParams;
import lombok.Data;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description :
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/29 15:39
 */

@Data
public class BatchRealTimeUserDataDTO extends InterfaceParams {

    /**
     * 批量电销接口入参
     */
    private DassImportDataDTO dassImportDataDTO;

    /**
     * 插入b_phone_sale_extend_info表入参
     */
    private PhoneSaleExtendInfo phoneSaleExtendInfo;
}
