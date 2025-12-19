package com.br.marketing.entity.common;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TimeRangePlus extends TimeRange{

    int order;

    boolean isToday;

    public TimeRangePlus(TimeRange timeRange, int order) {
        super(timeRange.getBegin(), timeRange.getEnd());
        this.order = order;
        this.isToday = isToday(timeRange.getBegin());
    }

    private boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate date = dateTime.toLocalDate();
        return today.equals(date);
    }

}
