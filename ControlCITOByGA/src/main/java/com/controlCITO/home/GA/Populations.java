package com.controlCITO.home.GA;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.controlCITO.home.Cplx.SCplx;
import com.controlCITO.home.manager.ClassInfo;

public class Populations {

	private List<Chromosome> populations = new ArrayList<Chromosome>();; // 种群表示
	private int POPNUM; // 种群大小
	private double pc = 0.5; // 交叉概率
	private double pm = 0.015; // 变异概率
	private boolean elitism = true; // 是否保留上一代的最优个体
	private List<Double> fit = new ArrayList<>();// 整个种群中对应个体适应值

	public Populations(int populationSize) {
		this.POPNUM = populationSize;
	}

	public Populations(int populationSize, List<ClassInfo> listOfGA, boolean initialise) {
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
		this.populations = new ArrayList<Chromosome>();
		for (int i = 0; i < POPNUM; i++) {
			Chromosome chromos = new Chromosome(listOfGA, false);
			populations.add(chromos);
		}
		initOcplxAndStubAndWoAndFit();
	}

	/**
	 * 每代种群进化
	 */
	public Populations evolvePops(Populations oldPopulations) {
		Populations newPopulations = new Populations(oldPopulations.getPOPNUM());
		// 根据适应值排序
		List<Chromosome> sortpopulations = sortPops(oldPopulations);
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
			Chromosome parent1 = sortpopulations.get(a);
			Chromosome parent2 = sortpopulations.get(b);
			// 返回两个父个体交叉变异过的两个子个体
			List<Chromosome> children = crossover(parent1, parent2);
			// 将交叉变异的添加到新种群
			newPopulations.getPopulations().addAll(children);
		}
		// 确定种群中变异的个体数
		int mutatenum = (int) (POPNUM * pm);
		// 选择用于变异的个体并执行变异操作
		for (int j = 0; j < mutatenum; j++) {
			int a = random.nextInt(POPNUM);
			Chromosome parent = sortpopulations.get(a);
			Chromosome child = mutate(parent);
			newPopulations.getPopulations().add(child);
		}

		return FullOrEmpty(oldPopulations, newPopulations);
	}

	/**
	 * 交叉操作 交叉概率为pc
	 */
	public List<Chromosome> crossover(Chromosome parent1, Chromosome parent2) {

		// children返回生成的两个子代染色体
		List<Chromosome> children = new ArrayList<Chromosome>();
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
		Chromosome child1 = new Chromosome(child1_list, true);

		for (int i = 0; i < parent2.getSizeOfChromos(); i++) {
			if (!(parent2.getGeneName(i).equals(parent1.getGeneName(startpos)))
					&& !(parent2.getGeneName(i).equals(parent1.getGeneName(endpos)))) {
				child2_list.add(parent2.getGene(i));
			}
		}
		child2_list.add(startpos, gene_start_p1);
		child2_list.add(endpos, gene_end_p1);
		Chromosome child2 = new Chromosome(child2_list, true);

		children.add(child1);
		children.add(child2);

		return children;
	}

	/**
	 * 变异操作 变异概率为pm
	 */
	public Chromosome mutate(Chromosome parent) {

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
		Chromosome child = new Chromosome(child_list, true);

		return child;
	}

	/**
	 * 判断种群满还是空 若满，删除部分个体； 若空，增加父代优良个体
	 */
	public Populations FullOrEmpty(Populations oldPopulations, Populations newPopulations) {
		//删除出重复
		List<Chromosome> list=newPopulations.getPopulations();
		for (int i = 0; i < list.size() - 1; i++) {
			for (int j = list.size() - 1; j > i; j--) {
				if (list.get(j).equals(list.get(i))) {
					list.remove(j);
				}
			}
		}
		newPopulations.setPopulations(list);
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
				Chromosome c;
				do{
					ranId = (int) (Math.random() * numofold);
					c=sortPops(oldPopulations).get(ranId);
				}while(newPopulations.getPopulations().contains(c)); 
				newPopulations.getPopulations().add(c);
			}
		}
		newPopulations.initOcplxAndStubAndWoAndFit();
		return newPopulations;

	}

	/**
	 * 根据个体适应度值，对种群个体进行排序，由适应度从小到大 对种群（染色体集合）中个体（染色体）进行排序
	 */
	public List<Chromosome> sortPops(Populations population) {
		List<Chromosome> sortpopulations = population.getPopulations();
		// 根据染色体通过适应度函数得到的适应值排序
		Collections.sort(sortpopulations);
		return sortpopulations;
	}

	/**
	 * 寻找最优个体，并将其记录下来 最优个体是根据适应值排序过后Chromosome集合里面第一个
	 */
	public Chromosome getBest(Populations population) {
		Chromosome bestchromos = null;
		List<Chromosome> sortpopulations = sortPops(population);
		bestchromos = sortpopulations.get(0);
		System.out.println(sortpopulations.get(0).getFit());
		return bestchromos;
	}

	/**
	 * 寻找最差个体，并将其记录下来
	 */
	public Chromosome getWorst(Populations population) {
		Chromosome worstchromos = null;
		List<Chromosome> sortpopulations = sortPops(population);
		worstchromos = sortpopulations.get(getPOPNUM() - 1);
		return worstchromos;
	}

	public List<Chromosome> getPopulations() {
		return populations;
	}

	public void setPopulations(List<Chromosome> populations) {
		this.populations = populations;
	}

	public void initOcplxAndStubAndWoAndFit() {
		for (Chromosome chromosome : this.getPopulations()) {
			Double fit = calculateFitness(chromosome.getListOfSCI());
			this.fit.add(fit);
			chromosome.setFit(fit);
		}
	}

	// 计算序列的测试桩复杂度
	// 适应度函数计算一个个体适应值
	public Double calculateFitness(List<ClassInfo> order) {
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

	public int getPOPNUM() {
		return POPNUM;
	}

}
