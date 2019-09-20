package com.controlCITO.home.RIA;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.controlCITO.home.manager.ClassInfo;
import com.controlCITO.home.Cplx.*;

public class RIA {

	private int Generations; // 运行代数
	private List<ClassInfo> listOfRIA; // 种群信息
	private double T = 100.0; // 退火初始温度
	private double a; // 降温系数
	private double finalT = 0.0; // 终止温度

	private List<ClassInfo> globalBestOrder; // 全局最优解
	private BigDecimal globalBestfitness = new BigDecimal("10000000");// 全局最优适应度值
	private List<ClassInfo> localBestOrder; // 局部最优解
	private BigDecimal localBestfitness; // 局部最优适应度值

	public RIA(int Generations, List<ClassInfo> listOfRIA, double T, double a) {
		this.Generations = Generations;
		this.listOfRIA = listOfRIA;
		this.T = T;
		this.a = a;
	}
	
	@SuppressWarnings("unused")
	public void run(){
		List<ClassInfo> listOfgen = new ArrayList<ClassInfo>();
		// 初步生成最优解
		listOfgen = generateClassListBasedonRIA(listOfRIA);
		// 对最优解进行优化
//		optimizeClassListBasedonRIA(listOfgen);
	}
	
	

//	public void optimizeClassListBasedonRIA(List<ClassInfo> listOfRIA) {
//		int generations = 1;
//		List<ClassInfo> initialOrder = listOfRIA;
//		while (generations <= 5) {
//			List<ClassInfo> riaOrder = new ArrayList<ClassInfo>();
//			for (int count = 0; count <= 5; count++) {
//				riaOrder = doRIA(initialOrder);
//				if (calculateFitness(riaOrder).compareTo(
//						calculateFitness(initialOrder)) == -1) {
//					break;
//				}
//			}
//			if (calculateFitness(riaOrder).compareTo(
//					calculateFitness(initialOrder)) == -1) {
//				localBestOrder = riaOrder;
//				localBestfitness = calculateFitness(riaOrder);
//			} else {
//				localBestOrder = initialOrder;
//				localBestfitness = calculateFitness(initialOrder);
//			}
//			
//			initialOrder = localBestOrder;
//			generations++;
//		}
//		System.out.println("---- RIA  CITO ------");
//		globalBestOrder = localBestOrder;
//		print(globalBestOrder);
//	}

	public List<ClassInfo> generateClassListBasedonRIA(List<ClassInfo> listOfRIA) {
		int generations = 1;
		while (generations <= Generations) {	
			System.out.println("第"+generations+"次");
			if(globalBestOrder!=null) {
				System.out.println(SCplx.calculateFitness(globalBestOrder));
			}
			List<ClassInfo> initialOrder = initialOrder(listOfRIA);
			List<ClassInfo> riaOrder = doRIA(initialOrder);
			if (calculateFitness(riaOrder).compareTo(
					calculateFitness(initialOrder)) == -1) {
				localBestOrder = riaOrder;
				localBestfitness = calculateFitness(riaOrder);
			} else {
				localBestOrder = initialOrder;
				localBestfitness = calculateFitness(initialOrder);
			}
			if (localBestfitness.compareTo(globalBestfitness) == -1) {
				globalBestOrder = localBestOrder;
				globalBestfitness = localBestfitness;
			}
			generations++;
		}
		return globalBestOrder;
	}

	/*
	 * 初始化序列
	 */
	private List<ClassInfo> initialOrder(List<ClassInfo> listOfRIA) {

		List<ClassInfo> sci_temp = new LinkedList<ClassInfo>();
		for (ClassInfo sci : listOfRIA) {
			sci_temp.add(sci);
		}

		int n = 0;
		List<ClassInfo> order = new LinkedList<ClassInfo>();
		Random random = new Random();
		while (sci_temp.size() != 0) {
			n = random.nextInt(sci_temp.size());
			order.add(sci_temp.get(n));
			sci_temp.remove(n);
		}
		return order;
	}

	/*
	 * 执行随机交互操作 结合爬山算法和模拟退火 是否需要加入终止温度？
	 */
	private List<ClassInfo> doRIA(List<ClassInfo> order) {

		// 创建数组sortOrder记录随机迭代后的序列中各类的位置
		int[] sortOrder = new int[order.size()];
		for (int a = 0; a <= sortOrder.length - 1; a++) {
			sortOrder[a] = a;
		}

		int k = 0; // 每次比较的类
		loop: for (int j = 0; j <= order.size() - 2; j++) { // j之前即已确定的序列
			k = j + 1;
			for (int i = 0; i <= j; i++) { // p 当前比较的类
				double g = gain(i, j, k, order); // 计算gain(i,j,k)
				if (g > 0) {
					if (Math.random() / 100 < 1 - Math.exp(-g / T)) {
						if (T > finalT) {
							SortOrder(i, k, sortOrder); // 将i j序列移至k之后
							T = a * T; // 降温
							break; // 跳出，本次循环结束，直接和下一个k值比较
						}
						if (T < finalT) {
							break loop;
						}
					}
				}
			}
		}

		List<ClassInfo> riaOrder = new LinkedList<ClassInfo>();
		for (int i = 0; i <= sortOrder.length - 1; i++) {
			riaOrder.add(order.get(sortOrder[i]));
		}
		return riaOrder;
	}

	/*
	 * 将ij序列移至k之后
	 */
	private void SortOrder(int i, int k, int[] order) {
		int temp = 0;
		for (int j = k - 1; j >= i; j--) {
			temp = order[k];
			order[k] = order[j];
			order[j] = temp;
			k--;
		}
	}

	/*
	 * 计算gain(i,j,k)
	 */
	private double gain(int i, int j, int k, List<ClassInfo> order) { // k=j+1
		double g = 0.0;
		if (i == 0 && j == 0 && k == 1) {
			g = SCplx(0, 1, order) - SCplx(1, 0, order);
		} else {
			double sumik = 0.0;
			for (int s = 0; s <= i; s++) {
				sumik = sumik + SCplx(s, k, order) - SCplx(k, s, order);
			}
			g = gain(0, j - 1, k - 1, order) + sumik;
		}
		return g;
	}

	/*
	 * 计算测试序列中第i个类依赖与第k个类之间的SCplx
	 */
	private double SCplx(int i, int k, List<ClassInfo> order) {
		ClassInfo sci_i = order.get(i);
		ClassInfo sci_k = order.get(k);
		double scplx = SCplx.calculateSCplx(order,sci_i, sci_k);
		return scplx;
	}

	/*
	 * 获取适应度函数的方法
	 */
	private BigDecimal calculateFitness(List<ClassInfo> order) {		
		return BigDecimal.valueOf(SCplx.calculateFitness(order));
	}

//	private void print(List<ClassInfo> order) {
//		for (ClassInfo sci : order) {
//			System.out.println(sci.getSootCNo() + "-->" + sci.getSootCName());
//		}
//	}

	public void printWithNo(List<ClassInfo> order) {
		int i = 0;
		for (ClassInfo sci : order) {
			System.out.println(i + " " + sci.getSootCName());
			i++;
		}
	}
	
	public List<ClassInfo> getGlobalBestOrder(){
		return this.globalBestOrder;	
	}
}
