package com.br.marketing.mapper;

import com.br.marketing.entity.BLoanResult;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Created by Bairong on 2019/10/18.
 */
@Repository
public interface LoanResultMapper {

    List<String> queryApiCodes();

}
