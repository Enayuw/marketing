package com.br.marketing.context;

/**
 * MQ幂等性上下文，使用ThreadLocal统一管理apiCode和tag
 */
public class MqIdempotentContext {
    
    private static final ThreadLocal<String> TAG_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> API_CODE_HOLDER = new ThreadLocal<>();
    
    /**
     * 设置MQ消息标签
     */
    public static void setTag(String tag) {
        TAG_HOLDER.set(tag);
    }
    
    /**
     * 获取MQ消息标签
     */
    public static String getTag() {
        return TAG_HOLDER.get();
    }
    
    /**
     * 设置客户编号
     */
    public static void setApiCode(String apiCode) {
        API_CODE_HOLDER.set(apiCode);
    }
    
    /**
     * 获取客户编号
     */
    public static String getApiCode() {
        return API_CODE_HOLDER.get();
    }
    
    /**
     * 清理所有ThreadLocal数据
     */
    public static void clear() {
        TAG_HOLDER.remove();
        API_CODE_HOLDER.remove();
    }
}

