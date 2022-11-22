package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.TransferFileTask;

import java.util.List;

public interface ITransferToFileService {

    /**
     * 获取对应apicode的参数
     * 比如自定义参数为 7410785#20220711,true;7412003#123 此时的apicode是7410785，返回的为20220711,true
     * @param apiCode
     * @param jobParameter
     * @return
     */
    String isMyParam(String apiCode,String jobParameter);

    Result<List<TransferFileTask>> buildTransferTask(String apiCode,String myParam);

    Result actionTransferToFile(TransferFileTask transferFileTask,String jobParameter);
}
