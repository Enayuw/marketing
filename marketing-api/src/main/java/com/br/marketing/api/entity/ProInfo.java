package com.br.marketing.api.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 *
 * @Description : 从配置中心获取到所有的产品信息
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2018/7/31 15:29
 */

@Data
public class ProInfo {

    private Integer id;
    //商户apiCode
    private String apiCode;

    //产品分类
    @JSONField(name = "pro_class")
    private String proClass;

    //产品名称
    @JSONField(name = "pro_name")
    private String proName;

    //产品代码
    @JSONField(name = "pro_code")
    private String proCode;

    //产品版本
    private String version;

    //备注
    private String remark;

    //依赖的数据产品名称
    @JSONField(name = "pro_data")
    private String proData;

    //依赖的数据产品代码
    @JSONField(name = "data_code")
    private String dataCode;

    //依赖的数据产品编码
    @JSONField(name = "data_bm")
    private String dataBm;

    //产品类型(使用业务类型)
    private String type;

    //推广状态
    private String protype;

    //创建时间
    @JSONField(name = "create_time")
    private String createTime;

    //更新时间
    @JSONField(name = "update_time")
    private String updateTime;

    //修改人员
    @JSONField(name = "update_user")
    private String updateUser;

    //电话虫优先级1
    private Integer dhcpriority1;

    //电话虫优先级2
    private Integer dhcpriority2;

    //测试条数
    @JSONField(name = "test_limit")
    private Long testLimit;

    //访问次数
    @JSONField(name = "limit_num")
    private Long limitNum;

    //已使用条数
    @JSONField(name = "use_num")
    private Long useNum;

    //状态(默认 0 ，删除 1)
    private Integer status;
}
