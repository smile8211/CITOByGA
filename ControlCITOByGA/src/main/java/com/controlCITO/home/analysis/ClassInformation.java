package com.controlCITO.home.analysis;

import java.util.ArrayList;
import java.util.List;

public class ClassInformation {
	String type ;
	String name;
	List<String> attributes;
	List<String> methods;
	
	public ClassInformation() {
		attributes=new ArrayList<String>();
		methods=new ArrayList<String>();
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
	public List<String> getMethods() {
		return methods;
	}
	public void setMethods(List<String> methods) {
		this.methods = methods;
	}
	
}
