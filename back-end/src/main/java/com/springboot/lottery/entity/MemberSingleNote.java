package com.springboot.lottery.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 注单实体类
 * 
 * @author Administrator
 *
 */
public class MemberSingleNote implements Serializable {

	private static final long serialVersionUID = 7694104586214115708L;
	// 主键id
	private String snid;
	// 会员id
	private String mid;
	// 下注时间
	private Date bet_time;
	// 注单号
	private String number;
	// 类型
	private String type;
	// 内容
	private String league;
	// 主场
	private String team_h;
	// 客场
	private String team_c;
	// 下注
	private String bet;
	// 比分
	private String score;
	// 下注类型
	private String bet_type;
	// 下注间隔 --- 全场 |半场
	private String occasion;
	// 比率类型--独赢|让球|大|小|单|双
	private String ior_type;
	// 比率
	private String ior_ratio;
	// 状态
	private String state;
	// 投注金额
	private String money;
	// 有效投注额
	private String valid_money;
	// 交易金额
	private String deal_money;
	// 输赢
	private String win_lose;

	public String getSnid() {
		return snid;
	}

	public void setSnid(String snid) {
		this.snid = snid;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public Date getBet_time() {
		return bet_time;
	}

	public void setBet_time(Date bet_time) {
		this.bet_time = bet_time;
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

	public String getLeague() {
		return league;
	}

	public void setLeague(String league) {
		this.league = league;
	}

	public String getTeam_h() {
		return team_h;
	}

	public String getOccasion() {
		return occasion;
	}

	public void setOccasion(String occasion) {
		this.occasion = occasion;
	}

	public String getIor_type() {
		return ior_type;
	}

	public void setIor_type(String ior_type) {
		this.ior_type = ior_type;
	}

	public String getIor_ratio() {
		return ior_ratio;
	}

	public void setIor_ratio(String ior_ratio) {
		this.ior_ratio = ior_ratio;
	}

	public void setTeam_h(String team_h) {
		this.team_h = team_h;
	}

	public String getTeam_c() {
		return team_c;
	}

	public void setTeam_c(String team_c) {
		this.team_c = team_c;
	}

	public String getBet_type() {
		return bet_type;
	}

	public void setBet_type(String bet_type) {
		this.bet_type = bet_type;
	}

	public String getBet() {
		return bet;
	}

	public void setBet(String bet) {
		this.bet = bet;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getValid_money() {
		return valid_money;
	}

	public void setValid_money(String valid_money) {
		this.valid_money = valid_money;
	}
	
	public String getDeal_money() {
		return deal_money;
	}

	public void setDeal_money(String deal_money) {
		this.deal_money = deal_money;
	}

	public String getWin_lose() {
		return win_lose;
	}

	public void setWin_lose(String win_lose) {
		this.win_lose = win_lose;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}
}
