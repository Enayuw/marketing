package com.br.marketing.common.annoation;


public class RetryMarkContext {

    private static ThreadLocal<Integer> data = new ThreadLocal();

    public static Integer getData() {
        return data.get();
    }

    public RetryMarkContext(){
        data.set(1);
    }

    public static void removeData() {
        data.remove();
    }

    public static void setData(Integer value) {
        data.set(value);
    }

}