package MinePumpSystem; 

import MinePumpSystem.Environment; 

public   class  MinePump {
	

	boolean pumpRunning = false;

	

	boolean systemActive = true;

	

	Environment env;

	

	public MinePump(Environment env) {
		super();
		this.env = env;
	}

	

	public void timeShift() {
		if (pumpRunning)
			env.lowerWaterLevel();
		if (systemActive)
			processEnvironment();
	}

	
	void  processEnvironment__before__highWaterSensor() {
		
	}

	
	public void  processEnvironment__role__highWaterSensor() {
		if (!pumpRunning && isHighWaterLevel()) {
			activatePump();
			processEnvironment__before__highWaterSensor();
		} else {
			processEnvironment__before__highWaterSensor();
		}
	}

	


	void 
processEnvironment__before__lowWaterSensor() {
    if (tests.MineTEST.SINGLETON.get_HIGHWATERSENSOR___()) {
        processEnvironment__role__highWaterSensor();
    } else {
        processEnvironment__before__highWaterSensor();
    }
}



	


	public void  processEnvironment__role__lowWaterSensor() {
		if (pumpRunning && isLowWaterLevel()) {
			deactivatePump();
		} else {
			processEnvironment__before__lowWaterSensor();
		}
	}

	

	void 
processEnvironment__before__methaneAlarm() {
    if (tests.MineTEST.SINGLETON.get_LOWWATERSENSOR___()) {
        processEnvironment__role__lowWaterSensor();
    } else {
        processEnvironment__before__lowWaterSensor();
    }
}



	

	public void  processEnvironment__role__methaneAlarm() {
		if (pumpRunning && isMethaneAlarm()) {
			deactivatePump();
		} else {
			processEnvironment__before__methaneAlarm();
		}
	}

	
	void
processEnvironment() {
    if (tests.MineTEST.SINGLETON.get_METHANEALARM___()) {
        processEnvironment__role__methaneAlarm();
    } else {
        processEnvironment__before__methaneAlarm();
    }
}



	

	void  activatePump__before__methaneQuery() {
		pumpRunning = true;
	}

	

	void  activatePump__role__methaneQuery() {
		if (!isMethaneAlarm()) {
			activatePump__before__methaneQuery();
		} else {
			//System.out.println("Pump not activated due to methane alarm");
		}
	}

	
	void
activatePump() {
    if (tests.MineTEST.SINGLETON.get_METHANEQUERY___()) {
        activatePump__role__methaneQuery();
    } else {
        activatePump__before__methaneQuery();
    }
}



	

	void deactivatePump() {
		pumpRunning = false;
	}

	
	
	boolean isMethaneAlarm() {
		return env.isMethaneLevelCritical();
	}

	

	@Override
	public String toString() {
		return "Pump(System:" + (systemActive?"On":"Off") + ",Pump:" + (pumpRunning?"On":"Off") +") " + env.toString(); 
	}

	
	
	private Environment getEnv() {
		return env;
	}

	
	
	boolean isHighWaterLevel() {
		return !env.isHighWaterSensorDry();
	}

	
	
	boolean isLowWaterLevel() {
		return !env.isLowWaterSensorDry();
	}

	
	public void stopSystem() {
		if (pumpRunning) {
			deactivatePump();
		}
		assert !pumpRunning;
		systemActive = false;
	}

	
	public void startSystem() {
		assert !pumpRunning;
		systemActive = true;
	}


}
