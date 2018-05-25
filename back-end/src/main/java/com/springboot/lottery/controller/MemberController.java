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
import org.springframework.web.bind.annotation.CrossOrigin;
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
	 * @return
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
			// 修改失败
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
	 * @param name 用户名
	 * @return
	 */
	@RequestMapping(value = "verify-name", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult verifyName(String name) {
		ObjectResult result = new ObjectResult();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		// 根据用户名查找数据
		List<Member> list = memberService.queryMember(map);
		// 判断否存在数据
		if (list.size() > 0) {
			// 用户名已存在
			result.setCode(MessageUtil.NAME_EXIST);
			return result;
		}
		return result;
	}

	/**
	 * 20180428会员登录
	 * 
	 * @param member
	 * @return
	 */
	@RequestMapping(value = "login-member", method = RequestMethod.POST)
	@ResponseBody
	@CrossOrigin(origins = "*")
	public ObjectResult loginMember(Member member) {
		Cache cache = getCache();
		String token = request.getHeader("token");
		// 判断缓存是否存在，如果存在则移除
		if (StringUtils.isNotEmpty(token)) {
			cache.evict(token);
		}
		String name = member.getName();
		String password = member.getPassword();
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectResult result = new ObjectResult();
		map.put("name", name);
		List<Member> list = memberService.queryMember(map);
		// 判断list对象是否为空
		if (list == null) {
			// 会员不存在
			result.setCode(MessageUtil.MEMBER_NOT);
			return result;
		}
		// 判断list集合中是否只有一条数据
		if (list.size() != 1) {
			// 数据库出现多条数据
			result.setCode(MessageUtil.DATABASE_DADA_ERROR);
			return result;
		}
		// 获取数据匹配密码
		member = list.get(0);
		if (!member.getPassword().equals(password)) {
			// 密码错误
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
	 * @return
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
	 * @return
	 */
	@RequestMapping(value = "member-money", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult getMemberByMoney() {
		String token = request.getHeader("token");
		ObjectResult result = new ObjectResult();
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
		// 获取缓存中的会员id
		String mid = member.getMid();
		// 创建一个map对象
		Map<String, Object> map = new HashMap<String, Object>();
		// 根据id查询账户余额
		map.put("mid", mid);
		List<Member> memberList =  memberService.queryMember(map);
		if (memberList == null || memberList.size() > 1) {
			System.err.println("mid查询不到数据");
			// 数据匹配错误
			result.setCode(MessageUtil.DATA_NOT_FOUND);
			return result;
		}
		member = memberList.get(0);
		map.remove("mid");
		// 往map中添加键值对返回
		map.put("name", member.getName());
		map.put("sum", member.getSum());
		result.setResult(map);
		return result;
	}
	
	/**
	 * 20180516汇率转换
	 * @param currency 货币
	 * @param record 存取款
	 * @return
	 */
	@RequestMapping(value = "money-exchange", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult moneyExchange(String currency, String record) {
		String token = request.getHeader("token");
		ObjectResult result = new ObjectResult();
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
		String exchange = JsoupUtil.getExchange(currency);
		if(exchange == null) {
			// 网络连接失败
			result.setCode(MessageUtil.NETWORK_CONNECTION);
			return result;
		}
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		// 获取缓存中的会员id
		String mid = member.getMid();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("exchange", exchange);
		// 判断是否是首充
		if(record.equals("0")) {
			// 记录类型
			map.put("record", record);
			// 会员id
			map.put("mid", mid);
			// 根据会员id与记录类型查询是否有记录
			int total = memberService.loadFundRecordTotal(map);
			if(total == 0) {
				map.put("total", 0);
			} else {
				map.put("total", 1);
			}
			// 移除不需要返回的值
			map.remove("record");
			map.remove("mid");
			result.setResult(map);
			return result;
		} else if(record.equals("1")) {
			result.setResult(map);
			return result;
		}
		// 参数错误
		result.setCode(MessageUtil.PARAMETER_ERROR);
		return result;
	}
	/**
	 * 20180508在线取款
	 * 
	 * @param money 取款金额
	 * @param currency 货币
	 * @param phone 手机号
	 * @param address 钱包地址
	 * @param remark 备注
	 * @param password 取款密码
	 * @param currencyCount 货币个数
	 * @return
	 */
	@RequestMapping(value = "member-withdrawn", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult memberWithdrawn(String money, String currency, String phone, String address, String remark,
			String password, String currencyCount) {
		String token = request.getHeader("token");
		ObjectResult result = new ObjectResult();
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
		// 获取缓存中的会员id
		String mid = member.getMid();
		// 根据会员id获取余额
		Map<String, Object> map = new HashMap<String, Object>();
		// 设置会员id
		map.put("mid", mid);
		List<Member> memberList =  memberService.queryMember(map);
		if (memberList == null || memberList.size() > 1) {
			System.err.println("mid查询不到数据");
			// 数据匹配错误
			result.setCode(MessageUtil.DATA_NOT_FOUND);
			return result;
		}
		member = memberList.get(0);
		// 判断取款密码是否正确
		if (!member.getBank_password().equals(password)) {
			// 密码错误
			result.setCode(MessageUtil.PASSWORD_ERROR);
			return result;
		}
		String sum = member.getSum();
		// 判断余额是否足够
		if (Float.parseFloat(sum) < Float.parseFloat(money)) {
			// 超过自己的余额
			result.setCode(MessageUtil.MONEY_EXCEED);
			return result;
		}
		// 添加资金交易记录
		MemberFundRecord memberFundRecord = new MemberFundRecord();
		memberFundRecord.setFrid(BeanLoad.getId());// 资金交易id
		memberFundRecord.setMid(mid);// 会员id
		memberFundRecord.setNumber(BeanLoad.getimeMillis());// 编号
		memberFundRecord.setPhone_code(phone);// 手机号
		memberFundRecord.setMoney_address(address);// 钱包地址
		memberFundRecord.setRecord("1");// 记录类型
		memberFundRecord.setMoney(money);// 金额
		memberFundRecord.setCurrency(currency);// 充值类型
		memberFundRecord.setCurrency_count(currencyCount);// 货币数量
		memberFundRecord.setDiscounts(null);// 优惠金额
		memberFundRecord.setState("0");// 状态
		memberFundRecord.setRemark(remark); // 备注
		int addFundRecord = memberService.addFundRecord(memberFundRecord);
		// 判断是否添加成功
		if (addFundRecord <= 0) {
			// 添加失败
			result.setCode(MessageUtil.INSERT_ERROR);
			return result;
		}
		// 设置余额
		map.put("sum", String.valueOf(Float.parseFloat(sum) - Float.parseFloat(money)));
		// 根据会员id修改账户余额
		int updateSum = memberService.updateSum(map);
		// 判断余额是否修改成功，如果不成功，则删除资金记录
		if (updateSum <= 0) {
			// 根据id删除资金记录
			memberService.deleteFundRecord(memberFundRecord.getFrid());
			// 修改失败
			result.setCode(MessageUtil.UPDATE_ERROR);
			return result;
		}
		return result;
	}

	/**
	 * 20180508在线存款
	 * 
	 * @param money 存款金额
	 * @param discountsMoeny 优惠金额
	 * @param currency 货币
	 * @param currencyCount 货币个数
	 * @return
	 */
	@RequestMapping(value = "member-deposit", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult memberDeposit(String money, String discountsMoeny, String currency ,String currencyCount) {
		String token = request.getHeader("token");
		ObjectResult result = new ObjectResult();
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
		// 获取缓存中的会员id
		String mid = member.getMid();
		// 生成订单号
		String number = BeanLoad.getNumber();
		// 添加资金交易记录
		MemberFundRecord memberFundRecord = new MemberFundRecord();
		memberFundRecord.setFrid(BeanLoad.getId());// 资金交易id
		memberFundRecord.setMid(mid);// 会员id
		memberFundRecord.setNumber(number);// 订单号
		memberFundRecord.setCurrency(currency);// 货币类型
		memberFundRecord.setCurrency_count(currencyCount);// 货币数量
		memberFundRecord.setRecord("0");// 记录类型
		memberFundRecord.setMoney(money);// 交易金额
		memberFundRecord.setDiscounts(discountsMoeny);// 优惠金额
		memberFundRecord.setState("0");// 状态
		int addFundRecord = memberService.addFundRecord(memberFundRecord);
		// 判断是否添加成功
		if (addFundRecord <= 0) {
			// 添加失败
			result.setCode(MessageUtil.INSERT_ERROR);
			return result;
		}
		result.setResult(number);
		return result;
	}

	/**
	 * 20180516支付金额
	 * 
	 * @param member
	 */
	@RequestMapping(value = "member-pay", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult memberPay(String number) {
		String token = request.getHeader("token");
		ObjectResult result = new ObjectResult();
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
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("number", number);// 订单号
		List<MemberFundRecord> fundRecordList = memberService.queryFundRecord(map);
		MemberFundRecord fundRecord = fundRecordList.get(0);
		// 获取相差的时间
		long  distance = new Date().getTime() - fundRecord.getTime().getTime();
		// 将时间转换为多少秒
		long second = ( distance % (1000 * 24 * 60 * 60) % (1000 * 60 * 60) % (1000 * 60)) / 1000;
		// 超过15分钟未完成支付则时间超时
		int time = 15 * 60;
		if(second > time) {
			//时间超时
			result.setCode(MessageUtil.TIME_OVERTIME);
			return result;
		}
		map.put("state",1);// 状态
		// 根据订单号修改状态
		int updateFundRecord = memberService.updateFundRecord(map);
		if (updateFundRecord <= 0) {
			result.setCode(MessageUtil.UPDATE_ERROR);
			return result;
		}
		return result;
	}
	
	/**
	 * 20180509提现记录/充值记录
	 * 
	 * @param record 存取款记录
	 * @param beginTime 开始时间
	 * @param endTime 结束时间
	 * @param type 充值类型
	 * @param state 状态
	 * @param pageNo 当前页
	 * @param pageSize 每页显示条数
	 * @return
	 */
	@RequestMapping(value = "member-record", method = RequestMethod.POST)
	@ResponseBody
	public Page<Map<String, Object>> allRecord(String record, String beginTime, String endTime, String type,
			String state, Integer pageNo, Integer pageSize) {
		String token = request.getHeader("token");
		Page<Map<String, Object>> page = new Page<Map<String, Object>>();
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			// 空值
			page.setCode(MessageUtil.NULL_ERROR);
			return page;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			// token过期
			page.setCode(MessageUtil.TOKEN_OVERDUE);
			return page;
		}
		// 0表示充值记录，1表示提现记录
		if (!record.equals("0") && !record.equals("1")) {
			// 参数错误
			page.setCode(MessageUtil.PARAMETER_ERROR);
			return page;
		}
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		// 获取缓存中的会员id
		String mid = member.getMid();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mid", mid);// 会员id
		map.put("beginTime", StringUtils.isBlank(beginTime) ? null : beginTime);// 开始时间
		map.put("endTime", StringUtils.isBlank(endTime) ? null : endTime);// 结束时间
		map.put("type", type);// 充值类型
		map.put("state", state);// 状态
		map.put("pageSize", pageSize);// 每页显示多少条
		map.put("beginIndex", (pageNo - 1) * pageSize);// 下标
		map.put("pageNo", pageNo);// 当前页
		map.put("record", record);// 记录类型
		page = memberService.listFundRecord(map);
		if (page.getResult() == null) {
			// 未找到数据
			page.setCode(MessageUtil.DATA_NOT);
			return page;
		}
		return page;
	}

	/**
	 * 20180502会员下注
	 * 
	 * @param url 
	 * @param gid 
	 * @param ratio 赔率字段
	 * @param ratioData 赔率数据
	 * @param money 金额
	 * @param bet 下注对象，主客场
	 * @param betType 下注类型，足篮球
	 * @param iorRatio 比率字段
	 * @return
	 */
	@RequestMapping(value = "bet-member", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult betMember(String url, String gid, String ratio, String ratioData, String money, String bet,
			String betType, String iorRatio) {
		String token = request.getHeader("token");
		ObjectResult result = new ObjectResult();
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
		// 获取缓存中的会员id
		String mid = member.getMid();
		// 根据url地址获取所有数据
		String stringAll = JsoupUtil.getStringAll(url);
		if (StringUtils.isBlank(stringAll)) {
			// 网络连接失败
			result.setCode(MessageUtil.NETWORK_CONNECTION);
			return result;
		}
		// 根据所有数据进行切割获取数据
		List<Map<String, String>> list = JsoupUtil.listFieldAndData(stringAll);
		if (list == null) {
			// 空值
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		// 获取地址中与gid匹配的数据
		Map<String, String> mapData = JsoupUtil.getMapData(list, gid);
		if (mapData == null) {
			// 赛事结束不能下注
			result.setCode(MessageUtil.LEAGUE_END);
			return result;
		}
		// 查找数据判断赔率是否为空
		String mapRatio = mapData.get(ratio);
		if (StringUtils.isBlank(mapRatio)) {
			// 未找到匹配的数据
			result.setCode(MessageUtil.DATA_NOT_FOUND);
			return result;
		}
		// 根据查找的赔率判断误差，误差不能超过正负0.1
		Float margin = Float.parseFloat(mapRatio) - Float.parseFloat(ratioData);
		if (margin > 0.1 || margin < (-0.1)) {
			// 赔率误差过大
			result.setCode(MessageUtil.DATA_ERROR_OVERSIZE);
			return result;
		}
		Map<String, Object> memberMap = new HashMap<String, Object>();
		memberMap.put("mid", mid);
		// 查询会员余额是否够下注
		List<Member> memberList =  memberService.queryMember(memberMap);
		if (memberList == null || memberList.size() > 1) {
			System.err.println("mid查询不到数据");
			// 数据匹配错误
			result.setCode(MessageUtil.DATA_NOT_FOUND);
			return result;
		}
		member = memberList.get(0);
		if (Float.parseFloat(member.getSum()) < Float.parseFloat(money)) {
			// 超过自己的余额
			result.setCode(MessageUtil.MONEY_EXCEED);
			return result;
		}

		Map<String, List<String>> fieldExplain = JsoupUtil.getFieldExplain(stringAll);
		// 定义场次与比分
		String occasion = null, score = null;
		// 判断是否是全场
		if (fieldExplain.get("fullList").contains(ratio)) {
			occasion = "全场";
		} else if (fieldExplain.get("hrList").contains(ratio)) {
			occasion = "半场";
		}
		// 定义比率类型
		String ratioType = JsoupUtil.ratioType(betType).get(ratio);
		// 判断是否是滚球
		if (betType.equals("FT")) {
			betType = url.contains("rtype=re") ? "REFT" : "RFT";
			score = betType.equals("REFT") ? mapData.get("score_h") + " - " + mapData.get("score_c") : null;
		} else if (betType.equals("BK")) {
			betType = url.contains("rtype=re") ? "REBK" : "RBK";
			score = betType.equals("REBK") ? mapData.get("score_h") + " - " + mapData.get("score_c") : null;
			// 判断是否有比分，有则是滚球
			if (StringUtils.isNotBlank(mapData.get("score_h")) || StringUtils.isNotBlank(mapData.get("scoreH"))) {
				occasion = "半场";
			}
		} else {
			// 参数错误
			result.setCode(MessageUtil.PARAMETER_ERROR);
			return result;
		}
		// 计算有效金额
		float validMoney = Integer.parseInt(money) * Float.parseFloat(ratioData);
		// 获取比率
		if (StringUtils.isBlank(iorRatio) || mapData.get(iorRatio).equals("单") || mapData.get(iorRatio).equals("双")) {
			iorRatio = null;
		} else {
			// 获取比率
			iorRatio = mapData.get(iorRatio);
			// 判断比率是否存在字母
			iorRatio = iorRatio.contains("O") ? iorRatio.replace("O", "") : iorRatio;
			iorRatio = iorRatio.contains("U") ? iorRatio.replace("U", "") : iorRatio;
		}
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
		memberSingleNote.setScore(score); // 设置比分
		memberSingleNote.setOccasion(occasion);// 设置场次
		memberSingleNote.setIor_type(ratioType);// 设置比率类型
		memberSingleNote.setIor_ratio(iorRatio);// 设置比率
		memberSingleNote.setBet(bet);// 设置下注对象
		memberSingleNote.setBet_type(betType);// 设置下注类型
		memberSingleNote.setLeague(league);// 设置联赛
		memberSingleNote.setMoney(money);// 设置下注金额
		memberSingleNote.setValid_money(String.format("%.2f", validMoney));// 设置有效金额
		int betMember = memberService.betMember(memberSingleNote);
		// 判断数据是否添加成功
		if (betMember <= 0) {
			// 添加失败
			result.setCode(MessageUtil.INSERT_ERROR);
			return result;
		}
		// 修改账户余额
		Float sum = Float.parseFloat(member.getSum()) - Float.parseFloat(money);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mid", mid);// 设置mid
		map.put("sum", String.valueOf(sum));// 设置余额
		// 根据mid修改余额
		int updateSum = memberService.updateSum(map);
		// 判断余额是否修改成功，如果不成功则删除添加的下注订单
		if (updateSum <= 0) {
			// 修改失败
			result.setCode(MessageUtil.UPDATE_ERROR);
			memberService.deleteSingleNote(snid);
			return result;
		}
		return result;
	}
	
	/**
	 * 20180517注单记录
	 * 
	 * @param keyword 关键字查询
	 * @param betType 下注类型足球|篮球
	 * @param beginTime 开始时间
	 * @param endTime 结束时间
	 * @param state 结算状态
	 * @param winLose 输赢状态
	 * @param pageNo 当前页
	 * @param pageSize 每页显示条数
	 * @return
	 */
	@RequestMapping(value = "single-note", method = RequestMethod.POST)
	@ResponseBody
	public Page<Map<String, Object>> allsingleNote(String keyword, String betType, String beginTime, String endTime,
			String state,String winLose, Integer pageNo, Integer pageSize) {
		String token = request.getHeader("token");
		Page<Map<String, Object>> page = new Page<Map<String, Object>>();
		Cache cache = getCache();
		// 判断token是否为空
		if (StringUtils.isBlank(token)) {
			// 空值
			page.setCode(MessageUtil.NULL_ERROR);
			return page;
		}
		// 判断token是否过期
		if (cache.get(token) == null) {
			// token过期
			page.setCode(MessageUtil.TOKEN_OVERDUE);
			return page;
		}
		// 获取缓存中的数据
		Member member = (Member) cache.get(token).get();
		// 获取缓存中的会员id
		String mid = member.getMid();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mid", mid);// 会员id
		map.put("beginTime", StringUtils.isBlank(beginTime) ? null : beginTime);// 开始时间
		map.put("endTime", StringUtils.isBlank(endTime) ? null : endTime);// 结束时间
		map.put("keyword", keyword);// 关键字查询
		map.put("betType", betType);// 下注类型足球|篮球
		map.put("state", state);// 结算状态
		map.put("winLose", winLose);// 输赢状态
		map.put("pageSize", pageSize);// 每页显示多少条
		map.put("beginIndex", (pageNo - 1) * pageSize);// 下标
		map.put("pageNo", pageNo);// 当前页
		page = memberService.listSingleNote(map);
		if (page.getResult() == null) {
			// 未找到数据
			page.setCode(MessageUtil.DATA_NOT);
			return page;
		}
		return page;
	}
	
	/**
	 * 注单结算
	 * 
	 * @return
	 * @throws ParseException
	 */
	public String singleNoteAccount() throws ParseException {
		// 定义时间格式
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// 将时间类型转换为字符串类型
		String date = format.format(new Date());
		// 指定时间查询
		String sj = "2018-05-15";
		// 将字符串转换为date类型
		Date parse = format.parse(sj);
		// 创建map对象
		Map<String, Object> map = new HashMap<String, Object>();
		// 往map集合中添加数据
		map.put("state", "0");
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
				boolean stateUpdate = resultGame(singleNote, listResult);
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
				boolean stateUpdate = resultGame(singleNote, listResult);
				if (stateUpdate) {
					System.out.println("注单结算成功");
				}
			}
			continue;
		}
		return MessageUtil.SUCCESS;
	}

	/**
	 * 足球篮球结算
	 * 
	 * @param singleNote 注单DTO
	 * @param listResult 足球篮球list数据
	 * @return
	 */
	public boolean resultGame(SingleNoteDTO singleNote, List<Object> listResult) {
		Map<String, Object> memberMap = new HashMap<String, Object>();
		boolean stateUpdate;
		String mid = singleNote.getMid();// 获取会员id
		memberMap.put("mid", mid);
		// 根据会员id查询会员所有信息
		List<Member> memberList = memberService.queryMember(memberMap);
		if (memberList == null || memberList.size() > 1) {
			System.err.println("mid查询不到数据");
		}
		Member member = memberList.get(0);
		Float memberByMoney = Float.parseFloat(member.getSum());// 得到账户余额
		Float money = Float.parseFloat(singleNote.getMoney());// 得到下注金额
		Float validMoney = Float.parseFloat(singleNote.getValid_money());// 得到有效金额
		Float sum = null;
		String bet = (String) listResult.get(0);// 主客场
		// 如果赛事腰斩了实行的操作
		if (bet.equals("赛事腰斩")) {
			// 返回下注金额到账户余额
			sum = memberByMoney + money;
			stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
			return stateUpdate ? true : false;
		}
		// 获取客场比分
		Integer firstFloat = (Integer) listResult.get(1);
		// 获取主场比分
		Integer lastFloat = (Integer) listResult.get(2);
		// 获取比分差
		Integer score = (Integer) listResult.get(3);
		// 获取下注比率
		String ratio = singleNote.getIor_ratio();
		if (JsoupUtil.getParticipantType().contains(singleNote.getIor_type())) {
			if (singleNote.getIor_type().equals("单")) {
				// 全输
				if((firstFloat + lastFloat) % 2 == 0) {
					sum = memberByMoney;
					stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
					return stateUpdate ? true : false;
				}
				// 全赢
				if((firstFloat + lastFloat) % 2 == 1) {
					sum = memberByMoney + validMoney;
					stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
					return stateUpdate ? true : false;
				}
			}
			if (singleNote.getIor_type().equals("双")) {
				// 全赢
				if((firstFloat + lastFloat) % 2 == 0) {
					sum = memberByMoney + validMoney;
					stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
					return stateUpdate ? true : false;
				}
				// 全输
				if((firstFloat + lastFloat) % 2 == 1) {
					sum = memberByMoney;
					stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
					return stateUpdate ? true : false;
				}
			}
			if (singleNote.getIor_type().equals("大")) {
				// 根据比率计算大输赢
				if (ratio.contains("/") && ratio.contains(".")) {
					String[] split = ratio.split("/");
					if (split[0].contains(".")) {
						// 既不是大球也不是小球，买大球的人赢一半
						if((firstFloat + lastFloat) == Integer.parseInt(split[1])) {
							sum = memberByMoney + money + (validMoney / 2);
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
						// 全输
						if((firstFloat + lastFloat) < Integer.parseInt(split[1])) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if((firstFloat + lastFloat) > Integer.parseInt(split[1])) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
					} else if (split[1].contains(".")) {
						// 既不是大球也不是小球，买大球的人输一半
						if((firstFloat + lastFloat) == Integer.parseInt(split[0])) {
							sum = memberByMoney + (money / 2);
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
						// 全输
						if((firstFloat + lastFloat) < Integer.parseInt(split[0])) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if((firstFloat + lastFloat) > Integer.parseInt(split[0])) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
					}
				} else if (ratio.contains(".") && !ratio.contains("/")) {
					// 全赢
					if((firstFloat + lastFloat) > Float.parseFloat(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
						return stateUpdate ? true : false;
					}
					// 全输
					if((firstFloat + lastFloat) < Float.parseFloat(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
						return stateUpdate ? true : false;
					}
				} else if (!ratio.contains(".") && !ratio.contains("/")) {
					// 全赢
					if((firstFloat + lastFloat) > Integer.parseInt(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
						return stateUpdate ? true : false;
					}
					// 全输
					if((firstFloat + lastFloat) < Integer.parseInt(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
						return stateUpdate ? true : false;
					}
					// 不输不赢
					if((firstFloat + lastFloat) == Integer.parseInt(ratio)) {
						sum = memberByMoney + money;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"0");
						return stateUpdate ? true : false;
					}
				}
			}
			if (singleNote.getIor_type().equals("小")) {
				// 根据比率计算小输赢
				if (ratio.contains("/") && ratio.contains(".")) {
					String[] split = ratio.split("/");
					if (split[0].contains(".")) {
						// 既不是大球也不是小球，买小球的人输一半
						if((firstFloat + lastFloat) == Integer.parseInt(split[1])) {
							sum = memberByMoney + (money / 2);
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
						// 全输
						if((firstFloat + lastFloat) > Integer.parseInt(split[1])) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if((firstFloat + lastFloat) < Integer.parseInt(split[1])) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
					} else if (split[1].contains(".")) {
						// 既不是大球也不是小球，买小球的人赢一半
						if((firstFloat + lastFloat) == Integer.parseInt(split[1])) {
							sum = memberByMoney + money + (validMoney / 2);
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
						// 全输
						if((firstFloat + lastFloat) > Integer.parseInt(split[1])) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if((firstFloat + lastFloat) < Integer.parseInt(split[1])) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
					}
				} else if (ratio.contains(".") && !ratio.contains("/")) {
					// 全赢
					if((firstFloat + lastFloat) < Float.parseFloat(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
						return stateUpdate ? true : false;
					}
					// 全输
					if((firstFloat + lastFloat) > Float.parseFloat(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
						return stateUpdate ? true : false;
					}
				} else if (!ratio.contains(".") && !ratio.contains("/")) {
					// 全赢
					if((firstFloat + lastFloat) < Integer.parseInt(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
						return stateUpdate ? true : false;
					}
					// 全输
					if((firstFloat + lastFloat) > Integer.parseInt(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
						return stateUpdate ? true : false;
					}
					// 不输不赢
					if((firstFloat + lastFloat) == Integer.parseInt(ratio)) {
						sum = memberByMoney + money;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"0");
						return stateUpdate ? true : false;
					}
				}else {
					return false;
				}
			}
			if (singleNote.getIor_type().equals("单大")) {
				if(singleNote.getBet().equals("H")) {
					if(ratio.contains(".")) {
						// 全赢
						if(lastFloat > Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
						// 全输
						if(lastFloat < Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
					} else if(!ratio.contains(".")) {
						// 全赢
						if(lastFloat > Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
						// 全输
						if(lastFloat < Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if(lastFloat == Integer.parseInt(ratio)) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"0");
							return stateUpdate ? true : false;
						}
					}
				}
				if(singleNote.getBet().equals("C")) {
					if(ratio.contains(".")) {
						// 全赢
						if(firstFloat > Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
						// 全输
						if(firstFloat < Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
					} else if(!ratio.contains(".")) {
						// 全赢
						if(firstFloat > Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
						// 全输
						if(firstFloat < Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if(firstFloat == Integer.parseInt(ratio)) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"0");
							return stateUpdate ? true : false;
						}
					}
				}
			}
			if (singleNote.getIor_type().equals("单小")) {
				if(singleNote.getBet().equals("H")) {
					if(ratio.contains(".")) {
						// 全赢
						if(lastFloat < Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
						// 全输
						if(lastFloat > Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
					} else if(!ratio.contains(".")) {
						// 全赢
						if(lastFloat < Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
						// 全输
						if(lastFloat > Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if(lastFloat == Integer.parseInt(ratio)) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"0");
							return stateUpdate ? true : false;
						}
					}
				}
				if(singleNote.getBet().equals("C")) {
					if(ratio.contains(".")) {
						// 全赢
						if(firstFloat < Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
						// 全输
						if(firstFloat > Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
					} else if(!ratio.contains(".")) {
						// 全赢
						if(firstFloat < Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
							return stateUpdate ? true : false;
						}
						// 全输
						if(firstFloat > Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if(firstFloat == Integer.parseInt(ratio)) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"0");
							return stateUpdate ? true : false;
						}
					}
				}
			}
		}
		// 如果赛事下注赢实行的操作
		if (bet.equals(singleNote.getBet())) {
			if (singleNote.getIor_type().equals("独赢")) {
				// 下注金额与有效金额都返回账户余额
				sum = memberByMoney + validMoney;
				stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
				return stateUpdate ? true : false;
			}
			if (singleNote.getIor_type().equals("让球")) {
				// 根据比率计算让球输赢
				if (ratio.contains("/") && ratio.contains(".")) {
					String[] split = ratio.split("/");
					if (split[0].contains(".")) {
						// 赢一半
						if(score - Integer.parseInt(split[1]) == 0) {
							sum =  memberByMoney + money + (validMoney / 2);
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if(score < Integer.parseInt(split[1])) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if(score > Integer.parseInt(split[1])) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
					} else if (split[1].contains(".")) {
						// 输一半
						if(score - Integer.parseInt(split[0]) == 0) {
							sum =  memberByMoney + (money / 2);
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全输
						if(score < Integer.parseInt(split[0])) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if(score > Integer.parseInt(split[0])) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
					}
				} else if (ratio.contains(".") && !ratio.contains("/")) {
					// 全输
					if(score < Float.parseFloat(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
						return stateUpdate ? true : false;
					}
					// 全赢
					if(score > Float.parseFloat(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
						return stateUpdate ? true : false;
					}
				} else if (!ratio.contains(".") && !ratio.contains("/")) {
					// 全输
					if(score < Integer.parseInt(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
						return stateUpdate ? true : false;
					}
					// 全赢
					if(score > Integer.parseInt(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
						return stateUpdate ? true : false;
					}
					// 不输不赢
					if(score - Integer.parseInt(ratio) == 0) {
						sum =  memberByMoney + money;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
						return stateUpdate ? true : false;
					}
				}
			}
			if (singleNote.getIor_type().equals("让分")) {
				if(ratio.contains(".")) {
					// 全赢
					if(score > Float.parseFloat(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
						return stateUpdate ? true : false;
					}
					// 全输
					if(score < Float.parseFloat(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
						return stateUpdate ? true : false;
					}
				} else if(!ratio.contains(".")) {
					// 全赢
					if(score > Integer.parseInt(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"1");
						return stateUpdate ? true : false;
					}
					// 全输
					if(score < Integer.parseInt(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
						return stateUpdate ? true : false;
					}
					// 不输不赢
					if(score - Integer.parseInt(ratio) == 0) {
						sum =  memberByMoney + money;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
						return stateUpdate ? true : false;
					}
				}
			}
		}
		// 如果赛事下注输实行的操作
		if (!bet.equals(singleNote.getBet())) {
			// 返回下注金额到账户余额
			sum = memberByMoney;
			stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote,"-1");
			return stateUpdate ? true : false;
		}
		return false;
	}
	/**
	 * 修改状态
	 * 
	 * @param mid
	 * @param sum
	 * @param memberByMoney
	 * @param singleNote
	 * @param winLose
	 * @return
	 */
	public boolean stateUpdate(String mid, Float sum, Float memberByMoney, Float money, SingleNoteDTO singleNote ,String winLose) {
		Map<String, Object> singleNoteMap = new HashMap<String, Object>();
		// 下注金额与有效金额都返回账户余额
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mid", mid);// 设置mid
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
		singleNoteMap.put("state", "1");
		singleNoteMap.put("winLose", winLose);
		money = sum - (memberByMoney + money);
		String dealMoney = null;
		if(money > 0) {
			dealMoney = "+" + String.valueOf(money);
		}else {
			dealMoney = String.valueOf(money);
		}
		singleNoteMap.put("dealMoney", dealMoney);
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
	
	/**
	 * 指定一个ehcache缓存
	 * 
	 * @return
	 */
	private Cache getCache() {
		Cache cache = manager.getCache("memberCache");
		return cache;
	}

	/**
	 * Member 放入 Map<String, Object> 只保留前端需要的字段
	 * 
	 * @param member
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
