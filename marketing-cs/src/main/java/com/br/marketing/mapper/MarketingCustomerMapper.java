package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.MarketingCustomerListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MarketingCustomerMapper extends MarketingCustomerMapperBase {

    /**
     * 获取客户cid或apiCode
     * 当参数{@code cid} 不为空时，结果集为apiCode集合
     * 为空时，结果集为cid集合
     *
     * @param cid 客户编号
     * @return {@link List<CustomerSelectVO>}
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/1 15:35
     */
    @AddDataAuth
    List<CustomerSelectVO> getCidOrApiCodeList(@Param("cid") String cid);

    @AddDataAuth
    List<MarketingCustomer> selectByLike(String search);

    /**
     * 获取客户信息列表数据
     * @param cid
     * @param apiCode
     * @return
     */
    List<MarketingCustomerListVO> getCustomerList(@Param("cid")String cid, @Param("apiCode")String apiCode);

    @AddDataAuth
    List<MarketingCustomer> getCidOrName(@Param("search") String search);

    List<MarketingCustomer> selectByExampleAndShard(@Param("example") MarketingCustomerExample example
            , @Param("shardingTotalCount") int shardingTotalCount
            , @Param("shardingItems") List<Integer> shardingItems);


    List<MarketingCustomer> selectCustomerBytestPartion();

    List<MarketingCustomer> selectCustomerBytestPartiontifh_();

    List<MarketingCustomer> selectCustomerBytestPartiontikv_();

    List<MarketingCustomer> getNameByApiCodeList(@Param("apiCode") String apiCode);

    List<String> getApiCodeByZs(@Param("apiCodePrefix") List<String> apiCodePrefix);

    List<Map<String, Object>> checkTableExist(@Param("tableName") String tableName);

    List<MarketingCustomer> getCidByApiCode(@Param("apiCode") String apiCode);

}