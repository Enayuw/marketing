package com.br.marketing.mapper;

import com.br.marketing.entity.Customer;
import com.br.marketing.entity.LoanFile;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by Bairong on 2019/10/18.
 */
@Repository
public interface CustomerMapper {
    List<Customer> getAllCustomer();
    Customer getCustomerByApiCode(String apiCode);
}
