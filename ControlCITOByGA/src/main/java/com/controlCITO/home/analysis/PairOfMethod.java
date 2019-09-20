package com.controlCITO.home.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PairOfMethod {
	// 方法对
	private String sourceCName; // 源方法所在的类
	private String sourceMName; // 源方法名
	private String targetCName; // 目标方法所在的类
	private String targetMName; // 目标方法名
	private int num; // 该方法对的个数
	private List<Integer> lines; // 该方法调用所在行数
	private Map<Integer, Integer> line_branches; //该方法调用行数-路径条件个数

	public PairOfMethod(String sourceCName, String sourceM, String targetCName,
			String targetM) {
		this.sourceCName = sourceCName;
		this.sourceMName = sourceM;
		this.targetCName = targetCName;
		this.targetMName = targetM;
		this.num = 1;
		this.lines = new ArrayList<Integer>();
	}

	public PairOfMethod(String sourceCName, String sourceM, String targetCName,
			String targetM, int lineNo) {
		this.sourceCName = sourceCName;
		this.sourceMName = sourceM;
		this.targetCName = targetCName;
		this.targetMName = targetM;
		this.num = 1;
		this.lines = new ArrayList<Integer>();
		this.lines.add(lineNo);
	}

	public PairOfMethod(String sourceCName, String sourceM, String targetCName,
			String targetM, int lineNo, int numOfBranch){
		this.sourceCName = sourceCName;
		this.sourceMName = sourceM;
		this.targetCName = targetCName;
		this.targetMName = targetM;
		this.num = 1;
		this.lines = new ArrayList<Integer>();
		this.lines.add(lineNo);
		this.line_branches = new HashMap<Integer, Integer>();
		this.line_branches.put(lineNo, numOfBranch);
	}
	
	public String toString() {
		String s = sourceCName + ":" + sourceMName + "--->" + targetCName + ":"
				+ targetMName;
		return s;
	}

	public boolean equals(PairOfMethod pm) {
		boolean equal = false;
		if (this.toString().equals(pm.toString())) {
			equal = true;
		}
		return equal;
	}

	public void addnum() {
		num = num + 1;
	}

	// 返回该方法调用对的数目
	public int getNum() {
		return num;
	}

	// 当该方法调用出现多次时，加入行号
	public void addLineNo(int lineNo) {
		this.lines.add(lineNo);
	}

	public List<Integer> getLineNo(){
		return this.lines;
	}
	
	// 判断pm是否和本方法构成传递依赖关系
	public int transition(PairOfMethod pm) {
		int a = -1;
		if (getTargetMethod().equals(pm.getSourceMethod())) {
			a = 0; // this->pm
		} else if (pm.getTargetMethod().equals(getSourceMethod())) {
			a = 1; // pm->this
		}
		return a;
	}

	private String getSourceMethod() {
		String s = sourceCName + ":" + sourceMName;
		return s;
	}

	public String getTargetMethod() {
		String s = targetCName + ":" + targetMName;
		return s;
	}

	public String getSourceClass(){
		return this.sourceCName;
	}
	
	public String getTargetClass(){
		return this.targetCName;
	}

	public void addBranch(int lineNo, int numOfBranch) {
		this.line_branches.put(lineNo, numOfBranch);
	}

	// 排除 A-B-A 的情况
	public boolean intraEquals(PairOfMethod intrapm) {
		boolean is = false;
		if(this.sourceCName.equals(intrapm.getTargetClass())){			
			is = true;
		}
		return is;
	}
}
