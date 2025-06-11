package com.br.marketing.datarelayservice.client;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName QiFuActuation
 * @Author kongbx
 * @Date 2025/6/9 14:52
 */
@Data
public class QiFuActuationDTO implements Serializable {

    private String issueMonth;
    private String issueDate;
    private String userType;
    private String supplier;
    private String validDate;

    private Integer creditUserCount;
    private Integer appLoginUserCount;
    private Integer startUserCount;
    private Integer userLoanCount;

    private BigDecimal appLoginRate;
    private BigDecimal userStartRate;
    private BigDecimal userLoanRate;

}
