package com.controlCITO.home.Cplx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.controlCITO.home.manager.ClassInfo;
import com.controlCITO.home.manager.ResultData;

public class SCplx {
	// 计算类i和类j间的测试桩复杂度
	public static double calculateSCplx(List<ClassInfo> listOfSrc,ClassInfo sci_i, ClassInfo sci_k) {
		// 初始化
		double scplx = 0.0;
		int sizeOfAttr = 0;
		int sizeOfMethod = 0;
		String targetCName = sci_k.getSootCName();
		// 计算属性依赖值
		if (sci_i.getAttrDeps() != null && sci_i.getAttrDeps().containsKey(targetCName)) {
			sizeOfAttr = sci_i.getAttrDeps().get(targetCName);
		} else {
			sizeOfAttr = 0;
		}
		// 计算方法依赖值
		if (sci_i.getMethodDeps() != null && sci_i.getMethodDeps().containsKey(targetCName)) {
			sizeOfMethod = sci_i.getMethodDeps().get(targetCName);
		} else {
			sizeOfMethod = 0;
		}
		int maxAttr = calMaxAttr(listOfSrc);
		int maxMethod = calMaxMethod(listOfSrc);
		// 归一化处理
		double _sizeOfAttr = (double) sizeOfAttr / maxAttr;
		double _sizeOfMethod = (double) sizeOfMethod / maxMethod;
		scplx = Math.sqrt(_sizeOfAttr * _sizeOfAttr / 2 + _sizeOfMethod * _sizeOfMethod / 2);
		return scplx;
	}

	private static int calMaxMethod(List<ClassInfo> listOfSrc) {
		int maxmethod = 0;
		for (ClassInfo cinfo : listOfSrc) {
			if (maxmethod < cinfo.searchMaxMethodDep())
				maxmethod = cinfo.searchMaxMethodDep();
		}
		return maxmethod;
	}

	private static int calMaxAttr(List<ClassInfo> listOfSrc) {
		int maxattr = 0;
		for (ClassInfo cinfo : listOfSrc) {
			if (maxattr < cinfo.searchMaxAttrDep())
				maxattr = cinfo.searchMaxAttrDep();
		}
		return maxattr;
	}
	// 根据类名depClass 获取ClassInfo

	private static ClassInfo getCIByCName(List<ClassInfo> listOfSrc,String depClass) {
		ClassInfo cinfo = null;
		for (ClassInfo ci : listOfSrc) {
			if (ci.getSootCName().equals(depClass)) {
				cinfo = ci;
				break;
			}
		}
		return cinfo;
	}

	// 输出序列的SCplx\ACplx\MCplx\TCplx\Stubs
	public static ResultData calculate(List<ClassInfo> order) {
		// 总体测试桩复杂度
		BigDecimal fit = new BigDecimal("0");
		// 属性测试桩调用值
		int attrCplx = 0;
		// 方法测试桩调用值
		int methodCplx = 0;
		// 测试桩数目
		int stubs = 0;
		// 测试桩列表
		List<String> listOfStubs = new ArrayList<String>();
		// 评估过的类名的集合
		Set<String> classNames = new HashSet<String>();

		for (ClassInfo sci : order) {
			String sci_name = sci.getSootCName();
			classNames.add(sci_name);
			// 目标类集合
			// 当前类依赖的目标类
			Set<String> depClasses = new HashSet<String>();
			if (sci.getAttrDeps() != null) {
				Set<String> attrClasses = sci.getAttrDeps().keySet(); // String类型的类名
				depClasses.addAll(attrClasses);
			}
			if (sci.getMethodDeps() != null) {
				Set<String> methodClasses = sci.getMethodDeps().keySet(); // String类型的类名
				depClasses.addAll(methodClasses);
			}
			// 当前类依赖的类是之前没有评估过的（没有在前面）
			if (depClasses.size() != 0) {
				for (String depClass : depClasses) {
					if (!classNames.contains(depClass)) {
						ClassInfo scd = getCIByCName(order,depClass);
						// 单个测试桩复杂度
						BigDecimal fitOne = new BigDecimal("0");
						// 计算总体测试桩复杂度
						fitOne = new BigDecimal(calculateSCplx(order,sci, scd));
						fit = fit.add(fitOne);
						// 计算属性调用值
						int a = calculateA(sci, scd);
						attrCplx = attrCplx + a;
						// 计算方法调用值
						int m = calculateM(sci, scd);
						methodCplx = methodCplx + m;
						// 计算测试桩数目
						stubs = stubs + 1;
						// 记录所需的测试桩
						listOfStubs.add(sci.getSootCNo() + "-->" + scd.getSootCNo() + "--(" + a + ", " + m + ", "
								+ fitOne + ")");
					}
				}
			}
		}
		printCalculate(fit, attrCplx, methodCplx, stubs, listOfStubs);
		ResultData result = new ResultData();
		result.setAttrCplx(attrCplx);
		result.setFit(fit);
		result.setListOfStubs(listOfStubs);
		result.setMethodCplx(methodCplx);
		result.setStubs(stubs);
		return result;
	}

	// 统计sci对scd的方法调用值
	private static int calculateM(ClassInfo sci, ClassInfo scd) {
		int sizeOfMethod = 0;
		String scd_name = scd.getSootCName();
		if (sci.getMethodDeps() != null && sci.getMethodDeps().containsKey(scd_name)) {
			sizeOfMethod = sci.getMethodDeps().get(scd_name);
		} else
			sizeOfMethod = 0;
		return sizeOfMethod;
	}

	// 统计sci对scd的属性调用值
	private static int calculateA(ClassInfo sci, ClassInfo scd) {
		int sizeOfAttr = 0;
		String scd_name = scd.getSootCName();
		if (sci.getAttrDeps() != null && sci.getAttrDeps().containsKey(scd_name)) {
			sizeOfAttr = sci.getAttrDeps().get(scd_name);
		} else
			sizeOfAttr = 0;
		return sizeOfAttr;
	}

	private static void printCalculate(BigDecimal fit, int attrCplx, int methodCplx, int stubs, List<String> listOfStubs) {
		System.out.println("总体测试桩复杂度OCplx为:" + fit);
		System.out.println("属性复杂度ACplx为:" + attrCplx);
		System.out.println("方法复杂度MCplx为:" + methodCplx);
		System.out.println("测试桩数目为：" + stubs);
	}
	// 适应度函数计算一个个体适应值
	public static Double calculateFitness(List<ClassInfo> order) {
		BigDecimal fit = new BigDecimal("0");
		// 评估过的类的集合
		Set<String> classNames = new HashSet<String>();
		for (ClassInfo sci : order) {
			String sci_name = sci.getSootCName();
			classNames.add(sci_name);

			// 目标类集合（当前类依赖（直接间接）的所有类集合）
			Set<String> depClasses = new HashSet<String>();
			if (sci.getAttrDeps() != null) {
				Set<String> attrClasses = sci.getAttrDeps().keySet(); // String类型的类名
				depClasses.addAll(attrClasses);
			}
			if (sci.getMethodDeps() != null) {
				Set<String> methodClasses = sci.getMethodDeps().keySet(); // String类型的类名
				depClasses.addAll(methodClasses);
			}
			if (depClasses.size() != 0) {
				for (String depClass : depClasses) {
					// 当前类依赖没有依赖已经测试过的类
					if (!classNames.contains(depClass)) {
						ClassInfo scd = getCIByCName(order, depClass);
						double complex = SCplx.calculateSCplx(order, sci, scd);
						fit = fit.add(new BigDecimal(complex));
					}
				}
			}
		}
		return fit.doubleValue();
	}
}
