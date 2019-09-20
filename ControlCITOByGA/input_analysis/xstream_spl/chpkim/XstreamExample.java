package chpkim;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

import tests.XstreamTEST;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.io.xml.StaxDriver;


public class XstreamExample {
	public static int COMMON_COUNT;
	public static int VARIABLE_COUNT;
	
	
	public static int PLACEHOLDER_COUNT;
	public static boolean BASE___ = true;
	public static boolean TREE_STRUCTURE___ = true;
	public static boolean CLASS_ALIAS___ = true;
	public static boolean FIELD_ALIAS___ = true;
	public static boolean OMIT_FIELD___ = true;
	public static boolean IMPLICIT_ARRAY___ = true;
	public static boolean ATTRIBUTES___ = true;
	public static boolean BOOLEAN_CONVERTER___ = true;	
	
	private static Employee boss;
	
	private static StringBuffer splBuffer;
	
	/*
	public static void main(String args[])
	{
		generateSPLChoice();
		
		System.setProperty("java.vm.vendor", "NASA");
		System.setProperty("java.specification.version", "n/a");

		XStream xstream = new XStream(new StaxDriver());
		xstream.autodetectAnnotations(true);

		DummyClass dc = new DummyClass();

		if(CLASS_ALIAS___)
		{
			xstream.alias("dumb", DummyClass.class);			
		}
		
		String xml = xstream.toXML(dc);
				
	} */  
	
	
	private static Placeholder createPlaceholder(int i)
	{
		Placeholder cp = new Placeholder("CommonFirstName" + i, "CommonLastName" + i);
		cp.setPhoneNumber(new PhoneNumber(i+i+i, i+i+i+"-"+i+i+i+i));
		cp.setEmails(new String[]{"normal@gmail.com" + i, "backup@email.com" + i});
		cp.setAge(i);
		
		return cp;
	}
	
	private static Employee createEmployee(int i)
	{
		Employee p= new Employee("VariableFirstName" + i, "VariableLastName" + i);
		p.setPhoneNumber(new PhoneNumber(i+i+i, i+i+i+"-"+i+i+i+i));
		p.setEmails(new String[]{"normal@gmail.com" + i, "backup@email.com" + i});
		p.setAge(i);
		try {
			p.setDOB(new SimpleDateFormat("EEE, d MMM yyyy HH:mm").parse("Wed, 4 Jul 2001 12:08"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		p.setMale(i % 2 == 0);
		p.setBoss(boss);
		
		return p;
	}

	public static void splStart___()
	{
		splBuffer = new StringBuffer();
		System.setProperty("java.vm.vendor", "NASA");
		System.setProperty("java.specification.version", "n/a");
	}
	
	public static void test0()
	{
		List<Object> objects = new Vector<Object>();
				
		
		for(int i = 0; i < (10000 * XstreamTEST.SCALE); i++)
			objects.add(new DummyClass());
		
		XStream xstream = new XStream(new StaxDriver());
		xstream.autodetectAnnotations(true);

		alias(xstream, "dummyy", DummyClass.class);					
		splPrint___(xstream.toXML(objects));
	}
	
	public static void test1()
	{
		List<Object> objects = new Vector<Object>();
		boss = new Employee("BossFirstName", "BossLastName");
		objects.add(boss);		
		for(int i = 0; i < PLACEHOLDER_COUNT; i++)
		{
			objects.add(createPlaceholder(i));
		}
		for(int i = 0; i < VARIABLE_COUNT; i++)
		{
			objects.add(createEmployee(i));			
		}
		for(int i = 0; i < PLACEHOLDER_COUNT; i++)
		{
			objects.add(createPlaceholder(i));
		}
		
		XStream xstream = new XStream(new StaxDriver());
		xstream.autodetectAnnotations(true);
		
		alias(xstream, "p", Employee.class);
		aliasField(xstream, "pn", Employee.class, "phonenumber");
		omitField(xstream, Employee.class, "firstname");		
		addImplicitArray(xstream, Employee.class, "emails");
		useAttributeFor(xstream, Employee.class, "age");
		registerBooleanConverter(xstream);
	
		splPrint___(xstream.toXML(objects));
	}
	
	public static void test2()
	{
		//<Data		
		List<Object> objects = new Vector<Object>();
		boss = new Employee("BossFirstName", "BossLastName");
		objects.add(boss);		
		for(int i = 0; i < PLACEHOLDER_COUNT; i++)
		{
			objects.add(createPlaceholder(i));
		}
		for(int i = 0; i < VARIABLE_COUNT; i++)
		{
			objects.add(createEmployee(i));			
		}
		for(int i = 0; i < PLACEHOLDER_COUNT; i++)
		{
			objects.add(createPlaceholder(i));
		}
		//>
		
		//<Conversion
		XStream xstream = new XStream(new StaxDriver());
		xstream.autodetectAnnotations(true);
		
		setNoReferences(xstream);
		alias(xstream, "p", Employee.class);
		aliasField(xstream, "pn", Employee.class, "phonenumber");
		omitField(xstream, Employee.class, "firstname");		
		addImplicitArray(xstream, Employee.class, "emails");
		useAttributeFor(xstream, Employee.class, "age");
		registerBooleanConverter(xstream);
		
		splPrint___(xstream.toXML(objects));
	}
	
	public static void setNoReferences(XStream xstream)
	{
		if(XstreamTEST.SINGLETON.get_TREE_STRUCTURE___())
		{
			xstream.setMode(XStream.NO_REFERENCES);
		}						
	}
	
	public static void alias(XStream xstream, String alias, Class clazz)
	{
		if(XstreamTEST.SINGLETON.get_CLASS_ALIAS___())
		{
			xstream.alias(alias, clazz);			
		}
	}
	
	public static void aliasField(XStream xstream, String alias, Class clazz, String attr)
	{
		if(XstreamTEST.SINGLETON.get_FIELD_ALIAS___())
		{
			xstream.aliasField(alias, clazz, attr);
		}
	}
	
	public static void omitField(XStream xstream, Class clazz, String attr)
	{
		if(XstreamTEST.SINGLETON.get_OMIT_FIELD___())
		{
			xstream.omitField(Employee.class, "firstname");
		}		
	}
	
	public static void addImplicitArray(XStream xstream, Class clazz, String attr)
	{
		if(XstreamTEST.SINGLETON.get_IMPLICIT_ARRAY___())
		{
			xstream.addImplicitArray(clazz, attr);
		}
	}
	
	public static void useAttributeFor(XStream xstream, Class clazz, String attr)
	{
		if(XstreamTEST.SINGLETON.get_ATTRIBUTES___())
		{
			xstream.useAttributeFor(clazz, attr);			
		}
	}
	
	public static void registerBooleanConverter(XStream xstream)
	{
		if(XstreamTEST.SINGLETON.get_BOOLEAN_CONVERTER___())
		{
			xstream.registerConverter(BooleanConverter.YES_NO);
		}
	}
	
	private static void splPrint___(String s)
	{
		splBuffer.append(s + "\n");
	}
	
	public static void splEnd___()
	{
	//	System.out.println(splBuffer.toString());		
	}
}
