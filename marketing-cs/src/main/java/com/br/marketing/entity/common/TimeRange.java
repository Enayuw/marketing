package com.br.marketing.entity.common;

import java.time.LocalDateTime;

public class TimeRange {
    private final LocalDateTime begin;
    private final LocalDateTime end;

    public TimeRange(LocalDateTime begin, LocalDateTime end) {
        this.begin = begin;
        this.end = end;
    }

    public LocalDateTime getBegin() {
        return begin;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "[" + begin + " - " + end + "]";
    }
}
