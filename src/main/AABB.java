package main;

/**
 * AABB.java
 * 
 * Axis-aligned bounding box used to store collider hitbox information.
 * 
 * @author Daniel
 *
 */
public class AABB{
	public float lowerBoundX;
	public float lowerBoundY;
	
	public float upperBoundX;
	public float upperBoundY;
	
	public boolean isOverlapping(AABB aabb){
		if(	lowerBoundX > aabb.upperBoundX || lowerBoundY > aabb.upperBoundY ||
			aabb.lowerBoundX > upperBoundX || aabb.lowerBoundY > upperBoundY)
			return false;
		
		return true;
	}
	
	public boolean isOverlappingY(AABB aabb){
		if(lowerBoundY > aabb.upperBoundY || aabb.lowerBoundY > upperBoundY)
			return false;
		
		return true;
	}
}
