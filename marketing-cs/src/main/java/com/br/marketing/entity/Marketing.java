package com.br.marketing.entity;

import lombok.Data;

/**
 * @Author: Bairong
 * @Time: 2021/2/6 15:49
 * @Company：百融
 * @Description: 功能描述
 */
@Data
public class Marketing implements Comparable{
    private String row;
    private Double score;
    public Marketing(String row) {
        this.row=row;
        String[] split = this.row.split(",");
        double v = Double.parseDouble(split[split.length - 1]);
        this.score=v;
    }


    @Override
    public int compareTo(Object o) {
        if(o instanceof Marketing){
            Marketing marketing = (Marketing) o;
            return marketing.score.compareTo(this.score);
        }
        throw new ClassCastException("不能转换为Marketing类型的对象...");
    }
}
