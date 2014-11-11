package template;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.task.Task;
import logist.plan.Plan;
import logist.topology.Topology;
import logist.topology.Topology.City;
import logist.simulation.Vehicle;

public class CSP{
	
	public void CSP(List<Vehicle> vehicles, TaskSet tasks, Topology topology){
		
	}
	
	public Encode Initialize(List<Vehicle> vehicles, TaskSet tasks){
		Plan initA;
		List<Plan> plan = new ArrayList<Plan>();
		Encode aEncode = new Encode();
		cAction act_pickup;
		cAction act_delivery;
		
		City current;
		
		
		int count = 0;

		
		
		
		cAction temp = new cAction();
		
		for(Task task : tasks) {
			act_pickup = new cAction();
			act_delivery = new cAction();
			
			act_pickup.task = task;
			act_pickup.type = 0;
			act_delivery.task = task;
			act_delivery.type = 1;
			
			aEncode.nextActions.put(act_pickup, act_delivery);						// next action of pickup is delivery
			
			if(temp.task != null) {
				aEncode.nextActions.put(temp, act_pickup); 	// Store last loop's delivery with this loops's pickup 
			}
			
			temp = new cAction();
			temp.task = task;
			temp.type = 1;
			
			aEncode.firstActions.put(vehicles.get(0), act_pickup);		// action is assigned to vehicle using round robin
			
			aEncode.Time.put(act_pickup,2*count + 1);			// time sequence for pickup task
			aEncode.Time.put(act_delivery,2*count + 2);			// time sequence for delivery task = corresponding pickup + 1
			count++;
			
			aEncode.carriedBy.put(act_pickup, vehicles.get(0));
		}
		aEncode.nextActions.put(act_delivery, null);
				
		return aEncode;
	}
	
	public Encode SLS(List<Vehicle> vehicles, Encode aVector, TaskSet tasks){
		Encode aOld, aNew;
		HashSet <Encode> newNeighbors = new HashSet<Encode>();
		aNew = aVector;
		int iteration = 0;
		
		while(iteration <= 10000){
			aOld = aNew;
			newNeighbors = ChooseNeighbors(vehicles, aOld);
			aNew = LocalChoice(newNeighbors, vehicles, tasks);
			
			iteration++;
		}
		
		return aNew; 
	}
	public HashSet<Encode> ChooseNeighbors(List<Vehicle> vehicles, Encode aVector){
		HashSet<Encode> aSet = new HashSet<Encode>();

		int randomInt;
		Task t;
		Encode aChangeV, aChangeT;
		int length;
		Vehicle currentVehicle;
		
		List<cAction> actionList = new ArrayList<cAction>();
		
		while(true) {
			Random randomGenerator = new Random();
			randomInt = randomGenerator.nextInt(vehicles.size());
			currentVehicle = vehicles.get(randomInt);
			if (aVector.firstActions.get(currentVehicle) != null){
				break;
			}
		}
		for (Vehicle v : vehicles) {
			if (!v.equals(currentVehicle) ) {
				t = aVector.firstActions.get(currentVehicle).task;
				if (t.weight <= v.capacity()) {
					aChangeV = ChangingVehicle(aVector, currentVehicle, v);
					aSet.add(aChangeV);
				}
			}
		}
		
		length = 0;
		
		cAction a = aVector.firstActions.get(currentVehicle);
		actionList.add(aVector.firstActions.get(currentVehicle));
		
		while (a != null){
			a = aVector.nextActions.get(a);
			actionList.add(a);
			length++;
		}
		
		if (length >= 2){
			for(int id1 = 0; id1 < actionList.size()-1; id1++) {
				for (int id2 = id1 + 1; id2 < actionList.size(); id2++) {
					aChangeT = ChangingTaskOrder(aVector, currentVehicle, id1, id2);
					aSet.add(aChangeT);
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
	
	public Encode ChangingVehicle(Encode aVector, Vehicle vi, Vehicle vj){
		
		Encode changed = new Encode(aVector);
		cAction p1 = aVector.firstActions.get(vi); //get vi's first Action
		
		if(aVector.nextActions.get(p1).task.equals(p1.task)) {
			cAction p2 = aVector.nextActions.get(p1);
			changed.firstActions.put(vi, aVector.nextActions.get(p2));
			changed.nextActions.put(p2, aVector.firstActions.get(vj));
			changed.firstActions.put(vj, p1);
		} else {
			cAction p2 = p1;
			cAction p2Pre = p1;
			
			while (true) {
				p2 = aVector.nextActions.get(p2Pre);
				if(p2.task.equals(p1.task)) {
					break;
				} else {
					p2Pre = p2;
				}
			}
			changed.firstActions.put(vi, aVector.nextActions.get(p1));
			changed.nextActions.put(p2Pre, aVector.nextActions.get(p2));
			changed.firstActions.put(vj, p1);
			changed.nextActions.put(p1, p2);
			changed.nextActions.put(p2, aVector.firstActions.get(vj));
		}
//		UpdateTime();
//		UpdateTime();
		
		return changed;
	}
	
	public Encode ChangingTaskOrder(Encode aVector, Vehicle v, int id1, int id2){
		
	}
	
	public double computeCost(Encode neighbor, List<Vehicle> vehicles, TaskSet tasks){
		double costTask = 0;
		double costVehicle = 0;
		double cost = 0;
		
		cAction firstAct, nextAct;
		City currentCity;
		
		for (Vehicle v : vehicles){
			firstAct = neighbor.firstActions.get(v);
			cost = cost + v.getCurrentCity().distanceTo(firstAct.task.pickupCity) * v.costPerKm();
			
			nextAct = neighbor.nextActions.get(firstAct);
			currentCity = firstAct.task.pickupCity;
			
			while(nextAct != null){
				if (nextAct.type == 0){   				// next action is pickup
					cost = cost + currentCity.distanceTo(nextAct.task.pickupCity) * v.costPerKm();
					currentCity = nextAct.task.pickupCity;
					nextAct = neighbor.nextActions.get(nextAct);
				}
				else{									// next action is delivery
					cost = cost + currentCity.distanceTo(nextAct.task.deliveryCity) * v.costPerKm();
					currentCity = nextAct.task.deliveryCity;
					nextAct = neighbor.nextActions.get(nextAct);
				}
			}
		}
		
		return cost;
	}
	public List<Plan> computePlan(Encode optimalA, List<Vehicle> vehicles, TaskSet tasks){
		
		List<Plan> plans = new ArrayList<Plan>();

		for (Vehicle v : vehicles){
			City current = v.getCurrentCity();
			Plan plan = new Plan(current);
			cAction firstAct, nextAct;
			
			firstAct = optimalA.firstActions.get(v);
			plan.appendMove(firstAct.task.pickupCity);								// move to first city
			plan.appendPickup(firstAct.task);
			
			nextAct = optimalA.nextActions.get(firstAct);
			
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
	
	public boolean Overload(Encode pendingA, List<Vehicle> vehicles){       // capacity < 0 returns true
		
		cAction firstAct, nextAct;
		int remainingCapacity;
		
		for (Vehicle v : vehicles){			
			firstAct = pendingA.firstActions.get(v);
			
			if (firstAct == null)
				break;
			
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
		}
		return false;
	}
}
