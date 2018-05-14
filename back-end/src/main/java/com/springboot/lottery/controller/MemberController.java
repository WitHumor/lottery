package com.springboot.lottery.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springboot.lottery.dto.SingleNoteDTO;
import com.springboot.lottery.entity.Member;
import com.springboot.lottery.entity.MemberFundRecord;
import com.springboot.lottery.entity.MemberSingleNote;
import com.springboot.lottery.service.MemberService;
import com.springboot.lottery.util.BeanLoad;
import com.springboot.lottery.util.MessageUtil;
import com.springboot.lottery.util.ObjectResult;
import com.springboot.lottery.util.Page;
import com.springboot.lottery.util.JsoupUtil;

/**
 * 控制层
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("member")
public class MemberController {

	@Autowired
	private MemberService memberService;
	@Autowired
	private EhCacheCacheManager manager;
	@Autowired
	private HttpServletRequest request;

	/**
	 * 20180428会员开户
	 * 
	 * @param member
	 */
	@RequestMapping(value = "add-member", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult addMemberAll(Member member) {
		Cache cache = getCache();
		ObjectResult result = new ObjectResult();
		String mid = BeanLoad.getId();
		member.setMid(mid);// 设置主键id
		member.setSum("0");// 余额默认设置为0
		int addMember = memberService.addMember(member);
		if (addMember <= 0) {
			result.setCode(MessageUtil.UPDATE_ERROR);
			return result;
		}
		// 产生token
		String token = BeanLoad.getUUID();
		// 把token做为key放入ehcache缓存
		cache.put(token, member);
		member.setToken(token);
		result.setResult(toMapByMember(member));
		return result;
	}

	/**
	 * 20180428用户名验证
	 * 
	 * @param member
	 */
	@RequestMapping(value = "verify-name", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult verifyName(String name) {
		ObjectResult result = new ObjectResult();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		// 根据用户名查找数据
		List<Member> list = memberService.loginMember(map);
		// 判断否存在数据
		if (list.size() > 0) {
			result.setCode(MessageUtil.NAME_EXIST);
			return result;
		}
		return result;
	}

	/**
	 * 20180428会员登录
	 * 
	 * @param member
	 */
	@RequestMapping(value = "login-member", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult loginMember(Member member) {
		Cache cache = getCache();
		// 判断缓存是否存在，如果存在则移除
		// if (StringUtils.isNotEmpty(token)) {
		// cache.evict(token);
		// }
		String name = member.getName();
		String password = member.getPassword();
		String token = member.getToken();
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectResult result = new ObjectResult();
		map.put("name", name);
		List<Member> list = memberService.loginMember(map);
		// 判断list对象是否为空
		if (list == null) {
			result.setCode(MessageUtil.MEMBER_NOT);
			return result;
		}
		// 判断list集合中是否只有一条数据
		if (list.size() != 1) {
			result.setCode(MessageUtil.DATABASE_DADA_ERROR);
			return result;
		}
		// 获取数据匹配密码
		member = list.get(0);
		if (!member.getPassword().equals(password)) {
			result.setCode(MessageUtil.PASSWORD_ERROR);
			return result;
		}
		// 产生token
		token = BeanLoad.getUUID();
		// 把token做为key放入ehcache缓存
		cache.put(token, member);
		member.setToken(token);
		result.setResult(toMapByMember(member));
		return result;
	}

	/**
	 * 20180428会员登出
	 * 
	 * @param member
	 */
	@RequestMapping(value = "exit-member", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult memberExit() {
		String token = request.getHeader("token");
		ObjectResult result = new ObjectResult();
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			return result;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			return result;
		}
		// 移除token
		cache.evict(token);
		// 判断token是否被移除
		if (cache.get(token) != null) {
			return result;
		}
		return result;
	}

	/**
	 * 20180507会员余额
	 * 
	 * @param member
	 */
	@RequestMapping(value = "member-money", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult getMemberByMoney(String mid) {
		ObjectResult result = new ObjectResult();
		// 根据id查询账户余额
		Member memberByMoney = memberService.getMemberByMoney(mid);
		if (memberByMoney == null) {
			System.err.println("mid查询不到数据");
		}
		// 创建一个map对象
		Map<String, String> map = new HashMap<String, String>();
		// 往map中添加键值对返回
		map.put("name", memberByMoney.getName());
		map.put("sum", memberByMoney.getSum());
		result.setResult(map);
		return result;
	}

	/**
	 * 20180508在线取款
	 * 
	 * @param member
	 */
	@RequestMapping(value = "member-withdrawn", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult memberWithdrawn(String money) {
		String token = request.getHeader("token");
		ObjectResult result = new ObjectResult();
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			result.setCode(MessageUtil.TOKEN_OVERDUE);
			return result;
		}
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		// 获取缓存中的会员id
		String mid = member.getMid();
		// 根据会员id获取余额
		Member memberByMoney = memberService.getMemberByMoney(mid);
		String sum = memberByMoney.getSum();
		// 判断余额是否足够
		if (Float.parseFloat(sum) > Float.parseFloat(money)) {
			result.setCode(MessageUtil.MONEY_EXCEED);
			return result;
		}
		// 添加资金交易记录
		MemberFundRecord memberFundRecord = new MemberFundRecord();
		memberFundRecord.setFrid(BeanLoad.getId());// 资金交易id
		memberFundRecord.setMid(mid);// 会员id
		memberFundRecord.setNumber(BeanLoad.getimeMillis());// 编号
		memberFundRecord.setType("提款");// 类型
		memberFundRecord.setDiscounts("无");
		memberFundRecord.setState("确认中");// 状态
		memberFundRecord.setRemark("这是备注"); // 备注
		int addFundRecord = memberService.addFundRecord(memberFundRecord);
		// 判断是否添加成功
		if (addFundRecord <= 0) {
			result.setCode(MessageUtil.INSERT_ERROR);
			return result;
		}
		Map<String, String> map = new HashMap<String, String>();
		// 设置会员id
		map.put("mid", mid);
		// 设置余额
		map.put("sum", String.valueOf(Float.parseFloat(sum) - Float.parseFloat(money)));
		// 根据会员id修改账户余额
		int updateSum = memberService.updateSum(map);
		// 判断余额是否修改成功，如果不成功，则删除资金记录
		if (updateSum <= 0) {
			// 根据id删除资金记录
			memberService.deleteFundRecord(memberFundRecord.getFrid());
			result.setCode(MessageUtil.UPDATE_ERROR);
			return result;
		}
		result.setCode(MessageUtil.SUCCESS);
		return result;
	}

	/**
	 * 20180508在线存款
	 * 
	 * @param member
	 */
	@RequestMapping(value = "member-deposit", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult memberDeposit(String money, String discountsMoeny) {
		String token = request.getHeader("token");
		ObjectResult result = new ObjectResult();
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			result.setCode(MessageUtil.TOKEN_OVERDUE);
			return result;
		}
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		// 获取缓存中的会员id
		String mid = member.getMid();
		// 添加资金交易记录
		MemberFundRecord memberFundRecord = new MemberFundRecord();
		memberFundRecord.setFrid(BeanLoad.getId());// 资金交易id
		memberFundRecord.setMid(mid);// 会员id
		memberFundRecord.setNumber(BeanLoad.getimeMillis());// 编号
		memberFundRecord.setType("存款");// 类型
		memberFundRecord.setMoney(money);// 交易金额
		memberFundRecord.setDiscounts(discountsMoeny);
		memberFundRecord.setState("确认中");// 状态
		memberFundRecord.setRemark("这是备注"); // 备注
		int addFundRecord = memberService.addFundRecord(memberFundRecord);
		// 判断是否添加成功
		if (addFundRecord <= 0) {
			result.setCode(MessageUtil.INSERT_ERROR);
			return result;
		}
		result.setCode(MessageUtil.SUCCESS);
		return result;
	}

	/**
	 * 20180509提现记录/充值记录
	 * 
	 * @param member
	 */
	@RequestMapping(value = "member-record", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult allRecord(String record, String beginTime, String endTime, String type, String state,
			Integer pageNo, Integer pageSize) {
		String token = request.getHeader("token");
		ObjectResult result = new ObjectResult();
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			result.setCode(MessageUtil.TOKEN_OVERDUE);
			return result;
		}
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		// 获取缓存中的会员id
		String mid = member.getMid();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mid", mid);// 会员id
		map.put("beginTime", beginTime);// 开始时间
		map.put("endTime", endTime);// 结束时间
		map.put("type", type);// 类型
		map.put("state", state);// 状态
		map.put("pageSize", pageSize);
		map.put("beginIndex", (pageNo - 1) * pageSize);
		map.put("pageNo", pageNo);
		map.put("record", record);
		Page<Map<String, Object>> page = memberService.listFundRecord(map);
		if (page == null) {
			result.setCode(MessageUtil.DATA_NOT);
		}
		// 0表示充值记录，1表示提现记录
		if (record.equals("0") || record.equals("1")) {
			result.setCode(MessageUtil.PARAMETER_ERROR);
			return result;
		}
		result.setResult(page);
		return result;
	}

	/**
	 * 20180502会员下注
	 * 
	 * @param member
	 */
	@RequestMapping(value = "bet-member", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult betMember(String url, String gid, String ratio, String ratioData, String money, String bet,
			String betType) {
		String token = request.getHeader("token");
		ObjectResult result = new ObjectResult();
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			result.setCode(MessageUtil.TOKEN_OVERDUE);
			return result;
		}
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		// 获取缓存中的会员id
		String mid = member.getMid();
		// 根据url地址获取所有数据
		String stringAll = JsoupUtil.getStringAll(url);
		if (StringUtils.isBlank(stringAll)) {
			result.setCode(MessageUtil.NETWORK_CONNECTION);
			return result;
		}
		// 根据所有数据进行切割获取数据
		List<Map<String, String>> list = JsoupUtil.listFieldAndData(stringAll);
		if (list == null) {
			result.setCode(MessageUtil.NETWORK_CONNECTION);
			return result;
		}
		// 获取地址中与gid匹配的数据
		Map<String, String> mapData = JsoupUtil.getMapData(list, gid);
		if (mapData == null) {
			result.setCode(MessageUtil.LEAGUE_END);
			return result;
		}
		// 查找数据判断赔率是否为空
		String mapRatio = mapData.get(ratio);
		if (StringUtils.isBlank(mapRatio)) {
			result.setCode(MessageUtil.DATA_NOT_FOUND);
			return result;
		}
		// 根据查找的赔率判断误差，误差不能超过正负0.1
		Float margin = Float.parseFloat(mapRatio) - Float.parseFloat(ratioData);
		if (margin > 0.1 || margin < (-0.1)) {
			result.setCode(MessageUtil.DATA_ERROR_OVERSIZE);
			return result;
		}
		// 查询会员余额是否够下注
		Member memberByMoney = memberService.getMemberByMoney(mid);
		if (Float.parseFloat(memberByMoney.getSum()) < Float.parseFloat(money)) {
			result.setCode(MessageUtil.MONEY_EXCEED);
			return result;
		}
		Map<String, List<String>> fieldExplain = JsoupUtil.getFieldExplain(stringAll);
		// 定义场次与比分
		String fullOrHr = "", score = "";
		// 判断是否是全场
		if (fieldExplain.get("fullList").contains(ratio)) {
			fullOrHr = "全场";
		} else if (fieldExplain.get("hrList").contains(ratio)) {
			fullOrHr = "半场";
		}
		// 判断是否是滚球
		if (betType.equals("FT")) {
			betType = url.contains("rtype=re") ? "REFT" : "RFT";
			score = betType.equals("REFT") ? mapData.get("score_h")+" - "+mapData.get("score_c") : "";
		} else if (betType.equals("BK")) {
			betType = url.contains("rtype=re") ? "REBK" : "RBK";
			score = betType.equals("REBK") ? mapData.get("score_h")+" - "+mapData.get("score_c") : "";
		} else{
			result.setCode(MessageUtil.PARAMETER_ERROR);
			return  result;
		}
		// 计算有效金额
		float validMoney = Integer.parseInt(money) * Float.parseFloat(ratioData);
		String league = mapData.get("league");// 获取数据的赛事
		String teamh = mapData.get("team_h");// 获取数据的主场
		String teamc = mapData.get("team_c");// 获取数据的客场
		String snid = BeanLoad.getId();// 随机生成主键id
		MemberSingleNote memberSingleNote = new MemberSingleNote();
		memberSingleNote.setSnid(snid);// 设置主键id
		memberSingleNote.setMid(mid);// 设置会员id
		memberSingleNote.setNumber(BeanLoad.getimeMillis());// 设置注单号
		memberSingleNote.setType("体育");// 设置类型
		memberSingleNote.setTeam_h(teamh);// 设置主场
		memberSingleNote.setTeam_c(teamc);// 设置客场
		memberSingleNote.setInterval(fullOrHr);// 设置场次
		memberSingleNote.setIor_type(JsoupUtil.betType().get(ratio));// 设置比率类型
		memberSingleNote.setIor_ratio(score);// 设置比率
		memberSingleNote.setBet(bet);// 设置下注对象
		memberSingleNote.setBet_type(betType);// 设置下注类型
		memberSingleNote.setLeague(league);// 设置联赛
		memberSingleNote.setState("未结算");// 设置状态
		memberSingleNote.setMoney(money);// 设置下注金额
		memberSingleNote.setValid_money(String.valueOf(validMoney));// 设置有效金额
		memberSingleNote.setWin_lose("--");// 设置输赢
		int betMember = memberService.betMember(memberSingleNote);
		// 判断数据是否添加成功
		if (betMember <= 0) {
			result.setCode(MessageUtil.INSERT_ERROR);
			return result;
		}
		// 修改账户余额
		Float sum = Float.parseFloat(memberByMoney.getSum()) - Float.parseFloat(money);
		Map<String, String> map = new HashMap<String, String>();
		map.put("mid", mid);// 设置mid
		map.put("sum", String.valueOf(sum));// 设置余额
		// 根据mid修改余额
		int updateSum = memberService.updateSum(map);
		// 判断余额是否修改成功，如果不成功则删除添加的下注订单
		if (updateSum <= 0) {
			result.setCode(MessageUtil.UPDATE_ERROR);
			memberService.deleteSingleNote(snid);
			return result;
		}
		return result;
	}

	/**
	 * 注单结算
	 * 
	 * @param member
	 * @throws ParseException
	 */
	public String singleNoteAccount() throws ParseException {
		// 定义时间格式
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// 将时间类型转换为字符串类型
		String date = format.format(new Date());
		// 指定时间查询
		String sj = "2018-05-10";
		// 将字符串转换为date类型
		Date parse = format.parse(sj);
		// 创建map对象
		Map<String, String> map = new HashMap<String, String>();
		// 往map集合中添加数据
		map.put("state", "未结算");
		map.put("betTime", sj);
		// 根据map在数据库中查询数据
		List<SingleNoteDTO> querySingleNoteById = memberService.querySingleNoteDTO(map);
		// 判断查询出来是否有数据
		if (querySingleNoteById == null || querySingleNoteById.size() <= 0) {
			System.out.println("没有注单结算的数据");
			return null;
		}
		// 使用迭代器循环遍历数据
		Iterator<SingleNoteDTO> iterator = querySingleNoteById.iterator();
		while (iterator.hasNext()) {
			SingleNoteDTO singleNote = iterator.next();
			if (singleNote == null) {
				continue;
			}
			// 判断是足球联赛还是篮球联赛
			if (singleNote.getBet_type().equals("REFT") || singleNote.getBet_type().equals("RFT")) {
				// 根据联赛数据与时间获取足球数据
				List<Map<String, String>> footballResult = JsoupUtil.getFootballResult(singleNote.getLeague(), parse);
				// 判断数据是否为空
				if (footballResult == null || footballResult.size() <= 0) {
					System.out.println(singleNote.getLeague() + "---没有注单结算的数据");
					continue;
				}
				// 足球数据匹配
				List<Object> listResult = JsoupUtil.getGameMap(footballResult, singleNote);
				if (listResult == null) {
					continue;
				}
				boolean stateUpdate = stateUpdate(singleNote, listResult);
				if (stateUpdate) {
					System.out.println("注单结算成功");
				}
			} else if (singleNote.getBet_type().equals("RBK") || singleNote.getBet_type().equals("REBK")) {
				// 根据联赛数据与时间获取篮球数据
				List<Map<String, String>> basketballResult = JsoupUtil.getBasketballResult(singleNote.getLeague(),
						parse);
				// 判断数据是否为空
				if (basketballResult == null || basketballResult.size() <= 0) {
					continue;
				}
				// 篮球数据匹配
				List<Object> listResult = JsoupUtil.getGameMap(basketballResult, singleNote);
				if (listResult == null) {
					continue;
				}
				// 返回true表示注单结算成功
				boolean stateUpdate = stateUpdate(singleNote, listResult);
				if (stateUpdate) {
					System.out.println("注单结算成功");
				}
			}
			return MessageUtil.SUCCESS;
		}
		return null;
	}

	/**
	 * 足球篮球结算
	 * 
	 * @param singleNote
	 * @param moneyAndSum
	 * @param bet
	 * @return
	 */
	public boolean stateUpdate(SingleNoteDTO singleNote, List<Object> listResult) {
		Map<String, String> singleNoteMap = new HashMap<String, String>();
		Member member = memberService.getMemberByMoney(singleNote.getMid());
		Float memberByMoney = Float.parseFloat(member.getSum());// 得到账户余额
		Float money = Float.parseFloat(singleNote.getMoney());// 得到下注金额
		Float validMoney = Float.parseFloat(singleNote.getValid_money());// 得到有效金额
		String bet = (String) listResult.get(0);
		Float sum = null;
		// 获取客场比分
		Integer firstFloat = (Integer) listResult.get(1);
		// 获取主场比分
		Integer lastFloat = (Integer) listResult.get(2);
		// 获取比分差
		Integer score = (Integer) listResult.get(3);
		// 获取下注比率
		String ratio = singleNote.getIor_ratio();
		// 如果赛事腰斩了实行的操作
		if (bet.equals("赛事腰斩")) {
			// 返回下注金额到账户余额
			sum = memberByMoney + money;
			Map<String, String> map = new HashMap<String, String>();
			map.put("mid", singleNote.getMid());// 设置mid
			map.put("sum", String.valueOf(sum));// 设置余额
			// 根据mid修改余额
			int updateSum = memberService.updateSum(map);
			if (updateSum <= 0) {
				System.err.println("修改余额失败！");
				return false;
			}
			// 根据snid修改数据
			singleNoteMap.put("snid", singleNote.getSnid());
			// 往map里添加需要修改的字段
			singleNoteMap.put("state", "已结算");
			singleNoteMap.put("winLose", "赛事腰斩");
			// 根据snid修改注单状态
			int singleNoteAccount = memberService.singleNoteAccount(singleNoteMap);
			// 如果注单表修改失败则把账户余额修改回去
			if (singleNoteAccount <= 0) {
				System.err.println("修改注单状态失败！");
				map.put("sum", String.valueOf(memberByMoney));// 设置余额
				memberService.updateSum(map);// 修改账户余额
				return false;
			} else {
				System.out.println("修改注单" + singleNote.getNumber() + "状态成功！");
			}
			return true;
		}
		// 如果赛事下注赢实行的操作
		if (bet.equals(singleNote.getBet())) {
			// 判断下注比率是否为空，为空则有以下状态
			if (StringUtils.isBlank(ratio)) {
				if (singleNote.getIor_type().equals("独赢")) {
					// 下注金额与有效金额都返回账户余额
					sum = memberByMoney + validMoney;
				}
				if (singleNote.getIor_type().equals("单")) {
					sum = (firstFloat + lastFloat) % 2 == 0 ? memberByMoney : sum;
					sum = (firstFloat + lastFloat) % 2 == 1 ? memberByMoney + money + validMoney : sum;
				}
				if (singleNote.getIor_type().equals("双")) {
					sum = (firstFloat + lastFloat) % 2 == 0 ? memberByMoney + money + validMoney : sum;
					sum = (firstFloat + lastFloat) % 2 == 1 ? memberByMoney : sum;
				}
			} else {
				if (singleNote.getIor_type().equals("让球")) {
					// 根据比率计算让球输赢
					if (ratio.contains("/") && ratio.contains(".")) {
						String[] split = ratio.split("/");
						if (split[0].contains(".")) {
							// 赢一半
							sum = score == Integer.parseInt(split[1]) ? memberByMoney + money + (validMoney / 2) : null;
							// 全输
							sum = score < Integer.parseInt(split[1]) ? memberByMoney : sum;
							// 全赢
							sum = score > Integer.parseInt(split[1]) ? memberByMoney + money + validMoney : sum;
						} else if (split[1].contains(".")) {
							// 输一半
							sum = score == Integer.parseInt(split[0]) ? memberByMoney + (money / 2) : null;
							// 全输
							sum = score < Integer.parseInt(split[0]) ? memberByMoney : sum;
							// 全赢
							sum = score > Integer.parseInt(split[0]) ? memberByMoney + money + validMoney : sum;
						}
					} else if (ratio.contains(".") && !ratio.contains("/")) {
						String[] split = ratio.split("\\.");
						// 全赢
						sum = score > Integer.parseInt(split[0]) ? memberByMoney + money + validMoney : null;
						// 全输
						sum = score <= Integer.parseInt(split[0]) ? memberByMoney : sum;

					} else if (!ratio.contains(".") && !ratio.contains("/")) {
						// 全赢
						sum = score > Integer.parseInt(ratio) ? memberByMoney + money + validMoney : null;
						// 全输
						sum = score < Integer.parseInt(ratio) ? memberByMoney : sum;
						// 不输不赢
						sum = score == Integer.parseInt(ratio) ? memberByMoney + money : sum;
					}
				}
				if (singleNote.getIor_type().equals("大")) {
					// 根据比率计算大输赢
					if (ratio.contains("/") && ratio.contains(".")) {
						String[] split = ratio.split("/");
						if (split[0].contains(".")) {
							// 既不是大球也不是小球，买大球的人赢一半
							sum = (firstFloat + lastFloat) == Integer.parseInt(split[1]) ? memberByMoney + money + (validMoney / 2) : sum;
							// 全输
							sum = (firstFloat + lastFloat) < Integer.parseInt(split[1]) ? memberByMoney : sum;
							// 全赢
							sum = (firstFloat + lastFloat) > Integer.parseInt(split[1]) ? memberByMoney + money + validMoney : sum;
						} else if (split[1].contains(".")) {
							// 既不是大球也不是小球，买大球的人输一半
							sum = (firstFloat + lastFloat) == Integer.parseInt(split[0]) ? memberByMoney + (money / 2) : sum;
							// 全输
							sum = (firstFloat + lastFloat) < Integer.parseInt(split[0]) ? memberByMoney : sum;
							// 全赢
							sum = (firstFloat + lastFloat) > Integer.parseInt(split[0]) ? memberByMoney + money + validMoney : sum;
						}
					} else if (ratio.contains(".") && !ratio.contains("/")) {
						// 全赢
						sum = (firstFloat + lastFloat) > Float.parseFloat(ratio) ? memberByMoney + money + validMoney : sum;
						// 全输
						sum = (firstFloat + lastFloat) < Float.parseFloat(ratio) ? memberByMoney : sum;
					} else if (!ratio.contains(".") && !ratio.contains("/")) {
						// 全赢
						sum = (firstFloat + lastFloat) > Integer.parseInt(ratio) ? memberByMoney + money + validMoney : sum;
						// 全输
						sum = (firstFloat + lastFloat) < Integer.parseInt(ratio) ? memberByMoney : sum;
						// 不输不赢
						sum = score == Integer.parseInt(ratio) ? memberByMoney + money : sum;
					}
				}
				if (singleNote.getIor_type().equals("小")) {
					// 根据比率计算小输赢
					if (ratio.contains("/") && ratio.contains(".")) {
						String[] split = ratio.split("/");
						if (split[0].contains(".")) {
							// 既不是大球也不是小球，买小球的人输一半
							sum = (firstFloat + lastFloat) == Integer.parseInt(split[1]) ? memberByMoney + (money / 2) : sum;
							// 全输
							sum = (firstFloat + lastFloat) > Integer.parseInt(split[1]) ? memberByMoney : sum;
							// 全赢
							sum = (firstFloat + lastFloat) < Integer.parseInt(split[1]) ? memberByMoney + money + validMoney : sum;
						} else if (split[1].contains(".")) {
							// 既不是大球也不是小球，买小球的人赢一半
							sum = (firstFloat + lastFloat) == Integer.parseInt(split[0]) ? memberByMoney + money + (validMoney / 2) : sum;
							// 全输
							sum = (firstFloat + lastFloat) > Integer.parseInt(split[0]) ? memberByMoney : sum;
							// 全赢
							sum = (firstFloat + lastFloat) < Integer.parseInt(split[0]) ? memberByMoney + money + validMoney : sum;
						}
					} else if (ratio.contains(".") && !ratio.contains("/")) {
						// 全输
						sum = (firstFloat + lastFloat) > Float.parseFloat(ratio) ? memberByMoney : sum;
						// 全赢
						sum = (firstFloat + lastFloat) < Float.parseFloat(ratio) ? memberByMoney + money + validMoney : sum;
					} else if (!ratio.contains(".") && !ratio.contains("/")) {
						// 全输
						sum = (firstFloat + lastFloat) > Integer.parseInt(ratio) ? memberByMoney : sum;
						// 全赢
						sum = (firstFloat + lastFloat) < Integer.parseInt(ratio) ? memberByMoney + money + validMoney : sum;
						// 不输不赢
						sum = score == Integer.parseInt(ratio) ? memberByMoney + money : sum;
					}
				}
			}
			if (sum == null) {
				System.err.println("余额为空！");
				return false;
			}
			// 下注金额与有效金额都返回账户余额
			Map<String, String> map = new HashMap<String, String>();
			map.put("mid", singleNote.getMid());// 设置mid
			map.put("sum", String.valueOf(sum));// 设置余额
			// 根据mid修改余额
			int updateSum = memberService.updateSum(map);
			if (updateSum <= 0) {
				System.err.println("修改余额失败！");
				return false;
			}
			// 根据snid修改数据
			singleNoteMap.put("snid", singleNote.getSnid());
			// 往map里添加需要修改的字段
			singleNoteMap.put("state", "已结算");
			singleNoteMap.put("winLose", "赢");
			// 根据snid修改注单状态
			int singleNoteAccount = memberService.singleNoteAccount(singleNoteMap);
			// 如果注单表修改失败则把账户余额修改回去
			if (singleNoteAccount <= 0) {
				System.err.println("修改注单状态失败！");
				map.put("sum", String.valueOf(memberByMoney));// 设置余额
				memberService.updateSum(map);// 修改账户余额
				return false;
			} else {
				System.out.println("修改注单" + singleNote.getNumber() + "状态成功！");
			}
			return true;
		}
		// 如果赛事下注输实行的操作
		if (!bet.equals(singleNote.getBet())) {
			// 根据snid修改数据
			singleNoteMap.put("snid", singleNote.getSnid());
			// 往map里添加需要修改的字段
			singleNoteMap.put("state", "已结算");
			singleNoteMap.put("winLose", "输");
			// 根据snid修改注单状态
			int singleNoteAccount = memberService.singleNoteAccount(singleNoteMap);
			if (singleNoteAccount <= 0) {
				System.err.println("修改注单状态失败！");
				return false;
			} else {
				System.out.println("修改注单" + singleNote.getNumber() + "状态成功！");
			}
			return true;
		}
		return false;
	}

	/**
	 * 指定一个ehcache缓存
	 * 
	 * @param token
	 */
	private Cache getCache() {
		Cache cache = manager.getCache("memberCache");
		return cache;
	}

	/**
	 * Member 放入 Map<String, Object> 只保留前端需要的字段
	 * 
	 * @param dto
	 * @return
	 */
	private Map<String, Object> toMapByMember(Member member) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mid", member.getMid());// mid
		map.put("name", member.getName());// 姓名
		map.put("address", member.getAddress());// ip地址
		map.put("token", member.getToken()); // token
		map.put("real_name", member.getReal_name());// 真实姓名
		return map;
	}
}
