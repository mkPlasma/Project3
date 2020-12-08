package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * CCSweepAndPrune.java
 * 
 * Sweep and prune collision checking algorithm.
 * 
 * @author Lucas
 *
 */

public class CCSweepAndPrune extends CollisionChecker{

	public CCSweepAndPrune(ArrayList<Collider> colliders){
		super(colliders);
		xIntervals = new ArrayList<ColliderInterval>();
	}

	private int numColliders = 0;

	private ArrayList<ColliderInterval> xIntervals;

	public ArrayList<Collision> checkCollisions(){

		ArrayList<Collision> collisions = new ArrayList<Collision>();

		//when colliders are removed, delete all collider intervals and add again
		if (numColliders > colliders.size()){
			ArrayList<ColliderInterval> toDelete = new ArrayList<ColliderInterval>();
			for (ColliderInterval CI : xIntervals){
				if (!colliders.contains(CI.collider)){
					toDelete.add(CI);
				}
			}
			for (ColliderInterval CI : toDelete){
				xIntervals.remove(CI);
			}

		}
		//when colliders added, add to xIntervals
		if (numColliders < colliders.size()){
			for (int i = numColliders; i < colliders.size(); i++){
				Collider curCol = colliders.get(i);
				ColliderInterval xLower = new ColliderInterval(curCol.getAABB().lowerBoundX, curCol, true);
				ColliderInterval xUpper = new ColliderInterval(curCol.getAABB().upperBoundX, curCol, false);

				xIntervals.add(xLower);
				xIntervals.add(xUpper);
				numColliders++;
			}
		}

		//update collider bounds
		for (ColliderInterval CI : xIntervals){
			if (CI.start){
				CI.interval = CI.collider.getAABB().lowerBoundX;
			}
			else {
				CI.interval = CI.collider.getAABB().upperBoundX;
			}
		}

		//sort interval list
		InsertionSort(xIntervals);

		//find overlapping bounds on x axis
		Set<ColliderInterval> xMinsFound = new HashSet<ColliderInterval>();
		//Set<Collision> xOverlaps = new HashSet<Collision>();
		for (ColliderInterval curInt : xIntervals){
			if (curInt.start){
				xMinsFound.add(curInt);
				
				for(ColliderInterval CI : xMinsFound){
					Collision newCollision = new Collision(curInt.collider, CI.collider);
					if (curInt != CI && newCollision.collider1.getAABB().isOverlappingY(newCollision.collider2.getAABB())){
						collisions.add(newCollision);
					}
				}
			}
			else {
				ColliderInterval toDelete = new ColliderInterval(0, null, true);
				for (ColliderInterval CI : xMinsFound){
					if (CI.collider.equals(curInt.collider)){
						toDelete = CI;
					}
				}
				xMinsFound.remove(toDelete);
			}
		}
		return  collisions;
	}

	private void InsertionSort(ArrayList<ColliderInterval> intervalList){
		for (int i = 1; i < intervalList.size(); i++){
			ColliderInterval key = intervalList.get(i);
			int j = i - 1;
			while (j >= 0 && key.interval < intervalList.get(j).interval){
				intervalList.set(j + 1, intervalList.get(j));
				j--;
			}
			intervalList.set(j + 1, key);
		}
	}
	
	public ArrayList<ColliderInterval> getIntervals(){
		return xIntervals;
	}
}