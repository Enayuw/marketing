package com.br.marketing.api.bean;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author jackcooper
 * @since 2018-05-04
 */
@Data
public class DtbHisList extends Model<DtbHisList> {

    private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	private Long id;
	/**
	 * 商户编号
	 */
	@TableField(value="api_code")
	private String apiCode;
	/**
	 * 
	 */
	@TableField(value="swift_number")
	private String swiftNumber;
	/**
	 * 策略编号
	 */
	@TableField(value="strategy_id")
	private String strategyId;
	/**
	 * 策略版本
	 */
	private String version;
	/**
	 * 状态返回码
	 */
	private String code;
	/**
	 * 创建时间
	 */
	@TableField(value="create_time")
	private Date createTime;
	/**
	 * 请求类型 api api_batch api批量 ；web web_batch  web批量
	 */
	@TableField(value="request_type")
	private String requestType;
	/**
	 * 策略状态 1 已完成计算 2 正在计算中 3 计算失败
	 */
	private Integer status;
	/**
	 * 身份证号
	 */
	@TableField(value="id_card")
	private String idCard;
	/**
	 * 姓名
	 */
	private String name;
	/**
	 * 手机号
	 */
	private String cell;
	/**
	 * 请求参数
	 */
	@TableField(value="request_param")
	private String requestParam;
	/**
	 * 策略返回结果
	 */
	@TableField(value="response_json")
	private String responseJson;

	@Override
	protected Serializable pkVal() {
		return this.id;
	}

}
