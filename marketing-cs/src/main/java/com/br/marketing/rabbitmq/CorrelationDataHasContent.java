package com.br.marketing.rabbitmq;

import lombok.Data;
import org.springframework.amqp.rabbit.connection.CorrelationData;

@Data
public class CorrelationDataHasContent extends CorrelationData {
    private Object message;

    public CorrelationDataHasContent(String id, Object message) {
        super(id);
        this.message = message;
    }
}
