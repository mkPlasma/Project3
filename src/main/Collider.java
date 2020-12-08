package main;

/**
 * Collider.java
 * 
 * Square collider object with position, size, velocity, and AABB.
 * 
 * @author Daniel
 *
 */
public class Collider{
	
	private float x, y;
	private final float size;
	
	private float velX, velY;
	
	private AABB aabb;
	
	private boolean collided;
	
	
	public Collider(float x, float y, float size, float velX, float velY){
		this.x = x;
		this.y = y;
		this.size = size;
		this.velX = velX;
		this.velY = velY;
		
		aabb = new AABB();
		
		updateAABB();
	}
	
	public void update(){
		
		// Update position
		x += velX;
		y += velY;
		
		updateAABB();
		
		// Reset collided bool
		collided = false;
	}
	
	private void updateAABB(){

		// Update AABB
		aabb.lowerBoundX = x - (size / 2);
		aabb.lowerBoundY = y - (size / 2);
		
		aabb.upperBoundX = x + (size / 2);
		aabb.upperBoundY = y + (size / 2);
	}

	
	public void setX(float x){
		this.x = x;
	}
	
	public float getX(){
		return x;
	}
	
	public void setY(float y){
		this.y = y;
	}
	
	public float getY(){
		return y;
	}
	
	public float getSize(){
		return size;
	}
	
	public void setVelX(float velX){
		this.velX = velX;
	}
	
	public float getVelX(){
		return velX;
	}
	
	public void setVelY(float velY){
		this.velY = velY;
	}
	
	public float getVelY(){
		return velY;
	}
	
	public AABB getAABB(){
		return aabb;
	}
	
	public void setCollided(){
		collided = true;
	}
	
	public boolean collided(){
		return collided;
	}
}
