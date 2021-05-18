package com.br.marketing.entity;

import lombok.Builder;
import lombok.Data;

/**
 * Created by Bairong on 2019/10/20.
 */
@Data
@Builder
public  class DayCompare{
    private int year;
    private int month;
    private int day;

    @Override
    public String toString() {
        return "DayCompare{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                '}';
    }
}
