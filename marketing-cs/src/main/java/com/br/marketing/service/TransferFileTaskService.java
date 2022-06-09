package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;


/**
 * 转化文件任务业务接口
 *
 * @author songjuanjuan
 * @dateTime 2022/05/26 11:12
 */
public interface TransferFileTaskService {

    PageResultReturn getTransferFileList(int current, int size, String serach, String startDateStart, String startDateEnd);

    ApiResult reStartTransfer(Integer id);
}
