package com.br.marketing.vo.xiecheng.param;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.date.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "携程撞库规则列表查询参数")
public class CollidingRuleListParam implements Serializable {

    private static final long serialVersionUID = -5816759852739248423L;
    @Schema(description = "数据包名称")
    private String keyword;

    @Schema(description = "ApiCode")
    private String apiCode;

    @Schema(description = "任务状态")
    private Integer collidingSwitch;

    @Schema(description = "开启撞库时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private String collidingStartTime;

    @Schema(description = "结束撞库时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private String collidingEndTime;

    @Schema(description = "排序字段")
    private String orderField;

    @Schema(description = "排序类型 正序:asc 倒叙:desc")
    private String orderType;

    @Schema(description = "当前页数")
    private Integer current = 1;

    @Schema(description = "每页显示条数")
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
