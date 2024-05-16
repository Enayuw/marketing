package com.br.marketing.vo.xiecheng.param;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.date.DateUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "携程撞库规则列表查询参数")
public class CollidingRuleListParam implements Serializable {

    private static final long serialVersionUID = -5816759852739248423L;
    @ApiModelProperty("数据包名称")
    private String keyword;

    @ApiModelProperty("ApiCode")
    private String apiCode;

    @ApiModelProperty("任务状态")
    private Integer collidingSwitch;

    @ApiModelProperty("开启撞库时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private String collidingStartTime;

    @ApiModelProperty("结束撞库时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private String collidingEndTime;

    @ApiModelProperty("排序字段")
    private String orderField;

    @ApiModelProperty("排序类型 正序:asc 倒叙:desc")
    private String orderType;

    @ApiModelProperty(value = "当前页数")
    private Integer current = 1;

    @ApiModelProperty(value = "每页显示条数")
    private Integer size = 20;

    // 添加自定义逻辑方法，在设置 collidingStartTime 时进行转换
    public void setCollidingStartTime() {
        this.collidingStartTime = formatDate(this.collidingStartTime);
    }

    // 添加自定义逻辑方法，在设置 collidingEndTime 时进行转换
    public void setCollidingEndTime() {
        this.collidingEndTime = formatDate(this.collidingEndTime);
    }

    // 自定义方法，用于将传入的时间字符串进行格式化
    private String formatDate(String dateString) {
        if (dateString == null) {
            return null;
        }
        // 假设 DateUtil 是一个工具类，用于处理日期格式
        return DateUtil.formatDateTime(DateUtil.parse(dateString, "yyyy-MM-dd+HH:mm:ss"));
    }

}
