package com.br.marketing.service.mark;

import com.br.marketing.entity.StraHisFile;

/**
 * @description 数据打标公共接口
 * @author hedongshuo
 * @date 2025/2/21 12:58
 **/
public interface DataMarkCommonService {

    /**
     * @description 获取当天最新的跑分文件记录
     * @param apiCode
     * @return com.br.marketing.entity.StraHisFile
     * @author hedongshuo
     * @date 2025/2/21 12:59
     **/
    public StraHisFile getStraHisFile(String apiCode);
}
