package com.br.marketing.task.service;

import com.br.marketing.entity.Customer;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

/**
 * Created by Bairong on 2019/8/20.
 */
public interface LoanWarningService {
    /**
     * 流失预警数据处理接口
     * 1.贷前进件变动模式（通用模式）
     * 2.ppd客户定制模式，t+1重点字段，t+5全量字段
     * 3.数据变动模式，每天查询全量，与之前的的结果对比，有变动的三要素返回结果。360金融
     * 4，海南农信定制，每隔七天跑一次全部存量数据，结果生成到同一个目录下（因为每天只能返回一个结果文件）
     * 5.360营销定制，按数据产品（营销分）区分结果文件的目录（因为每个评分需要一个结果文件）
     *
     * @param customer
     */
     void process(Customer customer,JobExecutionMultipleShardingContext context);

}
