package com.springboot.lottery.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.springboot.lottery.entity.DiceBet;
import com.springboot.lottery.entity.DiceDraw;
import com.springboot.lottery.service.DiceService;

/**
 * 任务类
 * @author Administrator
 *
 */
@Component // component是spring扫描组件时的标识
public class DiceTimedTask {
	
	@Autowired
	private DiceService diceService;
	
	@Scheduled(fixedDelay = DiceBetUtil.dice_draw_minutes * 60* 1000)
	public void diceDraw() {
		DiceBetUtil.drawing = true;
		System.out.println(Thread.currentThread().getName() + "定时Dice任务启动");
		
		Map<String, Object> q = new HashMap<String,Object>();
		q.put("current", "current");
		
		DiceDraw current = diceService.queryDiceDraw(q).get(0);
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("term", current.getCurrent_term());
		
		Map<Integer, Map<String, Object>> rt = diceService.queryDiceDrawResult(map);
		double total_bet = 0;
		double dan_bet = 0;
		double shuang_bet = 0;
		double da_bet = 0;
		double xiao_bet = 0;
		double one_bet = 0;
		double two_bet = 0;
		double three_bet = 0;
		double four_bet = 0;
		double five_bet = 0;
		double six_bet = 0;
		for (Map.Entry<Integer, Map<String, Object>> entry : rt.entrySet())
		{
			double bet_value = ((BigDecimal)entry.getValue().get("bet_value")).doubleValue();
			if(entry.getKey().equals(DiceBetUtil.dan)) {
				dan_bet = bet_value;
				
			}
			if(entry.getKey().equals(DiceBetUtil.shuang)) {
				shuang_bet = bet_value;
				
			}
			
			if(entry.getKey().equals(DiceBetUtil.da)) {
				da_bet = bet_value;
				
			}
			
			if(entry.getKey().equals(DiceBetUtil.xiao)) {
				xiao_bet = bet_value;
				
			}
			if(entry.getKey().equals(DiceBetUtil.one)) {
				one_bet = bet_value;
				
			}
			if(entry.getKey().equals(DiceBetUtil.two)) {
				two_bet = bet_value;
				
			}
			if(entry.getKey().equals(DiceBetUtil.three)) {
				three_bet = bet_value;
				
			}	
			if(entry.getKey().equals(DiceBetUtil.four)) {
				four_bet = bet_value;
				
			}		
			if(entry.getKey().equals(DiceBetUtil.five)) {
				five_bet = bet_value;
				
			}	
			if(entry.getKey().equals(DiceBetUtil.six)) {
				six_bet = bet_value;
				
			}			
			total_bet = total_bet + bet_value;
		}
		
		double one_win = total_bet - one_bet*5 - dan_bet*1.9 - xiao_bet*1.9;
		double two_win = total_bet - two_bet*5 - shuang_bet*1.9 - xiao_bet*1.9;
		double three_win = total_bet - three_bet*5 - dan_bet*1.9 - xiao_bet*1.9;
		double four_win = total_bet - four_bet*5 - shuang_bet*1.9 - da_bet*1.9;
		double five_win = total_bet - five_bet*5 - dan_bet*1.9 - da_bet*1.9;
		double six_win = total_bet - six_bet*5 - shuang_bet*1.9 - da_bet*1.9;
		
		Random rand = new Random();
		int draw = 0;
		double win = 0;
		//100000
		while(draw <= 0) {
			draw = rand.nextInt(6) + 1;
			if(draw == 1 && one_win>DiceBetUtil.single_win_limit){
				double temp_win = DiceBetUtil.total_win + one_win;
				if(temp_win > DiceBetUtil.total_win_limit) {
					DiceBetUtil.total_win = DiceBetUtil.total_win + one_win;
					win = one_win;
					break;
				}
			} 
			if(draw == 2 && two_win > DiceBetUtil.single_win_limit) {
				double temp_win = DiceBetUtil.total_win + two_win;
				if(temp_win > DiceBetUtil.total_win_limit) {
					DiceBetUtil.total_win = DiceBetUtil.total_win + two_win;
					win = two_win;
					break;
				}
			}
			if(draw == 3 && three_win > DiceBetUtil.single_win_limit){
				double temp_win = DiceBetUtil.total_win + three_win;
				if(temp_win > DiceBetUtil.total_win_limit) {
					DiceBetUtil.total_win = DiceBetUtil.total_win + three_win;
					win = three_win;
					break;
				}
			}
			if(draw == 4 && four_win > DiceBetUtil.single_win_limit){
				double temp_win = DiceBetUtil.total_win + four_win;
				if(temp_win > DiceBetUtil.total_win_limit) {
					DiceBetUtil.total_win = DiceBetUtil.total_win + four_win;
					win = four_win;
					break;
				}
			}
			if(draw == 5 && five_win > DiceBetUtil.single_win_limit){
				double temp_win = DiceBetUtil.total_win + five_win;
				if(temp_win > DiceBetUtil.total_win_limit) {
					DiceBetUtil.total_win = DiceBetUtil.total_win + five_win;
					win = five_win;
					break;
				}
			}
			if(draw == 6 && six_win > DiceBetUtil.single_win_limit) {
				double temp_win = DiceBetUtil.total_win + six_win;
				if(temp_win > DiceBetUtil.total_win_limit) {
					DiceBetUtil.total_win = DiceBetUtil.total_win + six_win;
					win = six_win;
					break;
				}
			}
			
		}
		
		Date drawTime = new Date();
		
		diceService.genereateNewDiceDraw(current, draw,win);
		q = new HashMap<String,Object>();
		q.put("term", current.getCurrent_term());
		List<DiceBet> dbs = diceService.queryDiceBet(q);
		for(DiceBet db:dbs) {
			db.setDraw_time(drawTime);
			diceService.rewardMember(current, draw, db);
		}
		
		DiceBetUtil.current_term = current.getCurrent_term() + 1;
		DiceBetUtil.last_term = current.getCurrent_term() ;
		DiceBetUtil.last_term_result = draw;
		DiceBetUtil.drawing = false;
	}
}
