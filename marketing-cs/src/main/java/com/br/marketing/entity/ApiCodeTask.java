package com.br.marketing.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author: Bairong
 * @Time: 2020/11/21 16:01
 * @Company：百融
 * @Description: 功能描述
 */
@Data
public class ApiCodeTask {
    String apiCode;
    List<MarketingTask> marketingTaskList;

    @Override
    public String toString() {
        return "ApiCodeTask{" +
                "apiCode='" + apiCode + '\'' +
                ", loanTaskList=" + marketingTaskList +
                '}';
    }
}
