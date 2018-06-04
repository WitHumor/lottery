package com.springboot.lottery.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springboot.lottery.dto.DiceDrawBetDTO;
import com.springboot.lottery.dto.DiceDrawDTO;
import com.springboot.lottery.entity.DiceBet;
import com.springboot.lottery.entity.DiceDraw;
import com.springboot.lottery.entity.Member;
import com.springboot.lottery.service.DiceService;
import com.springboot.lottery.service.MemberService;
import com.springboot.lottery.util.DiceBetUtil;
import com.springboot.lottery.util.MessageUtil;
import com.springboot.lottery.util.ObjectResult;

/**
 * 控制层
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("dice")
@CrossOrigin(origins = "*")
public class DiceController {

	@Autowired
	private DiceService diceService;
	
	@Autowired
	private MemberService memberService;
	
	
	@Autowired
	private EhCacheCacheManager manager;
	@Autowired
	private HttpServletRequest request;

	
	@RequestMapping(value = "member-info", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult memberInfo() throws InterruptedException {
		ObjectResult result = new ObjectResult();
		
		//check token
		String token = request.getHeader("token");
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			// 空值
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			// token过期
			result.setCode(MessageUtil.TOKEN_OVERDUE);
			return result;
		}
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		//check user credit
		
		Map<String, Object> map = new HashMap<String, Object>();
		// 根据id查询账户余额
		map.put("mid", member.getMid());
		List<Member> memberList =  memberService.queryMember(map);
		if (memberList == null || memberList.size() > 1) {
			System.err.println("mid查询不到数据");
			// 数据匹配错误
			result.setCode(MessageUtil.DATA_NOT_FOUND);
			return result;
		}
		member = memberList.get(0);
		result.setResult(member);
		return result;
	}
	
	
	@RequestMapping(value = "bet-draw-history", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult betDrawHistory(int page) throws InterruptedException {
		ObjectResult result = new ObjectResult();
		//check token
		String token = request.getHeader("token");
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			// 空值
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			// token过期
			result.setCode(MessageUtil.TOKEN_OVERDUE);
			return result;
		}
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		Map<String, Object> map = new HashMap<String, Object>();
		// 根据id查询账户余额
		map.put("mid", member.getMid());
		map.put("history", "true");
		map.put("beginIndex", (page-1)*DiceBetUtil.page_size);
		map.put("pageSize", DiceBetUtil.page_size);
		Integer total = diceService.queryDiceDrawBetTotal(map);
		Integer pages= total%DiceBetUtil.page_size == 0? total/DiceBetUtil.page_size: total/DiceBetUtil.page_size +  1;
		List<DiceDrawDTO> dbs =  diceService.queryDiceDrawBetDTO(map);
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("total", total);
		returnMap.put("pages", pages);
		returnMap.put("current_page", page);
		returnMap.put("result", dbs);
		
		result.setResult(returnMap);
		return result;
	}
	
	
	@RequestMapping(value = "bet-history", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult betHistory(int page) throws InterruptedException {
		ObjectResult result = new ObjectResult();
		//check token
		String token = request.getHeader("token");
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			// 空值
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			// token过期
			result.setCode(MessageUtil.TOKEN_OVERDUE);
			return result;
		}
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		Map<String, Object> map = new HashMap<String, Object>();
		// 根据id查询账户余额
		map.put("mid", member.getMid());
		map.put("history", "true");
		map.put("beginIndex", (page-1)*DiceBetUtil.page_size);
		map.put("pageSize", DiceBetUtil.page_size);
		Integer total = diceService.queryDiceDrawWithBetDTOTotal(map);
		Integer pages= total%DiceBetUtil.page_size == 0? total/DiceBetUtil.page_size: total/DiceBetUtil.page_size +  1;
		List<DiceDrawDTO> dbs =  diceService.queryDiceDrawWithBetDTO(map);
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("total", total);
		returnMap.put("pages", pages);
		returnMap.put("current_page", page);
		returnMap.put("result", dbs);
		
		result.setResult(returnMap);
		return result;
	}
	
	
	@RequestMapping(value = "dice-bet", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult addBet(int term, int bet, double bet_value) throws InterruptedException {
		
		ObjectResult result = new ObjectResult();
		
		//check token
		String token = request.getHeader("token");
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			// 空值
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			// token过期
			result.setCode(MessageUtil.TOKEN_OVERDUE);
			return result;
		}
		
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		//check user credit
		
		
		while(DiceBetUtil.drawing) {
			
			Thread.sleep(1000);
		}
		
		String mid = member.getMid();
		DiceBet db = new DiceBet();
		db.setBet(bet);
		db.setBet_value(bet_value);
		db.setMid(mid);
		db.setTerm(DiceBetUtil.current_term);
		db.setWin(null);
		db.setBet_time(new Date());
		int updateSum = diceService.addDiceBet(db,member.getMid());
		if(updateSum < 0) {
			result.setCode(MessageUtil.MONEY_EXCEED);
			return result;
		}
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("bet",db.getBet());
		returnMap.put("bet_value", db.getBet_value());
		returnMap.put("term",db.getTerm());
		
		result.setResult(returnMap);
		return result;
	}
	
	
	/**
	 * 20180428会员开户
	 * 
	 * @param member
	 * @return
	 */
	@RequestMapping(value = "account-info", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult getAccountInfo() {
		ObjectResult result = new ObjectResult();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		//check token
		String token = request.getHeader("token");
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			// 空值
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			// token过期
			result.setCode(MessageUtil.TOKEN_OVERDUE);
			return result;
		}
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		Map<String, Object> map = new HashMap<String, Object>();
		// 根据id查询账户余额
		map.put("mid", member.getMid());
		List<Member> memberList =  memberService.queryMember(map);
		member = memberList.get(0);

		returnMap.put("account",member.getSum());
		result.setResult(returnMap);
		
		return result;
		
	}
	

	
	/**
	 * 20180428会员开户
	 * 
	 * @param member
	 * @return
	 */
	@RequestMapping(value = "dice-draw", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult getDiceDrawInfo(boolean init) {
		ObjectResult result = new ObjectResult();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String,DiceBet> betMap = new HashMap<String,DiceBet>();
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
					returnMap.put("currentTerm",current.getCurrent_term());
					returnMap.put("betTotal",last.getBet_total());
					returnMap.put("betWinTotal",last.getWin_total());
					//
					
					//check token
					String token = request.getHeader("token");
					Cache cache = getCache();
					// 判断token是否为空
					if (StringUtils.isBlank(token)) {
						// 空值
						result.setCode(MessageUtil.NULL_ERROR);
						return result;
					}
					// 判断token是否过期
					if (cache.get(token) == null) {
						// token过期
						result.setCode(MessageUtil.TOKEN_OVERDUE);
						return result;
					}
					// 获取缓存中的数据
					Member member = (Member) cache.get(token).get();
					
					map = new HashMap<String, Object>();
					map.put("mid", member.getMid());
					if(init) {
						map.put("term", current.getCurrent_term());
					}else {
						map.put("term", last.getCurrent_term());
					}
					
					List<DiceBet> bets = diceService.queryDiceBet(map);
					
					for(DiceBet bet : bets) {
						betMap.put(bet.getBet().toString(),bet);
					}
					returnMap.put("bet", betMap);
					//
				}else {
					
					
					
				}
				
			}
		}else {
			returnMap.put("lastTerm",DiceBetUtil.last_term);
			returnMap.put("lastResult", DiceBetUtil.last_term_result);
			returnMap.put("drawTime",0);
			returnMap.put("currentTerm",DiceBetUtil.current_term);
			returnMap.put("bet", betMap);
			returnMap.put("betTotal",0);
			returnMap.put("betWinTotal",0);
		}
		result.setResult(returnMap);
		
		return result;
	}

	/**
	 * 指定一个ehcache缓存
	 * 
	 * @return
	 */
	private Cache getCache() {
		Cache cache = manager.getCache("memberCache");
		return cache;
	}

}
