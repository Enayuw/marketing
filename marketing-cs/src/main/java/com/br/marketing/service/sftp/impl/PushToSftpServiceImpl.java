package com.br.marketing.service.sftp.impl;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.service.sftp.PushFinishSucService;
import com.br.marketing.service.sftp.PushService;
import com.br.marketing.service.sftp.PushToSftpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @ClassName PushToSftpServiceImpl
 * @Description TODO
 * @Author kongbx
 * @Date 2025/1/14 16:28
 */
@Service
@Slf4j
public class PushToSftpServiceImpl implements PushToSftpService {

    @Autowired
    PushService pushService;
    @Autowired
    RedisChgService redisChgService;
    @Autowired
    PushFinishSucService pushFinishSucService;

    @Override
    public Result pushFiles(List<LoanFile> pushList) {

        try {
            pushService.push(pushList);
        }catch (Exception e){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("推送文件至SFTP失败！");
        }
        for (LoanFile loanFile : pushList) {
            pushFinishSucService.pushFinish(loanFile.getId());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("推送文件至SFTP成功");
    }
}
