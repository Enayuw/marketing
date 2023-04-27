package com.br.marketing.service;

import java.util.List;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/4/27 15:39
 */
public interface DidiCallRecordService {
    void pushDidiCallRecord(List<Integer> pushDate,String sourceType);
}
