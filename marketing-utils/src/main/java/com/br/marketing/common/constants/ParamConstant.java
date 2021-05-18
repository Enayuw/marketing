package com.br.marketing.common.constants;

/**
 * ${tags}
 *
 * @version V1.0
 * @ClassName: ${type_name}
 * @author: xiaowen.wang
 * @company BaiRong
 * @date ${date} ${time}
 * ${tags}
 * @Motified by:
 */
public class ParamConstant {

    /**
     * 流水号的版本枚举
     */
    public enum SwiftNumberVersionEnum{
        //1.0版本
        FIRST("v1","1.0版本"),
        //2.0版本
        SECOND("v2","2.0版本");
        private final String code;
        private final String name;
        SwiftNumberVersionEnum(String code, String name){
            this.code = code;
            this.name = name;
        }
        public String getCode(){ return code;}
        public String getName(){
            return name;
        }
    }

    /**
     * 集群区别枚举
     */
    public enum ClusterEnum{
        //k8s-pretest
        CLUSTER_PRE("P","k8s-pretest"),
        //k8s-prod-a
        CLUSTER_PROD_A("A","k8s-prod-a"),
        //k8s-prod-b
        CLUSTER_PROD_B("B","k8s-prod-b"),
        //k8s-prod-c
        CLUSTER_PROD_C("C","k8s-prod-c");
        private final String code;
        private final String name;
        ClusterEnum(String code, String name){
            this.code = code;
            this.name = name;
        }

        public String getCode(){ return code;}
        public String getName(){
            return name;
        }
        public static String getCode(String flag){
            for(ClusterEnum c : ClusterEnum.values()){
                if(c.getName().equals(flag)){
                    return c.code;
                }
            }
            return "Z";
        }
    }


}
