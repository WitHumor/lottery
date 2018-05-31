package com.springboot.lottery.entity;

import java.util.Date;

public class DiceDraw {
	private int id;
	private int current_term;
	
	private double prize_pool;
	
	private Date start_time;
	
	private Integer result;
	
	private Date end_time;
	
	private Integer bet_total;
	
	private Integer win_total;
	
	

	public Integer getBet_total() {
		return bet_total;
	}

	public void setBet_total(Integer bet_total) {
		this.bet_total = bet_total;
	}

	public Integer getWin_total() {
		return win_total;
	}

	public void setWin_total(Integer win_total) {
		this.win_total = win_total;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCurrent_term() {
		return current_term;
	}

	public void setCurrent_term(int current_term) {
		this.current_term = current_term;
	}

	public double getPrize_pool() {
		return prize_pool;
	}

	public void setPrize_pool(double prize_pool) {
		this.prize_pool = prize_pool;
	}

	public Date getStart_time() {
		return start_time;
	}

	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

	public Date getEnd_time() {
		return end_time;
	}

	public void setEnd_time(Date end_time) {
		this.end_time = end_time;
	}
	
	
}
