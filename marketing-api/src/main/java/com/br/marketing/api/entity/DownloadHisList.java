package com.br.marketing.api.entity;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * #br.loan.strategy.common
 * </p>
 *
 * @author jackcooper
 * @since 2018-05-02
 */
@TableName("download_his_list")
public class DownloadHisList extends Model<DownloadHisList> {

    private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;
	/**
	 * 商户api code
	 */
	@TableField(value="api_code")
	private String apiCode;
	/**
	 * 流水号
	 */
	@TableField(value="swift_number")
	private String swiftNumber;
	/**
	 * 下载来源 1：在线查询全量导出 2：定期监控全量导出 3：预警管理全量导出 4：号码状态核查全量导出
	 */
	private Integer source;
	/**
	 * 下载任务
	 */
	private String task;
	/**
	 * 下载状态 -1:下载失败 0:下载中 1:下载成功
	 */
	@TableField(value="download_status")
	private Integer downloadStatus;
	/**
	 * 下载开始时间
	 */
	@TableField(value="start_time")
	private Date startTime;
	/**
	 * 下载结束时间
	 */
	@TableField(value="end_time")
	private Date endTime;
	/**
	 * 操作员名称
	 */
	private String operator;
	/**
	 * 下载链接 各系统独立实现
	 */
	private String url;
	/**
	 * 状态 0：删除 1：正常
	 */
	private Integer status;
	/**
	 * 操作员id
	 */
	@TableField(value="create_user")
	private Integer createUser;
	/**
	 * 创建时间
	 */
	@TableField(value="create_time")
	private Date createTime;
	/**
	 * 修改人
	 */
	@TableField(value="modify_user")
	private Integer modifyUser;
	/**
	 * 修改时间
	 */
	@TableField(value="modify_time")
	private Date modifyTime;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getApiCode() {
		return apiCode;
	}

	public void setApiCode(String apiCode) {
		this.apiCode = apiCode;
	}

	public String getSwiftNumber() {
		return swiftNumber;
	}

	public void setSwiftNumber(String swiftNumber) {
		this.swiftNumber = swiftNumber;
	}

	public Integer getSource() {
		return source;
	}

	public void setSource(Integer source) {
		this.source = source;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public Integer getDownloadStatus() {
		return downloadStatus;
	}

	public void setDownloadStatus(Integer downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getCreateUser() {
		return createUser;
	}

	public void setCreateUser(Integer createUser) {
		this.createUser = createUser;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(Integer modifyUser) {
		this.modifyUser = modifyUser;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	@Override
	protected Serializable pkVal() {
		return this.id;
	}

}
