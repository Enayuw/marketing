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
public interface TcSyncDataDownFileService {

    //处理单个同城易融具体批次batchNo的文件 拉取GZ文件，txt信息入库
    Result dealTcyrTxtFileSync(MarketingTcyrSyncRecord syncRecord);


}
