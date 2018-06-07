package com.springboot.lottery.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 资金流水实体类
 * 
 * @author Administrator
 *
 */
public class MemberFundRecord implements Serializable {

	private static final long serialVersionUID = -4230386213092235937L;

	// 主键id
	private String frid;
	// 会员ID
	private String mid;
	// 处理人
	private String dispose;
	// 交易时间
	private Date time;
	// 编号
	private String number;
	// 电话
	private String phone_code;
	// 钱包地址
	private String money_address;
	// 货币
	private String currency;
	// 货币数量
	private String currency_count;
	// 金额
	private String money;
	// 记录
	private String record;
	// 取款类型
	private String withdrawn_type;
	// 优惠金额
	private String discounts;
	// 状态
	private String state;
	// 备注
	private String remark;
	// 结果备注
	private String result_remark;

	public String getFrid() {
		return frid;
	}

	public void setFrid(String frid) {
		this.frid = frid;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getNumber() {
		return number;
	}

	public String getWithdrawn_type() {
		return withdrawn_type;
	}

	public void setWithdrawn_type(String withdrawn_type) {
		this.withdrawn_type = withdrawn_type;
	}

	public void setNumber(String number) {
		this.number = number;
	}
	
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCurrency_count() {
		return currency_count;
	}

	public void setCurrency_count(String currency_count) {
		this.currency_count = currency_count;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getDiscounts() {
		return discounts;
	}

	public void setDiscounts(String discounts) {
		this.discounts = discounts;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
	}

	public String getPhone_code() {
		return phone_code;
	}

	public void setPhone_code(String phone_code) {
		this.phone_code = phone_code;
	}

	public String getMoney_address() {
		return money_address;
	}

	public void setMoney_address(String money_address) {
		this.money_address = money_address;
	}

	public String getResult_remark() {
		return result_remark;
	}

	public String getDispose() {
		return dispose;
	}

	public void setDispose(String dispose) {
		this.dispose = dispose;
	}

	public void setResult_remark(String result_remark) {
		this.result_remark = result_remark;
	}
}
