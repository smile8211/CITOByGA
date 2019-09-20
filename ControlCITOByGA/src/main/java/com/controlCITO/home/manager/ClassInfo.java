package com.controlCITO.home.manager;

import java.util.Map;
import java.util.Map.Entry;

public class ClassInfo {

	private int sootCNo; // 该类的编号
	private String sootCName; // 该类名称
	private Map<String, Integer> attrDeps; // 与其他类属性依赖数目集合
	private Map<String, Integer> methodDeps; // 与其他类方法依赖数目集合
	private int parentNo; // 该类的父类序号
	private String parentCName; // 该类的名字
	
	public ClassInfo(int i, String string) {
		this.sootCNo = i;
		this.sootCName = string;
	}
	
	public int getSootCNo() {
		return sootCNo;
	}

	public void setSootCNo(int sootCNo) {
		this.sootCNo = sootCNo;
	}

	public String getSootCName() {
		return sootCName;
	}

	public void setSootCName(String sootCName) {
		this.sootCName = sootCName;
	}

	public Map<String, Integer> getAttrDeps() {
		return attrDeps;
	}

	public void setAttrDeps(Map<String, Integer> attrDeps) {
		this.attrDeps = attrDeps;
	}

	public Map<String, Integer> getMethodDeps() {
		return methodDeps;
	}

	public void setMethodDeps(Map<String, Integer> methodDeps) {
		this.methodDeps = methodDeps;
	}

	public int getParentNo() {
		return parentNo;
	}

	public void setParentNo(int parentNo) {
		this.parentNo = parentNo;
	}

	public String getParentCName() {
		return parentCName;
	}

	public void setParentCName(String parentCName) {
		this.parentCName = parentCName;
	}
	// 设置方法最大值
	public int searchMaxMethodDep() {
		int maxMethodDep = 0;
		if (this.methodDeps != null) {
			for (Entry<String, Integer> entry : this.methodDeps.entrySet()) {
				if (maxMethodDep < entry.getValue().intValue())
					maxMethodDep = entry.getValue().intValue();
			}
		}
		return maxMethodDep;
	}
	// 设置属性最大值
	public int searchMaxAttrDep() {
		int maxAttrDep = 0;
		if (this.attrDeps != null) {
			for (Entry<String, Integer> entry : this.attrDeps.entrySet()) {
				if (maxAttrDep < entry.getValue().intValue())
					maxAttrDep = entry.getValue().intValue();
			}
		}
		return maxAttrDep;
	}
}
