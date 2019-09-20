package chpkim;

import java.util.Date;

public class Placeholder {
	private String field1;
	private String field2;
	private PhoneNumber field3;
	private String[] field4;
	private int field5;

	public Placeholder(String firstname, String lastname) {
		this.field1 = firstname;
		this.field2 = lastname;
	}

	public void setPhoneNumber(PhoneNumber phonenumber) {
		this.field3 = phonenumber;
	}
	
	public void setEmails(String[] emails) {
		this.field4 = emails;
	}

	public void setAge(int age)
	{
		this.field5 = age;
	}	
}
