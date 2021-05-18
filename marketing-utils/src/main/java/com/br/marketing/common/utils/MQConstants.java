package com.br.marketing.common.utils;


public class MQConstants {
    public static final String exchangerName="loanWarningExchange";

    /**
     * queue
     */
    public static final String taskQueueName="taskQueue";
    public static final String pushQueueName="pushQueue";
    public static final String checkQueueName="checkQueue";


    /**
     * routingkey
     */
    public static final String taskRoutingKey = "taskRoutingKey";
    public static final String pushRoutingKey="pushRoutingKey";
    public static final String checkRoutingKey="checkRoutingKey";

}
