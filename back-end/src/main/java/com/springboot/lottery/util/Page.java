package com.springboot.lottery.util;

import java.util.List;

public class Page<T> {
	// 成功信息
	private String code = MessageUtil.SUCCESS;

	// 当前页码 默认显示第一页
	private int pageNo = 1;

	// 每页显示数据行数 默认每页显示10行
	private int pageSize = 10;

	// 总共数据行数
	private long total;

	// 数据总页数
	private long pageCount;

	// 当前页要显示的数据集合
	private List<T> result;

	public Page(int pageNo, int pageSize) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
	}

	public Page(int pageNo, int pageSize, long total, List<T> result) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.total = total;
		this.pageCount = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
		this.result = result;
	}
	public Page(long total, List<T> result) {
		this.total = total;
		this.pageCount = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
		this.result = result;
	}
	public Page() {
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getPageCount() {
		return pageCount;
	}

	public void setPageCount(long pageCount) {
		this.pageCount = pageCount;
	}

	
	public List<T> getResult() {
		return result;
	}

	public void setResult(List<T> result) {
		this.result = result;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
