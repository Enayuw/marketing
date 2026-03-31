package com.br.marketing.datarelayservice.context;

/**
 * marketDataPush 落 sync_record 时的入口上下文（ThreadLocal）。
 * <p>
 * 标准 API 与 CPA 回落共用 {@link com.br.marketing.datarelayservice.processor.TcDataPushProcessor}，
 * 通过本上下文区分 {@code scene} 是否解析。
 * </p>
 */
public final class TcMarketDataPushContext {

    /**
     * HTTP 入口类型（仅影响 b_marketing_tcyr_sync_record.scene）。
     */
    public enum Entry {
        /**
         * /marketing/v1/api/marketDataPush：scene 固定为 null，走 common。
         */
        STANDARD_SYNC,
        /**
         * /marketing/v1/api/cpa/marketDataPush 且回落到标准 Processor：沿用原前缀解析（含 NEW）。
         */
        CPA_SYNC_FALLBACK
    }

    private static final ThreadLocal<Entry> HOLDER = new ThreadLocal<>();

    private TcMarketDataPushContext() {
    }

    /**
     * 设置当前线程入口类型，须在 {@code finally} 中 {@link #clear()}。
     *
     * @param entry 入口枚举，不可为 null
     */
    public static void set(Entry entry) {
        HOLDER.set(entry);
    }

    /**
     * @return 当前线程入口，未设置时返回 null
     */
    public static Entry get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
