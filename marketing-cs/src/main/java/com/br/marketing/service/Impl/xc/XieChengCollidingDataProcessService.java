package com.br.marketing.service.Impl.xc;

import com.br.marketing.enums.XcProcessTaskEnum;

public interface XieChengCollidingDataProcessService {
    void process();

    /**
     * @description 动态补充包剔除
     * @return void
     * @author hedongshuo
     * @date 2024/11/8 16:16
     **/
    void processDynaDelete();

     boolean queryDeletingTaskCount(String apiCode, XcProcessTaskEnum xcProcessTaskEnum);
}
