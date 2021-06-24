package com.br.marketing.common.utils;


import java.util.Random;

public class RandomUtils {

    private final static Random rand = new Random();

    /**
     * 生成随机数
     * @param n 位数
     * @return
     */
    public static String randomStr(int n){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<n;i++){
            sb.append(rand.nextInt(9));
        }
        return sb.toString();
    }

}
