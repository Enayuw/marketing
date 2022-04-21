package com.br.marketing.client.yiqianbao.input;

import lombok.Data;

import java.util.List;

@Data
public class YqbDetailVo {

    public List<UserInfo> userInfoList;


    @Data
    public static class UserInfo {
        /**
         * 数据下发时间
         */
        private String dataTime;

        /**
         * 订单编号，用于唯一标识数据记录
         */
        private String outerApplyNo;

        /**
         * 用户手机号md5
         */
        private String phoneMd5;

        /**
         * 是否营销Y/N
         */
        private String marketFlag;
    }


}
