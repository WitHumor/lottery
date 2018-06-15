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
	// 比赛id
	private String gid;
	// 下注时间
	private Date bet_time;
	// 比赛开始时间
	private Date start_time;
	// 比赛结算时间
	private Date end_time;
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
	// 下注比分
	private String bet_score;
	// 赛果比分
	private String score;
	// 下注类型
	private String bet_type;
	// 下注间隔 --- 全场 |半场
	private String occasion;
	// 比率类型--独赢|让球|大|小|单|双
	private String ior_type;
	// 比率
	private String ior_ratio;
	// 盘口字段
	private String tape_field;
	// 赔率
	private String ratio;
	// 让球方
	private String strong;
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

	public String getStrong() {
		return strong;
	}

	public void setStrong(String strong) {
		this.strong = strong;
	}

	public void setWin_lose(String win_lose) {
		this.win_lose = win_lose;
	}

	public String getBet_score() {
		return bet_score;
	}

	public void setBet_score(String bet_score) {
		this.bet_score = bet_score;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getRatio() {
		return ratio;
	}

	public void setRatio(String ratio) {
		this.ratio = ratio;
	}

	public Date getStart_time() {
		return start_time;
	}

	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}

	public Date getEnd_time() {
		return end_time;
	}

	public void setEnd_time(Date end_time) {
		this.end_time = end_time;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getTape_field() {
		return tape_field;
	}

	public void setTape_field(String tape_field) {
		this.tape_field = tape_field;
	}
}
