package com.br.marketing.service;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.vo.SyncConfigEditVO;

import java.util.List;

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
                                String targetSftpHost, Integer targetSftpPort, String targetSftpUser, String targetSftpPwd,String srcType,
                                String targetType, Integer isUnzip, String unzipFilenameCharset);


    /**
     * 获取sftp列表
     *
     * @param page     页面
     * @param pageSize 页面大小
     * @param apiCode  apiCode
     * @param dataType 文件类型
     * @return {@link PageResultReturn }
     * @author senyang.zheng
     * @date 2023/09/12
     */
    PageResultReturn getSftpList(int page, int pageSize, String apiCode, Integer dataType);

    /**
     * 编辑sftp配置信息
     * @param vo
     * @return
     */
    ApiResult<Boolean> editSftp(SyncConfigEditVO vo);

    JSONArray getDataTypeList();

    String getPath();

    String getPullCustomerFilePath(String apiCode);

    ApiResult<Boolean> batchDeleteSftpList(List<Long> ids);

    /**
     * SFTP 文件后缀可选项（来自 speed：sftpFileSuffixOptions）
     */
    List<String> getSftpSuffixConfigs();
}
