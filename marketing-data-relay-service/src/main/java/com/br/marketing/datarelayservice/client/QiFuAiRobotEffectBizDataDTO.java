package com.br.marketing.datarelayservice.client;

import com.br.marketing.entity.RobotEffectData;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName QiFuAiRobotEffectBizDataDTO
 * @Author hang.zhou
 * @Date 2025/12/5
 */
@Data
public class QiFuAiRobotEffectBizDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求流水号
     */
    private String flowNo;

    private List<RobotEffectData> list;

}
