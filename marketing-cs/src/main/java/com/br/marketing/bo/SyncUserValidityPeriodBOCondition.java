package com.br.marketing.bo;

import lombok.*;

/**
 * 描述：： 有效期返回实体类扩展
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName SyncUserValidityPeriodBOCondition
 * @author: it-yml
 * @create: 2023-08-29 22:28
 * @Version 1.0
 * --------------------------------------
 **/
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class SyncUserValidityPeriodBOCondition extends SyncUserValidityPeriodBO{

    /**
     * 中原情况扩展字段
     * 1，2，3，4，5，6
     */
    private String condition;

    /**
     * 电销userType
     */
    private String dxUserType;
}
