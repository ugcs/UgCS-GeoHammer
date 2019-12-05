package com.github.thecoldwine.sigrun.common.ext;

public enum AreaType {

	Hyperbola("hyperbola"), 
	Air("air"),
	Surface("surface"),
    Ground("ground");

	private String name;
	
	AreaType(String name){
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
