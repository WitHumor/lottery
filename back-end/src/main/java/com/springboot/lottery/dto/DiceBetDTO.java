package com.springboot.lottery.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.springboot.lottery.util.DiceBetUtil;

public class DiceBetDTO {
	private Integer id;
	private String mid;
	private Integer term;
	private Integer bet;
	private Double bet_value;
	private String win;
	private Date bet_time;
	private Date draw_time;
	private Integer draw_term;
	private Double win_money;
	private Integer result;
	public Integer getResult() {
		return result;
	}
	public void setResult(Integer result) {
		this.result = result;
	}
	public String getWin() {
		return win;
	}
	public Date getBet_time() {
		return bet_time;
	}
	

	public String getBet_time_str() {
		if(this.getBet_time() != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return format.format(this.getBet_time());
		}else {return "";}
		
	}
	
	public String getBet_name() {
		String rt = "";
		if(this.bet == DiceBetUtil.dan) {
			rt = "单";
		}else if(this.bet == DiceBetUtil.shuang) {
			rt = "双";
		}else if(this.bet == DiceBetUtil.da) {
			rt = "大";
		}else if(this.bet == DiceBetUtil.xiao) {
			rt = "小";
		}else {
			rt = this.bet + "点";
		}
		
			
			return rt;
	}
	public void setBet_time(Date bet_time) {
		this.bet_time = bet_time;
	}
	public Date getDraw_time() {
		return draw_time;
	}
	public void setDraw_time(Date draw_time) {
		this.draw_time = draw_time;
	}
	public Integer getDraw_term() {
		return draw_term;
	}
	public void setDraw_term(Integer draw_term) {
		this.draw_term = draw_term;
	}
	public Double getWin_money() {
		return win_money;
	}
	public void setWin_money(Double win_money) {
		this.win_money = win_money;
	}
	public void setWin(String win) {
		this.win = win;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public Integer getTerm() {
		return term;
	}
	public void setTerm(Integer term) {
		this.term = term;
	}
	public Integer getBet() {
		return bet;
	}
	public void setBet(Integer bet) {
		this.bet = bet;
	}
	public Double getBet_value() {
		return bet_value;
	}
	public void setBet_value(Double bet_value) {
		this.bet_value = bet_value;
	}
	
	
}
