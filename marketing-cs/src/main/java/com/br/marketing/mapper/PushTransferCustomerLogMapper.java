package com.br.marketing.mapper;

import com.br.marketing.entity.PushTransferCustomerLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/10/14 16:23
 */
public interface PushTransferCustomerLogMapper extends PushTransferCustomerLogMapperBase {


    @Insert("<script> INSERT INTO b_marketing_push_transfer_customer_log "
            + "(transfer_info_id,api_code,request_id,t_cid,request_body,response_body,service_code,message,swift_number,row_size,create_time,transfer_info_time,push_status,transfer_status,http_status,http_reason_phrase) "
            + "VALUES "
            + "<foreach collection = 'logs' item='record' separator=',' > "
            + " (" +
            "#{record.transferInfoId},#{record.apiCode},#{record.requestId},#{record.tCid},#{record.requestBody},#{record.responseBody},#{record.serviceCode},#{record.message},#{record.swiftNumber},#{record.rowSize},#{record.createTime},#{record.transferInfoTime},#{record.pushStatus},#{record.transferStatus},#{record.httpStatus},#{record.httpReasonPhrase}" +
            ") "
            + "</foreach> "
            + "</script>")
    boolean bathInsert(@Param("logs") List<PushTransferCustomerLog> logs);

    @Select("SELECT transfer_info_id,\n" +
            "       api_code,\n" +
            "       request_id,\n" +
            "       t_cid,\n" +
            "       transfer_info_time,\n" +
            "       compensate_times,\n" +
            "       push_status,\n" +
            "       transfer_status,\n" +
            "       service_code,\n" +
            "       request_body\n" +
            "FROM b_marketing_push_transfer_customer_log\n" +
            "WHERE mod(id, #{shardingTotalCount}) in \n" +
            "<foreach collection = 'shardingItems' item='sharding'  open='(' close=')' separator=','> "
            + "#{sharding}"
            + "</foreach> "
            + " and push_status = 1")
    List<PushTransferCustomerLog> findListByStatusIs1(@Param("shardingTotalCount") int shardingTotalCount, @Param("shardingItems") List<Integer> shardingItems);

    /**
     * 根据ApiCode createTime 统计当天数据量
     *
     * @param apiCode          客户编码
     * @param transferInfoTime 客户调用信息创建时间
     * @return int
     * @author Guo Zeqiang
     * @dateTime 2021/10/13 14:05
     */
    @Select("SELECT COUNT(*) from (SELECT transfer_info_id FROM b_marketing_push_transfer_customer_log where api_code=#{apiCode} and date_format(transfer_info_time,'%Y-%m-%d') = str_to_date(#{transferInfoTime},'%Y-%m-%d') group by transfer_info_id) as tab_count")
    Integer countByApiCodeAndTransferInfoTime(@Param("apiCode") String apiCode, @Param("transferInfoTime") Date transferInfoTime);


    /**
     * 根据ApiCode createTime transferStatus 统计当天数据量
     *
     * @param apiCode          客户编码
     * @param transferInfoTime 客户调用信息创建时间
     * @return int
     * @author Guo Zeqiang
     * @dateTime 2021/10/13 14:05
     */
    @Select("SELECT COUNT(*) from (SELECT transfer_info_id FROM b_marketing_push_transfer_customer_log where api_code=#{apiCode} and date_format(transfer_info_time,'%Y-%m-%d') = str_to_date(#{transferInfoTime},'%Y-%m-%d') and transfer_status=#{transferStatus} group by transfer_info_id) as tab_count")
    Integer countByApiCodeAndTransferInfoTimeAndStatus(@Param("apiCode") String apiCode, @Param("transferInfoTime") Date transferInfoTime, @Param("transferStatus") int transferStatus);

    /**
     * 根据ApiCode createTime 未上传成功的数据量
     *
     * @param apiCode          客户编码
     * @param transferInfoTime 客户调用信息创建时间
     * @return int
     * @author Guo Zeqiang
     * @dateTime 2021/10/13 14:05
     */
    @Select("SELECT COUNT(*) FROM b_marketing_push_transfer_customer_log where api_code=#{apiCode} and date_format(transfer_info_time,'%Y-%m-%d') = str_to_date(#{transferInfoTime},'%Y-%m-%d') AND push_status in(1,3,4)")
    Integer countByApiCodeAndTransferInfoTimeAndPushStatus(@Param("apiCode") String apiCode, @Param("transferInfoTime") Date transferInfoTime);


    /**
     * 根据ApiCode createTime 检查是否有上传成功的记录
     *
     * @param apiCode          客户编码
     * @param transferInfoTime 客户调用信息创建时间
     * @return List<PushTransferCustomerLog>
     * @author Guo Zeqiang
     * @dateTime 2021/10/13 14:05
     */
    @Select("SELECT id FROM b_marketing_push_transfer_customer_log where api_code=#{apiCode} and date_format(transfer_info_time,'%Y-%m-%d') = str_to_date(#{transferInfoTime},'%Y-%m-%d') AND transfer_status=2")
    List<PushTransferCustomerLog> findListByCodeAndInfoTimeAndTransferStatus(@Param("apiCode") String apiCode, @Param("transferInfoTime") Date transferInfoTime);
}
