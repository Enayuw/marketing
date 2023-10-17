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
     * 独立上传原始数据的集合，与builders中有效期范围不存在一一对应关系
     */
    private List<MarketingSyncUser> syncUsers = Lists.newArrayList();

    /**
     * 独立有效期范围集合，与syncUsers中上传数据不存在一一对应关系
     */
    private List<PeriodOfValidityBO.Builder> builders = Lists.newArrayList();
}
