package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import main.CCBoundingVolumeHierarchy.Node;

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
	private final int REGION_SIZE_X = 100000;
	private final int REGION_SIZE_Y = 100000;
	//private final int REGION_SIZE_INC = 1000;
	
	// Collider properties
	private final float COLLIDER_SIZE = 80;
	private final float COLLIDER_VEL_MIN = 2;
	private final float COLLIDER_VEL_MAX = 3;
	
	// Number of colliders to start with
	private final int COLLIDER_INITIAL_COUNT = 1000;
	private final int COLLIDER_MIN_COUNT = 2;
	
	// Number of colliders to add/remove at a time
	private final int COLLIDER_INCREMENT_COUNT = 10000;
	private final int COLLIDER_INCREMENT_MULT = 2;
	private final boolean COLLIDER_INC_USE_MULT = false;
	
	// Zoom factor and range
	private final float ZOOM_FACTOR = 1.2f;
	private final float ZOOM_MIN = 0.001f;
	private final float ZOOM_MAX = 5f;
	
	
	// Frame time for update/draw loop
	private long frameTime;
	
	// Time taken for checking collisions
	private float updateTimeMs;
	
	
	// Colliders
	private ArrayList<Collider> colliders;
	
	// Collision checkers
	private CollisionChecker ccCurrent;
	private CCBruteForce ccBruteForce;
	private CCSweepAndPrune ccSweepAndPrune;
	private CCBoundingVolumeHierarchy ccBoundingVolumeHierarchy;
	
	private final String[] ALG_NAMES = {"Brute Force", "Sweep and Prune", "Bounding Volume Hierarchy"};
	
	// Current algorithm name
	String algName;
	
	
	
	// Random number generator
	private Random random;
	
	// Region bounds
	private int regionSizeX;
	private int regionSizeY;
	
	
	// Camera position / zoom level
	private float camX, camY, zoom;
	
	// Mouse position
	private int mx, my;
	
	// Window half width / half height
	private int winHw, winHh;
	
	// Draw algorithm workings
	boolean debugDraw;
	
	private int colorMode;
	private int rTime;
	
	
	public void init(){
		
		// Initialize RNG
		random = new Random();
		
		// Initialize collider ArrayList
		colliders = new ArrayList<Collider>(COLLIDER_INITIAL_COUNT);
		
		// Initialize collision checker objects
		ccBruteForce				= new CCBruteForce(colliders);
		ccSweepAndPrune				= new CCSweepAndPrune(colliders);
		ccBoundingVolumeHierarchy	= new CCBoundingVolumeHierarchy(colliders);
		
		// Set default algorithm
		ccCurrent = ccBoundingVolumeHierarchy;
		algName = ALG_NAMES[2];
		
		// Set default region size
		regionSizeX = REGION_SIZE_X;
		regionSizeY = REGION_SIZE_Y;
		
		// Add initial colliders
		addColliders(COLLIDER_INITIAL_COUNT);
		
		// Set default zoom
		zoom = ZOOM_MIN;
		
		debugDraw = false;
	}
	
	public void update(){
		updateInputs();
		updateColliders();
		
		if(ccCurrent == ccBoundingVolumeHierarchy)
			ccBoundingVolumeHierarchy.update();
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
			if(camX < -regionSizeX / 2f)	camX = -regionSizeX / 2f;
			if(camX > regionSizeX / 2f)		camX = regionSizeX / 2f;
			if(camY < -regionSizeY / 2f)	camY = -regionSizeY / 2f;
			if(camY > regionSizeY / 2f)		camY = regionSizeY / 2f;
		}
		
		
		// Set algorithm
		if(InputListener.isKeyTyped(0)){
			ccCurrent = ccBruteForce;
			algName = ALG_NAMES[0];
		}
		else if(InputListener.isKeyTyped(1)){
			ccCurrent = ccSweepAndPrune;
			algName = ALG_NAMES[1];
		}
		else if(InputListener.isKeyTyped(2)){
			ccCurrent = ccBoundingVolumeHierarchy;
			algName = ALG_NAMES[2];
		}
		
		// Add / remove colliders
		if(InputListener.isKeyTyped(3)){
			
			if(!COLLIDER_INC_USE_MULT)
				addColliders(COLLIDER_INCREMENT_COUNT);
			else
				addColliders((colliders.size() * COLLIDER_INCREMENT_MULT) - colliders.size());
			
			//regionSizeX += REGION_SIZE_INC;
			//regionSizeY += REGION_SIZE_INC;
		}
		else if(InputListener.isKeyTyped(4)){
			
			if(!COLLIDER_INC_USE_MULT)
				removeColliders(COLLIDER_INCREMENT_COUNT);
			else
				removeColliders(colliders.size() - (colliders.size() / COLLIDER_INCREMENT_MULT));
			
			//regionSizeX -= REGION_SIZE_INC;
			//regionSizeY -= REGION_SIZE_INC;
			
			// Minimum count
			if(colliders.size() < COLLIDER_MIN_COUNT)
				addColliders(COLLIDER_MIN_COUNT - colliders.size());
		}
		
		if(InputListener.isKeyTyped(5))
			debugDraw = !debugDraw;
		
		if(InputListener.isKeyTyped(6)){
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
			if(c.getX() - (c.getSize() / 2) < -regionSizeX / 2f || c.getX() + (c.getSize() / 2) > regionSizeX / 2f)
				c.setVelX(-c.getVelX());
			
			if(c.getY() - (c.getSize() / 2) < -regionSizeY / 2f || c.getY() + (c.getSize() / 2) > regionSizeY / 2f)
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
			c1.setCollided();
			c2.setCollided();
			
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
			
			// Force conservation of momentum with slight damping
			float m = 0.98f * (float)((v1 + v2) / (Math.hypot(c1.getVelX(), c1.getVelY()) + Math.hypot(c2.getVelX(), c2.getVelY())));
			c1.setVelX(c1.getVelX() * m);
			c1.setVelY(c1.getVelY() * m);
			c2.setVelX(c2.getVelX() * m);
			c2.setVelY(c2.getVelY() * m);
			
			// Extra force to push stuck objects away from each other
			final float efm = 0.25f;
			c1.setVelX(c1.getVelX() - efm * (float)Math.cos(ang));
			c1.setVelY(c1.getVelY() - efm * (float)Math.sin(ang));
			c2.setVelX(c2.getVelX() + efm * (float)Math.cos(ang));
			c2.setVelY(c2.getVelY() + efm * (float)Math.sin(ang));
		}
	}
	
	private void addColliders(int num){
		for(int i = 0; i < num; i++){
			
			// Get random position, adjusting for collider size
			float x = (random.nextFloat() * (regionSizeX - COLLIDER_SIZE * 2)) - regionSizeX / 2f + COLLIDER_SIZE;
			float y = (random.nextFloat() * (regionSizeY - COLLIDER_SIZE * 2)) - regionSizeY / 2f + COLLIDER_SIZE;
			
			// Get random velocity and direction
			float vel = (random.nextFloat() * (COLLIDER_VEL_MAX - COLLIDER_VEL_MIN)) + COLLIDER_VEL_MIN;
			float dir = random.nextFloat() * 2 * (float)Math.PI;
			
			// Add collider
			Collider c = new Collider(x, y, COLLIDER_SIZE, vel * (float)Math.cos(dir), vel * (float)Math.sin(dir));
			colliders.add(c);
			ccBoundingVolumeHierarchy.add(c);
		}
		ccBoundingVolumeHierarchy.update();
	}
	
	private void removeColliders(int num){
		
		for(int i = 0; i < num; i++){
			if(colliders.isEmpty())
				return;
			
			ccBoundingVolumeHierarchy.remove(colliders.get(colliders.size() - 1));
			colliders.remove(colliders.size() - 1);
		}
		ccBoundingVolumeHierarchy.update();
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
		
		for(int i = 0; i < colliders.size(); i++){
			
			Collider c = colliders.get(i);
			
			switch(colorMode){
			case 0:
				g.setColor(c.collided() ? Color.YELLOW : Color.RED);
				break;
			
			case 1:
				// Rainbow uwu
				g.setColor(c.collided() ? Color.WHITE : Color.getHSBColor((float)(rTime / 1000f + Math.atan2(c.getY(), c.getX()) / Math.PI), 1, 1));
				break;
			}
			
			// Get zoomed size and set to 1 if <1 so colliders will always be visible 
			int size = (int)(c.getSize() * zoom);
			size = size == 0 ? 1 : size;
			
			g.fillRect(toCameraSpaceX(c.getX()) - (size / 2), toCameraSpaceY(c.getY()) - (size / 2), size, size);
		}
		
		
		// Debug draw
		if(debugDraw){
			if(ccCurrent == ccSweepAndPrune){
				
				// Draw x intervals
				ArrayList<ColliderInterval> intervals = ccSweepAndPrune.getIntervals();
				
				final int sizeY = 100;
				g.setColor(Color.BLUE);
				
				for(ColliderInterval i : intervals)
					g.fillRect(toCameraSpaceX(i.interval), toCameraSpaceY(regionSizeY / 2f), 1, sizeY);
			}
			else if(ccCurrent == ccBoundingVolumeHierarchy){
				Node root = ccBoundingVolumeHierarchy.getRoot();
				drawBVHTree(g, root);
			}
		}
		

		// Draw region boundary
		g.setColor(Color.GRAY);
		g.drawRect(toCameraSpaceX(-regionSizeX / 2f), toCameraSpaceY(-regionSizeY / 2f), (int)(regionSizeX * zoom), (int)(regionSizeY * zoom));
		
		
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
	
	private void drawBVHTree(Graphics2D g, Node node){
		
		// Draw margin AABB for leaf nodes
		AABB aabb = node.isLeaf() ? node.aabbMargin : node.aabb;
		g.setColor(node.isLeaf() ? Color.ORANGE : Color.BLUE);
		
		// Get sizes
		int sizeX = (int)((aabb.upperBoundX - aabb.lowerBoundX) * zoom);
		int sizeY = (int)((aabb.upperBoundY - aabb.lowerBoundY) * zoom);
		sizeX = sizeX == 0 ? 1 : sizeX;
		sizeY = sizeY == 0 ? 1 : sizeY;
		
		// Draw
		g.drawRect(toCameraSpaceX(aabb.lowerBoundX), toCameraSpaceY(aabb.lowerBoundY), sizeX, sizeY);
		
		
		// Draw children
		if(!node.isLeaf()){
			drawBVHTree(g, node.left);
			drawBVHTree(g, node.right);
		}
	}
	
	public void setFrameTime(long frameTime){
		this.frameTime = frameTime;
	}
}
