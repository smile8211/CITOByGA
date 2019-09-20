package com.controlCITO.home.GA;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.controlCITO.home.manager.ClassInfo;

public class Chromosome implements Comparable<Chromosome>, Cloneable {

	List<ClassInfo> chromos; // 染色体表示
	int sizeOfChromos; // 染色体大小，即类的总数
	Double fit;
	
	public Chromosome(List<ClassInfo> listOfGA, boolean isChromos) {

		this.sizeOfChromos = listOfGA.size();
		if (isChromos == false) {
			this.chromos = initialChromos(listOfGA);
		} else {
			this.chromos = listOfGA;
		}
	}

	/*
	 * 初始化染色体
	 * 将打乱顺序的ClassInfo集合返回（一条染色体）
	 */
	public List<ClassInfo> initialChromos(List<ClassInfo> listOfGA) {

		List<ClassInfo> sci_initial = new LinkedList<ClassInfo>();

		for (ClassInfo sci : listOfGA) {
			sci_initial.add(sci);
		}

		int n = 0;
		List<String> scs_chromos = new ArrayList<String>();
		List<ClassInfo> chromos = new LinkedList<ClassInfo>();
		Random random = new Random();
		do {
			n = random.nextInt(sci_initial.size());
			scs_chromos.add(sci_initial.get(n).getSootCName());
			chromos.add(sci_initial.get(n));
			sci_initial.remove(n);
		} while (sci_initial.size() != 0);

		return chromos;
	}

	/*
	 * 获取该染色体的List<SourceClassInfo>表现形式
	 */
	public List<ClassInfo> getListOfSCI() {

		return this.chromos;
	}

	public void setGene(int index, ClassInfo gene) {

		this.chromos.set(index, gene);
	}

	public ClassInfo getGene(int index) {

		return this.getListOfSCI().get(index);
	}

	public String getGeneName(int index) {
		return this.getListOfSCI().get(index).getSootCName();
	}

	public int getMaxSizeAttrDeps() {

		int max_size_attrs = 0;
		for (ClassInfo sci : this.getListOfSCI()) {
			if (sci.searchMaxAttrDep() > max_size_attrs) {
				max_size_attrs = sci.searchMaxAttrDep();
			}
		}
		return max_size_attrs;
	}

	public int getMaxSizeMethodDeps() {

		int max_size_method = 0;
		for (ClassInfo sci : this.getListOfSCI()) {
			if (sci.searchMaxMethodDep() > max_size_method) {
				max_size_method = sci.searchMaxMethodDep();
			}
		}
		return max_size_method;
	}
	
	/*
	 * 获取染色体大小，即类的数目
	 */
	public int getSizeOfChromos() {

		return sizeOfChromos;
	}

	public void print() {

		for (ClassInfo sci : this.getListOfSCI()) {
			System.out.println(sci.getSootCNo() + " " + sci.getSootCName());
		}
	}

	public Object clone() {
		Chromosome o = null;
		try {
			o = (Chromosome) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}

	@Override
	public int compareTo(Chromosome o) {
		//升序排列
		return this.fit > o.getFit() ? 1 : this.fit == o.getFit() ? 0 : -1;
	}

	public Double getFit() {
		return fit;
	}

	public void setFit(Double fit) {
		this.fit = fit;
	}
	
}
