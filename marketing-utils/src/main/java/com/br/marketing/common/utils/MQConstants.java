package com.br.marketing.common.utils;


public class MQConstants {
    public static final String EX_CHANGER_NAME = "loanWarningExchange";

    //营销平台交换机
    public static final String MARKETINGEXCHANGER_NAME = "gate";

    //营销平台死信交换机
    public static final String MARKETINGEXCHANGER_DEAD_NAME = "deadgate";


    /**
     * queue
     */
    public static final String TASK_QUEUE_NAME = "taskQueue";
    public static final String PUSH_QUEUE_NAME = "pushQueue";
    public static final String CHECK_QUEUE_NAME = "checkQueue";

    //异步处理人员入库的队列
    public static final String MARKETING_PRE_USER_RECEIVE = "Marketing_PreUser_Receive";
    public static final String MARKETING_TRANSFER_RECEIVE = "Marketing_Transfer_Receive";
    public static final String MARKETING_TRANSFER_PUSH_CUSTOMER = "Marketing_Transfer_Push_Customer";
    public static final String MARKETING_TRANSFER_PUSH_BLACK = "Marketing_Transfer_Push_Black";
    public static final String MARKETING_USER_RECEIVE = "Marketing_User_Receive";
    public static final String MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH_DELAY = "Marketing_Push_CustomerService_Search_Delay";
    public static final String MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH = "Marketing_Push_CustomerService_Search";
    public static final String MARKETING_PUSH_CUSTOMER_SERVICE = "Marketing_Push_CustomerService";
    public static final String MARKETING_PUSH_DASS_SCORE = "Marketing_Push_Dass_Score";
    public static final String MARKETING_PUSH_TWOSEVEN_FILETRANSFER = "Marketing_Push_Seven_FileTransfer";

    /**
     * routingkey
     */
    public static final String TASK_ROUTING_KEY = "taskRoutingKey";
    public static final String PUSH_ROUTING_KEY = "pushRoutingKey";
    public static final String CHECK_ROUTING_KEY = "checkRoutingKey";

    public static final String ROUTING_KEY_MARKETING_PRE_USER_RECEIVE = "Marketing.PreUser.Receive";
    public static final String ROUTING_KEY_MARKETING_TRANSFER_RECEIVE = "Marketing.Transfer.Receive";
    public static final String ROUTING_KEY_MARKETING_USER_RECEIVE = "Marketing.User.Receive";
    public static final String ROUTING_KEY_MARKETING_TRANSFER_PUSH_CUSTOMER = "Marketing.Transfer.Push.Customer";
    public static final String ROUTING_KEY_MARKETING_TRANSFER_PUSH_BLACK = "Marketing.Transfer.Push.Black";
    public static final String ROUTING_KEY_MARKETING_PUSH_CUSTOMER_SERVICE_SEARCH_DELAY = "Marketing.Push.CustomerService.Search.Delay";
    public static final String ROUTING_KEY_MARKETING_PUSH_CUSTOMER_SERVICE = "Marketing.Push.CustomerService";
    public static final String ROUTING_KEY_MARKETING_PUSH_DASS_SCORE = "Marketing.Push.Dass.Score";
    public static final String ROUTING_KEY_MARKETING_PUSH_TWOSEVEN_FILETRANSFER = "Marketing.Push.Seven.FileTransfer";

}
