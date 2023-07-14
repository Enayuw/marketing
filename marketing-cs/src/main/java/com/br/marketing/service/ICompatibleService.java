package com.br.marketing.service;

/**
 * 迁移兼容服务
 */
public interface ICompatibleService {

    /**
     * 是否执行，指定机房
     * @param config
     * @return
     */
    Boolean isAction(String config,String jobName);
}
