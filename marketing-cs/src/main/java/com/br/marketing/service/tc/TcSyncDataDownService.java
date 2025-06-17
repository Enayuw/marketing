package com.br.marketing.service.tc;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTcyrSyncRecord;

import java.util.Date;
import java.util.List;

/**
 * 同城易融downToDb拉取文件数据入库
 * @author zhiyong.zhang
 * @date 2025/04/21
 */
public interface TcSyncDataDownService {

    //获取未匹配处理的同城易融批次请求记录
    List<MarketingTcyrSyncRecord> searchTcyrSyncList(String apiCode, Integer status);

    //处理单个同城易融具体批次batchNo的文件加载和同步db
    Result dealTcyrFileSync(MarketingTcyrSyncRecord syncRecord);

    //修改单个down的处理结果
    Integer updateTcyrRecordDownStatus(String batchNo, Integer status);

    //处理单个同城易融具体批次batchNo的文件 拉取GZ文件，txt信息入库
    void dealTcyrTxtFileSync(MarketingTcyrSyncRecord syncRecord);

}
