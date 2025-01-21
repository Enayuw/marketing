package com.br.marketing.service.sftp;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.LoanFile;

import java.util.List;

/**
 * @ClassName PushToSftpService
 * @Description 跑分文件推送Sftp
 * @Author kongbx
 * @Date 2025/1/14 16:27
 */
public interface PushToSftpService {

    Result pushFiles(List<LoanFile> pushList);

}
