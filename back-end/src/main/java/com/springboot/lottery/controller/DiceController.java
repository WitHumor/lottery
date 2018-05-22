package com.springboot.lottery.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springboot.lottery.entity.DiceDraw;
import com.springboot.lottery.service.DiceService;
import com.springboot.lottery.util.DiceBetUtil;
import com.springboot.lottery.util.ObjectResult;

/**
 * 控制层
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("dice")
public class DiceController {

	@Autowired
	private DiceService diceService;
	@Autowired
	private EhCacheCacheManager manager;
	@Autowired
	private HttpServletRequest request;

	
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "dice-draw", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult addBet(int mid, int term, int bet, int bet_value) {
		
		ObjectResult result = new ObjectResult();
		//check user credit
		
		
		
		return result;
	}
	
	/**
	 * 20180428会员开户
	 * 
	 * @param member
	 * @return
	 */
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "dice-draw", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult getDiceDrawInfo() {
		ObjectResult result = new ObjectResult();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		if(!DiceBetUtil.drawing) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("beginIndex", 0);
			map.put("pageSize", 2);
			
			
			List<DiceDraw> list = diceService.queryDiceDraw(map);
			if(list.size() == 2) {
				DiceDraw current = list.get(0);
				DiceDraw last = list.get(1);
				if(current.getEnd_time() == null && last.getEnd_time() != null) {
					
					returnMap.put("lastTerm",last.getCurrent_term());
					returnMap.put("lastResult", last.getResult());
					long seconds = (new Date().getTime() -current.getStart_time().getTime())/1000;
					long leftTime = 0;
					if(seconds < DiceBetUtil.dice_draw_minutes * 60) {
						leftTime = DiceBetUtil.dice_draw_minutes * 60 - seconds;
					}
					returnMap.put("drawTime",leftTime);
					
				}else {
					
					
					
				}
				
			}
		}else {
			returnMap.put("lastTerm",DiceBetUtil.last_term);
			returnMap.put("lastResult", DiceBetUtil.last_term_result);
			returnMap.put("drawTime",0);
		}
		result.setResult(returnMap);
		
		return result;
	}

	@CrossOrigin(origins = "*")
	@RequestMapping(value = "test", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult test() {
		ObjectResult result = new ObjectResult();
		
		Map<String, Object> q = new HashMap<String,Object>();
		q.put("current", "current");
		
		DiceDraw current = diceService.queryDiceDraw(q).get(0);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("term", current.getCurrent_term());
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
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
		
		while(draw <= 0) {
			draw = rand.nextInt(6) + 1;
			if(draw == 1 && one_win>0){
				win = one_win;
				break;
			} 
			if(draw == 2 && two_win > 0) {
				win = two_win;
				break;
			}
			if(draw == 3 && three_win > 0){
				win = three_win;
				break;
			}
			if(draw == 4 && four_win > 0){
				win = four_win;
				break;
			}
			if(draw == 5 && five_win > 0){
				win = five_win;
				break;
			}
			if(draw == 6 && six_win > 0) {
				win = six_win;
				break;
			}
			
		}
		
		
		diceService.genereateNewDiceDraw(current, draw,win);
		//TODO 
		result.setResult(returnMap);
		
		
		return result;
	}

}
