package com.br.marketing.client.tc;

import com.br.marketing.client.ZipFileClient;
import com.br.marketing.common.commondto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * @Description TcyrServiceClient
 * @Author zhiyong.zhang
 * @CreateTime 2025/02/22
 */
@Slf4j
@Service
public class TcServiceClient {

    @Resource
    private ZipFileClient zipFileClient;


    public Result pullTcyrGzFileResult(String fileUrl, String targetPath) {
        Result result = new Result().failure();
        // 调用客户接口
        try {
            Result callResult = zipFileClient.downloadZipFile(fileUrl, targetPath, true);
            if(callResult == null || !callResult.isSuccess()){
                return result.failure();
            }
        } catch (Exception e){
            log.warn("queryConversionZipResult error", e);
            return result.failure();
        }
        return result.success();
    }



}