package com.br.marketing.check.service;

/**
 * 文件无表头的转化数据清洗：按列顺序解析无表头文件并推送转化接口
 *
 * @author kongbx
 */
public interface FileNoHeaderToMarketingDataService {

    /**
     * 执行无表头文件转化清洗（查询 SyncConfig 原始数据文件(无表头)、下载文件、按 b_marketing_data_file_config_no_header 规则解析并推送）
     *
     * @param jobParameter 可为空；非空时仅处理该 apiCode
     */
    void process(String jobParameter);
}
