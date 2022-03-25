package com.br.marketing.client.robotaiapi.output;

import java.util.List;

public class RepQueryBlackPhoneDetailVO {

    /**
     * 成功的返回结果
     */
    private List<SuccessData> successData;
    /**
     * 校验失败的结果
     */
    private List<ErrorData> errorData;

    public static class SuccessData {

        /**
         * Y命中 N未命中
         */
        private String blackFlag;
        /**
         * 数据ID
         */
        private String dataId;
    }

    public static class ErrorData {

        /**
         * 错误原因
         */
        private String errorReason;
        /**
         * 数据ID
         */
        private String dataId;
    }


}
