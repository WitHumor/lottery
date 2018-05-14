package com.springboot.lottery.service.impl;

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
	public List<Member> loginMember(Map<String, Object> map) {
		List<Member> list = memberDao.loginMember(map);
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
	 * 根据会员id查询余额
	 * 
	 * @param mid
	 * @return
	 */
	@Override
	public Member getMemberByMoney(String mid) {
		return memberDao.getMemberByMoney(mid);
	}

	/**
	 * 根据mid修改余额
	 * 
	 * @param map
	 */
	@Override
	public int updateSum(Map<String, String> map) {
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
	public int singleNoteAccount(Map<String, String> map) {
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
	 * 查询资金流水记录
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public Page<Map<String, Object>> listFundRecord(Map<String, Object> map) {
		List<FundRecordDTO> list = memberDao.listFundRecord(map);
		List<Map<String, Object>> listByFundRecords = toListByFundRecords(list,(String)map.get("record"));
		int total = memberDao.loadFundRecordTotal();
		Page<Map<String, Object>> page = new Page<Map<String, Object>>((int) map.get("pageNo"), (int) map.get("pageSize"), total,listByFundRecords);
		return page;
	}

	/**
	 * 查询注单DTO
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public List<SingleNoteDTO> querySingleNoteDTO(Map<String, String> map) {
		return memberDao.querySingleNoteDTO(map);
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
	 * FundRecordDTO 放入 Map<String, Object> 只保留前端需要的字段
	 * 
	 * @param dto
	 * @return
	 */
	private Map<String, Object> toMapByFundRecord(FundRecordDTO dto, String record) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("time", dto.getTime());// 时间
		map.put("bankNumber", dto.getBank_number());// 银行帐号
		map.put("money", dto.getMoney()); // 金额
		map.put("state", dto.getState());// 状态
		// 如果是提现记录添加两条信息，返回给前端
		if (record.equals("1")) {
			map.put("number", dto.getNumber()); // 订单号
			map.put("remark", dto.getRemark());// 备注
		}
		return map;
	}
}
