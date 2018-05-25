package com.springboot.lottery.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
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
	 * @return
	 */
	@RequestMapping(value = "add-member", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult addMemberAll(Member member) {
		ObjectResult result = new ObjectResult();
		// 根据请求头x-forwarded-for信息获取IP地址
		String ipAddress = request.getHeader("x-forwarded-for");
		// 判断IP地址是否为unknown或者为空，换为请求头Proxy-Client-IP信息
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("Proxy-Client-IP");  
        }
        // 判断IP地址是否为unknown或者为空，换为请求头WL-Proxy-Client-IP信息
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getHeader("WL-Proxy-Client-IP");  
        }
        // 判断IP地址是否为unknown或者为空，换为获取客户端的IP地址的方法
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
            ipAddress = request.getRemoteAddr();
            // 判断IP是否为真实IP，如果不是则根据网卡获取配置IP
            if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")){  
                //根据网卡取本机配置的IP  
                InetAddress inet=null;  
                try {  
                    inet = InetAddress.getLocalHost();  
                } catch (UnknownHostException e) {
                	// IP地址获取失败！
                	result.setCode(MessageUtil.IP_ADDRESS);
                	return result;
                }  
                ipAddress= inet.getHostAddress();  
            }  
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ipAddress!=null && ipAddress.length()>15){   
            if(ipAddress.indexOf(",")>0){  
                ipAddress = ipAddress.substring(0,ipAddress.indexOf(","));  
            }  
        }else {
        	// IP地址获取失败！
        	result.setCode(MessageUtil.IP_ADDRESS);
        	return result;
        }  
		String name = member.getName();
		String password = member.getPassword();
		String bankPassword = member.getBank_password();
		// 判断用户名长度
		if (name.length() > 10 && name.length() < 6) {
			result.setCode(MessageUtil.PARAMETER_ERROR);
			return result;
		}
		// 判断密码长度
		if (password.length() > 12 && password.length() < 6) {
			result.setCode(MessageUtil.PARAMETER_ERROR);
			return result;
		}
		// 判断密码长度
		if (bankPassword.length() != 4) {
			result.setCode(MessageUtil.PARAMETER_ERROR);
			return result;
		}
		Cache cache = getCache();
		String mid = BeanLoad.getId();
		String role = member.getRole();
		member.setAddress(ipAddress);// 设置IP地址
		member.setMid(mid);// 设置主键id
		member.setSum("0");// 余额默认设置为0
		member.setRole(role = StringUtils.isBlank(role) ? "0" : role);// 设置权限
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
	 * @param name
	 *            用户名
	 * @return
	 */
	@RequestMapping(value = "verify-name", method = RequestMethod.GET)
	@ResponseBody
	public ObjectResult verifyName(String name) {
		ObjectResult result = new ObjectResult();
		String verifyMember = memberService.verifyMember(name);
		if(verifyMember == null) {
			return result;
		}
		// 用户名已存在
		result.setCode(MessageUtil.NAME_EXIST);
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
	public ObjectResult loginMember(Member member) {
		Cache cache = getCache();
		String token = request.getHeader("token");
		// 判断缓存是否存在，如果存在则移除
		if (StringUtils.isNotEmpty(token)) {
			cache.evict(token);
		}
		// 获取用户名
		String name = member.getName();
		// 获取登录密码
		String password = member.getPassword();
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectResult result = new ObjectResult();
		map.put("name", name);
		List<Member> list = memberService.queryMember(map);
		// 判断list对象是否为空
		if (list == null || list.size() == 0) {
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
		String role = member.getRole();
		if(!role.equals("0")) {
			// 会员不存在
			result.setCode(MessageUtil.MEMBER_NOT);
			return result;
		}
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
	 * 20180522管理员登录
	 * 
	 * @param member
	 * @return
	 */
	@RequestMapping(value = "login-admin", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult loginAdmin(Member member) {
		Cache cache = getCache();
		String token = request.getHeader("token");
		// 判断缓存是否存在，如果存在则移除
		if (StringUtils.isNotEmpty(token)) {
			cache.evict(token);
		}
		// 获取用户名
		String name = member.getName();
		// 获取登录密码
		String password = member.getPassword();
		Map<String, Object> map = new HashMap<String, Object>();
		ObjectResult result = new ObjectResult();
		map.put("name", name);
		List<Member> list = memberService.queryMember(map);
		
		// 判断list对象是否为空
		if (list == null || list.size() == 0) {
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
		String role = member.getRole();
		if(!role.equals("1")) {
			// 会员不存在
			result.setCode(MessageUtil.MEMBER_NOT);
			return result;
		}
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
		List<Member> memberList = memberService.queryMember(map);
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
	 * 
	 * @param currency
	 *            货币
	 * @param record
	 *            存取款
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
		if (exchange == null) {
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
		if (record.equals("0")) {
			// 记录类型
			map.put("record", record);
			// 会员id
			map.put("mid", mid);
			// 根据会员id与记录类型查询是否有记录
			int total = memberService.loadFundRecordTotal(map);
			if (total == 0) {
				map.put("total", 0);
			} else {
				map.put("total", 1);
			}
			// 移除不需要返回的值
			map.remove("record");
			map.remove("mid");
			result.setResult(map);
			return result;
		} else if (record.equals("1")) {
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
	 * @param money
	 *            取款金额
	 * @param currency
	 *            货币
	 * @param phone
	 *            手机号
	 * @param address
	 *            钱包地址
	 * @param remark
	 *            备注
	 * @param password
	 *            取款密码
	 * @param currencyCount
	 *            货币个数
	 * @return
	 */
	@RequestMapping(value = "member-withdrawn", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult memberWithdrawn(String money, String currency, String phone, String address, String remark,
			String password) {
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
		// 获取汇率
		String exchange = JsoupUtil.getExchange(currency);
		if (exchange == null) {
			// 网络连接失败
			result.setCode(MessageUtil.NETWORK_CONNECTION);
			return result;
		}
		// 利用字符串切割和替换，改变为纯数字
		String[] split = exchange.split(" ");
		String replace = split[0].replace(",", "");
		// 换算金额
		Float currencyCount = Float.parseFloat(money) / Float.parseFloat(replace);
		// 根据会员id获取余额
		Map<String, Object> map = new HashMap<String, Object>();
		// 设置会员id
		map.put("mid", mid);
		List<Member> memberList = memberService.queryMember(map);
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
		memberFundRecord.setCurrency_count(String.format("%.3f", currencyCount));// 货币数量
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
		map.put("sum",String.format("%.2f", Float.parseFloat(sum) - Float.parseFloat(money)));
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
	 * @param money
	 *            存款金额
	 * @param discountsMoeny
	 *            优惠金额
	 * @param currency
	 *            货币
	 * @param currencyCount
	 *            货币个数
	 * @return
	 */
	@RequestMapping(value = "member-deposit", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult memberDeposit(String currency, String currencyCount) {
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
		String exchange = JsoupUtil.getExchange(currency);
		if (exchange == null) {
			// 网络连接失败
			result.setCode(MessageUtil.NETWORK_CONNECTION);
			return result;
		}
		// 利用字符串切割和替换，改变为纯数字
		String[] split = exchange.split(" ");
		String replace = split[0].replace(",", "");
		// 换算金额
		Float money = Float.parseFloat(replace) * Float.parseFloat(currencyCount);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("record", "0");
		//查询充值是否有记录
		int total = memberService.loadFundRecordTotal(map);
		Float discounts = null;
		// 判断是否存在首充
		if(total > 0) {
			discounts = money * 0.01f;
		}else {
			discounts = money * 0.01f + 188f;
		}
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
		memberFundRecord.setMoney(String.format("%.2f", money));// 交易金额
		memberFundRecord.setDiscounts(String.format("%.2f", discounts));// 优惠金额
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
		// 获得两个时间的毫秒时间差异
		long distance = new Date().getTime() - fundRecord.getTime().getTime();
		// 计算差多少天
		long day = distance / (1000 * 24 * 60 * 60);
		// 计算差多少小时
		long hour = (distance % (1000 * 24 * 60 * 60)) / (1000 * 60 * 60);
		// 计算差多少分
		long min = (distance % (1000 * 24 * 60 * 60) % (1000 * 60 * 60)) / (1000 * 60);
		// 计算差多少秒
		long second = (distance % (1000 * 24 * 60 * 60) % (1000 * 60 * 60) % (1000 * 60)) / 1000;
		// 计算总共相差多少秒
		long time = (day * 60 * 60 * 24) + (hour * 60 * 60) + (min * 60 ) + second;
		// 超过一天未完成支付则时间超时
		if (time > 60 * 60 * 24) {
			// 时间超时
			result.setCode(MessageUtil.TIME_OVERTIME);
			return result;
		}
		map.put("state", 1);// 状态
		// 根据订单号修改状态
		int updateFundRecord = memberService.updateFundRecord(map);
		if (updateFundRecord <= 0) {
			result.setCode(MessageUtil.UPDATE_ERROR);
			return result;
		}
		return result;
	}

	/**
	 * 20180523充值/取款结算
	 * 
	 * @param number 订单号
	 * @param state 状态
	 * @param record 记录类型：充值、取款
	 * @param mid 会员id
	 * @param money 金钱
	 * @return
	 */
	@RequestMapping(value = "alter/fund-record", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult alterFundRecord(String number, String state, String record, String mid, String money) {
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
		map.put("state", state);// 状态
		// 根据订单号修改状态
		int updateFundRecord = memberService.updateFundRecord(map);
		if (updateFundRecord <= 0) {
			result.setCode(MessageUtil.UPDATE_ERROR);
			return result;
		}
		Map<String, Object> memberMap = new HashMap<String, Object>();
		memberMap.put("mid", mid);
		// 根据mid获取会员所有信息
		List<Member> queryMember = memberService.queryMember(memberMap);
		if(queryMember.size() != 1 || queryMember == null) {
			// 空值
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		Member member = queryMember.get(0);
		// 如果充值成功向账户中添加金额
		if(record.equals("0") && state.equals("2")) {
			String sum = member.getSum();
			memberMap.put("sum", String.format("%.2f", Float.valueOf(sum) + Float.valueOf(money)));
			int updateSum = memberService.updateSum(memberMap);
			if(updateSum > 0) {
				return result;
			}else {
				result.setCode(MessageUtil.UPDATE_ERROR);
				return result;
			}
		}
		// 如果提款失败向账户返回金额
		if(record.equals("1") && (state.equals("-1") || state.equals("-2"))) {
			String sum = member.getSum();
			memberMap.put("sum", String.format("%.2f", Float.valueOf(sum) + Float.valueOf(money)));
			int updateSum = memberService.updateSum(memberMap);
			if(updateSum > 0) {
				return result;
			}else {
				result.setCode(MessageUtil.UPDATE_ERROR);
				return result;
			}
		}
		return result;
	}
	
	/**
	 * 20180509提现记录/充值记录
	 * 
	 * @param record
	 *            存取款记录
	 * @param beginTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @param type
	 *            充值类型
	 * @param state
	 *            状态
	 * @param pageNo
	 *            当前页
	 * @param pageSize
	 *            每页显示条数
	 * @return
	 */
	@RequestMapping(value = "member-record", method = RequestMethod.POST)
	@ResponseBody
	public Page<Map<String, Object>> allRecord(String record, String beginTime, String endTime, String type,
			String state, Integer pageNo, Integer pageSize, String keyword) {
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
		map.put("mid", mid = member.getRole().equals("0") ? mid : null);// 会员id
		map.put("beginTime", StringUtils.isBlank(beginTime) ? null : beginTime);// 开始时间
		map.put("endTime", StringUtils.isBlank(endTime) ? null : endTime);// 结束时间
		map.put("keyword", keyword);// 关键字
		map.put("type", type);// 充值类型
		map.put("state", state);// 状态
		map.put("pageSize", pageSize);// 每页显示多少条
		map.put("beginIndex", pageNo == null || pageSize == null ? null : (pageNo - 1) * pageSize);// 下标
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
	 * @param ratio
	 *            赔率字段
	 * @param ratioData
	 *            赔率数据
	 * @param money
	 *            金额
	 * @param bet
	 *            下注对象，主客场
	 * @param betType
	 *            下注类型，足篮球
	 * @param iorRatio
	 *            比率字段
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
		// 判断下注金额是否超过规定的金额
		if(Float.parseFloat(money) < 10 || Float.parseFloat(money) >10000) {
			// 超过下注的金额
			result.setCode(MessageUtil.MONEY_EXCEED);
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
		List<Member> memberList = memberService.queryMember(memberMap);
		if (memberList == null || memberList.size() != 1) {
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
		Map<String, List<String>> fieldExplain = JsoupUtil.getFieldExplain(stringAll);
		// 定义场次、比分、让球方
		String occasion = null, score = null, strong = null;
		// 判断是否是全场
		if (fieldExplain.get("fullList").contains(ratio)) {
			occasion = "全场";
			strong = mapData.get("strong");
			// 判断是否是让球
			if(ratio.equals("ior_RH") || ratio.equals("ior_RC")) {
				iorRatio = mapData.get("ratio");
			}
		} else if (fieldExplain.get("hrList").contains(ratio)) {
			occasion = "半场";
			strong = mapData.get("hstrong");
			// 判断是否是让球
			if(ratio.equals("ior_HRH") || ratio.equals("ior_HRC")) {
				iorRatio = mapData.get("hratio");
			}
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
		String league = mapData.get("league");// 获取数据的赛事
		String teamh = mapData.get("team_h");// 获取数据的主场
		String teamc = mapData.get("team_c");// 获取数据的客场
		String snid = BeanLoad.getId();// 随机生成主键id
		MemberSingleNote memberSingleNote = new MemberSingleNote();
		memberSingleNote.setSnid(snid);// 设置主键id
		memberSingleNote.setMid(mid);// 设置会员id
		memberSingleNote.setNumber(BeanLoad.getNumber());// 设置注单号
		memberSingleNote.setType("体育");// 设置类型
		memberSingleNote.setTeam_h(teamh);// 设置主场
		memberSingleNote.setTeam_c(teamc);// 设置客场
		memberSingleNote.setScore(score); // 设置比分
		memberSingleNote.setOccasion(occasion);// 设置场次
		memberSingleNote.setIor_type(ratioType);// 设置比率类型
		memberSingleNote.setIor_ratio(iorRatio);// 设置比率
		memberSingleNote.setBet(bet);// 设置下注对象
		memberSingleNote.setBet_type(betType);// 设置下注类型
		memberSingleNote.setStrong(strong);// 让球方
		memberSingleNote.setState("0");// 设置状态
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
		map.put("sum", String.format("%.2f", sum));// 设置余额
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
	 * @param keyword
	 *            关键字查询
	 * @param betType
	 *            下注类型足球|篮球
	 * @param beginTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @param state
	 *            结算状态
	 * @param winLose
	 *            输赢状态
	 * @param pageNo
	 *            当前页
	 * @param pageSize
	 *            每页显示条数
	 * @return
	 */
	@RequestMapping(value = "single-note", method = RequestMethod.POST)
	@ResponseBody
	public Page<Map<String, Object>> allsingleNote(String keyword, String betType, String beginTime, String endTime,
			String state, String winLose, Integer pageNo, Integer pageSize) {
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
		
		map.put("mid", mid = member.getRole().equals("0") ? mid : null);// 会员id
		map.put("beginTime", StringUtils.isBlank(beginTime) ? null : beginTime);// 开始时间
		map.put("endTime", StringUtils.isBlank(endTime) ? null : endTime);// 结束时间
		map.put("keyword", keyword);// 关键字查询
		map.put("betType", betType);// 下注类型足球|篮球
		map.put("state", state);// 结算状态
		map.put("winLose", winLose);// 输赢状态
		map.put("pageSize", pageSize);// 每页显示多少条
		map.put("beginIndex", pageNo == null || pageSize == null ? null : (pageNo - 1) * pageSize);// 下标
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
	 * 20180523注单取消
	 * 
	 * @param number 注单号
	 * @return
	 */
	@RequestMapping(value = "cancel/single-note", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult cancelSingleNote(String number) {
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
		map.put("number", number);
		// 根据snid获取注单表详细信息
		List<SingleNoteDTO> querySingleNoteDTO = memberService.querySingleNoteDTO(map);
		if(querySingleNoteDTO.size() != 1 || querySingleNoteDTO == null) {
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		SingleNoteDTO singleNoteDTO = querySingleNoteDTO.get(0);
		if(!singleNoteDTO.getState().equals("0")) {
			// 注单取消错误
			result.setCode(MessageUtil.SINGLE_NOTE_ERROR);
			return result;
		}
		String sum = singleNoteDTO.getSum();// 获取账户余额
		String money = singleNoteDTO.getMoney();// 获取下注金额
		String snid = singleNoteDTO.getSnid();// 获取注单id
		Map<String, Object> singleNoteMap = new HashMap<String, Object>();
		map.put("mid", singleNoteDTO.getMid());// 设置会员id
		map.put("sum",String.format("%.2f", Float.parseFloat(sum) + Float.parseFloat(money)));// 设置余额
		// 根据mid修改余额
		int updateSum = memberService.updateSum(map);
		if (updateSum <= 0) {
			result.setCode(MessageUtil.UPDATE_ERROR);
			return result;
		}
		// 根据snid修改数据
		singleNoteMap.put("snid", snid);
		// 往map里添加需要修改的字段
		singleNoteMap.put("state", "1");
		singleNoteMap.put("winLose", "0");
		singleNoteMap.put("dealMoney", "0");
		// 根据snid修改注单状态
		int singleNoteAccount = memberService.singleNoteAccount(singleNoteMap);
		// 如果注单表修改失败则把账户余额修改回去
		if (singleNoteAccount <= 0) {
			map.put("sum", sum);// 设置余额
			memberService.updateSum(map);// 修改账户余额
			result.setCode(MessageUtil.UPDATE_ERROR);
			return result;
		} 
		return result;
	}
	
	/**
	 * 20180523注单结算
	 * 
	 * @param number 注单号
	 * @param accident 意外情况：赛事腰折
	 * @param fullTeamh 全场主场得分
	 * @param fullTeamc 全场客场得分
	 * @param hrTeamh 半场主场得分
	 * @param hrTeamc 半场客场得分
	 * @return
	 */
	@RequestMapping(value = "account/single-note", method = RequestMethod.POST)
	@ResponseBody
	public ObjectResult accountSingleNote(String number, String accident, String fullTeamh, String fullTeamc,
			String hrTeamh, String hrTeamc) {
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
		map.put("number", number);
		// 根据snid获取注单表详细信息
		List<SingleNoteDTO> querySingleNoteDTO = memberService.querySingleNoteDTO(map);
		if(querySingleNoteDTO.size() != 1 || querySingleNoteDTO == null) {
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		SingleNoteDTO singleNoteDTO = querySingleNoteDTO.get(0);
		
		String state = singleNoteDTO.getState();// 获取状态
		// 判断注单是否被结算
		if(state.equals("1")) {
			// 注单结算失败
			result.setCode(MessageUtil.SINGLE_NOTE_ERROR);
			return result;
		}
		// 如果赛事腰折,则直接结算
		if(accident != null && accident.equals("赛事腰折")) {
			String mid = singleNoteDTO.getMid();// 获取mid
			Float memberByMoney = Float.parseFloat(singleNoteDTO.getSum());// 获取用户余额
			Float money = Float.parseFloat(singleNoteDTO.getMoney());// 获取下注金额
			Float sum = memberByMoney + money;
			boolean stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNoteDTO, "0");
			if(stateUpdate) {
				return result;
			}
		}
		String fullOrHrTeamc = null, fullOrHrTeamh = null;
		Integer scoreTeamc = null, scoreTeamh = null;
		String occasion = singleNoteDTO.getOccasion();// 获取注单表场次
		// 判断比赛是否是全场
		if (occasion.equals("全场")) {
			// 获取客场全场比分
			fullOrHrTeamc = fullTeamc;
			// 获取主场全场比分
			fullOrHrTeamh = fullTeamh;
		} else if (occasion.equals("半场")) {
			// 获取客场上半场比分
			fullOrHrTeamc = hrTeamc;
			// 获取主场上半场比分
			fullOrHrTeamh = hrTeamh;
		} else {
			result.setCode(MessageUtil.NULL_ERROR);
			return result;
		}
		scoreTeamc = Integer.parseInt(fullOrHrTeamc);
		scoreTeamh = Integer.parseInt(fullOrHrTeamh);
		String betType = singleNoteDTO.getBet_type();// 获取下注类型
		// 判断是否是滚球
		if (betType.equals("REFT") || betType.equals("REBK")) {
			String getScore = singleNoteDTO.getScore();// 获取比分
			String[] split = getScore.split(" - ");
			scoreTeamh = scoreTeamh - Integer.valueOf(split[0]);
			scoreTeamc = scoreTeamc - Integer.valueOf(split[1]);
		}
		String bet = null;
		Integer score = null;
		// 比较主客场比分返回结果
		if (scoreTeamc > scoreTeamh) {
			System.out.println(map.get("teamc") + "赢得整场比赛");
			bet = "C";
			score = scoreTeamc - scoreTeamh;
		} else if (scoreTeamc < scoreTeamh) {
			System.out.println(map.get("teamh") + "赢得整场比赛");
			bet = "H";
			score = scoreTeamh - scoreTeamc;
		} else if (scoreTeamc == scoreTeamh) {
			System.out.println(map.get("teamc") + "VS" + map.get("teamh") + "和局");
			bet = "N";
			score = scoreTeamh - scoreTeamc;
		}
		boolean stateUpdate = resultGame(singleNoteDTO, bet, scoreTeamh, scoreTeamc, score);
		if(stateUpdate) {
			// 注单结算失败
			result.setCode(MessageUtil.SINGLE_NOTE_ERROR);
			return result;
		}else {
			return result;
		}
	}
	
	/**
	 * 注单结算
	 * 
	 * @return
	 * @throws ParseException
	 */
	public boolean singleNoteAccount() throws ParseException {
		// 创建map对象
		Map<String, Object> map = new HashMap<String, Object>();
		// 往map集合中添加数据
		map.put("state", "0");
		// 根据map在数据库中查询数据
		List<SingleNoteDTO> querySingleNoteById = memberService.querySingleNoteDTO(map);
		// 判断查询出来是否有数据
		if (querySingleNoteById == null || querySingleNoteById.size() <= 0) {
			System.out.println("没有注单结算的数据");
			return false;
		}
		// 使用迭代器循环遍历数据
		Iterator<SingleNoteDTO> iterator = querySingleNoteById.iterator();
		while (iterator.hasNext()) {
			SingleNoteDTO singleNote = iterator.next();
			if (singleNote == null) {
				continue;
			}
			// 获取下注时间
			Date betTime = singleNote.getBet_time();
			// 判断是足球联赛还是篮球联赛
			if (singleNote.getBet_type().equals("REFT") || singleNote.getBet_type().equals("RFT")) {
				// 根据联赛数据与时间获取足球数据
				List<Map<String, String>> footballResult = JsoupUtil.getFootballResult(singleNote.getLeague(), betTime);
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
				// 获取赢方
				String bet = (String)listResult.get(0);
				// 如果赛事腰斩了实行的操作
				if (bet.equals("赛事腰斩")) {
					boolean stateUpdate;
					String mid = singleNote.getMid();// 获取mid
					Float memberByMoney = Float.parseFloat(singleNote.getSum());// 获取用户余额
					Float money = Float.parseFloat(singleNote.getMoney());// 获取下注金额
					// 返回下注金额到账户余额
					Float sum = memberByMoney + money;
					stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
					if(stateUpdate) {
						System.out.println("注单结算成功");
						continue;
					}
				}
				// 获取客场比分
				Integer scoreTeamc = (Integer) listResult.get(1);
				// 获取主场比分
				Integer scoreTeamh = (Integer) listResult.get(2);
				// 获取比分差
				Integer score = (Integer) listResult.get(3);
				boolean stateUpdate = resultGame(singleNote, bet, scoreTeamh, scoreTeamc, score);
				if (stateUpdate) {
					System.out.println("注单结算成功");
					continue;
				}
			} else if (singleNote.getBet_type().equals("RBK") || singleNote.getBet_type().equals("REBK")) {
				// 根据联赛数据与时间获取篮球数据
				List<Map<String, String>> basketballResult = JsoupUtil.getBasketballResult(singleNote.getLeague(), betTime);
				// 判断数据是否为空
				if (basketballResult == null || basketballResult.size() <= 0) {
					System.out.println(singleNote.getLeague() + "---没有注单结算的数据");
					continue;
				}
				// 篮球数据匹配
				List<Object> listResult = JsoupUtil.getGameMap(basketballResult, singleNote);
				if (listResult == null) {
					continue;
				}
				// 获取赢方
				String bet = (String)listResult.get(0);
				// 如果赛事腰斩了实行的操作
				if (bet.equals("赛事腰斩")) {
					boolean stateUpdate;
					String mid = singleNote.getMid();// 获取mid
					Float memberByMoney = Float.parseFloat(singleNote.getSum());// 获取用户余额
					Float money = Float.parseFloat(singleNote.getMoney());// 获取下注金额
					// 返回下注金额到账户余额
					Float sum = memberByMoney + money;
					stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
					if(stateUpdate) {
						System.out.println("注单结算成功");
						continue;
					}
				}
				// 获取客场比分
				Integer scoreTeamc = (Integer) listResult.get(1);
				// 获取主场比分
				Integer scoreTeamh = (Integer) listResult.get(2);
				// 获取比分差
				Integer score = (Integer) listResult.get(3);
				boolean stateUpdate = resultGame(singleNote, bet, scoreTeamh, scoreTeamc, score);
				if (stateUpdate) {
					System.out.println("注单结算成功");
					continue;
				}
			}
		}
		return true;
	}

	/**
	 * 足球篮球结算
	 * 
	 * @param singleNote
	 *            注单DTO
	 * @param listResult
	 *            足球篮球list数据
	 * @return
	 */
	public boolean resultGame(SingleNoteDTO singleNote, String bet, Integer scoreTeamh, Integer scoreTeamc,
			Integer score) {
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
		// 获取下注比率
		String ratio = singleNote.getIor_ratio();
		// 获取比率类型
		String iorType = singleNote.getIor_type();
			if (iorType.equals("单")) {
				// 全输
				if ((scoreTeamc + scoreTeamh) % 2 == 0) {
					sum = memberByMoney;
					stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
					return stateUpdate ? true : false;
				}
				// 全赢
				if ((scoreTeamc + scoreTeamh) % 2 == 1) {
					sum = memberByMoney + validMoney;
					stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
					return stateUpdate ? true : false;
				}
			}
			if (iorType.equals("双")) {
				// 全赢
				if ((scoreTeamc + scoreTeamh) % 2 == 0) {
					sum = memberByMoney + validMoney;
					stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
					return stateUpdate ? true : false;
				}
				// 全输
				if ((scoreTeamc + scoreTeamh) % 2 == 1) {
					sum = memberByMoney;
					stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
					return stateUpdate ? true : false;
				}
			}
			if (iorType.equals("大")) {
				// 根据比率计算大输赢
				if (ratio.contains("/") && ratio.contains(".")) {
					String[] split = ratio.split("/");
					if (split[0].contains(".")) {
						// 既不是大球也不是小球，买大球的人赢一半
						if ((scoreTeamc + scoreTeamh) - Integer.parseInt(split[1]) == 0) {
							sum = memberByMoney + money + (validMoney / 2);
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if ((scoreTeamc + scoreTeamh) < Integer.parseInt(split[1])) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if ((scoreTeamc + scoreTeamh) > Integer.parseInt(split[1])) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
					} else if (split[1].contains(".")) {
						// 既不是大球也不是小球，买大球的人输一半
						if ((scoreTeamc + scoreTeamh) - Integer.parseInt(split[0]) == 0) {
							sum = memberByMoney + (money / 2);
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全输
						if ((scoreTeamc + scoreTeamh) < Integer.parseInt(split[0])) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if ((scoreTeamc + scoreTeamh) > Integer.parseInt(split[0])) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
					}
				} else if (ratio.contains(".") && !ratio.contains("/")) {
					// 全赢
					if ((scoreTeamc + scoreTeamh) > Float.parseFloat(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
						return stateUpdate ? true : false;
					}
					// 全输
					if ((scoreTeamc + scoreTeamh) < Float.parseFloat(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
						return stateUpdate ? true : false;
					}
				} else if (!ratio.contains(".") && !ratio.contains("/")) {
					// 全赢
					if ((scoreTeamc + scoreTeamh) > Integer.parseInt(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
						return stateUpdate ? true : false;
					}
					// 全输
					if ((scoreTeamc + scoreTeamh) < Integer.parseInt(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
						return stateUpdate ? true : false;
					}
					// 不输不赢
					if ((scoreTeamc + scoreTeamh) - Integer.parseInt(ratio) == 0) {
						sum = memberByMoney + money;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
						return stateUpdate ? true : false;
					}
				}
			}
			if (iorType.equals("小")) {
				// 根据比率计算小输赢
				if (ratio.contains("/") && ratio.contains(".")) {
					String[] split = ratio.split("/");
					if (split[0].contains(".")) {
						// 既不是大球也不是小球，买小球的人输一半
						if ((scoreTeamc + scoreTeamh) - Integer.parseInt(split[1]) == 0) {
							sum = memberByMoney + (money / 2);
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全输
						if ((scoreTeamc + scoreTeamh) > Integer.parseInt(split[1])) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if ((scoreTeamc + scoreTeamh) < Integer.parseInt(split[1])) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
					} else if (split[1].contains(".")) {
						// 既不是大球也不是小球，买小球的人赢一半
						if ((scoreTeamc + scoreTeamh) - Integer.parseInt(split[0]) == 0) {
							sum = memberByMoney + money + (validMoney / 2);
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if ((scoreTeamc + scoreTeamh) > Integer.parseInt(split[0])) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if ((scoreTeamc + scoreTeamh) < Integer.parseInt(split[0])) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
					}
				} else if (ratio.contains(".") && !ratio.contains("/")) {
					// 全赢
					if ((scoreTeamc + scoreTeamh) < Float.parseFloat(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
						return stateUpdate ? true : false;
					}
					// 全输
					if ((scoreTeamc + scoreTeamh) > Float.parseFloat(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
						return stateUpdate ? true : false;
					}
				} else if (!ratio.contains(".") && !ratio.contains("/")) {
					// 全赢
					if ((scoreTeamc + scoreTeamh) < Integer.parseInt(ratio)) {
						sum = memberByMoney + money + validMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
						return stateUpdate ? true : false;
					}
					// 全输
					if ((scoreTeamc + scoreTeamh) > Integer.parseInt(ratio)) {
						sum = memberByMoney;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
						return stateUpdate ? true : false;
					}
					// 不输不赢
					if ((scoreTeamc + scoreTeamh) - Integer.parseInt(ratio) == 0) {
						sum = memberByMoney + money;
						stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
						return stateUpdate ? true : false;
					}
				}
			}
			if (iorType.equals("单大")) {
				if (singleNote.getBet().equals("H")) {
					if (ratio.contains(".")) {
						// 全赢
						if (scoreTeamh > Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (scoreTeamh < Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
					} else if (!ratio.contains(".")) {
						// 全赢
						if (scoreTeamh > Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (scoreTeamh < Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if (scoreTeamh - Integer.parseInt(ratio) == 0) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
							return stateUpdate ? true : false;
						}
					}
				}
				if (singleNote.getBet().equals("C")) {
					if (ratio.contains(".")) {
						// 全赢
						if (scoreTeamc > Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (scoreTeamc < Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
					} else if (!ratio.contains(".")) {
						// 全赢
						if (scoreTeamc > Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (scoreTeamc < Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if (scoreTeamc - Integer.parseInt(ratio) == 0) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
							return stateUpdate ? true : false;
						}
					}
				}
			}
			if (iorType.equals("单小")) {
				if (singleNote.getBet().equals("H")) {
					if (ratio.contains(".")) {
						// 全赢
						if (scoreTeamh < Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (scoreTeamh > Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
					} else if (!ratio.contains(".")) {
						// 全赢
						if (scoreTeamh < Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (scoreTeamh > Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if (scoreTeamh - Integer.parseInt(ratio) == 0) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
							return stateUpdate ? true : false;
						}
					}
				}
				if (singleNote.getBet().equals("C")) {
					if (ratio.contains(".")) {
						// 全赢
						if (scoreTeamc < Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (scoreTeamc > Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
					} else if (!ratio.contains(".")) {
						// 全赢
						if (scoreTeamc < Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (scoreTeamc > Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if (scoreTeamc - Integer.parseInt(ratio) == 0) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
							return stateUpdate ? true : false;
						}
					}
				}
			}
			if (iorType.equals("让球")) {
				if (singleNote.getStrong().equals(singleNote.getBet())) {
					// 根据比率计算让球输赢
					if (ratio.contains("/") && ratio.contains(".")) {
						String[] split = ratio.split("/");
						if (split[0].contains(".")) {
							// 赢一半
							if (score - Integer.parseInt(split[1]) == 0) {
								sum = memberByMoney + money + (validMoney / 2);
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
								return stateUpdate ? true : false;
							}
							// 全输
							if (score < Integer.parseInt(split[1])) {
								sum = memberByMoney;
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
								return stateUpdate ? true : false;
							}
							// 全赢
							if (score > Integer.parseInt(split[1])) {
								sum = memberByMoney + money + validMoney;
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
								return stateUpdate ? true : false;
							}
						} else if (split[1].contains(".")) {
							// 输一半
							if (score - Integer.parseInt(split[0]) == 0) {
								sum = memberByMoney + (money / 2);
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
								return stateUpdate ? true : false;
							}
							// 全输
							if (score < Integer.parseInt(split[0])) {
								sum = memberByMoney;
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
								return stateUpdate ? true : false;
							}
							// 全赢
							if (score > Integer.parseInt(split[0])) {
								sum = memberByMoney + money + validMoney;
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
								return stateUpdate ? true : false;
							}
						}
					} else if (ratio.contains(".") && !ratio.contains("/")) {
						// 全输
						if (score < Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if (score > Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
					} else if (!ratio.contains(".") && !ratio.contains("/")) {
						// 全输
						if (score < Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if (score > Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if (score - Integer.parseInt(ratio) == 0) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
							return stateUpdate ? true : false;
						}
					}
				}
				if (!singleNote.getStrong().equals(singleNote.getBet())) {
					// 根据比率计算让球输赢
					if (ratio.contains("/") && ratio.contains(".")) {
						String[] split = ratio.split("/");
						if (split[0].contains(".")) {
							// 输一半
							if (score - Integer.parseInt(split[1]) == 0) {
								sum = memberByMoney + (money / 2);
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
								return stateUpdate ? true : false;
							}
							// 全赢
							if (score < Integer.parseInt(split[1])) {
								sum = memberByMoney + money + validMoney;
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
								return stateUpdate ? true : false;
							}
							// 全输
							if (score > Integer.parseInt(split[1])) {
								sum = memberByMoney;
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
								return stateUpdate ? true : false;
							}
						} else if (split[1].contains(".")) {
							// 赢一半
							if (score - Integer.parseInt(split[0]) == 0) {
								sum = memberByMoney + money + (validMoney / 2);
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
								return stateUpdate ? true : false;
							}
							// 全赢
							if (score < Integer.parseInt(split[0])) {
								sum = memberByMoney + money + validMoney;
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
								return stateUpdate ? true : false;
							}
							// 全输
							if (score > Integer.parseInt(split[0])) {
								sum = memberByMoney;
								stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
								return stateUpdate ? true : false;
							}
						}
					} else if (ratio.contains(".") && !ratio.contains("/")) {
						// 全赢
						if (score < Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (score > Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
					} else if (!ratio.contains(".") && !ratio.contains("/")) {
						// 全赢
						if (score < Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (score > Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if (score - Integer.parseInt(ratio) == 0) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
							return stateUpdate ? true : false;
						}
					}
				}
			}
			if (iorType.equals("让分")) {
				if(singleNote.getStrong().equals(singleNote.getBet())) {
					if (ratio.contains(".")) {
						// 全赢
						if (score > Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (score < Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
					} else if (!ratio.contains(".")) {
						// 全赢
						if (score > Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 全输
						if (score < Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if (score - Integer.parseInt(ratio) == 0) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
							return stateUpdate ? true : false;
						}
					}
				}
				if (!singleNote.getStrong().equals(singleNote.getBet())) {
					if (ratio.contains(".")) {
						// 全输
						if (score > Float.parseFloat(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if (score < Float.parseFloat(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
					} else if (!ratio.contains(".")) {
						// 全输
						if (score > Integer.parseInt(ratio)) {
							sum = memberByMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
							return stateUpdate ? true : false;
						}
						// 全赢
						if (score < Integer.parseInt(ratio)) {
							sum = memberByMoney + money + validMoney;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
							return stateUpdate ? true : false;
						}
						// 不输不赢
						if (score - Integer.parseInt(ratio) == 0) {
							sum = memberByMoney + money;
							stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "0");
							return stateUpdate ? true : false;
						}
					}
				}
			}
		// 如果赛事下注赢实行的操作
		if (bet.equals(singleNote.getBet())) {
			if (iorType.equals("独赢")) {
				// 下注金额与有效金额都返回账户余额
				sum = memberByMoney + validMoney;
				stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "1");
				return stateUpdate ? true : false;
			}
		}
		// 如果赛事下注输实行的操作
		if (!bet.equals(singleNote.getBet())) {
			// 返回下注金额到账户余额
			sum = memberByMoney;
			stateUpdate = stateUpdate(mid, sum, memberByMoney, money, singleNote, "-1");
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
	public boolean stateUpdate(String mid, Float sum, Float memberByMoney, Float money, SingleNoteDTO singleNote,
			String winLose) {
		Map<String, Object> singleNoteMap = new HashMap<String, Object>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mid", mid);// 设置mid
		map.put("sum", String.format("%.2f", sum));// 设置余额
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
		// 保留两位小数
		String dealMoney = String.format("%.2f", money);
		// 如果为正数就拼接加号
		dealMoney = money > 0 ? "+" + dealMoney : dealMoney;
		// 如果为0,则什么都不要加
		dealMoney = money == 0 ? String.format("%.0f", money) : dealMoney;
		singleNoteMap.put("dealMoney", dealMoney);
		// 根据snid修改注单状态
		int singleNoteAccount = memberService.singleNoteAccount(singleNoteMap);
		// 如果注单表修改失败则把账户余额修改回去
		if (singleNoteAccount <= 0) {
			System.err.println("修改注单状态失败！");
			map.put("sum", String.format("%.2f", memberByMoney));// 设置余额
			memberService.updateSum(map);// 修改账户余额
			return false;
		} else {
			System.out.println("注单" + singleNote.getNumber() + "结算成功！");
		}
		return true;
	}
	
	/**
	 * 注单过期
	 * @return
	 */
	public boolean amidithionOvertime() {
		// 创建map对象
		Map<String, Object> map = new HashMap<String, Object>();
		// 往map集合中添加数据
		map.put("state", "0");
		// 根据map在数据库中查询数据
		List<SingleNoteDTO> querySingleNoteById = memberService.querySingleNoteDTO(map);
		// 判断查询出来是否有数据
		if (querySingleNoteById == null || querySingleNoteById.size() <= 0) {
			System.out.println("没有注单结算的数据");
			return false;
		}
		// 使用迭代器循环遍历数据
		Iterator<SingleNoteDTO> iterator = querySingleNoteById.iterator();
		while (iterator.hasNext()) {
			SingleNoteDTO singleNote = iterator.next();
			if (singleNote == null) {
				continue;
			}
			// 获得两个时间的毫秒时间差异
			long distance = new Date().getTime() - singleNote.getBet_time().getTime();
			// 计算差多少天
			long day = distance / (1000 * 24 * 60 * 60);
			// 计算差多少小时
			long hour = (distance % (1000 * 24 * 60 * 60)) / (1000 * 60 * 60);
			// 计算差多少分
			long min = (distance % (1000 * 24 * 60 * 60) % (1000 * 60 * 60)) / (1000 * 60);
			// 计算差多少秒
			long second = (distance % (1000 * 24 * 60 * 60) % (1000 * 60 * 60) % (1000 * 60)) / 1000;
			// 计算总共相差多少秒
			long time = (day * 60 * 60 * 24) + (hour * 60 * 60) + (min * 60 ) + second;
			// 如果注单超过两个小时未有结果就交给客服处理
			if(time <= 24 * 60 * 60) {
				continue;
			}
			Map<String, Object> singleNoteMap = new HashMap<String, Object>();
			// 根据snid修改数据
			singleNoteMap.put("snid", singleNote.getSnid());
			// 往map里添加需要修改的字段
			singleNoteMap.put("state", "2");
			// 根据snid修改注单状态
			int singleNoteAccount = memberService.singleNoteAccount(singleNoteMap);
			// 如果注单表修改失败则把账户余额修改回去
			if (singleNoteAccount <= 0) {
				System.err.println("修改注单状态失败！");
				continue;
			} else {
				System.out.println("注单" + singleNote.getNumber() + "无赛果，移交给后台客服处理！");
			}
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
