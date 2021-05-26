package com.br.marketing.es.util.es;

/**
 * 从流水号中获取yyyyMM格式的日期
 *
 * @Author linquan.guo
 * @CreateDate 2020/12/29 19:55
 * @UpdateUser linquan.guo
 * @UpdateDate 2020/12/29 19:55
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
public class EsHandleUtil {

    /**
     * 从流水号中获取yyyyMM_数字格式的日期，用于ES数据存储
     *
     * @param swiftNumber 举例：4002055_20191010010034_94784404P09
     * @return java.util.List<java.lang.String>  yyyyMM_01格式的日期
     */
    public static String getDateFromSwiftNumber(String swiftNumber) {
        String[] number = swiftNumber.split("_");
        String one = number[1].substring(0, 6);
        int i = Integer.parseInt(number[1].substring(6, 8)) / 11;
        i++;
        return String.format("%s_0%s", one, i);
    }

    /**
     * 从日期中获取索引
     *
     * @param ymd
     * @return
     */
    public static String getIndexFromStr(String ymd) {
        String one = ymd.substring(0, 6);
        int i = Integer.parseInt(ymd.substring(6, 8)) / 11;
        i++;
        return String.format("%s_0%s", one, i);
    }
}
