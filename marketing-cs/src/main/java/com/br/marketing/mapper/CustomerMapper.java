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
}
