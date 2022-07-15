package com.br.marketing.mapper;

import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.VariableDicListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VariableDicMapper extends VariableDicMapperBase {

    /**
     * 客户配置变量值列表数据
     * @param cid
     * @param apiCode
     * @return
     */
    @AddDataAuth
    List<VariableDicListVO> getVariableDicList(@Param("cid")String cid, @Param("apiCode")String apiCode);
}