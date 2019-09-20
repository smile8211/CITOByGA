package chpkim;

import java.util.Date;

public class Employee {
	private String firstname;
	private String lastname;
	private PhoneNumber phonenumber;
	private String[] emails;
	private int age;
	private Date dob;
	private Boolean male;
	private Employee boss;


	public Employee(String firstname, String lastname) {
		this.firstname = firstname;
		this.lastname = lastname;
	}

	public void setPhoneNumber(PhoneNumber phonenumber) {
		this.phonenumber = phonenumber;
	}
	
	public void setEmails(String[] emails) {
		this.emails = emails;
	}

	public void setAge(int age)
	{
		this.age = age;
	}
	
	public void setDOB(Date dob)
	{
		this.dob = dob;
	}
	
	public void setMale(Boolean male)
	{
		this.male = male;
	}
	
	public void setBoss(Employee p)
	{
		this.boss = boss;
	}
}
