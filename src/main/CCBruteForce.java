package main;

import java.util.ArrayList;

/**
 * CCBruteForce.java
 * 
 * Brute-force collision checking algorithm.
 * 
 * @author Daniel
 *
 */
public class CCBruteForce extends CollisionChecker{

	public CCBruteForce(ArrayList<Collider> colliders){
		super(colliders);
	}

	public ArrayList<Collision> checkCollisions(){
		
		ArrayList<Collision> collisions = new ArrayList<Collision>();
		
		// Iterate all collider pairs
		for(int i = 0; i < colliders.size() - 1; i++){
			for(int j = i + 1; j < colliders.size(); j++){
				
				Collider c1 = colliders.get(i);
				Collider c2 = colliders.get(j);
				
				// Check AABB overlap and add collision if overlapping
				if(c1.getAABB().isOverlapping(c2.getAABB()))
					collisions.add(new Collision(c1, c2));
			}
		}
		
		return collisions;
	}
}
