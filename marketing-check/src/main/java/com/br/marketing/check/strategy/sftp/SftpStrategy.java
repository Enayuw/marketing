package com.br.marketing.check.strategy.sftp;

import com.br.marketing.client.DecodeClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.PhoneSale;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * fstp文件处理策略
 *
 * @author Guo Zeqiang
 * @dateTime 2021/12/17 15:48
 */
public interface SftpStrategy {

    Result<Object> statisticsHead(String head, HashMap<Integer, String> address,
                                  HashMap<Integer, String> extSetField);

    Result<Object> setDataByPhone(String row, PhoneSale phoneSale, HashMap<Integer, String> address
            , HashMap<Integer, String> extSetFields, AtomicInteger errorMark
            , Integer line
            , String aesKey
            , DecodeClient decodeClient
    );
}
