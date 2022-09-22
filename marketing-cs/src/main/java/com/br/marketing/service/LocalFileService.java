package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;

/**
 * 文件接口
 * <p>
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.service
 * @Description: 文件接口
 * @CreateTime: 2022-09-15 15 :32
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
public interface LocalFileService {

    /**
     * 获取客户信息列表数据
     * @param page
     * @param pageSize
     * @param apiCode
     * @return
     */
    PageResultReturn list(int page, int pageSize,String search, String apiCode, String uploadStartTime,String uploadEndTime, String fileType);
    Integer allCount();
}
