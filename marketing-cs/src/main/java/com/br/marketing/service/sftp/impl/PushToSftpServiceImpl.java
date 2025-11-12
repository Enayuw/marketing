package com.br.marketing.service.sftp.impl;

import com.alibaba.fastjson.JSON;
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

import java.util.ArrayList;
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

        //重试方法 这里反序列化过来的不是 LoanFile类型
        if (!(pushList.get(0) instanceof LoanFile)) {
            List<LoanFile> list = new ArrayList<>();
            for (int i = 0; i < pushList.size(); i++) {
                if (pushList.get(i) != null) {
                    list.add(JSON.parseObject(JSON.toJSONString(pushList.get(i)), LoanFile.class));
                }
            }
            pushList = list;
        }

        try {
            pushService.push(pushList);
        }catch (Exception e){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("推送文件至SFTP失败！");
        }
        /*for (LoanFile loanFile : pushList) {
            pushFinishSucService.pushFinish(loanFile.getId());
        }*/
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("推送文件至SFTP成功");
    }
}
