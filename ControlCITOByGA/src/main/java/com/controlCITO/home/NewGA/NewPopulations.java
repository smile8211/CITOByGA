package com.controlCITO.home.NewGA;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.controlCITO.home.Cplx.NewSCplx;
import com.controlCITO.home.manager.ClassInfo;

public class NewPopulations {

	private List<NewChromosome> populations = new ArrayList<NewChromosome>(); // 种群表示
	private int POPNUM; // 种群大小
	private double pc = 0.5; // 交叉概率
	private double pm = 0.015; // 变异概率
	private boolean elitism = true; // 是否保留上一代的最优个体
	private List<Double> ocplxList = new ArrayList<>(); // 计算整个种群中每个个体复杂度和数目
	private List<Integer> ncplxList = new ArrayList<>();
	private Double wo = 0.0;// 熵权法获得总复杂度权值
	private List<Double> fit = new ArrayList<>();// 整个种群中对应个体适应值

	public NewPopulations(int populationSize) {
		this.POPNUM = populationSize;
	}

	public NewPopulations(int populationSize, List<ClassInfo> listOfGA, boolean initialise) {
		this.POPNUM = populationSize;
		if (initialise) {
			initialPops(populationSize, listOfGA);
		}
	}

	/**
	 * 初始化一组染色体 一代种群 一个种群Populations中有个染色体Chromosome集合
	 * 集合中每个不同染色体Chromosome中有一个不同染色体序列（类序列）
	 */
	public void initialPops(int POPNUM, List<ClassInfo> listOfGA) {
		for (int i = 0; i < POPNUM; i++) {
			NewChromosome chromos = new NewChromosome(listOfGA, false, this);
			populations.add(chromos);
		}
		initOcplxAndStubAndWoAndFit();
	}

	/**
	 * 每代种群进化
	 */
	public NewPopulations evolvePops(NewPopulations oldPopulations) {

		NewPopulations newPopulations = new NewPopulations(oldPopulations.getPOPNUM());
		// 根据适应值排序
		List<NewChromosome> sortpopulations = sortPops(oldPopulations);
		// 选择操作保留上一代的优良个体
		if (elitism) {
			newPopulations.getPopulations().add(sortpopulations.get(0));
			System.out.println(sortpopulations.get(0).getFit());
		}

		// 确定种群中交叉的个体数
		int crossnum = (int) (POPNUM * pc);
		if (crossnum % 2 != 0) {
			crossnum++;
		}

		// 选择用于交叉的个体并执行交叉操作
		Random random = new Random();
		for (int i = 0; i < crossnum / 2; i++) {
			int a = 0, b = 0;
			do {
				a = random.nextInt(POPNUM);
				b = random.nextInt(POPNUM);
			} while (a == b);
			NewChromosome parent1 = sortpopulations.get(a);
			NewChromosome parent2 = sortpopulations.get(b);
			// 返回两个父个体交叉变异过的两个子个体
			List<NewChromosome> children = crossover(parent1, parent2);
			// 将交叉变异的添加到新种群
			newPopulations.getPopulations().addAll(children);
		}

		// 确定种群中变异的个体数
		int mutatenum = (int) (POPNUM * pm);
		// 选择用于变异的个体并执行变异操作
		for (int j = 0; j < mutatenum; j++) {
			int a = random.nextInt(POPNUM);
			NewChromosome parent = sortpopulations.get(a);
			NewChromosome child = mutate(parent);
			newPopulations.getPopulations().add(child);
		}
		return FullOrEmpty(oldPopulations, newPopulations);
	}

	/**
	 * 交叉操作 交叉概率为pc
	 */
	public List<NewChromosome> crossover(NewChromosome parent1, NewChromosome parent2) {

		// children返回生成的两个子代染色体
		List<NewChromosome> children = new ArrayList<NewChromosome>();

		// 随机生成交叉点
		Random random = new Random();
		int startpos, endpos = 0;
		startpos = random.nextInt(parent1.getSizeOfChromos());

		do {
			endpos = random.nextInt(parent2.getSizeOfChromos());
		} while (endpos == startpos);
		if (startpos > endpos) {
			int temp = startpos;
			startpos = endpos;
			endpos = temp;
		}

		// 获取交叉点基因
		ClassInfo gene_start_p1 = parent1.getGene(startpos);
		ClassInfo gene_end_p1 = parent1.getGene(endpos);
		ClassInfo gene_start_p2 = parent2.getGene(startpos);
		ClassInfo gene_end_p2 = parent2.getGene(endpos);

		// 生成新的子代个体
		List<ClassInfo> child1_list = new ArrayList<ClassInfo>();
		List<ClassInfo> child2_list = new ArrayList<ClassInfo>();

		for (int i = 0; i < parent1.getSizeOfChromos(); i++) {
			if (!(parent1.getGeneName(i).equals(parent2.getGeneName(startpos)))
					&& !(parent1.getGeneName(i).equals(parent2.getGeneName(endpos)))) {
				// 将不变异基因添加到新子代
				child1_list.add(parent1.getGene(i));
			}
		}
		// 将变异基因添加到指定位置
		child1_list.add(startpos, gene_start_p2);
		child1_list.add(endpos, gene_end_p2);
		// 新染色体（个体）
		NewChromosome child1 = new NewChromosome(child1_list, true, this);

		for (int i = 0; i < parent2.getSizeOfChromos(); i++) {
			if (!(parent2.getGeneName(i).equals(parent1.getGeneName(startpos)))
					&& !(parent2.getGeneName(i).equals(parent1.getGeneName(endpos)))) {
				child2_list.add(parent2.getGene(i));
			}
		}
		child2_list.add(startpos, gene_start_p1);
		child2_list.add(endpos, gene_end_p1);
		NewChromosome child2 = new NewChromosome(child2_list, true, this);

		children.add(child1);
		children.add(child2);

		return children;
	}

	/**
	 * 变异操作 变异概率为pm
	 */
	public NewChromosome mutate(NewChromosome parent) {

		// 随机生成交叉点
		Random random = new Random();
		int startpos, endpos = 0;
		startpos = random.nextInt(parent.getSizeOfChromos());
		do {
			endpos = random.nextInt(parent.getSizeOfChromos());
		} while (endpos == startpos);
		if (startpos > endpos) {
			int temp = startpos;
			startpos = endpos;
			endpos = temp;
		}

		// 获取变异点基因
		ClassInfo gene_start = parent.getGene(startpos);
		ClassInfo gene_end = parent.getGene(endpos);

		// 生成新的子代个体
		List<ClassInfo> child_list = new ArrayList<ClassInfo>();
		for (int i = 0; i < parent.getSizeOfChromos(); i++) {
			if (i != startpos && i != endpos) {
				child_list.add(parent.getGene(i));
			}
		}

		child_list.add(startpos, gene_end);
		child_list.add(endpos, gene_start);
		NewChromosome child = new NewChromosome(child_list, true, this);

		return child;
	}

	/**
	 * 判断种群满还是空 若满，删除部分个体； 若空，增加父代优良个体
	 */
	public NewPopulations FullOrEmpty(NewPopulations oldPopulations, NewPopulations newPopulations) {

		// 删除出重复
//		List<NewChromosome> list = newPopulations.getPopulations();
//		for (int i = 0; i < list.size() - 1; i++) {
//			for (int j = list.size() - 1; j > i; j--) {
//				if (list.get(j).equals(list.get(i))) {
//					list.remove(j);
//				}
//			}
//		}
//		newPopulations.setPopulations(list);
		int numofnew = newPopulations.getPopulations().size();
		int numofold = oldPopulations.getPOPNUM();
		if (numofnew > numofold) {
			for (int i = 0; i < (numofnew - numofold); i++) {
				newPopulations.getPopulations().remove(newPopulations.getWorst(newPopulations));
			}
		}
		if (numofnew < numofold) {
			for (int i = 1; i <= (numofold - numofnew); i++) {
				int ranId = 0;
				NewChromosome c;
				do {
					ranId = (int) (Math.random() * numofold);
					c = sortPops(oldPopulations).get(ranId);
				} while (newPopulations.getPopulations().contains(c));
				newPopulations.getPopulations().add(c);
			}
		}
		newPopulations.initOcplxAndStubAndWoAndFit();
		return newPopulations;

	}

	/**
	 * 根据个体适应度值，对种群个体进行排序，由适应度从小到大 对种群（染色体集合）中个体（染色体）进行排序
	 */
	public List<NewChromosome> sortPops(NewPopulations population) {
		List<NewChromosome> sortpopulations = population.getPopulations();
		// 根据染色体通过适应度函数得到的适应值排序
		Collections.sort(sortpopulations);
		return sortpopulations;
	}

	/**
	 * 寻找最优个体，并将其记录下来 最优个体是根据适应值排序过后Chromosome集合里面第一个
	 */
	public NewChromosome getBest(NewPopulations population) {
		NewChromosome bestchromos = null;
		List<NewChromosome> sortpopulations = sortPops(population);
		bestchromos = sortpopulations.get(0);
		System.out.println(sortpopulations.get(0).getFit());
		return bestchromos;
	}

	/**
	 * 寻找最差个体，并将其记录下来
	 */
	public NewChromosome getWorst(NewPopulations population) {
		List<Double> currentFit = population.getFit();
		Double bestfit = Collections.max(currentFit);
		List<NewChromosome> currentChromos = population.getPopulations();
		NewChromosome bestchromos = currentChromos.get(currentFit.indexOf(bestfit));
		return bestchromos;
	}

	public List<NewChromosome> getPopulations() {
		return populations;
	}

	public void setPopulations(List<NewChromosome> populations) {
		this.populations = populations;
	}

	public void initOcplxAndStubAndWoAndFit() {
		// 计算整个种群中每个个体复杂度和数目
		List<Double> ocplxList = new ArrayList<>();
		List<Integer> ncplxList = new ArrayList<>();
		for (NewChromosome chromosome : this.getPopulations()) {
			// 总体测试桩复杂度
			Double fit = 0.0;
			// 测试桩数目
			Integer stubs = 0;
			// 评估过的类名的集合
			Set<String> classNames = new HashSet<String>();
			for (ClassInfo sci : chromosome.getListOfSCI()) {
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
							ClassInfo scd = getCIByCName(chromosome.getListOfSCI(), depClass);
							// 计算总体测试桩复杂度
							fit = fit + NewSCplx.calculateSCplxByEntropy(chromosome.getListOfSCI(), sci, scd);
							// 计算测试桩数目
							stubs = stubs + 1;
						}
					}
				}
			}
			ocplxList.add(fit);
			ncplxList.add(stubs);
		}
		this.ocplxList = ocplxList;
		this.ncplxList = ncplxList;
		this.setWo(getWocplxByEntropy());
		for (NewChromosome chromosome : this.getPopulations()) {
			Double fit = calculateFitnessByEntropy(chromosome.getListOfSCI());
			this.fit.add(fit);
			chromosome.setFit(fit);
		}
	}

	private ClassInfo getCIByCName(List<ClassInfo> listOfSrc, String depClass) {
		ClassInfo cinfo = null;
		for (ClassInfo ci : listOfSrc) {
			if (ci.getSootCName().equals(depClass)) {
				cinfo = ci;
				break;
			}
		}
		return cinfo;
	}

	// 返回总复杂度权值List<BigDecimal> ocplxList, List<BigDecimal> ncplxList
	private Double getWocplxByEntropy() {
		int m = ocplxList.size();
		// 使用上面的矩阵计算复杂度和数目各自总和
		Double sumOcplx = 0.0;
		Integer sumNcplx = 0;
		for (Double ocplx : ocplxList) {
			sumOcplx = sumOcplx + ocplx;
		}
		for (Integer ncplx : ncplxList) {
			sumNcplx = sumNcplx + ncplx;
		}
		// 计算po和pn复杂度和数目矩阵集合
		List<BigDecimal> po = new ArrayList<>();
		List<BigDecimal> pn = new ArrayList<>();
		for (int i = 0; i < ocplxList.size(); i++) {
			po.add(BigDecimal.valueOf(ocplxList.get(i)).divide(BigDecimal.valueOf(sumOcplx), 20,
					BigDecimal.ROUND_HALF_UP));
		}
		for (int i = 0; i < ncplxList.size(); i++) {
			pn.add(BigDecimal.valueOf(ncplxList.get(i)).divide(BigDecimal.valueOf(sumNcplx), 20,
					BigDecimal.ROUND_HALF_UP));
		}
		// 计算信息熵eo和en
		BigDecimal sumPoMultiplyLogPo = BigDecimal.valueOf(0);
		BigDecimal sumPnMultiplyLogPn = BigDecimal.valueOf(0);
		BigDecimal eo;
		BigDecimal en;
		for (int i = 0; i < po.size(); i++) {
			sumPoMultiplyLogPo = sumPoMultiplyLogPo
					.add(po.get(i).multiply(BigDecimal.valueOf(Math.log(po.get(i).doubleValue()))));
		}
		for (int i = 0; i < pn.size(); i++) {
			sumPnMultiplyLogPn = sumPnMultiplyLogPn
					.add(pn.get(i).multiply(BigDecimal.valueOf(Math.log(pn.get(i).doubleValue()))));
		}
		eo = (BigDecimal.valueOf(-1).divide(BigDecimal.valueOf(Math.log(m)), 20, BigDecimal.ROUND_HALF_UP))
				.multiply(sumPoMultiplyLogPo);
		en = (BigDecimal.valueOf(-1).divide(BigDecimal.valueOf(Math.log(m)), 20, BigDecimal.ROUND_HALF_UP))
				.multiply(sumPnMultiplyLogPn);
		// 计算权重wo(为什么是负的？)
		BigDecimal wo = (BigDecimal.valueOf(1).subtract(eo)).divide(BigDecimal.valueOf(2).subtract(eo.add(en)), 10,
				BigDecimal.ROUND_HALF_UP);
		return wo.doubleValue();
	}

	public Double getWo() {
		return wo;
	}

	public void setWo(Double wo) {
		this.wo = wo;
	}

	public List<Double> getOcplxList() {
		return ocplxList;
	}

	public List<Integer> getNcplxList() {
		return ncplxList;
	}

	public List<Double> getFit() {
		return fit;
	}

	public void setFit(List<Double> fit) {
		this.fit = fit;
	}

	// 计算新适应度函数计算适应度(**************熵权法**********************)
	public Double calculateFitnessByEntropy(List<ClassInfo> order) {
		// 计算当前序列复杂度
		BigDecimal currentOcplx = new BigDecimal("0");
		BigDecimal currentStubs = new BigDecimal("0");
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
						currentOcplx = currentOcplx
								.add(BigDecimal.valueOf(NewSCplx.calculateSCplxByEntropy(order, sci, scd)));
						// 计算测试桩数目
						currentStubs = currentStubs.add(new BigDecimal("1"));
					}
				}
			}
		}
		// 计算复杂度返回
		BigDecimal avgOcplx = currentOcplx.divide(newsub(Collections.max(ocplxList), Collections.min(ocplxList)), 20,
				BigDecimal.ROUND_HALF_UP);
		BigDecimal avgNstub = currentStubs.divide(newsub(Collections.max(ncplxList), Collections.min(ncplxList)), 20,
				BigDecimal.ROUND_HALF_UP);
		return Math.sqrt(((avgOcplx.multiply(avgOcplx).multiply(BigDecimal.valueOf(wo.doubleValue()))).add(avgNstub
				.multiply(avgNstub).multiply(new BigDecimal("1").subtract(BigDecimal.valueOf(wo.doubleValue())))))
						.doubleValue());
	}

	public BigDecimal newsub(Double a, Double b) {
		Double a_b = a - 0;
		if(a_b==0) {
			return new BigDecimal("0.001");
		}
		return new BigDecimal(a_b);
	}

	public BigDecimal newsub(int a, int b) {
		int a_b = a - 0;
		if(a_b==0) {
			return new BigDecimal("0.001");
		}
		return new BigDecimal(a_b);
	}

	public int getPOPNUM() {
		return POPNUM;
	}
}
