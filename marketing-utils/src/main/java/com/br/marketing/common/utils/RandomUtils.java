package com.br.marketing.common.utils;

import java.util.Random;

public class RandomUtils {

    /**
     * 生成随机数
     * @param n 位数
     * @return
     */
    public static String randomStr(int n){
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<n;i++){
            sb.append(random.nextInt(9));
        }
        return sb.toString();
    }

}
