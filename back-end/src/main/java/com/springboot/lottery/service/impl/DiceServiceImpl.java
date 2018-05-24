package com.springboot.lottery.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.lottery.entity.DiceBet;
import com.springboot.lottery.entity.DiceDraw;
import com.springboot.lottery.mybatis.DiceDao;
import com.springboot.lottery.service.DiceService;

/**
 * 业务逻辑层
 * 
 * @author Administrator
 *
 */
@Service
public class DiceServiceImpl implements DiceService {
	@Autowired
	private DiceDao diceDao;

	
	@Override
	public List<DiceDraw> queryDiceDraw(Map<String, Object> map)
	{
		
		return diceDao.queryDiceDraw(map);
	}

	
	public Map<Integer, Map<String, Object>> queryDiceDrawResult(Map<String, Object> map){
		return diceDao.queryDiceDrawResult(map);
	}
	
	public int addDiceBet(DiceBet diceBet) {
		
		return diceDao.addDiceBet(diceBet);
	}
	@Transactional
	public void genereateNewDiceDraw(DiceDraw current, int result, double win) {
		
		Map<String, Object> u = new HashMap<String,Object>();
		u.put("result", result);
		u.put("end_time", new Date());
		u.put("id", current.getId());
		diceDao.updateDiceDraw(u);
		
		DiceDraw newOne = new DiceDraw();
		newOne.setCurrent_term(current.getCurrent_term() + 1);
		newOne.setPrize_pool(current.getPrize_pool() + win);
		newOne.setStart_time(new Date());
		newOne.setResult(null);
		newOne.setEnd_time(null);
		diceDao.addDiceDraw(newOne);
	}
	
}
