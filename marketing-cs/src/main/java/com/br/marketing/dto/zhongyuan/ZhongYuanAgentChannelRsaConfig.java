package com.br.marketing.dto.zhongyuan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 对应 {@link com.br.marketing.speedconfig.MarketingCommonConfig#getZhongYuanAgentChannelRsa()} 的 JSON。
 */
@Data
public class ZhongYuanAgentChannelRsaConfig {

    private String apiCode;

    /** 中原公钥 Base64 */
    private String publicKey;

    /** 合作方私钥 Base64 */
    private String privateKey;

    public static ZhongYuanAgentChannelRsaConfig fromConfigJson(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            JSONObject obj = JSON.parseObject(json.trim());
            if (obj == null) {
                return null;
            }
            ZhongYuanAgentChannelRsaConfig c = new ZhongYuanAgentChannelRsaConfig();
            c.setApiCode(firstString(obj, "apiCode"));
            c.setPublicKey(firstString(obj, "publicKey", "PublicKey"));
            c.setPrivateKey(firstString(obj, "privateKey", "PrivateKey"));
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    private static String firstString(JSONObject obj, String... keys) {
        for (String k : keys) {
            String v = obj.getString(k);
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return null;
    }
}
