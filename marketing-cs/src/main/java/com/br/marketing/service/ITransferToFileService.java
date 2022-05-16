package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.TransferFileTask;

import java.util.List;

public interface ITransferToFileService {

    Result<List<TransferFileTask>> buildTransferTask(String apiCode,String jobParameter);

    Result actionTransferToFile(TransferFileTask transferFileTask);
}
