package com.controlCITO.home.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.controlCITO.home.analysis.Analyzer;
import com.controlCITO.home.analysis.ClassInformation;
import com.controlCITO.home.analysis.ClassLinks;

public class InitInformation {
	private String[] classNames; // 类名列表
	private Map<String, String> listOfInherited; // 继承关系列表
	private Map<String, Map<String, Integer>> listOfM; // 方法依赖列表
	private Map<String, Map<String, Integer>> listOfA; // 属性依赖列表
	private List<ClassInfo> listOfSrc; // 类信息列表
	private Set<String> setOfRelation; // 类依赖列表
	// 生成UML图需要的
	private List<ClassInformation> listClassInformation; // 方法调用列表
	private List<ClassLinks> listClassLinks; // 同一个类中的方法调用

	public InitInformation() {
		this.listOfSrc = new ArrayList<ClassInfo>();
		this.setOfRelation = new HashSet<String>();
		this.listOfInherited = new HashMap<>();
		this.listOfM = new HashMap<String, Map<String, Integer>>();
		this.listOfA = new HashMap<String, Map<String, Integer>>();
		this.listClassInformation = new ArrayList<ClassInformation>();
		this.listClassLinks = new ArrayList<ClassLinks>();
	}

	public void initData(Analyzer analyzer) {
		this.classNames = analyzer.getClassNo();
		this.listOfInherited = analyzer.getInherited();
		this.listOfM = analyzer.getMethodDeps();
		this.listOfA = analyzer.getAttrDeps();
		this.listClassInformation = analyzer.getListClassInformation();
		this.listClassLinks = analyzer.getListClassLinks();
		initClassInfoListAndSetOfRelation();
	}

	public String[] getClassNames() {
		return classNames;
	}

	public List<ClassInformation> getListClassInformation() {
		return listClassInformation;
	}

	public void setListClassInformation(List<ClassInformation> listClassInformation) {
		this.listClassInformation = listClassInformation;
	}

	public List<ClassLinks> getListClassLinks() {
		return listClassLinks;
	}

	public void setListClassLinks(List<ClassLinks> listClassLinks) {
		this.listClassLinks = listClassLinks;
	}

	public void setClassNames(String[] classNames) {
		this.classNames = classNames;
	}

	public List<ClassInfo> getListOfSrc() {
		return listOfSrc;
	}

	public void setListOfSrc(List<ClassInfo> listOfSrc) {
		this.listOfSrc = listOfSrc;
	}

	public Set<String> getSetOfRelation() {
		return setOfRelation;
	}

	public void setSetOfRelation(Set<String> setOfRelation) {
		this.setOfRelation = setOfRelation;
	}

	public Map<String, String> getListOfInherited() {
		return listOfInherited;
	}

	public void setListOfInherited(Map<String, String> listOfInherited) {
		this.listOfInherited = listOfInherited;
	}

	public Map<String, Map<String, Integer>> getListOfM() {
		return listOfM;
	}

	public void setListOfM(Map<String, Map<String, Integer>> listOfM) {
		this.listOfM = listOfM;
	}

	public Map<String, Map<String, Integer>> getListOfA() {
		return listOfA;
	}

	public void setListOfA(Map<String, Map<String, Integer>> listOfA) {
		this.listOfA = listOfA;
	}

	// 生成类信息列表
	public void initClassInfoListAndSetOfRelation() {
		int num = classNames.length;
		for (int i = 0; i <= num - 1; i++) {
			String sourceCName = classNames[i];
			ClassInfo sourceClass = new ClassInfo(i, sourceCName);
			addSourceClassInfo(sourceCName, sourceClass);
			this.listOfSrc.add(sourceClass);
		}
		recordADeps(this.listOfA);
		recordMDeps(this.listOfM);
	}

	// 为ClassInfo增加相应的信息
	private void addSourceClassInfo(String sourceCName, ClassInfo sourceClass) {
		// 增加ClassInfo的父类
		String pName = searchParentName(sourceCName);
		sourceClass.setParentCName(pName);
		int pNo = searchClassNo(pName);
		sourceClass.setParentNo(pNo);
		// 增加ClassInfo的属性依赖
		Map<String, Integer> attrMap = initDirectAttrDeps(sourceCName);
		sourceClass.setAttrDeps(attrMap);
		// 增加ClassInfo的直接方法依赖
		Map<String, Integer> methodMap = initDirectMethodDeps(sourceCName);
		sourceClass.setMethodDeps(methodMap);
	}

	// 获取sourceCName的父类
	private String searchParentName(String sourceCName) {
		String parentName = null;
		if (this.listOfInherited != null) {
			for (Map.Entry<String, String> entry : listOfInherited.entrySet()) {
				if (sourceCName.equals(entry.getKey())) {
					parentName = entry.getValue();
					break;
				}

			}
		}
		return parentName;
	}

	// 根据类名获取该类的编号
	public int searchClassNo(String cname) {
		for (int i = 0; i <= classNames.length - 1; i++) {
			if (classNames[i].equals(cname)) {
				return i;
			}
		}
		return -1;
	}

	// 根据sourceCName获取其attrDeps
	private Map<String, Integer> initDirectAttrDeps(String sourceCName) {
		if (listOfA != null && listOfA.size() != 0) {
			if (listOfA.containsKey(sourceCName)) {
				return listOfA.get(sourceCName);
			}
		}
		return null;
	}

	// 根据sourceCName获取其methodDeps
	private Map<String, Integer> initDirectMethodDeps(String sourceCName) {
		if (listOfM != null && listOfM.size() != 0) {
			if (listOfM.containsKey(sourceCName)) {
				return listOfM.get(sourceCName);
			}
		}
		return null;
	}

	// 将属性依赖加入到setOfRelation中0-->1
	private void recordADeps(Map<String, Map<String, Integer>> attrDeps) {
		if (attrDeps != null && attrDeps.size() != 0) {
			for (Map.Entry<String, Map<String, Integer>> entry : attrDeps.entrySet()) {
				String srcName = entry.getKey();
				int srcID = searchClassNo(srcName);

				for (Map.Entry<String, Integer> ventry : attrDeps.get(srcName).entrySet()) {
					String desName = ventry.getKey();
					int desID = searchClassNo(desName);
					String dep = srcID + "-->" + desID;
					this.setOfRelation.add(dep);
				}
			}
		}
	}

	// 将方法依赖加入到setOfRelation中
	private void recordMDeps(Map<String, Map<String, Integer>> methodDeps) {
		if (methodDeps != null && methodDeps.size() != 0) {
			for (Map.Entry<String, Map<String, Integer>> entry : methodDeps.entrySet()) {
				String srcName = entry.getKey();
				int srcID = searchClassNo(srcName);

				for (Map.Entry<String, Integer> ventry : methodDeps.get(srcName).entrySet()) {
					String desName = ventry.getKey();
					int desID = searchClassNo(desName);
					String dep = srcID + "-->" + desID;
					this.setOfRelation.add(dep);
				}
			}
		}
	}
}
