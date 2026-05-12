package com.br.marketing.service.tc;

import com.br.marketing.common.commondto.Result;

/**
 * 同程易融标准链路：按 batchNo 将 sync_record.api_code 从空补齐为灵霄匹配结果。
 */
public interface TcyrSyncRecordApiCodeFillService {

    /**
     * 幂等：已为相同 apiCode 时直接成功；已存在不同 apiCode 时拒绝覆盖。
     */
    Result<Void> fillApiCode(String batchNo, String apiCode);
}
