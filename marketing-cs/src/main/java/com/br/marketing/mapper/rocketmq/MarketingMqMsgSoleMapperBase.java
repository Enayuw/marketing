//package com.br.marketing.mapper.rocketmq;
//
//import com.br.marketing.entity.rocketmq.MarketingMqMsgSole;
//import com.br.marketing.entity.rocketmq.MarketingMqMsgSoleExample;
//import org.apache.ibatis.annotations.Param;
//
//import java.util.List;
//
//public interface MarketingMqMsgSoleMapperBase {
//    int countByExample(MarketingMqMsgSoleExample example);
//
//    int deleteByExample(MarketingMqMsgSoleExample example);
//
//    int deleteByPrimaryKey(Long id);
//
//    int insert(MarketingMqMsgSole record);
//
//    int insertSelective(MarketingMqMsgSole record);
//
//    List<MarketingMqMsgSole> selectByExample(MarketingMqMsgSoleExample example);
//
//    MarketingMqMsgSole selectByPrimaryKey(Long id);
//
//    int updateByExampleSelective(@Param("record") MarketingMqMsgSole record, @Param("example") MarketingMqMsgSoleExample example);
//
//    int updateByExample(@Param("record") MarketingMqMsgSole record, @Param("example") MarketingMqMsgSoleExample example);
//
//    int updateByPrimaryKeySelective(MarketingMqMsgSole record);
//
//    int updateByPrimaryKey(MarketingMqMsgSole record);
//}