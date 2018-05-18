package com.springboot.lottery.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		return memberDao.updateSum(map);
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
		List<FundRecordDTO> list = memberDao.listFundRecord(map);
		String record = (String)map.get("record");
		List<Map<String, Object>> listByFundRecords = toListByFundRecords(list,record);
		int total = memberDao.loadFundRecordTotal(map);
		Page<Map<String, Object>> page = new Page<Map<String, Object>>((int)map.get("pageNo"), (int)map.get("pageSize"), total,listByFundRecords);
		return page;
	}
	/**
	 * 查询注单记录
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public Page<Map<String, Object>> listSingleNote(Map<String, Object> map) {
		List<MemberSingleNote> list = memberDao.querySingleNote(map);
		List<Map<String, Object>> listBySingleNotes = toListBySingleNotes(list);
		int total = memberDao.loadSingleNoteTotal(map);
		Page<Map<String, Object>> page = new Page<Map<String, Object>>((int)map.get("pageNo"), (int)map.get("pageSize"), total,listBySingleNotes);
		return page;
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
	 * 查询资金流水记录总条数
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
	public List<MemberFundRecord> queryFundRecord(Map<String, Object> map){
		return memberDao.queryFundRecord(map);
	}
	
	/**
	 * List<FundRecordDTO> 转为 List<Map<String, Object>> 只保留前端需要的字段
	 * 
	 * @param dtos
	 * @return
	 */
	private List<Map<String, Object>> toListByFundRecords(List<FundRecordDTO> dtos, String record) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (dtos != null) {
			for (FundRecordDTO dto : dtos) {
				Map<String, Object> map = toMapByFundRecord(dto, record);
				list.add(map);
			}
		}
		return list;
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
	 * FundRecordDTO 放入 Map<String, Object> 只保留前端需要的字段
	 * 
	 * @param dto
	 * @return
	 */
	private Map<String, Object> toMapByFundRecord(FundRecordDTO dto, String record) {
		Map<String, Object> map = new HashMap<String, Object>();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String time = format.format(dto.getTime());
		map.put("time", time);// 时间
		map.put("number", dto.getNumber()); // 订单号
		map.put("money", dto.getMoney()); // 金额
		map.put("state", dto.getState());// 状态
		// 如果是提现记录添加两条信息，返回给前端
		if (record.equals("1")) {
			map.put("moneyAddress", dto.getMoney_address());// 钱包地址
			map.put("remark", dto.getRemark());// 备注
		}
		if (record.equals("0")) {
			map.put("type", dto.getCurrency());// 充值货币类型
		}
		return map;
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
		map.put("betTime", time);// 下注时间
		map.put("league", singleNote.getLeague());// 联赛
		map.put("teamh", singleNote.getTeam_h());// 主场
		map.put("teamc", singleNote.getTeam_c());// 客场
		map.put("betType", singleNote.getBet_type());// 下注类型 足球|篮球
		map.put("state", singleNote.getState());// 结算状态
		map.put("money", singleNote.getMoney());// 下注金额
		map.put("dealMoney", singleNote.getDeal_money());// 交易金额
		map.put("winLose", singleNote.getWin_lose());// 输赢状态
		return map;
	}
}
