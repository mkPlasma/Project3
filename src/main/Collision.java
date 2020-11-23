package main;

/**
 * Collision.java
 * 
 * Collision object containing a pair of objects.
 * 
 * @author Daniel
 *
 */
public class Collision{
	
	public Collider collider1;
	public Collider collider2;
	
	public Collision(Collider collider1, Collider collider2){
		this.collider1 = collider1;
		this.collider2 = collider2;
	}
}
