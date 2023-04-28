package com.br.marketing.service.Impl.transfertofile;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.service.ITransferToFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * 滴滴转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByDiDiServiceImpl implements ITransferToFileService {



    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }



    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        return null;
    }



    @Override
    public Result actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        return null;
    }
}
