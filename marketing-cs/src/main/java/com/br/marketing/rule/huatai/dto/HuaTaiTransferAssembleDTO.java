package com.br.marketing.rule.huatai.dto;

import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.InterfaceParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 华泰上传转转化：规则中心透传参数，转化 JSON 在 Handler 中组装。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class HuaTaiTransferAssembleDTO extends InterfaceParams {

    private MarketingSyncUser syncUser;
}
