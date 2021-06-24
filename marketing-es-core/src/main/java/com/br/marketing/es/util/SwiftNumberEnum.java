package com.br.marketing.es.util;

/**
 * 集群区分码
 * 集群区分码,取值：测试-A,预发-B,仿真-C,兆维-D,亦庄-E
 *
 * @Author linquan.guo
 * @CreateDate 2021/5/21 16:01
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/5/21 16:01
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
public enum SwiftNumberEnum {
    CLUSTER_PRE_B("B", "k8s-pretest"),
    CLUSTER_PROD_C("C", "k8s-prod-b"),
    CLUSTER_PROD_D("D", "k8s-prod-a"),
    CLUSTER_PROD_E("E", "k8s-prod-c");

    private final String code;
    private final String name;

    SwiftNumberEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getCode(String flag) {
        for (SwiftNumberEnum c : SwiftNumberEnum.values()) {
            if (c.getName().equals(flag)) {
                return c.code;
            }
        }
        return "A";
    }
}

