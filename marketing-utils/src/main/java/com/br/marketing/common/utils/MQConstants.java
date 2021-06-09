package com.br.marketing.common.utils;


public class MQConstants {
    public static final String exchangerName="loanWarningExchange";

    //营销平台交换机
    public static final String MarketingexchangerName = "gate";

    //营销平台死信交换机
    public static final String MarketingexchangerDeadName = "deadgate";


    /**
     * queue
     */
    public static final String taskQueueName="taskQueue";
    public static final String pushQueueName="pushQueue";
    public static final String checkQueueName="checkQueue";

    //异步处理人员入库的队列
    public static final String Marketing_PreUser_Receive = "Marketing_PreUser_Receive";
    public static final String Marketing_Push_CustomerService_Search_Delay = "Marketing_Push_CustomerService_Search_Delay";
    public static final String Marketing_Push_CustomerService_Search = "Marketing_Push_CustomerService_Search";

    /**
     * routingkey
     */
    public static final String taskRoutingKey = "taskRoutingKey";
    public static final String pushRoutingKey="pushRoutingKey";
    public static final String checkRoutingKey="checkRoutingKey";

    public static final String RoutingKey_Marketing_PreUser_Receive = "Marketing.PreUser.Receive";
    public static final String RoutingKey_Marketing_Push_CustomerService_Search_topic = "Marketing.Push.CustomerService.Search.#";
    public static final String RoutingKey_Marketing_Push_CustomerService_Search_Delay = "Marketing.Push.CustomerService.Search.Delay";
    public static final String RoutingKey_Marketing_Push_CustomerService_Search = "Marketing.Push.CustomerService.Search";




}
