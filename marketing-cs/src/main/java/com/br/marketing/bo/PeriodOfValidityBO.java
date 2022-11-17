package com.br.marketing.bo;

import com.br.common.util.DateUtils;
import com.br.marketing.common.utils.DateHelper;

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

    private PeriodOfValidityBO(Date beginDate, Date enDate) {
        this.beginDate = beginDate;
        this.enDate = enDate;
    }

    private PeriodOfValidityBO() {
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

    public static class Builder {
        private PeriodOfValidityBO periodOfValidityBO;

        public Builder(Date beginDate, Date enDate) {
            this.periodOfValidityBO = new PeriodOfValidityBO(beginDate, enDate);
        }

        public Builder(Date date) {
            this.periodOfValidityBO = new PeriodOfValidityBO(date, date);
        }

        public Builder() {
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
            if (periodOfValidityBO.getEnDate() != null) {
                periodOfValidityBO.beginDateTimeStr = DateUtils.format(
                        periodOfValidityBO.getEnDate(), DateHelper.LINE_DATE_COLON_TIME_FORMAT);
            }
            return this;
        }

        public Builder addDateOtherString(String pattern) {
            if (periodOfValidityBO.getBeginDate() != null) {
                periodOfValidityBO.beginDateTimeStr = DateUtils.format(periodOfValidityBO.getBeginDate(), pattern);
            }
            if (periodOfValidityBO.getEnDate() != null) {
                periodOfValidityBO.beginDateTimeStr = DateUtils.format(periodOfValidityBO.getEnDate(), pattern);
            }
            return this;
        }

        public PeriodOfValidityBO builder() {
            return this.periodOfValidityBO;
        }
    }
}
