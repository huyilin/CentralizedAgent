package template;

import logist.simulation.Vehicle;
import java.util.HashMap;

public class Encode{
	
	public HashMap<cAction, cAction> nextActions = new HashMap<cAction, cAction>();
	public HashMap<Vehicle, cAction> firstActions = new HashMap<Vehicle, cAction>();
	public HashMap<cAction, Integer> Time = new HashMap<cAction, Integer>();
	public HashMap<cAction, Vehicle> conductedBy = new HashMap<cAction, Vehicle>();
}
