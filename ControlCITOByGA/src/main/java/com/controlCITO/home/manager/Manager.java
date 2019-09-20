package com.controlCITO.home.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.controlCITO.home.Cplx.NewSCplx;
import com.controlCITO.home.Cplx.SCplx;
import com.controlCITO.home.GA.GeneticAlgorithms;
import com.controlCITO.home.NGA.NGeneticAlgorithms;
import com.controlCITO.home.NewGA.NewGeneticAlgorithms;
import com.controlCITO.home.RIA.RIA;
import com.controlCITO.home.analysis.Analyzer;

public class Manager {
	public static ResultData test(int i, String fileName) throws Exception {
		Analyzer analyzer = new Analyzer();
		analyzer.analysis(fileName);
		InitInformation initInformation = new InitInformation();
		initInformation.initData(analyzer);
		return run(i,fileName,initInformation);
	}
	public static ResultData run(int i, String fileName,InitInformation initInformation) throws Exception {

		String time = null;
		NewSCplx.initWa(initInformation.getListOfSrc());
		ResultData result = new ResultData();
		List<ClassInfo> globalBestOrder=new ArrayList<>();
		if (i == 0) {
			long time1 = System.currentTimeMillis();
			// 设置退火初始温度
			double T = 100.0;
			// 设置降温系数
			double a = 0.95;
			// 设置运行代数
			int Generations = 100;
			RIA ria = new RIA(Generations, initInformation.getListOfSrc(), T, a);
			ria.run();
			globalBestOrder = ria.getGlobalBestOrder();
			long time2 = System.currentTimeMillis();
			time = "RIA run time :" + (time2 - time1) + "ms";
			result.setAlgorithm("RIA");
		}
		if (i == 1) {
			long time1 = System.currentTimeMillis();
			GeneticAlgorithms ga = new GeneticAlgorithms(initInformation.getListOfSrc());
			ga.generateClassListBasedGA();
			globalBestOrder = ga.getGlobalBestOrder();
			long time2 = System.currentTimeMillis();
			time = "GA run time :" + (time2 - time1) + "ms";
			result.setAlgorithm("GA");
		}if (i == 2) {
			long time1 = System.currentTimeMillis();
			NGeneticAlgorithms ga = new NGeneticAlgorithms(initInformation.getListOfSrc());
			ga.generateClassListBasedGA();
			globalBestOrder = ga.getGlobalBestOrder();
			long time2 = System.currentTimeMillis();
			time = "GA’ run time :" + (time2 - time1) + "ms";
			result.setAlgorithm("NGA");
		}
		if (i == 3) {
			long time1 = System.currentTimeMillis();
			NewGeneticAlgorithms ga = new NewGeneticAlgorithms(initInformation.getListOfSrc());
			ga.generateClassListBasedGA();
			globalBestOrder = ga.getGlobalBestOrder();
			long time2 = System.currentTimeMillis();
			time = "GA’’ run time :" + (time2 - time1) + "ms";
			result.setAlgorithm("NewGA");
		}
		result = calculate(globalBestOrder);
		result.setListOfM(ResultData.toGenerateTable(initInformation.getListOfM(), initInformation.getClassNames()));
		result.setListOfA(ResultData.toGenerateTable(initInformation.getListOfA(), initInformation.getClassNames()));
		result.setFileName(fileName);
		result.setAllTime(time);
		result.setClassNames(initInformation.getClassNames());
		result.setSetOfRelation(initInformation.getSetOfRelation());
		result.setListClassInformation(initInformation.getListClassInformation());
		result.setListClassLinks(initInformation.getListClassLinks());
		return result;
	}

	// 对最优解的相关计算及输出
	public static ResultData calculate(List<ClassInfo> order) {
		List<Map<String, String>> myorder = new ArrayList<>();
		for (int i = 0; i < order.size(); i++) {
			System.out.println(order.get(i).getSootCNo());
			Map<String, String> one = new HashMap<>();
			one.put(String.valueOf(order.get(i).getSootCNo()), order.get(i).getSootCName());
			myorder.add(one);
		}
		ResultData result = SCplx.calculate(order);
		Double fitByEntropy =NewSCplx.calculateOplx(order);
		result.setFitByEntropy(BigDecimal.valueOf(fitByEntropy));
		result.setOrder(myorder);
		return result;
	}

}