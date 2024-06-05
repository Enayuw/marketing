package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;

import java.util.Date;

public interface IYiXinTransferService {
    Result actionYiXinToDx(String apiCode,String data);


    Result actionYiXinToRobotAI(String apiCode,String date);

    Result actionHaierToDx(String apiCode);

    Result<Date> checkPush(String apiCode, String date);
}
