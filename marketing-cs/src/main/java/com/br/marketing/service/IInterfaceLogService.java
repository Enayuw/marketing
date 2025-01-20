package com.br.marketing.service;

import com.br.marketing.dto.ApiRecordLogDTO;
import com.br.marketing.enums.ApiNmEnum;

import java.util.Map;

public interface IInterfaceLogService {

    /**
     * 获取接口日志的记录方式
     * @param apiNm 接口名称
     * @return
     */
    ApiRecordLogDTO isRecord(ApiNmEnum apiNm);
}
