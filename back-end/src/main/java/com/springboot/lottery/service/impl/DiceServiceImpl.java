package com.springboot.lottery.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.lottery.dto.DiceBetDTO;
import com.springboot.lottery.entity.DiceBet;
import com.springboot.lottery.entity.DiceDraw;
import com.springboot.lottery.entity.Member;
import com.springboot.lottery.mybatis.DiceDao;
import com.springboot.lottery.mybatis.MemberDao;
import com.springboot.lottery.service.DiceService;
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
	private MemberDao memberDao;
	
	@Override
	public List<DiceDraw> queryDiceDraw(Map<String, Object> map)
	{
		
		return diceDao.queryDiceDraw(map);
	}

	
	public Map<Integer, Map<String, Object>> queryDiceDrawResult(Map<String, Object> map){
		return diceDao.queryDiceDrawResult(map);
	}
	
	@Transactional
	public int addDiceBet(DiceBet diceBet,Member member) {
		// 修改账户余额
		Float sum = Float.parseFloat(member.getSum()) -  new Float(diceBet.getBet_value());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mid", member.getMid());// 设置mid
		map.put("sum", String.valueOf(sum));// 设置余额
		// 根据mid修改余额
		int updateSum = memberDao.updateSum(map);
		
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
