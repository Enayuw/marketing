package com.br.marketing.client.zhongan.input;

import com.br.marketing.rule.InterfaceParams;
import lombok.Data;

@Data
public class ZaRosterLockingDataDTO  extends InterfaceParams {

    /**
     * apicode
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 类型
     */
    private String type;

    /**
     * MD5手机号
     */
    private String caseNum;

    /**
     * MD5手机号
     */
    private String mobileMd5;

    /**
     * 营销日期,yyyy-MM-dd
     */
    private String bizDate;

    /**
     * 枚举,CG/MG
     */
    private String tag;

    /**
     * 状态 1-sftp文件;2-拨打明细
     */
    private Integer dataSource;

    /**
     * 机构运营场景
     */
    private String userType;

    /**
     * 是否接通(0-否;1-是)
     */
    private Integer isConnect;
}