package com.controlCITO.home.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.internal.JDynamicInvokeExpr;
import soot.jimple.internal.JInterfaceInvokeExpr;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.util.Chain;

public class Analyzer {
	static private Collection<SootClass> listOfClasses; // 待测类列表
	private String[] classNames; // 类名列表
	private List<SootMethod> listOfSootMethods; // 方法列表
	private Set<SootMethod> SetOfSootMethods;
	private Map<String, String> listOfInherited; // 继承关系列表

	private Map<String, Map<String, Integer>> listOfM; // 方法依赖列表
	private Map<String, Map<String, Integer>> listOfA; // 属性依赖列表

	private List<PairOfMethod> listOfMethodInvokes; // 方法调用列表
	private List<PairOfMethod> listOfCMethodInvokes; // 同一个类中的方法调用
	// 生成UML图需要的
	private List<ClassInformation> listClassInformation; // 方法调用列表
	private List<ClassLinks> listClassLinks; // 同一个类中的方法调用

	public Analyzer() {
		this.listOfSootMethods = new ArrayList<SootMethod>();
		this.listOfInherited = new HashMap<String, String>();
		this.listOfM = new HashMap<String, Map<String, Integer>>();
		this.listOfA = new HashMap<String, Map<String, Integer>>();
		this.listOfMethodInvokes = new ArrayList<PairOfMethod>();
		this.listOfCMethodInvokes = new ArrayList<PairOfMethod>();
		this.SetOfSootMethods = new HashSet<SootMethod>();
		this.listClassInformation = new ArrayList<ClassInformation>();
		this.listClassLinks = new ArrayList<ClassLinks>();
	}

	public void analysis(String fileName) throws Exception {
		listOfClasses = loadClassAndAnalysis(fileName);
		removeSystemClasses();
		setClassNo();
		setInherited();
		for (SootClass sClass : listOfClasses) {
			analysisMethod(sClass);
			analysisAttr(sClass);
		}
		// 初始化类信息listClassInformation和类间关系listClassLinks
		initlistClassInformation();
		initlistClassLinks();
	}

	public void initlistClassInformation() {
		for (SootClass sClass : listOfClasses) {
			if (!sClass.hasOuterClass()) {
				ClassInformation classInformation = new ClassInformation();
				classInformation.setName(sClass.getName());
				if (sClass.isAbstract()) {
					classInformation.setType("Abstract");
				} else if (sClass.isInterface()) {
					classInformation.setType("Interface");
				} else {
					classInformation.setType("class");
				}
				classInformation.setAttributes(analysisCurrentAttr(sClass));
				classInformation.setMethods(analysisCurrentMethod(sClass));
				listClassInformation.add(classInformation);
			}
		}
	}

	// 获取当前类中所有属性（根据UML的格式返回）
	private List<String> analysisCurrentAttr(SootClass sClass) {
		List<String> attrUML = new ArrayList<>();
		Chain<SootField> fields = sClass.getFields();
		for (SootField field : fields) {
			String modifiers = null;
			if (field.getModifiers() <= 1) {
				modifiers = "+";
			}
			if (field.getModifiers() > 1) {
				modifiers = "-";
			}
			String type = field.getType().toString();
			attrUML.add(modifiers + field.getName() + ":" + type.substring(type.lastIndexOf(".") + 1));
		}
		return attrUML;
	}

	// 获取当前类中所有方法（根据UML的格式返回）
	private List<String> analysisCurrentMethod(SootClass sClass) {
		List<String> methodUML = new ArrayList<>();
		List<SootMethod> methods = sClass.getMethods();
		for (SootMethod method : methods) {
			String modifiers = null;
			if (method.getModifiers() <= 1) {
				modifiers = "+";
			}
			if (method.getModifiers() > 1) {
				modifiers = "-";
			}
			String name = method.getName().toString();
			if (!("<init>".equals(name) || "toString".equals(name))) {
				if (name.length() > 4) {
					if (!("get".equals(name.substring(0, 3)) || "set".equals(name.substring(0, 3)))) {
						methodUML.add(modifiers + name + getTypesAndtype(method.getParameterTypes(),method.getReturnType()));
					}
				} else {
					methodUML.add(
							modifiers + name + getTypesAndtype(method.getParameterTypes(),method.getReturnType()));
				}
			}
		}
		return methodUML;
	}
	public String getTypesAndtype(List<Type> types,Type type) {
		String str="(";
		for(Type t:types) {
			str=str+t.toString().substring(t.toString().lastIndexOf(".")+1)+",";
		}
		if(str.equals("(")) {
			str=str+")";
		}else {
			str=str.substring(0,str.lastIndexOf(","))+")";
		}
		if(type.toString().equals("void")) {
			str=str+":"+"void";
		}else {
			str=str+":"+type.toString().substring(type.toString().lastIndexOf(".")+1);
		}
	    return str;
	  }
	public void initlistClassLinks() {
		for (SootClass sClass : listOfClasses) {
			// 泛化（继承）关系Generalization（实线+白三角箭头）
			SootClass superClass = sClass.getSuperclass();
			if (superClass != null && superClass.isApplicationClass()) {
				ClassLinks classLinks = new ClassLinks();
				classLinks.setSource(sClass.getName());
				classLinks.setTarget(superClass.getName());
				classLinks.setRelationship("Generalization");
				listClassLinks.add(classLinks);
			}
			// 实现关系Implementation（虚线+白三角箭头）
			Chain<SootClass> interfaces = sClass.getInterfaces();
			if (!interfaces.isEmpty()) {
				for (SootClass interfClass : interfaces) {
					for (String str : classNames) {
						if (str.equals(interfClass.getName())) {
							ClassLinks classLinks = new ClassLinks();
							classLinks.setSource(sClass.getName());
							classLinks.setTarget(interfClass.getName());
							classLinks.setRelationship("Implementation");
							listClassLinks.add(classLinks);
						}
					}
				}
			}
		}
		// 聚合关系Aggregation（实线+白菱形）
		// 组合关系Composition（实线+黑菱形）（这里两种关联关系随机：类之间既有属性又有方法依赖）
		// 使用关系Dependency（实线+箭头）（类只有属性或方法依赖一种）
		for (String sourseM : listOfM.keySet()) {
			for (String targetM : listOfM.get(sourseM).keySet()) {
				ClassLinks classLinks = new ClassLinks();
				classLinks.setSource(sourseM);
				classLinks.setTarget(targetM);
				classLinks.setRelationship("Transition");
				listClassLinks.add(classLinks);
			}
		}
		for (String sourseA : listOfA.keySet()) {
			for (String targetA : listOfA.get(sourseA).keySet()) {
				ClassLinks classLinks = new ClassLinks();
				classLinks.setSource(sourseA);
				classLinks.setTarget(targetA);
				classLinks.setRelationship("Transition");
				listClassLinks.add(classLinks);
			}
		}
		for (int i = 0; i < listClassLinks.size(); i++) {
			for (int j = listClassLinks.size() - 1; j > i; j--) {
				if (listClassLinks.get(i).getSource().equals(listClassLinks.get(j).getSource())
						&& listClassLinks.get(i).getTarget().equals(listClassLinks.get(j).getTarget())) {
					listClassLinks.remove(j);
					if ((int) (1 + Math.random() * (10 - 1 + 1)) > 5) {
						listClassLinks.get(i).setRelationship("Aggregation");
					} else {
						listClassLinks.get(i).setRelationship("Composition");
					}
				}
			}
		}
	}

	// 删除系统类
	private void removeSystemClasses() {
		List<SootClass> removelist = new ArrayList<SootClass>();
		for (SootClass sootClass : listOfClasses) {
			if (ignore(sootClass)) {
				removelist.add(sootClass);
			}
		}
		for (SootClass removeClass : removelist) {
			if (listOfClasses.contains(removeClass)) {
				listOfClasses.remove(removeClass);
			}
		}
	}

	// 获取继承关系
	private void setInherited() {
		for (SootClass childClass : listOfClasses) {
			SootClass superClass = childClass.getSuperclass();
			if (superClass.isApplicationClass()) {
				this.listOfInherited.put(childClass.getName(), superClass.getName());
			}
		}
	}

	public List<ClassInformation> getListClassInformation() {
		return listClassInformation;
	}

	public void setListClassInformation(List<ClassInformation> listClassInformation) {
		this.listClassInformation = listClassInformation;
	}

	public List<ClassLinks> getListClassLinks() {
		return listClassLinks;
	}

	public void setListClassLinks(List<ClassLinks> listClassLinks) {
		this.listClassLinks = listClassLinks;
	}

	// 返回继承关系
	public Map<String, String> getInherited() {
		return this.listOfInherited;
	}

	public List<SootMethod> getSootMethod() {
		return this.listOfSootMethods;
	}

	public Map<String, Map<String, Integer>> getMethodDeps() {
		return this.listOfM;
	}

	public Map<String, Map<String, Integer>> getAttrDeps() {
		return this.listOfA;
	}

	public List<PairOfMethod> getMIbetweenClass() {
		return this.listOfMethodInvokes;
	}

	public List<PairOfMethod> getMIinClass() {
		return this.listOfCMethodInvokes;
	}

	private Collection<SootClass> loadClassAndAnalysis(String fileName) throws Exception {
		// 选择待加载的文件
		// @SuppressWarnings("resource")
		// Scanner input = new Scanner(System.in);
		// System.out.println("选择待加载的文件：");
		// String fileName = input.next();
		System.out.println("选择的文件是" + fileName);
		// 设置Soot
		// String fileName = "ant";
		return SootOption.setOptions(fileName);
		// try {
		// Scene.v().loadNecessaryClasses();
		// } catch (Exception e) {
		// e.printStackTrace();
		// throw new Exception("class can not be loaded");
		// }

	}

	// 分析属性依赖
	private void analysisAttr(SootClass sClass) {
		analysisField(sClass);
		analysisRet(sClass);

	}

	private void analysisRet(SootClass sClass) {
		for (SootMethod m : sClass.getMethods()) {
			Type retType = m.getReturnType();
			String str = retType.toString();
			String sourceCName = sClass.getName();
			if (!str.equals(sourceCName)) {
				for (SootClass sc : listOfClasses) {
					String targetCName = sc.getName();
					if (str.equals(targetCName) && !isSuper(sClass, sc)) {
						addattrDeps(sourceCName, targetCName);
					}
				}
			}
		}
	}

	private void analysisField(SootClass sClass) {
		for (SootField sfield : sClass.getFields()) {
			if (sfield != null && sfield.getType() instanceof RefType) {
				RefType type = (RefType) sfield.getType();
				SootClass targetClass = type.getSootClass();
				String sourceCName = sClass.getName();
				String targetCName = targetClass.getName();
				if (isApplicationClass(targetCName) && !sourceCName.equals(targetCName)
						&& !isSuper(sClass, targetClass)) {
					addattrDeps(sourceCName, targetCName);
				}
			}
		}
	}

	// 分析方法依赖
	private void analysisMethod(SootClass sClass) {
		Collection<SootMethod> listOfM = sClass.getMethods();
		for (SootMethod m : listOfM) {

			this.SetOfSootMethods.add(m);// 所有方法
			if (!m.isConcrete()) {// 该方法不是抽象方法
				// Log.logInfo(m.getDeclaration() + "is not Concrete");
				// Log.logInfo("this method belongs to " + sClass.getName());
				continue;
			}
			this.listOfSootMethods.add(m);// 去除抽象方法
			analysismethod(sClass, m);
			analysisParainMethod(sClass, m);
		}
	}

	// 属于属性依赖分析的一种
	private void analysisParainMethod(SootClass sClass, SootMethod sMethod) {
		List<?> types = sMethod.getParameterTypes();
		for (int i = 0; i < types.size(); i++) {
			Object paratype = types.get(i);
			String str = paratype.toString();
			String sourceCName = sClass.getName();
			if (!str.equals(sourceCName)) {
				for (SootClass sc : listOfClasses) {
					String targetCName = sc.getName();
					if (str.equals(targetCName) && !isSuper(sClass, sc)) {
						addattrDeps(sourceCName, targetCName);
					}
					for (SootField sf : sc.getFields()) {
						if (paratype.toString().equals(sf.getName()))
							addattrDeps(sourceCName, targetCName);
					}
				}
			}
		}
	}

	// 添加属性依赖
	private void addattrDeps(String sourceCName, String targetCName) {
		if (this.listOfA.containsKey(sourceCName)) {
			Map<String, Integer> map = this.listOfA.get(sourceCName);
			if (map.containsKey(targetCName)) {
				int i = map.get(targetCName) + 1;
				map.put(targetCName, i);
			} else {
				map.put(targetCName, 1);
			}
			this.listOfA.put(sourceCName, map);
		} else {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put(targetCName, 1);
			this.listOfA.put(sourceCName, map);
		}
	}

	// 分析类sClass中的方法m
	private void analysismethod(SootClass sClass, SootMethod sMethod) {
		Body body = sMethod.retrieveActiveBody();
		Collection<Unit> units = body.getUnits();// 获取方法中的句子
		for (Unit unit : units) {
			// Log.logInfo(unit.toString());
			int lineNo;
			for (ValueBox valueBox : unit.getUseBoxes()) {
				Value useValue = valueBox.getValue();
				switch (isInvokeExpr(useValue)) {
				case 1:
					lineNo = getLineNo(unit);
					analysisVirtualInvoke(useValue, sClass, sMethod, lineNo);
					break;
				case 2:
					lineNo = getLineNo(unit);
					analysisInterfaceInvoke(useValue, sClass, sMethod, lineNo);
					break;
				case 3:
					lineNo = getLineNo(unit);
					analysisDynamicInvoke(useValue, sClass, sMethod, lineNo);
					break;
				case 4:
					lineNo = getLineNo(unit);
					analysisSpecialInvoke(useValue, sClass, sMethod, lineNo);
					break;
				case 5:
					lineNo = getLineNo(unit);
					analysisStaticInvoke(useValue, sClass, sMethod, lineNo);
					break;
				default:
					break;
				}
			}
		}

	}

	// 获取该Unit的行号
	private int getLineNo(Unit unit) {
		int num = 0;
		List<Tag> tagList = unit.getTags();
		for (Tag tag : tagList) {
			if (tag instanceof LineNumberTag) {
				num = ((LineNumberTag) tag).getLineNumber();
				break;
			}
		}
		return num;
	}

	// 分析StaticInvoke
	private void analysisStaticInvoke(Value useValue, SootClass sClass, SootMethod sMethod, int lineNo) {

		JStaticInvokeExpr jStaticInvokeExpr = (JStaticInvokeExpr) useValue;
		String sourceCName = sClass.getName();
		SootClass targetClass = jStaticInvokeExpr.getMethod().getDeclaringClass();
		String targetCName = targetClass.getName();

		if (isApplicationClass(targetCName) && !isSuper(sClass, targetClass)) {
			String sourceMethod = sMethod.toString();
			String targetMethod = jStaticInvokeExpr.getMethod().toString();

			String sourceM = extract(sourceMethod);
			String targetM = extract(targetMethod);

			PairOfMethod pmnew = new PairOfMethod(sourceCName, sourceM, targetCName, targetM, lineNo);
			if (sourceCName.equals(targetCName)) {
				addListOfM(pmnew, lineNo, listOfCMethodInvokes);
			}
			if (!sourceCName.equals(targetCName)) {
				addListOfM(pmnew, lineNo, listOfMethodInvokes);
				addmethodDeps(sourceCName, targetCName);
			}
		}
	}

	// 分析SpecialInvoke
	private void analysisSpecialInvoke(Value useValue, SootClass sClass, SootMethod sMethod, int lineNo) {

		JSpecialInvokeExpr jSpecialInvokeExpr = (JSpecialInvokeExpr) useValue;
		String sourceCName = sClass.getName();
		SootClass targetClass = jSpecialInvokeExpr.getMethod().getDeclaringClass();
		String targetCName = targetClass.getName();

		if (isApplicationClass(targetCName) && !isSuper(sClass, targetClass)) {
			String sourceMethod = sMethod.toString();
			String targetMethod = jSpecialInvokeExpr.getMethod().toString();

			String sourceM = extract(sourceMethod);
			String targetM = extract(targetMethod);

			PairOfMethod pmnew = new PairOfMethod(sourceCName, sourceM, targetCName, targetM, lineNo);
			if (sourceCName.equals(targetCName)) {
				addListOfM(pmnew, lineNo, listOfCMethodInvokes);
			}
			if (!sourceCName.equals(targetCName)) {
				addListOfM(pmnew, lineNo, listOfMethodInvokes);
				addmethodDeps(sourceCName, targetCName);
			}
		}
	}

	// 分析DynamicInvoke
	private void analysisDynamicInvoke(Value useValue, SootClass sClass, SootMethod sMethod, int lineNo) {

		JDynamicInvokeExpr jDynamicInvokeExpr = (JDynamicInvokeExpr) useValue;
		String sourceCName = sClass.getName();
		SootClass targetClass = jDynamicInvokeExpr.getMethod().getDeclaringClass();
		String targetCName = targetClass.getName();

		if (isApplicationClass(targetCName) && !isSuper(sClass, targetClass)) {
			String sourceMethod = sMethod.toString();
			String targetMethod = jDynamicInvokeExpr.getMethod().toString();

			String sourceM = extract(sourceMethod);
			String targetM = extract(targetMethod);

			PairOfMethod pmnew = new PairOfMethod(sourceCName, sourceM, targetCName, targetM, lineNo);
			if (sourceCName.equals(targetCName)) {
				addListOfM(pmnew, lineNo, listOfCMethodInvokes);
			}
			if (!sourceCName.equals(targetCName)) {
				addListOfM(pmnew, lineNo, listOfMethodInvokes);
				addmethodDeps(sourceCName, targetCName);
			}
		}
	}

	// 分析InterfaceInvoke
	private void analysisInterfaceInvoke(Value useValue, SootClass sClass, SootMethod sMethod, int lineNo) {

		JInterfaceInvokeExpr jInterfaceInvokeExpr = (JInterfaceInvokeExpr) useValue;
		String sourceCName = sClass.getName();
		SootClass targetClass = jInterfaceInvokeExpr.getMethod().getDeclaringClass();
		String targetCName = targetClass.getName();

		if (isApplicationClass(targetCName) && !isSuper(sClass, targetClass)) {
			String sourceMethod = sMethod.toString();
			String targetMethod = jInterfaceInvokeExpr.getMethod().toString();

			String sourceM = extract(sourceMethod);
			String targetM = extract(targetMethod);

			PairOfMethod pmnew = new PairOfMethod(sourceCName, sourceM, targetCName, targetM, lineNo);
			if (sourceCName.equals(targetCName)) {
				addListOfM(pmnew, lineNo, listOfCMethodInvokes);
			}
			if (!sourceCName.equals(targetCName)) {
				addListOfM(pmnew, lineNo, listOfMethodInvokes);
				addmethodDeps(sourceCName, targetCName);
			}
		}
	}

	// 分析VirtualInvoke
	private void analysisVirtualInvoke(Value useValue, SootClass sClass, SootMethod sMethod, int lineNo) {

		JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) useValue;
		String sourceCName = sClass.getName();
		SootClass targetClass = jVirtualInvokeExpr.getMethod().getDeclaringClass();
		String targetCName = targetClass.getName();

		if (isApplicationClass(targetCName) && !isSuper(sClass, targetClass)) {
			String sourceMethod = sMethod.toString();
			String targetMethod = jVirtualInvokeExpr.getMethod().toString();

			String sourceM = extract(sourceMethod);
			String targetM = extract(targetMethod);

			PairOfMethod pmnew = new PairOfMethod(sourceCName, sourceM, targetCName, targetM, lineNo);

			if (sourceCName.equals(targetCName)) {
				// 存入listOfMethod 方法调用列表
				addListOfM(pmnew, lineNo, listOfCMethodInvokes);
			}
			if (!sourceCName.equals(targetCName)) {
				// 存入listOfCMethod 同一类中方法调用列表
				addListOfM(pmnew, lineNo, listOfMethodInvokes);
				// 存入listOfM 方法依赖列表
				addmethodDeps(sourceCName, targetCName);
			}
		}
	}

	// 加入到方法依赖列表listOfM中
	private void addmethodDeps(String sourceCName, String targetCName) {
		if (this.listOfM.containsKey(sourceCName)) {
			Map<String, Integer> map = this.listOfM.get(sourceCName);
			if (map.containsKey(targetCName)) {
				int i = map.get(targetCName) + 1;
				map.put(targetCName, i);
			} else {
				map.put(targetCName, 1);
			}
			this.listOfM.put(sourceCName, map);
		} else {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put(targetCName, 1);
			this.listOfM.put(sourceCName, map);
		}
	}

	// 将pmnew存入列表中
	private void addListOfM(PairOfMethod pmnew, int lineNo, List<PairOfMethod> listOfM) {
		if (listOfM.size() != 0) {
			for (PairOfMethod pm : listOfM) {
				if (pmnew.toString().equals(pm.toString())) {
					pm.addnum();
					pm.addLineNo(lineNo);
					return;
				}
			}
			listOfM.add(pmnew);
		} else {
			listOfM.add(pmnew);
		}
	}

	// 判断sClass和targetClass是否存在继承关系
	private boolean isSuper(SootClass sClass, SootClass targetClass) {
		boolean is = true;
		String sCName = sClass.getName();
		String targetCName = targetClass.getName();
		if (!sClass.getSuperclass().getName().equals(targetCName)
				&& !targetClass.getSuperclass().getName().equals(sCName)) {
			is = false;
		}
		return is;
	}

	/*
	 * 获取源方法的关键部分 sourceMethod格式为public int getNumberWaitingUp(int) 或者 private
	 * synchronized void travel() 或者 public void summonElevatorUp(int,
	 * newSim.Person) targetMethod格式为<newSim.ElevatorController: int
	 * getNumberWaitingUp(int)>
	 */
	private String extract(String Method) {
		String[] strs = Method.split(" ");
		String m = "";
		for (int i = 2; i < strs.length; i++) {
			m += strs[i] + " ";
		}
		m = m.substring(0, m.length() - 2);
		return m;
	}

	// 判断某类是否在类列表中
	private boolean isApplicationClass(String ClassName) {
		boolean is = false;
		for (SootClass c : listOfClasses) {
			if (c.getName().equals(ClassName)) {
				is = true;
				break;
			}
		}
		return is;
	}

	private int isInvokeExpr(Value useValue) {
		int no = 0;
		if (useValue instanceof JVirtualInvokeExpr)
			no = 1;
		if (useValue instanceof JInterfaceInvokeExpr)
			no = 2;
		if (useValue instanceof JDynamicInvokeExpr)
			no = 3;
		if (useValue instanceof JSpecialInvokeExpr)
			no = 4;
		if (useValue instanceof JStaticInvokeExpr)
			no = 5;
		return no;

	}

	// 为SootClass设立编号
	private void setClassNo() {
		classNames = new String[listOfClasses.size()];
		int i = 0;
		for (SootClass sc : listOfClasses) {
			classNames[i] = sc.getName();
			// Log.logInfo(i+":" + sc.getName());
			i++;
		}
	}

	// 获取类的编号列表
	public String[] getClassNo() {
		return this.classNames;
	}

	private static boolean ignore(SootClass sootClass) {
		String className = sootClass.getName();
		boolean ignore = false;
		ignore = ignore || className.startsWith("com.sun.");
		ignore = ignore || className.startsWith("com.javax.tools.doclets.");
		ignore = ignore || className.startsWith("com.jrockit.mc.");
		ignore = ignore || className.startsWith("java.");
		ignore = ignore || className.startsWith("javafx.");
		ignore = ignore || className.startsWith("javax.");
		ignore = ignore || className.startsWith("jdk.");
		ignore = ignore || className.startsWith("sun.");
		ignore = ignore || className.startsWith("sunw.");

		ignore = ignore || className.startsWith("com.oracle.");
		ignore = ignore || className.startsWith("oracle.jrockit.jfr.");

		ignore = ignore || className.startsWith("netscape.javascript.");
		ignore = ignore || className.startsWith("netscape.security.");

		ignore = ignore || className.startsWith("org.apache.commons.");
		ignore = ignore || className.startsWith("org.apache.derby.");
		ignore = ignore || className.startsWith("org.apache.felix.");
		ignore = ignore || className.startsWith("org.apache.jasper.");
		ignore = ignore || className.startsWith("org.apache.lucene.");

		ignore = ignore || className.startsWith("com.ibm.icu.");
		ignore = ignore || className.startsWith("org.eclipse.");
		ignore = ignore || className.startsWith("org.glassfish.jsp.api.");
		ignore = ignore || className.startsWith("org.ietf.jgss.");
		ignore = ignore || className.startsWith("org.jcp.xml.dsig.internal.");
		ignore = ignore || className.startsWith("org.netbeans.");
		ignore = ignore || className.startsWith("org.omg.");
		ignore = ignore || className.startsWith("org.openide.");
		ignore = ignore || className.startsWith("org.osgi.");
		ignore = ignore || className.startsWith("org.relaxng.datatype.");
		ignore = ignore || className.startsWith("org.sat4j.");
		ignore = ignore || className.startsWith("org.w3c.dom.");
		ignore = ignore || className.startsWith("org.xml.sax.");

		return ignore;
	}
}
