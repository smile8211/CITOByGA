package com.controlCITO.home.Cplx;

import java.math.BigDecimal;

public class FitAndStub {
	// 总体测试桩复杂度
	BigDecimal fit = new BigDecimal("0");
	// 测试桩数目
	int stubs = 0;
	public BigDecimal getFit() {
		return fit;
	}
	public void setFit(BigDecimal fit) {
		this.fit = fit;
	}
	public int getStubs() {
		return stubs;
	}
	public void setStubs(int stubs) {
		this.stubs = stubs;
	}
	
}
