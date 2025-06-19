package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengSmsCollidingDataLogVt;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengSmsCollidingDataLogVtMapper extends XieChengSmsCollidingDataLogVtMapperBase {

    /**
     * 批量插入
     *
     * @param list
     */
    int saveBatchLogVt(@Param("list") List<XieChengSmsCollidingDataLogVt> list);

    /**
     * 修改返回参数
     * @param record
     * @return
     */
    int updateSelectiveVt(XieChengSmsCollidingDataLogVt record);

    /**
     * 批量更新
     * @param list
     * @param status
     * @param msg
     */
    void updateBatchVt(@Param("list") List<String> list,@Param("status") Integer status,@Param("msg")String msg,@Param("sendDate") Integer sendDate);

    XieChengSmsCollidingDataLogVt selectLatestVtLog(@Param("sha256Tel") String sha256Tel);
}
