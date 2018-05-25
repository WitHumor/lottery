package com.springboot.lottery.service;

import java.util.List;
import java.util.Map;

import com.springboot.lottery.dto.DiceBetDTO;
import com.springboot.lottery.entity.DiceBet;
import com.springboot.lottery.entity.DiceDraw;
import com.springboot.lottery.entity.Member;

public interface DiceService {

	List<DiceDraw> queryDiceDraw(Map<String, Object> map);
	
	List<DiceBet> queryDiceBet(Map<String, Object> map);
	
	Integer queryDiceBetTotal(Map<String, Object> map);
	
	Map<Integer, Map<String, Object>> queryDiceDrawResult(Map<String, Object> map);
	
	void genereateNewDiceDraw(DiceDraw current, int result, double win);
	
	int addDiceBet(DiceBet diceBet,Member member);
	
	void rewardMember(DiceDraw current, int result, DiceBet db);
	
	List<DiceBetDTO> queryDiceBetDTO(Map<String, Object> map);
}
