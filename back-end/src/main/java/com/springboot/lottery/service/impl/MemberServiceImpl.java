package com.springboot.lottery.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	 * 会员登录
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
	 * 会员下注
	 * 
	 * @param memberSingleNote
	 * @return
	 */
	@Override
	public int betMember(MemberSingleNote memberSingleNote) {
		return memberDao.betMember(memberSingleNote);
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
		Float money = Float.parseFloat((String) map.get("money"));
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
			// 获取会员余额
			Float sum = Float.parseFloat(member.getSum());
			// 判断余额是否足够
			if(sum < money) {
				updateSum = -1;
				break;
			}
			// 设置余额
			sum = sum + money;
			map.put("sum", String.format("%.2f", sum));
			updateSum = memberDao.updateSum(map);
			if (updateSum > 0) {
				break;
			}
		}
		return updateSum;
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
		// 根据条件查询条数
		int total = memberDao.loadFundRecordTotal(map);
		if (map.get("pageNo") == null || map.get("pageSize") == null) {
			// 不进行分页
			Page<Map<String, Object>> page = new Page<Map<String, Object>>(total, listByFundRecords);
			return page;
		} else {
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
				if (StringUtils.isBlank(state) && state.equals("0")) {
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
		// 根据条件查询条数
		int total = memberDao.loadSingleNoteTotal(map);
		if (map.get("pageNo") == null || map.get("pageSize") == null) {
			// 不进行分页
			Page<Map<String, Object>> page = new Page<Map<String, Object>>(total, listBySingleNotes);
			return page;
		} else {
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
				map.put("remark", dto.getRemark());// 备注
			}
			if (record.equals("0")) {
				map.put("type", dto.getCurrency());// 充值货币类型
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
		map.put("betType", singleNote.getBet_type());// 下注类型 足球|篮球
		map.put("ratio", singleNote.getRatio());// 赔率
		map.put("bet", singleNote.getBet());// 下注赢方
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
		map.put("ratio", singleNote.getRatio());// 赔率
		map.put("bet", singleNote.getBet());// 下注赢方
		map.put("betType", singleNote.getBet_type());// 下注类型 足球|篮球
		map.put("state", singleNote.getState());// 结算状态
		map.put("money", singleNote.getMoney());// 下注金额
		map.put("dealMoney", singleNote.getDeal_money());// 交易金额
		map.put("winLose", singleNote.getWin_lose());// 输赢状态
		map.put("name", singleNote.getName());// 会员名
		return map;
	}
}
