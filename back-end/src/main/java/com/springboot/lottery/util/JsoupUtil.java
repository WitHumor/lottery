package com.springboot.lottery.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.springboot.lottery.dto.SingleNoteDTO;

/**
 * 数据工具类
 * 
 * @author Administrator
 *
 */
public class JsoupUtil {

	private static final String FIELD_BEGIN_STRING = "= ['";// 获取字段开始点切割

	private static final String FIELD_END_STRING = "'];";// 获取字段结束点切割

	private static final String DATA_BEGIN_STRING = "(['";// 获取数据开始点切割

	private static final String DATA_END_STRING = "']\\);";// 获取数据结束点切割

	private static final String GAME_RESULT = "table[class].game";// 获取所有赛果

	private static final String LEG_BAR = "span[class].leg_bar";// 获取足球/篮球联赛

	private static final String TEAM_C_FT = "td[class].team_c_ft";// 获取足球第一个队伍

	private static final String TEAM_H_FT = "td[class].team_h_ft";// 获取足球第二个队伍

	private static final String HR_MAIN_FT = "td[class].hr_main_ft span";// 获取上半场足球比分

	private static final String FULL_MAIN_FT = "td[class].full_main_ft span";// 获取全场足球比分

	private static final String TEAM_C = "td[class].team_c";// 获取篮球第一个队伍

	private static final String TEAM_H = "td[class].team_h";// 获取篮球第二个队伍

	private static final String HR_MAIN = "td[class].hr_main";// 获取上半场篮球比分

	private static final String FULL_MAIN = "td[class].full_main";// 获取全场篮球比分
	
	private static final String EXCHANGE_PRICE = "div[class].box p[class].price";// 获取全场篮球比分

	public static void main(String[] args) {
		// String url =
		// "https://www.ylg56789.com/app/member/FT_Result?game_type=FT&today1=2018-05-10&uid=&langx=zh-cn";
		// String url2 =
		// "https://www.ylg56789.com/app/member/BK_Result?game_type=BK&today1=2018-05-10&uid=&langx=zh-cn";
		// List<Map<String, String>> gameResult = getGameResult(url, LEG_BAR, TEAM_C_FT,
		// TEAM_H_FT,HR_MAIN_FT,FULL_MAIN_FT, "印度I联赛乙组");
		// List<Map<String, String>> gameResult = getGameResult(url, LEG_BAR, TEAM_C,
		// TEAM_H, HR_MAIN_FIRST, HR_MAIN_LAST,
		// FULL_MAIN_FIRST, FULL_MAIN_LAST,
		// "哥斯达黎加甲组联赛-附加赛");
		String url = "https://www.ylg56789.com/app/member/FT_browse/body_var?uid=41E1C90D347A90C6A60811350&rtype=r&langx=zh-cn&mtype=3&page_no=0&league_id=&hot_game=";
		// List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		// for (Map<String, String> map : gameResult) {
		// System.out.println(map);
		// }
		// for (Map<String, String> map : list) {
		// System.out.println(map);
		// }

		String stringAll = getStringAll(url);
		getFieldExplain(stringAll);

		String s = "121.5";

		System.out.println(s.contains("."));
		List<String> hrList = new ArrayList<String>();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		map.put("hrList", hrList);
		System.out.println(map.get("hrList"));
		// List<Map<String, String>> list = listFieldAndData(
		// "https://www.ylg56789.com/app/member/FT_browse/body_var?uid=41E1C90D347A90C6A60811350&rtype=r&langx=zh-cn&mtype=3&page_no=0&league_id=&hot_game=");
		// for (Map<String, String> map : list) {
		// System.out.println(map);
		// }
		String exchange = getExchange("eos");
		System.out.println(exchange);
	}

	/**
	 * 根据赛事名称进行匹配-足球
	 * 
	 * @param league
	 * @return
	 */
	public static List<Map<String, String>> getFootballResult(String league, Date leagueDate) {
		// 足球接口地址
		String url = "";
		// 定义时间格式
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// 转换时间格式
		String taday = format.format(leagueDate);
		url = "https://www.ylg56789.com/app/member/FT_Result?game_type=FT&today1=" + taday + "&uid=&langx=zh-cn";
		// 根据字段进行切割，并放入到list集合中
		List<Map<String, String>> gameResult = getGameResult(url, LEG_BAR, TEAM_C_FT, TEAM_H_FT, HR_MAIN_FT,
				FULL_MAIN_FT, league);
		// 判断对象是否为空
		if (gameResult == null) {
			return null;
		}
		return gameResult;
	}

	/**
	 * 根据赛事名称进行匹配-篮球
	 * 
	 * @param league
	 * @param taday
	 *            日期格式要求yyyy-MM-dd
	 * @return
	 */
	public static List<Map<String, String>> getBasketballResult(String league, Date leagueDate) {
		// 篮球接口地址
		String url = "";
		// 定义时间格式
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// 转换时间格式
		String taday = format.format(leagueDate);
		url = "https://www.ylg56789.com/app/member/BK_Result?game_type=BK&today1=" + taday + "&uid=&langx=zh-cn";
		// 根据字段进行切割，并放入到list集合中
		List<Map<String, String>> gameResult = getGameResult(url, LEG_BAR, TEAM_C, TEAM_H, HR_MAIN, FULL_MAIN, league);
		// 判断对象是否为空
		if (gameResult == null) {
			return null;
		}
		return gameResult;
	}

	/**
	 * 比赛结果数据匹配
	 * 
	 * @param list
	 * @return
	 */
	public static List<Object> getGameMap(List<Map<String, String>> list, SingleNoteDTO singleNote) {
		String bet = null, teamc = null, teamh = null, fullOrHrFirst = null, fullOrHrLast = null;
		Integer score = null, scorec = null, scoreh = null;
		List<Object> listResult = new ArrayList<Object>();
		// 使用迭代器遍历map数据
		Iterator<Map<String, String>> iterator = list.iterator();
		while (iterator.hasNext()) {
			Map<String, String> map = iterator.next();
			System.out.println(map);
			// 根据key值获取数据进行匹配
			teamc = map.get("teamc");// 获取客场
			teamh = map.get("teamh");// 获取主场
			System.out.println(teamc);
			// 如果数据匹配不成功直接跳出当前循环
			if (!teamc.equals(singleNote.getTeam_c()) || !teamh.equals(singleNote.getTeam_h())) {
				System.out.println("-----------");
				continue;
			}
			// if (!teamc.contains(team_c) || !teamh.contains(team_h)) {
			// System.out.println("-----------");
			// continue;
			// }

			if (singleNote.getOccasion().equals("全场")) {
				// 获取客场全场比分
				fullOrHrFirst = map.get("fullFirst");
				// 获取主场全场比分
				fullOrHrLast = map.get("fullLast");
			} else if (singleNote.getOccasion().equals("半场")) {
				// 获取客场上半场比分
				fullOrHrFirst = map.get("hrFirst");
				// 获取主场上半场比分
				fullOrHrLast = map.get("hrLast");
			}
			// 判断比分是否有结果
			if (StringUtils.isBlank(fullOrHrFirst) || StringUtils.isBlank(fullOrHrLast)) {
				System.out.println(teamh + "VS" + teamc + "还在比赛中，目前没有结果。。");
				continue;
			}
			scorec = Integer.parseInt(fullOrHrFirst);
			scoreh = Integer.parseInt(fullOrHrLast);
			// 判断是否是滚球
			if (singleNote.getBet_type().equals("REFT") || singleNote.getBet_type().equals("REBK")) {
				// 获取比分
				String getScore = singleNote.getScore();
				String[] split = getScore.split(" - ");
				scoreh = scoreh - Integer.valueOf(split[0]);
				scorec = scorec - Integer.valueOf(split[1]);
			}
			// 判断赛事是否腰斩
			if (fullOrHrFirst.equals("赛事腰斩") || fullOrHrLast.equals("赛事腰斩")) {
				listResult.add(bet);
				return listResult;
			}

			// 比较主客场比分返回结果
			if (scorec > scoreh) {
				System.out.println(map.get("teamc") + "赢得整场比赛");
				bet = "C";
				score = scorec - scoreh;
			} else if (scorec < scoreh) {
				System.out.println(map.get("teamh") + "赢得整场比赛");
				bet = "H";
				score = scoreh - scorec;
			} else if (scorec == scoreh) {
				System.out.println(map.get("teamc") + "VS" + map.get("teamh") + "和局");
				bet = "N";
				score = scoreh - scorec;
			} else {
				continue;
			}
			listResult.add(bet);
			listResult.add(scorec);
			listResult.add(scoreh);
			listResult.add(score);
			return listResult;
		}
		return null;
	}

	/**
	 * 根据url地址与gid进行数据匹配
	 * 
	 * @return Map<String, String>
	 */
	public static Map<String, String> getMapData(List<Map<String, String>> list, String gid) {
		// 循环匹配gid是否存在，如果存在则返回map，如果不存在则返回null
		for (Map<String, String> map : list) {
			String mapGid = map.get("gid");
			if (mapGid == null) {
				return null;
			}
			if (mapGid.equals(gid)) {
				// for(Entry<String, String> entry : map.entrySet()) {
				// System.out.println(entry.getKey() +" "+entry.getValue());
				// }
				System.out.println(map);
				return map;
			}
		}
		return null;
	}

	/**
	 * 根据url地址获取所有数据和字段
	 * 
	 * @param url
	 * @return List<Map<String, String>>
	 */
	public static List<Map<String, String>> listFieldAndData(String stringAll) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		String dataString = null;
		String[] strs = null, dataArray = null, fieldArray = null;
		Map<String, String> map = null;
		int index = 0;
		// 在url地址中获取数据数组
		dataArray = stringAll.split(DATA_END_STRING);
		// 在所有数据中获取分割后的字段数组
		fieldArray = JsoupUtil.getField(stringAll, FIELD_BEGIN_STRING, FIELD_END_STRING);
		// 循环分割数据
		for (int i = 0; i < dataArray.length - 1; i++) {
			// 根据字符串获取第一次出现的索引值
			index = dataArray[i].indexOf(DATA_BEGIN_STRING);
			// 获取需要的字符串
			dataString = dataArray[i].substring(index + DATA_BEGIN_STRING.length()).replace(" ", "");
			// 切割字符串，取字段 -1表示切割最后一位值为空的也能切割
			strs = dataString.split("','", -1);
			map = new HashMap<String, String>();
			if (fieldArray == null) {
				System.err.println("字段数组为空！");
				return null;
			}
			// 判断字符与数据的长度是否相等，如果相等则循环添加到map集合中
			if (fieldArray.length == strs.length) {
				for (int j = 0; j < fieldArray.length; j++) {
					map.put(fieldArray[j], strs[j]);
					if (StringUtil.isBlank(fieldArray[j])) {
						continue;
					}
				}
				list.add(map);
			}
		}
		return list;
	}

	/**
	 * 根据字符串获取字段数组
	 * 
	 * @param stringAll
	 * @param beginString
	 * @param endString
	 * @return String[]
	 */
	private static String[] getField(String stringAll, String beginString, String endString) {
		int index = 0;
		String fieldString = null;
		String[] fieldArray = null, stringArray = null;
		// 在所有数据中进行第一次分割
		stringArray = stringAll.split(endString);
		// 判断是否存在多条字段和没有字段
		if (stringArray.length != 2) {
			System.err.println("出现多条字段或者没有字段，无法与数据进行匹配！");
			return null;
		}
		// 循环分割数据
		for (int i = 0; i < stringArray.length - 1; i++) {
			// 根据字符串获取第一次出现的索引值
			index = stringArray[i].indexOf(beginString);
			// 获取需要的字符串
			fieldString = stringArray[i].substring(index + beginString.length()).replace(" ", "");
			// 切割字符串，取字段 -1表示切割最后一位值为空的也能切割
			fieldArray = fieldString.split("','", -1);
		}
		return fieldArray;
	}

	/**
	 * 根据地址获取全部数据
	 * 
	 * @param url
	 * @return String
	 */
	public static String getStringAll(String url) {
		Element link = null;
		String stringAll = null;
		// 加载url地址
		Document loadUrl = loadUrl(url);
		// 判断url地址是否连接成功
		if(loadUrl == null) {
			return null;
		}
		// 根据指定的位置查找数据
		link = loadUrl.getElementsByTag("script").last();
		// 获取script标签中的数据
		stringAll = link.data().toString();
		return stringAll;
	}

	/**
	 * 根据地址爬取赛事结果并放入到list中
	 * 
	 * @param url
	 * @param legBarSelect
	 * @param teamcSelect
	 * @param teamhSelect
	 * @param fullFirstSelect
	 * @param fullLastSelect
	 * @param league
	 * @return
	 */
	private static List<Map<String, String>> getGameResult(String url, String legBarSelect, String teamcSelect,
			String teamhSelect, String hrSelect, String fullSelect, String league) {
		// 加载url地址
		Document loadUrl = loadUrl(url);
		// 判断url地址是否连接成功
		if(loadUrl == null) {
			return null;
		}
		// 根据指定的位置查找数据
		Elements select = loadUrl.select(GAME_RESULT);
		List<Map<String, String>> listAll = new ArrayList<Map<String, String>>();
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> listMap = null;
		Elements legBar = null, teamh = null, teamc = null, hrFirst = null, hrLast = null, fullFirst = null,
				fullLast = null;
		for (Element element : select) {
			listMap = new HashMap<String, String>();
			legBar = element.select(legBarSelect);// 获取联赛
			// 判断联赛是否为空，为空则结束当前这一次循环
			if (StringUtil.isBlank(getNonBlankStr(legBar.html()))) {
				continue;
			}
			teamc = element.select(teamcSelect);// 获取第一个队伍
			teamh = element.select(teamhSelect);// 获取第二个队伍
			hrFirst = element.select(hrSelect).eq(0);// 获取上半场第一个比分
			hrLast = element.select(hrSelect).eq(1);// 获取上半场第二个比分
			fullFirst = element.select(fullSelect).eq(0);// 获取全场第一个比分
			fullLast = element.select(fullSelect).eq(1);// 获取全场第二个比分

			// 放入map集合
			listMap.put("legBar", legBar == null ? "" : getNonBlankStr(legBar.html()));
			listMap.put("teamc", teamc == null ? "" : getNonBlankStr(teamc.html()));
			listMap.put("teamh", teamh == null ? "" : getNonBlankStr(teamh.html()));
			// Element 需要判断是否为空，否则会空指针
			listMap.put("hrFirst", hrFirst == null ? "" : getNonBlankStr(hrFirst.html()));
			listMap.put("hrLast", hrLast == null ? "" : getNonBlankStr(hrLast.html()));
			listMap.put("fullFirst", fullFirst == null ? "" : getNonBlankStr(fullFirst.html()));
			listMap.put("fullLast", fullLast == null ? "" : getNonBlankStr(fullLast.html()));
			// 放入list集合
			listAll.add(listMap);
		}
		// 循环匹配数据并放入list中
		for (Map<String, String> map : listAll) {
			// System.out.println(map);
			if (StringUtil.isBlank(map.get("legBar"))) {
				return null;
			}
			if (league.equals(map.get("legBar"))) {
				list.add(map);
			}
		}
		return list;
	}

	/**
	 * 根据全部数据分割全场与半场字段
	 * 
	 * @param stringAll
	 * @return
	 */
	public static Map<String, List<String>> getFieldExplain(String stringAll) {
		// 用于分割全场与半场的索引
		int count = 0;
		List<String> fullList = new ArrayList<String>();
		List<String> hrList = new ArrayList<String>();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		String[] fieldArray = JsoupUtil.getField(stringAll, FIELD_BEGIN_STRING, FIELD_END_STRING);
		// 判断是否存在hgid
		if (!stringAll.contains("hgid")) {
			// 循环查找半场索引，赋值给count
			for (int i = 0; i < fieldArray.length; i++) {
				fullList.add(fieldArray[i]);
			}
		} else {
			// 循环查找半场索引，赋值给count
			for (int i = 0; i < fieldArray.length; i++) {
				if (fieldArray[i].equals("hgid")) {
					count = i;
					break;
				}
			}
			// 循环添加到两个不同的list中
			for (int i = 0; i < fieldArray.length; i++) {
				if (i >= count) {
					hrList.add(fieldArray[i]);
				} else {
					fullList.add(fieldArray[i]);
				}
			}
		}
		// 将list添加到map中
		map.put("fullList", fullList);
		map.put("hrList", hrList);
		return map;
	}

	/**
	 * 比率类型定义
	 * 
	 * @return
	 */
	public static Map<String, String> ratioType(String bet) {
		Map<String, String> map = new HashMap<String, String>();

		map.put("ior_OUH", "小");
		map.put("ior_OUC", "大");
		map.put("ior_MH", "独赢");
		map.put("ior_MC", "独赢");
		map.put("ior_MN", "独赢");
		map.put("ior_HOUH", "小");
		map.put("ior_HOUC", "大");
		map.put("ior_HMH", "独赢");
		map.put("ior_HMC", "独赢");
		map.put("ior_HMN", "独赢");
		if(bet.equals("FT")) {
			map.put("ior_HRH", "让球");
			map.put("ior_HRC", "让球");
			map.put("ior_RH", "让球");
			map.put("ior_RC", "让球");
			map.put("ior_EOO", "单");
			map.put("ior_EOE", "双");
		}
		if(bet.equals("BK")) {
			map.put("ior_RH", "让分");
			map.put("ior_RC", "让分");
			map.put("ior_HRH", "让分");
			map.put("ior_HRC", "让分");
			map.put("ior_OUHO", "单大");
			map.put("ior_OUHU", "单小");
			map.put("ior_OUCO", "单大");
			map.put("ior_OUCU", "单小");
		}
		return map;
	}
	/**
	 * 比率类型，只参与结果比分运算
	 * 
	 * @return
	 */
	public static List<String> getParticipantType(){
		List<String> list = new ArrayList<String>();
		list.add("大");
		list.add("小");
		list.add("单");
		list.add("双");
		list.add("单大");
		list.add("单小");
		return list;
	}
	/**
	 * 字符串去掉空格
	 * 
	 * @param str
	 * @return
	 */
	public static String getNonBlankStr(String str) {
		if (str != null && !"".equals(str)) {
			Pattern pattern = Pattern.compile("&ensp;|&nbsp;|\\s*|\t|\r|\n"); // 去掉空格符合换行符
			Matcher matcher = pattern.matcher(str);
			String result = matcher.replaceAll("");
			return result;
		} else {
			return str;
		}
	}
	/**
	 * 根据货币类型汇率转换
	 * @param currency
	 * @return
	 */
	public static String getExchange(String currency) {
		// 汇率转换地址
		String url = "https://otcbtc.com/buy_offers?currency="+currency+"&fiat_currency=cny&payment_type=all";
		// 加载url地址
		Document loadUrl = loadUrl(url);
		// 判断url地址是否连接成功
		if(loadUrl == null) {
			return null;
		}
		// 根据指定的位置查找数据
		Elements select = loadUrl.select(EXCHANGE_PRICE);
		// 获取文本内容
		String price = select.text();
		return price;
	}

	/**
	 * 根据url地址获取Document对象
	 * 
	 * @param url
	 * @return
	 */
	private static Document loadUrl(String url) {
		try {
			Document doc = Jsoup.connect(url).get();
			return doc;
		} catch (IOException e) {
			System.err.println("url地址连接失败！");
			return null;
		}
	}
}
