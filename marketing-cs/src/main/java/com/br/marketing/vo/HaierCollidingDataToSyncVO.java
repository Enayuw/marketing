package com.br.marketing.vo;

import lombok.Data;

/**
 * 海尔撞库待上传数据vo
 *
 * @author senyang.zheng
 * @date 2023/12/26
 */
@Data
public class HaierCollidingDataToSyncVO {

    private Long id;

    private String apiCode;

    private String mobileDigest;

    private String send_date;

}
