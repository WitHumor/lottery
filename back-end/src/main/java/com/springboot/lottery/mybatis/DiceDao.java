package com.springboot.lottery.mybatis;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;

import com.springboot.lottery.entity.DiceBet;
import com.springboot.lottery.entity.DiceDraw;

/**
 * 数据访问层
 * 
 * @author Administrator
 *
 */
public interface DiceDao {
	/**
	 * 会员开户
	 * 
	 * @param member
	 */
	int addDiceDraw(DiceDraw diceDraw);
	
	/**
	 * 查询资金流水记录
	 * 
	 * @param map
	 * @return
	 */
	List<DiceDraw> queryDiceDraw(Map<String, Object> map);
	
	
	@MapKey("bet")
	Map<Integer, Map<String, Object>> queryDiceDrawResult(Map<String, Object> map);
	
	int updateDiceDraw(Map<String, Object> map);
	
	int addDiceBet(DiceBet diceBet);

}
