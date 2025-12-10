package com.br.marketing.datarelayservice.enums;

import com.br.marketing.datarelayservice.client.*;

public enum QiFuAiBizTypeEnum {
    UPLOAD_DATA("original", QiFuAiBizDataDTO.class),
    ROBOT_REPORT("robot_report", QiFuAiRobotReportBizDataDTO.class),
    ROBOT_RANKING_REPORT("robot_ranking_report", QiFuAiRobotRankingReportBizDataDTO.class),
    ROBOT_EVENT_PUSH("robot_event_push", QiFuAiRobotEventPushBizDataDTO.class),
    ROBOT_EFFECT("robot_effect", QiFuAiRobotEffectBizDataDTO.class);

    private String type;
    private Class<?> clazz;

    QiFuAiBizTypeEnum(String type, Class<?> clazz) {
        this.type = type;
        this.clazz = clazz;
    }

    public static Object getClassObject(String type) {
        for (QiFuAiBizTypeEnum classType : values()) {
            if (classType.type.equals(type)) {
                try {
                    return classType.clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create instance for type: " + type, e);
                }
            }
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }

    public String getType() {
        return type;
    }
}
