package com.br.marketing.chain.zhongan;

public interface ZhongAnReportHandler {
    /**
     * 执行一次检查
     *
     * @param cellMd5 cellMd5
     * @param bizDate bizDate
     * @return boolean
     * @throws Exception 异常
     * @author senyang.zheng
     * @date 2025/07/22
     */
    boolean check(String cellMd5, String userType, String bizDate) throws Exception;

    String ruleName();
}
