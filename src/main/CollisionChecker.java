package main;

import java.util.ArrayList;

/**
 * CollisionChecker.java
 * 
 * Abstract class to inherit from when creating a collision checking algorithm.
 * 
 * @author Daniel
 *
 */
public abstract class CollisionChecker{
	
	protected ArrayList<Collider> colliders;
	
	public CollisionChecker(ArrayList<Collider> colliders){
		this.colliders = colliders;
	}
	
	public abstract ArrayList<Collision> checkCollisions();
}
