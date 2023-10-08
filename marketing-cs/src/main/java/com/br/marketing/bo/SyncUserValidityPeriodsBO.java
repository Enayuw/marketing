package com.br.marketing.bo;

import com.br.marketing.entity.MarketingSyncUser;
import com.google.api.client.util.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;


/**
 * 全部上传原始数据+全部有效期范围
 *
 * @author senyang.zheng
 * @date 2023/10/07
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class SyncUserValidityPeriodsBO {

    /**
     * 全部上传原始数据
     */
    private List<MarketingSyncUser> syncUsers = Lists.newArrayList();

    /**
     * 全部有效期范围
     */
    private List<PeriodOfValidityBO.Builder> builders = Lists.newArrayList();
}
