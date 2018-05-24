package com.springboot.lottery.service;

import java.util.List;
import java.util.Map;

import com.springboot.lottery.entity.DiceBet;
import com.springboot.lottery.entity.DiceDraw;

public interface DiceService {

	List<DiceDraw> queryDiceDraw(Map<String, Object> map);
	
	Map<Integer, Map<String, Object>> queryDiceDrawResult(Map<String, Object> map);
	
	void genereateNewDiceDraw(DiceDraw current, int result, double win);
	
	int addDiceBet(DiceBet diceBet);
}
