package com.br.marketing.monkeydata.query;

/**
 * 查询电话及撞库日期
 *
 * @author Guo Zeqiang
 * @dateTime 2022-12-12 17:05
 */
public class ZhongAnCellZkDateQuery {

    /**
     * 手机号
     */
    private String cell;


    /**
     * 撞库日期
     */
    private String zkDate;

    public ZhongAnCellZkDateQuery() {
    }

    public ZhongAnCellZkDateQuery(String cell, String zkDate) {
        this.cell = cell;
        this.zkDate = zkDate;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getZkDate() {
        return zkDate;
    }

    public void setZkDate(String zkDate) {
        this.zkDate = zkDate;
    }

    @Override
    public String toString() {
        return "ZhongAnCellZkDate{" +
                "cell='" + cell + '\'' +
                ", zkDate='" + zkDate + '\'' +
                '}';
    }
}
