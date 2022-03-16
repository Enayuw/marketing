package com.br.marketing.client.dassservice.output;

import com.br.marketing.client.dassservice.input.black.BlackListDTO;
import lombok.Data;

import java.util.List;

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
 * @Description : 适配全局定时重试任务规则
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/2 20:06
 */
@Data
public class DassExportAdapterDTO {

    private Long transferInfoId;

    private List<BlackListDTO> list;

    public DassExportAdapterDTO(){}

    public DassExportAdapterDTO(List<BlackListDTO> blackListDTOS){
        this.list = blackListDTOS;
    }
}
