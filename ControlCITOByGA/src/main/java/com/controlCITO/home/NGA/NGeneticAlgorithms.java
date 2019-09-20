package com.controlCITO.home.NGA;

import java.util.List;

import com.controlCITO.home.manager.ClassInfo;

public class NGeneticAlgorithms {//遗传算法GA

	private int Pops;							//种群大小
	private List<ClassInfo> listOfGA;			//种群信息
	private int Generations;   				    //运行代数
	private  NChromosome global_bestchromos;	

	public NGeneticAlgorithms(List<ClassInfo> listOfGA){
		this.Pops = listOfGA.size();	//listOfGA.size();
		this.listOfGA = listOfGA; 
		this.Generations = 100;
	}
	
	public void generateClassListBasedGA(){	//MyGA生成序列
		NPopulations initialPops = new NPopulations(this.Pops, this.listOfGA, true);
		int generations = 0;
		NPopulations Pops_evolver = initialPops;
				
		while(generations < Generations){
			System.out.println("第"+generations+"代");
			NPopulations newPops = Pops_evolver.evolvePops(Pops_evolver);
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
