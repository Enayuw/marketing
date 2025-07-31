package com.br.marketing.service.Impl.xc;

import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.XieChengCpsCollidingDataFront;

import java.util.List;

/**
 * 携程CPS撞库业务服务接口
 * @Author chenh
 * @Date 2025-06-26
 */
public interface XieChengCpsCollidingDataBusinessService {
    
    /**
     * 插入到rob表并更新front表状态
     * @param frontList front表数据列表
     * @param localFile 本地文件信息
     */
    void insertToRobAndUpdateFront(List<XieChengCpsCollidingDataFront> frontList, LocalFile localFile);
} 