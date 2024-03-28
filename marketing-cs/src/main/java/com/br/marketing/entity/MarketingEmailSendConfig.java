package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_marketing_email_send_config
 * @author 
 */
@Data
public class MarketingEmailSendConfig implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 邮件发送主题，见EmailSubjectEnum枚举
     */
    private Integer subject;

    /**
     * 邮件接收人：，号分割
     */
    private String receiverUser;

    /**
     * 0-不含附件；1-有附件
     */
    private Integer isAttachment;

    /**
     * 附件文件名称
     */
    private String attachmentFileName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

    private static final long serialVersionUID = 1L;
}