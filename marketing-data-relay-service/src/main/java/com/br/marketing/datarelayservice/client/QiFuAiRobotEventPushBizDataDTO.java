package com.br.marketing.datarelayservice.client;

import com.br.marketing.entity.EventPushData;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName QiFURobotEventPushBizDataDTO
 * @Author hang.zhou
 * @Date 2025/11/17
 */
@Data
public class QiFuAiRobotEventPushBizDataDTO implements Serializable {

    private List<EventPushData> eventList;

}
