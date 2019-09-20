package defpackage;
import MinePumpSystem.Environment; 
import MinePumpSystem.MinePump; 

public   class  Actions {
	

	Environment env;

	
	public MinePump p;

	
	
	public Actions() {
		env = new Environment();
		p = new MinePump(env);
	}

	
	
	public void waterRise() {
		env.waterRise();
	}

	
	public void methaneChange() {
		env.changeMethaneLevel();
	}

	
	public void  stopSystem__before__stopCommand() {
		PL_Interface_impl.executedUnimplementedAction = true;
	}

	
	public void  stopSystem__role__stopCommand() {
		p.stopSystem();
	}

	
	public void
stopSystem() {
    if (tests.MineTEST.SINGLETON.get_STOPCOMMAND___()) {
        stopSystem__role__stopCommand();
    } else {
        stopSystem__before__stopCommand();
    }
}



	
	void  startSystem__before__startCommand() {
		PL_Interface_impl.executedUnimplementedAction = true;
	}

	
	void  startSystem__role__startCommand() {
		p.startSystem();
	}

	

	public void
startSystem() {
    if (tests.MineTEST.SINGLETON.get_STARTCOMMAND___()) {
        startSystem__role__startCommand();
    } else {
        startSystem__before__startCommand();
    }
}



	
	
	public void timeShift() {
		p.timeShift();
	}

	
	
	String getSystemState() {
		return p.toString();
	}


}
