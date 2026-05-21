package com.br.marketing.bridge.job.tc;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 同程易融分片任务使用的 apiCode 列表：优先 Speed 中 {@code tcyrMatchCandidateApiCodes}，未配置时回退 {@code tcyrApiCode}。
 */
public final class TcyrShardJobApiCodes {

    private TcyrShardJobApiCodes() {
    }

    public static List<String> resolve(MarketingCommonConfig config) {
        List<String> candidates = config.getTcyrMatchCandidateApiCodes();
        if (candidates != null && !candidates.isEmpty()) {
            return candidates.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
        }
        String single = config.getTcyrApiCode();
        if (StringUtils.isBlank(single)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(single.trim());
    }
}
