package com.br.marketing.bo;

import com.br.marketing.entity.MarketingSyncUser;
import lombok.*;

/**
 * 原始数据（上传数据）有效期
 *
 * @author Guo Zeqiang
 * @dateTime 2023-03-22 10:48
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class SyncUserValidityPeriodBO {

    /**
     * 2023-03-22 10:52
     * 原始数据
     */
    private MarketingSyncUser syncUser;

    /**
     * 2023-03-22 10:53
     * 有效期范围
     */
    private PeriodOfValidityBO.Builder builder;
}
