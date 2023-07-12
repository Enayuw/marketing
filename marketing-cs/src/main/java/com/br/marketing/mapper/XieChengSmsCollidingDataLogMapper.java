package com.br.marketing.mapper;

import com.br.marketing.entity.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface XieChengSmsCollidingDataLogMapper extends XieChengSmsCollidingDataLogMapperBase {


   XieChengSmsCollidingDataLog selectByCodeAndTime(@Param("sha256CodeList") String sha256CodeList, @Param("lastTimeDay") String lastTimeDay);
   List<String> selectBySha256CodeSet(@Param("sha256CodeSet") Set<String> sha256CodeSet, @Param("sendDate")String sendDate);

   /**
    * 批量插入
    *
    * @param list
    */
   void saveBatch(@Param("list") List<XieChengSmsCollidingDataLog> list);

   int insertSelectiveVt(XieChengSmsCollidingDataLogVt record);
   int updateSelectiveVt(XieChengSmsCollidingDataLogVt record);


   /**
    * 批量更新
    * @param list
    */
   void updateBatch(@Param("list") List<XieChengSmsCollidingDataLog> list);
   void updateBatchVt(@Param("list") List<XieChengSmsCollidingDataLog> list);



   /**
    * 根据cell查询数据
    *
    */
   List<XieChengSmsCollidingDataLog> getDataByCells(@Param("cells") List<String> cells);

}