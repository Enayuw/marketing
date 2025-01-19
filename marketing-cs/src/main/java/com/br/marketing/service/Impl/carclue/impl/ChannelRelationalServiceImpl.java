package com.br.marketing.service.Impl.carclue.impl;

import com.br.marketing.service.Impl.carclue.ChannelRelationalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @ClassName ChannelRelationalServiceImpl
 * @Description 外采渠道映射关系维护
 * @Author kongbx
 * @Date 2025/1/19 17:15
 */
@Service
@Slf4j
public class ChannelRelationalServiceImpl implements ChannelRelationalService {
    @Override
    public void getProvinceAndCity() {
        //调用省市接口
        //存储省市数据
    }

    @Override
    public void getBrandAndSeries() {

    }

    @Override
    public void relationalMapping() {
        //获取外采初始信息
        //匹配初始信息
        //校验信息准确性
        //最终映射关系存储外采关系表
    }

}
