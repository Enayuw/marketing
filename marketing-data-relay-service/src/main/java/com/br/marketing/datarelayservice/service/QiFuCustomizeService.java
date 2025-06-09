package com.br.marketing.datarelayservice.service;

import com.br.marketing.client.qifu.enums.CodeEnum;
import com.br.marketing.client.qifu.enums.FlagEnum;
import com.br.marketing.datarelayservice.client.QiFuAiReqDTO;
import javafx.util.Pair;

/**
 * @ClassName QiFuCustomizeService
 * @Description 奇富360促动接口
 * @Author kongbx
 * @Date 2025/6/9 14:05
 */
public interface QiFuCustomizeService {

    Pair<CodeEnum, FlagEnum> handle(QiFuAiReqDTO requestBody, String bizType);
}
