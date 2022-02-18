package com.br.marketing.dos;

import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 有效期
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/12 17:57
 */
public class PeriodOfValidityDO implements Serializable {
    private Period startTime;
    private Period endTime;

    private static class Period implements Serializable {
        private String sign;
        private String time;

        private Period() {
        }

        private Period(SignENum signENum, String time) {
            this.sign = signENum.value;
            this.time = time;
        }

        private Period(String time) {
            this.time = time;
        }

        public String getSign() {
            return sign;
        }

        public String getTime() {
            return time;
        }

        @Override
        public String toString() {
            return "PeriodOfValidity{" +
                    "sign='" + sign + '\'' +
                    ", time='" + time + '\'' +
                    '}';
        }
    }

    private PeriodOfValidityDO() {
    }

    public PeriodOfValidityDO(String startTime, String endTime) {
        this.startTime = new Period(startTime);
        this.endTime = new Period(endTime);
        this.closInterval();
    }

    public Period getStartTime() {
        return startTime;
    }

    /**
     * 设置开始时间
     */
    public PeriodOfValidityDO setStartTime(SignENum signENum, String startTime) {
        this.startTime = new Period(signENum, startTime);
        return this;
    }

    public Period getEndTime() {
        return endTime;
    }

    /**
     * 设置结束时间
     */
    public PeriodOfValidityDO setEndTime(SignENum signENum, String endTime) {
        this.endTime = new Period(signENum, endTime);
        return this;
    }

    /**
     * 开区间
     */
    public void openInterval() {
        this.startTime.sign = SignENum.GREATER_THAN.value;
        this.endTime.sign = SignENum.LESS_THAN.value;
    }

    /**
     * 闭区间
     */
    public void closInterval() {
        this.startTime.sign = SignENum.GREATER_THAN_OR_EQUAL_TO.value;
        this.endTime.sign = SignENum.LESS_THAN_OR_EQUAL_TO.value;
    }

    /**
     * 左开右闭区间
     */
    public void openClosInterval() {
        this.startTime.sign = SignENum.GREATER_THAN.value;
        this.endTime.sign = SignENum.LESS_THAN_OR_EQUAL_TO.value;
    }

    /**
     * 右开左闭区间
     */
    public void closOpenInterval() {
        this.startTime.sign = SignENum.GREATER_THAN_OR_EQUAL_TO.value;
        this.endTime.sign = SignENum.LESS_THAN.value;
    }


    /**
     * 闭区间 当前时间前15天间隔
     * eg: 2022-02-02 00:00:00 到 2022-02-16 23:59:59
     */
    public static PeriodOfValidityDO closInterval15Day() {
        return closInterval15Day(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
    }

    /**
     * 闭区间 @{code date}前15天间隔
     * eg: 2022-02-02 00:00:00 到 2022-02-16 23:59:59
     */
    public static PeriodOfValidityDO closInterval15Day(Date date) {
        PeriodOfValidityDO periodOfValidityDO = closIntervalDay(-14, date, "yyyy-MM-dd");
        Period startTime = periodOfValidityDO.getStartTime();
        Period endTime = periodOfValidityDO.getEndTime();
        startTime.time = startTime.getTime().concat(" 00:00:00");
        endTime.time = endTime.getTime().concat(" 23:59:59");
        return periodOfValidityDO;
    }


    /**
     * 闭区间
     *
     * @param day     正数 当前时间之后， 负数当前时间之前
     * @param date    时间
     * @param pattern 时间格式
     */
    public static PeriodOfValidityDO closIntervalDay(int day, Date date, String pattern) {
        final PeriodOfValidityDO periodOfValidityDO = buildPV(day, date, pattern);
        periodOfValidityDO.closInterval();
        return periodOfValidityDO;
    }

    /**
     * 开区间
     *
     * @param day     正数 当前时间之后， 负数当前时间之前
     * @param date    时间
     * @param pattern 时间格式
     */
    public static PeriodOfValidityDO openIntervalDay(int day, Date date, String pattern) {
        final PeriodOfValidityDO periodOfValidityDO = buildPV(day, date, pattern);
        periodOfValidityDO.openInterval();
        return periodOfValidityDO;
    }

    private static PeriodOfValidityDO buildPV(int day, Date date, String pattern) {
        Assert.notNull(date, "date is not null");
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String entTime;
        String startTime = localDateTime.format(DateTimeFormatter.ofPattern(pattern));
        if (day > -1) {
            entTime = getTime(day, localDateTime, pattern);
        } else {
            entTime = startTime;
            startTime = getTime(day, localDateTime, pattern);
        }
        return new PeriodOfValidityDO(startTime, entTime);
    }

    private static String getTime(int day, LocalDateTime localDateTimeEnd, String pattern) {
        final LocalDateTime localDateTimeStart = localDateTimeEnd.plusDays(day);
        return localDateTimeStart.format(DateTimeFormatter.ofPattern(pattern));
    }


    @Override
    public String toString() {
        return "PeriodOfValidityDO{" +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    public enum SignENum {
        //大于号
        GREATER_THAN(">"),
        //大于等于号
        GREATER_THAN_OR_EQUAL_TO(">="),
        //小于号
        LESS_THAN("<"),
        //小于等于号
        LESS_THAN_OR_EQUAL_TO("<=");
        private String value;

        SignENum() {
        }

        SignENum(String value) {
            this.value = value;
        }
    }
}
