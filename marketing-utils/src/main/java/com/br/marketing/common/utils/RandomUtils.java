package com.br.marketing.common.utils;


import java.util.Random;

public class RandomUtils {

    private final static Random RAND = new Random();

    /**
     * 生成随机数
     * @param n 位数
     * @return
     */
    public static String randomStr(int n){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<n;i++){
            sb.append(RAND.nextInt(9));
        }
        return sb.toString();
    }

}
