package com.br.marketing.service;


import java.time.LocalDate;

/**
 * 众邦转化数据推人工转化过滤接口
 * @author chenh
 * @version 1.0
 * @date 2023/6/16 17:19
 */
public interface ZhongBangToDassFilterProcessService {
    // 首次
    void doProcessFirst(LocalDate requestDate);
    // 非首次
    void doProcessNoFirst(LocalDate requestDate);

}
