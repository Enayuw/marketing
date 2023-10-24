package com.br.marketing.client.robotaiapi.output;

import lombok.Data;

import java.util.List;

/**
 * @Description TransferRobot
 * @Author hong.chen
 * @CreateTime 2023/06/13
 */
@Data
public class TransferRobotDataVO {
    List<UnsuccessfulData> unsuccessfulData;
}
