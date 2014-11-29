package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.task.Task;
import logist.plan.Plan;
import logist.topology.Topology.City;

public class CSP{
	public static final int PICKUP = 0;
	public static final int DELIVERY = 1;
	private List<Vehicle> vehicles; 
	private TaskSet tasks;
	
	public CSP(List<Vehicle> vehicles, TaskSet tasks) {
		this.vehicles = vehicles;
		this.tasks = tasks;
	}
	
	public Encode Initialize(){
		
		HashMap<Vehicle, Integer> capacities= new HashMap<Vehicle, Integer> ();
		HashMap<Vehicle, cAction> lastAction = new HashMap<Vehicle, cAction> ();
		Encode aEncode = new Encode();
		
		
		for(Vehicle vehicle : vehicles) {
			capacities.put(vehicle, vehicle.capacity());
		}

		Random randomGenerator = new Random();

		for(Task task : tasks) {
			while (true) {
				int rand = randomGenerator.nextInt(vehicles.size()); 
				if(vehicles.get(rand).capacity() >= task.weight) {
					Vehicle v = vehicles.get(rand);
					cAction act_pickup = new cAction();
					cAction act_delivery = new cAction();
					
					act_pickup.task = task;
					act_pickup.type = 0;
					act_delivery.task = task;
					act_delivery.type = 1;
					
					if(aEncode.firstActions.get(v) == null) {
						aEncode.firstActions.put(v, act_pickup);
					}
					
					if(lastAction.get(v) != null) {
						aEncode.nextActions.put(lastAction.get(v), act_pickup);
					} 
					aEncode.nextActions.put(act_pickup, act_delivery);
					lastAction.put(v, act_delivery);
				}
				break;
			}
		}
		return aEncode;
	}
	
	public Encode SLS(Encode aVector){
		Encode aOld, aNew;
		HashSet <Encode> newNeighbors = new HashSet<Encode>();
		aNew = aVector;
		int iteration = 0;
		Random randomGenerator = new Random();

		while(iteration < 10000){
			aOld = aNew;
			newNeighbors = ChooseNeighbors(aOld);
			int samplespace = randomGenerator.nextInt(100);
			if (samplespace <= 40) {
				aNew = LocalChoice(newNeighbors, vehicles, tasks);
			}
			iteration++;
		}
		
		return aNew; 
	}
	
	public HashSet<Encode> ChooseNeighbors(Encode aVector){
		
		HashSet<Encode> aSet = new HashSet<Encode>();
		Vehicle currentVehicle = null;
		List<cAction> actionList = new ArrayList<cAction>();
		int limit = 200;
		
		while(limit > 0) {
			actionList = new ArrayList<cAction>();
			Random randomGenerator = new Random();
			while(true) {
				int randomInt = randomGenerator.nextInt(vehicles.size());
				currentVehicle = vehicles.get(randomInt);
				if (aVector.firstActions.get(currentVehicle) != null){
					break;
				}
			}
			
			cAction action = aVector.firstActions.get(currentVehicle);
			
			while (action != null){
				actionList.add(action);
				action = aVector.nextActions.get(action);
			}
			
			for (Vehicle v : vehicles) {
				if (!v.equals(currentVehicle) ) {
					Encode aChangeV = ChangeVehicle(aVector, currentVehicle, v, actionList);
					if(aChangeV != null) {
						aSet.add(aChangeV);
					}
				}
			}
			limit --;
		}
		
//		System.out.println("Finished change vehicle");
		
		if (actionList.size() >= 2){
			for(int id1 = 0; id1 < actionList.size()-1; id1++) {
				for (int id2 = id1 + 1; id2 < actionList.size(); id2++) {
					if(actionList.get(id2).task.equals(actionList.get(id1).task) && actionList.get(id1).type == PICKUP) {
						break;
					}
					Encode aChangedT = ChangeTaskOrder(aVector, currentVehicle, id1, id2, actionList);
					if(aChangedT != null) {
						aSet.add(aChangedT);			
					}

				}
			}
		}
		return aSet;
	}
	
	public Encode LocalChoice(HashSet<Encode> aSet, List<Vehicle> vehicles, TaskSet tasks){
		
		double optimalCost = 0;
		double tempCost;

		Encode optimal = null;
		for(Encode neighbor : aSet) {  
			optimal = neighbor;
			optimalCost = computeCost(neighbor);	          // set the first neighbor as the optimal solution	
			break;
		}
	
		
		for(Encode neighbor : aSet) {                         // compute the optimal cost and optimal solution 
			tempCost = computeCost(neighbor);
			if (tempCost < optimalCost){
				optimalCost = tempCost;
				optimal = neighbor;
			}
		}
		return optimal;
		
	}
	
	private int findFinalPre(Encode aVector, Vehicle vi, List<cAction> actionList) {
		for(int i = actionList.size() -1; i > 0 ; i--) {
			if(actionList.get(i).type == PICKUP) {
				return i;
			}
		}
		return 0;
	}
	
	public Encode ChangeVehicle(Encode aVector, Vehicle vi, Vehicle vj, List<cAction> actionList){
		
		Encode changed = new Encode(aVector);
		int finalPick = findFinalPre(aVector, vi, actionList);
		cAction p1 = actionList.get(finalPick);
		cAction d1;
		if(finalPick == 0) {
			if(aVector.nextActions.get(p1).task.equals(p1.task)) {
				d1 = aVector.nextActions.get(p1);
				cAction d1Post = aVector.nextActions.get(d1);
				changed.firstActions.put(vi, d1Post);
			} else {
				d1 = p1;
				cAction d1Pre = p1;
				cAction p1Post = aVector.nextActions.get(p1);
				while (true) {
					d1 = aVector.nextActions.get(d1Pre);
					if(d1.task.equals(p1.task)) {
						break;
					} else {
						d1Pre = d1;
					}
				}
				cAction d1Post = aVector.nextActions.get(d1);
				changed.firstActions.put(vi, p1Post);
				changed.nextActions.put(d1Pre, d1Post);
			}
		} else{
			if(aVector.nextActions.get(p1).task.equals(p1.task)) {
				d1 = aVector.nextActions.get(p1);
				cAction d1Post = aVector.nextActions.get(d1);
				cAction p1Pre = actionList.get(finalPick - 1);
				changed.nextActions.put(p1Pre, d1Post);
			} else {
				d1 = p1;
				cAction d1Pre = p1;
				cAction p1Pre = actionList.get(finalPick - 1);
				cAction p1Post = aVector.nextActions.get(p1);
				while (true) {
					d1 = aVector.nextActions.get(d1Pre);
					if(d1.task.equals(p1.task)) {
						break;
					} else {
						d1Pre = d1;
					}
				}
				cAction d1Post = aVector.nextActions.get(d1);
				changed.nextActions.put(p1Pre, p1Post);
				changed.nextActions.put(d1Pre, d1Post);
			}
		}
		
		cAction vjtail = changed.firstActions.get(vj);
		while (true) {
			if(changed.nextActions.get(vjtail) == null) {
				break;
			}
			vjtail = changed.nextActions.get(vjtail);
		}
		
		if(changed.firstActions.get(vj) == null) {
			changed.firstActions.put(vj, p1);
		} else {
			changed.nextActions.put(vjtail, p1);
		}
		
		changed.nextActions.put(p1, d1);
		changed.nextActions.put(d1, null);
		
		if(p1.task.weight > vj.capacity()) {
			return null;
		} else return changed;
	}
	
	public Encode ChangeTaskOrder(Encode aVector, Vehicle v, int id1, int id2, List<cAction> actionList) {
		/*Situation 1*/
		cAction a1 = actionList.get(id1);
		cAction a2 = actionList.get(id2);
		Encode reEncode;
		
		if(a1.type == PICKUP && a2.type == PICKUP) {
			reEncode = ChangeOrder(aVector, v, id1, id2, actionList);
			if(a2.task.weight > a1.task.weight) {
				if(this.overload(reEncode, v)) {
					return null;
				} 
			}
			return reEncode;
		}	
		
		/*Situation 2*/
		else if(a1.type == PICKUP && a2.type == DELIVERY) {
			for(int i = id1 + 1; i < id2; i++) {
				if(actionList.get(i).task.equals(a2.task)) {
					return null;
				}
			}
			reEncode = ChangeOrder(aVector, v, id1, id2, actionList);
			return reEncode;
		}
		
		/*Situation 3*/
		else if(a1.type == DELIVERY && a2.type == PICKUP) {
			reEncode = ChangeOrder(aVector, v, id1, id2, actionList);
			if(this.overload(reEncode, v)) {
				return null;
			}
			return reEncode;
		}	
		
		/*Situation 4*/		
		else if(a1.type == DELIVERY && a2.type == DELIVERY) {
			for(int i = id1; i < id2; i++) {
				if(actionList.get(i).task.equals(a2.task)) {
					return null;
				}
			}
			reEncode = ChangeOrder(aVector, v, id1, id2, actionList);
			if(a2.task.weight < a1.task.weight) {
				if(this.overload(reEncode, v)) {
					return null;
				}
			}
			return reEncode;
		} 
		return null;
	}
	
	
	public Encode ChangeOrder(Encode aVector, Vehicle v, int id1, int id2, List<cAction> actionList) {
		
		Encode reEncode = new Encode(aVector);
		cAction a1 = actionList.get(id1);
		cAction a2 = actionList.get(id2);
		cAction a1Post = actionList.get(id1 + 1);
		cAction a2Pre = actionList.get(id2 - 1);
		cAction a2Post;
		if(id2 == actionList.size() - 1 ){
			a2Post = null;
		} else {
			a2Post = actionList.get(id2 + 1);
		}
		
		if(id1 + 1 == id2) {
			if(id1 == 0) {
				reEncode.firstActions.put(v, a2);
				reEncode.nextActions.put(a2, a1);
				reEncode.nextActions.put(a1, a2Post);
			} else {
				cAction a1Pre = actionList.get(id1 -1);
				reEncode.nextActions.put(a1Pre, a2);
				reEncode.nextActions.put(a2, a1);
				reEncode.nextActions.put(a1, a2Post);
			} 
		} else{
			if(id1 == 0) {
				reEncode.firstActions.put(v, a2);
				reEncode.nextActions.put(a2, a1Post);
				reEncode.nextActions.put(a2Pre, a1);
				reEncode.nextActions.put(a1, a2Post);
			} else {
				cAction a1Pre = actionList.get(id1 -1);
				reEncode.nextActions.put(a1Pre, a2);
				reEncode.nextActions.put(a2, a1Post);
				reEncode.nextActions.put(a2Pre, a1);
				reEncode.nextActions.put(a1, a2Post);
			}
		}
		return reEncode;
	}
	
	public double computeCost(Encode neighbor) {
		
		double cost = 0;
		
//		if(neighbor.firstActions.get(vehicles.get(3)) != null ) {
//			this.displayEncode(neighbor);
//		}
		
		for (Vehicle v : vehicles){
			
			cAction nextAct = neighbor.firstActions.get(v);
			City currentCity = v.getCurrentCity();
			while(nextAct != null) {
				if (nextAct.type == 0) {							// next action is pickup				
					cost = cost + currentCity.distanceTo(nextAct.task.pickupCity) * v.costPerKm();
					currentCity = nextAct.task.pickupCity;
					nextAct = neighbor.nextActions.get(nextAct);
				}
				else{														// next action is delivery
					cost = cost + currentCity.distanceTo(nextAct.task.deliveryCity) * v.costPerKm();			
					currentCity = nextAct.task.deliveryCity;
					nextAct = neighbor.nextActions.get(nextAct);
				}
			}
		}
		
//		System.out.println(cost);
		return cost;
	}
	
	public List<Plan> computePlan(Encode optimalA){
		
		List<Plan> plans = new ArrayList<Plan>();

		for (Vehicle v : vehicles){
			City current = v.getCurrentCity();
			Plan plan = new Plan(current);
			cAction firstAct, nextAct;
			
			firstAct = optimalA.firstActions.get(v);
			
			if(firstAct != null ) {
				for(City city : current.pathTo(firstAct.task.pickupCity)) {
					plan.appendMove(city);
				}
				plan.appendPickup(firstAct.task);
			
			
				nextAct = optimalA.nextActions.get(firstAct);							// next action of first action
				current = firstAct.task.pickupCity;
				while(nextAct != null){
					if (nextAct.type == 0){       									
						for(City city : current.pathTo(nextAct.task.pickupCity)) {
							plan.appendMove(city);	
						}
						plan.appendPickup(nextAct.task);
						current = nextAct.task.pickupCity;
						nextAct = optimalA.nextActions.get(nextAct); 
					}
					else{																// next action is delivery
						for(City city : current.pathTo(nextAct.task.deliveryCity)) {
							plan.appendMove(city);	
						}
						plan.appendDelivery(nextAct.task);
						current = nextAct.task.deliveryCity;
						nextAct = optimalA.nextActions.get(nextAct);
					}
				}
				plans.add(plan);
			} else {
				plans.add(plan);
			}
		}
		return plans;
	}
	
	public void UpdateTime(){
		
	}
	
	public boolean overload(Encode pendingA, Vehicle v){       // capacity < 0 returns true
		
		cAction firstAct, nextAct;
		int remainingCapacity;
			
		firstAct = pendingA.firstActions.get(v);
		
		if (firstAct == null)
			return false;
		
		remainingCapacity = v.capacity() - firstAct.task.weight;	    // capacity after first action
		
		if (remainingCapacity < 0)										// overload happens
			return true;
		
		nextAct = pendingA.nextActions.get(firstAct);					
		while (nextAct != null) {										// examine a vehicle's capacity
			if (nextAct.type == 0)           	// next action is pickup
				remainingCapacity = remainingCapacity - nextAct.task.weight;
			else															// next action is delivery
				remainingCapacity = remainingCapacity + nextAct.task.weight;
			if (remainingCapacity < 0)									// overload happens
				return true;
			else
				nextAct = pendingA.nextActions.get(nextAct);
		}
		return false;
	}
	
	
	public void displayEncode(Encode encode ) {
		for(Vehicle v : this.vehicles) {
			cAction a = encode.firstActions.get(v);
			System.out.print("vehicle" + v.id() + ":" );
			while (a != null) {
				if(a.type == PICKUP) {
					System.out.print("p" + a.task.id + "----->");
				} else {
					System.out.print("d" + a.task.id + "----->");	
				}
				a = encode.nextActions.get(a);
			}
			System.out.println("");
		}
		System.out.println("");
	}
}
