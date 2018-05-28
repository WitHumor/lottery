package com.springboot.lottery.dto;

import java.io.Serializable;
import java.util.Date;

public class FundRecordDTO implements Serializable {

	private static final long serialVersionUID = -6949818367309917483L;
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
	// 优惠金额
	private String discounts;
	// 状态
	private String state;
	// 备注
	private String remark;
	// 结果备注
	private String result_remark;
	// 登录名
	private String name;
	// 登录密码
	private String password;
	// 权限
	private String role;
	// 手机号
	private String phone;
	// IP地址
	private String address;
	// 余额
	private String sum;
	// 邀请码
	private String invitation_code;
	// 真实姓名
	private String real_name;
	// 取款密码
	private String bank_password;
	// 取款开户行
	private String bank_name;
	// 取款银行帐号
	private String bank_number;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getDispose() {
		return dispose;
	}

	public void setDispose(String dispose) {
		this.dispose = dispose;
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

	public String getSum() {
		return sum;
	}

	public void setSum(String sum) {
		this.sum = sum;
	}

	public String getReal_name() {
		return real_name;
	}

	public void setReal_name(String real_name) {
		this.real_name = real_name;
	}
	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
	}

	public String getBank_password() {
		return bank_password;
	}

	public void setBank_password(String bank_password) {
		this.bank_password = bank_password;
	}

	public String getBank_name() {
		return bank_name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
	}

	public String getBank_number() {
		return bank_number;
	}

	public void setBank_number(String bank_number) {
		this.bank_number = bank_number;
	}

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

	public void setNumber(String number) {
		this.number = number;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public String getInvitation_code() {
		return invitation_code;
	}
	
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setInvitation_code(String invitation_code) {
		this.invitation_code = invitation_code;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getResult_remark() {
		return result_remark;
	}

	public void setResult_remark(String result_remark) {
		this.result_remark = result_remark;
	}
}
