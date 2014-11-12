package template;

import logist.simulation.Vehicle;

import java.util.HashMap;
import java.util.Map;

public class Encode{
	
	public HashMap<cAction, cAction> nextActions = new HashMap<cAction, cAction>();
	public HashMap<Vehicle, cAction> firstActions = new HashMap<Vehicle, cAction>();
	public HashMap<cAction, Integer> Time = new HashMap<cAction, Integer>();
	public HashMap<cAction, Vehicle> carriedBy = new HashMap<cAction, Vehicle>();
	
	public <A, B> void copy(HashMap<A, B> a, HashMap<A,B> b) {
		for(Map.Entry<A, B> entry : a.entrySet()) {
			b.put(entry.getKey(), entry.getValue());
		}
	}
	
	public Encode() {
		
	}
	
	public Encode(Encode input) {
		this.copy(input.nextActions, this.nextActions);
		this.copy(input.firstActions, this.firstActions);
		this.copy(input.Time, this.Time);
		this.copy(input.carriedBy, this.carriedBy);	
	}
	
}
