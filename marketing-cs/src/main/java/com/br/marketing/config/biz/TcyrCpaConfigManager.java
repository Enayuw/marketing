package com.br.marketing.config.biz;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.TcyrCpaFailMsgConfig;
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

        return null;
    }

}
