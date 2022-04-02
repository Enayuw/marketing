package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;

import java.util.Set;

public interface IYiXinTransferService {
    Result actionYiXinToDx(String apiCode,String data);


    Result actionYiXinToRobotAI(String apiCode,String date);

}
