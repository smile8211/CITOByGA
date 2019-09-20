package com.controlCITO.home.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.controlCITO.home.analysis.ClassInformation;
import com.controlCITO.home.analysis.ClassLinks;

public class ResultData {

	private String fileName;
	private String algorithm;
	private String[] classNames; // 类编号数组
	private Set<String> setOfRelation; // 类依赖列表
	private String[][] listOfM; // 方法依赖表
	private String[][] listOfA; // 属性依赖表
	private String allTime;
	private List<Map<String, String>> order = new ArrayList<>();
	private BigDecimal fit = new BigDecimal("0");// 总体测试桩复杂度
	private BigDecimal fitByEntropy = new BigDecimal("0");// 总体测试桩复杂度
	private int attrCplx = 0;// 属性测试桩复杂度
	private int methodCplx = 0; // 方法测试桩复杂度
	private int stubs = 0; // 测试桩数目
	private List<String> listOfStubs = new ArrayList<String>(); // 测试桩列表
	// 生成UML图需要的
	private List<ClassInformation> listClassInformation; // 方法调用列表
	private List<ClassLinks> listClassLinks; // 同一个类中的方法调用

	public ResultData() {
		this.listClassInformation = new ArrayList<ClassInformation>();
		this.listClassLinks = new ArrayList<ClassLinks>();
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

	public BigDecimal getFitByEntropy() {
		return fitByEntropy;
	}

	public void setFitByEntropy(BigDecimal fitByEntropy) {
		this.fitByEntropy = fitByEntropy;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String[][] getListOfM() {
		return listOfM;
	}

	public void setListOfM(String[][] listOfM) {
		this.listOfM = listOfM;
	}

	public String[][] getListOfA() {
		return listOfA;
	}

	public void setListOfA(String[][] listOfA) {
		this.listOfA = listOfA;
	}

	public List<Map<String, String>> getOrder() {
		return order;
	}

	public void setOrder(List<Map<String, String>> order) {
		this.order = order;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String[] getClassNames() {
		return classNames;
	}

	public void setClassNames(String[] classNames) {
		this.classNames = classNames;
	}

	public Set<String> getSetOfRelation() {
		return setOfRelation;
	}

	public void setSetOfRelation(Set<String> setOfRelation) {
		this.setOfRelation = setOfRelation;
	}

	public String getAllTime() {
		return allTime;
	}

	public void setAllTime(String allTime) {
		this.allTime = allTime;
	}

	public BigDecimal getFit() {
		return fit;
	}

	public void setFit(BigDecimal fit) {
		this.fit = fit;
	}

	public int getAttrCplx() {
		return attrCplx;
	}

	public void setAttrCplx(int attrCplx) {
		this.attrCplx = attrCplx;
	}

	public int getMethodCplx() {
		return methodCplx;
	}

	public void setMethodCplx(int methodCplx) {
		this.methodCplx = methodCplx;
	}

	public int getStubs() {
		return stubs;
	}

	public void setStubs(int stubs) {
		this.stubs = stubs;
	}

	public List<String> getListOfStubs() {
		return listOfStubs;
	}

	public void setListOfStubs(List<String> listOfStubs) {
		this.listOfStubs = listOfStubs;
	}

	public static String[][] toGenerateTable(Map<String, Map<String, Integer>> map, String[] firstLine) {
		String[][] table = new String[firstLine.length + 1][firstLine.length + 1];
		for (int i = 0; i < firstLine.length; i++) {
			table[0][i + 1] = firstLine[i];
		}
		for (int i = 0; i < firstLine.length; i++) {
			table[i + 1][0] = firstLine[i];
		}
		for (int i = 1; i < firstLine.length + 1; i++) {
			for (String key : map.keySet()) {
				Map<String, Integer> value = map.get(key);
				if (table[i][0].equals(key)) {
					for (int j = 1; j < firstLine.length + 1; j++) {
						for (String key2 : value.keySet()) {
							Integer value2 = value.get(key2);
							if (table[0][j].equals(key2)) {
								table[i][j] = Integer.toString(value2);
							}
						}
					}
				}
			}
		}
		for (int i = 0; i < firstLine.length; i++) {
			table[0][i + 1] = Integer.toString(i);
		}
		for (int i = 0; i < firstLine.length; i++) {
			table[i + 1][0] = Integer.toString(i);
		}
		return table;
	}
}
