package com.springboot.lottery.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.springboot.lottery.util.DiceBetUtil;

public class DiceBet {
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
	public String getWin() {
		return win;
	}
	public Date getBet_time() {
		return bet_time;
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
