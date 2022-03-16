package com.br.marketing.vo;

import lombok.Data;

/**
 * -----------------------------
 *
 * @author guangchao.zhang
 * @Date 2022/3/7 4:49 PM
 * -----------------------------
 * @Description 哈啰数据
 */
@Data
public class HaloCallingDataVo {
    private Long id;

    private String customerNo;

    private Long callStartTime;

    private String userType;

    private String taskId;
}
