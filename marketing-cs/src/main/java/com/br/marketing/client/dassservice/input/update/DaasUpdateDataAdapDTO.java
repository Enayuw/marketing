package com.br.marketing.client.dassservice.input.update;

import lombok.Data;

import java.util.List;

/**
 * @Description : 人工业务数据更新接口适配器DTO
 * ---------------------------------
 * @Author : Assistant
 * @Date : Create in 2024/12/18
 */
@Data
public class DaasUpdateDataAdapDTO {

    private List<DaasUpdateDataDTO> daasUpdateDataDTOList;

} 