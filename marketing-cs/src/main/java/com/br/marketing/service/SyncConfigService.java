package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;

/**
 * sftp账号配置业务接口
 *
 * @author songjuanjuan
 * @dateTime 2021/10/27 13:12
 */
public interface SyncConfigService {

    /**
     * 客户sftp账号列表
     * @param apiCode
     * @param srcPath
     * @param targePath
     * @return
     */
    ApiResult<Boolean> copySftp(String id, String apiCode, String srcPath, String targePath);

    /**
     * 复制sftp配置信息
     * @param page
     * @param pageSize
     * @param apiCode
     * @return
     */
    PageResultReturn getSftpList(int page, int pageSize, String apiCode);

    /**
     * 编辑sftp配置信息
     * @param id
     * @param apiCode
     * @param srcPath
     * @param targePath
     * @return
     */
    ApiResult<Boolean> editSftp(String id, String apiCode, String srcPath, String targePath);

}
