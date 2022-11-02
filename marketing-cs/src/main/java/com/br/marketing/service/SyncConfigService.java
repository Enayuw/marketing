package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.vo.SyncConfigEditVO;

import java.util.List;
import java.util.Map;

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
     * @param targetPath
     * @return
     */
    ApiResult<Boolean> copySftp(String id, String apiCode, String srcPath, String targetPath, Integer type, Integer dataType,
                                String suffix, String srcSftpHost, Integer srcSftpPort, String srcSftpUser, String srcSftpPwd,
                                String targetSftpHost, Integer targetSftpPort, String targetSftpUser, String targetSftpPwd);

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
     * @param vo
     * @return
     */
    ApiResult<Boolean> editSftp(SyncConfigEditVO vo);

    List<Map> getDataTypeList();

    String getPath();
}
