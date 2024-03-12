package com.br.marketing.mapper;

import com.br.marketing.entity.Customer;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Bairong on 2019/10/18.
 */
@Repository
public interface CustomerMapper {
    List<Customer> getAllCustomer();
    Customer getCustomerByApiCode(String apiCode);

    /**
     * 查询T-1日至今有上传数据的客户列表
     * @return
     */
    List<Customer> getAllCustomerByResentlySyncInfotikv_();
}
