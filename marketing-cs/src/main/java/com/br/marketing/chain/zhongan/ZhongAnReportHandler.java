package com.br.marketing.chain.zhongan;

public interface ZhongAnReportHandler {
    /**
     * 执行一次检查
     *
     * @return true 表示通过；false 表示不通过，需要短路
     * @throws Exception 如果检查过程中有异常，可抛出，最终视为失败
     */
    boolean check(String cellMd5, String bizDate) throws Exception;
}
