package com.springboot.lottery.dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DiceDrawDTO {
	private Integer id;
	private Integer current_term;
	private Integer result;
	private Date end_time;
	private Integer bet_total;
	private Integer win_total;
	
	private List<DiceBetDTO> diceBetDTOs;

	
	public String getEnd_time_str() {
		if(this.getEnd_time() != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return format.format(this.getEnd_time());
		}else {return "";}
		
	}
	
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCurrent_term() {
		return current_term;
	}

	public void setCurrent_term(Integer current_term) {
		this.current_term = current_term;
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

	public List<DiceBetDTO> getDiceBetDTOs() {
		return diceBetDTOs;
	}

	public void setDiceBetDTOs(List<DiceBetDTO> diceBetDTOs) {
		this.diceBetDTOs = diceBetDTOs;
	}
	
	
	
}
