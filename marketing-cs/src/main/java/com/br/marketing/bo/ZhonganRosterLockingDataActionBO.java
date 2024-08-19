package com.br.marketing.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 众安锁定名单Action组合属性
 *
 * @author senyang.zheng
 * @date 2024/07/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZhonganRosterLockingDataActionBO {

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 营销日期,yyyy-MM-dd
     */
    private String bizDate;

    /**
     * 枚举,CG/MG
     */
    private String tag;

    /**
     * 1-sftp文件;2-拨打明细
     */
    private Integer dataSource;

    /**
     * 运营场景
     */
    private String userType;

    /**
     * 是否接通(0-否;1-是)
     */
    private Integer isConnect;

}
