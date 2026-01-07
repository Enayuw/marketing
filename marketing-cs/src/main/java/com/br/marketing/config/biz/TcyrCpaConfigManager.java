package com.br.marketing.config.biz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.entity.TcyrCpaFailMsgConfig;
import com.br.marketing.vo.tccpa.TcyrCpaFailMsgVO;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TcyrCpaConfigManager {

    private List<TcyrCpaFailMsgConfig> failMsgConfig;

    public TcyrCpaConfigManager create(List<JSONObject> tcyrCpaFailMsgConfig) {

        return TcyrCpaConfigManager.builder()
                .failMsgConfig(createFailMsgConfig(tcyrCpaFailMsgConfig))
                .build();
    }

    private List<TcyrCpaFailMsgConfig> createFailMsgConfig(List<JSONObject> tcyrCpaFailMsgConfig) {
        try {
            String jsonString = JSON.toJSONString(tcyrCpaFailMsgConfig);
            List<TcyrCpaFailMsgConfig> configs = JSON.parseObject(
                    jsonString,
                    new TypeReference<List<TcyrCpaFailMsgConfig>>() {}
            );
            return configs;
        } catch (Exception e) {
            throw new RuntimeException("JSON转换失败", e);
        }
    }

    public static List<TcyrCpaFailMsgVO> createFailMsgVOs(List<JSONObject> tcyrCpaFailMsgConfig) {
        try {
            String jsonString = JSON.toJSONString(tcyrCpaFailMsgConfig);
            List<TcyrCpaFailMsgVO> vos = JSON.parseObject(
                    jsonString,
                    new TypeReference<List<TcyrCpaFailMsgVO>>() {}
            );
            return vos;
        } catch (Exception e) {
            throw new RuntimeException("JSON转换失败", e);
        }
    }

}
