package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengSmsCollidingDataLog;
import com.br.marketing.entity.XieChengSmsCollidingDataLogVt;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengSmsCollidingDataLogVtMapper extends XieChengSmsCollidingDataLogVtMapperBase {

    /**
     * 批量插入
     *
     * @param list
     */
    void saveBatchLogVt(@Param("list") List<XieChengSmsCollidingDataLogVt> list);

    int updateSelectiveVt(XieChengSmsCollidingDataLogVt record);

    void updateBatchVt(@Param("list") List<String> list,@Param("status") Integer status,@Param("msg")String msg);
}
