package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCpsCollidingDataFront;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCpsCollidingDataFrontMapper extends XieChengCpsCollidingDataFrontMapperBase {
    
    /**
     * 使用窗口函数查询去重数据，按localId分页查询
     * @param localId 本地文件ID
     * @param minId 最小ID
     * @param pageSize 分页大小
     * @return 去重后的数据列表
     */
    List<XieChengCpsCollidingDataFront> selectNoDupDataByLocalIdtikv_(@Param("localId") Long localId,
                                                                      @Param("minId") Long minId,
                                                                      @Param("pageSize") Integer pageSize);

    /**
     * 批量更新推送状态
     * @param list 前端数据列表
     * @param localId 本地文件ID
     */
    void batchUpdatePushStatusByCell(@Param("list") List<XieChengCpsCollidingDataFront> list,
                                     @Param("localId") Long localId);
}