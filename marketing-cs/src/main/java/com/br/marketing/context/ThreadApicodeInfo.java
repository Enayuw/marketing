package com.br.marketing.context;

/**
 * 用户权限 线程ApiCode
 *
 * @Author lizhen
 * @CreateDate 2022/07/14 14:29
 */
public class ThreadApicodeInfo {

    private static ThreadLocal<String> data = new ThreadLocal<String>();


    public static String getData() {
        return data.get();
    }

    public static void setData(String apiCode) {
        data.set(apiCode);
    }

    public static void removeData() {
        data.remove();
    }

}
