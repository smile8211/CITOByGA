package com.controlCITO.home.Cplx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.controlCITO.home.manager.ClassInfo;
import com.controlCITO.home.manager.ResultData;

public class NewSCplx {

	private static Double wa = 0.0;
	public static void initWa(List<ClassInfo> listOfSrc) {
		wa = getByEntropy(listOfSrc);
	}
	// 计算类i和类j间的测试桩复杂度(**************熵权法**********************)
	public static double calculateSCplxByEntropy(List<ClassInfo> listOfSrc, ClassInfo sci_i, ClassInfo sci_k) {
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
		// 计算最大值
		int maxAttr = calMaxAttr(listOfSrc);
		int maxMethod = calMaxMethod(listOfSrc);
		// 归一化处理
		double _sizeOfAttr = (double) sizeOfAttr / maxAttr;
		double _sizeOfMethod = (double) sizeOfMethod / maxMethod;
		// 获得权值
		// BigDecimal wa = getByEntropy();
		if (wa == 0.0) {
			wa = getByEntropy(listOfSrc);
			System.out.println("wa=======" + wa);
		}
		// 计算终值
		scplx = Math.sqrt(
				_sizeOfAttr * _sizeOfAttr * wa.doubleValue() + _sizeOfMethod * _sizeOfMethod * (1 - wa.doubleValue()));
		return scplx;
	}

	// 返回属性依赖权值
	private static Double getByEntropy(List<ClassInfo> listOfSrc) {
		// 找出m依赖个数对应关系m行2列矩阵(最后要一致)
		Map<Integer, Integer> rA = new HashMap<>();
		Map<Integer, Integer> rM = new HashMap<>();
		int m = 1;
		for (ClassInfo src : listOfSrc) {
			if (src.getAttrDeps() != null) {// 有属性和方法依赖，有属性无方法依赖
				for (String akey : src.getAttrDeps().keySet()) {
					rA.put(m, src.getAttrDeps().get(akey));
					Integer rm = 0;
					if (src.getMethodDeps() != null) {
						for (String mkey : src.getMethodDeps().keySet()) {
							if (akey == mkey) {
								rm = src.getMethodDeps().get(mkey);
							}
						}
					}
					rM.put(m, rm);
					m++;
				}
			}
			if (src.getMethodDeps() != null) {// 有方法无属性依赖
				for (String mkey : src.getMethodDeps().keySet()) {
					rM.put(m, src.getMethodDeps().get(mkey));
					Integer ra = 0;
					if (src.getAttrDeps() != null) {
						for (String akey : src.getAttrDeps().keySet()) {
							if (akey == mkey) {
								ra = src.getAttrDeps().get(akey);
							}
						}
					}
					if (ra != 0) {
						rM.remove(m);
					} else {
						rA.put(m, ra);
						m++;
					}
				}
			}
		}
		m = m - 1;
		// 使用上面的矩阵计算属性依赖和方法依赖各自总和
		Integer sumAttr1 = 0;
		Integer sumMethod1 = 0;
		for (Integer key : rA.keySet()) {
			sumAttr1 = sumAttr1 + rA.get(key);
		}
		for (Integer key : rM.keySet()) {
			sumMethod1 = sumMethod1 + rM.get(key);
		}
		// 计算pa和pm属性和方法矩阵集合
		Map<Integer, BigDecimal> pA = new HashMap<>();
		Map<Integer, BigDecimal> pM = new HashMap<>();
		for (Integer key : rA.keySet()) {
			if (rA.get(key) == 0) {
				pA.put(key, new BigDecimal("0"));
			} else {
				pA.put(key, new BigDecimal(rA.get(key).toString()).divide(new BigDecimal(sumAttr1.toString()), 10,
						BigDecimal.ROUND_HALF_UP));
			}
		}
		for (Integer key : rM.keySet()) {
			if (rM.get(key) == 0) {
				pM.put(key, new BigDecimal("0"));
			} else {
				pM.put(key, new BigDecimal(rM.get(key).toString()).divide(new BigDecimal(sumMethod1.toString()), 10,
						BigDecimal.ROUND_HALF_UP));
			}
		}
		// 计算信息熵eA和eM
		BigDecimal sumPaMultiplylogPa = new BigDecimal("0");
		BigDecimal sumPmMultiplylogPm = new BigDecimal("0");
		BigDecimal eA;
		BigDecimal eM;
		for (Integer key : pA.keySet()) {
			double a = pA.get(key).doubleValue();
			if (a == 0.0) {
				sumPaMultiplylogPa = sumPaMultiplylogPa.add(new BigDecimal("0"));
			} else {
				sumPaMultiplylogPa = sumPaMultiplylogPa.add((pA.get(key)).multiply(BigDecimal.valueOf(Math.log(a))));
			}
		}
		for (Integer key : pM.keySet()) {
			double a = pM.get(key).doubleValue();
			if (a == 0.0) {
				sumPmMultiplylogPm = sumPmMultiplylogPm.add(new BigDecimal("0"));
			} else {
				sumPmMultiplylogPm = sumPmMultiplylogPm.add((pM.get(key)).multiply(BigDecimal.valueOf(Math.log(a))));
			}
		}
		eA = (new BigDecimal("-1").divide(BigDecimal.valueOf(Math.log(m)), 10, BigDecimal.ROUND_HALF_UP))
				.multiply(sumPaMultiplylogPa);
		eM = (new BigDecimal("-1").divide(BigDecimal.valueOf(Math.log(m)), 10, BigDecimal.ROUND_HALF_UP))
				.multiply(sumPmMultiplylogPm);
		// 计算权重wa(为什么是负的？)
		BigDecimal wA = (new BigDecimal("1").subtract(eA)).divide(new BigDecimal("2").subtract(eA.add(eM)), 10,
				BigDecimal.ROUND_HALF_UP);
		return wA.doubleValue();
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

	private static ClassInfo getCIByCName(List<ClassInfo> listOfSrc, String depClass) {
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
						ClassInfo scd = getCIByCName(order, depClass);
						// 单个测试桩复杂度
						BigDecimal fitOne = new BigDecimal("0");
						// 计算总体测试桩复杂度
						fitOne = BigDecimal.valueOf(calculateSCplxByEntropy(order, sci, scd));
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
		result.setFitByEntropy(fit);
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

	private static void printCalculate(BigDecimal fit, int attrCplx, int methodCplx, int stubs,
			List<String> listOfStubs) {
		System.out.println("总体测试桩复杂度OCplx为:" + fit);
		System.out.println("属性复杂度ACplx为:" + attrCplx);
		System.out.println("方法复杂度MCplx为:" + methodCplx);
		System.out.println("测试桩数目为：" + stubs);
	}

	// 计算一个个体总复杂度(**************熵权法**********************)
	public static Double calculateOplx(List<ClassInfo> order) {
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
						double complex = calculateSCplxByEntropy(order, sci, scd);
						fit = fit.add(new BigDecimal(complex));
					}
				}
			}
		}
		return fit.doubleValue();
	}
}
