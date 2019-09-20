package com.controlCITO.home.GA;

import java.util.List;

import com.controlCITO.home.manager.ClassInfo;

import com.controlCITO.home.GA.Chromosome;
import com.controlCITO.home.GA.Populations;

public class GeneticAlgorithms {//遗传算法GA

	private int Pops;							//种群大小
	private List<ClassInfo> listOfGA;			//种群信息
	private int Generations;   				    //运行代数
	private  Chromosome global_bestchromos;	

	public GeneticAlgorithms(List<ClassInfo> listOfGA){
		this.Pops = listOfGA.size();	//listOfGA.size();
		this.listOfGA = listOfGA; 
		this.Generations = 100;
	}
	
	public void generateClassListBasedGA(){	//MyGA生成序列
		Populations initialPops = new Populations(this.Pops, this.listOfGA, true);
		int generations = 0;
		Populations Pops_evolver = initialPops;
				
		while(generations < Generations){
			System.out.println("第"+generations+"代");
			Populations newPops = Pops_evolver.evolvePops(Pops_evolver);
			Pops_evolver = newPops;
			generations++;
		}
		//最后一代中的染色体通过适应度函数计算的适应值排序，取其中最优染色体（即最优序列）
		global_bestchromos = Pops_evolver.getBest(Pops_evolver);
		//打印染色体中基因序列
		System.out.println("---- GA CITO ------");
		global_bestchromos.print();
		
	}

	public List<ClassInfo> getGlobalBestOrder(){
		return global_bestchromos.getListOfSCI();
	}
}
