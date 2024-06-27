package com.br.marketing.client.qifu;


/**
 * @ClassName RealDataesReq
 * @Description TODO
 * @Author kongbx
 * @Date 2024/6/25 16:30
 */
public class RealDataesReq {

    /**
     * 唯一号
     */
    private String uniqueReqNo;
    /**
     * md5 手机号
     */
    private String mobileMd5;

    public String getUniqueReqNo() {
        return uniqueReqNo;
    }

    public void setUniqueReqNo(String uniqueReqNo) {
        this.uniqueReqNo = uniqueReqNo;
    }

    public String getMobileMd5() {
        return mobileMd5;
    }

    public void setMobileMd5(String mobileMd5) {
        this.mobileMd5 = mobileMd5;
    }

    @Override
    public String toString() {
        return "RealDataesReq{" +
                "uniqueReqNo='" + uniqueReqNo + '\'' +
                ", mobileMd5='" + mobileMd5 + '\'' +
                '}';
    }
}
