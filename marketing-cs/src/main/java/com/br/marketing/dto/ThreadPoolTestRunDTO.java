package com.br.marketing.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 线程池空跑请求（仅线上测试/Postman）：固定线程池名，启动 1 个空跑任务。
 * 放在 cs 包下供 inner 等各服务复用。
 */
@Data
public class ThreadPoolTestRunDTO {

    /** 空跑任务睡眠时长（毫秒），默认 60000 */
    @Min(100)
    @Max(600_000)
    private Integer taskSleepMs = 60_000;
}
