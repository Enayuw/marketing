package com.br.marketing.context;


import com.br.marketing.entity.MarketingInfoLog;

/**
 * 运行时线程共享数据
 *
 * @Author linquan.guo
 * @CreateDate 2020/9/14 17:29
 * @UpdateUser linquan.guo
 * @UpdateDate 2020/9/14 17:29
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
public class RuntimeDataContext {

    private static ThreadLocal<MarketingInfoLog> data = new ThreadLocal();

    public static MarketingInfoLog getData() {
        return data.get();
    }

    public static void initData() {
        data.remove();
        data.set(new MarketingInfoLog());
    }

    public static void removeData() {
        data.remove();
    }

    public static void setData(MarketingInfoLog marketingInfoLog) {
        data.set(marketingInfoLog);
    }

}