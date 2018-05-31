package com.springboot.lottery.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.lottery.dto.DiceBetDTO;
import com.springboot.lottery.dto.DiceDrawBetDTO;
import com.springboot.lottery.entity.DiceBet;
import com.springboot.lottery.entity.DiceDraw;
import com.springboot.lottery.mybatis.DiceDao;
import com.springboot.lottery.service.DiceService;
import com.springboot.lottery.service.MemberService;
import com.springboot.lottery.util.DiceBetUtil;

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
	
	@Autowired
	private MemberService memberService;
	
	@Override
	public List<DiceDraw> queryDiceDraw(Map<String, Object> map)
	{
		
		return diceDao.queryDiceDraw(map);
	}

	
	public Map<Integer, Map<String, Object>> queryDiceDrawResult(Map<String, Object> map){
		return diceDao.queryDiceDrawResult(map);
	}
	
	@Transactional
	public int addDiceBet(DiceBet diceBet,String mid) {
		// 修改账户余额
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("mid", mid);
		map.put("money", String.valueOf((0-diceBet.getBet_value())));
		
		int updateSum = memberService.updateSum(map);
		if(updateSum > 0) {
			diceDao.addDiceBet(diceBet);
		}
		return updateSum;
	}
	@Transactional
	public void genereateNewDiceDraw(DiceDraw current, int result, double win) {
		Date now = new Date();
		Map<String, Object> u = new HashMap<String,Object>();
		u.put("result", result);
		u.put("end_time", now);
		u.put("id", current.getId());
		
		Random rand = new Random();
		Integer bet_total = rand.nextInt(1000) + 100;
		Integer win_total = rand.nextInt(10000) + 2000;	
		u.put("bet_total", bet_total);
		u.put("win_total", win_total);
		
		diceDao.updateDiceDraw(u);
		
		/*
		SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
		int nowI = Integer.parseInt(format.format(now));
		int currentI = Integer.parseInt(format.format(current.getStart_time()));
		*/
		DiceDraw newOne = new DiceDraw();
		newOne.setPrize_pool(current.getPrize_pool() + win);
		newOne.setStart_time(now);
		newOne.setResult(null);
		newOne.setEnd_time(null);
		newOne.setCurrent_term(current.getCurrent_term() + 1);
		/*
		if(nowI == currentI ) {
			newOne.setCurrent_term(current.getCurrent_term() + 1);
		}else {
			String term = nowI + "0001";
			newOne.setCurrent_term(Integer.parseInt(term));
		}
		*/
		diceDao.addDiceDraw(newOne);
	}
	
	
	@Override
	public List<DiceBet> queryDiceBet(Map<String, Object> map)
	{
		
		return diceDao.queryDiceBet(map);
	}

	public Integer queryDiceBetTotal(Map<String, Object> map) {
		return diceDao.queryDiceBetTotal(map);
	}
	
	public List<DiceBetDTO> queryDiceBetDTO(Map<String, Object> map){
		
		return diceDao.queryDiceBetDTO(map);
	}
	
	public List<DiceDrawBetDTO> queryDiceDrawBetDTO(Map<String, Object> map){
		
		return diceDao.queryDiceDrawBetDTO(map);
	}
	
	public int  queryDiceDrawBetTotal(Map<String, Object> map) {
		
		return diceDao.queryDiceDrawBetTotal(map);
	}
	
	
	@Transactional
	public void rewardMember(DiceDraw current, int result, DiceBet db) {
			double winRate = 0;
			if(result == 1){
				if(db.getBet() == DiceBetUtil.one) {
					winRate = DiceBetUtil.number;
				}else if(db.getBet() == DiceBetUtil.dan) {
					winRate = DiceBetUtil.danshuang;
				}else if(db.getBet() == DiceBetUtil.xiao) {
					winRate = DiceBetUtil.daxiao;
				}
				
			}else if(result == 2){
				if(db.getBet() == DiceBetUtil.two) {
					winRate = DiceBetUtil.number;
				}else if(db.getBet() == DiceBetUtil.shuang) {
					winRate = DiceBetUtil.danshuang;
				}else if(db.getBet() == DiceBetUtil.xiao) {
					winRate = DiceBetUtil.daxiao;
				}
			}else if(result == 3){
				if(db.getBet() == DiceBetUtil.three) {
					winRate = DiceBetUtil.number;
				}else if(db.getBet() == DiceBetUtil.dan) {
					winRate = DiceBetUtil.danshuang;
				}else if(db.getBet() == DiceBetUtil.xiao) {
					winRate = DiceBetUtil.daxiao;
				}
			}else if(result == 4){
				if(db.getBet() == DiceBetUtil.four) {
					winRate = DiceBetUtil.number;
				}else if(db.getBet() == DiceBetUtil.shuang) {
					winRate = DiceBetUtil.danshuang;
				}else if(db.getBet() == DiceBetUtil.da) {
					winRate = DiceBetUtil.daxiao;
				}
			}else if(result == 5){
				if(db.getBet() == DiceBetUtil.five) {
					winRate = DiceBetUtil.number;
				}else if(db.getBet() == DiceBetUtil.dan) {
					winRate = DiceBetUtil.danshuang;
				}else if(db.getBet() == DiceBetUtil.da) {
					winRate = DiceBetUtil.daxiao;
				}
			}else if(result == 6){
				if(db.getBet() == DiceBetUtil.six) {
					winRate = DiceBetUtil.number;
				}else if(db.getBet() == DiceBetUtil.shuang) {
					winRate = DiceBetUtil.danshuang;
				}else if(db.getBet() == DiceBetUtil.da) {
					winRate = DiceBetUtil.daxiao;
				}				
			}
			double win = 0;
			if(winRate > 0) {
				win = db.getBet_value() * winRate;
				db.setWin("W");
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("mid", db.getMid());
				map.put("sum", win);// 设置余额
				// 根据mid修改余额
				int updateSum = diceDao.updateSum(map);
			}else {
				db.setWin("L");
			}
			
			db.setDraw_term(current.getCurrent_term());
			db.setDraw_time(new Date());
			db.setWin_money(win);
			diceDao.updateDiceBet(db);
			
	}
}
