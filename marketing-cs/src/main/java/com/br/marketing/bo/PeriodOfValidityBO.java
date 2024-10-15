package com.br.marketing.bo;

import com.br.common.util.DateUtils;
import com.br.marketing.common.utils.DateHelper;

import java.util.Calendar;
import java.util.Date;

/**
 * 有效期
 *
 * @author Guo Zeqiang
 * @dateTime 2022/11/17 11:17
 */
public class PeriodOfValidityBO {
    /**
     * 2022/11/17 11:19
     * 开始日期
     */
    private Date beginDate;
    /**
     * 2022/11/17 11:19
     * 结尾日期
     */
    private Date enDate;

    /**
     * 2022/11/17 11:19
     * 开始日期
     * 格式：yyyy-MM-dd hh:mm:dd
     */
    private String beginDateTimeStr;
    /**
     * 2022/11/17 11:19
     * 结尾日期
     * 格式：yyyy-MM-dd hh:mm:dd
     */
    private String enDateTimeStr;

    /**
     * 2022/11/17 11:19
     * 开始日期
     * 格式：yyyy-MM-dd
     */
    private String beginDateStr;
    /**
     * 2022/11/17 11:19
     * 结尾日期
     * 格式：yyyy-MM-dd
     */
    private String enDateStr;

    /**
     * 2022/11/17 11:19
     * 开始日期
     */
    private String beginDateOtherStr;

    /**
     * 2022/11/17 11:19
     * 结尾日期
     */
    private String enDateOtherStr;

    /**
     * 2023/03/17 11:19
     * 一天的开始时间
     * 格式：yyyy-MM-dd hh:mm:dd
     * eg：2023/03/17 00:00:00
     */
    private String startOfDayTimeStr;

    /**
     * 2023/03/17 11:19
     * 一天的结束时间
     * 格式：yyyy-MM-dd hh:mm:dd
     * eg：2023/03/17 23:59:59
     */
    private String endOfDayTimeStr;


    PeriodOfValidityBO(Date beginDate, Date enDate) {
        this.beginDate = beginDate;
        this.enDate = enDate;
    }

    protected PeriodOfValidityBO() {
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public void setEnDate(Date enDate) {
        this.enDate = enDate;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public Date getEnDate() {
        return enDate;
    }

    public String getBeginDateTimeStr() {
        return beginDateTimeStr;
    }

    public String getEnDateTimeStr() {
        return enDateTimeStr;
    }

    public String getBeginDateStr() {
        return beginDateStr;
    }

    public String getEnDateStr() {
        return enDateStr;
    }

    public String getBeginDateOtherStr() {
        return beginDateOtherStr;
    }

    public String getEnDateOtherStr() {
        return enDateOtherStr;
    }

    public String getStartOfDayTimeStr() {
        return startOfDayTimeStr;
    }

    public String getEndOfDayTimeStr() {
        return endOfDayTimeStr;
    }

    @Override
    public String toString() {
        return "PeriodOfValidityBO{" +
                "beginDate=" + beginDate +
                ", enDate=" + enDate +
                ", beginDateTimeStr='" + beginDateTimeStr + '\'' +
                ", enDateTimeStr='" + enDateTimeStr + '\'' +
                ", beginDateStr='" + beginDateStr + '\'' +
                ", enDateStr='" + enDateStr + '\'' +
                ", beginDateOtherStr='" + beginDateOtherStr + '\'' +
                ", enDateOtherStr='" + enDateOtherStr + '\'' +
                ", startOfDayTimeStr='" + startOfDayTimeStr + '\'' +
                ", endOfDayTimeStr='" + endOfDayTimeStr + '\'' +
                '}';
    }

    public static PeriodOfValidityBO.Builder custom(Date beginDate, Date enDate) {
        return new PeriodOfValidityBO.Builder(beginDate, enDate);
    }

    public static PeriodOfValidityBO.Builder custom(Date date) {
        return new PeriodOfValidityBO.Builder(date);
    }

    public static class Builder {
        private final PeriodOfValidityBO periodOfValidityBO;

        Builder(Date beginDate, Date enDate) {
            this.periodOfValidityBO = new PeriodOfValidityBO(beginDate, enDate);
        }

        Builder(Date date) {
            this.periodOfValidityBO = new PeriodOfValidityBO(date, date);
        }

        /**
         * 2024-08-28 22:56
         * 添加已格式化的日期字符串，格式：yyyy-MM-dd
         */
        public Builder addBeginDateStrAndEnDateStr(String beginDateStr, String enDateStr) {
            periodOfValidityBO.beginDateStr = beginDateStr;
            periodOfValidityBO.enDateStr = enDateStr;
            return this;
        }

        public Builder addDateString() {
            if (periodOfValidityBO.getBeginDate() != null) {
                periodOfValidityBO.beginDateStr = DateUtils.format(periodOfValidityBO.getBeginDate());
            }
            if (periodOfValidityBO.getEnDate() != null) {
                periodOfValidityBO.enDateStr = DateUtils.format(periodOfValidityBO.getEnDate());
            }
            return this;
        }

        public Builder addDateTimeString() {
            if (periodOfValidityBO.getBeginDate() != null) {
                periodOfValidityBO.beginDateTimeStr = DateUtils.format(
                        periodOfValidityBO.getBeginDate(), DateHelper.LINE_DATE_COLON_TIME_FORMAT);
            }
            Date enDate1 = periodOfValidityBO.getEnDate();
            if (enDate1 != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(enDate1);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                Date updatedDate = calendar.getTime();
                periodOfValidityBO.enDateTimeStr = DateUtils.format(
                        updatedDate, DateHelper.LINE_DATE_COLON_TIME_FORMAT);
            }
            return this;
        }

        public Builder addDateOtherString(String pattern) {
            if (periodOfValidityBO.getBeginDate() != null) {
                periodOfValidityBO.beginDateOtherStr = DateUtils.format(periodOfValidityBO.getBeginDate(), pattern);
            }
            if (periodOfValidityBO.getEnDate() != null) {
                periodOfValidityBO.enDateOtherStr = DateUtils.format(periodOfValidityBO.getEnDate(), pattern);
            }
            return this;
        }

        /**
         * 2023-03-23 12:53
         * 添加
         * 开始时间的零点时间(00:00:00)与结束时间的末尾时间（23:59:59）
         */
        public Builder addOfDayTimeStrString() {
            if (periodOfValidityBO.getBeginDate() != null) {
                periodOfValidityBO.startOfDayTimeStr = DateUtils.format(periodOfValidityBO.getBeginDate()).concat(" 00:00:00");
            }
            if (periodOfValidityBO.getEnDate() != null) {
                periodOfValidityBO.endOfDayTimeStr = DateUtils.format(periodOfValidityBO.getEnDate()).concat(" 23:59:59");
            }
            return this;
        }

        public PeriodOfValidityBO builder() {
            return this.periodOfValidityBO;
        }
    }
}
