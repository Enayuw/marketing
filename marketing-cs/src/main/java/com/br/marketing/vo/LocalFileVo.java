package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 文件返回vo
 * <p>
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.vo
 * @Description: 文件返回vo
 * @CreateTime: 2022-09-15 15 :50
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@Data
public class LocalFileVo {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     *
     */
    @ApiModelProperty(value = "cid")
    private String cid;

    /**
     *
     */
    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    @ApiModelProperty(value = "客户名称")
    private String shortName;
    /**
     * 文件类型
     */
    @ApiModelProperty(value = "文件类型")
    private String fileType;

    /**
     *
     */
    @ApiModelProperty(value = "文件名称")
    private String fileName;


    /**
     * 状态，1校验通过，2表头有问题，3数据有问题
     */
    @ApiModelProperty(value = "上传状态  非1  为异常")
    private String complete;

    /**
     *
     */
    @ApiModelProperty(value = "上传数量级")
    private Integer actualNumber;

    /**
     *推送数量
     */
    @ApiModelProperty(value = "推送数量级")
    private Integer pushNumber;

    /**
     *错误数量
     */
    @ApiModelProperty(value = "错误数据数量级")
    private Integer errorActualNumber;

    /**
     * 推送开始时间
     */
    @ApiModelProperty(value = "推送开始时间")
    private Date pushStartTime;

    /**
     * 推送结束时间
     */
    @ApiModelProperty(value = "推送结束时间")
    private Date pushEndTime;

    /**
     * 上传时间
     */
    private Date createTime;

}
