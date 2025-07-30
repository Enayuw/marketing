package com.br.marketing.datarelayservice.client;

import com.br.marketing.entity.BillReport;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName QiFuAiRobotRankingReportBizDataDTO
 * @Author hang.zhou
 * @Date 2025/7/28
 */
@Data
public class QiFuAiRobotRankingReportBizDataDTO implements Serializable {

    private List<BillReport> billReportList;

}