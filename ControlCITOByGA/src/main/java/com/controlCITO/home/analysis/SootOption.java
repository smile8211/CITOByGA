package com.controlCITO.home.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.options.Options;

public class SootOption{

	public static Collection<SootClass> setOptions(String fileName){

			List<String> processdir= new ArrayList<String>();
			processdir.add(System.getProperty("user.dir")+File.separator+"input_analysis"+File.separator+fileName);
//			String JAVA_HOME = "C:\\Program Files\\Java\\jdk1.8.0_121\\jre";
//			String CLASS_PATH = ".;"+JAVA_HOME+"\\lib\\dt.jar;" +JAVA_HOME+"\\lib\\tools.jar;"+JAVA_HOME+"\\jre\\lib\\rt.jar;";
//			System.setProperty("java.class.path", CLASS_PATH);
			soot.G.reset();
			Options.v().set_app(true);
		
	        Options.v().set_whole_program(true);
	       
	        Options.v().set_keep_line_number(true);
	       
	        Options.v().set_allow_phantom_refs(true);
	        
	        Options.v().set_process_dir(processdir);
	      
	        Options.v().setPhaseOption("jb", "enabled:true");
	              
	        Options.v().setPhaseOption("jb", "use-original-names:true");
	               
	        Options.v().set_soot_classpath(System.getProperty("java.class.path"));
	        
	        Scene.v().loadNecessaryClasses();
	        
	        Collection<SootClass> coll=Scene.v().getApplicationClasses();// 获取所有的待测类
	        
	        return  coll;
	        
	}

}
