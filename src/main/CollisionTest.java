package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

/**
 * CollisionTest.java
 * 
 * Contains the main update and draw code for the program.
 * 
 * @author Daniel
 *
 */
public class CollisionTest{
	
	// Offsets to correct drawing position
	private final int DRAW_OFFSET_X = 8;
	private final int DRAW_OFFSET_Y = 31;
	
	// Bounds for collider region
	private final int REGION_SIZE_X = 10000;
	private final int REGION_SIZE_Y = 10000;
	
	// Collider properties
	private final float COLLIDER_SIZE = 80;
	private final float COLLIDER_VEL_MIN = 2;
	private final float COLLIDER_VEL_MAX = 3;
	
	// Number of colliders to start with
	private final int COLLIDER_INITIAL_COUNT = 1000;
	
	// Number of colliders to add/remove at a time
	private final int COLLIDER_INCREMENT_COUNT = 100;
	
	// Zoom factor and range
	private final float ZOOM_FACTOR = 1.2f;
	private final float ZOOM_MIN = 0.05f;
	private final float ZOOM_MAX = 5f;
	
	
	// Frame time for update/draw loop
	private long frameTime;
	
	// Time taken for checking collisions
	private float updateTimeMs;
	
	
	// Colliders
	private ArrayList<Collider> colliders;
	
	// Collision checkers
	private CollisionChecker ccCurrent;
	private CollisionChecker ccBruteForce;
	private CollisionChecker ccSweepAndPrune;
	private CollisionChecker ccBoundingVolumeHierarchy;
	
	// Current algorithm name
	String algName;
	
	
	
	// Random number generator
	private Random random;
	
	
	// Camera position / zoom level
	private float camX, camY, zoom;
	
	// Mouse position
	private int mx, my;
	
	// Window half width / half height
	private int winHw, winHh;
	
	private int colorMode;
	private int rTime;
	
	
	public void init(){
		
		// Initialize RNG
		random = new Random();
		
		// Initialize collider ArrayList and add initial colliders
		colliders = new ArrayList<Collider>(COLLIDER_INITIAL_COUNT);
		addColliders(COLLIDER_INITIAL_COUNT);
		
		// Initialize collision checker objects
		ccBruteForce				= new CCBruteForce(colliders);
		ccSweepAndPrune				= new CCSweepAndPrune(colliders);
		ccBoundingVolumeHierarchy	= new CCBoundingVolumeHierarchy(colliders);
		
		ccCurrent = ccBruteForce;
		algName = "Brute Force";
		
		
		// Set default zoom
		zoom = ZOOM_MIN;
	}
	
	public void update(){
		updateInputs();
		updateColliders();
	}
	
	private void updateInputs(){
		
		// Zoom
		int mouseScroll = InputListener.getMouseScroll();

		if(mouseScroll < 0)			zoom *= Math.pow(ZOOM_FACTOR, -mouseScroll);
		else if(mouseScroll > 0)	zoom /= Math.pow(ZOOM_FACTOR, mouseScroll);
		
		// Constrain zoom
		if(zoom >= ZOOM_MAX)	zoom = ZOOM_MAX;
		if(zoom <= ZOOM_MIN)	zoom = ZOOM_MIN;
		
		
		// Update mouse position, keeping old position
		int mxPrev = mx;
		int myPrev = my;
		
		mx = InputListener.getMouseX();
		my = InputListener.getMouseY();
		
		// Camera dragging
		if(InputListener.isMouseDown()){
			camX += (mxPrev - mx) / zoom;
			camY += (myPrev - my) / zoom;
			
			// Constrain to region boundary
			if(camX < -REGION_SIZE_X / 2f)	camX = -REGION_SIZE_X / 2f;
			if(camX > REGION_SIZE_X / 2f)	camX = REGION_SIZE_X / 2f;
			if(camY < -REGION_SIZE_Y / 2f)	camY = -REGION_SIZE_Y / 2f;
			if(camY > REGION_SIZE_Y / 2f)	camY = REGION_SIZE_Y / 2f;
		}
		
		
		// Set algorithm
		if(InputListener.isKeyTyped(0)){
			ccCurrent = ccBruteForce;
			algName = "Brute Force";
		}
		else if(InputListener.isKeyTyped(1)){
			ccCurrent = ccSweepAndPrune;
			algName = "Sweep and Prune";
		}
		else if(InputListener.isKeyTyped(2)){
			ccCurrent = ccBoundingVolumeHierarchy;
			algName = "Bounding Volume Hierarchy";
		}
		
		// Add / remove colliders
		if(InputListener.isKeyTyped(3))
			addColliders(COLLIDER_INCREMENT_COUNT);
		else if(InputListener.isKeyTyped(4))
			removeColliders(COLLIDER_INCREMENT_COUNT);

		if(InputListener.isKeyTyped(5)){
			colorMode++;
			
			if(colorMode > 1)
				colorMode = 0;
		}
	}
	
	private void updateColliders(){
		
		// Update all collider positions and check collision with region boundary
		for(Collider c : colliders){
			c.update();
			
			// Check boundary collision and set velocity to 'bounce' object off boundary
			if(c.getX() - (c.getSize() / 2) < -REGION_SIZE_X / 2f || c.getX() + (c.getSize() / 2) > REGION_SIZE_X / 2f)
				c.setVelX(-c.getVelX());
			
			if(c.getY() - (c.getSize() / 2) < -REGION_SIZE_Y / 2f || c.getY() + (c.getSize() / 2) > REGION_SIZE_Y / 2f)
				c.setVelY(-c.getVelY());
		}
		
		// Check collisions using selected collision checker
		long timeBefore = System.nanoTime();
		ArrayList<Collision> collisions = ccCurrent.checkCollisions();
		
		// Calculate collision check time
		updateTimeMs = (System.nanoTime() - timeBefore) / 1000000f;
		
		
		// Resolve collisions using a basic elastic collision formula
		// Not strictly necessary since we only care about the collision detection itself but
		// it makes it much more fun
		for(Collision c : collisions){

			Collider c1 = c.collider1;
			Collider c2 = c.collider2;
			
			// Collider velocities
			float vx1 = c1.getVelX();
			float vy1 = c1.getVelY();
			float vx2 = c2.getVelX();
			float vy2 = c2.getVelY();
			
			// Collider velocity magnitudes
			float v1 = (float)Math.hypot(vx1, vy1);
			float v2 = (float)Math.hypot(vx2, vy2);
			
			// Collider directions
			float dir1 = (float)Math.atan2(c1.getVelY(), c1.getVelX());
			float dir2 = (float)Math.atan2(c2.getVelY(), c2.getVelX());
			
			// Angle between colliders
			float ang = (float)Math.atan2(c2.getY() - c1.getY(), c2.getX() - c1.getX());
			
			// Set new velocities
			c1.setVelX((float)(v2 * Math.cos(dir2 - ang) * Math.cos(ang) + v1 * Math.sin(dir1 - ang) * Math.sin(ang)));
			c1.setVelY((float)(v2 * Math.cos(dir2 - ang) * Math.sin(ang) + v1 * Math.sin(dir1 - ang) * Math.cos(ang)));
			
			c2.setVelX((float)(v1 * Math.cos(dir1 - ang) * Math.cos(ang) + v2 * Math.sin(dir2 - ang) * Math.sin(ang)));
			c2.setVelY((float)(v1 * Math.cos(dir1 - ang) * Math.sin(ang) + v2 * Math.sin(dir2 - ang) * Math.cos(ang)));
			
			// Force conservation of momentum
			float m = (float)((v1 + v2) / (Math.hypot(c1.getVelX(), c1.getVelY()) + Math.hypot(c2.getVelX(), c2.getVelY())));
			c1.setVelX(c1.getVelX() * m);
			c1.setVelY(c1.getVelY() * m);
			c2.setVelX(c2.getVelX() * m);
			c2.setVelY(c2.getVelY() * m);
		}
	}
	
	private void addColliders(int num){
		for(int i = 0; i < num; i++){
			
			// Get random position, adjusting for collider size
			float x = (random.nextFloat() * (REGION_SIZE_X - COLLIDER_SIZE * 2)) - REGION_SIZE_X / 2f + COLLIDER_SIZE;
			float y = (random.nextFloat() * (REGION_SIZE_Y - COLLIDER_SIZE * 2)) - REGION_SIZE_Y / 2f + COLLIDER_SIZE;
			
			// Get random velocity and direction
			float vel = (random.nextFloat() * (COLLIDER_VEL_MAX - COLLIDER_VEL_MIN)) + COLLIDER_VEL_MIN;
			float dir = random.nextFloat() * 2 * (float)Math.PI;
			
			// Add collider
			colliders.add(new Collider(x, y, COLLIDER_SIZE, vel * (float)Math.cos(dir), vel * (float)Math.sin(dir)));
		}
	}
	
	private void removeColliders(int num){
		
		for(int i = 0; i < num; i++){
			colliders.remove(colliders.size() - 1);
			
			if(colliders.isEmpty())
				return;
		}
	}
	
	public void draw(Graphics2D g, int winWidth, int winHeight){
		
		// Note: Drawing with BufferStrategy offsets the drawing positions, so the top left corner is at (8, 31) rather than (0, 0)
		// This behavior may not be platform-independent
		
		// Window half width / half height for camera space conversion
		winHw = winWidth / 2;
		winHh = winHeight / 2;
		
		// Fill background
		g.setColor(Color.BLACK);
		g.fillRect(DRAW_OFFSET_X, DRAW_OFFSET_Y, winWidth, winHeight);
		
		// Draw colliders
		g.setColor(Color.RED);
		
		for(int i = 0; i < colliders.size(); i++){
			
			Collider c = colliders.get(i);
			
			// Rainbow uwu
			if(colorMode == 1)
				g.setColor(Color.getHSBColor((float)(rTime / 1000f + Math.atan2(c.getY(), c.getX()) / Math.PI), 1, 1));
			
			// Get zoomed size and set to 1 if <1 so colliders will always be visible 
			int size = (int)(c.getSize() * zoom);
			size = size == 0 ? 1 : size;
			
			g.fillRect(toCameraSpaceX(c.getX()) - (size / 2), toCameraSpaceY(c.getY()) - (size / 2), size, size);
		}
		

		// Draw region boundary
		g.setColor(Color.GRAY);
		g.drawRect(toCameraSpaceX(-REGION_SIZE_X / 2f), toCameraSpaceY(-REGION_SIZE_Y / 2f), (int)(REGION_SIZE_X * zoom), (int)(REGION_SIZE_Y * zoom));
		
		
		// Draw info
		g.setColor(Color.WHITE);
		g.drawString("FPS: " + (frameTime == 0 ? "" : (String.format("%.2f", 1000000000d / frameTime))), DRAW_OFFSET_X + 10, DRAW_OFFSET_Y + 15);
		g.drawString("Colliders: " + colliders.size(), DRAW_OFFSET_X + 10, DRAW_OFFSET_Y + 35);
		g.drawString("Algorithm: " + algName, DRAW_OFFSET_X + 10, DRAW_OFFSET_Y + 55);
		g.drawString("Collision update time (ms): " + String.format("%.2f", updateTimeMs), DRAW_OFFSET_X + 10, DRAW_OFFSET_Y + 75);
		
		rTime++;
		
	}
	
	private int toCameraSpaceX(float x){
		return DRAW_OFFSET_X + winHw + (int)((x - camX) * zoom);
	}
	
	private int toCameraSpaceY(float y){
		return DRAW_OFFSET_X + winHh + (int)((y - camY) * zoom);
	}
	
	public void setFrameTime(long frameTime){
		this.frameTime = frameTime;
	}
}
