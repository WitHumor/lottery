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
	// 交易时间
	private Date time;
	// 编号
	private String number;
	// 类型
	private String type;
	// 金额
	private String money;
	// 优惠金额
	private String discounts;
	// 状态
	private String state;
	// 备注
	private String remark;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

}
