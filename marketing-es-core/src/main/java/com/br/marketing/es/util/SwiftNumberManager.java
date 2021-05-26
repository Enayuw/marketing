package com.br.marketing.es.util;

import java.text.SimpleDateFormat;

/**
 * 流水号生成
 *
 * @Author yunfei.dong
 * @CreateDate 2019/7/10 11:49
 * @UpdateUser linquan.guo
 * @UpdateDate 2019/7/10 11:49
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
public class SwiftNumberManager {
    private static final String YMDHMS = "yyyyMMddHHmmss";

    private SwiftNumberManager() {
        throw new IllegalStateException("Utility class");
    }

    private static class SwiftNumberHolder {
        // This will be lazily initialised
        public static final SwiftNumber RESOURCE = new SwiftNumber();
    }

    public static SwiftNumber getSwiftNumberManager() {
        return SwiftNumberManager.SwiftNumberHolder.RESOURCE;
    }

    public static class SwiftNumber {
        private static long SEQUENCE_ID = System.currentTimeMillis();
        private static final Object SYNC_LOCK = new Object();
        private static final String ADD = SwiftNumberV2Additional.getAdditional();

        public String getSwiftNumber() {
            String date = new SimpleDateFormat(YMDHMS).format(System.currentTimeMillis());
            String nextTime;
            synchronized (SYNC_LOCK) {
                nextTime = String.valueOf(getNextId());
            }
            //当前时间+下划线+时间戳最后四位
            return new StringBuilder().append(date).append('_').append(nextTime.substring(nextTime.length() - 4)).toString();
        }

        public String getSwiftNumberPre() {
            String date = new SimpleDateFormat(YMDHMS).format(System.currentTimeMillis());
            String nextTime;
            synchronized (SYNC_LOCK) {
                nextTime = String.valueOf(getNextId());
            }
            //当前时间+下划线+时间戳最后四位
            return new StringBuilder().append(date).append('_').append(nextTime.substring(nextTime.length() - 4))
                    .append(ADD).toString();
        }

        /**
         * 时间戳+1
         *
         * @param
         * @return
         */
        private static long getNextId() {
            return SEQUENCE_ID++;
        }
    }

}