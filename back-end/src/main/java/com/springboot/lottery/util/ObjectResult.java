package com.springboot.lottery.util;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 接口返回结果标准数据结构(非集合) 如{ "code":0, "result":{....} }
 * 
 * @author Administrator
 *
 */
public class ObjectResult implements Serializable {

	private static final long serialVersionUID = 8730976317980727734L;
	private String code = MessageUtil.SUCCESS;
	private Object result = new HashMap<String, String>();

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, new String[0]);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, new String[0]);
	}

	public ObjectResult() {
	}

	public ObjectResult(String code, Object result) {
		this.result = code;
		if (result != null)
			this.result = result;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}
