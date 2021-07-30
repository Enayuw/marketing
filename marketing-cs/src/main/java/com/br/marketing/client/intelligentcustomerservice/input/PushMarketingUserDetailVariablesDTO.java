package com.br.marketing.client.intelligentcustomerservice.input;

import lombok.Data;

import java.io.Serializable;

@Data
public class PushMarketingUserDetailVariablesDTO implements Serializable {

    public static final long serialVersionUID = 1L;

    /**
     *评分结果
     */
    private String score;

    /**
     *跑评分日期
     */
    private String scoreDate;

    /**
     *模型英文名称
     */
    private String scoreName;

    /**
     *上传日期
     */
    private String update;

    private String taskId;

    private String groupType;
}
