package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * QA自动化巡检：按配置动态查表取数。
 *
 * <p>注意：表名/列名需在 Service 层做白名单校验后再传入（此处使用 ${} 拼接）。</p>
 */
public interface AutoCheckDynamicDataMapper {

    /**
     * 查询指定表在“当天”的最新一条数据（按 create_time DESC, id DESC）。
     *
     * @param tableName     已校验的表名（建议带反引号，如：`b_xxx`）
     * @param selectColumns 已校验的列清单SQL片段（例如：id, DATE_FORMAT(create_time,'%Y-%m-%d %H:%i:%s') AS create_time, `a`, `b`）
     * @param today         yyyy-MM-dd
     * @param apiCode       api_code
     */
    Map<String, Object> selectLatestToday(@Param("tableName") String tableName,
                                         @Param("selectColumns") String selectColumns,
                                         @Param("today") String today,
                                         @Param("apiCode") String apiCode);

    /**
     * 查询指定表在“前一天 08:00:00~08:05:00”时间段的一条数据（按 create_time DESC, id DESC）。
     *
     * @param tableName     已校验的表名（建议带反引号）
     * @param selectColumns 已校验的列清单SQL片段
     * @param startTime     yyyy-MM-dd HH:mm:ss（例如：2026-01-14 08:00:00）
     * @param endTime       yyyy-MM-dd HH:mm:ss（例如：2026-01-14 08:05:00）
     * @param apiCode       api_code
     */
    Map<String, Object> selectLastDay8(@Param("tableName") String tableName,
                                       @Param("selectColumns") String selectColumns,
                                       @Param("startTime") String startTime,
                                       @Param("endTime") String endTime,
                                       @Param("apiCode") String apiCode);
}


