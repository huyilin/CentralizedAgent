package template;

import java.util.ArrayList;
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
		Encode aEncode = new Encode();
		cAction act_pickup;
		cAction act_delivery = new cAction();
		int count = 0;
		cAction temp = new cAction();
		Vehicle v = vehicles.get(0);
		
		for(Task task : tasks) {
			act_pickup = new cAction();
			act_delivery = new cAction();
			
			act_pickup.task = task;
			act_pickup.type = 0;
			act_delivery.task = task;
			act_delivery.type = 1;
			
			if(aEncode.firstActions.get(v) == null) {
				aEncode.firstActions.put(v, act_pickup);
			}
			
			aEncode.nextActions.put(act_pickup, act_delivery);					// next action of pickup is delivery
			
			if(temp.task != null) {
				aEncode.nextActions.put(temp, act_pickup); 	// Store last loop's delivery with this loops's pickup 
			}
			
			temp = act_delivery;
			
					// all actions are assigned to one vehicle
			
			aEncode.Time.put(act_pickup,2*count + 1);			// time sequence for pickup task
			aEncode.Time.put(act_delivery,2*count + 2);			// time sequence for delivery task = corresponding pickup + 1
			count++;
			
			aEncode.carriedBy.put(act_pickup, v);
			aEncode.carriedBy.put(act_delivery, v);
		}
		
		aEncode.nextActions.put(act_delivery, null);
		return aEncode;
	}
	
	public Encode SLS(Encode aVector){
		Encode aOld, aNew;
		HashSet <Encode> newNeighbors = new HashSet<Encode>();
		aNew = aVector;
		int iteration = 0;
		
		while(iteration <= 10000){
			aOld = aNew;
			newNeighbors = ChooseNeighbors(aOld);
			
			aNew = LocalChoice(newNeighbors, vehicles, tasks);
			iteration++;
		}
		return aNew; 
	}
	
	public HashSet<Encode> ChooseNeighbors(Encode aVector){
		
		HashSet<Encode> aSet = new HashSet<Encode>();
		Vehicle currentVehicle;
		List<cAction> actionList = new ArrayList<cAction>();
		
		while(true) {
			Random randomGenerator = new Random();
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
		
		for(cAction a: actionList) {
			System.out.println(a.task);
		}
		
		System.out.println("here");
		
		for (Vehicle v : vehicles) {
			if (!v.equals(currentVehicle) ) {
				Task t = aVector.firstActions.get(currentVehicle).task;
				if (t.weight <= v.capacity()) {
					Encode aChangeV = ChangeVehicle(aVector, currentVehicle, v);
					if(aChangeV != null) {
						displayEncode(aChangeV);
						aSet.add(aChangeV);			
					}
				}
			}
		}
		
		if (actionList.size() >= 2){
			for(int id1 = 0; id1 < actionList.size()-1; id1++) {
				for (int id2 = id1 + 1; id2 < actionList.size(); id2++) {
					if(actionList.get(id2).task.equals(actionList.get(id1).task)) {
						break;
					} 
					Encode aChangedT = ChangeTaskOrder(aVector, currentVehicle, id1, id2, actionList);
					if(aChangedT != null) {
//						displayEncode(aChangedT);
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
			optimalCost = computeCost(neighbor, vehicles, tasks);	          // set the first neighbor as the optimal solution	
			break;
		}
		
		for(Encode neighbor : aSet) {                         // compute the optimal cost and optimal solution 
			tempCost = computeCost(neighbor, vehicles, tasks);
			if (optimalCost >= tempCost){
				optimalCost = tempCost;
				optimal = neighbor;
			}
		}
		return optimal;
		
	}
	
	public Encode ChangeVehicle(Encode aVector, Vehicle vi, Vehicle vj){
		
		Encode changed = new Encode(aVector);
		cAction p1 = aVector.firstActions.get(vi); //get vi's first Action
		
		if(aVector.nextActions.get(p1).task.equals(p1.task)) {
			cAction d1 = aVector.nextActions.get(p1);
			cAction p2 = aVector.nextActions.get(d1);
			changed.firstActions.put(vi, aVector.nextActions.get(cost = cost + v.getCurrentCity().distanceTo(firstAct.task.pickupCity) * v.costPerKm();p2));
			changed.nextActions.put(d1, aVector.firstActions.get(vj));
			changed.firstActions.put(vj, p1);
		} else {
			cAction d1 = p1;
			cAction d1Pre = p1;
			
			while (true) {
				d1 = aVector.nextActions.get(d1Pre);
				if(d1.task.equals(p1.task)) {
					break;
				} else {
					d1Pre = d1;
				}
			}
			changed.firstActions.put(vi, aVector.nextActions.get(p1));
			changed.nextActions.put(d1Pre, aVector.nextActions.get(d1));
			changed.firstActions.put(vj, p1);
			changed.nextActions.put(p1, d1);
			changed.nextActions.put(d1, aVector.firstActions.get(vj));
		}
//		UpdateTime();
//		UpdateTime();
		
		return changed;
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
			for(int i = id1 + 1; i <= id2; i++) {
				if(actionList.get(i).task.equals(a1.task)) {
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
		cAction a2Post = actionList.get(id2 + 1);
		
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
	
	public double computeCost(Encode neighbor, List<Vehicle> vehicles, TaskSet tasks){

		double cost = 0;
		
		cAction firstAct, nextAct;
		City currentCity;
		
		for (Vehicle v : vehicles){
			firstAct = neighbor.firstActions.get(v);
			cost = cost + v.getCurrentCity().distanceTo(firstAct.task.pickupCity) * v.costPerKm();
			
			nextAct = neighbor.nextActions.get(firstAct);					// next action of first action
			currentCity = firstAct.task.pickupCity;							// set current city to first city
			
			while(nextAct != null){
				if (nextAct.type == 0){   									// next action is pickup
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
		return cost;
	}
	
	public List<Plan> computePlan(Encode optimalA){
		
		List<Plan> plans = new ArrayList<Plan>();

		for (Vehicle v : vehicles){
			City current = v.getCurrentCity();
			Plan plan = new Plan(current);
			cAction firstAct, nextAct;
			
			firstAct = optimalA.firstActions.get(v);
			plan.appendMove(firstAct.task.pickupCity);								// move to first city
			plan.appendPickup(firstAct.task);
			
			nextAct = optimalA.nextActions.get(firstAct);							// next action of first action
			
			while(nextAct != null){
				
				if (nextAct.type == 0){       										// next action is pickup
					plan.appendMove(nextAct.task.pickupCity);
					plan.appendPickup(nextAct.task);
					nextAct = optimalA.nextActions.get(nextAct);
				}
				else{																// next action is delivery
					plan.appendMove(nextAct.task.deliveryCity);
					plan.appendDelivery(nextAct.task);
					nextAct = optimalA.nextActions.get(nextAct);
				}
			}
			plans.add(plan);
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
		while (nextAct != null){										// examine a vehicle's capacity
			if (pendingA.nextActions.get(firstAct).type == 0)           	// next action is pickup
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
				System.out.print("task" + a.task.id + "----->");
				a = encode.nextActions.get(a);
			}
			System.out.println("");
		}
		System.out.println("");
	}
}
