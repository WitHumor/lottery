package com.springboot.lottery.entity;

import java.io.Serializable;

/**
 * 会员实体类
 * 
 * @author Administrator
 *
 */
public class Member implements Serializable {

	private static final long serialVersionUID = 6885765727486854665L;

	// 主键id
	private String mid;
	// 登录名
	private String name;
	// 登录密码
	private String password;
	// 手机号
	private String phone;
	// IP地址
	private String address;
	// 余额
	private String sum;
	// 真实姓名
	private String real_name;
	// 取款密码
	private String bank_password;
	// 取款开户行
	private String bank_name;
	// 取款银行帐号
	private String bank_number;
	// token
	private String token;

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

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

	public String getBank_password() {
		return bank_password;
	}

	public void setBank_password(String bank_password) {
		this.bank_password = bank_password;
	}

	public String getBank_name() {
		return bank_name;
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

	public Member() {
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Member(String mid, String name, String password, String phone, String address, String sum, String real_name,
			String bank_password, String bank_name, String bank_number) {
		this.mid = mid;
		this.name = name;
		this.password = password;
		this.phone = phone;
		this.address = address;
		this.sum = sum;
		this.real_name = real_name;
		this.bank_password = bank_password;
		this.bank_name = bank_name;
		this.bank_number = bank_number;
	}

	@Override
	public String toString() {
		return "Member [mid=" + mid + ", name=" + name + ", password=" + password + ", phone=" + phone + ", address="
				+ address + ", sum=" + sum + ", real_name=" + real_name + ", bank_password=" + bank_password
				+ ", bank_name=" + bank_name + ", bank_number=" + bank_number + "]";
	}

}
