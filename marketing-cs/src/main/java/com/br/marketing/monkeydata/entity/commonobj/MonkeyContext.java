package com.br.marketing.monkeydata.entity.commonobj;


public class MonkeyContext {
    static final ThreadLocal<Object> context = new ThreadLocal<>();

    public static void setProcessContext(Object processContext) {
        context.set(processContext);
    }
    public static Object getProcessContext() {
        if(context !=null){
            return context.get();
        }
        throw new NullPointerException("没有获取到monkey上线文");
    }
    public static void clearProcessContext() {
        if(context !=null){
            context.remove();
        }
    }
}