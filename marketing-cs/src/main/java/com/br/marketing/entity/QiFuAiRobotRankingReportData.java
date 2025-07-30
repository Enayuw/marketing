package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName QiFuRobotRankingReportData
 * @Author hang.zhou
 * @Date 2025/7/28
 */
@Data
public class QiFuAiRobotRankingReportData implements Serializable {

    private List<BillReport> billReportList;

}