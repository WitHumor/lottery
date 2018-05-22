package com.springboot.lottery.entity;

public class DiceBet {
	private int id;
	private int mid;
	private int term;
	private int bet;
	private double bet_value;
	private String win;
	public String getWin() {
		return win;
	}
	public void setWin(String win) {
		this.win = win;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getMid() {
		return mid;
	}
	public void setMid(int mid) {
		this.mid = mid;
	}
	public int getTerm() {
		return term;
	}
	public void setTerm(int term) {
		this.term = term;
	}
	public int getBet() {
		return bet;
	}
	public void setBet(int bet) {
		this.bet = bet;
	}
	public double getBet_value() {
		return bet_value;
	}
	public void setBet_value(double bet_value) {
		this.bet_value = bet_value;
	}
	
	
}
