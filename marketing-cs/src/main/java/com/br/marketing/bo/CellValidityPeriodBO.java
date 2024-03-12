package com.br.marketing.bo;

/**
 * 手机号有效期
 *
 * @author Guo Zeqiang
 * @dateTime 2023-07-13 13:51
 */
public class CellValidityPeriodBO {
    /**
     * 2023-07-13 14:05
     * 手机号
     */
    private String cell;

    /**
     * 2023-07-13 14:05
     * 场景
     */
    private String userType;

    /**
     * 2023-07-13 14:05
     * 请求日期 yyyy-MM-dd
     */
    private String requestDate;


    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public CellValidityPeriodBO() {
    }

    public CellValidityPeriodBO(String cell) {
        this.cell = cell;
    }

    public CellValidityPeriodBO(String cell, String userType) {
        this.cell = cell;
        this.userType = userType;
    }

    public CellValidityPeriodBO(String cell, String userType, String requestDate) {
        this.cell = cell;
        this.userType = userType;
        this.requestDate = requestDate;
    }

    @Override
    public String toString() {
        return "CellValidityPeriodBO{" +
                "cell='" + cell + '\'' +
                ", userType='" + userType + '\'' +
                ", requestDate='" + requestDate + '\'' +
                '}';
    }
}
