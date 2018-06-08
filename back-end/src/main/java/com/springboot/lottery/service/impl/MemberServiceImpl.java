package com.springboot.lottery.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.springboot.lottery.dto.FundRecordDTO;
import com.springboot.lottery.dto.SingleNoteDTO;
import com.springboot.lottery.entity.Member;
import com.springboot.lottery.entity.MemberFundRecord;
import com.springboot.lottery.entity.MemberSingleNote;
import com.springboot.lottery.mybatis.MemberDao;
import com.springboot.lottery.service.MemberService;
import com.springboot.lottery.util.Page;

/**
 * 业务逻辑层
 * 
 * @author Administrator
 *
 */
@Service
public class MemberServiceImpl implements MemberService {
	@Autowired
	private MemberDao memberDao;

	/**
	 * 会员开户
	 * 
	 * @param member
	 */
	@Override
	public int addMember(Member member) {
		return memberDao.addMember(member);
	}

	/**
	 * 会员名验证
	 * 
	 * @param name
	 * @return
	 */
	@Override
	public String verifyMember(String name) {
		return memberDao.verifyMember(name);
	}

	/**
	 * 查询会员信息
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public List<Member> queryMember(Map<String, Object> map) {
		List<Member> list = memberDao.queryMember(map);
		return list;
	}

	/**
	 * 查询会员信息条数
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public int queryMemberTotal(Map<String, Object> map) {
		return memberDao.queryMemberTotal(map);
	}
	
	/**
	 * 会员下注
	 * 
	 * @param memberSingleNote
	 * @return
	 */
	@Override
	public int betMember(MemberSingleNote memberSingleNote) {
		int betMember = memberDao.betMember(memberSingleNote);
		return betMember;
	}

	/**
	 * 根据mid修改余额
	 * 
	 * @param map
	 */
	@Override
	public int updateSum(Map<String, Object> map) {
		// 获取会员id
		String mid = (String) map.get("mid");
		if (StringUtils.isBlank(mid)) {
			return 0;
		}
		// 获取money
		String money = (String) map.get("money");
		int updateSum = 0;
		for (int i = 0; i < 3; i++) {
			// 查询会员信息
			List<Member> queryMember = memberDao.queryMember(map);
			if (queryMember == null || queryMember.size() != 1) {
				return 0;
			}
			Member member = queryMember.get(0);
			// 获取锁值
			int version = member.getVersion();
			// 设置上限
			version = version >= 999999999 ? 0 : version;
			map.put("version", version);
			// 用于判断是否是返利余额
			String state = (String)map.get("rebate");
			Float sum = null;
			if(state != null && state.equals("1")) {
				// 获取返利余额
				sum = Float.parseFloat(member.getRebate());
				// 判断是否是扣减余额
				if (money.contains("-")) {
					String[] split = money.split("-");
					// 判断余额是否足够
					if (sum < Float.parseFloat(split[1])) {
						updateSum = -1;
						break;
					}
				}
				// 设置余额
				sum = sum + Float.parseFloat(money);
				map.put("rebate", String.format("%.2f", sum));
			} else {
				// 获取会员余额
				sum = Float.parseFloat(member.getSum());
				// 判断是否是扣减余额
				if (money.contains("-")) {
					String[] split = money.split("-");
					// 判断余额是否足够
					if (sum < Float.parseFloat(split[1])) {
						updateSum = -1;
						break;
					}
				}
				// 设置余额
				sum = sum + Float.parseFloat(money);
				map.put("sum", String.format("%.2f", sum));
			}
			updateSum = memberDao.updateSum(map);
			if (updateSum > 0) {
				break;
			}
		}
		return updateSum;
	}

	/**
	 * 根据mid修改用户信息
	 * 
	 * @param map
	 */
	@Override
	public int updateMember(Map<String, Object> map) {
		return memberDao.updateMember(map);
	}

	/**
	 * 根据snid删除注单
	 * 
	 * @param string
	 * @return
	 */
	@Override
	public int deleteSingleNote(String snid) {
		return memberDao.deleteSingleNote(snid);
	}

	/**
	 * 注单结算
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public int singleNoteAccount(Map<String, Object> map) {
		return memberDao.singleNoteAccount(map);
	}

	/**
	 * 在线存款与在线取款
	 * 
	 * @param memberFundRecord
	 * @return
	 */
	@Override
	public int addFundRecord(MemberFundRecord memberFundRecord) {
		return memberDao.addFundRecord(memberFundRecord);
	}

	/**
	 * 根据资金流水表id删除信息
	 * 
	 * @param frid
	 * @return
	 */
	@Override
	public int deleteFundRecord(String frid) {
		return memberDao.deleteFundRecord(frid);
	}

	/**
	 * 查询存取款记录
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public Page<Map<String, Object>> listFundRecord(Map<String, Object> map) {
		String record = (String) map.get("record");// 获取取款、存款状态
		String mid = (String) map.get("mid");// 获取会员id
		String state = (String) map.get("state");// 获取状态
		if (StringUtils.isBlank(mid) && StringUtils.isBlank(state)) {
			if (StringUtils.isNotBlank(record)) {
				// 判断充值还是取款进行排序
				if (record.equals("0")) {
					map.put("states", "1");
				} else if (record.equals("1")) {
					map.put("states", "0");
				}
			}
		}
		// 根据条件获取数据
		List<FundRecordDTO> list = memberDao.queryFundRecordDTO(map);
		// 根据会员id是否为空判断权限，根据record判断是存取款
		List<Map<String, Object>> listByFundRecords = toListByFundRecords(list, record, mid);
		if (map.get("pageNo") == null || map.get("pageSize") == null) {
			// 不进行分页
			Page<Map<String, Object>> page = new Page<Map<String, Object>>(listByFundRecords);
			return page;
		} else {
			// 根据条件查询条数
			int total = memberDao.loadFundRecordTotal(map);
			// 进行分页
			Page<Map<String, Object>> page = new Page<Map<String, Object>>((int) map.get("pageNo"),
					(int) map.get("pageSize"), total, listByFundRecords);
			return page;
		}
	}

	/**
	 * 查询注单记录
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public Page<Map<String, Object>> listSingleNote(Map<String, Object> map) {
		// 获取会员id
		String mid = (String) map.get("mid");
		String betType = (String) map.get("betType");
		String state = (String) map.get("state");
		List<Map<String, Object>> listBySingleNotes = null;
		if (StringUtils.isNotBlank(betType)) {
			// 根据FT与BK查询滚球
			if (betType.equals("FT") || betType.equals("BK")) {
				if (StringUtils.isNotBlank(state) && state.equals("0")) {
					String states = "'0','2'";
					map.put("states", states);
				}
				map.put("betTypes", betType);
				map.remove("state");
				map.remove("betType");
			}
		}
		// 根据会员id是否为空判断权限
		if (StringUtils.isBlank(mid)) {
			// 增加用户名查询
			map.put("name", map.get("keyword"));
			// 自定义值排序
			if (StringUtils.isBlank(state)) {
				map.put("forAccount", "2");
				map.put("notAccount", "0");
			}
			List<SingleNoteDTO> list = memberDao.querySingleNoteDTO(map);
			listBySingleNotes = toListBySingleNoteDTO(list);
		} else {
			List<MemberSingleNote> list = memberDao.querySingleNote(map);
			listBySingleNotes = toListBySingleNotes(list);
		}
		if (map.get("pageNo") == null || map.get("pageSize") == null) {
			// 不进行分页
			Page<Map<String, Object>> page = new Page<Map<String, Object>>(listBySingleNotes);
			return page;
		} else {
			// 根据条件查询条数
			int total = memberDao.loadSingleNoteTotal(map);
			// 进行分页
			Page<Map<String, Object>> page = new Page<Map<String, Object>>((int) map.get("pageNo"),
					(int) map.get("pageSize"), total, listBySingleNotes);
			return page;
		}
	}

	/**
	 * 查询注单DTO
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public List<SingleNoteDTO> querySingleNoteDTO(Map<String, Object> map) {
		return memberDao.querySingleNoteDTO(map);
	}

	/**
	 * 查询取款存款DTO
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public List<FundRecordDTO> queryFundRecordDTO(Map<String, Object> map) {
		return memberDao.queryFundRecordDTO(map);
	}

	/**
	 * 查询资金流水记录总条数
	 * 
	 * @return
	 */
	@Override
	public int loadFundRecordTotal(Map<String, Object> map) {
		return memberDao.loadFundRecordTotal(map);
	}

	/**
	 * 资金流水记录状态修改
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public int updateFundRecord(Map<String, Object> map) {
		return memberDao.updateFundRecord(map);
	}

	/**
	 * 查询资金流水记录
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public List<MemberFundRecord> queryFundRecord(Map<String, Object> map) {
		return memberDao.queryFundRecord(map);
	}

	/**
	 * 查询推广返利记录
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public Page<Map<String, Object>> queryGeneralizeRebate(Map<String, Object> map){
		List<SingleNoteDTO> list = memberDao.queryGeneralizeRebate(map);
		List<Map<String, Object>> generalizeRebates = toGeneralizeRebates(list);
		int total = memberDao.queryGeneralizeRebateTotal(map);
		Page<Map<String, Object>> page = new Page<Map<String, Object>>((int) map.get("pageNo"),
					(int) map.get("pageSize"), total, generalizeRebates);
		return page;
	}
	
	/**
	 * 查询推广返利
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public List<SingleNoteDTO> generalizeRebate(Map<String, Object> map){
		return memberDao.queryGeneralizeRebate(map);
	}
	/**
	 * 在线取款-事务
	 * 
	 * @param map
	 * @return
	 */
	@Transactional
	public int memberWithdrawn(Map<String, Object> map) {
		int addFundRecord = 0;
		MemberFundRecord memberFundRecord = (MemberFundRecord) map.get("memberFundRecord");
		addFundRecord = addFundRecord(memberFundRecord);
		if (addFundRecord <= 0) {
			return addFundRecord;
		}
		String money = memberFundRecord.getMoney();
		// 设置余额
		map.put("money", String.format("%.2f", 0 - Float.parseFloat(money)));
		// 判断是否是返利余额
		if(memberFundRecord.getWithdrawn_type().equals("1")) {
			map.put("rebate", "1");
		}
		// 根据会员id修改账户余额
		addFundRecord = updateSum(map);
		if (addFundRecord == 0 || addFundRecord == -1) {
			// 手动回滚，这样上层就无需去处理异常（现在项目的做法）
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		return addFundRecord;
	}

	/**
	 * 注单取消-事务
	 * 
	 * @param map
	 * @return
	 */
	@Transactional
	public int cancelSingleNote(Map<String, Object> map) {
		int cancelSingleNote = 0;
		Map<String, Object> singleNoteMap = new HashMap<String, Object>();
		// 根据snid修改数据
		singleNoteMap.put("snid", map.get("snid"));
		// 往map里添加需要修改的字段
		singleNoteMap.put("state", "-1");
		singleNoteMap.put("winLose", "0");
		singleNoteMap.put("dealMoney", "0");
		// 根据snid修改注单状态
		cancelSingleNote = singleNoteAccount(singleNoteMap);
		if (cancelSingleNote <= 0) {
			return cancelSingleNote;
		}
		// 根据mid修改余额
		cancelSingleNote = updateSum(map);
		if (cancelSingleNote == 0 || cancelSingleNote == -1) {
			// 手动回滚，这样上层就无需去处理异常（现在项目的做法）
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		return cancelSingleNote;
	}

	/**
	 * 会员下注-事务
	 * 
	 * @param map
	 * @return
	 */
	@Transactional
	public int betMember(Map<String, Object> map) {
		MemberSingleNote memberSingleNote = (MemberSingleNote) map.get("memberSingleNote");
		int betMember = 0;
		betMember = betMember(memberSingleNote);
		// 判断余额是否修改成功，如果不成功则删除添加的下注订单
		if (betMember <= 0) {
			return betMember;
		}
		// 修改账户余额
		map.put("money", String.format("%.2f", 0 - Float.parseFloat(memberSingleNote.getMoney())));// 设置余额
		// 根据mid修改余额
		betMember = updateSum(map);
		if (betMember == 0 || betMember == -1) {
			// 手动回滚，这样上层就无需去处理异常（现在项目的做法）
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		return betMember;
	}

	/**
	 * 充值/取款结算-事务
	 * 
	 * @param map
	 * @return
	 */
	@Transactional
	public int alterFundRecord(Map<String, Object> map) {
		int alertFundRecord = 0;
		// 根据订单号修改状态
		alertFundRecord = updateFundRecord(map);
		// 获取记录DTO
		FundRecordDTO fundRecordDTO = (FundRecordDTO) map.get("fundRecordDTO");
		Map<String, Object> memberMap = new HashMap<String, Object>();
		String mid = fundRecordDTO.getMid();// 获取mid
		String money = fundRecordDTO.getMoney();// 获取金额
		memberMap.put("mid", mid);
		// 获取状态
		String state = (String) map.get("state");
		// 获取记录类型：充值、取款
		String record = (String) map.get("record");
		// 如果充值成功向账户中添加金额
		if (record.equals("0") && state.equals("2")) {
			String discounts = fundRecordDTO.getDiscounts();// 获取优惠金额
			memberMap.put("money", String.format("%.2f", Float.valueOf(money) + Float.valueOf(discounts)));
			alertFundRecord = updateSum(memberMap);
			if (alertFundRecord == 0 || alertFundRecord == -1) {
				// 手动回滚，这样上层就无需去处理异常（现在项目的做法）
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			}
			return alertFundRecord;
		}
		// 如果提款失败向账户返回金额
		if (record.equals("1") && (state.equals("-1") || state.equals("-2"))) {
			memberMap.put("money", String.format("%.2f", Float.valueOf(money)));
			alertFundRecord = updateSum(memberMap);
			if (alertFundRecord == 0 || alertFundRecord == -1) {
				// 手动回滚，这样上层就无需去处理异常（现在项目的做法）
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			}
			return alertFundRecord;
		}
		return alertFundRecord;
	}

	/**
	 * 注单自动结算-事务
	 * 
	 * @param mid
	 * @param sum
	 * @param memberByMoney
	 * @param singleNote
	 * @param winLose
	 * @return
	 */
	@Transactional
	public boolean stateUpdate(String mid, Float sum, Float memberByMoney, Float money, SingleNoteDTO singleNote,
			String winLose, String amidithion) {
		Map<String, Object> singleNoteMap = new HashMap<String, Object>();
		// 根据snid修改数据
		singleNoteMap.put("snid", singleNote.getSnid());
		// 往map里添加需要修改的字段
		singleNoteMap.put("state", "1");
		singleNoteMap.put("winLose", winLose);
		singleNoteMap.put("score", amidithion);
		money = sum - (memberByMoney + money);
		// 保留两位小数
		String dealMoney = String.format("%.2f", money);
		// 如果赢就拼接加号
		dealMoney = winLose.equals("1") ? "+" + dealMoney : dealMoney;
		// 如果不输不赢,则什么都不要加
		dealMoney = winLose.equals("0") ? String.format("%.0f", money) : dealMoney;
		singleNoteMap.put("dealMoney", dealMoney);
		// 根据snid修改注单状态
		int singleNoteAccount = singleNoteAccount(singleNoteMap);
		// 注单表修改失败
		if (singleNoteAccount <= 0) {
			return false;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mid", mid);// 设置mid
		map.put("money", String.format("%.2f", sum - memberByMoney));// 设置余额
		// 根据mid修改余额
		int updateSum = updateSum(map);
		if (updateSum == 0 || updateSum == -1) {
			// 手动回滚，这样上层就无需去处理异常（现在项目的做法）
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return false;
		}
		System.out.println("注单" + singleNote.getNumber() + "结算成功！");
		return true;
	}

	/**
	 * List<FundRecordDTO> 转为 List<Map<String, Object>> 只保留前端需要的字段
	 * 
	 * @param dtos
	 * @return
	 */
	public List<Map<String, Object>> toListByFundRecords(List<FundRecordDTO> dtos, String record, String mid) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (dtos != null) {
			for (FundRecordDTO dto : dtos) {
				Map<String, Object> map = toMapByFundRecord(dto, record, mid);
				list.add(map);
			}
		}
		return list;
	}

	/**
	 * FundRecordDTO 放入 Map<String, Object> 只保留前端需要的字段
	 * 
	 * @param dto
	 * @return
	 */
	public Map<String, Object> toMapByFundRecord(FundRecordDTO dto, String record, String mid) {
		Map<String, Object> map = new HashMap<String, Object>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String time = format.format(dto.getTime());
		map.put("time", time);// 时间
		map.put("number", dto.getNumber()); // 订单号
		map.put("money", dto.getMoney()); // 金额
		map.put("state", dto.getState());// 状态
		map.put("resultRemark", dto.getResult_remark());// 结果备注
		if (StringUtils.isNotBlank(record)) {
			// 如果是提现记录添加两条信息，返回给前端
			if (record.equals("1")) {
				map.put("moneyAddress", dto.getMoney_address());// 钱包地址
				map.put("withdrawnType", dto.getWithdrawn_type());// 取款类型
				map.put("remark", dto.getRemark());// 备注
			}
			// 如果是充值记录添加两条信息，返回给前端
			if (record.equals("0")) {
				map.put("type", dto.getCurrency());// 充值货币类型
				map.put("discounts", dto.getDiscounts());// 优惠金额
			}
		}
		if (StringUtils.isBlank(mid)) {
			map.put("currencyCount", dto.getCurrency_count());// 货币个数
			map.put("name", dto.getName());// 会员名
		}
		return map;
	}

	/**
	 * List<SingleNote> 转为 List<Map<String, Object>> 只保留前端需要的字段
	 * 
	 * @param singleNotes
	 * @return
	 */
	private List<Map<String, Object>> toListBySingleNotes(List<MemberSingleNote> singleNotes) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (singleNotes != null) {
			for (MemberSingleNote singleNote : singleNotes) {
				Map<String, Object> map = toMapBySingleNote(singleNote);
				list.add(map);
			}
		}
		return list;
	}

	/**
	 * MemberSingleNote 放入 Map<String, Object> 只保留前端需要的字段
	 * 
	 * @param singleNote
	 * @return
	 */
	private Map<String, Object> toMapBySingleNote(MemberSingleNote singleNote) {
		Map<String, Object> map = new HashMap<String, Object>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String time = format.format(singleNote.getBet_time());
		map.put("number", singleNote.getNumber());// 注单号
		map.put("betTime", time);// 下注时间
		map.put("league", singleNote.getLeague());// 联赛
		map.put("teamh", singleNote.getTeam_h());// 主场
		map.put("teamc", singleNote.getTeam_c());// 客场
		map.put("iorType", singleNote.getIor_type());// 比率类型
		map.put("strong", singleNote.getStrong());// 让球方与受让方
		map.put("iorRatio", singleNote.getIor_ratio());// 盘口
		map.put("betType", singleNote.getBet_type());// 下注类型 足球|篮球
		map.put("ratio", singleNote.getRatio());// 赔率
		map.put("bet", singleNote.getBet());// 下注赢方
		map.put("score", singleNote.getScore());// 赛果
		map.put("occasion", singleNote.getOccasion());// 下注场次 全场 |半场
		if(singleNote.getBet_type().equals("REBK") || singleNote.getBet_type().equals("RBK")) {
			map.put("occasion", null);// 下注场次 全场 |半场
		}
		map.put("state", singleNote.getState());// 结算状态
		map.put("money", singleNote.getMoney());// 下注金额
		map.put("dealMoney", singleNote.getDeal_money());// 交易金额
		map.put("winLose", singleNote.getWin_lose());// 输赢状态
		return map;
	}

	/**
	 * List<FundRecordDTO> 转为 List<Map<String, Object>> 只保留前端需要的字段
	 * 
	 * @param dtos
	 * @return
	 */
	private List<Map<String, Object>> toListBySingleNoteDTO(List<SingleNoteDTO> dtos) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (dtos != null) {
			for (SingleNoteDTO dto : dtos) {
				Map<String, Object> map = toMapBySingleNoteDTO(dto);
				list.add(map);
			}
		}
		return list;
	}

	/**
	 * MemberSingleNote 放入 Map<String, Object> 只保留前端需要的字段
	 * 
	 * @param singleNote
	 * @return
	 */
	private Map<String, Object> toMapBySingleNoteDTO(SingleNoteDTO singleNote) {
		Map<String, Object> map = new HashMap<String, Object>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String time = format.format(singleNote.getBet_time());
		map.put("snid", singleNote.getSnid());// 注单id
		map.put("number", singleNote.getNumber());// 注单号
		map.put("mid", singleNote.getMid());// 会员id
		map.put("betTime", time);// 下注时间
		map.put("league", singleNote.getLeague());// 联赛
		map.put("teamh", singleNote.getTeam_h());// 主场
		map.put("teamc", singleNote.getTeam_c());// 客场
		map.put("iorType", singleNote.getIor_type());// 比率类型
		map.put("iorRatio", singleNote.getIor_ratio());// 盘口
		map.put("strong", singleNote.getStrong());// 让球方与受让方
		map.put("ratio", singleNote.getRatio());// 赔率
		map.put("bet", singleNote.getBet());// 下注赢方
		map.put("score", singleNote.getScore());// 赛果
		map.put("occasion", singleNote.getOccasion());// 下注场次 全场 |半场
		map.put("betType", singleNote.getBet_type());// 下注类型 足球|篮球
		if(singleNote.getBet_type().equals("REBK") || singleNote.getBet_type().equals("RBK")) {
			map.put("occasion", null);// 下注场次 全场 |半场
		}
		map.put("state", singleNote.getState());// 结算状态
		map.put("money", singleNote.getMoney());// 下注金额
		map.put("dealMoney", singleNote.getDeal_money());// 交易金额
		map.put("winLose", singleNote.getWin_lose());// 输赢状态
		map.put("name", singleNote.getName());// 会员名
		return map;
	}
	
	/**
	 * List<FundRecordDTO> 转为 List<Map<String, Object>> 只保留前端需要的字段
	 * 
	 * @param dtos
	 * @return
	 */
	public List<Map<String, Object>> toGeneralizeRebates(List<SingleNoteDTO> dtos) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (dtos != null) {
			for (SingleNoteDTO dto : dtos) {
				Map<String, Object> map = toGeneralizeRebate(dto);
				list.add(map);
			}
		}
		return list;
	}
	
	/**
	 * FundRecordDTO 放入 Map<String, Object> 只保留前端需要的字段
	 * 
	 * @param dto
	 * @return
	 */
	public Map<String, Object> toGeneralizeRebate(SingleNoteDTO dto) {
		Map<String, Object> map = new HashMap<String, Object>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月");
		String betTime = format.format(dto.getBet_time());
		map.put("time", betTime);// 时间
		map.put("name", dto.getName());// 会员名
		String money = dto.getMoney();
		map.put("betMoney", money);// 下注金额
		Float rebate = Float.parseFloat(money) * 0.01f;
		// 返利金额
		map.put("rebate", String.format("%.2f", rebate));
		return map;
	}
}
