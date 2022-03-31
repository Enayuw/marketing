package com.br.marketing.client.dassservice.input.userdata;

import com.br.marketing.client.dassservice.input.DassImportDataDTO;
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
 * @Description : 批量调电销接口入参
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/29 15:35
 */

@Data
public class DassBatchImportDataDTO extends DassImportDataDTO {

    /**
     * 拨打优先级（枚举值：1、2、3）
     */
    private String prioritySymbol;
    /**
     * 筛选项1
     */
    private String raiseLimiSuccess;

}
