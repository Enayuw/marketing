package com.br.marketing.datarelayservice.service;

import com.br.marketing.dto.zhongyuan.MtStandardResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * 中原消金坐席批量导入（MT 标准报文
 */
public interface ZhongYuanAgentService {

    MtStandardResponse importAgentCustomer(String jsonData, HttpServletRequest request);
}
