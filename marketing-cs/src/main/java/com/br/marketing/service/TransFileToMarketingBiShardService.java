package com.br.marketing.service;

import java.util.List;

/**
 * @author xiong.luo
 * @description: 内部服务器的转化文件落库到marketingBI(分片)
 * @date 2025/06/30
 */
public interface TransFileToMarketingBiShardService {
    void process(String jobParameter, List<Integer> shardingItems);
}
