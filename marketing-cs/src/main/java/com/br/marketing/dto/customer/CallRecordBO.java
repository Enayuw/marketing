package com.br.marketing.dto.customer;

import lombok.Data;

@Data
public class CallRecordBO extends CallRecordDTO{

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 0:正常队列 1:延迟队列
     */
    private Integer dataSource;

    @Override
    public String toString() {
        return "CallRecordBO{" +
                "id=" + id +
                ", dataSource=" + dataSource +
                '}';
    }
}
